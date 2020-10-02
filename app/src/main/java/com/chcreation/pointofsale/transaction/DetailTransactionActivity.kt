package com.chcreation.pointofsale.transaction

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.fragment.app.FragmentStatePagerAdapter
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.checkout.CheckOutActivity
import com.chcreation.pointofsale.checkout.ReceiptActivity
import com.chcreation.pointofsale.model.ActivityLogs
import com.chcreation.pointofsale.presenter.TransactionPresenter
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transCodeItems
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transItems
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transPosition
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_detail_transaction.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.util.*

class DetailTransactionActivity : AppCompatActivity(), MainView {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter : TransactionPresenter

    companion object{
        var existPayment = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_transaction)

        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = TransactionPresenter(this,mAuth,mDatabase,this)

        btnDetailTransactionCancel.onClick {
            btnDetailTransactionCancel.startAnimation(normalClickAnimation())
            alert ("Are You Sure Want to Cancel?"){
                title = "Cancel Transaction"
                yesButton {
                    pbDetailTransaction.visibility = View.VISIBLE
                    layoutDetailTransaction.alpha = 0.3F

                    presenter.cancelTransaction(transCodeItems[transPosition])

                    presenter.saveActivityLogs(ActivityLogs("Cancel Transaction ${receiptFormat(transCodeItems[transPosition])}",mAuth.currentUser!!.uid,dateFormat().format(Date())))
                }
                noButton {

                }
            }.show()
        }

        btnDetailTransactionConfirmPayment.onClick {
            btnDetailTransactionConfirmPayment.startAnimation(normalClickAnimation())

            existPayment = true
            startActivity<CheckOutActivity>()
            finish()
        }

        btnDetailTransactionReceipt.onClick {
            btnDetailTransactionReceipt.startAnimation(normalClickAnimation())

            startActivity(intentFor<ReceiptActivity>("existReceipt" to true))
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

        if (transItems[transPosition].TOTAL_OUTSTANDING == 0F &&
            transItems[transPosition].STATUS_CODE != EStatusCode.CANCEL.toString()){
            tvDetailTransactionStatus.text = "Status : Done"
            btnDetailTransactionConfirmPayment.visibility = View.GONE
            ivDetailTransactionStatus.imageResource = R.drawable.success
        }else if (transItems[transPosition].TOTAL_OUTSTANDING!! > 0 &&
            transItems[transPosition].STATUS_CODE != EStatusCode.CANCEL.toString()){
            tvDetailTransactionStatus.text = "Pending : ${currencyFormat(getLanguage(this),
                getCountry(this)).format(transItems[transPosition].TOTAL_OUTSTANDING)}"
            ivDetailTransactionStatus.imageResource = R.drawable.pending
            //btnDetailTransactionReceipt.visibility = View.GONE
        }else if (transItems[transPosition].STATUS_CODE == EStatusCode.CANCEL.toString()){
            tvDetailTransactionStatus.text = "Status : Cancel"
            btnDetailTransactionConfirmPayment.visibility = View.GONE
            btnDetailTransactionCancel.visibility = View.GONE
            ivDetailTransactionStatus.imageResource = R.drawable.error
        }

        tvDetailTransactionDate.text = parseDateFormatFull(transItems[transPosition].CREATED_DATE.toString())
        tvDetailTransactionCode.text = receiptFormat(transCodeItems[transPosition].toInt())
        tvDetailTransactionDiscount.text = currencyFormat(getLanguage(this),
            getCountry(this)).format(transItems[transPosition].DISCOUNT)
        tvDetailTransactionTax.text = currencyFormat(getLanguage(this),
            getCountry(this)).format(transItems[transPosition].TAX)

        if (transItems[transPosition].TAX == 0F && transItems[transPosition].DISCOUNT == 0F)
            layoutDetailTransactionSubTotal.visibility = View.GONE
        else
            tvDetailTransactionSubTotal.text = currencyFormat(getLanguage(this),
                getCountry(this)).format(transItems[transPosition].TOTAL_PRICE)

        tvDetailTransactionTotalPrice.text = currencyFormat(getLanguage(this),
            getCountry(this)).format(transItems[transPosition].TOTAL_PRICE!! +
                transItems[transPosition].TAX!! - transItems[transPosition].DISCOUNT!!
        ).toString()
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

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString()){
            toast("Success Cancel Transaction")
            finish()
        }else
            toast(message)

        pbDetailTransaction.visibility = View.GONE
        layoutDetailTransaction.alpha = 1F

    }

}