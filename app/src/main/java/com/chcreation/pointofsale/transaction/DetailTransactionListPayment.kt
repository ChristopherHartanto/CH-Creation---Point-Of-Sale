package com.chcreation.pointofsale.transaction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.*

import com.chcreation.pointofsale.checkout.CartRecyclerViewAdapter
import com.chcreation.pointofsale.model.Cart
import com.chcreation.pointofsale.model.Payment
import com.chcreation.pointofsale.presenter.TransactionPresenter
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transCodeItems
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transPosition
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_detail_transaction.*
import kotlinx.android.synthetic.main.fragment_detail_transaction_list_payment.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.yesButton

/**
 * A simple [Fragment] subclass.
 */
class DetailTransactionListPayment : Fragment(), MainView {

    private lateinit var adapter: DetailTransactionListPaymentRecyclerViewAdapter
    private lateinit var presenter: TransactionPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private var itemListPayments : MutableList<Payment> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail_transaction_list_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = TransactionPresenter(this,mAuth,mDatabase)

        adapter = DetailTransactionListPaymentRecyclerViewAdapter(ctx,itemListPayments){

        }

        rvListPayment.adapter = adapter
        rvListPayment.layoutManager = LinearLayoutManager(ctx)

        presenter.retrieveTransactionListPayments(transCodeItems[transPosition],getMerchant(ctx))
    }

    override fun onStart() {
        super.onStart()

    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_TRANS_LIST_PAYMENT_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                itemListPayments.clear()
                for (data in dataSnapshot.children){
                    val item = data.getValue(Payment::class.java)
                    itemListPayments.add(item!!)
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
