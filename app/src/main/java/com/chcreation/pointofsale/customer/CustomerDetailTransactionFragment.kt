package com.chcreation.pointofsale.customer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.*

import com.chcreation.pointofsale.checkout.CheckOutActivity
import com.chcreation.pointofsale.checkout.ReceiptActivity
import com.chcreation.pointofsale.customer.CustomerDetailActivity.Companion.custCode
import com.chcreation.pointofsale.model.Enquiry
import com.chcreation.pointofsale.model.Transaction
import com.chcreation.pointofsale.presenter.CustomerPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_customer_detail_transaction.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.textColorResource

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
    private var totalGrossEarning = 0

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
            CheckOutActivity.transCode = enquiryItems[it].TRANS_KEY!!
            ctx.startActivity<ReceiptActivity>()
        }

        rvCustomerDetailTransaction.adapter = adapter
        rvCustomerDetailTransaction.layoutManager = LinearLayoutManager(ctx)
        tvCustomerDetailTransactionTotalGrossEarning.text = currencyFormat( getLanguage(ctx),
            getCountry(ctx)).format(0)

        presenter.retrieveCustomerTransaction(custCode)
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (isVisible){
            if (response == EMessageResult.FETCH_CUSTOMER_TRANSACTION_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    enquiryItems.clear()
                    var tmpItems = mutableListOf<Enquiry>()
                    var checkTransKey = ""

                    for ((index,data) in dataSnapshot.children.withIndex()){
                        val item = data.getValue(Enquiry::class.java)
                        tmpItems.add(item!!)

                        if (index == 0){
                            checkTransKey = item.TRANS_KEY.toString()
                            presenter.retrieveTransaction(item.TRANS_KEY.toString())
                        }else if (item.TRANS_KEY.toString() != checkTransKey){
                            checkTransKey = item.TRANS_KEY.toString()
                            presenter.retrieveTransaction(item.TRANS_KEY.toString())
                        }
                    }
                    var itemGroupByReceipt = tmpItems.groupBy { it.TRANS_KEY }
                    for (data in itemGroupByReceipt){
                        val item = data.value
                        enquiryItems.add(item[0])
                    }
                    enquiryItems.reverse()
                    adapter.notifyDataSetChanged()
                    totalTransaction = enquiryItems.size
                    tvCustomerDetailTransactionTotal.text = totalTransaction.toString()
                }
            }
            if (response == EMessageResult.FETCH_TRANS_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    val transItem = dataSnapshot.getValue(Transaction::class.java)

                    if (transItem != null) {
                        if (transItem.STATUS_CODE != EStatusCode.CANCEL.toString())
                            totalGrossEarning += transItem.TOTAL_PRICE!!

                        if (transItem.STATUS_CODE == EStatusCode.PENDING.toString())
                            tvCustomerDetailTransactionTotalGrossEarning.textColorResource = R.color.colorPrimary

                        tvCustomerDetailTransactionTotalGrossEarning.text = currencyFormat(
                            getLanguage(ctx),
                            getCountry(ctx)
                        ).format(totalGrossEarning)
                    }

                }
            }
        }

    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
