package com.chcreation.pointofsale.home

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.checkout.CartActivity
import com.chcreation.pointofsale.model.Cart
import com.chcreation.pointofsale.model.Cat
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.product.NewProductActivity
import com.chcreation.pointofsale.view.MainView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.Result
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.*
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() , MainView,ZXingScannerView.ResultHandler {

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
    private var isEnabled = true
    private var isScanning = false
    private var CAMERA_PERMISSION  = 101
    private var sortBy = ESort.PROD_NAME.toString()
    private lateinit var mScannerView : ZXingScannerView

    companion object{
        var tempProductItems : ArrayList<Product> = arrayListOf()
        var cartItems: ArrayList<Cart> = arrayListOf()
        var imageItems: ArrayList<String> = arrayListOf()
        var totalQty = 0
        var totalPrice = 0

        var active = false // to end application
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
        mScannerView = ZXingScannerView(ctx)

        adapter = HomeRecyclerViewAdapter(
            ctx,
            tempProductItems
        ) {
            try {
                if(!isScanning){
                    addCart(it)
                    totalQty = countQty()
                    totalPrice = sumPrice()

                    //cartAnimation(tempProductItems[it].IMAGE.toString())

                    if (tempProductItems[it].MANAGE_STOCK)
                        tempProductItems[it] = Product(tempProductItems[it].NAME,tempProductItems[it].PRICE,tempProductItems[it].DESC,tempProductItems[it].COST, tempProductItems[it].MANAGE_STOCK,
                            tempProductItems[it].STOCK!! - 1,tempProductItems[it].IMAGE,tempProductItems[it].PROD_CODE,tempProductItems[it].UOM_CODE,tempProductItems[it].CAT,
                            tempProductItems[it].CODE,tempProductItems[it].STATUS_CODE,tempProductItems[it].CREATED_DATE,tempProductItems[it].UPDATED_DATE,
                            tempProductItems[it].CREATED_BY,tempProductItems[it].UPDATED_BY)

                    adapter.notifyDataSetChanged()

                    btnHomeAddItem.text = "$totalQty Item = ${currencyFormat(getLanguage(ctx),
                        getCountry(ctx)).format(totalPrice)}"
                    btnHomeAddItem.startAnimation(normalClickAnimation())
                    btnHomeAddItem.backgroundResource = R.drawable.button_border_fill
                    btnHomeAddItem.textColorResource = R.color.colorWhite
                }
            }catch (e: Exception){
                showError(ctx,e.message.toString())
            }
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
                if (categoryItems.size > 0)
                    fetchProductByCat()
            }

        })

        svHomeSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                searchFilter = newText
                if (categoryItems.size > 0)
                    fetchProductByCat()
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

        })

        btnHomeAddItem.onClick {
            if (!isScanning){
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
        }

        srHome.onRefresh {
            GlobalScope.launch {
                presenter.retrieveProducts()
            }
        }

        pbHome.visibility = View.VISIBLE

        ivHomeSort.onClick{
            ivHomeSort.startAnimation(normalClickAnimation())

            selector("Sort by", arrayListOf("Product Name","Product Code","Product Price")){dialogInterface, i ->
                when(i){
                    0->{
                        sortBy = ESort.PROD_NAME.toString()
                    }
                    1->{
                        sortBy = ESort.PROD_CODE.toString()
                    }
                    2->{
                        sortBy = ESort.PROD_PRICE.toString()
                    }
                }
                fetchProductByCat()
            }
        }

        tvHomeAddProd.onClick {
            tvHomeAddProd.startAnimation(normalClickAnimation())

            startActivity<NewProductActivity>()
        }

        ivHomeScan.onClick { cs->
            ivHomeScan.startAnimation(normalClickAnimation())

            if (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(android.Manifest.permission.CAMERA),CAMERA_PERMISSION
                )
            else
                openScanBarcode()
        }

        btnHomeScanCancel.onClick {
            btnHomeScanCancel.startAnimation(normalClickAnimation())
            cancelScan()
        }
    }

    override fun onStart() {
        super.onStart()
        isEnabled = true
        loading()
        GlobalScope.launch {
//            if (tempProductItems.size == 0 || productItems.size == 0)
//                presenter.retrieveProducts()
//            else{
//                tvHomeAddProd.visibility = View.GONE
//                rvHome.visibility = View.VISIBLE
//            }
            presenter.retrieveProducts()
            presenter.retrieveCategories()

        }

        btnHomeAddItem.text = "$totalQty Item = ${currencyFormat(getLanguage(ctx),
            getCountry(ctx)).format(totalPrice)}"

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

    override fun onResume() {
        super.onResume()

        active = true
    }

    override fun onPause() {
        super.onPause()
        presenter.dismissListener()
        isEnabled = false
        active = false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openScanBarcode()
            }
            else
                toast("Permission Denied")
        }
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

    private fun openScanBarcode(){
        cancelScan()
        isScanning = true
        layoutHomeScan.visibility = View.VISIBLE
        mScannerView.setAutoFocus(true)
        mScannerView.setResultHandler(this@HomeFragment)
        layoutHomeScanContent.addView(mScannerView)
        mScannerView.startCamera()
    }

    private fun cancelScan(){
        mScannerView.stopCamera();
        mScannerView.removeAllViewsInLayout()
        layoutHomeScanContent.removeAllViews()
        layoutHomeScan.visibility = View.GONE
        isScanning = false
    }

    private fun cartAnimation(image: String){
        val location = IntArray(2)
        homeLayout.getLocationOnScreen(location)

//        val layoutParams = FrameLayout.LayoutParams(ivHomeCartAnimation.width, ivHomeCartAnimation.height)
//        layoutParams.setMargins(0,0,10,location[1])
//        layoutParams.gravity = Gravity.BOTTOM
//        ivHomeCartAnimation.layoutParams = layoutParams

//        ivHomeCartAnimation.visibility = View.VISIBLE
//        if (image != "")
//            Glide.with(ctx).load(image).into(ivHomeCartAnimation)
//        else if (image == "")
//            ivHomeCartAnimation.backgroundResource = R.drawable.default_image
//
//        slideDown(ivHomeCartAnimation)
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
        if (cartItems.size == 0){
            imageItems.add(tempProductItems[position].IMAGE.toString())
            cartItems.add(Cart(tempProductItems[position].NAME, tmpProductKeys[position],tempProductItems[position].PROD_CODE,
                tempProductItems[position].MANAGE_STOCK,tempProductItems[position].PRICE,1))
        }
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
            if (!check){
                imageItems.add(tempProductItems[position].IMAGE.toString())
                cartItems.add(Cart(tempProductItems[position].NAME, tmpProductKeys[position],tempProductItems[position].PROD_CODE,
                    tempProductItems[position].MANAGE_STOCK,tempProductItems[position].PRICE,1))
            }
        }
    }

    fun fetchProductByCat(){
        pbHome.visibility = View.VISIBLE
        tempProductItems.clear()
        tmpProductKeys.clear()

        when (sortBy) {
            ESort.PROD_PRICE.toString() -> productItems.sortWith(compareBy {it.PRICE})
            ESort.PROD_CODE.toString() -> productItems.sortWith(compareBy {it.CODE})
            else -> productItems.sortWith(compareBy {it.NAME})
        }

        if (searchFilter != ""){
            for ((index, data) in productItems.withIndex()) {
                if (data.NAME.toString().toLowerCase(Locale.getDefault()).contains(searchFilter.toLowerCase(Locale.getDefault()))
                    //|| data.CAT.toString().contains(searchFilter)
                    && (data.CAT.toString() == categoryItems[currentCat]
                    || currentCat == 0)
                ){
                    tempProductItems.add(productItems[index])
                    tmpProductKeys.add(productKeys[index])
                }
                else if (data.CODE.toString().toLowerCase(Locale.getDefault()).contains(searchFilter.toLowerCase(Locale.getDefault()))
                    //|| data.CAT.toString().contains(searchFilter)
                    && (data.CAT.toString() == categoryItems[currentCat]
                            || currentCat == 0)
                ){
                    tempProductItems.add(productItems[index])
                    tmpProductKeys.add(productKeys[index])
                }
                else if (data.PRICE.toString().toLowerCase(Locale.getDefault()).contains(searchFilter.toLowerCase(Locale.getDefault()))
                    //|| data.CAT.toString().contains(searchFilter)
                    && (data.CAT.toString() == categoryItems[currentCat]
                            || currentCat == 0)
                ){
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
        pbHome.visibility = View.GONE
        srHome.isRefreshing = false
    }

    private fun loading(){
        svHomeSearch.visibility = View.INVISIBLE
        pbHome.visibility = View.VISIBLE
    }

    private fun endLoading(){
        svHomeSearch.visibility = View.VISIBLE
        pbHome.visibility = View.GONE
        srHome.isRefreshing = false
        srHome.visibility = View.VISIBLE
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (context != null && isEnabled && isVisible && isResumed){
            if (response == EMessageResult.FETCH_PROD_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    tvHomeAddProd.visibility = View.GONE
                    rvHome.visibility = View.VISIBLE
                    productKeys.clear()
                    productItems.clear()
                    tempProductItems.clear()

                    for (data in dataSnapshot.children) {
                        val item = data.getValue(Product::class.java)!!

                        if (item.STATUS_CODE == EStatusCode.ACTIVE.toString()){
                            tempProductItems.add(item)
                            productKeys.add(data.key!!.toInt())
                        }
                    }
                    productItems.addAll(tempProductItems)
                    tmpProductKeys.addAll(productKeys)

                    adapter.notifyDataSetChanged()
                    if (categoryItems.size > 0)
                        fetchProductByCat()
                }else{
                    tvHomeAddProd.visibility = View.VISIBLE
                    rvHome.visibility = View.GONE
                }
                endLoading()
            }
            else if (response == EMessageResult.FETCH_CATEGORY_SUCCESS.toString()){
                categoryItems.clear()
                tlHome.removeAllTabs()
                tlHome.addTab(tlHome.newTab().setText("All"))
                categoryItems.add("All")
                if (dataSnapshot.exists()  && dataSnapshot.value != ""){
                    val gson = Gson()
                    val arrayCartType = object : TypeToken<MutableList<Cat>>() {}.type
                    val items : MutableList<Cat> = gson.fromJson(dataSnapshot.value.toString(),arrayCartType)

                    for (data in items) {
                        if (data.STATUS_CODE == EStatusCode.ACTIVE.toString()){
                            tlHome.addTab(tlHome.newTab().setText(data.CAT))
                            categoryItems.add(data.CAT.toString())
                        }
                    }
                    if (categoryItems.size > 4)
                        tlHome.tabMode = TabLayout.MODE_SCROLLABLE
                }
            }
        }

    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleResult(p0: Result?) {
        if (p0 != null) {
            svHomeSearch.setQuery(p0.text,false)
            fetchProductByCat()
        }
        cancelScan()
    }

}
