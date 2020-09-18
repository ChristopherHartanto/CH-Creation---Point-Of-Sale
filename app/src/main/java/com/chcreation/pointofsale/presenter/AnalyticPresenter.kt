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
import kotlin.collections.HashMap

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
                .child(getMerchantCode(context))
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
                .child(getMerchantCode(context))
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
                .child(userCode)
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    suspend fun updateUser(user: User, callBack :(success: Boolean) -> Unit){
        try {
            val values  = hashMapOf(
                EUser.NAME.toString() to user.NAME,
                EUser.EMAIL.toString() to user.EMAIL,
                EUser.CREATED_DATE.toString() to user.CREATED_DATE,
                EUser.UPDATED_DATE.toString() to user.UPDATED_DATE,
                EUser.ACTIVE.toString() to user.ACTIVE,
                EUser.MEMBER_STATUS.toString() to user.MEMBER_STATUS
            )
            database.child(ETable.USER.toString())
                .child(auth.currentUser!!.uid)
                .setValue(values).addOnFailureListener {
                    view.response(it.message.toString())
                }.addOnSuccessListener {
                    callBack(true)
                }

        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    suspend fun removeUserList(userCode: String){
        GlobalScope.launch {
            removeMerchantUserList(userCode)
            removeAvailableMerchant(userCode)
        }
    }

    suspend fun updateUserList(userCode: String,userGroup: String){
        GlobalScope.launch {
            updateUserMerchant(userCode,userGroup)
            updateUserAvailableMerchant(userCode,userGroup)
        }
    }
    private suspend fun removeMerchantUserList(userCode: String){
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
                        val userListItems : MutableList<UserList> = gson.fromJson(item?.USER_LIST.toString(),arrayUserListType)

                        for((index,data) in userListItems.withIndex()){
                            if (data.USER_CODE == userCode){
                                userListItems[index].STATUS_CODE = EStatusCode.DELETE.toString()
                                userListItems[index].UPDATED_DATE = dateFormat().format(Date())
                            }
                        }
                        val newUserList = gson.toJson(userListItems)

                        database.child(ETable.MERCHANT.toString())
                            .child(getMerchantCredential(context))
                            .child(getMerchantCode(context))
                            .child(EMerchant.USER_LIST.toString())
                            .setValue(newUserList).addOnFailureListener {
                                view.response(it.message.toString())
                            }
                    }
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

    suspend fun removeAvailableMerchant(userCode: String){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        try{
                            var key = 0
                            var item = AvailableMerchant()
                            for(data in p0.children){
                                key = data.key.toString().toInt()
                                item = data.getValue(AvailableMerchant::class.java)!!
                                break
                            }
                            if (item.CREDENTIAL != ""){
                                val values = hashMapOf(
                                    EAvailableMerchant.CREATED_DATE.toString() to item.CREATED_DATE,
                                    EAvailableMerchant.UPDATED_DATE.toString() to dateFormat().format(Date()),
                                    EAvailableMerchant.CREDENTIAL.toString() to item.CREDENTIAL,
                                    EAvailableMerchant.STATUS.toString() to EStatusCode.DELETE.toString(),
                                    EAvailableMerchant.USER_GROUP.toString() to item.USER_GROUP,
                                    EAvailableMerchant.NAME.toString() to item.NAME,
                                    EAvailableMerchant.MERCHANT_CODE.toString() to item.MERCHANT_CODE
                                )

                                database.child(ETable.AVAILABLE_MERCHANT.toString())
                                    .child(userCode)
                                    .child(key.toString())
                                    .setValue(values).addOnFailureListener {
                                        view.response(it.message.toString())
                                    }
                                    .addOnSuccessListener {
                                        view.response(EMessageResult.SUCCESS.toString())
                                    }
                            }else
                                view.response("Please Contact Your Administrator!! -- Call Remove Available Merchant")

                        }catch (e: Exception){
                            showError(context,e.message.toString())
                            e.printStackTrace()
                        }

                    }else
                        removeAvailableMerchantByName(userCode)
                }

            }
            database.child(ETable.AVAILABLE_MERCHANT.toString())
                .child(userCode)
                .orderByChild(EAvailableMerchant.MERCHANT_CODE.toString())
                .equalTo(getMerchantCode(context))
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun removeAvailableMerchantByName(userCode: String){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        try{
                            var key = 0
                            var item = AvailableMerchant()
                            for(data in p0.children){
                                key = data.key.toString().toInt()
                                item = data.getValue(AvailableMerchant::class.java)!!
                                break
                            }

                            val values = hashMapOf(
                                EAvailableMerchant.CREATED_DATE.toString() to item.CREATED_DATE,
                                EAvailableMerchant.UPDATED_DATE.toString() to dateFormat().format(Date()),
                                EAvailableMerchant.CREDENTIAL.toString() to item.CREDENTIAL,
                                EAvailableMerchant.STATUS.toString() to EStatusCode.DELETE.toString(),
                                EAvailableMerchant.USER_GROUP.toString() to item.USER_GROUP,
                                EAvailableMerchant.NAME.toString() to item.NAME,
                                EAvailableMerchant.MERCHANT_CODE.toString() to getMerchantCode(context)
                            )

                            database.child(ETable.AVAILABLE_MERCHANT.toString())
                                .child(userCode)
                                .child(key.toString())
                                .setValue(values).addOnFailureListener {
                                    view.response(it.message.toString())
                                }
                                .addOnSuccessListener {
                                    view.response(EMessageResult.SUCCESS.toString())
                                }
                        }catch (e: Exception){
                            showError(context,e.message.toString())
                            e.printStackTrace()
                        }

                    }else
                        view.response("Please Contact Your Administrator -- Call Delete Available Merchant")

                }

            }
            database.child(ETable.AVAILABLE_MERCHANT.toString())
                .child(userCode)
                .orderByChild(EAvailableMerchant.NAME.toString())
                .equalTo(getMerchantCode(context))
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    private suspend fun updateUserMerchant(userCode: String, userGroup: String){
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
                                userListItems[index].USER_GROUP = userGroup
                                userListItems[index].UPDATED_DATE = dateFormat().format(Date())
                            }
                        }
                        val newUserList = gson.toJson(userListItems)

                        database.child(ETable.MERCHANT.toString())
                            .child(getMerchantCredential(context))
                            .child(getMerchantCode(context))
                            .child(EMerchant.USER_LIST.toString())
                            .setValue(newUserList).addOnFailureListener {
                                view.response(it.message.toString())
                            }
                    }else
                        view.response("Failed to Update Merchant -- Call Update User Merchant")
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

    suspend fun updateUserAvailableMerchant(userCode: String, userGroup: String){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        var key = 0
                        var item = AvailableMerchant()
                        for(data in p0.children){
                            key = data.key.toString().toInt()
                            item = data.getValue(AvailableMerchant::class.java)!!
                            break
                        }
                        if (item.CREDENTIAL != ""){
                            val values = hashMapOf(
                                EAvailableMerchant.CREATED_DATE.toString() to item.CREATED_DATE,
                                EAvailableMerchant.UPDATED_DATE.toString() to dateFormat().format(Date()),
                                EAvailableMerchant.CREDENTIAL.toString() to item.CREDENTIAL,
                                EAvailableMerchant.STATUS.toString() to EStatusCode.ACTIVE.toString(),
                                EAvailableMerchant.USER_GROUP.toString() to userGroup,
                                EAvailableMerchant.NAME.toString() to item.NAME,
                                EAvailableMerchant.MERCHANT_CODE.toString() to item.MERCHANT_CODE
                            )

                            database.child(ETable.AVAILABLE_MERCHANT.toString())
                                .child(userCode)
                                .child(key.toString())
                                .setValue(values).addOnFailureListener {
                                    view.response(it.message.toString())
                                }
                                .addOnSuccessListener {
                                    view.response(EMessageResult.UPDATE.toString())
                                }
                        }else
                            view.response("Please Contact Your Administrator!! -- Call Update User Available Merchant")
                    }else
                        updateUserAvailableMerchantByName(userCode,userGroup)
                }

            }
            database.child(ETable.AVAILABLE_MERCHANT.toString())
                .child(userCode)
                .orderByChild(EAvailableMerchant.MERCHANT_CODE.toString())
                .equalTo(getMerchantCode(context))
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun updateUserAvailableMerchantByName(userCode: String, userGroup: String){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        var key = 0
                        var item = AvailableMerchant()
                        for(data in p0.children){
                            key = data.key.toString().toInt()
                            item = data.getValue(AvailableMerchant::class.java)!!
                            break
                        }
                        if (item.CREATED_DATE != ""){

                            val values = hashMapOf(
                                EAvailableMerchant.CREATED_DATE.toString() to item.CREATED_DATE,
                                EAvailableMerchant.UPDATED_DATE.toString() to dateFormat().format(Date()),
                                EAvailableMerchant.CREDENTIAL.toString() to item.CREDENTIAL,
                                EAvailableMerchant.STATUS.toString() to EStatusCode.ACTIVE.toString(),
                                EAvailableMerchant.USER_GROUP.toString() to userGroup,
                                EAvailableMerchant.NAME.toString() to item.NAME,
                                EAvailableMerchant.MERCHANT_CODE.toString() to getMerchantCode(context)
                            )

                            database.child(ETable.AVAILABLE_MERCHANT.toString())
                                .child(userCode)
                                .child(key.toString())
                                .setValue(values).addOnFailureListener {
                                    view.response(it.message.toString())
                                }
                                .addOnSuccessListener {
                                    view.response(EMessageResult.UPDATE.toString())
                                }
                        }else
                            view.response("Please Contact Your Administrator!! -- Call Available Merchant")
                    }else
                        view.response("Please Contact Your Administrator!! -- Call Available Merchant")
                }

            }
            database.child(ETable.AVAILABLE_MERCHANT.toString())
                .child(userCode)
                .orderByChild(EAvailableMerchant.NAME.toString())
                .equalTo(getMerchantCode(context))
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
                            key = try {
                                data.key.toString().toInt() + 1
                            } catch (e: NumberFormatException) {
                                -99
                            }
                            break
                        }
                    }
                    if (key != -99){
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

    fun dismissListener(){
    }
}

