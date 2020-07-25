package com.chcreation.pointofsale.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.getMerchant
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.model.Transaction
import com.chcreation.pointofsale.presenter.TransactionPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_transaction.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.startActivity

class TransactionFragment : Fragment(), MainView {

    private lateinit var presenter: TransactionPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var adapter : TransactionRecyclerViewAdapter
    private var tmpCustomerItems: MutableList<Customer> = mutableListOf()
    private var tmpTransItems: MutableList<Transaction> = mutableListOf()
    private var currentTab = 0

   companion object{
       var transPosition = 0
       var transItems: MutableList<Transaction> = mutableListOf()
       var customerItems : MutableList<String> = mutableListOf()
       var transCodeItems: MutableList<String> = mutableListOf()
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

        tlTransaction.addTab(tlTransaction.newTab().setText("All"),true)
        tlTransaction.addTab(tlTransaction.newTab().setText("Pending"))
        tlTransaction.addTab(tlTransaction.newTab().setText("Success"))

        tlTransaction.tabMode = TabLayout.MODE_SCROLLABLE

        tlTransaction.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab!!.position
                fetchTransByCat()
            }

        })
    }

    override fun onStart() {
        super.onStart()

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = TransactionPresenter(this,mAuth,mDatabase)
        adapter = TransactionRecyclerViewAdapter(ctx, transItems, customerItems, transCodeItems){
            transPosition = it
            startActivity<DetailTransactionActivity>()
        }

        rvTransaction.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(ctx)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true

        rvTransaction.layoutManager = linearLayoutManager

        presenter.retrieveTransactions(getMerchant(ctx))
    }

    private fun fetchTransByCat(){
        if (currentTab == 0)
            transItems.addAll(tmpTransItems)
        else{
            for (data in tmpTransItems){
                if (currentTab == 1){
                    if (data.TOTAL_OUTSTANDING!! > 0)
                        transItems.add(data)
                }else if (currentTab == 2){
                    if (data.TOTAL_OUTSTANDING!! == 0)
                        transItems.add(data)
                }
            }
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_TRANS_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                tmpTransItems.clear()
                for (data in dataSnapshot.children){
                    val item = data.getValue(com.chcreation.pointofsale.model.Transaction::class.java)
                    if (item != null) {
                        tmpTransItems.add(item)
                        transCodeItems.add(data.key.toString())
                    }
                }
                fetchTransByCat()
                presenter.retrieveCustomers(getMerchant(ctx))
            }
        }else if (response == EMessageResult.FETCH_CUSTOMER_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                tmpCustomerItems.clear()
                for (data in dataSnapshot.children){
                    val item = data.getValue(Customer::class.java)
                    if (item != null) {
                        tmpCustomerItems.add(item)
                    }
                }
                fecthCustomer()
            }
        }
    }

    fun fecthCustomer(){
        customerItems.clear()

        for(data in transItems){
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

        adapter.notifyDataSetChanged()
    }

    override fun response(message: String) {
    }
}
