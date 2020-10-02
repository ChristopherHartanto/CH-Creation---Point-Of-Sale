package com.chcreation.pointofsale

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.chcreation.pointofsale.analytic.GenerateReport
import com.chcreation.pointofsale.checkout.PostCheckOutActivity
import com.chcreation.pointofsale.home.HomeFragment.Companion.active
import com.chcreation.pointofsale.login.LoginActivity
import com.chcreation.pointofsale.merchant.ManageMerchantActivity
import com.chcreation.pointofsale.model.*
import com.chcreation.pointofsale.presenter.AnalyticPresenter
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.presenter.MerchantPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_manage_merchant.*
import kotlinx.coroutines.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.File
import java.io.FileOutputStream
import java.lang.Runnable
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity(), MainView, GenerateReport {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var doubleBackToExitPressedOnce = false
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var presenter: Homepresenter
    private lateinit var merchantPresenter: MerchantPresenter
    private lateinit var analyticPresenter: AnalyticPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var view : View
    private lateinit var tvNavHeaderMerchantName : TextView
    private lateinit var ivNavHeader : ImageView
    private lateinit var tvNavHeaderFirstName : TextView
    private lateinit var tvNavHeaderMerchantMemberStatus : TextView
    private lateinit var tvUserName : TextView
    private lateinit var layoutNavHeaderDefaultImage: FrameLayout
    private lateinit var layoutNavHeader: LinearLayout

    companion object{
        var userList : MutableList<UserList> = mutableListOf()
        var userListName : MutableList<String> = mutableListOf()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        sharedPreference =  getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_check_out,R.id.nav_manage_product,R.id.nav_catalog,R.id.nav_customer,
                R.id.nav_transaction,R.id.nav_custom_receipt,R.id.nav_analytics,R.id.nav_activity_logs,
                R.id.nav_user_list,R.id.nav_merchant_list,R.id.nav_about
            ), drawerLayout
        )
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = Homepresenter(this,mAuth,mDatabase,this)
        merchantPresenter = MerchantPresenter(this,mAuth,mDatabase,this)
        analyticPresenter = AnalyticPresenter(this,mAuth,mDatabase,this)

        if (mAuth.currentUser == null || getMerchantCredential(this) == "" || getMerchantCode(this) == ""){
            logOut("Session End")
        }

        presenter.checkVersion {
            var currentVersionCode = ""
            try {
                currentVersionCode = packageManager.getPackageInfo(packageName,0).versionName.toString()
            }catch (e: java.lang.Exception){
                currentVersionCode = ""
                e.printStackTrace()
            }
            if (it.VERSION_NAME != packageManager.getPackageInfo(packageName,0).versionName.toString()
                && currentVersionCode != ""
                && it.VERSION_NAME  != ""){
                alert ("Please Update to Latest Version for Better Performance"){
                    title = "Update Available!"

                    yesButton {
                        openGooglePlay(this@MainActivity)
                    }

                    noButton {

                    }
                }.show()
            }
        }

        GlobalScope.launch {
            //presenter.retrieveUserLists()
            merchantPresenter.retrieveInvitation(encodeEmail(getEmail(this@MainActivity)))

        }

        presenter.retrieveSincere(){
            val editor = sharedPreference.edit()
            editor.putString(ESharedPreference.SINCERE.toString(),it.SINCERE)
            editor.apply()
        }

        presenter.getMerchant(){success, merchant ->
            if (!success)
                logOut("Session End")
            else{
                val editor = sharedPreference.edit()
                editor.putString(ESharedPreference.MEMBER_DEADLINE.toString(),merchant.MEMBER_DEADLINE)
                editor.apply()

                if (merchant.NAME != getMerchantName(this@MainActivity) || merchant.IMAGE != getMerchantImage(this@MainActivity)
                    || merchant.NO_TELP != getMerchantNoTel(this@MainActivity) || merchant.MEMBER_STATUS != getMerchantMemberStatus(this@MainActivity)
                    || merchant.COUNTRY != getCountry(this@MainActivity) || merchant.LANGUAGE != getLanguage(this@MainActivity))
                    logOut("Merchant Status Have Been Updated\nPlease Login Again!")
            }
        }
        presenter.getUserDetail(mAuth.currentUser!!.uid){
            if (it.ACTIVE != EStatusUser.ACTIVE.toString() || it.DEVICE_ID != getDeviceId(this@MainActivity)
                || getDeviceId(this@MainActivity) == "" || it.DEVICE_ID == ""){
                logOut("Session End")
            }
        }

        presenter.getAvailableMerchant(){success, availableMerchant ->
            if (!success)
                logOut("You Have Been Removed From This Merchant!")
            else if (availableMerchant != null && availableMerchant.USER_GROUP != getMerchantUserGroup(this@MainActivity))
                logOut("User Status Changed\nPlease Login Again!")
        }

        view = navView.getHeaderView(0)
        tvNavHeaderMerchantName = view.findViewById<TextView>(R.id.tvNavHeaderMerchantName)
        tvUserName = view.findViewById<TextView>(R.id.textView)
        tvNavHeaderFirstName = view.findViewById<TextView>(R.id.tvNavHeaderFirstName)
        ivNavHeader = view.findViewById<ImageView>(R.id.imageView)
        layoutNavHeaderDefaultImage = view.findViewById<FrameLayout>(R.id.layoutNavHeaderDefaultImage)
        layoutNavHeader = view.findViewById<LinearLayout>(R.id.layoutNavHeader)
        tvNavHeaderMerchantMemberStatus = view.findViewById<TextView>(R.id.tvNavHeaderMerchantMemberStatus)

        tvNavHeaderMerchantMemberStatus.text = if (getMerchantMemberStatus(this) == EMerchantMemberStatus.PREMIUM.toString())
            "PREMIUM" else "FREE TRIAL"
        tvNavHeaderMerchantName.text = getMerchantName(this)
        tvUserName.text = "${getName(this)} (${getMerchantUserGroup(this)})"

        layoutNavHeader.onClick {
            tvNavHeaderMerchantName.text = getMerchantName(this@MainActivity)
            layoutNavHeader.startAnimation(normalClickAnimation())
            startActivity<ManageMerchantActivity>()
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        if (getMerchantCode(this) != "")
            presenter.retrieveMerchant()
    }


    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            setResult(RESULT_CLOSE_ALL)

            super.onBackPressed()
            return
        }
        if (active){
            this.doubleBackToExitPressedOnce = true
            toast("Please click BACK again to exit")
        }else
            super.onBackPressed()

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_check_out -> findNavController(R.id.nav_host_fragment).navigate(R.id.nav_check_out)
            R.id.nav_customer -> findNavController(R.id.nav_host_fragment).navigate(R.id.nav_customer)
            R.id.nav_manage_product -> findNavController(R.id.nav_host_fragment).navigate(R.id.nav_manage_product)
            R.id.nav_transaction -> findNavController(R.id.nav_host_fragment).navigate(R.id.nav_transaction)
        }
        false
    }

    private fun logOut(message: String){
        mAuth.signOut()
        toast(message)
        removeAllSharedPreference(this)
        PostCheckOutActivity().clearCartData()
        startActivity<LoginActivity>()
        finish()
    }

    @ObsoleteCoroutinesApi
    private suspend fun transactionReport(transactions: MutableList<Transaction>, transactionCodeItems: MutableList<Int>,
                                          month: Int,
                                          year: Int,
                                          userCode: String) : String {
        return suspendCoroutine {ctx ->
            try{
                analyticPresenter.retrieveCustomer{ it ->
                    var customerList = mutableListOf<Customer>()

                    if (it.exists())
                        for (data in it.children){
                            val item = data.getValue(Customer::class.java)
                            if (item != null){
                                customerList.add(item)
                            }
                        }

                    val fileName = "Transaction Report.xlsx"
                    val path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

                    val fileOutputStream = FileOutputStream(File(path,"/$fileName"))

                    val workbook = XSSFWorkbook()
                    val sheet = workbook.createSheet("Transaction")
                    var row = sheet.createRow(0)
                    row.createCell(0).setCellValue("No")
                    row.createCell(1).setCellValue("Receipt")
                    row.createCell(2).setCellValue("Customer Name")
                    row.createCell(3).setCellValue("Cashier")
                    row.createCell(4).setCellValue("Created Date")
                    row.createCell(5).setCellValue("Updated Date")
                    row.createCell(6).setCellValue("Sub Total")
                    row.createCell(7).setCellValue("Discount")
                    row.createCell(8).setCellValue("Tax")
                    row.createCell(9).setCellValue("Total")
                    row.createCell(10).setCellValue("Amount Received")
                    row.createCell(11).setCellValue("Pending")
                    row.createCell(12).setCellValue("Changes")
                    row.createCell(13).setCellValue("Status")
                    row.createCell(14).setCellValue("Note")

                    GlobalScope.launch {
                        var count = 0
                        for ((index, data) in transactions.withIndex()) {
                            val transactionMonth = getMonth(data.CREATED_DATE.toString())
                            val transactionYear = getYear(data.CREATED_DATE.toString())
                            val currentYear = getCurrentYear()
                            val currentMonth = getCurrentMonth()

                            if (((transactionMonth) == currentMonth && month == 99) || ((transactionMonth) == month-1)
                                && ((transactionYear) == currentYear && year == 99 || (transactionYear) == year)
                                && (data.CREATED_BY.toString() == userCode || userCode == "")) {

                                tvMainGenerateReportProgress.text = "${(index * 100 / transactions.size)}%"

                                val discount = data.DISCOUNT!!
                                val tax = data.TAX!!
                                val totalPrice = data.TOTAL_PRICE!!
                                val totalOutstanding = data.TOTAL_OUTSTANDING!!

                                val totalPayment = totalPrice - discount + tax

                                row = sheet.createRow(count + 1)

                                row.createCell(0).setCellValue((count + 1).toString())
                                row.createCell(1)
                                    .setCellValue(receiptFormat(transactionCodeItems[index]))
                                if (data.CUST_CODE != "") {
                                    val customer = customerList.first { it.CODE == data.CUST_CODE }
                                    row.createCell(2)
                                        .setCellValue(customer.NAME)
                                }

                                val name = analyticPresenter.getUserName(data.UPDATED_BY.toString())
                                Log.d("Username", name.toString())
                                row.createCell(3).setCellValue(name)

                                row.createCell(4)
                                    .setCellValue(parseDateFormatFull(data.CREATED_DATE.toString()))
                                row.createCell(5)
                                    .setCellValue(parseDateFormatFull(data.UPDATED_DATE.toString()))

                                row.createCell(6).setCellValue(
                                    currencyFormat(
                                        getLanguage(this@MainActivity),
                                        getCountry(this@MainActivity)
                                    ).format(totalPrice)
                                )
                                row.createCell(7).setCellValue(
                                    currencyFormat(
                                        getLanguage(this@MainActivity),
                                        getCountry(this@MainActivity)
                                    ).format(data.DISCOUNT)
                                )
                                row.createCell(8).setCellValue(
                                    currencyFormat(
                                        getLanguage(this@MainActivity),
                                        getCountry(this@MainActivity)
                                    ).format(data.TAX)
                                )
                                row.createCell(9).setCellValue(
                                    currencyFormat(
                                        getLanguage(this@MainActivity),
                                        getCountry(this@MainActivity)
                                    ).format(totalPayment)
                                )

                                var totalPaid = 0F

                                val paymentData = analyticPresenter.retrieveTransactionListPayments(
                                    transactionCodeItems[index]
                                )
                                if (paymentData != null) {
                                    if (paymentData.exists())
                                        for (payment in paymentData.children) {
                                            totalPaid += payment.getValue(Payment::class.java)!!.TOTAL_RECEIVED!!
                                        }
                                }

                                row.createCell(10).setCellValue(
                                    currencyFormat(
                                        getLanguage(this@MainActivity),
                                        getCountry(this@MainActivity)
                                    ).format(totalPaid)
                                )


                                row.createCell(11).setCellValue(
                                    currencyFormat(
                                        getLanguage(this@MainActivity),
                                        getCountry(this@MainActivity)
                                    ).format(data.TOTAL_OUTSTANDING)
                                )
                                row.createCell(12)
                                    .setCellValue(
                                        if (data.TOTAL_OUTSTANDING!! > 0) currencyFormat(
                                            getLanguage(this@MainActivity),
                                            getCountry(this@MainActivity)
                                        ).format(0)
                                        else currencyFormat(
                                            getLanguage(this@MainActivity),
                                            getCountry(this@MainActivity)
                                        ).format(totalPaid - totalPayment)
                                    )
                                row.createCell(13).setCellValue(data.STATUS_CODE)
                                row.createCell(14).setCellValue(data.NOTE)

                                count ++
                            }
                        }
                        workbook.write(fileOutputStream)
                        fileOutputStream.close()
                        ctx.resume("Generate Success!")
                        share(
                            FileProvider.getUriForFile(
                                this@MainActivity, "com.example.android.fileprovider",
                                File(path, "/$fileName")
                            )
                        )
                    }
                }

            }catch(e:Exception){
                toast(e.message.toString())
                e.printStackTrace()
                ctx.resume(e.message.toString())
            }
        }

    }

    @ObsoleteCoroutinesApi
    private suspend fun inventoryReport(stockMovements: MutableList<StockMovement>,
                                          month: Int,
                                          year: Int,
                                          userCode: String) : String {
        return suspendCoroutine {ctx ->
            try{
                analyticPresenter.retrieveCustomer{ it ->
                    var customerList = mutableListOf<Customer>()

                    if (it.exists())
                        for (data in it.children){
                            val item = data.getValue(Customer::class.java)
                            if (item != null){
                                customerList.add(item)
                            }
                        }

                    val fileName = "Inventory Report.xlsx"
                    val path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

                    val fileOutputStream = FileOutputStream(File(path,"/$fileName"))

                    val workbook = XSSFWorkbook()
                    val sheet = workbook.createSheet("Inventory")
                    var row = sheet.createRow(0)
                    row.createCell(0).setCellValue("No")
                    row.createCell(1).setCellValue("Product Name")
                    row.createCell(2).setCellValue("Status")
                    row.createCell(3).setCellValue("Receipt")
                    row.createCell(4).setCellValue("Quantity")
                    row.createCell(5).setCellValue("Date")
                    row.createCell(6).setCellValue("PIC")
                    row.createCell(7).setCellValue("Note")

                    GlobalScope.launch {
                        var count = 0
                        for ((index, data) in stockMovements.withIndex()) {
                            val transactionMonth = getMonth(data.CREATED_DATE.toString())
                            val transactionYear = getYear(data.CREATED_DATE.toString())
                            val currentYear = getCurrentYear()
                            val currentMonth = getCurrentMonth()

                            if (((transactionMonth) == currentMonth && month == 99) || ((transactionMonth) == month-1)
                                && ((transactionYear) == currentYear && year == 99 || (transactionYear) == year)
                                && (data.UPDATED_BY.toString() == userCode || userCode == "")) {

                                tvMainGenerateReportProgress.text = "${(index * 100 / stockMovements.size)}%"

                                row = sheet.createRow(count + 1)

                                row.createCell(0).setCellValue((count + 1).toString())

                                val productName = analyticPresenter.getProductName(data.PROD_CODE.toString())
                                row.createCell(1).setCellValue(productName)

                                row.createCell(2).setCellValue(data.STATUS)

                                row.createCell(3).setCellValue(receiptFormat(data.TRANS_KEY!!.toInt()))

                                row.createCell(4)
                                    .setCellValue(data.QTY.toString())

                                row.createCell(5)
                                    .setCellValue(parseDateFormatFull(data.UPDATED_DATE.toString()))

                                val pic = analyticPresenter.getUserName(data.UPDATED_BY.toString())
                                row.createCell(6).setCellValue(pic)

                                row.createCell(7)
                                    .setCellValue(data.NOTE.toString())

                                count ++
                            }
                        }
                        workbook.write(fileOutputStream)
                        fileOutputStream.close()
                        ctx.resume("Generate Success!")
                        share(
                            FileProvider.getUriForFile(
                                this@MainActivity, "com.example.android.fileprovider",
                                File(path, "/$fileName")
                            )
                        )
                    }
                }

            }catch(e:Exception){
                toast(e.message.toString())
                e.printStackTrace()
                ctx.resume(e.message.toString())
            }
        }

    }

    private fun share(uri: Uri) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "application/xlsx"
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        try {
            startActivity(Intent.createChooser(intent, "Share Report"))
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            toast("No App Available")
        }
    }

    private suspend fun getMonth(convertDate: String): Int {
        return suspendCoroutine {
            val date = dateFormat().parse(convertDate)
            val calendar = Calendar.getInstance()
            calendar.time = date
            it.resume(calendar.get(Calendar.MONTH))
        }
    }

    private suspend fun getYear(convertDate: String): Int {
        return suspendCoroutine {
            val date = dateFormat().parse(convertDate)
            val calendar = Calendar.getInstance()
            calendar.time = date
            it.resume(calendar.get(Calendar.YEAR))
        }
    }

    private suspend fun getCurrentMonth(): Int {
        return  suspendCoroutine { it.resume(Calendar.getInstance().get(Calendar.MONTH)) }
    }

    private suspend fun getCurrentYear(): Int {
        return  suspendCoroutine { it.resume(Calendar.getInstance().get(Calendar.YEAR)) }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_MERCHANT_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                val item = dataSnapshot.getValue(Merchant::class.java)
                if (item != null) {
                    if (item.IMAGE != "")
                    {
                        layoutNavHeaderDefaultImage.visibility = View.GONE
                        ivNavHeader.visibility = View.VISIBLE
                        Glide.with(this).load(item.IMAGE).into(ivNavHeader)
                    }else{
                        layoutNavHeaderDefaultImage.visibility = View.VISIBLE
                        ivNavHeader.visibility = View.GONE

                        try {
                            tvNavHeaderFirstName.text = item.NAME!!.first().toString().toUpperCase(Locale.getDefault())
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }

                }
            }
        }
        if (response == EMessageResult.FETCH_INVITATION_SUCCESS.toString())
        {
            if (dataSnapshot.exists() && dataSnapshot.value != null)
            {
                val item = dataSnapshot.getValue(UserAcceptance::class.java)
                if (item!!.NAME != ""){
                    alert ("You're Invite as ${item!!.USER_GROUP} in ${item.NAME}"){
                        title = "Accept"
                        yesButton {
                            merchantPresenter.acceptInvitation(encodeEmail(getEmail(this@MainActivity)),item)
                        }
                        noButton {
                            GlobalScope.launch {
                                merchantPresenter.removeInvitation(encodeEmail(getEmail(this@MainActivity)))
                            }
                        }
                    }.show()
                }
            }
        }
//        if (response == EMessageResult.FETCH_USER_LIST_SUCCESS.toString()){
//            if(dataSnapshot.exists() && dataSnapshot.value != null && dataSnapshot.value != ""){
//                userListName.clear()
//                userList.clear()
//                val gson = Gson()
//                val arrayUserListType = object : TypeToken<MutableList<UserList>>() {}.type
//                userList = gson.fromJson(dataSnapshot.value.toString(),arrayUserListType)
//
//                GlobalScope.launch {
//                    for ((index,data) in userList.withIndex()){
//                        presenter.getUserName(data.USER_CODE.toString()){
//                            userListName.add(it)
//                        }
//                    }
//                }
//            }
//        }
    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString()){ // for invitation
            toast("Accept Success")
        }else
            toast(message)
    }

    @ObsoleteCoroutinesApi
    override fun printTransactionReport(
        transactionList: MutableList<Transaction>,
        transactionCodeItems: MutableList<Int>,
        month: Int,
        year: Int,
        userCode: String
    ) {
        cvMainGenerateReport.visibility = View.VISIBLE
        GlobalScope.launch (Dispatchers.Main){
            val message = transactionReport(transactionList,transactionCodeItems,month,year,userCode)
            cvMainGenerateReport.visibility = View.GONE
        }
    }

    @ObsoleteCoroutinesApi
    override fun printInventoryReport(month: Int, year: Int, userCode: String) {
        cvMainGenerateReport.visibility = View.VISIBLE
        GlobalScope.launch (Dispatchers.Main){
            val dataSnapshot = analyticPresenter.retrieveStockMovements()
            val stockMovements = mutableListOf<StockMovement>()
            if (dataSnapshot != null) {
                if (dataSnapshot.exists()){
                    for (data in dataSnapshot.children){
                        val item = data.getValue(StockMovement::class.java)
                        if (item != null) {
                            stockMovements.add(item)
                        }
                    }
                }
            }
            val message = inventoryReport(stockMovements,month,year,userCode)
            cvMainGenerateReport.visibility = View.GONE
        }
    }

}