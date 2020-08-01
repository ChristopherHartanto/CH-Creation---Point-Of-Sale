package com.chcreation.pointofsale.customer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.EMessageResult

import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.customer.CustomerDetailActivity.Companion.custCode
import com.chcreation.pointofsale.model.Enquiry
import com.chcreation.pointofsale.presenter.CustomerPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_customer_detail_transaction.*
import org.jetbrains.anko.support.v4.ctx

/**
 * A simple [Fragment] subclass.
 */
class CustomerDetailTransactionFragment : Fragment(), MainView {

    private lateinit var adapter: CustomerDetailTransactionRecyclerViewAdapter
    private lateinit var presenter: CustomerPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private var enquiryItems: MutableList<Enquiry> = mutableListOf()
    private var totalTransaction = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_customer_detail_transaction, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = CustomerPresenter(this,mAuth,mDatabase,ctx)
        adapter = CustomerDetailTransactionRecyclerViewAdapter(ctx,enquiryItems){

        }

        rvCustomerDetailTransaction.adapter = adapter
        rvCustomerDetailTransaction.layoutManager = LinearLayoutManager(ctx)

        presenter.retrieveCustomerTransaction(custCode)
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_CUSTOMER_TRANSACTION_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                enquiryItems.clear()
                var tmpItems = mutableListOf<Enquiry>()
                for (data in dataSnapshot.children){
                    val item = data.getValue(Enquiry::class.java)

                    tmpItems.add(item!!)
                }
                var itemGroupByReceipt = tmpItems.groupBy { it.TRANS_KEY }
                for (data in itemGroupByReceipt){
                    val item = data.value
                    enquiryItems.add(item[0])
                }
                adapter.notifyDataSetChanged()
                totalTransaction = enquiryItems.size
                tvCustomerDetailTransactionTotal.text = "Total Transaction : $totalTransaction"
            }
        }
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
