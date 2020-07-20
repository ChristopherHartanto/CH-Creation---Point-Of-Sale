package com.chcreation.pointofsale.product

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.getMerchant
import com.chcreation.pointofsale.home.HomeRecyclerViewAdapter
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.view.MainView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_list_product.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.support.v4.ctx

class ListProductActivity : AppCompatActivity(), MainView {

    private lateinit var adapter: HomeRecyclerViewAdapter
    private var productItems : MutableList<Product> = mutableListOf()
    private var tempProductItems : MutableList<Product> = mutableListOf()
    private lateinit var presenter: Homepresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private var categoryItems: MutableList<String> = mutableListOf()
    private var currentCat = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_product)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = Homepresenter(this,mAuth,mDatabase)

        currentCat = intent.extras!!.getInt("category",0)

        adapter = HomeRecyclerViewAdapter(
            this,
            tempProductItems
        ) {
            startActivity(intentFor<ProductDetailActivity>("prodCode" to it.PROD_CODE))
        }
        rvListProduct.layoutManager = LinearLayoutManager(this)
        rvListProduct.adapter = adapter
    }

    override fun onStart() {
        super.onStart()

        presenter.retrieveCategories(getMerchant(this))
    }

    private fun fetchProductByCat(){
        tempProductItems.clear()
        for ((index, data) in productItems.withIndex()) {
            if (currentCat == 0 || data.CAT.toString() == categoryItems[currentCat])
                tempProductItems.add(productItems[index])

        }
        adapter.notifyDataSetChanged()
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_PROD_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                tempProductItems.clear()
                adapter.notifyDataSetChanged()

                for (data in dataSnapshot.children) {
                    val item = data.getValue(Product::class.java)!!
                    tempProductItems.add(item)
                }
                productItems.addAll(tempProductItems)

                if (currentCat == 0)
                    adapter.notifyDataSetChanged()
                else
                    fetchProductByCat()
            }
        }
        else if (response == EMessageResult.FETCH_CATEGORY_SUCCESS.toString()){
            categoryItems.clear()
            categoryItems.add("All")
            if (dataSnapshot.exists()){
                for (data in dataSnapshot.children) {
                    categoryItems.add(data.key.toString())
                }

                presenter.retrieveProducts(getMerchant(this))
            }
        }
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
