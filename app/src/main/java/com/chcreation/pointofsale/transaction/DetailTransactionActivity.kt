package com.chcreation.pointofsale.transaction

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.checkout.CartRecyclerViewAdapter
import com.chcreation.pointofsale.checkout.CheckOutActivity
import com.chcreation.pointofsale.getMerchant
import com.chcreation.pointofsale.home.HomeFragment
import com.chcreation.pointofsale.indonesiaCurrencyFormat
import com.chcreation.pointofsale.model.Cart
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.receiptFormat
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transCodeItems
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transItems
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transPosition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_detail_transaction.*
import kotlinx.android.synthetic.main.activity_receipt.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.util.*

class DetailTransactionActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference

    companion object{
        var existPayment = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_transaction)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        btnDetailTransactionCancel.onClick {
            alert ("Are You Sure Want to Cancel?"){
                title = "Cancel Transaction"
                yesButton {

                }
                noButton {

                }
            }.show()
        }

        btnDetailTransactionConfirmPayment.onClick {
            existPayment = true
            startActivity<CheckOutActivity>()
            finish()
        }

        val adapter = TabAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        vpDetailTransaction.adapter = adapter

        tlDetailTransaction.setupWithViewPager(vpDetailTransaction)
    }

    override fun onStart() {
        super.onStart()

        val discount = transItems[transPosition].DISCOUNT
        val note = transItems[transPosition].NOTE

        if (transItems[transPosition].TOTAL_OUTSTANDING == 0){
            btnDetailTransactionConfirmPayment.visibility = View.GONE
            ivDetailTransactionStatus.imageResource = R.drawable.success
        }
        else{
            ivDetailTransactionStatus.imageResource = R.drawable.pending
            btnDetailTransactionReceipt.visibility = View.GONE
        }

        tvDetailTransactionDate.text = transItems[transPosition].CREATED_DATE.toString()
        tvDetailTransactionCode.text = receiptFormat(transCodeItems[transPosition].toInt())

        tvDetailTransactionTotalPrice.text = indonesiaCurrencyFormat().format(transItems[transPosition].TOTAL_PRICE).toString()
    }

    class TabAdapter(fm: FragmentManager, behavior: Int) : FragmentStatePagerAdapter(fm, behavior) {
        private val tabName : Array<String> = arrayOf("List Products", "Payment")

        override fun getItem(position: Int): Fragment = when (position) {
            0 -> {
                DetailTransactionListProductFragment()
            }
            else -> DetailTransactionListPayment()
        }

        override fun getCount(): Int = tabName.size
        override fun getPageTitle(position: Int): CharSequence? = tabName[position]
    }

}