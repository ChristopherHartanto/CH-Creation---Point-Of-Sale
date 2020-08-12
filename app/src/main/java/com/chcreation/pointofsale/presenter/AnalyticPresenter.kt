package com.chcreation.pointofsale.presenter

import android.content.Context
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.*
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

    suspend fun retrieveUser(userCode: String){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    view.loadData(p0, EMessageResult.FETCH_USER_SUCCESS.toString())
                }

            }
            database.child(ETable.USER.toString())
                .orderByChild(EUser.CODE.toString())
                .equalTo(userCode)
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    suspend fun removeUserList(userCode: String){
        GlobalScope.launch {
            removeMerchantUserList(userCode)
            removeMerchantUserList(userCode)
        }
    }

    suspend fun removeMerchantUserList(userCode: String){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        val item = p0.getValue(Merchant::class.java)

                        val gson = Gson()
                        val arrayUserListType = object : TypeToken<MutableList<UserList>>() {}.type
                        val userListItems : MutableList<UserList> = gson.fromJson(item!!.USER_LIST.toString(),arrayUserListType)

                        for((index,data) in userListItems.withIndex()){
                            if (data.USER_CODE == userCode){
                                userListItems[index].STATUS_CODE = EStatusCode.DELETE.toString()
                                userListItems[index].UPDATED_DATE = dateFormat().format(Date())
                            }
                        }
                        val newUserList = gson.toJson(userListItems)

                        database.child(ETable.MERCHANT.toString())
                            .child(getMerchantCredential(context))
                            .child(getMerchant(context))
                            .child(EMerchant.USER_LIST.toString())
                            .setValue(newUserList).addOnFailureListener {
                                view.response(it.message.toString())
                            }
                    }
                }

            }
            database.child(ETable.MERCHANT.toString())
                .child(getMerchantCredential(context))
                .child(getMerchant(context))
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    suspend fun removeAvailableMerchant(userCode: String){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        val item = p0.getValue(AvailableMerchant::class.java)

                        val values = hashMapOf(
                            EAvailableMerchant.CREATED_DATE.toString() to item!!.CREATED_DATE,
                            EAvailableMerchant.UPDATED_DATE.toString() to dateFormat().format(Date()),
                            EAvailableMerchant.CREDENTIAL.toString() to item.CREDENTIAL,
                            EAvailableMerchant.STATUS.toString() to EStatusCode.DELETE.toString(),
                            EAvailableMerchant.USER_GROUP.toString() to item.USER_GROUP,
                            EAvailableMerchant.NAME.toString() to item.NAME
                        )

                        database.child(ETable.AVAILABLE_MERCHANT.toString())
                            .child(auth.currentUser!!.uid)
                            .child(p0.key.toString())
                            .setValue(values).addOnFailureListener {
                                view.response(it.message.toString())
                            }
                            .addOnSuccessListener {
                                view.response(EMessageResult.SUCCESS.toString())
                            }
                    }
                }

            }
            database.child(ETable.AVAILABLE_MERCHANT.toString())
                .child(userCode)
                .orderByChild(EAvailableMerchant.NAME.toString())
                .equalTo(getMerchant(context))
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun dismissListener(){
    }
}

