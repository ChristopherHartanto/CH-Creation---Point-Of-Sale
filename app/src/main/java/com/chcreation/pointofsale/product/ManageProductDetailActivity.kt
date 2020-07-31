package com.chcreation.pointofsale.product

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.chcreation.pointofsale.EProduct
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.product.ManageProductUpdateProductFragment.Companion.bitmap
import com.chcreation.pointofsale.product.ManageProductUpdateProductFragment.Companion.currentPhotoPath
import com.chcreation.pointofsale.product.ManageProductUpdateProductFragment.Companion.filePath
import com.chcreation.pointofsale.transaction.DetailTransactionActivity
import com.chcreation.pointofsale.transaction.DetailTransactionListPayment
import com.chcreation.pointofsale.transaction.DetailTransactionListProductFragment
import kotlinx.android.synthetic.main.activity_detail_transaction.*
import kotlinx.android.synthetic.main.activity_manage_product_detail.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton

class ManageProductDetailActivity : AppCompatActivity() {

    companion object{
        var prodCode = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_product_detail)

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
}
