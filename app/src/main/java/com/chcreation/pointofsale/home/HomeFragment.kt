package com.chcreation.pointofsale.home

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
import com.chcreation.pointofsale.checkout.CartActivity
import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.indonesiaCurrencyFormat
import com.chcreation.pointofsale.model.Cart
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.normalClickAnimation
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.view.MainView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.*

class HomeFragment : Fragment() , MainView {

    private lateinit var adapter: HomeRecyclerViewAdapter
    private var productItems : ArrayList<Product> = arrayListOf()
    private lateinit var presenter: Homepresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var sharedPreference: SharedPreferences
    private var categoryItems: ArrayList<String> = arrayListOf()
    private var productKeys: ArrayList<Int> = arrayListOf()
    private var tmpProductKeys: ArrayList<Int> = arrayListOf()
    private var currentCat = 0
    private var searchFilter = ""

    companion object{
        var tempProductItems : ArrayList<Product> = arrayListOf()
        var cartItems: ArrayList<Cart> = arrayListOf()
        var totalQty = 0
        var totalPrice = 0
    }

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
        presenter = Homepresenter(this,mAuth,mDatabase,ctx)

        adapter = HomeRecyclerViewAdapter(
            ctx,
            tempProductItems
        ) {
            addCart(it)
            totalQty = countQty()
            totalPrice = sumPrice()

            if (tempProductItems[it].MANAGE_STOCK)
                tempProductItems[it] = Product(tempProductItems[it].NAME,tempProductItems[it].PRICE,tempProductItems[it].DESC,tempProductItems[it].COST, tempProductItems[it].MANAGE_STOCK,
                    tempProductItems[it].STOCK!! - 1,tempProductItems[it].IMAGE,tempProductItems[it].PROD_CODE,tempProductItems[it].UOM_CODE,tempProductItems[it].CAT,
                    tempProductItems[it].CODE)

            adapter.notifyDataSetChanged()

            btnHomeAddItem.text = "$totalQty Item = ${indonesiaCurrencyFormat().format(totalPrice)}"
            btnHomeAddItem.startAnimation(normalClickAnimation())
            btnHomeAddItem.backgroundResource = R.drawable.button_border_fill
            btnHomeAddItem.textColorResource = R.color.colorWhite
        }

        rvHome.layoutManager = LinearLayoutManager(ctx)
        rvHome.adapter = adapter

        tlHome.tabMode = TabLayout.MODE_FIXED

        tlHome.addTab(tlHome.newTab().setText("All"),true)
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

        btnHomeAddItem.onClick {
            if (cartItems.size == 0)
                alert("Add some Items to Your Cart First.") {
                    title = "Empty Cart"

                    yesButton {  }
                }.show()
            else{
                btnHomeAddItem.startAnimation(normalClickAnimation())
                startActivity<CartActivity>()
            }
        }

        srHome.onRefresh {
            presenter.retrieveProducts()
            fetchProductByCat()
        }

        pbHome.visibility = View.VISIBLE


    }

    override fun onStart() {
        super.onStart()

        if (tempProductItems.size == 0)
            presenter.retrieveProducts()
        presenter.retrieveCategories()

        btnHomeAddItem.text = "$totalQty Item = ${indonesiaCurrencyFormat().format(totalPrice)}"

        if (totalPrice != 0){
            btnHomeAddItem.backgroundResource = R.drawable.button_border_fill
            btnHomeAddItem.textColorResource = R.color.colorWhite
        }
        else{
            btnHomeAddItem.backgroundResource = R.drawable.button_border
            btnHomeAddItem.textColorResource = R.color.colorBlack
        }
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

    private fun countQty() : Int{
        var total = 0
        for (data in cartItems){
            total += data.Qty!!
        }
        return total
    }

    private fun sumPrice() : Int{
        var total = 0

        for (data in cartItems){
            total += (data.PRICE!! * data.Qty!!)
        }
        return total
    }

    private fun addCart(position: Int){
        if (cartItems.size == 0)
            cartItems.add(Cart(tempProductItems[position].NAME, tmpProductKeys[position],tempProductItems[position].PROD_CODE,
                tempProductItems[position].MANAGE_STOCK,tempProductItems[position].PRICE,1))
        else{
            var check = false
            for ((i , data) in cartItems.withIndex()){
                if (data.NAME.equals(tempProductItems[position].NAME)){
                    val lastQty = data.Qty
                    cartItems[i].Qty = lastQty!! + 1

                    check = true
                    break
                }

            }
            if (!check)
                cartItems.add(Cart(tempProductItems[position].NAME, tmpProductKeys[position],tempProductItems[position].PROD_CODE,
                    tempProductItems[position].MANAGE_STOCK,tempProductItems[position].PRICE,1))
        }
    }

    fun fetchProductByCat(){
        tempProductItems.clear()
        tmpProductKeys.clear()
        adapter.notifyDataSetChanged()

        if (searchFilter != ""){
            for ((index, data) in productItems.withIndex()) {
                if (data.NAME.toString().contains(searchFilter) || data.CAT.toString().contains(searchFilter)){
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

        srHome.isRefreshing = false
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (context != null){
            if (response == EMessageResult.FETCH_PROD_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    productKeys.clear()
                    productItems.clear()
                    tempProductItems.clear()
                    adapter.notifyDataSetChanged()

                    for (data in dataSnapshot.children) {
                        val item = data.getValue(Product::class.java)!!
                        tempProductItems.add(item)
                        productKeys.add(data.key!!.toInt())
                    }
                    productItems.addAll(tempProductItems)
                    tmpProductKeys.addAll(productKeys)

                    adapter.notifyDataSetChanged()
                    if (context != null)
                        srHome.isRefreshing = false
                }
                else{
                    if (context != null)
                        srHome.isRefreshing = false
                }
            }
            else if (response == EMessageResult.FETCH_CATEGORY_SUCCESS.toString()){
                categoryItems.clear()
                categoryItems.add("All")
                if (dataSnapshot.exists()){
                    for (data in dataSnapshot.children) {
                        tlHome.addTab(tlHome.newTab().setText(data.key))
                        categoryItems.add(data.key.toString())
                    }
                    if (categoryItems.size > 4)
                        tlHome.tabMode = TabLayout.MODE_SCROLLABLE
                }
                svHomeSearch.visibility = View.VISIBLE
                pbHome.visibility = View.GONE
                srHome.visibility = View.VISIBLE
            }
        }

    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
