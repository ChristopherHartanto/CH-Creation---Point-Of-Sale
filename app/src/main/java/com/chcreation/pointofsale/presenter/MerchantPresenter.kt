package com.chcreation.pointofsale.presenter

import android.content.Context
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.AvailableMerchant
import com.chcreation.pointofsale.model.Merchant
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class MerchantPresenter(private val view: MainView, private val auth: FirebaseAuth, private val database: DatabaseReference, private var context: Context){

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    suspend fun retrieveUserName(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_USER_SUCCESS.toString())
            }

        }
        database.child(ETable.USER.toString())
            .child(auth.currentUser!!.uid)
            .addListenerForSingleValueEvent(postListener)
    }

    suspend fun retrieveMerchants(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_AVAIL_MERCHANT_SUCCESS.toString())
            }

        }
        database.child(ETable.AVAILABLE_MERCHANT.toString())
            .child(auth.currentUser!!.uid)
            .addListenerForSingleValueEvent(postListener)
    }

    suspend fun retrieveCurrentMerchant(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_MERCHANT_SUCCESS.toString())
            }

        }
        database.child(ETable.MERCHANT.toString())
            .child(getMerchantCredential(context))
            .child(getMerchant(context))
            .addListenerForSingleValueEvent(postListener)
    }

    fun retrieveMerchantInfo(credential: String, merchantName: String){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_MERCHANT_SUCCESS.toString())
            }

        }
        database.child(ETable.MERCHANT.toString())
            .child(credential)
            .child(merchantName)
            .addListenerForSingleValueEvent(postListener)
    }

    fun createNewMerchant(merchant: Merchant, availableMerchant: AvailableMerchant){

        val values  = hashMapOf(
            EMerchant.NAME.toString() to merchant.NAME,
            EMerchant.BUSINESS_INFO.toString() to merchant.BUSINESS_INFO,
            EMerchant.NO_TELP.toString() to merchant.NO_TELP,
            EMerchant.CREATED_BY.toString() to merchant.CREATED_BY,
            EMerchant.CREATED_DATE.toString() to merchant.CREATED_DATE,
            EMerchant.UPDATED_BY.toString() to merchant.UPDATED_BY,
            EMerchant.UPDATED_DATE.toString() to merchant.CREATED_DATE,
            EMerchant.IMAGE.toString() to merchant.IMAGE
        )

        val valueUser  = hashMapOf(
            EMerchantUserList.USER_CODE.toString() to auth.currentUser!!.uid,
            EMerchantUserList.STATUS_CODE.toString() to EStatusCode.ACTIVE.toString(),
            EMerchantUserList.CREATED_DATE.toString() to dateFormat().format(Date()),
            EMerchantUserList.UPDATED_DATE.toString() to dateFormat().format(Date())
        )
        database.child(ETable.MERCHANT.toString())
            .child(auth.currentUser!!.uid)
            .child(merchant.NAME.toString())
            .child(ETable.USER_LIST.toString())
            .setValue(valueUser).addOnFailureListener {
                view.response(it.message.toString())
            }
            .addOnSuccessListener {
                database.child(ETable.MERCHANT.toString())
                    .child(auth.currentUser!!.uid)
                    .child(merchant.NAME.toString())
                    .setValue(values).addOnFailureListener {
                        view.response(it.message.toString())
                    }
                    .addOnSuccessListener {
                        createNewMerchantList(availableMerchant)
                    }
            }

    }

    fun createNewMerchantList(availableMerchant: AvailableMerchant){
        var key = 1

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
                    EAvailableMerchant.CREATED_DATE.toString() to availableMerchant.CREATED_DATE,
                    EAvailableMerchant.UPDATED_DATE.toString() to availableMerchant.UPDATED_DATE,
                    EAvailableMerchant.CREDENTIAL.toString() to availableMerchant.CREDENTIAL,
                    EAvailableMerchant.STATUS.toString() to availableMerchant.STATUS,
                    EAvailableMerchant.USER_GROUP.toString() to availableMerchant.USER_GROUP,
                    EAvailableMerchant.NAME.toString() to availableMerchant.NAME
                )

                database.child(ETable.AVAILABLE_MERCHANT.toString())
                    .child(auth.currentUser!!.uid)
                    .child(key.toString())
                    .setValue(values).addOnFailureListener {
                        view.response(it.message.toString())
                    }
                    .addOnSuccessListener {
                        view.response(EMessageResult.SUCCESS.toString())
                    }
            }

        }
        database.child(ETable.AVAILABLE_MERCHANT.toString())
            .child(auth.currentUser!!.uid)
            .orderByKey()
            .limitToLast(1)
            .addListenerForSingleValueEvent(postListener)
    }
}

