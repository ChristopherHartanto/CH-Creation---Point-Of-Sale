package com.chcreation.pointofsale.analytic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chcreation.pointofsale.*

import com.chcreation.pointofsale.model.Cart
import com.chcreation.pointofsale.model.Cat
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.model.Transaction
import com.chcreation.pointofsale.presenter.AnalyticPresenter
import com.chcreation.pointofsale.presenter.CheckOutPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_analytic.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.support.v4.ctx
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
    private var totalProfit = 0
    private var totalPrice = 0
    private var totalCost = 0
    private var totalPending = 0

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
    }

    override fun onStart() {
        super.onStart()

        GlobalScope.launch {
            presenter.retrieveProducts()
            presenter.retrieveTransactions()
        }
    }

    fun calculate(){
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

            for (cart in cartItems){
                val product = products.filter { it.PROD_CODE == cart.PROD_CODE }
                if (!product.isNullOrEmpty()){
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

        tvAnalyticTotalGross.text = indonesiaCurrencyFormat().format(totalPrice)
        tvAnalyticTotalProfit.text = indonesiaCurrencyFormat().format(totalPrice-totalCost-totalPending)
        tvAnalyticTodayIncome.text = indonesiaCurrencyFormat().format(todayPrice)
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
