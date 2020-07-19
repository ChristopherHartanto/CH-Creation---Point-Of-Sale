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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Homepresenter(private val view: MainView, private val auth: FirebaseAuth, private val database: DatabaseReference){

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
        database.child("users")
            .child(auth.currentUser!!.uid)
            .child("product")
            .orderByKey()
            .addListenerForSingleValueEvent(postListener)

    }

    fun dismissListener(){
    }
}

