package com.chcreation.pointofsale.transaction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager

import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.checkout.CartRecyclerViewAdapter
import com.chcreation.pointofsale.model.Cart
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_detail_transaction_list_product.*
import org.jetbrains.anko.support.v4.ctx

/**
 * A simple [Fragment] subclass.
 */
class DetailTransactionListProductFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail_transaction_list_product, container, false)
    }
    private lateinit var adapter: CartRecyclerViewAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        val gson = Gson()
        val arrayCartType = object : TypeToken<MutableList<Cart>>() {}.type
        val purchasedItems : MutableList<Cart> = gson.fromJson(TransactionFragment.transItems[TransactionFragment.transPosition].DETAIL,arrayCartType)

        adapter = CartRecyclerViewAdapter(ctx, purchasedItems){

        }

        rvDetailTransaction.adapter = adapter
        rvDetailTransaction.layoutManager = LinearLayoutManager(ctx)

    }

    override fun onStart() {
        super.onStart()

//        val discount = TransactionFragment.transItems[TransactionFragment.transPosition].DISCOUNT
//        val note = TransactionFragment.transItems[TransactionFragment.transPosition].NOTE
//
//        if (discount != 0){
//            tvDetailTransactionDiscount.text = TransactionFragment.transItems[TransactionFragment.transPosition].DISCOUNT.toString()
//            tvDetailTransactionSubTotal.visibility = View.VISIBLE
//            tvDetailTransactionSubTotal.text = TransactionFragment.transItems[TransactionFragment.transPosition].TOTAL_PRICE.toString()
//        }
//        if (note != "")
//            tvDetailTransactionNote.text = TransactionFragment.transItems[TransactionFragment.transPosition].NOTE.toString()
//
//        tvDetailTransactionTotalPrice.text = indonesiaCurrencyFormat().format(TransactionFragment.transItems[TransactionFragment.transPosition].TOTAL_PRICE).toString()
    }
}
