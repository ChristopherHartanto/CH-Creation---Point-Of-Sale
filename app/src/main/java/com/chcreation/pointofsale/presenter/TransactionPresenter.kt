package com.chcreation.pointofsale.presenter

import android.content.Context
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.transCode
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.transDate
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.model.Transaction
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*


class TransactionPresenter(private val view: MainView, private val auth: FirebaseAuth, private val database: DatabaseReference, private val context: Context){

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun retrieveTransactions(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_TRANS_SUCCESS.toString())
            }

        }
        database.child(ETable.TRANSACTION.toString())
            .child(getMerchantCredential(context))
            .child(getMerchant(context))
            .addListenerForSingleValueEvent(postListener)
    }

    fun retrieveTransactionListPayments(transactionCode:Int){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_TRANS_LIST_PAYMENT_SUCCESS.toString())
            }

        }
        database.child(ETable.PAYMENT.toString())
            .child(getMerchantCredential(context))
            .child(getMerchant(context))
            .child(transactionCode.toString())
            .addListenerForSingleValueEvent(postListener)
    }

    fun retrieveCustomers(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_CUSTOMER_SUCCESS.toString())
            }

        }
        database.child(ETable.CUSTOMER.toString())
            .child(getMerchantCredential(context))
            .child(getMerchant(context))
            .addListenerForSingleValueEvent(postListener)
    }

    fun cancelTransaction(transactionCode: Int){
        database.child(ETable.TRANSACTION.toString())
            .child(getMerchantCredential(context))
            .child(getMerchant(context))
            .child(transactionCode.toString())
            .child(ETransaction.STATUS_CODE.toString())
            .setValue(EStatusCode.CANCEL.toString()).addOnFailureListener {
                view.response(it.message.toString())
            }
            .addOnSuccessListener {
                cancelPayment(transactionCode)
            }
    }

    fun cancelPayment(transactionCode: Int){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    for (data in dataSnapshot.children){
                        database.child(ETable.PAYMENT.toString())
                            .child(getMerchantCredential(context))
                            .child(getMerchant(context))
                            .child(transactionCode.toString())
                            .child(data.key.toString())
                            .child(EPayment.STATUS_CODE.toString())
                            .setValue(EStatusCode.CANCEL.toString()).addOnFailureListener {
                                view.response(it.message.toString())
                            }
                            .addOnSuccessListener {
                                view.response(EMessageResult.SUCCESS.toString())
                            }
                    }
                }else
                    view.response("No Payment to Cancel !")
            }

        }
        database.child(ETable.PAYMENT.toString())
            .child(getMerchantCredential(context))
            .child(getMerchant(context))
            .child(transactionCode.toString())
            .addListenerForSingleValueEvent(postListener)
    }

    private fun generateTransCode() : String{
        return database.push().key.toString()
    }

    fun dismissListener(){
    }
}

