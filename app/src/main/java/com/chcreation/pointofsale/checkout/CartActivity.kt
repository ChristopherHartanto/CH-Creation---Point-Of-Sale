package com.chcreation.pointofsale.checkout

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.checkout.DiscountActivity.Companion.discount
import com.chcreation.pointofsale.checkout.DiscountActivity.Companion.tax
import com.chcreation.pointofsale.checkout.NoteActivity.Companion.note
import com.chcreation.pointofsale.home.HomeFragment
import com.chcreation.pointofsale.home.HomeFragment.Companion.cartItems
import com.chcreation.pointofsale.home.HomeFragment.Companion.imageItems
import com.chcreation.pointofsale.home.HomeFragment.Companion.tempProductItems
import com.chcreation.pointofsale.home.HomeFragment.Companion.totalPrice
import com.chcreation.pointofsale.home.HomeFragment.Companion.totalQty
import com.chcreation.pointofsale.model.WholeSale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_cart.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx

class CartActivity : AppCompatActivity() {

    private lateinit var adapter: CheckOutRecyclerViewAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        supportActionBar!!.title = "Cart"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        adapter = CheckOutRecyclerViewAdapter(this, cartItems, imageItems){ type,it->
            if (type == 1){
                if (cartItems[it].Qty == 1){
                    alert ("Remove ${cartItems[it].NAME} From Cart?"){
                        title = "Remove"
                        yesButton {a->
                            cartItems.removeAt(it)

                            totalPrice = sumPrice()
                            totalQty = countQty()
                            sumPriceDetail()

                            if (cartItems.size == 0){
                                PostCheckOutActivity().clearCartData()
                                finish()
                            }
                        }
                        noButton {  }
                    }.show()
                }else if (cartItems[it].Qty!! > 1){
                    cartItems[it].Qty = cartItems[it].Qty!! - 1
                    val wholeSalePrice: Int?
                    if (tempProductItems.single { f -> f.PROD_CODE == cartItems[it].PROD_CODE }.WHOLE_SALE != ""){
                        wholeSalePrice = getWholeSale(cartItems[it].Qty!!
                            ,tempProductItems.single { f -> f.PROD_CODE == cartItems[it].PROD_CODE }.WHOLE_SALE.toString())

                        cartItems[it].WHOLE_SALE_PRICE = wholeSalePrice
                    }
                    totalPrice = sumPrice()
                    totalQty = countQty()
                    sumPriceDetail()
                }else
                    toast("Error, Please Clear Your Cart!")
            }else if (type == 2){
                cartItems[it].Qty = cartItems[it].Qty!! + 1

                val wholeSalePrice: Int?
                if (tempProductItems.single { f -> f.PROD_CODE == cartItems[it].PROD_CODE }.WHOLE_SALE != ""){
                    wholeSalePrice = getWholeSale(cartItems[it].Qty!!
                        ,tempProductItems.single { f -> f.PROD_CODE == cartItems[it].PROD_CODE }.WHOLE_SALE.toString())

                    cartItems[it].WHOLE_SALE_PRICE = wholeSalePrice
                }

                totalPrice = sumPrice()
                totalQty = countQty()
                sumPriceDetail()
            }
            adapter.notifyDataSetChanged()
        }
        rvCart.adapter = adapter
        rvCart.layoutManager = LinearLayoutManager(this)

        tvCartTotal.text = "Total: ${currencyFormat(getLanguage(this), getCountry(this)).format(totalPrice)}"
        btnCart.onClick {
            btnCart.startAnimation(normalClickAnimation())
            if (cartItems.size != 0)
                startActivity<CheckOutActivity>()
            else
                toast("Error Please Fix Your Cart Items!")
        }

        ivCartMoreOptions.onClick {
            ivCartMoreOptions.startAnimation(normalClickAnimation())
            val options = mutableListOf("Add Note","Add Discount","Add Tax", "Delete Cart")


            selector("More Options",options) { dialogInterface, i ->
                when(i) {
                    0 ->{
                        startActivity<NoteActivity>()
                    }
                    1 ->{
                        startActivity(intentFor<DiscountActivity>("action" to 1))
                    }
                    2 ->{
                        startActivity(intentFor<DiscountActivity>("action" to 2))
                    }
                    3 ->{
                        alert ("Do You Want to Remove Cart?"){
                            title = "Delete Cart"
                            yesButton {
                                cartItems.clear()
                                imageItems.clear()
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

        sumPriceDetail()

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_CLOSE_ALL ->{
                setResult(RESULT_CLOSE_ALL)
                finish()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
//        note = ""
//        newTotal = 0
        super.onBackPressed()
    }


    private fun sumPrice() : Int{
        var total = 0

        for (data in cartItems){
            total += ((if (data.WHOLE_SALE_PRICE != -1) data.WHOLE_SALE_PRICE!! else data.PRICE!!) * data.Qty!!)
        }
        return total
    }

    private fun sumPriceDetail(){

        if (discount != 0) tvCartDiscount.visibility = View.VISIBLE else tvCartDiscount.visibility = View.GONE
        if (tax != 0) tvCartTax.visibility = View.VISIBLE else tvCartTax.visibility = View.GONE

        val totalPayment = totalPrice - discount + tax

        if (discount != 0 || tax != 0){

            tvCartDiscount.text = "Discount : ${currencyFormat(getLanguage(this), getCountry(this)).format(discount)}"
            tvCartTax.text = "Tax: ${currencyFormat(getLanguage(this), getCountry(this)).format(tax)}"

            tvCartSubTotal.text ="Sub Total : ${currencyFormat(getLanguage(this), getCountry(this)).format(totalPrice)}"
            tvCartSubTotal.visibility = View.VISIBLE
        }else
            tvCartSubTotal.visibility = View.GONE

        tvCartTotal.text = "Total : ${currencyFormat(getLanguage(this), getCountry(this)).format(totalPayment)}"
        if (note != "")
        {
            tvCartNote.visibility = View.VISIBLE
            tvCartNote.text = "Note: ${note}"
        }
        else
            tvCartNote.visibility = View.GONE

        btnCart.text = "$totalQty Item = ${currencyFormat(getLanguage(this), getCountry(this)).format(totalPayment)}"
    }

    private fun countQty() : Int{
        var total = 0
        for (data in cartItems){
            total += data.Qty!!
        }
        return total
    }

    private fun getWholeSale(qty:Int, wholeSale: String) : Int{
        val gson = Gson()
        val arrayWholeSaleType = object : TypeToken<MutableList<WholeSale>>() {}.type
        val items : MutableList<WholeSale> = gson.fromJson(wholeSale,arrayWholeSaleType)

        for (item in items){
            if (item.MIN_QTY!! <= qty && item.MAX_QTY!! >= qty){
                return item.PRICE!!
            }
        }
        return -1
    }
}
