package com.chcreation.pointofsale.presenter

import android.content.Context
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.checkout.CheckOutActivity
import com.chcreation.pointofsale.model.Cart
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.model.StockMovement
import com.chcreation.pointofsale.model.Transaction
import com.chcreation.pointofsale.product.NewCategory
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

    fun getProductPrimaryKey(product: Product){
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
            .child(getMerchant(context))
            .orderByKey()
            .limitToLast(1)
            .addListenerForSingleValueEvent(postListener)
    }

    fun saveProduct(product: Product, key: Int){

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
            EProduct.CREATED_DATE.toString() to product.CREATED_DATE,
            EProduct.CREATED_BY.toString() to product.CREATED_BY,
            EProduct.UPDATED_DATE.toString() to product.UPDATED_DATE,
            EProduct.UPDATED_BY.toString() to product.UPDATED_BY
        )
        database.child(ETable.PRODUCT.toString())
            .child(getMerchantCredential(context))
            .child(getMerchant(context))
            .child(key.toString())
            .setValue(values).addOnFailureListener {
                view.response(it.message.toString())
            }
            .addOnSuccessListener {
                view.response(EMessageResult.SUCCESS.toString())
            }
    }

    fun saveNewCategory(newCategory: String){
        val timeStamp: String = dateFormat().format(Date())

        val values  = hashMapOf(
            EMerchant.CREATED_DATE.toString() to timeStamp,
            EMerchant.UPDATED_DATE.toString() to timeStamp,
            EMerchant.CAT.toString() to newCategory
        )

        database.child(ETable.MERCHANT.toString())
            .child(getMerchantCredential(context))
            .child(getMerchant(context))
            .child(EMerchant.CAT.toString())
            .child(newCategory)
            .setValue(values).addOnFailureListener {
                view.response(it.message.toString())
            }
            .addOnSuccessListener {
                view.response(EMessageResult.SUCCESS.toString())
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
                .child(getMerchant(context))
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
                .child(getMerchant(context))
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
                .child(getMerchant(context))
                .orderByChild(EProduct.PROD_CODE.toString())
                .equalTo(prodCode)
                .addListenerForSingleValueEvent(postListener)

        }catch (e: Exception){
            view.response(e.message.toString())
        }

    }

    fun retrieveStockMovement(prodCode: String){
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
                .child(getMerchant(context))
                .orderByChild(EProduct.PROD_CODE.toString())
                .equalTo(prodCode)
                .addListenerForSingleValueEvent(postListener)

        }catch (e: Exception){
            view.response(e.message.toString())
        }
    }

    private fun saveStockMovement(product: Product, prodKey: Int){
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
                    EStock_Movement.UPDATED_DATE.toString() to product.UPDATED_DATE
                )

                database.child(ETable.STOCK_MOVEMENT.toString())
                    .child(getMerchantCredential(context))
                    .child(getMerchant(context))
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
            .child(getMerchant(context))
            .orderByKey()
            .limitToLast(1)
            .addListenerForSingleValueEvent(postListener)
    }

    fun addStockMovement(stockMovement: StockMovement){
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
                    EStock_Movement.PROD_CODE.toString() to stockMovement.PROD_CODE,
                    EStock_Movement.PROD_KEY.toString() to stockMovement.PROD_KEY,
                    EStock_Movement.STATUS.toString() to stockMovement.STATUS,
                    EStock_Movement.QTY.toString() to stockMovement.QTY,
                    EStock_Movement.STATUS_CODE.toString() to stockMovement.STATUS_CODE,
                    EStock_Movement.CREATED_DATE.toString() to stockMovement.CREATED_DATE,
                    EStock_Movement.UPDATED_DATE.toString() to stockMovement.UPDATED_DATE
                )

                database.child(ETable.STOCK_MOVEMENT.toString())
                    .child(getMerchantCredential(context))
                    .child(getMerchant(context))
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
            .child(getMerchant(context))
            .orderByKey()
            .limitToLast(1)
            .addListenerForSingleValueEvent(postListener)
    }


    private fun generateProdCode() : String{
        return database.push().key.toString()
    }

    fun dismissListener(){
    }
}

