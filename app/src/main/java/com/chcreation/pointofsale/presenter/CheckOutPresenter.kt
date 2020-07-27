package com.chcreation.pointofsale.presenter

import android.content.Context
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
import kotlin.math.E


class CheckOutPresenter(private val view: MainView, private val auth: FirebaseAuth, private val database: DatabaseReference,private val context: Context){

    var transactionKey = 1
    var paymentKey = 1

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun saveTransaction(transaction: Transaction,payment: Payment){
        try{
            getTransPrimaryKey(transaction,payment)
        }catch (e: Exception){
            view.response(e.message.toString())
        }
    }

    private fun getTransPrimaryKey(transaction: Transaction,payment: Payment){
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
                transCode = transactionKey
                transDate = transaction.CREATED_DATE.toString()
                val values  = hashMapOf(
                    ETransaction.DETAIL.toString() to transaction.DETAIL,
                    ETransaction.CUST_CODE.toString() to transaction.CUST_CODE,
                    ETransaction.NOTE.toString() to transaction.NOTE,
                    ETransaction.TRANS_CODE.toString() to transaction.TRANS_CODE,
                    ETransaction.PAYMENT_METHOD.toString() to transaction.PAYMENT_METHOD,
                    ETransaction.TOTAL_PRICE.toString() to transaction.TOTAL_PRICE,
                    ETransaction.TOTAL_OUTSTANDING.toString() to transaction.TOTAL_OUTSTANDING,
                    ETransaction.DISCOUNT.toString() to transaction.DISCOUNT,
                    ETransaction.TAX.toString() to transaction.TAX,
                    ETransaction.STATUS_CODE.toString() to transaction.STATUS_CODE,
                    ETransaction.CREATED_DATE.toString() to transaction.CREATED_DATE,
                    ETransaction.UPDATED_DATE.toString() to transaction.UPDATED_DATE,
                    ETransaction.CREATED_BY.toString() to transaction.CREATED_BY,
                    ETransaction.UPDATED_BY.toString() to transaction.UPDATED_BY
                )
                database.child(ETable.TRANSACTION.toString())
                    .child(getMerchantCredential(context))
                    .child(getMerchant(context))
                    .child(transactionKey.toString())
                    .setValue(values).addOnFailureListener {
                        view.response(it.message.toString())
                    }
                    .addOnSuccessListener {
                        updatePayment(transactionKey,payment)
                    }
            }

        }
        database.child(ETable.TRANSACTION.toString())
            .child(getMerchantCredential(context))
            .child(getMerchant(context))
            .orderByKey()
            .limitToLast(1)
            .addListenerForSingleValueEvent(postListener)
    }

    fun updatePayment(transactionKey: Int,payment: Payment){
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
                    EPayment.PAYMENT_METHOD.toString() to payment.PAYMENT_METHOD,
                    EPayment.STATUS_CODE.toString() to payment.STATUS_CODE
                )
                database.child(ETable.PAYMENT.toString())
                    .child(getMerchantCredential(context))
                    .child(getMerchant(context))
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
            .child(getMerchantCredential(context))
            .child(getMerchant(context))
            .child(transactionKey.toString())
            .orderByKey()
            .limitToLast(1)
            .addListenerForSingleValueEvent(postListener)
    }

    fun retrievePendingPayment(transactionCode: Int){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_PEND_PAYMENT_SUCCESS.toString())
            }

        }
        database.child(ETable.TRANSACTION.toString())
            .child(getMerchantCredential(context))
            .child(getMerchant(context))
            .child(transactionCode.toString())
            .addListenerForSingleValueEvent(postListener)
    }

    fun savePendingPayment(transactionCode: Int,payment: Payment, newTotalOutstanding: Int){

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
                    EPayment.PAYMENT_METHOD.toString() to payment.PAYMENT_METHOD,
                    EPayment.STATUS_CODE.toString() to payment.STATUS_CODE,
                    EPayment.CREATED_BY.toString() to payment.CREATED_BY,
                    EPayment.UPDATED_BY.toString() to payment.UPDATED_BY,
                    EPayment.UPDATED_DATE.toString() to payment.UPDATED_DATE
                )
                database.child(ETable.PAYMENT.toString())
                    .child(getMerchantCredential(context))
                    .child(getMerchant(context))
                    .child(transactionCode.toString())
                    .child(paymentKey.toString())
                    .setValue(values).addOnFailureListener {
                        view.response(it.message.toString())
                    }
                    .addOnSuccessListener {
                        updateTotalOutstanding(transactionCode,newTotalOutstanding)
                    }
            }

        }
        database.child(ETable.PAYMENT.toString())
            .child(getMerchantCredential(context))
            .child(getMerchant(context))
            .child(transactionCode.toString())
            .orderByKey()
            .limitToLast(1)
            .addListenerForSingleValueEvent(postListener)
    }

    fun updateTotalOutstanding(transactionCode: Int, newTotalOutstanding: Int){
        database.child(ETable.TRANSACTION.toString())
            .child(getMerchantCredential(context))
            .child(getMerchant(context))
            .child(transactionCode.toString())
            .child(ETransaction.TOTAL_OUTSTANDING.toString())
            .setValue(newTotalOutstanding).addOnFailureListener {
                view.response(it.message.toString())
            }
            .addOnSuccessListener {
                if (newTotalOutstanding == 0){
                    database.child(ETable.TRANSACTION.toString())
                        .child(getMerchantCredential(context))
                        .child(getMerchant(context))
                        .child(transactionCode.toString())
                        .child(ETransaction.STATUS_CODE.toString())
                        .setValue(EStatusCode.DONE.toString()).addOnFailureListener {
                            view.response(it.message.toString())
                        }
                        .addOnSuccessListener {
                            view.response(EMessageResult.SUCCESS.toString())
                        }
                }else
                    view.response(EMessageResult.SUCCESS.toString())
            }

    }

    private fun generateTransCode() : String{
        return database.push().key.toString()
    }

    fun dismissListener(){
    }
}

