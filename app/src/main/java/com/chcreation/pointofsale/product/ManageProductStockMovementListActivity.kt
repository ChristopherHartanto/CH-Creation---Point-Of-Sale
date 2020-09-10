package com.chcreation.pointofsale.product

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.EStatusCode
import com.chcreation.pointofsale.MainActivity.Companion.userList
import com.chcreation.pointofsale.MainActivity.Companion.userListName
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.model.StockMovement
import com.chcreation.pointofsale.model.User
import com.chcreation.pointofsale.model.UserList
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.presenter.ProductPresenter
import com.chcreation.pointofsale.product.ManageProductDetailActivity.Companion.prodCode
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_manage_product_stock_movement_list.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.yesButton

class ManageProductStockMovementListActivity : AppCompatActivity(), MainView {

    private lateinit var presenter: ProductPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var adapter: StockMovementListRecyclerView
    private var stockMovementItems: MutableList<StockMovement> = mutableListOf()
    private var nameItems: MutableList<String> = mutableListOf()
//    private var userNames: MutableList<String> = mutableListOf() // user list
//    private var userList : MutableList<UserList> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_product_stock_movement_list)

        supportActionBar?.title = "Stock Movement"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = ProductPresenter(this,mAuth,mDatabase,this)

        adapter = StockMovementListRecyclerView(this,stockMovementItems){
            stockMovementItems[it].UPDATED_BY?.let { it1 -> presenter.getUserName(it1){name ->
                alert ("PIC: $name"){
                    title = "Info"
                    yesButton {  }
                }.show()
            } }
        }

        rvManageProductStockMovementList.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        rvManageProductStockMovementList.layoutManager = linearLayoutManager
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()

        GlobalScope.launch{
            //presenter.retrieveUserLists()

            presenter.retrieveStockMovement(prodCode)
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_STOCK_MOVEMENT_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                stockMovementItems.clear()
                nameItems.clear()

                for (data in dataSnapshot.children){
                    val item = data.getValue(StockMovement::class.java)
                    //if (item!!.STATUS_CODE != EStatusCode.CANCEL.toString())
                    if (item != null) {
                        stockMovementItems.add(item)

//                        if (item.UPDATED_BY.toString() == "")
//                            nameItems.add("")
//                        else{
//                            for ((index,check) in userList.withIndex()){
//                                if (check.USER_CODE == item.UPDATED_BY){
//                                    nameItems.add(userListName[index])
//                                    break
//                                }
//                            }
//                        }
                    }
                }
                adapter.notifyDataSetChanged()

            }
        }
//        if (response == EMessageResult.FETCH_USER_LIST_SUCCESS.toString()){
//            if(dataSnapshot.exists() && dataSnapshot.value != null && dataSnapshot.value != ""){
//                userNames.clear()
//                val gson = Gson()
//                val arrayUserListType = object : TypeToken<MutableList<UserList>>() {}.type
//                userList = gson.fromJson(dataSnapshot.value.toString(),arrayUserListType)
//
//                userList.sortBy { it.USER_GROUP }
//
//                GlobalScope.launch {
//                    for (data in userList){
//                        presenter.getUserName(data.USER_CODE.toString()){
//                            userNames.add(it)
//                        }
//                    }
//                }
//            }
//        }
    }

    override fun response(message: String) {
    }
}
