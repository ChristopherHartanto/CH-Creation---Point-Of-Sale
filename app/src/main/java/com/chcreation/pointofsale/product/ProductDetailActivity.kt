package com.chcreation.pointofsale.product

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.getMerchant
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.presenter.ProductPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_product_detail.*
import org.jetbrains.anko.toast

class ProductDetailActivity : AppCompatActivity(), MainView {

    private lateinit var presenter: ProductPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var product: Product
    private var prodCode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)


        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = ProductPresenter(this,mAuth,mDatabase,this)

        prodCode = intent.extras!!.getString("prodCode","")
    }

    private fun fetchData(){
        if (product.IMAGE.toString() != "")
            Picasso.get().load(product.IMAGE.toString()).into(ivProductDetailImage)

        tvProductDetailName.text = product.NAME.toString()
        tvProductDetailDesc.text = product.DESC.toString()
    }

    override fun onStart() {
        super.onStart()

        presenter.retrieveProducts(this)
    }
    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_PROD_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                for (data in dataSnapshot.children){
                    val item = data.getValue(Product::class.java)

                    if (item!!.PROD_CODE.toString() == prodCode){
                        product = item
                        fetchData()
                        return
                    }
                }
            }
        }
    }

    override fun response(message: String) {
        toast(message)
    }
}
