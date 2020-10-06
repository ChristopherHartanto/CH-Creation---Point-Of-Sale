package com.chcreation.pointofsale.presenter

import android.content.Context
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.custom_receipt.Sincere
import com.chcreation.pointofsale.model.ActivityLogs
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.model.User
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class TransactionPresenter(private val view: MainView, private val auth: FirebaseAuth, private val database: DatabaseReference, private val context: Context){

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun retrieveTransactions(){
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

    suspend fun retrieveProducts() : DataSnapshot?{
        return suspendCoroutine {ctx->
            try{
                postListener = object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        database.removeEventListener(this)
                        ctx.resume(null)
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            ctx.resume(p0)
                        }else
                            ctx.resume(null)
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
    }

    suspend fun retrieveTransaction(transactionCode: Int){
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
                .child(transactionCode.toString())
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    suspend fun retrieveTransactionListPayments(transactionCode:Int){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    view.loadData(p0, EMessageResult.FETCH_TRANS_LIST_PAYMENT_SUCCESS.toString())
                }

            }
            database.child(ETable.PAYMENT.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .child(transactionCode.toString())
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    suspend fun retrieveCashier(userId: String){
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
                .child(userId)
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun retrieveCustomers(){
        try{
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
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun retrieveCustomerByCode(custCode: String,callBack: (success: Boolean,customer: Customer?) -> Unit){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        for (data in p0.children){
                            val item = data.getValue(Customer::class.java)
                            if (item != null){
                                callBack(true,item)
                            }else
                                callBack(false,null)
                        }
                    }else{
                        callBack(false,null)
                    }
                }

            }
            database.child(ETable.CUSTOMER.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .orderByChild(ECustomer.CODE.toString())
                .equalTo(custCode)
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun cancelTransaction(transactionCode: Int){
        try {
            database.child(ETable.TRANSACTION.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .child(transactionCode.toString())
                .child(ETransaction.STATUS_CODE.toString())
                .setValue(EStatusCode.CANCEL.toString()).addOnFailureListener {
                    view.response(it.message.toString())
                }
                .addOnSuccessListener {
                    GlobalScope.launch(Dispatchers.Main) {
                        updateEnquiry(transactionCode,EStatusCode.CANCEL.toString())
                        updateStockMovement(transactionCode,EStatusCode.CANCEL.toString())
                        cancelPayment(transactionCode)
                    }
                }
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    suspend fun cancelPayment(transactionCode: Int){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()){
                        for (data in dataSnapshot.children){
                            database.child(ETable.PAYMENT.toString())
                                .child(getMerchantCredential(context))
                                .child(getMerchantCode(context))
                                .child(transactionCode.toString())
                                .child(data.key.toString())
                                .child(EPayment.STATUS_CODE.toString())
                                .setValue(EStatusCode.CANCEL.toString()).addOnFailureListener {
                                    view.response(it.message.toString())
                                }
                                .addOnSuccessListener {
                                    view.response(EMessageResult.SUCCESS.toString())
                                }
                        }
                    }else
                        view.response("No Payment to Cancel !")
                }

            }
            database.child(ETable.PAYMENT.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .child(transactionCode.toString())
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    suspend fun updateEnquiry(transactionCode: Int,statusCode: String){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        for (data in p0.children){

                            val currentDate: String = dateFormat().format(Date())
                            database.child(ETable.ENQUIRY.toString())
                                .child(getMerchantCredential(context))
                                .child(getMerchantCode(context))
                                .child(data.key.toString())
                                .child(E_Enquiry.STATUS_CODE.toString())
                                .setValue(statusCode).addOnFailureListener {
                                    view.response(it.message.toString())
                                }
                            database.child(ETable.ENQUIRY.toString())
                                .child(getMerchantCredential(context))
                                .child(getMerchantCode(context))
                                .child(data.key.toString())
                                .child(E_Enquiry.UPDATED_DATE.toString())
                                .setValue(currentDate).addOnFailureListener {
                                    view.response(it.message.toString())
                                }
                            database.child(ETable.ENQUIRY.toString())
                                .child(getMerchantCredential(context))
                                .child(getMerchantCode(context))
                                .child(data.key.toString())
                                .child(E_Enquiry.UPDATED_BY.toString())
                                .setValue(auth.currentUser!!.uid).addOnFailureListener {
                                    view.response(it.message.toString())
                                }
                        }
                    }

                }

            }
            database.child(ETable.ENQUIRY.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .orderByChild(E_Enquiry.TRANS_KEY.toString())
                .equalTo(transactionCode.toDouble())
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    private suspend fun updateStockMovement(transactionCode: Int, statusCode: String){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        for (data in p0.children){

                            val currentDate: String = dateFormat().format(Date())

                            val values: HashMap<String,Any>  = hashMapOf(
                                EStock_Movement.STATUS_CODE.toString() to statusCode,
                                EStock_Movement.UPDATED_DATE.toString() to currentDate
                            )

                            database.child(ETable.STOCK_MOVEMENT.toString())
                                .child(getMerchantCredential(context))
                                .child(getMerchantCode(context))
                                .child(data.key.toString())
                                .updateChildren(values)
                                .addOnFailureListener {
                                    view.response(it.message.toString())
                                }.addOnSuccessListener {
                                    database.child(ETable.STOCK_MOVEMENT.toString())
                                        .child(getMerchantCredential(context))
                                        .child(getMerchantCode(context))
                                        .child(data.key.toString())
                                        .child(EStock_Movement.UPDATED_DATE.toString())
                                        .setValue(currentDate).addOnFailureListener {
                                            view.response(it.message.toString())
                                        }
                                }
                        }
                    }

                }

            }
            database.child(ETable.STOCK_MOVEMENT.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .orderByChild(EStock_Movement.TRANS_KEY.toString())
                .equalTo(transactionCode.toDouble())
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun getUserName(userCode : String, callBack:(userName:String) -> Unit){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        val item = p0.getValue(User::class.java)

                        if (item != null) {
                            callBack(item.NAME.toString())
                        }
                    }

                }

            }
            database.child(ETable.USER.toString())
                .child(userCode)
                .addListenerForSingleValueEvent(postListener)
        }catch (e:java.lang.Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    suspend fun retrieveUserLists(){
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

    suspend fun getSincere(): String{
        return suspendCoroutine {ctx->
            try{
                postListener = object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        database.removeEventListener(this)
                        ctx.resume(getMerchantSincere(context))
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            val item = p0.getValue(Sincere::class.java)
                            if (item != null) {
                                ctx.resume(item.SINCERE.toString())
                            }else
                                ctx.resume(getMerchantSincere(context))
                        }else
                            ctx.resume(getMerchantSincere(context))
                    }

                }
                database.child(ETable.SINCERE.toString())
                    .child(getMerchantCredential(context))
                    .child(getMerchantCode(context))
                    .addListenerForSingleValueEvent(postListener)
            }catch (e: Exception){
                showError(context,e.message.toString())
                e.printStackTrace()
            }
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

    private fun generateTransCode() : String{
        return database.push().key.toString()
    }

    fun dismissListener(){
    }
}

