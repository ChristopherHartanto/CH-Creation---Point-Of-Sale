package com.chcreation.pointofsale.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.HomeRecyclerViewAdapter
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
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.onRefresh

class HomeFragment : Fragment() , MainView {

    private lateinit var adapter: HomeRecyclerViewAdapter
    private var productItems : MutableList<Product> = mutableListOf()
    private lateinit var presenter: Homepresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var sharedPreference: SharedPreferences
    private var merchant = ""

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

        adapter = HomeRecyclerViewAdapter(ctx,productItems){

        }

        tlHome.tabGravity = TabLayout.GRAVITY_FILL

        rvHome.layoutManager = LinearLayoutManager(ctx)
        rvHome.adapter = adapter

        presenter.retrieveProducts(merchant)

        tlHome.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
            }

        })
        srHome.onRefresh {
            presenter.retrieveProducts(merchant)
        }

        presenter.retrieveCategories(merchant)
    }
    override fun onStart() {
        super.onStart()

    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_PROD_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                productItems.clear()
                for (data in dataSnapshot.children) {
                    val item = data.getValue(Product::class.java)!!
                    productItems.add(item)
                }
                adapter.notifyDataSetChanged()
                srHome.isRefreshing = false
            }
            else
                srHome.isRefreshing = false
        }
        else if (response == EMessageResult.FETCH_CATEGORY_SUCCESS.toString()){
            tlHome.addTab(tlHome.newTab().setText("All"),true)
            if (dataSnapshot.exists()){
                for (data in dataSnapshot.children) {
                    tlHome.addTab(tlHome.newTab().setText(data.key))
                }
            }
        }

    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
