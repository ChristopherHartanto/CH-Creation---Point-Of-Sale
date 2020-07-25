package com.chcreation.pointofsale.checkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.chcreation.pointofsale.MainActivity
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.totalReceived
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.transDate
import com.chcreation.pointofsale.checkout.DiscountActivity.Companion.newTotal
import com.chcreation.pointofsale.checkout.SelectCustomerActivity.Companion.selectCustomerCode
import com.chcreation.pointofsale.checkout.SelectCustomerActivity.Companion.selectCustomerName
import com.chcreation.pointofsale.customer.CustomerFragment
import com.chcreation.pointofsale.home.HomeFragment
import com.chcreation.pointofsale.home.HomeFragment.Companion.totalPrice
import com.chcreation.pointofsale.indonesiaCurrencyFormat
import kotlinx.android.synthetic.main.activity_post_check_out.*
import kotlinx.android.synthetic.main.activity_receipt.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

class PostCheckOutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_check_out)

        supportActionBar?.hide()

        var change = 0
        if (totalPrice - totalReceived < 0)
            change = totalReceived - totalPrice

        var totalPayment = 0
        totalPayment = if (newTotal != 0)
            0
        else
            totalPrice

        tvPostCheckOutTotalPrice.text = indonesiaCurrencyFormat().format(totalPrice)

        if (totalReceived >= totalPayment)
            tvPostCheckOutTotalPrice.text = indonesiaCurrencyFormat().format(totalReceived - totalPayment)
        else{
            tvPostCheckOutChange.text = "Pending : " + indonesiaCurrencyFormat().format(totalPayment-totalReceived)
        }

        btnPostCheckOut.onClick {
            clearCartData()
            startActivity<MainActivity>()
            finish()
        }

        btnPostCheckOutReceipt.onClick {
            startActivity<ReceiptActivity>()
        }
    }

    override fun onBackPressed() {

    }

    fun clearCartData(){
        HomeFragment.totalPrice = 0
        HomeFragment.cartItems.clear()
        NoteActivity.note = ""
        DiscountActivity.newTotal = 0
        HomeFragment.totalQty = 0
        totalReceived = 0
        CheckOutActivity.transDate = ""
        transDate = ""
        selectCustomerName = ""
        selectCustomerCode = ""
    }
}
