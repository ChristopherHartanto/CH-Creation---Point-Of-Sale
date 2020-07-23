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
        var newTotal = 0F
    }

    private var percentageDiscount = 0F
    private var cashDiscount = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discount)

        tvDiscountNewTotalContent.text = totalPrice.toString()

        etDiscountCash.doOnTextChanged { text, start, before, count ->
            if (etDiscountCash.hasFocus()){

                newTotal = totalPrice -  text.toString().toFloat()
                percentageDiscount =  text.toString().toFloat() / totalPrice * 100

                etDiscountPercentage.setText(percentageDiscount.toString())
                tvDiscountNewTotalContent.text = newTotal.toString()

                etDiscountCash.clearFocus()
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
            if (etDiscountPercentage.hasFocus()){

                newTotal = totalPrice * text.toString().toFloat() / 100
                cashDiscount = totalPrice * text.toString().toFloat() / 100

                etDiscountCash.setText(cashDiscount.toString())
                tvDiscountNewTotalContent.text = (totalPrice - newTotal).toString()

                etDiscountPercentage.clearFocus()
            }
        }

    }
}
