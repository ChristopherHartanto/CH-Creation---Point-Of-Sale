package com.chcreation.pointofsale.customer

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
import com.chcreation.pointofsale.ECustomer
import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.EProduct
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.customer.CustomerDetailManageCustomerFragment.Companion.custKey
import com.chcreation.pointofsale.presenter.CustomerPresenter
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
import kotlinx.android.synthetic.main.activity_customer_detail.*
import kotlinx.android.synthetic.main.activity_detail_transaction.*
import kotlinx.android.synthetic.main.activity_manage_product_detail.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton

class CustomerDetailActivity : AppCompatActivity(),MainView {

    companion object{
        var custCode = ""
    }

    private lateinit var presenter: CustomerPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_detail)

        supportActionBar?.title = "Customer Detail"
        custCode = intent.extras!!.getString(ECustomer.CODE.toString(),"")

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = CustomerPresenter(this,mAuth,mDatabase,this)
    }

    override fun onDestroy() {
        super.onDestroy()

        custCode = ""
    }

    override fun onStart() {
        super.onStart()

        val adapter = TabAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )
        vpCustomerDetail.adapter = adapter

        tlCustomerDetail.setupWithViewPager(vpCustomerDetail)
    }

    override fun onBackPressed() {
        if(filePath != null || bitmap != null || currentPhotoPath != "")
            alert ("Are you want to Discard Edit?"){
                title = "Discard"
                yesButton {
                    super.onBackPressed()
                }
                noButton {  }
            }.show()
        else{
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
                alert ("Are Sure Want to Delete?"){
                    title = "Delete"
                    yesButton {
                        presenter.deleteCustomer(custKey)
                    }
                    noButton {

                    }
                }.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class TabAdapter(fm: FragmentManager, behavior: Int) : FragmentStatePagerAdapter(fm, behavior) {
        private val tabName : Array<String> = arrayOf("Profile", "Transaction")

        override fun getItem(position: Int): Fragment = when (position) {
            0 -> {
                CustomerDetailManageCustomerFragment()
            }
            else -> CustomerDetailTransactionFragment()
        }

        override fun getCount(): Int = tabName.size
        override fun getPageTitle(position: Int): CharSequence? = tabName[position]
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
    }

    override fun response(message: String) {
        if (message == EMessageResult.DELETE_SUCCESS.toString()){
            toast("Delete Success")
            finish()
        }
    }
}
