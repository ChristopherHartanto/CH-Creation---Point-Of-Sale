package com.chcreation.pointofsale.presenter

import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.Product
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
                       private val database: DatabaseReference){

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun saveProduct(product: Product, merchant: String){

       getProductPrimaryKey(product,merchant)
    }

    fun getProductPrimaryKey(product: Product, merchant: String){
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

                val values  = hashMapOf(
                    EProduct.NAME.toString() to product.NAME,
                    EProduct.COST.toString() to product.COST,
                    EProduct.DESC.toString() to product.DESC,
                    EProduct.PRICE.toString() to product.PRICE,
                    EProduct.PROD_CODE.toString() to product.PROD_CODE,
                    EProduct.UOM_CODE.toString() to product.UOM_CODE,
                    EProduct.STOCK.toString() to product.STOCK,
                    EProduct.IMAGE.toString() to product.IMAGE,
                    EProduct.CAT.toString() to product.CAT,
                    EProduct.CODE.toString() to product.CODE
                )
                database.child(ETable.PRODUCT.toString())
                    .child(auth.currentUser!!.uid)
                    .child(merchant)
                    .child(key.toString())
                    .setValue(values).addOnFailureListener {
                        view.response(it.message.toString())
                    }
                    .addOnSuccessListener {
                        view.response(EMessageResult.SUCCESS.toString())
                    }
            }

        }
        database.child(ETable.PRODUCT.toString())
            .child(auth.currentUser!!.uid)
            .child(merchant)
            .orderByKey()
            .limitToLast(1)
            .addListenerForSingleValueEvent(postListener)
    }

    fun saveNewCategory(merchant: String, newCategory: String){
        val timeStamp: String = dateFormat().format(Date())

        val values  = hashMapOf(
            EMerchant.CREATED_DATE.toString() to timeStamp,
            EMerchant.CAT.toString() to newCategory
        )

        database.child(ETable.MERCHANT.toString())
            .child(auth.currentUser!!.uid)
            .child(merchant)
            .child(EMerchant.CAT.toString())
            .child(newCategory)
            .setValue(values).addOnFailureListener {
                view.response(it.message.toString())
            }
            .addOnSuccessListener {
                view.response(EMessageResult.SUCCESS.toString())
            }
    }

    fun retrieveCategories(merchant: String){
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
                .child(auth.currentUser!!.uid)
                .child(merchant)
                .child(EMerchant.CAT.toString())
                .addListenerForSingleValueEvent(postListener)
        }catch (e: Exception){
            view.response(e.message.toString())
        }
    }

    fun retrieveProducts(merchant: String){
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
                .child(auth.currentUser!!.uid)
                .child(merchant)
                .orderByKey()
                .addListenerForSingleValueEvent(postListener)

        }catch (e: Exception){
            view.response(e.message.toString())
        }

    }

    private fun generateProdCode() : String{
        return database.push().key.toString()
    }

    fun dismissListener(){
    }
}

