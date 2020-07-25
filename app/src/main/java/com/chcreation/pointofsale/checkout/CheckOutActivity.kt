package com.chcreation.pointofsale.checkout

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.checkout.DiscountActivity.Companion.newTotal
import com.chcreation.pointofsale.checkout.NoteActivity.Companion.note
import com.chcreation.pointofsale.checkout.SelectCustomerActivity.Companion.selectCustomerCode
import com.chcreation.pointofsale.checkout.SelectCustomerActivity.Companion.selectCustomerName
import com.chcreation.pointofsale.customer.CustomerFragment
import com.chcreation.pointofsale.home.HomeFragment.Companion.cartItems
import com.chcreation.pointofsale.home.HomeFragment.Companion.totalPrice
import com.chcreation.pointofsale.home.HomeFragment.Companion.totalQty
import com.chcreation.pointofsale.model.Transaction
import com.chcreation.pointofsale.presenter.CheckOutPresenter
import com.chcreation.pointofsale.presenter.Homepresenter
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

class CheckOutActivity : AppCompatActivity(), MainView {

    companion object{
        var isCustomer = 0 // 0 havent select, 1 on select, 2 selected
        var totalReceived = 0
        var transDate = ""
        var transCode = 0
        var totalOutStanding = 0
    }
    private var paymentMethod = ""
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: CheckOutPresenter
    private var checkOutClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = CheckOutPresenter(this,mAuth,mDatabase)

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

        etCheckOutAmountReceived.setText(totalPrice.toString())
        tvCheckOutTotal.text = indonesiaCurrencyFormat().format(totalPrice)

        etCheckOutAmountReceived.onClick {
            etCheckOutAmountReceived.setText("")
        }

        btnCheckOut.onClick {
            btnCheckOut.startAnimation(normalClickAnimation())

            if (!checkOutClicked){
                if (paymentMethod == "")
                    toast("Please Select Payment Method !")
                else{
                    checkOutClicked = true

                    totalReceived = etCheckOutAmountReceived.text.toString().toInt()

                    if (totalPrice - totalReceived > 0)
                        totalOutStanding = totalPrice - totalReceived

                    val gson = Gson()
                    val orderDetail = gson.toJson(cartItems)

                    alert ("Continue to Check Out?"){
                        title = "Confirmation"

                        yesButton {
                            pbCheckOut.visibility = View.VISIBLE
                            tvCheckOutProcessTitle.visibility = View.VISIBLE
                            layoutCheckOutContent.alpha = 0.3F

                            var discount = 0
                            if (newTotal != 0)
                                discount = totalPrice - newTotal
                            presenter.saveTransaction(Transaction("", totalPrice,totalOutStanding,
                                totalReceived,discount,paymentMethod,orderDetail,selectCustomerCode, note,"",
                                mAuth.currentUser!!.uid)
                                , getMerchant(this@CheckOutActivity))
                        }

                        noButton {

                        }
                    }.show()
                }

            }
        }

    }

    override fun onStart() {
        super.onStart()

        if (selectCustomerName != "")
            supportActionBar?.title = selectCustomerName
        else
            supportActionBar?.title = "No Customer"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_check_out, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_select_customer -> {
                isCustomer = 1
                startActivity<SelectCustomerActivity>()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
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
        checkOutClicked = false
    }
}
