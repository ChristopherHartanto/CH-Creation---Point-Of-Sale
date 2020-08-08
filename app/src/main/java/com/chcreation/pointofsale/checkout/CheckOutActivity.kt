package com.chcreation.pointofsale.checkout

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.checkout.DiscountActivity.Companion.discount
import com.chcreation.pointofsale.checkout.DiscountActivity.Companion.tax
import com.chcreation.pointofsale.checkout.NoteActivity.Companion.note
import com.chcreation.pointofsale.checkout.SelectCustomerActivity.Companion.selectCustomerCode
import com.chcreation.pointofsale.checkout.SelectCustomerActivity.Companion.selectCustomerName
import com.chcreation.pointofsale.customer.CustomerFragment
import com.chcreation.pointofsale.home.HomeFragment.Companion.cartItems
import com.chcreation.pointofsale.home.HomeFragment.Companion.totalPrice
import com.chcreation.pointofsale.home.HomeFragment.Companion.totalQty
import com.chcreation.pointofsale.model.Payment
import com.chcreation.pointofsale.model.Transaction
import com.chcreation.pointofsale.presenter.CheckOutPresenter
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.transaction.DetailTransactionActivity.Companion.existPayment
import com.chcreation.pointofsale.transaction.TransactionFragment
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.customerItems
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transCodeItems
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transItems
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transPosition
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_check_out.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import java.util.*

class CheckOutActivity : AppCompatActivity(), MainView {

    companion object{
        var isCustomer = 0 // 0 havent select, 1 on select, 2 selected
        var totalReceived = 0
        var transDate = ""
        var transCode = 0
        var totalOutStanding = 0
        var postTotalPayment = 0
    }
    private var paymentMethod = ""
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: CheckOutPresenter
    private var totalPayment = 0
    private var existCheckOutNote = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = CheckOutPresenter(this,mAuth,mDatabase,this)

        btnCheckOutCard.onClick {
            btnCheckOutCard.startAnimation(normalClickAnimation())

            paymentMethod = EPaymentMethod.CARD.toString()

            btnCheckOutCard.backgroundResource = R.drawable.button_border_fill
            btnCheckOutCard.textColorResource = R.color.colorWhite
            btnCheckOutCash.backgroundResource = R.drawable.button_border
            btnCheckOutCard.textColorResource = R.color.colorBlack
        }

        btnCheckOutCash.onClick {
            btnCheckOutCash.startAnimation(normalClickAnimation())

            paymentMethod = EPaymentMethod.CASH.toString()

            btnCheckOutCard.backgroundResource = R.drawable.button_border
            btnCheckOutCash.backgroundResource = R.drawable.button_border_fill
            btnCheckOutCash.textColorResource = R.color.colorWhite
            btnCheckOutCash.textColorResource = R.color.colorBlack
        }

        etCheckOutAmountReceived.onClick {
            etCheckOutAmountReceived.setText("")
        }

        if (existPayment)
            existCheckOutSetUp()
        else
            newCheckOutSetUp()

    }

    override fun onStart() {
        super.onStart()

        btnCheckOut.onClick {
            btnCheckOut.startAnimation(normalClickAnimation())

            if (!existPayment)
                newCheckOut()
            else
                existCheckOut()
        }

        if (!existPayment){
            newCheckOutSetUp()
        }
    }

    override fun onDestroy() {
        existPayment = false

        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!existPayment){
            val inflater: MenuInflater = menuInflater
            inflater.inflate(R.menu.menu_check_out, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_select_customer -> {
                if (!existPayment){
                    isCustomer = 1
                    startActivity<SelectCustomerActivity>()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun newCheckOutSetUp(){
        if (selectCustomerName != "")
            supportActionBar?.title = selectCustomerName
        else
            supportActionBar?.title = "No Customer"

        totalPayment = totalPrice - discount + tax
        etCheckOutAmountReceived.setText(totalPayment.toString())
        tvCheckOutTotal.text = indonesiaCurrencyFormat().format(totalPayment)
    }

    private fun newCheckOut(){
        if (paymentMethod == "")
            toast("Please Select Payment Method !")
        else{

            totalReceived = etCheckOutAmountReceived.text.toString().toInt()

            if (totalPayment - totalReceived > 0)
                totalOutStanding = totalPayment - totalReceived

            val gson = Gson()
            val orderDetail = gson.toJson(cartItems)
            var statusCode = if (totalOutStanding > 0) EStatusCode.PENDING else EStatusCode.DONE

            alert ("Continue to Check Out?"){
                title = "Confirmation"

                yesButton {

                    if (totalOutStanding > 0 && selectCustomerName == ""){
                        toast("Must be Completed Payment if Without Customer")
                    }else{
                        pbCheckOut.visibility = View.VISIBLE
                        tvCheckOutProcessTitle.visibility = View.VISIBLE
                        layoutCheckOutContent.alpha = 0.3F

                        presenter.saveTransaction(Transaction(totalPrice,totalOutStanding,
                            discount,tax,paymentMethod,orderDetail,selectCustomerCode, note,"",
                            statusCode.toString(), dateFormat().format(Date()), dateFormat().format(Date()),
                            mAuth.currentUser!!.uid,mAuth.currentUser!!.uid)
                            , Payment("", totalReceived,paymentMethod, note,
                                mAuth.currentUser!!.uid,EStatusCode.DONE.toString()), cartItems)
                    }
                }

                noButton {
                }
            }.show()
        }
    }

    private fun existCheckOutSetUp(){

        supportActionBar?.title = customerItems[transPosition]

        presenter.retrievePendingPayment(transCodeItems[transPosition])
    }

    private fun existCheckOut(){
        if (paymentMethod == "")
            toast("Please Select Payment Method !")
        else{

            totalReceived = etCheckOutAmountReceived.text.toString().toInt()

            totalOutStanding = postTotalPayment - totalReceived
            if (totalOutStanding < 0)
                totalOutStanding = 0

            val gson = Gson()
            val orderDetail = gson.toJson(cartItems)

            if (!existCheckOutNote && note == ""){
                alert ("Need Additional Note?"){
                    title = "Note"

                    yesButton {
                        startActivity<NoteActivity>()
                        existCheckOutNote = true
                        return@yesButton
                    }
                    noButton {
                        pbCheckOut.visibility = View.VISIBLE
                        tvCheckOutProcessTitle.visibility = View.VISIBLE
                        layoutCheckOutContent.alpha = 0.3F

                        presenter.savePendingPayment(transCodeItems[transPosition],
                            Payment("", totalReceived,paymentMethod, note, mAuth.currentUser?.uid,EStatusCode.DONE.toString()),
                            totalOutStanding,transItems[transPosition])
                    }
                }.show()
            }
            if (existCheckOutNote){
                alert ("Continue to Check Out?"){
                    title = "Confirmation"


                    yesButton {
                        pbCheckOut.visibility = View.VISIBLE
                        tvCheckOutProcessTitle.visibility = View.VISIBLE
                        layoutCheckOutContent.alpha = 0.3F

                        presenter.savePendingPayment(transCodeItems[transPosition],
                            Payment("", totalReceived,paymentMethod, note, mAuth.currentUser?.uid,EStatusCode.DONE.toString()),
                            totalOutStanding,transItems[transPosition])

                    }

                    noButton {
                    }
                }.show()
            }

        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_PEND_PAYMENT_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                val item = dataSnapshot.getValue(Transaction::class.java)
                if (item != null) {
                    postTotalPayment = item.TOTAL_OUTSTANDING!!.toInt()
                    etCheckOutAmountReceived.setText(postTotalPayment.toString())
                    tvCheckOutTotal.text = indonesiaCurrencyFormat().format(postTotalPayment)
                }
            }
        }
    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString()){
            startActivity<PostCheckOutActivity>()
            finish()
        }
        else{
            toast(message)
        }

        pbCheckOut.visibility = View.GONE
        tvCheckOutProcessTitle.visibility = View.GONE
        layoutCheckOutContent.alpha = 1F
    }
}
