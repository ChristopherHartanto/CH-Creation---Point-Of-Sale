package com.chcreation.pointofsale.presenter

import android.content.Context
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.AvailableMerchant
import com.chcreation.pointofsale.model.Merchant
import com.chcreation.pointofsale.model.UserAcceptance
import com.chcreation.pointofsale.model.UserList
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

    suspend fun retrieveInvitation(email: String){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_INVITATION_SUCCESS.toString())
            }

        }
        database.child(ETable.USER_ACCEPTANCE.toString())
            .child(email)
            .addListenerForSingleValueEvent(postListener)
    }

    suspend fun removeInvitation(email: String){
        database.child(ETable.USER_ACCEPTANCE.toString())
            .child(email)
            .removeValue()
    }

    fun acceptInvitation(email: String,userAcceptance: UserAcceptance){
        try {
            GlobalScope.launch {
                removeInvitation(email)
                createUserList(userAcceptance)
                createNewMerchantList(AvailableMerchant(
                    userAcceptance.NAME,userAcceptance.USER_GROUP,userAcceptance.CREATED_DATE,userAcceptance.CREATED_DATE,
                    userAcceptance.CREDENTIAL,EStatusCode.ACTIVE.toString()))
            }
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    suspend fun createUserList(userAcceptance: UserAcceptance){
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

                    userListItems.add(UserList(auth.currentUser!!.uid,userAcceptance.USER_GROUP,EStatusCode.ACTIVE.toString(),
                        userAcceptance.CREATED_DATE,userAcceptance.CREATED_DATE))
                    val newUserList = gson.toJson(userListItems)

                    database.child(ETable.MERCHANT.toString())
                        .child(userAcceptance.CREDENTIAL.toString())
                        .child(userAcceptance.NAME.toString())
                        .child(EMerchant.USER_LIST.toString())
                        .setValue(newUserList).addOnFailureListener {
                            view.response(it.message.toString())
                        }
                }
            }

        }
        database.child(ETable.MERCHANT.toString())
            .child(userAcceptance.CREDENTIAL.toString())
            .child(userAcceptance.NAME.toString())
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

        val userItem = arrayListOf(UserList(auth.currentUser!!.uid,EUserGroup.MANAGER.toString(),EStatusCode.ACTIVE.toString(),dateFormat().format(Date()),dateFormat().format(Date())))

        val gson = Gson()
        val userList = gson.toJson(userItem)

        val values  = hashMapOf(
            EMerchant.NAME.toString() to merchant.NAME,
            EMerchant.ADDRESS.toString() to merchant.ADDRESS,
            EMerchant.BUSINESS_INFO.toString() to merchant.BUSINESS_INFO,
            EMerchant.NO_TELP.toString() to merchant.NO_TELP,
            EMerchant.CREATED_BY.toString() to merchant.CREATED_BY,
            EMerchant.CREATED_DATE.toString() to merchant.CREATED_DATE,
            EMerchant.UPDATED_BY.toString() to merchant.UPDATED_BY,
            EMerchant.UPDATED_DATE.toString() to merchant.CREATED_DATE,
            EMerchant.IMAGE.toString() to merchant.IMAGE,
            EMerchant.USER_LIST.toString() to userList
        )

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

    fun updateMerchant(merchant: Merchant, oldName: String){
        try {

            GlobalScope.launch {
                if (merchant.NAME != oldName)
                    updateAvailableMerchantName(merchant.NAME.toString(),oldName)

                val values  = hashMapOf(
                    EMerchant.NAME.toString() to merchant.NAME,
                    EMerchant.ADDRESS.toString() to merchant.ADDRESS,
                    EMerchant.BUSINESS_INFO.toString() to merchant.BUSINESS_INFO,
                    EMerchant.NO_TELP.toString() to merchant.NO_TELP,
                    EMerchant.CREATED_BY.toString() to merchant.CREATED_BY,
                    EMerchant.CREATED_DATE.toString() to merchant.CREATED_DATE,
                    EMerchant.UPDATED_BY.toString() to merchant.UPDATED_BY,
                    EMerchant.UPDATED_DATE.toString() to merchant.CREATED_DATE,
                    EMerchant.IMAGE.toString() to merchant.IMAGE,
                    EMerchant.USER_LIST.toString() to merchant.USER_LIST,
                    EMerchant.CAT.toString() to merchant.CAT
                )

                database.child(ETable.MERCHANT.toString())
                    .child(getMerchantCredential(context))
                    .child(merchant.NAME.toString())
                    .setValue(values).addOnFailureListener {
                        view.response(it.message.toString())
                    }
                    .addOnSuccessListener {
                        view.response(EMessageResult.UPDATE.toString())
                    }

            }

        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }

    }

    suspend fun updateAvailableMerchantName(merchantName: String, oldName: String){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    database.child(ETable.AVAILABLE_MERCHANT.toString())
                        .child(getMerchantCredential(context))
                        .child(p0.key.toString())
                        .child(EAvailableMerchant.NAME.toString())
                        .setValue(merchantName).addOnFailureListener {
                            view.response(it.message.toString())
                        }
                        .addOnSuccessListener {
                            view.response(EMessageResult.SUCCESS.toString())
                        }
                }
            }

        }
        database.child(ETable.AVAILABLE_MERCHANT.toString())
            .child(getMerchantCredential(context))
            .orderByChild(EAvailableMerchant.NAME.toString())
            .equalTo(oldName)
            .addListenerForSingleValueEvent(postListener)
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

