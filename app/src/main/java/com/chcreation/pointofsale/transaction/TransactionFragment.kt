package com.chcreation.pointofsale.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.analytic.AnalyticFragment
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.model.Transaction
import com.chcreation.pointofsale.model.User
import com.chcreation.pointofsale.model.UserList
import com.chcreation.pointofsale.presenter.TransactionPresenter
import com.chcreation.pointofsale.presenter.UserPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_analytic_filter.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.startActivity

class TransactionFragment : Fragment(), MainView {

    private lateinit var presenter: TransactionPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var adapter : TransactionRecyclerViewAdapter
    private var tmpCustomerItems: MutableList<Customer> = mutableListOf()
    private var tmpCustomerNameItems: MutableList<String> = mutableListOf()
    private var tmpTransItems: MutableList<Transaction> = mutableListOf()
    private var tmpTransCodeItems: MutableList<Int> = mutableListOf()
    private var userCodes: MutableList<String> = mutableListOf()
    private var userNames: MutableList<String> = mutableListOf()
    private lateinit var spMonthAdapter: ArrayAdapter<EMonth>
    private lateinit var spYearAdapter: ArrayAdapter<String>
    private lateinit var spUserAdapter: ArrayAdapter<String>
    private var yearItems = mutableListOf<String>("All","2020","2021","2022","2023","2024","2025")
    private var monthItems = mutableListOf<EMonth>(
        EMonth.All,EMonth.January,EMonth.February,EMonth.March,EMonth.April,EMonth.May,EMonth.June,
        EMonth.July,EMonth.August,EMonth.September,EMonth.October,EMonth.November,EMonth.December
    )
    private var currentTab = 0
    private var month = 99
    private var monthName = ""
    private var year = 99
    private var userCode = ""
    private var userName = ""

   companion object{
       var transPosition = 0
       var transItems: MutableList<Transaction> = mutableListOf()
       var customerItems : MutableList<String> = mutableListOf()
       var transCodeItems: MutableList<Int> = mutableListOf()
   }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = TransactionPresenter(this,mAuth,mDatabase,ctx)

        tlTransaction.addTab(tlTransaction.newTab().setText("All"),true)
        tlTransaction.addTab(tlTransaction.newTab().setText("Pending"))
        tlTransaction.addTab(tlTransaction.newTab().setText("Success"))
        tlTransaction.addTab(tlTransaction.newTab().setText("Cancel"))

        tlTransaction.tabMode = TabLayout.MODE_FIXED

        tlTransaction.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab!!.position
                clearData()
                fetchTransByCat()
            }

        })

        spMonthAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item,monthItems)
        spMonthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spTransactionMonth.adapter = spMonthAdapter
        if (month != 99)
            spTransactionMonth.setSelection(month)

        spTransactionMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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

        spYearAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item,yearItems)
        spYearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spTransactionYear.adapter = spYearAdapter
        if (year != 99)
            spTransactionYear.setSelection(yearItems.indexOf(getCurrentYear().toString()))
        else
            spTransactionYear.setSelection(yearItems.indexOf(year.toString()))
        spTransactionYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0)
                    year = 99
                else
                    year = yearItems[position].toInt()
            }

        }

        spUserAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item,userNames)
        spUserAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spTransactionUser.adapter = spUserAdapter

        spTransactionUser.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
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

        tvTransactionOpenPopupFilter.onClick {
            cvTransactionPopUpFilter.visibility = if (cvTransactionPopUpFilter.isVisible) View.GONE else View.VISIBLE
            tvTransactionOpenPopupFilter.text = if (cvTransactionPopUpFilter.isVisible) "Close" else "Filter"
        }

        btnTransactionApplyFilter.onClick {
            btnTransactionApplyFilter.startAnimation(normalClickAnimation())
            cvTransactionPopUpFilter.visibility = View.GONE

            tvTransactionFilterDate.text = "${monthName} - ${if (year == 99) "All" else year}"
            tvTransactionFilterName.text = userName

            tvTransactionOpenPopupFilter.text = "Filter"

            fetchTransByCat()
        }

        srTransaction.onRefresh {
            fetchTransByCat()
            GlobalScope.launch {
                presenter.retrieveUserLists()
            }
            srTransaction.isRefreshing = false
        }
    }

    override fun onStart() {
        super.onStart()

        month = 99
        year = 99
        userCode = ""
        userName = ""
        tvTransactionFilterDate.text = "All - All"
        tvTransactionFilterName.text = "All"

        clearData()
        adapter = TransactionRecyclerViewAdapter(ctx,requireActivity(), transItems, customerItems, transCodeItems){
            transPosition = it
            startActivity<DetailTransactionActivity>()
        }

        rvTransaction.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(ctx)
//        linearLayoutManager.reverseLayout = true
//        linearLayoutManager.stackFromEnd = true

        rvTransaction.layoutManager = linearLayoutManager
        GlobalScope.launch {
            presenter.retrieveTransactions()
            presenter.retrieveUserLists()
        }
    }

    private fun clearData(){
        transPosition = 0
        transCodeItems.clear()
        customerItems.clear()
        transItems.clear()

    }

    private fun fetchTransByCat(){
        clearData()
        fecthCustomer()

        for ((index,data) in tmpTransItems.withIndex()){
            if ((month == 99 || (getMonth(data.CREATED_DATE.toString()) == month-1))
                && ( year == 99 || (getYear(data.CREATED_DATE.toString()) == year))
                && ((data.CREATED_BY.toString() == userCode) || userCode == "")) {
                if (currentTab == 0){
                    transItems.add(data)
                    customerItems.add(tmpCustomerNameItems[index])
                    transCodeItems.add(tmpTransCodeItems[index])
                }
                if (currentTab == 1) {
                    if (data.TOTAL_OUTSTANDING!! > 0 && data.STATUS_CODE != EStatusCode.CANCEL.toString()) {
                        transItems.add(data)
                        customerItems.add(tmpCustomerNameItems[index])
                        transCodeItems.add(tmpTransCodeItems[index])
                    }
                } else if (currentTab == 2) {
                    if (data.STATUS_CODE == EStatusCode.DONE.toString()) {
                        transItems.add(data)
                        customerItems.add(tmpCustomerNameItems[index])
                        transCodeItems.add(tmpTransCodeItems[index])
                    }
                } else if (currentTab == 3) {
                    if (data.STATUS_CODE == EStatusCode.CANCEL.toString()) {
                        transItems.add(data)
                        customerItems.add(tmpCustomerNameItems[index])
                        transCodeItems.add(tmpTransCodeItems[index])
                    }
                }
            }
        }

        transItems.reverse()
        customerItems.reverse()
        transCodeItems.reverse()
        if (transItems.size == transCodeItems.size && transItems.size == customerItems.size)
            adapter.notifyDataSetChanged()
    }


    fun fecthCustomer(){
        customerItems.clear()
        tmpCustomerNameItems.clear()
        for(data in tmpTransItems){
            if (data.CUST_CODE == "")
                customerItems.add("")
            else{
                var check = false

                for (customer in tmpCustomerItems){
                    if (customer.CODE == data.CUST_CODE){
                        customerItems.add(customer.NAME.toString())
                        check = true
                    }
                }
                if (!check)
                    customerItems.add("")
            }
        }
        tmpCustomerNameItems.addAll(customerItems)
        customerItems.clear()
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (context != null && isResumed && isVisible){
            if (response == EMessageResult.FETCH_TRANS_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    tmpTransItems.clear()
                    tmpTransCodeItems.clear()
                    for (data in dataSnapshot.children){
                        val item = data.getValue(com.chcreation.pointofsale.model.Transaction::class.java)
                        if (item != null) {
                            tmpTransItems.add(item)
                            tmpTransCodeItems.add(data.key!!.toInt())
                        }
                    }
                    presenter.retrieveCustomers()
                }
                pbTransaction.visibility = View.GONE
            }else if (response == EMessageResult.FETCH_CUSTOMER_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    tmpCustomerItems.clear()
                    for (data in dataSnapshot.children){
                        val item = data.getValue(Customer::class.java)
                        if (item != null) {
                            tmpCustomerItems.add(item)
                        }
                    }
                }
            }
            else if (response == EMessageResult.FETCH_USER_LIST_SUCCESS.toString()){
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
            else if (response == EMessageResult.FETCH_USER_SUCCESS.toString()){
                if(dataSnapshot.exists()){
                    val item = dataSnapshot.getValue(User::class.java)
                    userNames.add(item!!.NAME.toString())
                    userCodes.add(dataSnapshot.key.toString())
                }
                if (userNames.size == userCodes.size){
                    if (userName != "")
                        spTransactionUser.setSelection(userNames.indexOf(userName))
                    spUserAdapter.notifyDataSetChanged()
                }
            }
            fetchTransByCat()
        }
    }

    override fun response(message: String) {
    }
}
