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
import com.chcreation.pointofsale.model.Cart
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
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.yesButton

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

    companion object{
        var cartItems: ArrayList<Cart> = arrayListOf()
        var totalQty = 0
        var totalPrice = 0F
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
        presenter = Homepresenter(this,mAuth,mDatabase)

        merchant = sharedPreference.getString("merchant","").toString()

        adapter = HomeRecyclerViewAdapter(
            ctx,
            tempProductItems
        ) {
            addCart(it)
            totalQty = countQty()
            totalPrice = sumPrice()
            btnHomeAddItem.text = "$totalQty Item = Rp ${totalPrice},00"
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

        btnHomeAddItem.onClick {
            if (cartItems.size == 0)
                alert("Add some Items to Your Cart First.") {
                    title = "Empty Cart"

                    yesButton {  }
                }.show()
            else
                startActivity<CartActivity>()
        }

        srHome.onRefresh {
            fetchProductByCat()
        }

        presenter.retrieveProducts(merchant)
        presenter.retrieveCategories(merchant)

    }

    override fun onStart() {
        super.onStart()

        btnHomeAddItem.text = "$totalQty Item = Rp ${totalPrice},00"
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

    private fun sumPrice() : Float{
        var total = 0F

        for (data in cartItems){
            total += (data.PRICE!! * data.Qty!!)
        }
        return total
    }

    private fun addCart(product: Product){
        if (cartItems.size == 0)
            cartItems.add(Cart(product.NAME,product.PRICE,1))
        else{
            var check = false
            for ((i , data) in cartItems.withIndex()){
                if (data.NAME.equals(product.NAME)){
                    val lastQty = data.Qty
                    cartItems[i].Qty = lastQty!! + 1

                    check = true
                    break
                }

            }
            if (!check)
                cartItems.add(Cart(product.NAME,product.PRICE,1))
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
