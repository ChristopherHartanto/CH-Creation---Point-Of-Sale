package com.chcreation.pointofsale.presenter

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


class CheckOutPresenter(private val view: MainView, private val auth: FirebaseAuth, private val database: DatabaseReference){

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun saveTransaction(transaction: Transaction, merchant: String){

        getTransPrimaryKey(transaction,merchant)
    }

    private fun getTransPrimaryKey(transaction: Transaction, merchant: String){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                var key = 0
                if (p0.exists()){
                    for (data in p0.children){
                        key = data.key.toString().toInt() + 1
                        break
                    }
                }

                transaction.TRANS_CODE = "T"+generateTransCode()
                val createdDate: String = dateFormat().format(Date())
                transCode = key
                transDate = createdDate
                val values  = hashMapOf(
                    ETransaction.DETAIL.toString() to transaction.DETAIL,
                    ETransaction.USER_CODE.toString() to transaction.USER_CODE,
                    ETransaction.CUST_CODE.toString() to transaction.CUST_CODE,
                    ETransaction.NOTE.toString() to transaction.NOTE,
                    ETransaction.CREATED_DATE.toString() to createdDate,
                    ETransaction.TRANS_CODE.toString() to transaction.TRANS_CODE,
                    ETransaction.PAYMENT_METHOD.toString() to transaction.PAYMENT_METHOD,
                    ETransaction.TOTAL_PRICE.toString() to transaction.TOTAL_PRICE,
                    ETransaction.TOTAL_OUTSTANDING.toString() to transaction.TOTAL_OUTSTANDING,
                    ETransaction.TOTAL_RECEIVED.toString() to transaction.TOTAL_RECEIVED,
                    ETransaction.DISCOUNT.toString() to transaction.DISCOUNT
                )
                database.child(ETable.TRANSACTION.toString())
                    .child(auth.currentUser!!.uid)
                    .child(merchant)
                    .child(key.toString())
                    .setValue(values).addOnFailureListener {
                        view.response(it.message.toString())
                    }
                    .addOnSuccessListener {
                        view.response(EMessageResult.SUCCESS.toString())
                    }
            }

        }
        database.child(ETable.TRANSACTION.toString())
            .child(auth.currentUser!!.uid)
            .child(merchant)
            .orderByKey()
            .limitToLast(1)
            .addListenerForSingleValueEvent(postListener)
    }


    fun retrieveCustomers(merchant: String){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_CUSTOMER_SUCCESS.toString())
            }

        }
        database.child(ETable.CUSTOMER.toString())
            .child(auth.currentUser!!.uid)
            .child(merchant)
            .addListenerForSingleValueEvent(postListener)
    }

    private fun generateTransCode() : String{
        return database.push().key.toString()
    }

    fun dismissListener(){
    }
}

