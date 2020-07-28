package com.chcreation.pointofsale.presenter

import android.content.Context
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UserPresenter(private val view: MainView, private val auth: FirebaseAuth, private val database: DatabaseReference, private val context: Context){

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun saveCustomer(customer: Customer){

        getCustomerPrimaryKey(customer)
    }

    private fun getCustomerPrimaryKey(customer: Customer){
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
                val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
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
                    .child(getMerchant(context))
                    .child(key.toString())
                    .setValue(values).addOnFailureListener {
                        view.response(it.message.toString())
                    }
                    .addOnSuccessListener {
                        view.response(EMessageResult.SUCCESS.toString())
                    }
            }

        }
        database.child(ETable.CUSTOMER.toString())
            .child(getMerchantCredential(context))
            .child(getMerchant(context))
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
            .child(getMerchant(context))
            .addListenerForSingleValueEvent(postListener)
    }

    private fun generateCustomerCode() : String{
        return database.push().key.toString()
    }

    fun dismissListener(){
    }
}
