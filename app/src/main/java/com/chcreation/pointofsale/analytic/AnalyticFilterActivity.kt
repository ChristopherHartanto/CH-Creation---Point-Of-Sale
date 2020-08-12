package com.chcreation.pointofsale.analytic

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.analytic.AnalyticFragment.Companion.endDate
import com.chcreation.pointofsale.analytic.AnalyticFragment.Companion.endMonth
import com.chcreation.pointofsale.analytic.AnalyticFragment.Companion.endYear
import com.chcreation.pointofsale.analytic.AnalyticFragment.Companion.startDate
import com.chcreation.pointofsale.analytic.AnalyticFragment.Companion.startMonth
import com.chcreation.pointofsale.analytic.AnalyticFragment.Companion.startYear
import kotlinx.android.synthetic.main.activity_analytic_filter.*
import kotlinx.android.synthetic.main.nav_header_main.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.util.*

class AnalyticFilterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytic_filter)

        etAnalyticFilterStartDate.onClick {

            val dpd = DatePickerDialog(this@AnalyticFilterActivity, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                // Display Selected date in TextView
                etAnalyticFilterStartDate.setText("$dayOfMonth $monthOfYear, $year")
            }, startDate, startMonth, startYear)
            dpd.show()
        }

        etAnalyticFilterEndDate.onClick {

            val dpd = DatePickerDialog(this@AnalyticFilterActivity, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                // Display Selected date in TextView
                etAnalyticFilterStartDate.setText("$dayOfMonth $monthOfYear, $year")
            }, endDate, endMonth, endYear)
            dpd.show()
        }

        btnAnalyticFilter.onClick {
            finish()
        }
    }
}
