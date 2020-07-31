package com.chcreation.pointofsale.product

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.StockMovement
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.presenter.ProductPresenter
import com.chcreation.pointofsale.product.ManageProductDetailActivity.Companion.prodCode
import com.chcreation.pointofsale.product.ManageProductUpdateProductFragment.Companion.product
import com.chcreation.pointofsale.product.ManageProductUpdateProductFragment.Companion.productKey
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_manage_product_manage_stock.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import java.util.*

class ManageProductManageStockActivity : AppCompatActivity(), MainView {

    private lateinit var presenter: ProductPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private var action = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_product_manage_stock)

        supportActionBar?.title = product.NAME.toString()

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = ProductPresenter(this,mAuth,mDatabase,this)

        btnManageStockAddStock.onClick {
            btnManageStockAddStock.startAnimation(normalClickAnimation())
            btnManageStockAddStock.isEnabled = false
            action = 1

            when {
                etManageProductManageStockQty.text.toString() == "" -> toast("Quantity Must be Fill !!")
                etManageProductManageStockQty.text.toString().toInt() <= 0 -> toast("Quantity Must be Greater than Zero !!")
                etManageProductManageStockNote.text.toString() == "" -> showAlert()
            }
            btnManageStockAddStock.isEnabled = true
        }

        btnManageStockMissingStock.onClick {
            btnManageStockMissingStock.startAnimation(normalClickAnimation())
            btnManageStockMissingStock.isEnabled = false

            action = 2
            when {
                etManageProductManageStockQty.text.toString() == "" -> toast("Quantity Must be Fill !!")
                etManageProductManageStockQty.text.toString().toInt() <= 0 -> toast("Quantity Must be Greater than Zero !!")
                etManageProductManageStockNote.text.toString() == "" -> showAlert()
            }
            btnManageStockMissingStock.isEnabled = true
        }
    }

    private fun showAlert(){
        alert ("Skip Adding Additional Note?"){
            title = "Note"
            yesButton {
                save()
            }
            noButton {

            }
        }.show()
    }

    private fun save(){
        pbManageProductManageStock.visibility = View.VISIBLE
//        btnManageStockAddStock.isEnabled = false
//        btnManageStockMissingStock.isEnabled = false

        val qty = etManageProductManageStockQty.text.toString()
        val note = etManageProductManageStockNote.text.toString()

        if (action == 1)
            product.STOCK = product.STOCK!! + qty.toInt()
        else if (action == 2)
            product.STOCK = product.STOCK!! - qty.toInt()

        presenter.saveProduct(product)

        presenter.addStockMovement(StockMovement(qty.toInt(),EStatusStock.INBOUND.toString(),EStatusCode.DONE.toString(),
            prodCode,productKey,0,note,
            dateFormat().format(Date()),dateFormat().format(Date())))
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {

    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString())
        {
            toast("Success Manage Stock")
            finish()
        }
        else
            showError(this,message)
        pbManageProductManageStock.visibility = View.GONE
    }
}
