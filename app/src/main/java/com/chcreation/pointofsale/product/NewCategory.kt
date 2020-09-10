package com.chcreation.pointofsale.product

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.Cart
import com.chcreation.pointofsale.model.Cat
import com.chcreation.pointofsale.presenter.ProductPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_new_category.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import java.util.*

class NewCategory : AppCompatActivity(), MainView {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter : ProductPresenter
    private lateinit var sharedPreference: SharedPreferences
    private var categoryItems: MutableList<Cat> = mutableListOf()
    private var merchant = ""

    companion object{
        var newCategory = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_category)

        supportActionBar?.title = "Set Up Category"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = ProductPresenter(this,mAuth,mDatabase,this)
        sharedPreference =  this.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

        merchant = getMerchant(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()

        presenter.retrieveCategories()

        btnNewCategory.onClick {
            btnNewCategory.startAnimation(normalClickAnimation())
            newCategory = etNewCategory.text.toString()

            if (newCategory.isEmpty())
                etNewCategory.error = "Please Fill the Field"
            else{
                val check = categoryItems.any {
                    it.CAT!!.toLowerCase(Locale.getDefault()) == newCategory.toLowerCase(Locale.getDefault())
                            && it.STATUS_CODE == EStatusCode.ACTIVE.toString()
                }

                if (check)
                    toast("This Category Already Exist")
                else{
                    categoryItems.add(Cat(newCategory, dateFormat().format(Date()),dateFormat().format(Date()),
                        mAuth.currentUser!!.uid,EStatusCode.ACTIVE.toString()))

                    val gson = Gson()
                    val categoryItem = gson.toJson(categoryItems)
                    presenter.saveNewCategory(categoryItem)
                }
            }
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_CATEGORY_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                val gson = Gson()
                val arrayCartType = object : TypeToken<MutableList<Cat>>() {}.type
                categoryItems = gson.fromJson(dataSnapshot.value.toString(),arrayCartType)
            }
        }
    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString()){
            finish()
        }
        else{
            newCategory = ""
            toast(message)
        }
    }
}
