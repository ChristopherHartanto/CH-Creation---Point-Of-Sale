package com.chcreation.pointofsale.checkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.widget.doOnTextChanged
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.home.HomeFragment.Companion.totalPrice
import kotlinx.android.synthetic.main.activity_discount.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.textChangedListener

class DiscountActivity : AppCompatActivity() {

    companion object{
        var newTotal = 0
    }

    private var percentageDiscount = 0
    private var cashDiscount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discount)

        tvDiscountNewTotalContent.text = totalPrice.toString()

        etDiscountCash.doOnTextChanged { text, start, before, count ->
            if (etDiscountCash.hasFocus() && etDiscountCash.text.toString() != ""){

                var value = text.toString().toInt()
                if (value > totalPrice){
                    value = totalPrice
                    etDiscountCash.setText(totalPrice.toString())
                }

                newTotal = totalPrice -  value
                percentageDiscount =  value / totalPrice * 100

                etDiscountPercentage.setText(percentageDiscount.toString())
                tvDiscountNewTotalContent.text = newTotal.toString()

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

                newTotal = totalPrice - (totalPrice * value / 100)
                cashDiscount = totalPrice * value / 100

                etDiscountCash.setText(cashDiscount.toString())
                tvDiscountNewTotalContent.text = (newTotal).toString()

                etDiscountPercentage.requestFocus()
            }
        }

        btnDiscount.onClick {
            finish()
        }
    }
}
