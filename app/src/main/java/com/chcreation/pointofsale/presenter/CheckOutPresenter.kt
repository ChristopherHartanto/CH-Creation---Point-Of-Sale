package com.chcreation.pointofsale.presenter

import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.transCode
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.transDate
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.model.Payment
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

    var transactionKey = 1
    var paymentKey = 1

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun saveTransaction(transaction: Transaction,payment: Payment, merchant: String){
        try{
            getTransPrimaryKey(transaction,payment,merchant)
        }catch (e: Exception){
            view.response(e.message.toString())
        }
    }

    private fun getTransPrimaryKey(transaction: Transaction,payment: Payment, merchant: String){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    for (data in p0.children){
                        transactionKey = data.key.toString().toInt() + 1
                        break
                    }
                }

                transaction.TRANS_CODE = "T"+generateTransCode()
                val createdDate: String = dateFormat().format(Date())
                transCode = transactionKey
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
                    ETransaction.DISCOUNT.toString() to transaction.DISCOUNT
                )
                database.child(ETable.TRANSACTION.toString())
                    .child(auth.currentUser!!.uid)
                    .child(merchant)
                    .child(transactionKey.toString())
                    .setValue(values).addOnFailureListener {
                        view.response(it.message.toString())
                    }
                    .addOnSuccessListener {
                        updatePayment(transactionKey,payment,merchant)
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

    fun updatePayment(transactionKey: Int,payment: Payment, merchant: String){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    for (data in p0.children){
                        paymentKey = data.key.toString().toInt() + 1
                        break
                    }
                }

                val createdDate: String = dateFormat().format(Date())
                transCode = transactionKey
                transDate = createdDate
                val values  = hashMapOf(
                    EPayment.TOTAL_RECEIVED.toString() to payment.TOTAL_RECEIVED,
                    EPayment.USER_CODE.toString() to payment.USER_CODE,
                    EPayment.NOTE.toString() to payment.NOTE,
                    EPayment.CREATED_DATE.toString() to createdDate,
                    EPayment.PAYMENT_METHOD.toString() to payment.PAYMENT_METHOD
                )
                database.child(ETable.PAYMENT.toString())
                    .child(auth.currentUser!!.uid)
                    .child(merchant)
                    .child(transactionKey.toString())
                    .child(paymentKey.toString())
                    .setValue(values).addOnFailureListener {
                        view.response(it.message.toString())
                    }
                    .addOnSuccessListener {
                        view.response(EMessageResult.SUCCESS.toString())
                    }
            }

        }
        database.child(ETable.PAYMENT.toString())
            .child(auth.currentUser!!.uid)
            .child(merchant)
            .child(transactionKey.toString())
            .orderByKey()
            .limitToLast(1)
            .addListenerForSingleValueEvent(postListener)
    }

    fun retrievePendingPayment(transactionCode: Int,merchant: String){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_PEND_PAYMENT_SUCCESS.toString())
            }

        }
        database.child(ETable.TRANSACTION.toString())
            .child(auth.currentUser!!.uid)
            .child(merchant)
            .child(transactionCode.toString())
            .addListenerForSingleValueEvent(postListener)
    }

    fun savePendingPayment(transactionCode: Int, merchant: String,payment: Payment, newTotalOutstanding: Int){

        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    for (data in p0.children){
                        paymentKey = data.key.toString().toInt() + 1
                        break
                    }
                }

                val createdDate: String = dateFormat().format(Date())

                val values  = hashMapOf(
                    EPayment.TOTAL_RECEIVED.toString() to payment.TOTAL_RECEIVED,
                    EPayment.USER_CODE.toString() to payment.USER_CODE,
                    EPayment.NOTE.toString() to payment.NOTE,
                    EPayment.CREATED_DATE.toString() to createdDate,
                    EPayment.PAYMENT_METHOD.toString() to payment.PAYMENT_METHOD
                )
                database.child(ETable.PAYMENT.toString())
                    .child(auth.currentUser!!.uid)
                    .child(merchant)
                    .child(transactionCode.toString())
                    .child(paymentKey.toString())
                    .setValue(values).addOnFailureListener {
                        view.response(it.message.toString())
                    }
                    .addOnSuccessListener {
                        updateTotalOutstanding(transactionCode,merchant,newTotalOutstanding)
                    }
            }

        }
        database.child(ETable.PAYMENT.toString())
            .child(auth.currentUser!!.uid)
            .child(merchant)
            .child(transactionCode.toString())
            .orderByKey()
            .limitToLast(1)
            .addListenerForSingleValueEvent(postListener)
    }

    fun updateTotalOutstanding(transactionCode: Int,merchant: String, newTotalOutstanding: Int){
        database.child(ETable.TRANSACTION.toString())
            .child(auth.currentUser!!.uid)
            .child(merchant)
            .child(transactionCode.toString())
            .child(ETransaction.TOTAL_OUTSTANDING.toString())
            .setValue(newTotalOutstanding).addOnFailureListener {
                view.response(it.message.toString())
            }
            .addOnSuccessListener {
                view.response(EMessageResult.SUCCESS.toString())
            }

    }

    private fun generateTransCode() : String{
        return database.push().key.toString()
    }

    fun dismissListener(){
    }
}

