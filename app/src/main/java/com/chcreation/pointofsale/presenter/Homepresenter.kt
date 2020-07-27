package com.chcreation.pointofsale.presenter

import android.content.Context
import android.util.Log
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Homepresenter(private val view: MainView, private val auth: FirebaseAuth, private val database: DatabaseReference, private val  context: Context){

    var postListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onDataChange(p0: DataSnapshot) {
        }

    }

    fun retrieveProducts(){
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
            .orderByChild(EProduct.NAME.toString())
            .addListenerForSingleValueEvent(postListener)

    }

    fun retrieveCategories(){
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
    }

    fun dismissListener(){
    }
}

