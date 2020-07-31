package com.chcreation.pointofsale.product

import com.chcreation.pointofsale.home.HomeRecyclerViewAdapter

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.checkout.CartActivity
import com.chcreation.pointofsale.model.Cart
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.view.MainView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_manage_product.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.*

class ManageProductFragment : Fragment() , MainView {

    private lateinit var adapter: HomeRecyclerViewAdapter
    private var productItems : ArrayList<Product> = arrayListOf()
    private lateinit var presenter: Homepresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var sharedPreference: SharedPreferences
    private var categoryItems: ArrayList<String> = arrayListOf()
    private var productKeys: ArrayList<Int> = arrayListOf()
    private var tmpProductKeys: ArrayList<Int> = arrayListOf()
    private var tempProductItems : ArrayList<Product> = arrayListOf()
    private var currentCat = 0
    private var searchFilter = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_manage_product, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = Homepresenter(this,mAuth,mDatabase,ctx)

        adapter = HomeRecyclerViewAdapter(
            ctx,
            tempProductItems
        ) {
            try {
                startActivity(intentFor<ManageProductDetailActivity>(EProduct.PROD_CODE.toString() to tempProductItems[it].PROD_CODE))

            }catch (e: Exception){
                showError(ctx,e.message.toString())
            }
        }

        rvManageProduct.layoutManager = LinearLayoutManager(ctx)
        rvManageProduct.adapter = adapter

        tlManageProduct.tabMode = TabLayout.MODE_FIXED

        tlManageProduct.addTab(tlManageProduct.newTab().setText("All"),true)
        tlManageProduct.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentCat = tab!!.position

                fetchProductByCat()
            }

        })

        svManageProduct.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                searchFilter = newText
                fetchProductByCat()
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

        })

        srManageProduct.onRefresh {
            presenter.retrieveProducts()
        }

        pbManageProduct.visibility = View.VISIBLE


    }

    override fun onStart() {
        super.onStart()

        if (tempProductItems.size == 0)
            presenter.retrieveProducts()
        presenter.retrieveCategories()

        currentCat = 0
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putStringArrayList("categoryItems",categoryItems)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState != null) {
            savedInstanceState.getStringArray("categoryItems")?.let { categoryItems.addAll(it) }
        }
    }

    fun fetchProductByCat(){
        tempProductItems.clear()
        tmpProductKeys.clear()
        if (searchFilter != ""){
            for ((index, data) in productItems.withIndex()) {
                if (data.NAME.toString().toLowerCase().contains(searchFilter.toLowerCase()) || data.CAT.toString().contains(searchFilter)){
                    tempProductItems.add(productItems[index])
                    tmpProductKeys.add(productKeys[index])
                }
            }
        }else{
            for ((index, data) in productItems.withIndex()) {
                if (currentCat == 0 || data.CAT.toString() == categoryItems[currentCat]){
                    tempProductItems.add(productItems[index])
                    tmpProductKeys.add(productKeys[index])
                }
            }
        }

        adapter.notifyDataSetChanged()

        srManageProduct.isRefreshing = false
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (context != null){
            if (response == EMessageResult.FETCH_PROD_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    productKeys.clear()
                    productItems.clear()
                    tempProductItems.clear()

                    for (data in dataSnapshot.children) {
                        val item = data.getValue(Product::class.java)!!
                        tempProductItems.add(item)
                        productKeys.add(data.key!!.toInt())
                    }
                    productItems.addAll(tempProductItems)
                    tmpProductKeys.addAll(productKeys)

                    adapter.notifyDataSetChanged()
                    if (context != null)
                        srManageProduct.isRefreshing = false

                    fetchProductByCat()
                }
                else{
                    if (context != null)
                        srManageProduct.isRefreshing = false
                }
            }
            else if (response == EMessageResult.FETCH_CATEGORY_SUCCESS.toString()){
                categoryItems.clear()
                categoryItems.add("All")
                if (dataSnapshot.exists()){
                    for (data in dataSnapshot.children) {
                        tlManageProduct.addTab(tlManageProduct.newTab().setText(data.key))
                        categoryItems.add(data.key.toString())
                    }
                    if (categoryItems.size > 4)
                        tlManageProduct.tabMode = TabLayout.MODE_SCROLLABLE
                }
                svManageProduct.visibility = View.VISIBLE
                pbManageProduct.visibility = View.GONE
                srManageProduct.visibility = View.VISIBLE
            }
        }

    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
