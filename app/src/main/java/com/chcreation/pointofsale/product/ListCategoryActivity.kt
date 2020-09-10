package com.chcreation.pointofsale.product

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.Cat
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.presenter.ProductPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_list_category.*
import kotlinx.android.synthetic.main.fragment_manage_product_update_product.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.onRefresh
import java.util.*

class ListCategoryActivity : AppCompatActivity(), MainView {

    private lateinit var rvAdapter : CategoryRecyclerViewAdapter
    private lateinit var presenter: ProductPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private var categoryItems: MutableList<Cat> = mutableListOf()
    private var categoryNames: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_category)

        supportActionBar?.title = "Category"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = ProductPresenter(this,mAuth,mDatabase,this)
        rvAdapter = CategoryRecyclerViewAdapter(this,categoryNames){
            alert ("Delete ${categoryNames[it]}?"){
                title = "Delete Category"
                yesButton {a->
                    categoryItems[it].STATUS_CODE = EStatusCode.DELETE.toString()
                    categoryItems[it].UPDATED_BY = mAuth.currentUser!!.uid
                    categoryItems[it].UPDATED_DATE = dateFormat().format(Date())

                    val gson = Gson()
                    val categoryItem = gson.toJson(categoryItems)
                    presenter.saveNewCategory(categoryItem)
                }

                noButton {  }
            }.show()
        }

        rvCategory.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(this@ListCategoryActivity)
        }

        fbCategory.onClick {
            fbCategory.startAnimation(normalClickAnimation())
            startActivity<NewCategory>()
            finish()
        }

        srCategory.onRefresh {
            presenter.retrieveCategories()
        }
    }

    override fun onStart() {
        super.onStart()

        loading()
        presenter.retrieveCategories()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun loading(){
        srCategory.isRefreshing = true
    }

    private fun endLoading(){
        srCategory.isRefreshing = false
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_CATEGORY_SUCCESS.toString()){
            if (dataSnapshot.exists() && dataSnapshot.value != ""){
                categoryItems.clear()
                categoryNames.clear()

                val gson = Gson()
                val arrayCartType = object : TypeToken<MutableList<Cat>>() {}.type
                val items : MutableList<Cat> = gson.fromJson(dataSnapshot.value.toString(),arrayCartType)

                for (data in items) {
                    val item = data.CAT
                    if (item != ""){
                        if (data.STATUS_CODE == EStatusCode.ACTIVE.toString()){
                            categoryItems.add(data)
                            categoryNames.add(item.toString())
                        }
                    }
                }
                rvAdapter.notifyDataSetChanged()
            }
            endLoading()
        }
    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString())
            toast("Delete Success")
        else
            toast(message)
        loading()
        presenter.retrieveCategories()
    }
}
