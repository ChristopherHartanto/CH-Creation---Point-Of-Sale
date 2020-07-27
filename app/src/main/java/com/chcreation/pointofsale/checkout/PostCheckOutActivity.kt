package com.chcreation.pointofsale.checkout

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.chcreation.pointofsale.MainActivity
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.postTotalPayment
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.totalOutStanding
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.totalReceived
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.transDate
import com.chcreation.pointofsale.checkout.DiscountActivity.Companion.discount
import com.chcreation.pointofsale.checkout.DiscountActivity.Companion.tax
import com.chcreation.pointofsale.checkout.SelectCustomerActivity.Companion.selectCustomerCode
import com.chcreation.pointofsale.checkout.SelectCustomerActivity.Companion.selectCustomerName
import com.chcreation.pointofsale.customer.CustomerFragment
import com.chcreation.pointofsale.home.HomeFragment
import com.chcreation.pointofsale.home.HomeFragment.Companion.totalPrice
import com.chcreation.pointofsale.indonesiaCurrencyFormat
import com.chcreation.pointofsale.transaction.DetailTransactionActivity.Companion.existPayment
import com.chcreation.pointofsale.transaction.TransactionFragment
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.customerItems
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transCodeItems
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transItems
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transPosition
import kotlinx.android.synthetic.main.activity_post_check_out.*
import kotlinx.android.synthetic.main.activity_receipt.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

class PostCheckOutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_check_out)

        supportActionBar?.hide()

        if (existPayment)
            postCheckOutExist()
        else
            postCheckOutNew()

        btnPostCheckOut.onClick {
            clearCartData()
            val i = Intent(this@PostCheckOutActivity, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)

            //startActivity<MainActivity>()
            finish()
        }

        btnPostCheckOutReceipt.onClick {
            startActivity<ReceiptActivity>()
        }
    }

    override fun onBackPressed() {

    }

    fun postCheckOutNew(){
        var totalPayment = totalPrice - discount + tax

        tvPostCheckOutTotalPrice.text = indonesiaCurrencyFormat().format(totalPayment)

        if (totalReceived >= totalPayment){
            tvPostCheckOutChange.text = "Change: "+indonesiaCurrencyFormat().format(totalReceived - totalPayment)
            ivPostCheckOut.backgroundResource = R.drawable.success
        }
        else{
            ivPostCheckOut.backgroundResource = R.drawable.pending
            tvPostCheckOutChange.text = "Pending : " + indonesiaCurrencyFormat().format(totalPayment-totalReceived)
        }
    }

    fun postCheckOutExist(){
        tvPostCheckOutTotalPrice.text = indonesiaCurrencyFormat().format(postTotalPayment)

        if (totalReceived >= postTotalPayment){
            tvPostCheckOutChange.text = "Change: "+indonesiaCurrencyFormat().format(totalReceived - postTotalPayment)
            ivPostCheckOut.backgroundResource = R.drawable.success
        }
        else{
            ivPostCheckOut.backgroundResource = R.drawable.pending
            tvPostCheckOutChange.text = "Pending : " + indonesiaCurrencyFormat().format(postTotalPayment-totalReceived)
        }
    }

    fun clearCartData(){
        // Transaction Fragment
        existPayment = false
        transPosition = 0
        transCodeItems.clear()
        transItems.clear()
        customerItems.clear()

        // Check Out Activity
        postTotalPayment = 0
        totalPrice = 0
        totalReceived = 0
        totalOutStanding = 0

        HomeFragment.cartItems.clear()
        NoteActivity.note = ""
        DiscountActivity.discount = 0
        tax = 0
        HomeFragment.totalQty = 0
        transDate = ""
        selectCustomerName = ""
        selectCustomerCode = ""
    }
}
