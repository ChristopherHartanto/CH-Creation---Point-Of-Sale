package com.chcreation.pointofsale.analytic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chcreation.pointofsale.*
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
    private var totalPrice = 0
    private var totalCost = 0
    private var totalPending = 0

    companion object{
        var startDate = 0
        var startMonth = 0
        var startYear = 0

        var endDate = 0
        var endMonth = 0
        var endYear = 0
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

        initData()
        srAnalytic.onRefresh {
            initData()
        }

    }

    private fun initData(){
        if (getMerchantUserGroup(ctx) == EUserGroup.WAITER.toString()){
            hideProgressBar()
            tvAnalyticTotalGross.text = "---Invisible---"
            tvAnalyticTotalProfit.text = "---Invisible---"
            tvAnalyticTodayIncome.text = "---Invisible---"
            tvAnalyticMostPurchasedProduct.text = "---Invisible---"
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
        val tahunAwal = 2016f

        val dataPemasukan: MutableList<BarEntry> = ArrayList()
        val dataPengeluaran: MutableList<BarEntry> = ArrayList()
//
        for (date in 1..30){
            var profit = 0F
            var gross = 0F
            for (data in transactions){
                if (getDateOfMonth(data.CREATED_DATE.toString()) == date)
                    profit += data.TOTAL_PRICE!!.toFloat()
            }
            dataPemasukan.add(BarEntry(date.toFloat(), profit))
            dataPengeluaran.add(BarEntry(date.toFloat(), profit))
        }

        // Pengaturan atribut bar, seperti warna dan lain-lain
        // Pengaturan atribut bar, seperti warna dan lain-lain
        val dataSet1 = BarDataSet(dataPemasukan, "Profit")
        dataSet1.color = ColorTemplate.JOYFUL_COLORS[3]

        val dataSet2 = BarDataSet(dataPengeluaran, "Gross")
        dataSet2.color = ColorTemplate.JOYFUL_COLORS[1]


        // Membuat Bar data yang akan di set ke Chart
        // Membuat Bar data yang akan di set ke Chart
        val barData = BarData(dataSet1, dataSet2)

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
        ) * 30F
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

        startDate = 0
        startMonth = 0
        startYear = 0

        endDate = 0
        endMonth = 0
        endYear = 0
    }

    private fun clearData(){
        products.clear()
        transactions.clear()
        boughtProducts.clear()
        totalProfit = 0
        totalPrice = 0
        totalCost = 0
        totalPending = 0
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

            if (((getYear(data.CREATED_DATE.toString()) >= startYear && getMonth(data.CREATED_DATE.toString()) >= startMonth && getDateOfMonth(data.CREATED_DATE.toString()) >= startDate)
                        || startYear == 0 || startMonth == 0 || startDate == 0)
                    &&
                ((getYear(data.CREATED_DATE.toString()) <= endYear && getMonth(data.CREATED_DATE.toString()) >= endMonth && getDateOfMonth(data.CREATED_DATE.toString()) >= endDate)
                        || endYear == 0 || endMonth == 0 || endDate == 0 )
            ){
                for (cart in cartItems){
                    val product = products.filter { it.PROD_CODE == cart.PROD_CODE }
                    if (!product.isNullOrEmpty()){
                        boughtProducts.add(product[0])
                        totalPrice += product[0].PRICE!! * cart.Qty!!
                        totalCost += product[0].COST!! * cart.Qty!!

                        if (todayDate == transactionDate){
                            todayPrice += product[0].PRICE!! * cart.Qty!!
                            todayCost += product[0].COST!! * cart.Qty!!
                        }
                    }
                }
                if (data.TOTAL_OUTSTANDING!! > 0){
                    totalPending += data.TOTAL_OUTSTANDING!!
                    if (todayDate == transactionDate)
                        todayPending += data.TOTAL_OUTSTANDING!!
                }
            }
        }
        hideProgressBar()
        tvAnalyticTotalGross.text = indonesiaCurrencyFormat().format(totalPrice)
        tvAnalyticTotalProfit.text = indonesiaCurrencyFormat().format(totalPrice-totalCost-totalPending)
        tvAnalyticTodayIncome.text = indonesiaCurrencyFormat().format(todayPrice)

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

    override fun response(message: String) {
    }

}
