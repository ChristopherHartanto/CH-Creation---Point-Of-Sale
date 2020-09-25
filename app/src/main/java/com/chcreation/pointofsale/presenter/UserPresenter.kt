package com.chcreation.pointofsale.presenter

import android.content.Context
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.ActivityLogs
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.model.User
import com.chcreation.pointofsale.model.UserAcceptance
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class UserPresenter(private val view: MainView, private val auth: FirebaseAuth, private val database: DatabaseReference, private val context: Context){

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun saveUser(name: String, email: String,instanceId: String){
        try {
            val values  = hashMapOf(
                EUser.NAME.toString() to name,
                EUser.EMAIL.toString() to email,
                EUser.CREATED_DATE.toString() to dateFormat().format(Date()),
                EUser.UPDATED_DATE.toString() to dateFormat().format(Date()),
                EUser.MEMBER_STATUS.toString() to EUserMemberStatus.FREE_TRIAL.toString(),
                EUser.ACTIVE.toString() to EStatusCode.ACTIVE.toString(),
                EUser.DEVICE_ID.toString() to instanceId
            )
            database.child(ETable.USER.toString())
                .child(auth.currentUser!!.uid)
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
                    .child(getMerchantCode(context))
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
            .addListenerForSingleValueEvent(postListener)
    }

    fun retrieveUserLists(){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_USER_LIST_SUCCESS.toString())
            }

        }
        database.child(ETable.MERCHANT.toString())
            .child(getMerchantCredential(context))
            .child(getMerchantCode(context))
            .child(EMerchant.USER_LIST.toString())
            .addListenerForSingleValueEvent(postListener)
    }

    suspend fun retrieveUser(userId: String){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                view.loadData(p0, EMessageResult.FETCH_USER_SUCCESS.toString())
            }

        }
        database.child(ETable.USER.toString())
            .child(userId)
            .addListenerForSingleValueEvent(postListener)
    }

    fun retrieveUser(userId: String,key:Int,callback:(success:Boolean,key:Int,user:User)->Unit){
        postListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                database.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    val item = p0.getValue(User::class.java)
                    if (item != null) {
                        callback(true,key,item)
                    }else
                        callback(false,key, User())
                }else
                    callback(false,key,User())
            }

        }
        database.child(ETable.USER.toString())
            .child(userId)
            .addListenerForSingleValueEvent(postListener)
    }

    fun inviteUser(userAcceptance: UserAcceptance, email: String){
        val values  = hashMapOf(
            EUserAcceptance.NAME.toString() to userAcceptance.NAME,
            EUserAcceptance.MERCHANT_CODE.toString() to userAcceptance.MERCHANT_CODE,
            EUserAcceptance.USER_GROUP.toString() to userAcceptance.USER_GROUP,
            EUserAcceptance.CREATED_DATE.toString() to userAcceptance.CREATED_DATE,
            EUserAcceptance.CREDENTIAL.toString() to userAcceptance.CREDENTIAL
        )

        database.child(ETable.USER_ACCEPTANCE.toString())
            .child(email)
            .setValue(values).addOnFailureListener {
                view.response(it.message.toString())
            }
            .addOnSuccessListener {
                view.response(EMessageResult.CREATE_INVITATION_SUCCESS.toString())
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

