package com.chcreation.pointofsale.product

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.EStatusCode
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.getMerchant
import com.chcreation.pointofsale.home.HomeRecyclerViewAdapter
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_list_product.*
import kotlinx.android.synthetic.main.activity_new_product.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange
import org.jetbrains.anko.toast

class ListProductActivity : AppCompatActivity(), MainView {

    private lateinit var adapter: ProductListRecyclerViewAdapter
    private var productItems : MutableList<Product> = mutableListOf()
    private var tempProductItems : MutableList<Product> = mutableListOf()
    private var tempProductImageItems : MutableList<String> = mutableListOf()
    private lateinit var presenter: Homepresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private var categoryItems: MutableList<String> = mutableListOf()
    private var currentCat = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_product)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = Homepresenter(this,mAuth,mDatabase,this)

        currentCat = intent.extras!!.getString("category","")

        supportActionBar?.title = currentCat
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = ProductListRecyclerViewAdapter(
            this,
            tempProductImageItems
        ) {
            startActivity(intentFor<ProductDetailActivity>("prodCode" to tempProductItems[it].PROD_CODE))
        }

        rvListProduct.layoutManager = GridLayoutManager(this,2)
        rvListProduct.adapter = adapter
    }

    override fun onStart() {
        super.onStart()

        GlobalScope.launch {
            presenter.retrieveCategories()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun fetchProductByCat(){
        tempProductItems.clear()
        for ((index, data) in productItems.withIndex()) {
            if (data.CAT.toString() == currentCat){
                tempProductImageItems.add(productItems[index].IMAGE.toString())
                tempProductItems.add(productItems[index])
            }

        }
        adapter.notifyDataSetChanged()
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_PROD_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                tempProductImageItems.clear()
                tempProductItems.clear()
                productItems.clear()
                adapter.notifyDataSetChanged()

                for (data in dataSnapshot.children) {
                    val item = data.getValue(Product::class.java)!!

                    if (item.STATUS_CODE == EStatusCode.ACTIVE.toString())
                        tempProductItems.add(item)
                }
                productItems.addAll(tempProductItems)

                fetchProductByCat()
            }
            pbListProduct.visibility = View.GONE
        }
        else if (response == EMessageResult.FETCH_CATEGORY_SUCCESS.toString()){
            categoryItems.clear()
            categoryItems.add("All")
            if (dataSnapshot.exists()){
                for (data in dataSnapshot.children) {
                    categoryItems.add(data.key.toString())
                }
                GlobalScope.launch {
                    presenter.retrieveProducts()
                }
            }
        }
    }

    override fun response(message: String) {
        toast(message)
    }
}
