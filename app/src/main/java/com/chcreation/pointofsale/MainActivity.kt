package com.chcreation.pointofsale

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
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
import com.bumptech.glide.Glide
import com.chcreation.pointofsale.checkout.PostCheckOutActivity
import com.chcreation.pointofsale.home.HomeFragment.Companion.active
import com.chcreation.pointofsale.login.LoginActivity
import com.chcreation.pointofsale.merchant.ManageMerchantActivity
import com.chcreation.pointofsale.model.Merchant
import com.chcreation.pointofsale.model.UserAcceptance
import com.chcreation.pointofsale.model.UserList
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.presenter.MerchantPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.startActivity
import java.util.*

class MainActivity : AppCompatActivity(), MainView {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var doubleBackToExitPressedOnce = false
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var presenter: Homepresenter
    private lateinit var merchantPresenter: MerchantPresenter
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

        if (mAuth.currentUser == null || getMerchantCredential(this) == "" || getMerchantCode(this) == ""){
            logOut()
        }

        presenter.retrieveSincere(){
            val editor = sharedPreference.edit()
            editor.putString(ESharedPreference.SINCERE.toString(),it.SINCERE)
            editor.apply()
        }

        presenter.checkVersion {
            var currentVersionCode = ""
            try {
                currentVersionCode = packageManager.getPackageInfo(packageName,0).versionName.toString()
            }catch (e: java.lang.Exception){
                currentVersionCode = ""
                e.printStackTrace()
            }
            if (it.VERSION_NAME != packageManager.getPackageInfo(packageName,0).versionName.toString() && currentVersionCode != ""){
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

        presenter.getMerchant(){success, merchant ->
            if (!success)
                logOut()
            else{
                if (merchant.NAME != getMerchantName(this) || merchant.IMAGE != getMerchantImage(this)
                    || merchant.NO_TELP != getMerchantNoTel(this) || merchant.MEMBER_STATUS != getMerchantMemberStatus(this)
                    || merchant.COUNTRY != getCountry(this) || merchant.LANGUAGE != getLanguage(this))
                    logOut()
            }
        }
        presenter.getUserDetail(mAuth.currentUser!!.uid){
            if (it.ACTIVE != EStatusUser.ACTIVE.toString() || it.DEVICE_ID != getDeviceId(this)
                || getDeviceId(this) == "" || it.DEVICE_ID == ""){
                logOut()
            }
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

    private fun logOut(){
        mAuth.signOut()
        toast("Session End")
        removeAllSharedPreference(this)
        PostCheckOutActivity().clearCartData()
        startActivity<LoginActivity>()
        finish()
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
}