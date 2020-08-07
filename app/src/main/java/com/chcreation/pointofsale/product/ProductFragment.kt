package com.chcreation.pointofsale.product

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.chcreation.pointofsale.EMessageResult

import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.getMerchant
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.presenter.ProductPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_product.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.*

/**
 * A simple [Fragment] subclass.
 */
class ProductFragment : Fragment(), MainView {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var rvAdapter: ProductRecyclerViewAdapter
    private lateinit var presenter: ProductPresenter
    private lateinit var handle: Handler
    private lateinit var runnable: Runnable
    private var items: MutableList<Product> = mutableListOf()
    private var productItems: MutableList<Product> = mutableListOf()
    private var categoryTotalItems: MutableList<Int> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product, container, false)
    }

    override fun onStart() {
        super.onStart()

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = ProductPresenter(this,mAuth,mDatabase,ctx)
        handle = Handler()

        rvAdapter = ProductRecyclerViewAdapter(ctx,productItems,categoryTotalItems){
            startActivity(intentFor<ListProductActivity>("category" to productItems[it].CAT))
        }

        rvProductCat.apply {
            adapter = rvAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }

        runnable = Runnable {
            pbProduct.visibility = View.GONE
            srProduct.isRefreshing = false
            toast("No Data")
        }
        handle.postDelayed(runnable,5000)

        srProduct.onRefresh {
            handle.postDelayed(runnable,5000)
            presenter.retrieveProducts()
        }
        presenter.retrieveProducts()
    }

    override fun onPause() {
        super.onPause()

        handle.removeCallbacks(runnable)
    }

    fun fetchData(){
        productItems.clear()
        categoryTotalItems.clear()
        var count = 1

        val itemGroup = items.groupBy { it.CAT }
        Log.d("group by:", itemGroup.toString())

        for (data in itemGroup){
            val item = data.value
            categoryTotalItems.add(item.size)
            productItems.add(item[0])
        }

//        for ((index,item) in items.withIndex()){
//            var check = false
//
//            for ((index,data) in productItems.withIndex()){
//                if (data.CAT == item.CAT)
//                    check = true
//            }
//            if (!check){
//                productItems.add(item)
//            }
//
//            if (index > 0 && items[index].CAT != items[index-1].CAT){
//                categoryTotalItems.add(count)
//                count = 1
//            }
//            else if (index == items.size-1 && items[index].CAT == items[index-1].CAT){
//                categoryTotalItems.add(count+1)
//            }
//            else if (index > 0)
//                count++
//
//        }

        rvAdapter.notifyDataSetChanged()
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_PROD_SUCCESS.toString()) {
            items.clear()

            if (dataSnapshot.exists()) {
                for (data in dataSnapshot.children) {
                    items.add(data.getValue(Product::class.java)!!)
                }
                fetchData()
            }
            handle.removeCallbacks(runnable)
            pbProduct.visibility = View.GONE
            srProduct.isRefreshing = false
        }


    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
