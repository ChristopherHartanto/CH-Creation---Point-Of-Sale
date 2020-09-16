package com.chcreation.pointofsale.presenter

import android.content.Context
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.ActivityLogs
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.lang.Exception
import java.util.*

class CustomerPresenter(private val view: MainView,
                        private val auth: FirebaseAuth,
                        private val database: DatabaseReference,
                        private val context: Context){

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun saveCustomer(customer: Customer, custKey: Int){
        val values  = hashMapOf(
            ECustomer.NAME.toString() to customer.NAME,
            ECustomer.EMAIL.toString() to customer.EMAIL,
            ECustomer.PHONE.toString() to customer.PHONE,
            ECustomer.ADDRESS.toString() to customer.ADDRESS,
            ECustomer.CREATED_DATE.toString() to customer.CREATED_DATE,
            ECustomer.UPDATED_DATE.toString() to customer.UPDATED_DATE,
            ECustomer.CODE.toString() to customer.CODE,
            ECustomer.IMAGE.toString() to customer.IMAGE
        )
        database.child(ETable.CUSTOMER.toString())
            .child(getMerchantCredential(context))
            .child(getMerchantCode(context))
            .child(custKey.toString())
            .setValue(values).addOnFailureListener {
                view.response(it.message.toString())
            }
            .addOnSuccessListener {
                view.response(EMessageResult.SUCCESS.toString())
            }
    }

    fun saveCustomer(customer: Customer, callback: (custKey: String,success:Boolean)->Unit){

        getCustomerPrimaryKey(customer,callback)
    }

    fun deleteCustomer(custKey: Int){

        database.child(ETable.CUSTOMER.toString())
            .child(getMerchantCredential(context))
            .child(getMerchantCode(context))
            .child(custKey.toString())
            .child(ECustomer.STATUS_CODE.toString())
            .setValue(EStatusCode.DELETE.toString()).addOnFailureListener {
                view.response(it.message.toString())
            }
            .addOnSuccessListener {
                view.response(EMessageResult.DELETE_SUCCESS.toString())
            }
    }

    private fun getCustomerPrimaryKey(customer: Customer,callback:(custKey: String, success: Boolean)->Unit){
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

                customer.CODE = "C"+generateCustomerCode()
                val timeStamp: String = dateFormat().format(Date())
                val values  = hashMapOf(
                    ECustomer.NAME.toString() to customer.NAME,
                    ECustomer.EMAIL.toString() to customer.EMAIL,
                    ECustomer.PHONE.toString() to customer.PHONE,
                    ECustomer.ADDRESS.toString() to customer.ADDRESS,
                    ECustomer.CREATED_DATE.toString() to timeStamp,
                    ECustomer.UPDATED_DATE.toString() to timeStamp,
                    ECustomer.CODE.toString() to customer.CODE
                )
                database.child(ETable.CUSTOMER.toString())
                    .child(getMerchantCredential(context))
                    .child(getMerchantCode(context))
                    .child(key.toString())
                    .setValue(values).addOnFailureListener {
                        callback("",false)
                    }
                    .addOnSuccessListener {
                        callback(customer.CODE.toString(),true)
                    }
            }
        }
        database.child(ETable.CUSTOMER.toString())
            .child(getMerchantCredential(context))
            .child(getMerchantCode(context))
            .orderByKey()
            .limitToLast(1)
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
            .child(getMerchantCode(context))
            .orderByChild(ECustomer.NAME.toString())
            .addListenerForSingleValueEvent(postListener)
    }

    fun retrieveCustomerByCustCode(custCode: String){
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
            .child(getMerchantCode(context))
            .orderByChild(ECustomer.CODE.toString())
            .equalTo(custCode)
            .addListenerForSingleValueEvent(postListener)
    }

    fun retrieveCustomerTransaction(custCode: String){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_CUSTOMER_TRANSACTION_SUCCESS.toString())
            }

        }
        database.child(ETable.ENQUIRY.toString())
            .child(getMerchantCredential(context))
            .child(getMerchantCode(context))
            .orderByChild(E_Enquiry.CUST_CODE.toString())
            .equalTo(custCode)
            .addListenerForSingleValueEvent(postListener)
    }

    fun retrieveTransaction(transKey: String){
        try{
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
                .child(getMerchantCode(context))
                .child(transKey)
                .addListenerForSingleValueEvent(postListener)
        }catch (e:Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun saveActivityLogs(logs: ActivityLogs){
        try{
            var key = 0
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        for (data in p0.children){
                            key = data.key.toString().toInt() + 1
                            break
                        }
                    }
                    val values  = hashMapOf(
                        EActivityLogs.LOG.toString() to logs.LOG,
                        EActivityLogs.CREATED_BY.toString() to logs.CREATED_BY,
                        EActivityLogs.CREATED_DATE.toString() to logs.CREATED_DATE
                    )

                    database.child(ETable.ACTIVITY_LOGS.toString())
                        .child(getMerchantCredential(context))
                        .child(getMerchantCode(context))
                        .child(key.toString())
                        .setValue(values)

                }

            }
            database.child(ETable.ACTIVITY_LOGS.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .orderByKey()
                .limitToLast(1)
                .addListenerForSingleValueEvent(postListener)
        }catch (e:java.lang.Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    private fun generateCustomerCode() : String{
        return database.push().key.toString()
    }

    fun dismissListener(){
    }
}

