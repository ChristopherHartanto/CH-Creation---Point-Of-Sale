package com.chcreation.pointofsale.product

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.chcreation.pointofsale.*
import kotlinx.android.synthetic.main.activity_installment_plan.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class InstallmentPlanActivity : AppCompatActivity() {

    private lateinit var spMonthAdapter: ArrayAdapter<String>
    private lateinit var spInterestAdapter: ArrayAdapter<String>
    private var monthItems = arrayListOf(3,4,5,6,7,8,9,10,11,12)
    private var interestItems = arrayListOf(1,2,3,3.5F,4,5)
    private var monthStringItems = arrayListOf("3 month","4 month","5 month","6 month","7 month","8 month","9 month","10 month","11 month","12 month")
    private var interestStringItems = arrayListOf("1%","2%","3%","3.5%","4%","5%")
    private var selectedMonth = 0
    private var selectedInterest = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installment_plan)

        supportActionBar!!.title = "Installment Plan"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var price = intent.extras!!.getInt("price",0)

        tvInstallmentPlanPrice.text = currencyFormat(  getLanguage(this),
            getCountry(this)
        ).format(price)

        spMonthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,monthStringItems)
        spInstallmentPlanMonth.adapter = spMonthAdapter
        spInstallmentPlanMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedMonth = position
            }

        }
        spInstallmentPlanMonth.gravity = Gravity.CENTER

        spInterestAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,interestStringItems)
        spInstallmentPlanInterest.adapter = spInterestAdapter
        spInstallmentPlanInterest.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedInterest = position
            }

        }
        spInstallmentPlanInterest.gravity = Gravity.CENTER


        btnInstallmentPlan.onClick {
            btnInstallmentPlan.startAnimation(normalClickAnimation())
            val dp = if (etInstallmentPlanDp.text.toString() == "") 0 else etInstallmentPlanDp.text.toString().toInt()
            price -= dp
            val totalPerMonth = (price.toFloat() + (price * interestItems[selectedInterest].toString().toFloat() * monthItems[selectedMonth].toFloat() / 100) ) / monthItems[selectedMonth].toFloat()

            tvInstallmentPlanTotalMonth.text = "${currencyFormat(getLanguage(this@InstallmentPlanActivity),
                getCountry(this@InstallmentPlanActivity)).format(totalPerMonth)} / month"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }
}
