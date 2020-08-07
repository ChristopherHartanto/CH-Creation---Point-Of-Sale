package com.chcreation.pointofsale.product

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.checkout.CheckOutActivity
import com.chcreation.pointofsale.checkout.SelectCustomerActivity
import com.chcreation.pointofsale.presenter.ProductPresenter
import com.chcreation.pointofsale.product.ManageProductUpdateProductFragment.Companion.bitmap
import com.chcreation.pointofsale.product.ManageProductUpdateProductFragment.Companion.currentPhotoPath
import com.chcreation.pointofsale.product.ManageProductUpdateProductFragment.Companion.filePath
import com.chcreation.pointofsale.transaction.DetailTransactionActivity
import com.chcreation.pointofsale.transaction.DetailTransactionListPayment
import com.chcreation.pointofsale.transaction.DetailTransactionListProductFragment
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_detail_transaction.*
import kotlinx.android.synthetic.main.activity_manage_product_detail.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx

class ManageProductDetailActivity : AppCompatActivity(),MainView {

    companion object{
        var prodCode = ""
    }
    private lateinit var presenter: ProductPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_product_detail)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = ProductPresenter(this,mAuth,mDatabase,this)

        supportActionBar?.title = "Product Detail"
        prodCode = intent.extras!!.getString(EProduct.PROD_CODE.toString(),"")
    }

    override fun onDestroy() {
        super.onDestroy()

        prodCode = ""
    }

    override fun onStart() {
        super.onStart()

        val adapter = TabAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )
        vpManageProductDetail.adapter = adapter

        tlManageProductDetail.setupWithViewPager(vpManageProductDetail)
    }

    override fun onBackPressed() {
        if(filePath != null || bitmap != null || currentPhotoPath != "")
            alert ("Are you want to Discard Edit?"){
                title = "Discard"
                yesButton {
                    pbManageProductDetail.visibility = View.VISIBLE
                    vpManageProductDetail.visibility = View.GONE
                    ManageProductUpdateProductFragment().clearData()
                    super.onBackPressed()
                }
                noButton {  }
            }.show()
        else{
            ManageProductUpdateProductFragment().clearData()
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_delete -> {
                if (getMerchantUserGroup(this) == EUserGroup.WAITER.toString())
                    toast("Only Manager Can Delete")
                else{
                    alert ("Are Sure Want to Delete?"){
                        title = "Delete"
                        yesButton {
                            presenter.deleteProduct(prodCode)
                        }
                        noButton {

                        }
                    }.show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class TabAdapter(fm: FragmentManager, behavior: Int) : FragmentStatePagerAdapter(fm, behavior) {
        private val tabName : Array<String> = arrayOf("Product", "Stock")

        override fun getItem(position: Int): Fragment = when (position) {
            0 -> {
                ManageProductUpdateProductFragment()
            }
            else -> ManageProductUpdateStockFragment()
        }

        override fun getCount(): Int = tabName.size
        override fun getPageTitle(position: Int): CharSequence? = tabName[position]
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {

    }

    override fun response(message: String) {
        if (message == EMessageResult.DELETE_SUCCESS.toString()){
            pbManageProductDetail.visibility = View.GONE
            vpManageProductDetail.visibility = View.VISIBLE

            toast("Delete Success")
            finish()
        }
    }
}
