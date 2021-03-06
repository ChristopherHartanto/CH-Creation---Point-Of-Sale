package com.chcreation.pointofsale.product

import com.chcreation.pointofsale.home.HomeRecyclerViewAdapter

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.checkout.CartActivity
import com.chcreation.pointofsale.model.Cart
import com.chcreation.pointofsale.model.Cat
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.product.ManageProductDetailActivity.Companion.prodName
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
import kotlinx.android.synthetic.main.fragment_manage_product.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.*
import java.util.*
import kotlin.collections.ArrayList

class ManageProductFragment : Fragment() , MainView, ZXingScannerView.ResultHandler  {

    private lateinit var adapter: HomeRecyclerViewAdapter
    private var productItems : ArrayList<Product> = arrayListOf()
    private lateinit var presenter: Homepresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var sharedPreference: SharedPreferences
    private var categoryItems: ArrayList<String> = arrayListOf()
    private var productKeys: ArrayList<Int> = arrayListOf()
    private var productCodes: ArrayList<String> = arrayListOf()
    private var tempProductItems : ArrayList<Product> = arrayListOf()
    private var currentCat = 0
    private var searchFilter = ""
    private var isSlideUp = true
    private var isSlideDown = true
    private var isScanning = false
    private var CAMERA_PERMISSION  = 101
    private var sortBy = ESort.PROD_NAME.toString()
    private lateinit var mScannerView : ZXingScannerView

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
        mScannerView = ZXingScannerView(ctx)
        sharedPreference =  ctx.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

        adapter = HomeRecyclerViewAdapter(
            ctx,tempProductItems
        ) {
            try {
                if (!isScanning){
                    prodName = tempProductItems[it].NAME.toString()
                    startActivity(intentFor<ManageProductDetailActivity>(EProduct.PROD_CODE.toString() to tempProductItems[it].PROD_CODE))
                }
            }catch (e: Exception){
                showError(ctx,e.message.toString())
            }
        }

        rvConfig()

        rvManageProduct.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0){
                    if (isSlideDown){
                        slideDown(fbManageProduct)
                        isSlideDown = false
                    }
                    isSlideUp = true
                }else{
                    if (isSlideUp)
                        slideUp(fbManageProduct)
                    isSlideUp = false
                    isSlideDown = true
                }
            }

        })

        tlManageProduct.tabMode = TabLayout.MODE_FIXED

        tlManageProduct.addTab(tlManageProduct.newTab().setText("All"),true)
        tlManageProduct.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
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

        svManageProduct.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

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

        fbManageProduct.onClick {
            startActivity<NewProductActivity>()
        }

        srManageProduct.onRefresh {
            GlobalScope.launch {
                presenter.retrieveProducts()
            }
        }

        pbManageProduct.visibility = View.VISIBLE

        ivSort.onClick{
            ivSort.startAnimation(normalClickAnimation())

            selector("Sort by", arrayListOf("Name","Product Code","Price","Newest")){dialogInterface, i ->
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
                    3->{
                        sortBy = ESort.NEWEST.toString()
                    }
                }
                fetchProductByCat()
            }
        }

        ivManageProductScan.onClick { cs->
            ivManageProductScan.startAnimation(normalClickAnimation())

            if (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(android.Manifest.permission.CAMERA),CAMERA_PERMISSION
                )
            else
                openScanBarcode()
        }

        btnManageProductCancel.onClick {
            btnManageProductCancel.startAnimation(normalClickAnimation())
            cancelScan()
        }

        ivManageProductView.onClick {
            ivManageProductView.startAnimation(normalClickAnimation())
            val editor = sharedPreference.edit()
            if (getProductView(ctx) == EProductView.LIST.toString()){
                editor.putString(ESharedPreference.PRODUCT_VIEW.toString(),EProductView.GRID.toString())
            }
            else if (getProductView(ctx) == EProductView.GRID.toString()){
                editor.putString(ESharedPreference.PRODUCT_VIEW.toString(),EProductView.LIST.toString())
            }
            editor.apply()
            rvConfig()
        }
    }

    override fun onStart() {
        super.onStart()

        pbManageProduct.visibility = View.VISIBLE

        GlobalScope.launch {
//            if (tempProductItems.size == 0)
//                presenter.retrieveProducts()
            presenter.retrieveProducts()
            presenter.retrieveCategories()
        }

        currentCat = 0
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

    private fun rvConfig(){
        if (getProductView(ctx) == EProductView.LIST.toString()){
            ivManageProductView.imageResource = R.drawable.ic_view_module_black_24dp
            rvManageProduct.layoutManager = LinearLayoutManager(ctx)
            rvManageProduct.adapter = adapter
        }else{
            ivManageProductView.imageResource = R.drawable.ic_format_list_bulleted_black_24dp
            rvManageProduct.layoutManager = GridLayoutManager(ctx,3)
            rvManageProduct.adapter = adapter
        }
    }

    private fun openScanBarcode(){
        cancelScan()
        isScanning = true
        layoutManageProductScan.visibility = View.VISIBLE
        mScannerView.setAutoFocus(true)
        mScannerView.setResultHandler(this@ManageProductFragment)
        layoutManageProductScanContent.addView(mScannerView)
        mScannerView.startCamera()
    }

    private fun cancelScan(){
        mScannerView.stopCamera();
        mScannerView.removeAllViewsInLayout()
        layoutManageProductScanContent.removeAllViews()
        layoutManageProductScan.visibility = View.GONE
        isScanning = false
    }

    fun fetchProductByCat(){
        pbManageProduct.visibility = View.VISIBLE
        tempProductItems.clear()
        when (sortBy) {
            ESort.PROD_PRICE.toString() -> productItems.sortWith(compareBy {it.PRICE})
            ESort.PROD_CODE.toString() -> productItems.sortWith(compareBy {it.CODE})
            ESort.NEWEST.toString() -> {
                productItems.sortWith(compareBy {it.CREATED_DATE})
                productItems.reverse()
            }
            else -> productItems.sortWith(compareBy {it.NAME})
        }

        if (searchFilter != ""){
            for ((index, data) in productItems.withIndex()) {
                if (data.NAME.toString().toLowerCase(Locale.getDefault()).contains(searchFilter.toLowerCase(Locale.getDefault()))
                    && (data.CAT.toString() == categoryItems[currentCat]
                    || currentCat == 0)
                ){
                    tempProductItems.add(productItems[index])
                }
                else if (data.PRICE.toString().toLowerCase(Locale.getDefault()).contains(searchFilter.toLowerCase(Locale.getDefault()))
                    && (data.CAT.toString() == categoryItems[currentCat]
                            || currentCat == 0)
                ){
                    tempProductItems.add(productItems[index])
                }
                else if (data.CODE.toString().toLowerCase(Locale.getDefault()).contains(searchFilter.toLowerCase(Locale.getDefault()))
                    && (data.CAT.toString() == categoryItems[currentCat]
                            || currentCat == 0)
                ){
                    tempProductItems.add(productItems[index])
                }
            }
        }else{
            for ((index, data) in productItems.withIndex()) {
                if (currentCat == 0 || data.CAT.toString() == categoryItems[currentCat]){
                    tempProductItems.add(productItems[index])
                }
            }
        }

        adapter.notifyDataSetChanged()

        pbManageProduct.visibility = View.GONE
        srManageProduct.isRefreshing = false
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (context != null  && isVisible && isResumed){
            if (response == EMessageResult.FETCH_PROD_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    productKeys.clear()
                    productItems.clear()
                    tempProductItems.clear()

                    for (data in dataSnapshot.children) {
                        val item = data.getValue(Product::class.java)!!

                        if (item.STATUS_CODE == EStatusCode.ACTIVE.toString()){
                            tempProductItems.add(item)
                            productCodes.add(item.PROD_CODE.toString())
                            productKeys.add(data.key!!.toInt())
                        }
                    }
                    productItems.addAll(tempProductItems)

                    adapter.notifyDataSetChanged()
                    if (context != null)
                        srManageProduct.isRefreshing = false
                    if (categoryItems.size > 0)
                        fetchProductByCat()
                }
                else{
                    if (context != null)
                        srManageProduct.isRefreshing = false
                }
            }
            else if (response == EMessageResult.FETCH_CATEGORY_SUCCESS.toString()){
                categoryItems.clear()
                tlManageProduct.removeAllTabs()
                tlManageProduct.addTab(tlManageProduct.newTab().setText("All"))
                categoryItems.add("All")
                if (dataSnapshot.exists() && dataSnapshot.value != ""){
                    val gson = Gson()
                    val arrayCartType = object : TypeToken<MutableList<Cat>>() {}.type
                    val items : MutableList<Cat> = gson.fromJson(dataSnapshot.value.toString(),arrayCartType)

                    for (data in items) {
                        if (data.STATUS_CODE == EStatusCode.ACTIVE.toString()) {
                            tlManageProduct.addTab(tlManageProduct.newTab().setText(data.CAT))
                            categoryItems.add(data.CAT.toString())
                        }
                    }

                    if (categoryItems.size > 4)
                        tlManageProduct.tabMode = TabLayout.MODE_SCROLLABLE
                }
                svManageProduct.visibility = View.VISIBLE
                pbManageProduct.visibility = View.GONE
            }
        }

    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleResult(p0: Result?) {
        if (p0 != null) {
            svManageProduct.setQuery(p0.text,false)
            fetchProductByCat()
        }
        cancelScan()
    }
}
