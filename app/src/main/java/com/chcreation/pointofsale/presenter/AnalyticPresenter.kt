package com.chcreation.pointofsale.presenter

import android.content.Context
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.model.UserAcceptance
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class AnalyticPresenter(private val view: MainView, private val auth: FirebaseAuth, private val database: DatabaseReference, private val context: Context){

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    suspend fun retrieveProducts(){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    view.loadData(p0, EMessageResult.FETCH_PROD_SUCCESS.toString())
                }

            }
            database.child(ETable.PRODUCT.toString())
                .child(getMerchantCredential(context))
                .child(getMerchant(context))
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    suspend fun retrieveTransactions(){
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
                .child(getMerchant(context))
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun dismissListener(){
    }
}

