package com.chcreation.pointofsale.analytic

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.analytic.AnalyticFilterActivity.Companion.monthItems
import com.chcreation.pointofsale.model.Cart
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.model.Transaction
import com.chcreation.pointofsale.presenter.AnalyticPresenter
import com.chcreation.pointofsale.view.MainView
import com.github.mikephil.charting.components.AxisBase
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.startActivity
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
    private var boughtProducts : MutableList<Product> = mutableListOf()
    private var totalProfit = 0
    private var totalGross = 0
    private var todayProfit = 0
    private var todayGross = 0

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

        cvAnalyticFilter.onClick {
            startActivity<AnalyticFilterActivity>()
        }
    }

    override fun onStart() {
        super.onStart()

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
                presenter.retrieveTransactions()
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

        for (date in 1..31){
            for (data in transactions){

                var profit = 0F
                var gross = 0F
                if (((getMonth(data.CREATED_DATE.toString()) == getCurrentMonth() && month == 99) || (getMonth(data.CREATED_DATE.toString()) == month-1))
                    && getDateOfMonth(data.CREATED_DATE.toString()) == date
                    && ((getYear(data.CREATED_DATE.toString()) == getCurrentYear() && year == 99) || (getYear(data.CREATED_DATE.toString()) == year))
                    && (data.CREATED_BY.toString() == userCode || userCode == "")){
                    val gson = Gson()
                    val arrayCartType = object : TypeToken<MutableList<Cart>>() {}.type
                    val cartItems : MutableList<Cart> = gson.fromJson(data.DETAIL,arrayCartType)

                    for (cart in cartItems){
                        val product = products.filter { it.PROD_CODE == cart.PROD_CODE }
                        if (!product.isNullOrEmpty()){
//                            boughtProducts.add(product[0])
//                            totalPrice += product[0].PRICE!! * cart.Qty!!
//                            totalCost += product[0].COST!! * cart.Qty!!

                            profit += (product[0].PRICE!! * cart.Qty!!) - (product[0].COST!! * cart.Qty!!)
                            gross += (product[0].PRICE!! * cart.Qty!!)
                        }
                    }

                    // total pending
                    profit -= data.TOTAL_OUTSTANDING!!
                    gross -= data.TOTAL_OUTSTANDING!!

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
                    if (date > 22){
                        forthGrossWeek += gross
                        forthProfitWeek += profit
                    }
                }
            }
        }

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
        pbAnalyticMostPurchasedProduct.visibility = View.GONE

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
        totalProfit = 0
        totalGross = 0
        todayGross = 0
        todayProfit = 0
    }


    private fun calculate(){
        val todayDate = parseDateFormat(dateFormat().format(Date()))
        var transactionDate = ""
        var todayPrice = 0
        var todayCost = 0
        var todayPending = 0

        for (data in transactions){
            val gson = Gson()
            val arrayCartType = object : TypeToken<MutableList<Cart>>() {}.type
            val cartItems : MutableList<Cart> = gson.fromJson(data.DETAIL,arrayCartType)

            transactionDate = parseDateFormat(data.CREATED_DATE.toString())

            // total
            for (cart in cartItems){
                val product = products.filter { it.PROD_CODE == cart.PROD_CODE }
                if (!product.isNullOrEmpty()){
                    boughtProducts.add(product[0])
                    totalProfit += product[0].PRICE!! * cart.Qty!! - (product[0].COST!! * cart.Qty!!)
                    totalGross += product[0].PRICE!! * cart.Qty!!

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
                        todayProfit += product[0].PRICE!! * cart.Qty!! - (product[0].COST!! * cart.Qty!!)
                        todayGross += product[0].PRICE!! * cart.Qty!!

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
        tvAnalyticTotalGross.text = indonesiaCurrencyFormat().format(totalGross)
        tvAnalyticTotalProfit.text = indonesiaCurrencyFormat().format(totalProfit)
        tvAnalyticTodayIncome.text = indonesiaCurrencyFormat().format(todayGross)

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

        initChart()
        srAnalytic.isRefreshing = false
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
            }
            if (response == EMessageResult.FETCH_TRANS_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    for (data in dataSnapshot.children){
                        val item = data.getValue(Transaction::class.java)

                        if (item != null && item.STATUS_CODE != EStatusCode.CANCEL.toString()) {
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