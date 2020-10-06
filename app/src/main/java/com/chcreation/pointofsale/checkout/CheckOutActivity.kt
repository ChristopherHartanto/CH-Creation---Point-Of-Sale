package com.chcreation.pointofsale.checkout

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
import com.chcreation.pointofsale.home.HomeFragment.Companion.cartItems
import com.chcreation.pointofsale.home.HomeFragment.Companion.totalPrice
import com.chcreation.pointofsale.model.ActivityLogs
import com.chcreation.pointofsale.model.Payment
import com.chcreation.pointofsale.model.Transaction
import com.chcreation.pointofsale.presenter.CheckOutPresenter
import com.chcreation.pointofsale.transaction.DetailTransactionActivity.Companion.existPayment
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.util.*

class CheckOutActivity : AppCompatActivity(), MainView {

    companion object{
        var isCustomer = 0F // 0 havent select, 1 on select, 2 selected
        var totalReceived = 0F
        var transDate = ""
        var transCode = 0
        var totalOutStanding = 0F
        var postTotalPayment = 0F
        var peopleNo = 0F
        var tableNo = ""
        var orderNo = ""
    }
    private var paymentMethod = ""
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: CheckOutPresenter
    private var totalPayment = 0F
    private var custName = "-"
    private var existCheckOutNote = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title = "Check Out"

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = CheckOutPresenter(this,mAuth,mDatabase,this)

        btnCheckOutCard.onClick {
            btnCheckOutCard.startAnimation(normalClickAnimation())

            paymentMethod = EPaymentMethod.CARD.toString()

            btnCheckOutCard.backgroundResource = R.drawable.button_border_fill
            btnCheckOutCard.textColorResource = R.color.colorWhite
            btnCheckOutCash.backgroundResource = R.drawable.button_border
            btnCheckOutCash.textColorResource = R.color.colorBlack
        }

        btnCheckOutCash.onClick {
            btnCheckOutCash.startAnimation(normalClickAnimation())

            paymentMethod = EPaymentMethod.CASH.toString()

            btnCheckOutCard.backgroundResource = R.drawable.button_border
            btnCheckOutCard.textColorResource = R.color.colorBlack
            btnCheckOutCash.backgroundResource = R.drawable.button_border_fill
            btnCheckOutCash.textColorResource = R.color.colorWhite
        }

        etCheckOutAmountReceived.onClick {
            etCheckOutAmountReceived.setText("")
        }

        btnCheckOutAddNote.onClick {
            btnCheckOutAddNote.startAnimation(normalClickAnimation())
            startActivity<NoteActivity>()
        }

        tvCheckOutRemoveCustomer.onClick {
            tvCheckOutRemoveCustomer.startAnimation(normalClickAnimation())
            tvCheckOutRemoveCustomer.visibility = View.GONE

            tvCheckOutCustomer.text = "Select Customer"
            selectCustomerName = ""
            selectCustomerCode = ""
            custName = ""
        }

        tvCheckOutCustomer.onClick {
            tvCheckOutCustomer.startAnimation(normalClickAnimation())
            if (!existPayment){
                isCustomer = 1F
                startActivity<SelectCustomerActivity>()
            }
        }

        if (existPayment){
            btnCheckOutAddNote.visibility = View.VISIBLE
            existCheckOutSetUp()
        }
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

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        if (!existPayment){
//            val inflater: MenuInflater = menuInflater
//            inflater.inflate(R.menu.menu_check_out, menu)
//        }
//        return true
//    }
//
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
//            R.id.action_select_customer -> {
//                if (!existPayment){
//                    isCustomer = 1F
//                    startActivity<SelectCustomerActivity>()
//                }
//                true
//            }
            android.R.id.home->{
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun newCheckOutSetUp(){
        if (selectCustomerName != ""){
            custName = selectCustomerName
            tvCheckOutCustomer.text = selectCustomerName
            tvCheckOutRemoveCustomer.visibility = View.VISIBLE
        }
        else{
            tvCheckOutCustomer.text = "Select Customer"
            tvCheckOutRemoveCustomer.visibility = View.GONE
        }

        totalPayment = totalPrice - discount + tax
        etCheckOutAmountReceived.setText(totalPayment.toString())
        tvCheckOutTotal.text = currencyFormat(getLanguage(this), getCountry(this)).format(totalPayment)
    }

    private fun newCheckOut(){
	    totalOutStanding = 0F

        if (paymentMethod == "")
            toast("Please Select Payment Method !")
        else{
            if (etCheckOutAmountReceived.text.toString() != "")
                totalReceived = etCheckOutAmountReceived.text.toString().toFloat()
            else if (etCheckOutAmountReceived.text.toString() == "")
                totalReceived = 0F

            if (totalPayment - totalReceived > 0)
                totalOutStanding = totalPayment - totalReceived

            val gson = Gson()
            val orderDetail = gson.toJson(cartItems)
            var statusCode = if (totalOutStanding > 0) EStatusCode.PENDING else EStatusCode.DONE

            alert ("Payment Received: ${currencyFormat(getLanguage(this), getCountry(this)).format(totalReceived)}" +
                    "\nCustomer: $custName" +
                    "\nNote: $note"){
                title = "Confirmation"

                yesButton {
                    btnCheckOut.isEnabled = false
                    if (totalOutStanding > 0 && selectCustomerName == ""){
                        toast("Must be Completed Payment if Without Customer")
                    }else{
                        pbCheckOut.visibility = View.VISIBLE
                        tvCheckOutProcessTitle.visibility = View.VISIBLE
                        layoutCheckOutContent.alpha = 0.3F

                        GlobalScope.launch {

                            val receipt = presenter.saveTransaction(Transaction(totalPrice,totalOutStanding,
                                discount,tax,paymentMethod,orderDetail,selectCustomerCode, note,"",
                                statusCode.toString(), dateFormat().format(Date()), dateFormat().format(Date()),
                                mAuth.currentUser!!.uid,mAuth.currentUser!!.uid, peopleNo, tableNo,"","")
                                , Payment("", totalReceived,paymentMethod, note,
                                    mAuth.currentUser!!.uid,EStatusCode.DONE.toString()), cartItems)

                            val log = if (custName != "-") "Receive ${currencyFormat(getLanguage(this@CheckOutActivity),
                                getCountry(this@CheckOutActivity)).format(totalReceived)} from $custName"
                            else
                                "Receive ${currencyFormat(getLanguage(this@CheckOutActivity),
                                    getCountry(this@CheckOutActivity)).format(totalReceived)} from Receipt ${receiptFormat(receipt.toInt())}"
                            presenter.saveActivityLogs(ActivityLogs(log,mAuth.currentUser!!.uid,dateFormat().format(Date())))
                        }

                    }
                }

                noButton {
                }
            }.show()
        }
    }

    private fun existCheckOutSetUp(){
        custName = customerItems[transPosition]

        tvCheckOutCustomer.text = customerItems[transPosition]
        tvCheckOutRemoveCustomer.visibility = View.GONE

        presenter.retrievePendingPayment(transCodeItems[transPosition])
    }

    private fun existCheckOut(){
        if (paymentMethod == "")
            toast("Please Select Payment Method !")
        else{
            if (etCheckOutAmountReceived.text.toString() != "")
                totalReceived = etCheckOutAmountReceived.text.toString().toFloat()
            else if (etCheckOutAmountReceived.text.toString() == "")
                totalReceived = 0F

            totalOutStanding = postTotalPayment - totalReceived
            if (totalOutStanding < 0)
                totalOutStanding = 0F

            val gson = Gson()
            val orderDetail = gson.toJson(cartItems)

            alert ("Payment Received: ${currencyFormat(getLanguage(this@CheckOutActivity),
                getCountry(this@CheckOutActivity)).format(totalReceived)}\nCustomer: $custName"){
                title = "Confirmation"

                yesButton {
                    btnCheckOut.isEnabled = false
                    pbCheckOut.visibility = View.VISIBLE
                    tvCheckOutProcessTitle.visibility = View.VISIBLE
                    layoutCheckOutContent.alpha = 0.3F

                    presenter.savePendingPayment(transCodeItems[transPosition],
                        Payment("", totalReceived,paymentMethod, note, mAuth.currentUser?.uid,EStatusCode.DONE.toString()),
                        totalOutStanding,transItems[transPosition])

                    val log = "Receive ${currencyFormat(getLanguage(this@CheckOutActivity),
                        getCountry(this@CheckOutActivity)).format(totalReceived)} from Receipt ${receiptFormat(transCodeItems[transPosition])}"
                    presenter.saveActivityLogs(ActivityLogs(log,mAuth.currentUser!!.uid,dateFormat().format(Date())))
                }

                noButton {
                }
            }.show()

        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_PEND_PAYMENT_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                val item = dataSnapshot.getValue(Transaction::class.java)
                if (item != null) {
                    postTotalPayment = item.TOTAL_OUTSTANDING!!.toFloat()
                    etCheckOutAmountReceived.setText(postTotalPayment.toString())
                    tvCheckOutTotal.text = currencyFormat(getLanguage(this@CheckOutActivity),
                        getCountry(this@CheckOutActivity)).format(postTotalPayment)
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

        btnCheckOut.isEnabled = true
        pbCheckOut.visibility = View.GONE
        tvCheckOutProcessTitle.visibility = View.GONE
        layoutCheckOutContent.alpha = 1F
    }
}
