package com.chcreation.pointofsale.checkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.EStatusCode
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.customer.CustomerRecyclerViewAdapter
import com.chcreation.pointofsale.customer.NewCustomerActivity
import com.chcreation.pointofsale.getMerchant
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.presenter.CustomerPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_select_customer.*
import kotlinx.android.synthetic.main.fragment_customer.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.startActivity

class SelectCustomerActivity : AppCompatActivity(), MainView {

    companion object{
        var selectCustomerName = ""
        var selectCustomerCode = ""
    }

    private lateinit var adapter: CustomerRecyclerViewAdapter
    private var customerItems : ArrayList<Customer> = arrayListOf()
    private lateinit var presenter: CustomerPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_customer)

        supportActionBar!!.title = "Select Customer"

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = CustomerPresenter(this,mAuth,mDatabase,this)

        adapter = CustomerRecyclerViewAdapter(this,customerItems){
            selectCustomerCode = customerItems[it].CODE.toString()
            selectCustomerName = customerItems[it].NAME.toString()
            finish()
        }

        rvSelectCustomer.adapter = adapter
        rvSelectCustomer.layoutManager = LinearLayoutManager(this)

    }


    override fun onStart() {
        super.onStart()
        presenter.retrieveCustomers()

        fbSelectCustomer.onClick {
            startActivity<NewCustomerActivity>()
            finish()
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_CUSTOMER_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                customerItems.clear()
                for (data in dataSnapshot.children){
                    val item = data.getValue(Customer::class.java)
                    if(item!!.STATUS_CODE == EStatusCode.ACTIVE.toString()){

                        customerItems.add(item)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            pbSelectCustomer.visibility = View.GONE
        }
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
