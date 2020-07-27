package com.chcreation.pointofsale.customer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.EMessageResult

import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.checkout.CheckOutActivity
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.isCustomer
import com.chcreation.pointofsale.getMerchant
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
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.startActivity

class CustomerFragment : Fragment(), MainView {

    private lateinit var adapter: CustomerRecyclerViewAdapter
    private var customerItems : ArrayList<Customer> = arrayListOf()
    private lateinit var presenter: CustomerPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference

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

        adapter = CustomerRecyclerViewAdapter(ctx,customerItems){
        }

        fbCustomer.onClick {
            startActivity<NewCustomerActivity>()
        }
        rvCustomer.adapter = adapter
        rvCustomer.layoutManager = LinearLayoutManager(ctx)

        srCustomer.onRefresh {
            presenter.retrieveCustomers()
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.retrieveCustomers()
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_CUSTOMER_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                customerItems.clear()
                adapter.notifyDataSetChanged()

                for (data in dataSnapshot.children){
                    val item = data.getValue(Customer::class.java)

                    customerItems.add(item!!)
                    adapter.notifyDataSetChanged()
                }
                srCustomer.isRefreshing = false
            }

            srCustomer.isRefreshing = false
        }
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
