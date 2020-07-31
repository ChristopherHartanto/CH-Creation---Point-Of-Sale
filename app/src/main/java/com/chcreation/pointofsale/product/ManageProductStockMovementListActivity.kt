package com.chcreation.pointofsale.product

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.EStatusCode
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.model.StockMovement
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.presenter.ProductPresenter
import com.chcreation.pointofsale.product.ManageProductDetailActivity.Companion.prodCode
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_manage_product_stock_movement_list.*

class ManageProductStockMovementListActivity : AppCompatActivity(), MainView {

    private lateinit var presenter: ProductPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var adapter: StockMovementListRecyclerView
    private var stockMovementItems: MutableList<StockMovement> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_product_stock_movement_list)

        supportActionBar?.title = "Stock Movement"

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = ProductPresenter(this,mAuth,mDatabase,this)

        adapter = StockMovementListRecyclerView(this,stockMovementItems){

        }

        rvManageProductStockMovementList.adapter = adapter
        rvManageProductStockMovementList.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()

        presenter.retrieveStockMovement(prodCode)
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_STOCK_MOVEMENT_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                stockMovementItems.clear()
                for (data in dataSnapshot.children){
                    val item = data.getValue(StockMovement::class.java)
                    if (item!!.STATUS_CODE != EStatusCode.CANCEL.toString())
                        stockMovementItems.add(item)
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun response(message: String) {
    }
}
