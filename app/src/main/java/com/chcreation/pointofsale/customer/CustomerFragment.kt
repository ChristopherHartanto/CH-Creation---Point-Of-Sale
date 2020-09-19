package com.chcreation.pointofsale.customer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.*

import com.chcreation.pointofsale.checkout.CheckOutActivity
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.isCustomer
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.presenter.CustomerPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_customer.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.startActivity
import java.util.*
import kotlin.collections.ArrayList

class CustomerFragment : Fragment(), MainView {

    private lateinit var adapter: CustomerRecyclerViewAdapter
    private var customerItems : ArrayList<Customer> = arrayListOf()
    private var filteredCustomerItems : ArrayList<Customer> = arrayListOf()
    private lateinit var presenter: CustomerPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private var searchFilter = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_customer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = CustomerPresenter(this,mAuth,mDatabase,ctx)

        adapter = CustomerRecyclerViewAdapter(ctx,filteredCustomerItems){
            ctx.startActivity(intentFor<CustomerDetailActivity>(ECustomer.CODE.toString() to filteredCustomerItems[it].CODE))
        }

        fbCustomer.onClick {
            startActivity(intentFor<NewCustomerActivity>("checkOut" to false))
        }
        rvCustomer.adapter = adapter
        rvCustomer.layoutManager = LinearLayoutManager(ctx)

        srCustomer.onRefresh {
            presenter.retrieveCustomers()
        }

        svCustomerSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                searchFilter = newText
                fetchData()
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

        })
    }

    override fun onStart() {
        super.onStart()
        presenter.retrieveCustomers()
    }

    fun fetchData(){
        filteredCustomerItems.clear()
        srCustomer.isRefreshing = true

        for (data in customerItems){
            if (searchFilter == "")
                filteredCustomerItems.add(data)
            else{
                if (data.NAME.toString().toLowerCase(Locale.getDefault()).contains(searchFilter.toLowerCase(
                        Locale.getDefault()))){
                    filteredCustomerItems.add(data)
                }
                else if (data.EMAIL.toString().toLowerCase(Locale.getDefault()).contains(searchFilter.toLowerCase(
                        Locale.getDefault()))){
                    filteredCustomerItems.add(data)
                }
                else if (data.PHONE.toString().toLowerCase(Locale.getDefault()).contains(searchFilter.toLowerCase(
                        Locale.getDefault()))){
                    filteredCustomerItems.add(data)
                }
                else if (data.ADDRESS.toString().toLowerCase(Locale.getDefault()).contains(searchFilter.toLowerCase(
                        Locale.getDefault()))){
                    filteredCustomerItems.add(data)
                }
            }
        }
        adapter.notifyDataSetChanged()
        srCustomer.isRefreshing = false
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (isVisible && isResumed){
            if (response == EMessageResult.FETCH_CUSTOMER_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    customerItems.clear()
                    adapter.notifyDataSetChanged()

                    for (data in dataSnapshot.children){
                        val item = data.getValue(Customer::class.java)
                        if (item!!.STATUS_CODE == EStatusCode.ACTIVE.toString()){
                            customerItems.add(item)
                        }
                    }
                    fetchData()
                }
                svCustomerSearch.visibility = View.VISIBLE
                srCustomer.isRefreshing = false
            }
        }
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
