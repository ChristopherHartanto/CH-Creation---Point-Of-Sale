package com.chcreation.pointofsale.presenter

import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.EProduct
import com.chcreation.pointofsale.model.Product
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

class ProductPresenter(private val view: MainView,
                       private val auth: FirebaseAuth,
                       private val database: DatabaseReference){

    fun saveProduct(product: Product){

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

        database.child("product")
            .child(auth.currentUser!!.uid)
            .child(product.PROD_CODE.toString())
            .setValue(values).addOnFailureListener {
                view.response(it.message.toString())
            }
            .addOnSuccessListener {
                view.response(EMessageResult.SUCCESS.toString())
            }
    }

    private fun generateProdCode() : String{
        return database.push().key.toString()
    }

    fun dismissListener(){
    }
}

