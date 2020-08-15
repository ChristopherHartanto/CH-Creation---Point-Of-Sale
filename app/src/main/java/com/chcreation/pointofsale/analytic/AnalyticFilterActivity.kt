package com.chcreation.pointofsale.analytic

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.analytic.AnalyticFragment.Companion.isDiscount
import com.chcreation.pointofsale.analytic.AnalyticFragment.Companion.isTax
import com.chcreation.pointofsale.analytic.AnalyticFragment.Companion.month
import com.chcreation.pointofsale.analytic.AnalyticFragment.Companion.monthName
import com.chcreation.pointofsale.analytic.AnalyticFragment.Companion.userCode
import com.chcreation.pointofsale.analytic.AnalyticFragment.Companion.userName
import com.chcreation.pointofsale.analytic.AnalyticFragment.Companion.year
import com.chcreation.pointofsale.model.User
import com.chcreation.pointofsale.model.UserList
import com.chcreation.pointofsale.presenter.UserPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_analytic_filter.*
import kotlinx.android.synthetic.main.activity_user_detail.*
import kotlinx.android.synthetic.main.fragment_user_list.*
import kotlinx.android.synthetic.main.nav_header_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import java.util.*

class AnalyticFilterActivity : AppCompatActivity(),MainView {

    private lateinit var presenter: UserPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private var userCodes: MutableList<String> = mutableListOf()
    private var userNames: MutableList<String> = mutableListOf()
    private lateinit var spMonthAdapter: ArrayAdapter<EMonth>
    private lateinit var spYearAdapter: ArrayAdapter<String>
    private lateinit var spUserAdapter: ArrayAdapter<String>
    private var yearItems = mutableListOf<String>("2020","2021","2022","2023","2024","2025")

    companion object{
        var monthItems = mutableListOf<EMonth>(
            EMonth.January,EMonth.February,EMonth.March,EMonth.April,EMonth.March,EMonth.June,EMonth.July,
            EMonth.August,EMonth.September,EMonth.October,EMonth.November,EMonth.December
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytic_filter)

        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = UserPresenter(this,mAuth,mDatabase,this)

        presenter.retrieveUserLists()

    }

    override fun onStart() {
        super.onStart()

        btnAnalyticFilter.onClick {
            btnAnalyticFilter.startAnimation(normalClickAnimation())
            finish()
        }

        spMonthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,monthItems)
        spMonthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spAnalyticMonth.adapter = spMonthAdapter
        if (month == 99)
            spAnalyticMonth.setSelection(getCurrentMonth())
        else
            spAnalyticMonth.setSelection(month-1)

        spAnalyticMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                month = monthItems[position].value
                monthName = monthItems[position].toString()
            }

        }

        spYearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,yearItems)
        spYearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spAnalyticYear.adapter = spYearAdapter
        if (year == 99)
            spAnalyticYear.setSelection(yearItems.indexOf(getCurrentYear().toString()))
        else
            spAnalyticYear.setSelection(yearItems.indexOf(year.toString()))
        spAnalyticYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                year = yearItems[position].toInt()
            }

        }

        spUserAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,userNames)
        spUserAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spAnalyticUser.adapter = spUserAdapter

        spAnalyticUser.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (userCodes.size > 0 && userNames.size > 0){
                    userCode = userCodes[position]
                    userName = userNames[position]
                }
            }

        }

        cbAnalyticFilterTax.isChecked = isTax

        cbAnalyticFilterTax.onCheckedChange { buttonView, isChecked ->
            cbAnalyticFilterTax.isChecked = isChecked
            isTax = isChecked
        }

        cbAnalyticFilterDiscount.isChecked = isDiscount

        cbAnalyticFilterDiscount.onCheckedChange { buttonView, isChecked ->
            cbAnalyticFilterDiscount.isChecked = isChecked
            isDiscount = isChecked
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_USER_LIST_SUCCESS.toString()){
            if(dataSnapshot.exists() && dataSnapshot.value != null && dataSnapshot.value != ""){
                userCodes.clear()
                userNames.clear()
                userCodes.add("")
                userNames.add("All")
                val gson = Gson()
                val arrayUserListType = object : TypeToken<MutableList<UserList>>() {}.type
                val items : MutableList<UserList> = gson.fromJson(dataSnapshot.value.toString(),arrayUserListType)

                items.sortBy { it.CREATED_DATE }
                GlobalScope.launch {
                    for (data in items){
                        if (data.STATUS_CODE == EStatusCode.ACTIVE.toString()){

                            presenter.retrieveUser(data.USER_CODE.toString())
                        }
                    }
                }
            }
        }
        if (response == EMessageResult.FETCH_USER_SUCCESS.toString()){
            if(dataSnapshot.exists()){
                val item = dataSnapshot.getValue(User::class.java)
                userNames.add(item!!.NAME.toString())
                userCodes.add(dataSnapshot.key.toString())
            }
            if (userNames.size == userCodes.size){
                if (userName != "")
                    spAnalyticUser.setSelection(userNames.indexOf(userName))
                spUserAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun response(message: String) {

    }
}
