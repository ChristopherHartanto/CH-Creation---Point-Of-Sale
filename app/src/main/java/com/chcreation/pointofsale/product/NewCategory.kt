package com.chcreation.pointofsale.product

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.ETable
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.getMerchant
import com.chcreation.pointofsale.presenter.ProductPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_new_category.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast

class NewCategory : AppCompatActivity(), MainView {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter : ProductPresenter
    private lateinit var sharedPreference: SharedPreferences
    private var merchant = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_category)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = ProductPresenter(this,mAuth,mDatabase)
        sharedPreference =  this.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

        merchant = getMerchant(this)
    }

    override fun onStart() {
        super.onStart()

        btnNewCategory.onClick {
            val newCategory = etNewCategory.text.toString()

            if (newCategory.isEmpty())
                etNewCategory.error = "Please Fill the Field"
            else
                presenter.saveNewCategory(merchant,newCategory)
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {

    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString())
            finish()
        else
            toast(message)
    }
}
