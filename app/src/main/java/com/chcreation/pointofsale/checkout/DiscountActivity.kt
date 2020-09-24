package com.chcreation.pointofsale.checkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.doOnTextChanged
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.home.HomeFragment.Companion.totalPrice
import kotlinx.android.synthetic.main.activity_discount.*
import org.jetbrains.anko.sdk27.coroutines.onClick

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
            btnDiscount.text = "Apply Tax"
        }

        tvDiscountNewTotalContent.text = currencyFormat(
            getLanguage(this),
            getCountry(this)
        ).format(totalPrice)

        etDiscountCash.doOnTextChanged { text, start, before, count ->
            if (etDiscountCash.hasFocus() && etDiscountCash.text.toString() != ""){
                if (action == 1){
                    discount = text.toString().toInt()
                    if (discount > totalPrice){
                        discount = totalPrice
                        etDiscountCash.setText(totalPrice.toString())
                    }

                    percentageDiscount =  discount * 100 / totalPrice

                    etDiscountPercentage.setText(percentageDiscount.toString())
                    tvDiscountNewTotalContent.text = currencyFormat( getLanguage(this),
                        getCountry(this)).format(totalPrice - discount)
                }else if(action == 2){
                    tax = text.toString().toInt()
                    if (tax > totalPrice){
                        tax = totalPrice
                        etDiscountCash.setText(totalPrice.toString())
                    }

                    percentageDiscount =  tax * 100 / totalPrice

                    etDiscountPercentage.setText(percentageDiscount.toString())
                    tvDiscountNewTotalContent.text = currencyFormat( getLanguage(this),
                        getCountry(this)).format(totalPrice + tax)
                }


                etDiscountCash.requestFocus()
            }
        }

        etDiscountCash.onClick {
            etDiscountCash.setText("")
            etDiscountPercentage.setText("")
            tvDiscountNewTotalContent.text = currencyFormat(getLanguage(this@DiscountActivity),
                getCountry(this@DiscountActivity)).format(totalPrice)
        }

        etDiscountPercentage.onClick {
            etDiscountCash.setText("")
            etDiscountPercentage.setText("")
            tvDiscountNewTotalContent.text = currencyFormat( getLanguage(this@DiscountActivity),
                getCountry(this@DiscountActivity)).format(totalPrice)
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
                    tvDiscountNewTotalContent.text = currencyFormat(getLanguage(this@DiscountActivity),
                        getCountry(this@DiscountActivity)).format(totalPrice - discount)
                else if (action == 2)
                    tvDiscountNewTotalContent.text = currencyFormat(getLanguage(this@DiscountActivity),
                        getCountry(this@DiscountActivity)).format(totalPrice + tax)

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
