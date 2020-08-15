package com.chcreation.pointofsale.checkout

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.widget.doOnTextChanged
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.home.HomeFragment.Companion.totalPrice
import com.chcreation.pointofsale.indonesiaCurrencyFormat
import com.chcreation.pointofsale.normalClickAnimation
import kotlinx.android.synthetic.main.activity_discount.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.textChangedListener

class DiscountActivity : AppCompatActivity() {

    companion object{
        var discount = 0
        var tax = 0
    }

    private var percentageDiscount = 0
    private var cashDiscount = 0
    private var action = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discount)

        supportActionBar!!.hide()
        action = intent.extras!!.getInt("action",1)

        if(action == 2){
            tvDiscountPercentage.text = "Percentage Tax"
            tvDiscountCash.text = "Cash Tax"
            tvDiscountNewTotal.text = "Total After Tax"
        }

        tvDiscountNewTotalContent.text = indonesiaCurrencyFormat().format(totalPrice)

        etDiscountCash.doOnTextChanged { text, start, before, count ->
            if (etDiscountCash.hasFocus() && etDiscountCash.text.toString() != ""){
                if (action == 1){
                    discount = text.toString().toInt()
                    if (discount > totalPrice){
                        discount = totalPrice
                        etDiscountCash.setText(totalPrice.toString())
                    }

                    percentageDiscount =  (discount / totalPrice) * 100

                    etDiscountPercentage.setText(percentageDiscount.toString())
                    tvDiscountNewTotalContent.text = indonesiaCurrencyFormat().format(totalPrice - discount)
                }else if(action == 2){
                    tax = text.toString().toInt()
                    if (tax > totalPrice){
                        tax = totalPrice
                        etDiscountCash.setText(totalPrice.toString())
                    }

                    percentageDiscount =  tax / totalPrice * 100

                    etDiscountPercentage.setText(percentageDiscount.toString())
                    tvDiscountNewTotalContent.text = indonesiaCurrencyFormat().format(totalPrice + tax)
                }


                etDiscountCash.requestFocus()
            }
        }

        etDiscountCash.onClick {
            etDiscountCash.setText("")
            etDiscountPercentage.setText("")
            tvDiscountNewTotalContent.text = totalPrice.toString()
        }

        etDiscountPercentage.onClick {
            etDiscountCash.setText("")
            etDiscountPercentage.setText("")
            tvDiscountNewTotalContent.text = totalPrice.toString()
        }

        etDiscountPercentage.doOnTextChanged { text, start, before, count ->
            if (etDiscountPercentage.hasFocus() && etDiscountPercentage.text.toString() != ""){

                var value = text.toString().toInt()
                if (value > 100){
                    value = 100
                    etDiscountPercentage.setText("100")
                }
                if (action == 1)
                    discount = (totalPrice * value / 100)
                else if(action == 2)
                    tax = (totalPrice * value / 100)

                cashDiscount = totalPrice * value / 100
                etDiscountCash.setText(cashDiscount.toString())

                if (action == 1)
                    tvDiscountNewTotalContent.text = indonesiaCurrencyFormat().format(totalPrice - discount)
                else if (action == 2)
                    tvDiscountNewTotalContent.text = indonesiaCurrencyFormat().format(totalPrice + tax)

                etDiscountPercentage.requestFocus()
            }
        }

        btnDiscount.onClick {
            btnDiscount.startAnimation(normalClickAnimation())
            finish()
        }
    }

    override fun onBackPressed() {

        var discount = 0
        var tax = 0

        super.onBackPressed()
    }
}
