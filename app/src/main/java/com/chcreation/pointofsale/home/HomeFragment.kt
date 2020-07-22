package com.chcreation.pointofsale.home

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.view.MainView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onTouch
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.toast

class HomeFragment : Fragment() , MainView {

    private lateinit var adapter: HomeRecyclerViewAdapter
    private var productItems : ArrayList<Product> = arrayListOf()
    private var tempProductItems : ArrayList<Product> = arrayListOf()
    private lateinit var presenter: Homepresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var sharedPreference: SharedPreferences
    private val clickAnimation = AlphaAnimation(1.2F,0.6F)
    private var categoryItems: ArrayList<String> = arrayListOf()
    private var merchant = ""
    private var currentCat = 0
    private var searchFilter = ""
    private var cartItems: ArrayList<Product> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        sharedPreference =  ctx.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)
        presenter = Homepresenter(this,mAuth,mDatabase)

        merchant = sharedPreference.getString("merchant","").toString()

        adapter = HomeRecyclerViewAdapter(
            ctx,
            tempProductItems
        ) {
            cartItems.add(it)
            btnHomeAddItem.text = "${cartItems.size} Item"
        }

        rvHome.layoutManager = LinearLayoutManager(ctx)
        rvHome.adapter = adapter

        tlHome.tabMode = TabLayout.MODE_SCROLLABLE

        tlHome.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentCat = tab!!.position

                fetchProductByCat()
            }

        })

        svHomeSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                searchFilter = newText
                fetchProductByCat()
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

        })

        srHome.onRefresh {
            fetchProductByCat()
        }

        presenter.retrieveProducts(merchant)
        presenter.retrieveCategories(merchant)

    }

    override fun onStart() {
        super.onStart()

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
        adapter.notifyDataSetChanged()

        if (searchFilter != ""){
            for ((index, data) in productItems.withIndex()) {
                if (data.NAME.toString().contains(searchFilter) || data.CAT.toString().contains(searchFilter))
                    tempProductItems.add(productItems[index])
            }
        }else{
            for ((index, data) in productItems.withIndex()) {
                if (currentCat == 0 || data.CAT.toString() == categoryItems[currentCat])
                    tempProductItems.add(productItems[index])
            }
        }

        adapter.notifyDataSetChanged()

        srHome.isRefreshing = false
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_PROD_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                productItems.clear()
                tempProductItems.clear()
                adapter.notifyDataSetChanged()

                for (data in dataSnapshot.children) {
                    val item = data.getValue(Product::class.java)!!
                    tempProductItems.add(item)
                }
                productItems.addAll(tempProductItems)

                adapter.notifyDataSetChanged()
                srHome.isRefreshing = false
            }
            else
                srHome.isRefreshing = false
        }
        else if (response == EMessageResult.FETCH_CATEGORY_SUCCESS.toString()){
            categoryItems.clear()
            tlHome.addTab(tlHome.newTab().setText("All"),true)
            categoryItems.add("All")
            if (dataSnapshot.exists()){
                for (data in dataSnapshot.children) {
                    tlHome.addTab(tlHome.newTab().setText(data.key))
                    categoryItems.add(data.key.toString())
                }
            }
        }

    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
