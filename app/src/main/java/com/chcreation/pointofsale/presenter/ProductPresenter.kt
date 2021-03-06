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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ProductPresenter(private val view: MainView,
                       private val auth: FirebaseAuth,
                       private val database: DatabaseReference,
                       private val context: Context){

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun saveProduct(product: Product){
        try {
            getProductPrimaryKey(product)

        }catch (e:java.lang.Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun deleteProduct(prodCode: String){
        try {
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        for (data in p0.children){
                            database.child(ETable.PRODUCT.toString())
                                .child(getMerchantCredential(context))
                                .child(getMerchantCode(context))
                                .child(data.key.toString())
                                .child(EProduct.STATUS_CODE.toString())
                                .setValue(EStatusCode.DELETE.toString()).addOnFailureListener {
                                    view.response(it.message.toString())
                                }
                                .addOnSuccessListener {
                                    view.response(EMessageResult.DELETE_SUCCESS.toString())
                                }
                        }
                    }
                }

            }
            database.child(ETable.PRODUCT.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .orderByChild(EProduct.PROD_CODE.toString())
                .equalTo(prodCode)
                .addListenerForSingleValueEvent(postListener)

        }catch (e:java.lang.Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    fun checkProductSize(callback:(size:Int) -> Unit){
        try {
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        val listProduct = mutableListOf<Product>()
                        for (data in p0.children){
                            val item = data.getValue(Product::class.java)
                            if (item != null) {
                                listProduct.add(item)
                            }
                        }
                        callback(listProduct.size)
                    }else
                        callback(0)
                }

            }
            database.child(ETable.PRODUCT.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .addListenerForSingleValueEvent(postListener)

        }catch (e:java.lang.Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    private fun getProductPrimaryKey(product: Product){
        try{
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

                    product.PROD_CODE = "P${generateProdCode()}"
                    saveProduct(product,key)

                    if (product.MANAGE_STOCK)
                        saveStockMovement(product,key)
                }

            }
            database.child(ETable.PRODUCT.toString())
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

    fun saveProduct(product: Product, key: Int){
        try{
            val values  = hashMapOf(
                EProduct.NAME.toString() to product.NAME,
                EProduct.COST.toString() to product.COST,
                EProduct.DESC.toString() to product.DESC,
                EProduct.PRICE.toString() to product.PRICE,
                EProduct.PROD_CODE.toString() to product.PROD_CODE,
                EProduct.UOM_CODE.toString() to product.UOM_CODE,
                EProduct.MANAGE_STOCK.toString() to product.MANAGE_STOCK,
                EProduct.STOCK.toString() to product.STOCK,
                EProduct.IMAGE.toString() to product.IMAGE,
                EProduct.CAT.toString() to product.CAT,
                EProduct.CODE.toString() to product.CODE,
                EProduct.STATUS_CODE.toString() to product.STATUS_CODE,
                EProduct.CREATED_DATE.toString() to product.CREATED_DATE,
                EProduct.CREATED_BY.toString() to product.CREATED_BY,
                EProduct.UPDATED_DATE.toString() to product.UPDATED_DATE,
                EProduct.UPDATED_BY.toString() to product.UPDATED_BY,
                EProduct.WHOLE_SALE.toString() to product.WHOLE_SALE
            )
            database.child(ETable.PRODUCT.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .child(key.toString())
                .updateChildren(values).addOnFailureListener {
                    view.response(it.message.toString())
                }
                .addOnSuccessListener {
                    view.response(EMessageResult.SUCCESS.toString())
                }
        }catch (e:java.lang.Exception){
            showError(context,e.message.toString())
            e.printStackTrace()
        }
    }

    suspend fun saveNewCategory(categoryItems: String) : String{
        return suspendCoroutine { ctx->
            try{
                database.child(ETable.MERCHANT.toString())
                    .child(getMerchantCredential(context))
                    .child(getMerchantCode(context))
                    .child(EMerchant.CAT.toString())
                    .setValue(categoryItems).addOnFailureListener {
                        ctx.resume(it.message.toString())
                    }
                    .addOnSuccessListener {
                        view.response(EMessageResult.SUCCESS.toString())
                    }
            }catch (e:java.lang.Exception){
                showError(context,e.message.toString())
                e.printStackTrace()
            }
        }
    }

    fun retrieveCategories(){
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
            view.response(e.message.toString())
        }
    }

    fun retrieveProducts(){
        try {
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
                .orderByChild(EProduct.CAT.toString())
                .addListenerForSingleValueEvent(postListener)

        }catch (e: Exception){
            view.response(e.message.toString())
        }

    }

    fun retrieveProductByProdCode(prodCode: String){
        try {
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
                .orderByChild(EProduct.PROD_CODE.toString())
                .equalTo(prodCode)
                .addListenerForSingleValueEvent(postListener)

        }catch (e: Exception){
            view.response(e.message.toString())
        }

    }

    suspend fun retrieveStockMovement(prodCode: String){
        try {
            postListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    database.removeEventListener(this)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    view.loadData(p0, EMessageResult.FETCH_STOCK_MOVEMENT_SUCCESS.toString())
                }

            }
            database.child(ETable.STOCK_MOVEMENT.toString())
                .child(getMerchantCredential(context))
                .child(getMerchantCode(context))
                .orderByChild(EProduct.PROD_CODE.toString())
                .equalTo(prodCode)
                .addListenerForSingleValueEvent(postListener)

        }catch (e: Exception){
            view.response(e.message.toString())
        }
    }

    private fun saveStockMovement(product: Product, prodKey: Int){
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
                    val values  = hashMapOf(
                        EStock_Movement.PROD_CODE.toString() to product.PROD_CODE,
                        EStock_Movement.PROD_KEY.toString() to prodKey,
                        EStock_Movement.STATUS.toString() to EStatusStock.INBOUND,
                        EStock_Movement.QTY.toString() to product.STOCK,
                        EStock_Movement.STATUS_CODE.toString() to EStatusCode.DONE,
                        EStock_Movement.CREATED_DATE.toString() to product.CREATED_DATE,
                        EStock_Movement.UPDATED_DATE.toString() to product.UPDATED_DATE,
                        EStock_Movement.UPDATED_BY.toString() to product.UPDATED_BY
                    )

                    database.child(ETable.STOCK_MOVEMENT.toString())
                        .child(getMerchantCredential(context))
                        .child(getMerchantCode(context))
                        .child(stockMovementKey.toString())
                        .setValue(values).addOnFailureListener {
                            view.response(it.message.toString())
                        }
                        .addOnSuccessListener {
                            view.response(EMessageResult.SUCCESS.toString())
                        }

                }

            }
            database.child(ETable.STOCK_MOVEMENT.toString())
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

    suspend fun addStockMovement(stockMovement: StockMovement) : String{
        return suspendCoroutine {ctx->
            try{
                var stockMovementKey = 0
                postListener = object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        database.removeEventListener(this)
                        ctx.resume(p0.message)
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            for (data in p0.children){
                                stockMovementKey = data.key.toString().toInt() + 1
                                break
                            }
                        }
                        val values  = hashMapOf(
                            EStock_Movement.NOTE.toString() to stockMovement.NOTE,
                            EStock_Movement.PROD_CODE.toString() to stockMovement.PROD_CODE,
                            EStock_Movement.PROD_KEY.toString() to stockMovement.PROD_KEY,
                            EStock_Movement.STATUS.toString() to stockMovement.STATUS,
                            EStock_Movement.QTY.toString() to stockMovement.QTY,
                            EStock_Movement.STATUS_CODE.toString() to stockMovement.STATUS_CODE,
                            EStock_Movement.CREATED_DATE.toString() to stockMovement.CREATED_DATE,
                            EStock_Movement.UPDATED_DATE.toString() to stockMovement.UPDATED_DATE,
                            EStock_Movement.UPDATED_BY.toString() to stockMovement.UPDATED_BY
                        )

                        database.child(ETable.STOCK_MOVEMENT.toString())
                            .child(getMerchantCredential(context))
                            .child(getMerchantCode(context))
                            .child(stockMovementKey.toString())
                            .setValue(values).addOnFailureListener {
                                ctx.resume(it.message.toString())
                            }
                            .addOnSuccessListener {
                                ctx.resume("Success")
                            }

                    }

                }
                database.child(ETable.STOCK_MOVEMENT.toString())
                    .child(getMerchantCredential(context))
                    .child(getMerchantCode(context))
                    .orderByKey()
                    .limitToLast(1)
                    .addListenerForSingleValueEvent(postListener)
            }catch (e:java.lang.Exception){
                showError(context,e.message.toString())
                e.printStackTrace()
                ctx.resume(e.message.toString())
            }
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

    suspend fun saveActivityLogs(logs: ActivityLogs) : Boolean{
        return suspendCoroutine {ctx->
            try{
                var key = 0
                postListener = object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        database.removeEventListener(this)
                        ctx.resume(false)
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
                            .addOnCompleteListener {
                                ctx.resume(true)
                            }.addOnFailureListener {
                                ctx.resume(false)
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
                ctx.resume(false)
            }
        }
    }

    private fun generateProdCode() : String{
        return database.push().key.toString()
    }

    fun dismissListener(){
    }
}

