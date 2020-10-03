package com.chcreation.pointofsale.analytic

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.analytic.AnalyticFilterActivity.Companion.monthItems
import com.chcreation.pointofsale.model.*
import com.chcreation.pointofsale.presenter.AnalyticPresenter
import com.chcreation.pointofsale.view.MainView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_analytic.*
import kotlinx.coroutines.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import java.io.File
import java.io.FileOutputStream
import java.time.Year
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class AnalyticFragment : Fragment(), MainView {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: AnalyticPresenter
    private var products : MutableList<Product> = mutableListOf()
    private var transactions : MutableList<Transaction> = mutableListOf()
    private var transactionCodeItems : MutableList<Int> = mutableListOf()
    private var boughtProducts : MutableList<Product> = mutableListOf()
    private var totalProfit = 0F
    private var totalGross = 0F
    private var todayProfit = 0F
    private var todayGross = 0F
    private lateinit var jobReport : Job
    private lateinit var printCallback: GenerateReport

    companion object{
        var month = 99
        var monthName = ""
        var year = 99
        var isDiscount = false
        var isTax = false
        var userCode = ""
        var userName = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_analytic, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = AnalyticPresenter(this,mAuth,mDatabase,ctx)
        printCallback = ctx as GenerateReport

        cvAnalyticFilter.onClick {
            startActivity<AnalyticFilterActivity>()
        }

        tvAnalyticGenerateReport.onClick {
            val currentDate = dateFormat().format(Date())
            tvAnalyticGenerateReport.startAnimation(normalClickAnimation())
            if (getMerchantMemberDeadline(ctx) == ""){
                alert ("Upgrade to Premium for Generate Report"){
                    title = "Premium Feature!"
                    yesButton {
                        sendEmail("Upgrade Premium",
                            "Merchant: ${getMerchantName(ctx)}",ctx)
                    }

                    noButton {  }
                }.show()
            }else if (compareDate(getMerchantMemberDeadline(ctx),currentDate) == 2){
                alert ("Your Premium Member Has Ended, Do You Want to Extend?"){
                    title = "Premium End"
                    yesButton {
                        sendEmail("Extend Premium",
                            "Merchant: ${getMerchantName(ctx)}",ctx)
                    }

                    noButton {  }
                }.show()
            }else {
                cvAnalyticSelectReport.visibility = View.VISIBLE
                bgAnalyticSelectReport.visibility = View.VISIBLE
            }
        }

        btnReceiptSelectReportClose.onClick{
            btnReceiptSelectReportClose.startAnimation(normalClickAnimation())
            cvAnalyticSelectReport.visibility = View.GONE
            bgAnalyticSelectReport.visibility = View.GONE
        }

        layoutReceiptTransReport.onClick {
            layoutReceiptTransReport.startAnimation(normalClickAnimation())
            cvAnalyticSelectReport.visibility = View.GONE
            bgAnalyticSelectReport.visibility = View.GONE
            printCallback.printTransactionReport(transactions,transactionCodeItems, month, year, userCode)
        }

        layoutReceiptInventoryReport.onClick {
            layoutReceiptInventoryReport.startAnimation(normalClickAnimation())
            cvAnalyticSelectReport.visibility = View.GONE
            bgAnalyticSelectReport.visibility = View.GONE
            printCallback.printInventoryReport(month, year, userCode)
        }

        bgAnalyticSelectReport.onClick {
            cvAnalyticSelectReport.visibility = View.GONE
            bgAnalyticSelectReport.visibility = View.GONE
        }

    }

    override fun onStart() {
        super.onStart()

        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        if (month != 99 && year != 99){
            tvAnalyticFilterDate.text = "${monthName} - ${year}"
        }else{
            tvAnalyticFilterDate.text = "${monthItems[getCurrentMonth()]} - ${getCurrentYear()}"
        }
        if (userName != "")
            tvAnalyticFilterName.text = userName
        else
            tvAnalyticFilterName.text = "All"

        initData()
        srAnalytic.onRefresh {
            initData()
        }

    }


    private fun initData(){
        if (getMerchantUserGroup(ctx) == EUserGroup.WAITER.toString()){
            hideProgressBar()
            tvAnalyticTotalGross.text = "-Not Authorized-"
            tvAnalyticTotalProfit.text = "-Not Authorized-"
            tvAnalyticTodayIncome.text = "-Not Authorized-"
            tvAnalyticMostPurchasedProduct.text = "-Not Authorized-"
        }else{
            clearData()
            GlobalScope.launch {
                presenter.retrieveProducts()
            }
        }
    }

    private fun initChart(){

        val groupSpace = 0.08f
        val barSpace = 0.02f
        val barWidth = 0.45f

        val profitList: MutableList<BarEntry> = ArrayList()
        val grossList: MutableList<BarEntry> = ArrayList()

        var firstProfitWeek = 0F
        var secondProfitWeek = 0F
        var thirdProfitWeek = 0F
        var forthProfitWeek = 0F

        var firstGrossWeek = 0F
        var secondGrossWeek = 0F
        var thirdGrossWeek = 0F
        var forthGrossWeek = 0F

        var totalSale = 0
        var totalRevenue = 0F
        var avgRevenue = 0F
        var totalTax = 0F
        var totalDiscount = 0F
        var totalProfit = 0F
        var topRevenue = 0F
        var avgPerSale = 0F
        var countItems = 0F
        var cogs = 0F
        var topCustomer = arrayListOf<String>()

        for (date in 1..31){
            for (data in transactions){

                var profit = 0F
                var gross = 0F
                if (((getMonth(data.CREATED_DATE.toString()) == getCurrentMonth() && month == 99) || (getMonth(data.CREATED_DATE.toString()) == month-1))
                    && getDateOfMonth(data.CREATED_DATE.toString()) == date
                    && ((getYear(data.CREATED_DATE.toString()) == getCurrentYear() && year == 99) || (getYear(data.CREATED_DATE.toString()) == year))
                    && (data.CREATED_BY.toString() == userCode || userCode == "")
                    && data.STATUS_CODE != EStatusCode.CANCEL.toString()){
                    val gson = Gson()
                    val arrayCartType = object : TypeToken<MutableList<Cart>>() {}.type
                    val cartItems : MutableList<Cart> = gson.fromJson(data.DETAIL,arrayCartType)

                    if (data.CUST_CODE != "") topCustomer.add(data.CUST_CODE.toString())

                    for (cart in cartItems){
                        val product = products.filter { it.PROD_CODE == cart.PROD_CODE }
                        if (!product.isNullOrEmpty()){
                            boughtProducts.add(product[0]) // for top products
//                            boughtProducts.add(product[0])
//                            totalPrice += product[0].PRICE!! * cart.Qty!!
//                            totalCost += product[0].COST!! * cart.Qty!!
                            cogs += (product[0].COST!! * cart.Qty!!).toFloat()
                            val price = (if (cart.WHOLE_SALE_PRICE != -1F) cart.WHOLE_SALE_PRICE!! else cart.PRICE!!)
                            profit += (price * cart.Qty!!) - (product[0].COST!! * cart.Qty!!)
                            gross += (price * cart.Qty!!)

                            countItems++
                        }
                    }

                    // total pending
                    profit -= data.TOTAL_OUTSTANDING!!
                    gross -= data.TOTAL_OUTSTANDING!!


                    //region more analytic

                    if (topRevenue < gross)
                        topRevenue = gross

                    totalSale++
                    totalRevenue += gross
                    totalProfit += profit
                    totalTax += data.TAX!!
                    totalDiscount += data.DISCOUNT!!

                    //end region

                    // filter
                    if (isDiscount){
                        profit -= data.DISCOUNT!!
                        gross -= data.DISCOUNT!!
                    }
                    if (isTax){
                        profit += data.TAX!!
                        gross += data.TAX!!
                    }

                    // by week
                    if (date in 1..7){
                        firstProfitWeek += profit
                        firstGrossWeek += gross
                    }
                    if (date in 8..14){
                        secondProfitWeek += profit
                        secondGrossWeek += gross
                    }
                    if (date in 15..21){
                        thirdProfitWeek += profit
                        thirdGrossWeek += gross
                    }
                    if (date > 21){
                        forthGrossWeek += gross
                        forthProfitWeek += profit
                    }
                }
            }
        }

        avgRevenue = totalRevenue / totalSale
        avgPerSale = countItems / totalSale
        tvAnalyticTotalSale.text = totalSale.toString()
        tvAnalyticRevenue.text = currencyFormat(getLanguage(ctx), getCountry(ctx)).format(totalRevenue)
        tvAnalyticProfit.text = currencyFormat(getLanguage(ctx), getCountry(ctx)).format(totalProfit)
        tvAnalyticAvgRevenue.text = currencyFormat(getLanguage(ctx), getCountry(ctx)).format(avgRevenue)
        tvAnalyticTopRevenue.text = currencyFormat(getLanguage(ctx), getCountry(ctx)).format(topRevenue)
        tvAnalyticSalesDiscount.text = currencyFormat(getLanguage(ctx), getCountry(ctx)).format(totalDiscount)
        tvAnalyticSalesTax.text = currencyFormat(getLanguage(ctx), getCountry(ctx)).format(totalTax)
        tvAnalyticAvgItemsPerSale.text = String.format("%.2f",avgPerSale)
        tvAnalyticGpm.text = String.format("%.1f",((totalRevenue - cogs - (totalDiscount - totalTax)) / totalRevenue * 100)) +"%"
        val groupCustomer = topCustomer.groupingBy { it }.eachCount().maxBy { it.value }
        if (groupCustomer != null) {
            presenter.retrieveCustomer(groupCustomer.key){
                tvAnalyticTopCustomer.text = it
            }
        }else
            tvAnalyticTopCustomer.text = "-"


        profitList.add(BarEntry(1F, firstProfitWeek))
        profitList.add(BarEntry(2F, secondProfitWeek))
        profitList.add(BarEntry(3F, thirdProfitWeek))
        profitList.add(BarEntry(4F, forthProfitWeek))

        grossList.add(BarEntry(1F, firstGrossWeek))
        grossList.add(BarEntry(2F, secondGrossWeek))
        grossList.add(BarEntry(3F, thirdGrossWeek))
        grossList.add(BarEntry(4F, forthGrossWeek))

        // Pengaturan atribut bar, seperti warna dan lain-lain
        // Pengaturan atribut bar, seperti warna dan lain-lain
        val dataSet1 = BarDataSet(profitList, "Profit")
        dataSet1.color = ColorTemplate.JOYFUL_COLORS[3]

        val dataSet2 = BarDataSet(grossList, "Gross")
        dataSet2.color = ColorTemplate.JOYFUL_COLORS[1]

        // Membuat Bar data yang akan di set ke Chart
        // Membuat Bar data yang akan di set ke Chart
        val barData = BarData(dataSet1,dataSet2)

        // Pengaturan sumbu X
        // Pengaturan sumbu X
        val xAxis: XAxis = chartAnalytic.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setCenterAxisLabels(true)

        // Agar ketika di zoom tidak menjadi pecahan
        // Agar ketika di zoom tidak menjadi pecahan
        xAxis.granularity = 1f

        // Diubah menjadi integer, kemudian dijadikan String
        // Ini berfungsi untuk menghilankan koma, dan tanda ribuah pada tahun
        // Diubah menjadi integer, kemudian dijadikan String
// Ini berfungsi untuk menghilankan koma, dan tanda ribuah pada tahun
        xAxis.valueFormatter =
            IAxisValueFormatter { value, axis -> value.toInt().toString() }

        //Menghilangkan sumbu Y yang ada di sebelah kanan
        //Menghilangkan sumbu Y yang ada di sebelah kanan
        chartAnalytic.axisRight.isEnabled = false

        // Menghilankan deskripsi pada Chart
        // Menghilankan deskripsi pada Chart
        chartAnalytic.description.isEnabled = false

        // Set data ke Chart
        // Tambahkan invalidate setiap kali mengubah data chart
        // Set data ke Chart
// Tambahkan invalidate setiap kali mengubah data chart
        chartAnalytic.data = barData
        chartAnalytic.barData.barWidth = barWidth
        chartAnalytic.xAxis.axisMinimum = 1F
        chartAnalytic.xAxis.axisMaximum = 1F + chartAnalytic.barData.getGroupWidth(
            groupSpace,
            barSpace
        ) * 4F
        //chartAnalytic.xAxis.axisMaximum = 30F
        chartAnalytic.groupBars(1F, groupSpace, barSpace)
        chartAnalytic.isDragEnabled = true
        chartAnalytic.invalidate()
    }

    private fun hideProgressBar(){
        pbAnalyticTodayIncome.visibility = View.GONE
        pbAnalyticTotalGross.visibility = View.GONE
        pbAnalyticTotalProfit.visibility = View.GONE

        tvAnalyticTodayIncome.visibility = View.VISIBLE
        tvAnalyticTotalGross.visibility = View.VISIBLE
        tvAnalyticTotalProfit.visibility = View.VISIBLE
        tvAnalyticMostPurchasedProduct.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()

        month = 99
        year = 99
        isDiscount = false
        isTax = false
        userCode = ""
        userName = ""
    }

    private fun clearData(){
        products.clear()
        transactions.clear()
        boughtProducts.clear()
        totalProfit = 0F
        totalGross = 0F
        todayGross = 0F
        todayProfit = 0F
    }


    private fun calculate(){
        val todayDate = parseDateFormat(dateFormat().format(Date()))
        var transactionDate = ""
        var todayPrice = 0
        var todayCost = 0
        var todayPending = 0

        val transactionYearly = arrayListOf<Transaction>()
            transactions.forEach {
                if (getYear(it.CREATED_DATE.toString()) == getCurrentYear()
                    && it.STATUS_CODE != EStatusCode.CANCEL.toString())
                    transactionYearly.add(it)}

        for (data in transactionYearly){
            val gson = Gson()
            val arrayCartType = object : TypeToken<MutableList<Cart>>() {}.type
            val cartItems : MutableList<Cart> = gson.fromJson(data.DETAIL,arrayCartType)

            transactionDate = parseDateFormat(data.CREATED_DATE.toString())

            // total
            for (cart in cartItems){
                val product = products.filter { it.PROD_CODE == cart.PROD_CODE }
                if (!product.isNullOrEmpty()){
                    totalProfit += (if (cart.WHOLE_SALE_PRICE != -1F) cart.WHOLE_SALE_PRICE!! else cart.PRICE!!) * cart.Qty!! - (product[0].COST!!.toInt() * cart.Qty!!.toInt())
                    totalGross += (if (cart.WHOLE_SALE_PRICE != -1F) cart.WHOLE_SALE_PRICE!! else cart.PRICE!!) * cart.Qty!!

                }
            }
            totalProfit -= data.TOTAL_OUTSTANDING!!
            totalGross -= data.TOTAL_OUTSTANDING!!

            if (isDiscount){
                totalProfit -= data.DISCOUNT!!
                totalGross -= data.DISCOUNT!!
            }
            if (isTax){
                totalProfit += data.TAX!!
                totalGross += data.TAX!!
            }


            // today
            for (cart in cartItems){
                val product = products.filter { it.PROD_CODE == cart.PROD_CODE }
                if (!product.isNullOrEmpty()){
                    if (todayDate == transactionDate){
                        todayProfit += (if (cart.WHOLE_SALE_PRICE != -1F) cart.WHOLE_SALE_PRICE!! else cart.PRICE!!) * cart.Qty!! - (product[0].COST!!.toInt() * cart.Qty!!.toInt())
                        todayGross += (if (cart.WHOLE_SALE_PRICE != -1F) cart.WHOLE_SALE_PRICE!! else cart.PRICE!!) * cart.Qty!!

                    }
                }
            }
            if (data.TOTAL_OUTSTANDING!! > 0){
                if (todayDate == transactionDate){
                    todayProfit -= data.TOTAL_OUTSTANDING!!
                    todayGross -= data.TOTAL_OUTSTANDING!!
                }
            }
            if (isDiscount){
                if (todayDate == transactionDate){
                    todayProfit -= data.DISCOUNT!!
                    todayGross -= data.DISCOUNT!!
                }
            }
            if (isTax){
                if (todayDate == transactionDate){
                    todayProfit += data.TAX!!
                    todayGross += data.TAX!!
                }
            }

        }
        hideProgressBar()
        tvAnalyticTotalGross.text = currencyFormat(getLanguage(ctx), getCountry(ctx)).format(totalGross)
        tvAnalyticTotalProfit.text = currencyFormat(getLanguage(ctx), getCountry(ctx)).format(totalProfit)
        tvAnalyticTodayIncome.text = currencyFormat(getLanguage(ctx), getCountry(ctx)).format(todayGross)

        initChart()
        calculateMostProduct()
        srAnalytic.isRefreshing = false
    }

    private fun calculateMostProduct(){
        val sortBoughtProducts = boughtProducts.sortedBy { it.PROD_CODE }
        var mostBoughtProduct = ""
        var count = 1
        var max = 1
        var tmpData = ""
        for ((index,data) in sortBoughtProducts.withIndex()){
            if (index == 0){
                mostBoughtProduct = data.NAME.toString()
                tmpData = data.NAME.toString()
            }
            else if(tmpData == data.NAME.toString())
                count++
            else if(tmpData != data.NAME.toString()){
                if (max < count) {
                    max = count
                    mostBoughtProduct = tmpData
                }
                tmpData = data.NAME.toString()
                count = 1
            }
        }
        tvAnalyticMostPurchasedProduct.text = mostBoughtProduct
    }



    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (isVisible && isResumed){
            if (response == EMessageResult.FETCH_PROD_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    for (data in dataSnapshot.children){
                        val item = data.getValue(Product::class.java)

                        if (item != null) {
                            products.add(item)
                        }
                    }
                }
                GlobalScope.launch {
                    presenter.retrieveTransactions()
                }
            }
            if (response == EMessageResult.FETCH_TRANS_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    for (data in dataSnapshot.children){
                        val item = data.getValue(Transaction::class.java)

                        if (item != null) {
                            transactionCodeItems.add(data.key!!.toInt())
                            transactions.add(item)
                        }
                    }
                }
                calculate()
            }
        }
    }

    override fun response(message: String) {
    }

}

interface GenerateReport{
    fun printTransactionReport(transactionList: MutableList<Transaction>,
                               transactionCodeItems: MutableList<Int>,
                               month: Int,
                               year: Int,
                               userCode: String)
    fun printInventoryReport(
        month: Int,
        year: Int,
        userCode: String)
}