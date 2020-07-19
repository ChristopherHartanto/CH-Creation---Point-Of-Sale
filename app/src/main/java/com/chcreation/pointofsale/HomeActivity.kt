package com.chcreation.pointofsale

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.product.ProductActivity
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_home.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

class HomeActivity : AppCompatActivity(), MainView {

    private lateinit var adapter: HomeRecyclerViewAdapter
    private var productItems : MutableList<Product> = mutableListOf()
    private lateinit var presenter: Homepresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        presenter = Homepresenter(this,mAuth,mDatabase)

        adapter = HomeRecyclerViewAdapter(this,productItems)

        rvHome.layoutManager = LinearLayoutManager(this)
        rvHome.adapter = adapter

        presenter.retrieveProducts()

    }

    override fun onStart() {
        super.onStart()

        btnProducts.onClick {
            startActivity<ProductActivity>()
        }
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
            }
        }
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
