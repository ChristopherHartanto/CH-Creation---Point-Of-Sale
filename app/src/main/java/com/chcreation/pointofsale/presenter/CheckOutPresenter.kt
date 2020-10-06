package com.chcreation.pointofsale.presenter

import android.content.Context
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.transCode
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.transDate
import com.chcreation.pointofsale.model.ActivityLogs
import com.chcreation.pointofsale.model.Cart
import com.chcreation.pointofsale.model.Payment
import com.chcreation.pointofsale.model.Transaction
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class CheckOutPresenter(private val view: MainView, private val auth: FirebaseAuth, private val database: DatabaseReference,private val context: Context){

    var transactionKey = 1
    var paymentKey = 1

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    suspend fun saveTransaction(transaction: Transaction,payment: Payment, cartItems: ArrayList<Cart>) : String{
        return suspendCoroutine {ctx->
            try{
                for ((index,data) in cartItems.withIndex()){
                    if (cartItems[index].MANAGE_STOCK!!){
                        postListener = object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                                database.removeEventListener(this)
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                var currentStock = 0F
                                if (p0.exists())
                                    currentStock = p0.value.toString().toFloat()

                                database.child(ETable.PRODUCT.toString())
                                    .child(getMerchantCredential(context))
                                    .child(getMerchantCode(context))
                                    .child(cartItems[index].PROD_KEY.toString())
                                    .child(EProduct.STOCK.toString())
                                    .setValue(currentStock - cartItems[index].Qty!!).addOnFailureListener {
                                        view.response(it.message.toString())
                                    }
                                    .addOnSuccessListener {
                                    }
                            }

                        }
                        database.child(ETable.PRODUCT.toString())
                            .child(getMerchantCredential(context))
                            .child(getMerchantCode(context))
                            .child(cartItems[index].PROD_KEY.toString())
                            .child(EProduct.STOCK.toString())
                            .addListenerForSingleValueEvent(postListener)
                    }
                    if (index == cartItems.size-1)
                        getTransPrimaryKey(transaction,payment,cartItems){
                            ctx.resume(it)
                        }
                }
            }catch (e: Exception){
                showError(context,e.message.toString())
                e.printStackTrace()
            }
        }
    }

    private fun saveEnquiry(transaction: Transaction, cartItems: ArrayList<Cart>){
        try{
            var enquiryKey = 0
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        for (data in p0.children){
                            enquiryKey = data.key.toString().toInt() + 1
                            break
                        }
                    }
                    for ((index,data) in cartItems.withIndex()){

                        val values  = hashMapOf(
                            E_Enquiry.TRANS_KEY.toString() to transCode,
                            E_Enquiry.PROD_CODE.toString() to cartItems[index].PROD_CODE,
                            E_Enquiry.PROD_KEY.toString() to cartItems[index].PROD_KEY,
                            E_Enquiry.CUST_CODE.toString() to transaction.CUST_CODE,
                            E_Enquiry.MANAGE_STOCK.toString() to cartItems[index].MANAGE_STOCK,
                            E_Enquiry.STOCK.toString() to cartItems[index].Qty,
                            E_Enquiry.STATUS_CODE.toString() to transaction.STATUS_CODE,
                            E_Enquiry.CREATED_DATE.toString() to transaction.CREATED_DATE,
                            E_Enquiry.UPDATED_DATE.toString() to transaction.UPDATED_DATE,
                            E_Enquiry.CREATED_BY.toString() to transaction.CREATED_BY,
                            E_Enquiry.UPDATED_BY.toString() to transaction.UPDATED_BY
                        )

                        database.child(ETable.ENQUIRY.toString())
                            .child(getMerchantCredential(context))
                            .child(getMerchantCode(context))
                            .child(enquiryKey.toString())
                            .setValue(values).addOnFailureListener {
                                view.response(it.message.toString())
                            }
                            .addOnSuccessListener {
                            }

                        enquiryKey += 1
                    }

                }

            }
            database.child(ETable.ENQUIRY.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .orderByKey()
                .limitToLast(1)
                .addListenerForSingleValueEvent(postListener)
        }catch (e: java.lang.Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    private fun saveStockMovement(transaction: Transaction, cartItems: ArrayList<Cart>){
        try{
            var stockMovementKey = 0
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        for (data in p0.children){
                            stockMovementKey = data.key.toString().toInt() + 1
                            break
                        }
                    }
                    for ((index,data) in cartItems.withIndex()){

                        val values  = hashMapOf(
                            EStock_Movement.TRANS_KEY.toString() to transCode,
                            EStock_Movement.PROD_CODE.toString() to cartItems[index].PROD_CODE,
                            EStock_Movement.PROD_KEY.toString() to cartItems[index].PROD_KEY,
                            EStock_Movement.STATUS.toString() to EStatusStock.OUTBOUND,
                            EStock_Movement.QTY.toString() to cartItems[index].Qty,
                            EStock_Movement.STATUS_CODE.toString() to transaction.STATUS_CODE,
                            EStock_Movement.CREATED_DATE.toString() to transaction.CREATED_DATE,
                            EStock_Movement.UPDATED_DATE.toString() to transaction.UPDATED_DATE,
                            EStock_Movement.UPDATED_BY.toString() to transaction.UPDATED_BY
                        )

                        database.child(ETable.STOCK_MOVEMENT.toString())
                            .child(getMerchantCredential(context))
                            .child(getMerchantCode(context))
                            .child(stockMovementKey.toString())
                            .setValue(values).addOnFailureListener {
                                view.response(it.message.toString())
                            }
                            .addOnSuccessListener {
                            }

                        stockMovementKey += 1
                    }

                }

            }
            database.child(ETable.STOCK_MOVEMENT.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .orderByKey()
                .limitToLast(1)
                .addListenerForSingleValueEvent(postListener)
        }catch (e: java.lang.Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    private fun getTransPrimaryKey(transaction: Transaction,payment: Payment, cartItems: ArrayList<Cart>, callback:(receipt:String)->Unit){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        for (data in p0.children){
                            transactionKey = data.key.toString().toInt() + 1
                            break
                        }
                    }

                    transaction.TRANS_CODE = "T"+generateTransCode()
                    transCode = transactionKey

                    GlobalScope.launch(Dispatchers.IO) {
                        saveEnquiry(transaction,cartItems)
                        saveStockMovement(transaction,cartItems)
                    }

                    transDate = transaction.CREATED_DATE.toString()
                    val values  = hashMapOf(
                        ETransaction.DETAIL.toString() to transaction.DETAIL,
                        ETransaction.CUST_CODE.toString() to transaction.CUST_CODE,
                        ETransaction.NOTE.toString() to transaction.NOTE,
                        ETransaction.TRANS_CODE.toString() to transaction.TRANS_CODE,
                        ETransaction.PAYMENT_METHOD.toString() to transaction.PAYMENT_METHOD,
                        ETransaction.TOTAL_PRICE.toString() to transaction.TOTAL_PRICE,
                        ETransaction.TOTAL_OUTSTANDING.toString() to transaction.TOTAL_OUTSTANDING,
                        ETransaction.DISCOUNT.toString() to transaction.DISCOUNT,
                        ETransaction.TAX.toString() to transaction.TAX,
                        ETransaction.STATUS_CODE.toString() to transaction.STATUS_CODE,
                        ETransaction.CREATED_DATE.toString() to transaction.CREATED_DATE,
                        ETransaction.UPDATED_DATE.toString() to transaction.UPDATED_DATE,
                        ETransaction.CREATED_BY.toString() to transaction.CREATED_BY,
                        ETransaction.UPDATED_BY.toString() to transaction.UPDATED_BY,
                        ETransaction.TABLE_NO.toString() to transaction.TABLE_NO,
                        ETransaction.PEOPLE_NO.toString() to transaction.PEOPLE_NO
                    )
                    database.child(ETable.TRANSACTION.toString())
                        .child(getMerchantCredential(context))
                        .child(getMerchantCode(context))
                        .child(transactionKey.toString())
                        .setValue(values).addOnFailureListener {
                            view.response(it.message.toString())
                        }
                        .addOnSuccessListener {
                            updatePayment(transactionKey,payment)
                            callback(transactionKey.toString())
                        }
                }

            }
            database.child(ETable.TRANSACTION.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .orderByKey()
                .limitToLast(1)
                .addListenerForSingleValueEvent(postListener)
        }catch (e: java.lang.Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun updatePayment(transactionKey: Int,payment: Payment){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        for (data in p0.children){
                            paymentKey = data.key.toString().toInt() + 1
                            break
                        }
                    }

                    val createdDate: String = dateFormat().format(Date())
                    transCode = transactionKey
                    transDate = createdDate
                    val values  = hashMapOf(
                        EPayment.TOTAL_RECEIVED.toString() to payment.TOTAL_RECEIVED,
                        EPayment.USER_CODE.toString() to payment.USER_CODE,
                        EPayment.NOTE.toString() to payment.NOTE,
                        EPayment.CREATED_DATE.toString() to createdDate,
                        EPayment.PAYMENT_METHOD.toString() to payment.PAYMENT_METHOD,
                        EPayment.STATUS_CODE.toString() to payment.STATUS_CODE
                    )
                    database.child(ETable.PAYMENT.toString())
                        .child(getMerchantCredential(context))
                        .child(getMerchantCode(context))
                        .child(transactionKey.toString())
                        .child(paymentKey.toString())
                        .setValue(values).addOnFailureListener {
                            view.response(it.message.toString())
                        }
                        .addOnSuccessListener {
                            view.response(EMessageResult.SUCCESS.toString())
                        }
                }

            }
            database.child(ETable.PAYMENT.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .child(transactionKey.toString())
                .orderByKey()
                .limitToLast(1)
                .addListenerForSingleValueEvent(postListener)
        }catch (e: java.lang.Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun retrievePendingPayment(transactionCode: Int){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    view.loadData(p0, EMessageResult.FETCH_PEND_PAYMENT_SUCCESS.toString())
                }

            }
            database.child(ETable.TRANSACTION.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .child(transactionCode.toString())
                .addListenerForSingleValueEvent(postListener)
        }catch (e: java.lang.Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun savePendingPayment(transactionCode: Int,payment: Payment, newTotalOutstanding: Float, transaction: Transaction){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        for (data in p0.children){
                            paymentKey = data.key.toString().toInt() + 1
                            break
                        }
                    }

                    val createdDate: String = dateFormat().format(Date())

                    val values  = hashMapOf(
                        EPayment.TOTAL_RECEIVED.toString() to payment.TOTAL_RECEIVED,
                        EPayment.USER_CODE.toString() to payment.USER_CODE,
                        EPayment.NOTE.toString() to payment.NOTE,
                        EPayment.CREATED_DATE.toString() to createdDate,
                        EPayment.PAYMENT_METHOD.toString() to payment.PAYMENT_METHOD,
                        EPayment.STATUS_CODE.toString() to payment.STATUS_CODE,
                        EPayment.CREATED_BY.toString() to payment.CREATED_BY,
                        EPayment.UPDATED_BY.toString() to payment.UPDATED_BY,
                        EPayment.UPDATED_DATE.toString() to payment.UPDATED_DATE
                    )
                    database.child(ETable.PAYMENT.toString())
                        .child(getMerchantCredential(context))
                        .child(getMerchantCode(context))
                        .child(transactionCode.toString())
                        .child(paymentKey.toString())
                        .setValue(values).addOnFailureListener {
                            view.response(it.message.toString())
                        }
                        .addOnSuccessListener {
                            updateTotalOutstanding(transactionCode,newTotalOutstanding,transaction)
                        }
                }

            }
            database.child(ETable.PAYMENT.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .child(transactionCode.toString())
                .orderByKey()
                .limitToLast(1)
                .addListenerForSingleValueEvent(postListener)
        }catch (e: java.lang.Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun updateTotalOutstanding(transactionCode: Int, newTotalOutstanding: Float, transaction: Transaction){
        try{
            var status = EStatusCode.DONE.toString()
            if (newTotalOutstanding > 0)
                status = EStatusCode.PENDING.toString()

            val values  = hashMapOf(
                ETransaction.DETAIL.toString() to transaction.DETAIL,
                ETransaction.CUST_CODE.toString() to transaction.CUST_CODE,
                ETransaction.NOTE.toString() to transaction.NOTE,
                ETransaction.TRANS_CODE.toString() to transaction.TRANS_CODE,
                ETransaction.PAYMENT_METHOD.toString() to transaction.PAYMENT_METHOD,
                ETransaction.TOTAL_PRICE.toString() to transaction.TOTAL_PRICE,
                ETransaction.TOTAL_OUTSTANDING.toString() to newTotalOutstanding,
                ETransaction.DISCOUNT.toString() to transaction.DISCOUNT,
                ETransaction.TAX.toString() to transaction.TAX,
                ETransaction.STATUS_CODE.toString() to status,
                ETransaction.CREATED_DATE.toString() to transaction.CREATED_DATE,
                ETransaction.UPDATED_DATE.toString() to dateFormat().format(Date()),
                ETransaction.CREATED_BY.toString() to transaction.CREATED_BY,
                ETransaction.UPDATED_BY.toString() to transaction.UPDATED_BY,
                ETransaction.TABLE_NO.toString() to transaction.TABLE_NO,
                ETransaction.PEOPLE_NO.toString() to transaction.PEOPLE_NO
            )
            database.child(ETable.TRANSACTION.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .child(transactionCode.toString())
                .setValue(values).addOnFailureListener {
                    view.response(it.message.toString())
                }
                .addOnSuccessListener {
                    if (status == EStatusCode.DONE.toString()){
                        updateStockMovement(transactionCode,status)
                        updateEnquiry(transactionCode,status, transaction.UPDATED_BY.toString())
                    }
                    view.response(EMessageResult.SUCCESS.toString())
                }
        }catch (e: java.lang.Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }

    }



    private fun updateStockMovement(transactionKey: Int,status: String){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        for (data in p0.children){
                            database.child(ETable.STOCK_MOVEMENT.toString())
                                .child(getMerchantCredential(context))
                                .child(getMerchantCode(context))
                                .child(data.key.toString())
                                .child(EStock_Movement.STATUS_CODE.toString())
                                .setValue(status).addOnFailureListener {
                                    view.response(it.message.toString())
                                }
                                .addOnSuccessListener {
                                }

                            database.child(ETable.STOCK_MOVEMENT.toString())
                                .child(getMerchantCredential(context))
                                .child(getMerchantCode(context))
                                .child(data.key.toString())
                                .child(EStock_Movement.UPDATED_DATE.toString())
                                .setValue(dateFormat().format(Date())).addOnFailureListener {
                                    view.response(it.message.toString())
                                }
                                .addOnSuccessListener {
                                }
                        }
                    }


                }

            }
            database.child(ETable.STOCK_MOVEMENT.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .orderByChild(EStock_Movement.TRANS_KEY.toString())
                .equalTo(transactionKey.toDouble())
                .addListenerForSingleValueEvent(postListener)
        }catch (e: java.lang.Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    private fun updateEnquiry(transactionKey: Int,status: String, updatedBy: String){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        for (data in p0.children){
                            database.child(ETable.ENQUIRY.toString())
                                .child(getMerchantCredential(context))
                                .child(getMerchantCode(context))
                                .child(data.key.toString())
                                .child(E_Enquiry.STATUS_CODE.toString())
                                .setValue(status).addOnFailureListener {
                                    view.response(it.message.toString())
                                }
                                .addOnSuccessListener {
                                }

                            database.child(ETable.ENQUIRY.toString())
                                .child(getMerchantCredential(context))
                                .child(getMerchantCode(context))
                                .child(data.key.toString())
                                .child(E_Enquiry.UPDATED_DATE.toString())
                                .setValue(dateFormat().format(Date())).addOnFailureListener {
                                    view.response(it.message.toString())
                                }
                                .addOnSuccessListener {
                                }

                            database.child(ETable.ENQUIRY.toString())
                                .child(getMerchantCredential(context))
                                .child(getMerchantCode(context))
                                .child(data.key.toString())
                                .child(E_Enquiry.UPDATED_BY.toString())
                                .setValue(updatedBy).addOnFailureListener {
                                    view.response(it.message.toString())
                                }
                                .addOnSuccessListener {
                                }
                        }
                    }


                }

            }
            database.child(ETable.ENQUIRY.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .orderByChild(E_Enquiry.TRANS_KEY.toString())
                .equalTo(transactionKey.toDouble())
                .addListenerForSingleValueEvent(postListener)
        }catch (e: java.lang.Exception){
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

    private fun generateTransCode() : String{
        return database.push().key.toString()
    }

    fun dismissListener(){
    }
}

