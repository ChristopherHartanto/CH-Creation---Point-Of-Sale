package com.chcreation.pointofsale.checkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.checkout.DiscountActivity.Companion.newTotal
import com.chcreation.pointofsale.checkout.NoteActivity.Companion.note
import com.chcreation.pointofsale.home.HomeFragment
import com.chcreation.pointofsale.home.HomeFragment.Companion.cartItems
import com.chcreation.pointofsale.home.HomeFragment.Companion.totalPrice
import com.chcreation.pointofsale.home.HomeFragment.Companion.totalQty
import com.chcreation.pointofsale.indonesiaCurrencyFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_cart.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class CartActivity : AppCompatActivity() {

    private lateinit var adapter: CartRecyclerViewAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        supportActionBar!!.title = "Cart"

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        adapter = CartRecyclerViewAdapter(this, cartItems){

        }
        rvCart.adapter = adapter
        rvCart.layoutManager = LinearLayoutManager(this)

        tvCartTotal.text = "Total: ${indonesiaCurrencyFormat().format(totalPrice)}"
        btnCart.onClick {
            startActivity<CheckOutActivity>()
        }

        ivCartMoreOptions.onClick {

            val options = mutableListOf("Add Note","Add Discount", "Delete Cart")


            selector("More Options",options) { dialogInterface, i ->
                when(i) {
                    0 ->{
                        startActivity<NoteActivity>()
                    }
                    1 ->{
                        startActivity<DiscountActivity>()
                    }
                    2 ->{
                        alert ("Do You Want to Remove Cart?"){
                            title = "Delete Cart"
                            yesButton {
                                cartItems.clear()
                                totalQty = 0
                                totalPrice = 0
                                PostCheckOutActivity().clearCartData()
                                finish()
                            }
                            noButton {

                            }
                        }.show()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (newTotal != 0){
            val discount = totalPrice - newTotal
            if (newTotal != 0)
                tvCartDiscount.text = "Discount : $discount"

            tvCartTotal.text = "Total = ${indonesiaCurrencyFormat().format(newTotal)}"

            tvCartSubTotal.text ="Sub Total : $totalPrice"
            tvCartSubTotal.visibility = View.VISIBLE
        }else{
            tvCartTotal.text = "Total= ${indonesiaCurrencyFormat().format(totalPrice)}"
        }

        tvCartNote.text = "Note: ${note}"
        btnCart.text = "$totalQty Item = ${indonesiaCurrencyFormat().format(totalPrice)}"

    }

    override fun onBackPressed() {
        note = ""
        newTotal = 0
        super.onBackPressed()
    }
}
