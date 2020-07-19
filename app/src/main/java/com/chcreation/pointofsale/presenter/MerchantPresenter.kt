package com.chcreation.pointofsale.presenter

import com.chcreation.pointofsale.EMerchant
import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.ETable
import com.chcreation.pointofsale.model.Merchant
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class MerchantPresenter(private val view: MainView, private val auth: FirebaseAuth, private val database: DatabaseReference){

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun retrieveMerchants(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_MERCHANT_SUCCESS.toString())
            }

        }
        database.child(ETable.MERCHANT.toString())
            .child(auth.currentUser!!.uid)
            .addListenerForSingleValueEvent(postListener)
    }

    fun createNewMerchant(merchant: Merchant){

        val values  = hashMapOf(
            EMerchant.CREATED_DATE.toString() to merchant.CREATED_DATE,
            EMerchant.NAME.toString() to merchant.NAME
        )

        database.child(ETable.MERCHANT.toString())
            .child(auth.currentUser!!.uid)
            .child(merchant.NAME.toString())
            .setValue(values).addOnFailureListener {
                view.response(it.message.toString())
            }
            .addOnSuccessListener {
                view.response(EMessageResult.SUCCESS.toString())
            }
    }

    fun dismissListener(){
    }
}

