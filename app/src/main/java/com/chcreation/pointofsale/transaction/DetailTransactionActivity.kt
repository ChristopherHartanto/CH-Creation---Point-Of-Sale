package com.chcreation.pointofsale.transaction

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.checkout.CartRecyclerViewAdapter
import com.chcreation.pointofsale.checkout.CheckOutActivity
import com.chcreation.pointofsale.getMerchant
import com.chcreation.pointofsale.home.HomeFragment
import com.chcreation.pointofsale.indonesiaCurrencyFormat
import com.chcreation.pointofsale.model.Cart
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.receiptFormat
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transCodeItems
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transItems
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transPosition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_detail_transaction.*
import kotlinx.android.synthetic.main.activity_receipt.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.yesButton
import java.util.*

class DetailTransactionActivity : AppCompatActivity() {

    private lateinit var adapter: CartRecyclerViewAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_transaction)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        val gson = Gson()
        val arrayCartType = object : TypeToken<MutableList<Cart>>() {}.type
        val purchasedItems : MutableList<Cart> = gson.fromJson(transItems[transPosition].DETAIL,arrayCartType)

        adapter = CartRecyclerViewAdapter(this, purchasedItems){

        }

        btnDetailTransactionCancel.onClick {
            alert ("Are You Sure Want to Cancel?"){
                title = "Cancel Transaction"
                yesButton {

                }
                noButton {

                }
            }.show()
        }

        rvDetailTransaction.adapter = adapter
        rvDetailTransaction.layoutManager = LinearLayoutManager(this)

    }

    override fun onStart() {
        super.onStart()

        val discount = transItems[transPosition].DISCOUNT
        val note = transItems[transPosition].NOTE

        tvDetailTransactionDate.text = transItems[transPosition].CREATED_DATE.toString()
        tvDetailTransactionCode.text = receiptFormat(transCodeItems[transPosition].toInt())

        if (discount != 0){
            tvDetailTransactionDiscount.text = transItems[transPosition].DISCOUNT.toString()
            tvDetailTransactionSubTotal.visibility = View.VISIBLE
            tvDetailTransactionSubTotal.text = transItems[transPosition].TOTAL_PRICE.toString()
        }
        if (note != "")
            tvDetailTransactionNote.text = transItems[transPosition].NOTE.toString()

        tvDetailTransactionTotalPrice.text = indonesiaCurrencyFormat().format(transItems[transPosition].TOTAL_PRICE).toString()
    }
}
