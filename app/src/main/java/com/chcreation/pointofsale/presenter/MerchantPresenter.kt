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
import java.util.*

class MerchantPresenter(private val view: MainView, private val auth: FirebaseAuth, private val database: DatabaseReference, private var context: Context){

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    suspend fun retrieveUserName(){
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
                .child(auth.currentUser!!.uid)
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    suspend fun retrieveInvitation(email: String){
        try{
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
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
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
                    userAcceptance.CREDENTIAL,EStatusCode.ACTIVE.toString(),userAcceptance.MERCHANT_CODE))
            }
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    suspend fun createUserList(userAcceptance: UserAcceptance){
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
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }


    suspend fun retrieveMerchants(){
        try{
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
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    suspend fun retrieveCurrentMerchant(){
        try{
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
                .child(getMerchantCode(context))
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun retrieveMerchantInfo(credential: String, merchantCode: String){
        try {
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
                .child(merchantCode)
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun getMerchantName(credential: String, merchantCode: String,key:Int, callback:(success:Boolean, merchantName:String, key: Int) -> Unit){
        try {
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        val item = p0.getValue(Merchant::class.java)
                        if (item != null) {
                            item.NAME?.let { callback(true, it,key) }
                        }else
                            callback(false,"",-99)
                    }else
                        callback(false,"",-99)
                }

            }
            database.child(ETable.MERCHANT.toString())
                .child(credential)
                .child(merchantCode)
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun createNewMerchant(merchant: Merchant, availableMerchant: AvailableMerchant){
        try {
            val userItem = arrayListOf(
                UserList(
                    auth.currentUser!!.uid,
                    EUserGroup.MANAGER.toString(),
                    EStatusCode.ACTIVE.toString(),
                    dateFormat().format(Date()),
                    dateFormat().format(Date())
                )
            )

            val gson = Gson()
            val userList = gson.toJson(userItem)

            val values = hashMapOf(
                EMerchant.NAME.toString() to merchant.NAME,
                EMerchant.ADDRESS.toString() to merchant.ADDRESS,
                EMerchant.BUSINESS_INFO.toString() to merchant.BUSINESS_INFO,
                EMerchant.NO_TELP.toString() to merchant.NO_TELP,
                EMerchant.CREATED_BY.toString() to merchant.CREATED_BY,
                EMerchant.CREATED_DATE.toString() to merchant.CREATED_DATE,
                EMerchant.UPDATED_BY.toString() to merchant.UPDATED_BY,
                EMerchant.UPDATED_DATE.toString() to merchant.CREATED_DATE,
                EMerchant.IMAGE.toString() to merchant.IMAGE,
                EMerchant.MERCHANT_CODE.toString() to merchant.MERCHANT_CODE,
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
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }

    }

    fun updateMerchant(merchant: Merchant, oldName: String){
        try {

            GlobalScope.launch {
//                if (merchant.NAME != oldName)
//                    updateAvailableMerchantName(merchant.NAME.toString(),oldName)

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
                    EMerchant.CAT.toString() to merchant.CAT,
                    EMerchant.MERCHANT_CODE.toString() to merchant.MERCHANT_CODE
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
        try {
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
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
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun createNewMerchantList(availableMerchant: AvailableMerchant){
        try {
            var key = 1

            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        for (data in p0.children) {
                            key = data.key.toString().toInt() + 1
                            break
                        }
                    }
                    val values = hashMapOf(
                        EAvailableMerchant.CREATED_DATE.toString() to availableMerchant.CREATED_DATE,
                        EAvailableMerchant.UPDATED_DATE.toString() to availableMerchant.UPDATED_DATE,
                        EAvailableMerchant.CREDENTIAL.toString() to availableMerchant.CREDENTIAL,
                        EAvailableMerchant.STATUS.toString() to availableMerchant.STATUS,
                        EAvailableMerchant.USER_GROUP.toString() to availableMerchant.USER_GROUP,
                        EAvailableMerchant.NAME.toString() to availableMerchant.NAME,
                        EAvailableMerchant.MERCHANT_CODE.toString() to availableMerchant.MERCHANT_CODE
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
        }catch (e: Exception){
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
}

