package com.chcreation.pointofsale.presenter

import com.chcreation.pointofsale.EMerchant
import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.EProduct
import com.chcreation.pointofsale.ETable
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

        if (product.PROD_CODE.equals(""))
            product.PROD_CODE = generateProdCode()

        val values  = hashMapOf(
            EProduct.NAME.toString() to product.NAME,
            EProduct.COST.toString() to product.COST,
            EProduct.DESC.toString() to product.DESC,
            EProduct.PRICE.toString() to product.PRICE,
            EProduct.PROD_CODE.toString() to product.PROD_CODE,
            EProduct.UOM_CODE.toString() to product.UOM_CODE,
            EProduct.STOCK.toString() to product.STOCK,
            EProduct.IMAGE.toString() to product.IMAGE
        )

        database.child(ETable.PRODUCT.toString())
            .child(auth.currentUser!!.uid)
            .child(merchant)
            .child(generateProdCode())
            .setValue(values).addOnFailureListener {
                view.response(it.message.toString())
            }
            .addOnSuccessListener {
                view.response(EMessageResult.SUCCESS.toString())
            }
    }

    fun saveNewCategory(merchant: String, newCategory: String){
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

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
    }

    private fun generateProdCode() : String{
        return database.push().key.toString()
    }

    fun dismissListener(){
    }
}

