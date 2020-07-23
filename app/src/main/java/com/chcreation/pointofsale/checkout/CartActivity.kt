package com.chcreation.pointofsale.checkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.checkout.DiscountActivity.Companion.newTotal
import com.chcreation.pointofsale.checkout.NoteActivity.Companion.note
import com.chcreation.pointofsale.home.HomeFragment
import com.chcreation.pointofsale.home.HomeFragment.Companion.cartItems
import com.chcreation.pointofsale.home.HomeFragment.Companion.totalPrice
import com.chcreation.pointofsale.home.HomeFragment.Companion.totalQty
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

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        adapter = CartRecyclerViewAdapter(this, cartItems){

        }
        rvCart.adapter = adapter
        rvCart.layoutManager = LinearLayoutManager(this)

        tvCartTotal.text = "Total: Rp $totalPrice,00"
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
                                totalPrice = 0F
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

        if (newTotal != 0F)
            tvCartDiscount.text = "Discount $totalPrice - $newTotal"

        tvCartNote.text = "Note: ${note}"

        if (newTotal != 0F)
            btnCart.text = "${HomeFragment.totalQty} Item = Rp ${totalPrice},00"
        else
            btnCart.text = "${HomeFragment.totalQty} Item = Rp ${newTotal},00"
    }

    override fun onBackPressed() {
        note = ""
        newTotal = 0F
        super.onBackPressed()
    }
}
