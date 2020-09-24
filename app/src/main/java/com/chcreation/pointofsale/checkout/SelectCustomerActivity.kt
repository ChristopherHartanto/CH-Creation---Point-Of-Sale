package com.chcreation.pointofsale.checkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.EStatusCode
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.customer.CustomerRecyclerViewAdapter
import com.chcreation.pointofsale.customer.NewCustomerActivity
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.presenter.CustomerPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_select_customer.*
import kotlinx.android.synthetic.main.fragment_customer.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.util.*
import kotlin.collections.ArrayList

class SelectCustomerActivity : AppCompatActivity(), MainView {

    companion object{
        var selectCustomerName = ""
        var selectCustomerCode = ""
    }

    private lateinit var adapter: CustomerRecyclerViewAdapter
    private var customerItems : ArrayList<Customer> = arrayListOf()
    private var filteredCustomerItems : ArrayList<Customer> = arrayListOf()
    private lateinit var presenter: CustomerPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private var searchFilter = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_customer)

        supportActionBar!!.title = "Select Customer"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = CustomerPresenter(this,mAuth,mDatabase,this)

        adapter = CustomerRecyclerViewAdapter(this,filteredCustomerItems){
            selectCustomerCode = filteredCustomerItems[it].CODE.toString()
            selectCustomerName = filteredCustomerItems[it].NAME.toString()
            finish()
        }

        rvSelectCustomer.adapter = adapter
        rvSelectCustomer.layoutManager = LinearLayoutManager(this)

        svSelectCustomerSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

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

        fbSelectCustomer.onClick {
            startActivity(intentFor<NewCustomerActivity>("checkOut" to true))
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    fun fetchData(){
        filteredCustomerItems.clear()

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
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_CUSTOMER_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                customerItems.clear()
                for (data in dataSnapshot.children){
                    val item = data.getValue(Customer::class.java)
                    if(item!!.STATUS_CODE == EStatusCode.ACTIVE.toString()){

                        customerItems.add(item)
                    }
                }
                fetchData()
            }
            svSelectCustomerSearch.visibility = View.VISIBLE
            pbSelectCustomer.visibility = View.GONE
        }
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
