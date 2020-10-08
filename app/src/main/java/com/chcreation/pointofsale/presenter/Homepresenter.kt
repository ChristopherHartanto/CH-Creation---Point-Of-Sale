package com.chcreation.pointofsale.presenter

import android.content.Context
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.custom_receipt.Sincere
import com.chcreation.pointofsale.model.*
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Homepresenter(private val view: MainView, private val auth: FirebaseAuth, private val database: DatabaseReference, private val  context: Context){

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    suspend fun getSincere() : String{
        return suspendCoroutine {ctx->
            try{
                postListener = object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        database.removeEventListener(this)
                        ctx.resume("Thank You")
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            val item = p0.getValue(Sincere::class.java)
                            if (item != null) {
                                ctx.resume(item.SINCERE.toString())
                            }else
                                ctx.resume("Thank You")
                        }else
                            ctx.resume("Thank You")
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
                .orderByChild(EProduct.NAME.toString())
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun retrieveActivityLogs(){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    view.loadData(p0, EMessageResult.FETCH_ACTIVITY_LOG_SUCCESS.toString())
                }

            }

            database.child(ETable.ACTIVITY_LOGS.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .limitToLast(200)
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    suspend fun retrieveCategories(){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    view.loadData(p0, EMessageResult.FETCH_CATEGORY_SUCCESS.toString())
                }

            }
            database.child(ETable.MERCHANT.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .child(EMerchant.CAT.toString())
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun retrieveMerchant(){
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

    fun getUserDetail(userCode : String, callBack:(user:User) -> Unit){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        val item = p0.getValue(User::class.java)

                        if (item != null) {
                            callBack(item)
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

    fun getMerchant(callback:(success: Boolean,merchant: Merchant)->Unit){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        val item = p0.getValue(Merchant::class.java)
                        if (item != null) {
                            callback(true, item)
                        }
                        else
                            callback(false,Merchant())
                    }
                    else
                        callback(false,Merchant())
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

    fun getAvailableMerchant(callback:(success: Boolean, availableMerchant: AvailableMerchant?)->Unit){
        try{

            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        var check = false
                        for (data in p0.children){
                            val item = data.getValue(AvailableMerchant::class.java)
                            if (item != null && item.STATUS == EStatusCode.ACTIVE.toString()) {
                                callback(true,item)
                                return
                            }else
                                check = false
                        }
                        if (!check)
                            callback(false,null)
                    }
                    else{ // merchant haven't keep merchant code, so using merchant name to check
                        try{
                            postListener = object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                    database.removeEventListener(this)
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    if (p0.exists()){
                                        var check = false
                                        for (data in p0.children){
                                            val item = data.getValue(AvailableMerchant::class.java)
                                            if (item != null && item.STATUS == EStatusCode.ACTIVE.toString()) {
                                                callback(true,item)
                                                return
                                            }else
                                                check = false
                                        }
                                        if (!check)
                                            callback(false,null)
                                    }
                                    else
                                        callback(false,null)
                                }

                            }
                            database.child(ETable.AVAILABLE_MERCHANT.toString())
                                .child(auth.currentUser!!.uid)
                                .orderByChild(EAvailableMerchant.NAME.toString())
                                .equalTo(getMerchantCode(context))
                                .addListenerForSingleValueEvent(postListener)
                        }catch (e: Exception){
                            showError(context,e.message.toString())
                            e.printStackTrace()
                        }
                    }
                }

            }
            database.child(ETable.AVAILABLE_MERCHANT.toString())
                .child(auth.currentUser!!.uid)
                .orderByChild(EAvailableMerchant.MERCHANT_CODE.toString())
                .equalTo(getMerchantCode(context))
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun retrieveAbout(callBack:(userName:About) -> Unit){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        val item = p0.getValue(About::class.java)

                        if (item != null) {
                            callBack(item)
                        }
                    }

                }

            }
            database.child(ETable.ABOUT.toString())
                .addListenerForSingleValueEvent(postListener)
        }catch (e:java.lang.Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    suspend fun retrieveOurCustomer() : DataSnapshot?{
        return suspendCoroutine { ctx->
            try{
                postListener = object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        database.removeEventListener(this)
                        ctx.resume(null)
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists())
                            ctx.resume(p0)
                        else
                            ctx.resume(null)
                    }

                }
                database.child(ETable.OUR_CUSTOMER.toString())
                    .addListenerForSingleValueEvent(postListener)
            }catch (e:java.lang.Exception){
                showError(context,e.message.toString())
                e.printStackTrace()
                ctx.resume(null)
            }
        }
    }

    suspend fun saveSincere(sincere:String) : Boolean{
        return suspendCoroutine {ctx->
            try{
                database.child(ETable.SINCERE.toString())
                    .child(getMerchantCredential(context))
                    .child(getMerchantCode(context))
                    .child(ESincere.SINCERE.toString())
                    .setValue(sincere)
                    .addOnSuccessListener {
                        ctx.resume(true)
                    }
                    .addOnFailureListener {
                        ctx.resume(false)
                    }
            }catch (e:java.lang.Exception){
                showError(context,e.message.toString())
                e.printStackTrace()
            }
        }
    }

    fun retrieveSincere(callBack:(sincere:Sincere) -> Unit){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        val item = p0.getValue(Sincere::class.java)

                        if (item != null) {
                            callBack(item)
                        }
                    }

                }

            }
            database.child(ETable.SINCERE.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .addListenerForSingleValueEvent(postListener)
        }catch (e:java.lang.Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun checkVersion(callBack:(version:Version) -> Unit){
        try{
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        val item = p0.getValue(Version::class.java)

                        if (item != null) {
                            callBack(item)
                        }
                    }

                }

            }
            database.child(ETable.VERSION.toString())
                .addListenerForSingleValueEvent(postListener)
        }catch (e:java.lang.Exception){
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

    fun dismissListener(){
        database.removeEventListener(postListener)
    }
}

