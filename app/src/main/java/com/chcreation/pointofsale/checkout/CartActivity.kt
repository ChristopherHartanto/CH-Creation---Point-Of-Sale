package com.chcreation.pointofsale.checkout

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.peopleNo
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.tableNo
import com.chcreation.pointofsale.checkout.DiscountActivity.Companion.discount
import com.chcreation.pointofsale.checkout.DiscountActivity.Companion.tax
import com.chcreation.pointofsale.checkout.NoteActivity.Companion.note
import com.chcreation.pointofsale.home.HomeFragment.Companion.cartItems
import com.chcreation.pointofsale.home.HomeFragment.Companion.imageItems
import com.chcreation.pointofsale.home.HomeFragment.Companion.tempProductItems
import com.chcreation.pointofsale.home.HomeFragment.Companion.totalPrice
import com.chcreation.pointofsale.home.HomeFragment.Companion.totalQty
import com.chcreation.pointofsale.model.ActivityLogs
import com.chcreation.pointofsale.model.Discount
import com.chcreation.pointofsale.model.Tax
import com.chcreation.pointofsale.model.WholeSale
import com.chcreation.pointofsale.presenter.CheckOutPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.onRefresh
import java.lang.Exception
import java.util.*

class CartActivity : AppCompatActivity(),MainView {

    private lateinit var presenter: CheckOutPresenter
    private lateinit var adapter: CheckOutRecyclerViewAdapter
    private lateinit var taxAdapter: TaxListRecyclerViewAdapter
    private lateinit var discountAdapter: DiscountListRecyclerViewAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private var selectedCart = 0
    private var taxItems = mutableListOf<Tax>()
    private var discountItems = mutableListOf<Discount>()
    private var selectedTax = -1
    private var selectedUpdateTax = -1
    private var selectedDiscount = -1
    private var selectedUpdateDiscount = -1

    companion object{
        var taxCode = ""
        var discountCode = ""
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        supportActionBar!!.title = "Cart"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = CheckOutPresenter(this,mAuth,mDatabase,this)

        taxAdapter = TaxListRecyclerViewAdapter(this,taxItems){type, position ->
            Thread.sleep(100) // for UI purpose
            when(type){
                0->{
                    val percentage = taxItems[position].PERCENT
                    tax = totalPrice * percentage!! / 100
                    taxCode = taxItems[position].CODE.toString()
                    selectedTax = position
                    tvCartSelectedTax.text = taxItems[selectedTax].NAME +"/"+currencyFormat(getLanguage(this), getCountry(this)).format(tax)

                    sumPriceDetail()
                    visibleView()
                }
                1 ->{
                    selectedUpdateTax = position
                    cvCartTax.visibility = View.GONE
                    cvCartAddTax.visibility = View.VISIBLE
                    initAddTax()
                }
                2 ->{
                    alert ("Are You Sure Want to Delete ${taxItems[position].NAME}?"){
                        yesButton {
                            GlobalScope.launch (Dispatchers.Main){
                                val message = presenter.deleteTax(taxItems[position].CODE.toString(),
                                    dateFormat().format(Date()),mAuth.currentUser!!.uid)

                                presenter.saveActivityLogs(ActivityLogs("Delete Tax ${taxItems[position].NAME}"
                                    ,mAuth.currentUser!!.uid,dateFormat().format(Date())))

                                toast(message)
                                fetchTax()
                            }
                            if (position == selectedTax){
                                selectedTax = -1
                                tax = 0F
                                taxCode = ""
                                sumPriceDetail()
                                visibleView()
                            }
                        }
                        noButton {

                        }
                    }.show()
                }
            }
        }

        initTax()

        discountAdapter = DiscountListRecyclerViewAdapter(this,discountItems){type, position ->
            Thread.sleep(100) // for UI purpose
            when(type){
                0->{
                    val percentage = discountItems[position].PERCENT
                    discount = totalPrice * percentage!! / 100
                    discountCode = discountItems[position].CODE.toString()
                    selectedDiscount = position
                    tvCartSelectedDisc.text = discountItems[selectedDiscount].NAME +"/"+currencyFormat(getLanguage(this), getCountry(this)).format(
                        discount)

                    sumPriceDetail()
                    visibleView()
                }
                1 ->{
                    selectedUpdateDiscount = position
                    cvCartDisc.visibility = View.GONE
                    cvCartAddDisc.visibility = View.VISIBLE
                    initAddDiscount()
                }
                2 ->{
                    alert ("Are You Sure Want to Delete ${discountItems[position].NAME}?"){
                        yesButton {
                            GlobalScope.launch (Dispatchers.Main){
                                val message = presenter.deleteDiscount(discountItems[position].CODE.toString(),
                                    dateFormat().format(Date()),mAuth.currentUser!!.uid)

                                presenter.saveActivityLogs(ActivityLogs("Delete Discount ${discountItems[position].NAME}"
                                    ,mAuth.currentUser!!.uid,dateFormat().format(Date())))
                                toast(message)
                                fetchDisc()
                            }
                            if (position == selectedDiscount){
                                selectedDiscount = -1
                                discount = 0F
                                discountCode = ""
                                sumPriceDetail()
                                visibleView()
                            }
                        }
                        noButton {

                        }
                    }.show()
                }
            }
        }

        initDisc()

        adapter = CheckOutRecyclerViewAdapter(this, cartItems, imageItems){ type,it->
            Thread.sleep(100)

            if (type == 1){
                if (cartItems[it].Qty!! <= 1F){
                    alert ("Remove ${cartItems[it].NAME} From Cart?"){
                        title = "Remove"
                        yesButton {a->
                            deleteItem(it)
                        }
                        noButton {  }
                    }.show()
                }else if (cartItems[it].Qty!! > 1){
                    cartItems[it].Qty = cartItems[it].Qty!! - 1
                    val wholeSalePrice: Float?
                    if (tempProductItems.single { f -> f.PROD_CODE == cartItems[it].PROD_CODE }.WHOLE_SALE != ""){
                        wholeSalePrice = getWholeSale(cartItems[it].Qty!!
                            ,tempProductItems.single { f -> f.PROD_CODE == cartItems[it].PROD_CODE }.WHOLE_SALE.toString())

                        cartItems[it].WHOLE_SALE_PRICE = wholeSalePrice
                    }
                    totalPrice = sumPrice()
                    totalQty = countQty()
                    sumPriceDetail()
                    adapter.notifyDataSetChanged()
                }else
                    toast("Error, Please Clear Your Cart!")
            }else if (type == 2){
                cartItems[it].Qty = cartItems[it].Qty!! + 1

                val wholeSalePrice: Float?
                if (tempProductItems.single { f -> f.PROD_CODE == cartItems[it].PROD_CODE }.WHOLE_SALE != ""){
                    wholeSalePrice = getWholeSale(cartItems[it].Qty!!
                        ,tempProductItems.single { f -> f.PROD_CODE == cartItems[it].PROD_CODE }.WHOLE_SALE.toString())

                    cartItems[it].WHOLE_SALE_PRICE = wholeSalePrice
                }

                totalPrice = sumPrice()
                totalQty = countQty()
                sumPriceDetail()
                adapter.notifyDataSetChanged()
            }else if (type == 3){
                selectedCart = it
                etCartQty.setText((if (isInt(cartItems[it].Qty!!)) cartItems[it].Qty!!.toInt() else cartItems[it].Qty).toString())
                tvCartProdName.text = cartItems[it].NAME.toString()
                tvCartFirstName.text = cartItems[it].NAME!!.first().toString().toUpperCase(Locale.getDefault())

                if (tempProductItems.single { f -> f.PROD_CODE == cartItems[it].PROD_CODE }.CODE != ""){
                    tvCartProdCode.text = (if (tempProductItems.single { f -> f.PROD_CODE == cartItems[it].PROD_CODE }.CODE == "") "-"
                            else tempProductItems.single { f -> f.PROD_CODE == cartItems[it].PROD_CODE }.CODE).toString()
                }else
                    tvCartProdCode.text = "-"

                if (imageItems[it] != ""){
                    layoutCartDefaultImage.visibility = View.GONE
                    ivCartProdImage.visibility = View.VISIBLE

                    Glide.with(this).load(imageItems[it]).listener(object : RequestListener<String,GlideDrawable>{
                        override fun onException(
                            e: Exception?,
                            model: String?,
                            target: Target<GlideDrawable>?,
                            isFirstResource: Boolean
                        ): Boolean {

                            pbCart.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: GlideDrawable?,
                            model: String?,
                            target: Target<GlideDrawable>?,
                            isFromMemoryCache: Boolean,
                            isFirstResource: Boolean
                        ): Boolean {

                            pbCart.visibility = View.GONE
                            return false
                        }

                    }).into(ivCartProdImage)
                }
                else{
                    pbCart.visibility = View.GONE
                    layoutCartDefaultImage.visibility = View.VISIBLE
                    ivCartProdImage.visibility = View.GONE
                }
                cvCartProdDetail.visibility = View.VISIBLE
                bgCartProdDetail.visibility = View.VISIBLE
            }
            visibleView()
        }

        btnCartDeleteProd.onClick {
            btnCartDeleteProd.startAnimation(normalClickAnimation())
            cvCartProdDetail.visibility = View.GONE
            bgCartProdDetail.visibility = View.GONE

            deleteItem(selectedCart)
        }

        btnCartDoneProd.onClick {
            btnCartDeleteProd.startAnimation(normalClickAnimation())
            cvCartProdDetail.visibility = View.GONE
            bgCartProdDetail.visibility = View.GONE
            visibleView()

            val qty = etCartQty.text.toString().toFloat()
            if (qty <= 0)
                deleteItem(selectedCart)
            else{
                cartItems[selectedCart].Qty = qty

                val wholeSalePrice: Float?
                if (tempProductItems.single { f -> f.PROD_CODE == cartItems[selectedCart].PROD_CODE }.WHOLE_SALE != ""){
                    wholeSalePrice = getWholeSale(cartItems[selectedCart].Qty!!
                        ,tempProductItems.single { f -> f.PROD_CODE == cartItems[selectedCart].PROD_CODE }.WHOLE_SALE.toString())

                    cartItems[selectedCart].WHOLE_SALE_PRICE = wholeSalePrice
                }

                totalPrice = sumPrice()
                totalQty = countQty()
                sumPriceDetail()
                adapter.notifyDataSetChanged()
            }
        }

        btnCartMinQty.onClick {
            btnCartMinQty.startAnimation(normalClickAnimation())
            val qty = if(etCartQty.text.toString() == "") cartItems[selectedCart].Qty!! else etCartQty.text.toString().toFloat() - 1F
            etCartQty.setText((if (isInt(qty)) qty.toInt() else qty).toString())
        }

        btnCartAddQty.onClick {
            btnCartAddQty.startAnimation(normalClickAnimation())
            val qty = if(etCartQty.text.toString() == "") cartItems[selectedCart].Qty!! else etCartQty.text.toString().toFloat() + 1F
            etCartQty.setText((if (isInt(qty)) qty.toInt() else qty).toString())
        }

        bgCartProdDetail.onClick {
            cvCartProdDetail.visibility = View.GONE
            bgCartProdDetail.visibility = View.GONE
            cvCartCustTable.visibility = View.GONE
            visibleView()
        }

        btnCartCustTableCancel.onClick {
            cvCartCustTable.visibility = View.GONE
            bgCartProdDetail.visibility = View.GONE
            peopleNo = 0F
            tableNo = ""
            visibleView()
        }

        btnCustTableMinQty.onClick {
            btnCustTableMinQty.startAnimation(normalClickAnimation())
            val qty = if(etCartCustTablePeopleNumber.text.toString() == "") peopleNo else etCartCustTablePeopleNumber.text.toString().toFloat() - 1F
            etCartCustTablePeopleNumber.setText((if (isInt(qty)) qty.toInt() else qty).toString())
        }

        btnCustTableAddQty.onClick {
            btnCustTableAddQty.startAnimation(normalClickAnimation())
            val qty = if(etCartCustTablePeopleNumber.text.toString() == "") peopleNo else etCartCustTablePeopleNumber.text.toString().toFloat() + 1F
            etCartCustTablePeopleNumber.setText((if (isInt(qty)) qty.toInt() else qty).toString())
        }

        btnCartCustTableDone.onClick {
            btnCartCustTableDone.startAnimation(normalClickAnimation())

            if (etCartCustTablePeopleNumber.text.toString() == ""
                && etCartCustTableNumber.text.toString() == "")
                toast("Please Fill Following Field!")
            else if (etCartCustTablePeopleNumber.text.toString() != "")
                if (etCartCustTablePeopleNumber.text.toString().toFloat() < 0F){
                    toast("Wrong Input!")
                }
            else{
                visibleView()
                tableNo = etCartCustTableNumber.text.toString()

                if (etCartCustTablePeopleNumber.text.toString() != "") {
                    peopleNo = etCartCustTablePeopleNumber.text.toString().toFloat()
                }
                cvCartCustTable.visibility = View.GONE
                bgCartProdDetail.visibility = View.GONE
            }
            visibleView()
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
            val options = mutableListOf("Add Note","Add Discount","Add Tax","Add Table Number", "Delete Cart")


            selector("More Options",options) { dialogInterface, i ->
                when(i) {
                    0 ->{
                        goToNote()
                    }
                    1 ->{
                        goToDiscount()
                    }
                    2 ->{
                        goToTax()
                    }
                    3 ->{
                        goToTableNumber()
                    }
                    4 ->{
                        deleteCart()
                    }
                }
            }
        }

        btnCartTax.onClick {
            btnCartTax.startAnimation(normalClickAnimation())
            goToTax()
        }

        btnCartDiscount.onClick {
            btnCartDiscount.startAnimation(normalClickAnimation())
            goToDiscount()
        }

        btnCartTableNumber.onClick {
            btnCartTableNumber.startAnimation(normalClickAnimation())
            goToTableNumber()
        }

        btnCartNote.onClick {
            btnCartNote.startAnimation(normalClickAnimation())
            goToNote()
        }

        tvCartDelete.onClick {
            tvCartDelete.startAnimation(normalClickAnimation())
            deleteCart()
        }
    }

    override fun onStart() {
        super.onStart()

        if (discountCode == "") selectedDiscount = -1
        if (taxCode == "") selectedTax = -1

        sumPriceDetail()
        visibleView()
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

    private fun visibleView(){
        if (note == ""){
            btnCartNote.text = "Note"
            btnCartNote.textColorResource = R.color.colorBlack
            btnCartNote.backgroundResource = R.drawable.button_border
        }else{
            btnCartNote.text = note
            btnCartNote.textColorResource = R.color.colorWhite
            btnCartNote.backgroundResource = R.drawable.button_border_fill
        }
        if (tax == 0F){
            btnCartTax.text = "Tax"
            btnCartTax.textColorResource = R.color.colorBlack
            btnCartTax.backgroundResource = R.drawable.button_border
        }else{
            btnCartTax.text = currencyFormat(getLanguage(this), getCountry(this)).format(tax)
            btnCartTax.textColorResource = R.color.colorWhite
            btnCartTax.backgroundResource = R.drawable.button_border_fill
        }
        if (discount == 0F){
            btnCartDiscount.text = "Discount"
            btnCartDiscount.textColorResource = R.color.colorBlack
            btnCartDiscount.backgroundResource = R.drawable.button_border
        }else{
            btnCartDiscount.text = currencyFormat(getLanguage(this), getCountry(this)).format(discount)
            btnCartDiscount.textColorResource = R.color.colorWhite
            btnCartDiscount.backgroundResource = R.drawable.button_border_fill
        }
        if (tableNo == "" && peopleNo == 0F){
            btnCartTableNumber.text = "Table No"
            btnCartTableNumber.textColorResource = R.color.colorBlack
            btnCartTableNumber.backgroundResource = R.drawable.button_border
        }else{
            btnCartTableNumber.text = "${if (tableNo == "") "-" else tableNo}" +
                    "/${if (peopleNo == 0F) "-" else if (isInt(peopleNo)) peopleNo.toInt() else peopleNo}"
            btnCartTableNumber.textColorResource = R.color.colorWhite
            btnCartTableNumber.backgroundResource = R.drawable.button_border_fill
        }
    }

    private fun deleteItem(position: Int){

        cartItems.removeAt(position)
        imageItems.removeAt(position)

        totalPrice = sumPrice()
        totalQty = countQty()
        sumPriceDetail()
        adapter.notifyDataSetChanged()

        if (cartItems.size == 0){
            PostCheckOutActivity().clearCartData()
            finish()
        }
    }

    private fun sumPrice() : Float{
        var total = 0F

        for (data in cartItems){
            total += ((if (data.WHOLE_SALE_PRICE != -1F) data.WHOLE_SALE_PRICE!! else data.PRICE!!) * data.Qty!!)
        }
        return total
    }

    private fun sumPriceDetail(){

//        if (discount != 0F) tvCartDiscount.visibility = View.VISIBLE else tvCartDiscount.visibility = View.GONE
//        if (tax != 0F) tvCartTax.visibility = View.VISIBLE else tvCartTax.visibility = View.GONE

        val totalPayment = totalPrice - discount + tax

        if (discount != 0F || tax != 0F){

//            tvCartDiscount.text = currencyFormat(getLanguage(this), getCountry(this)).format(discount)
//            tvCartTax.text = currencyFormat(getLanguage(this), getCountry(this)).format(tax)

            tvCartSubTotal.text = currencyFormat(getLanguage(this), getCountry(this)).format(totalPrice)
            layoutCartSubTotal.visibility = View.VISIBLE
            //tvCartSubTotal.visibility = View.VISIBLE
        }
        else
            layoutCartSubTotal.visibility = View.GONE
//            tvCartSubTotal.visibility = View.GONE

        tvCartTotal.text = currencyFormat(getLanguage(this), getCountry(this)).format(totalPayment)
        //tvCartNote.text = note

        btnCart.text = "Check Out ${if (isInt(totalQty)) totalQty.toInt() else totalQty} Items"
    }

    private fun countQty() : Float{
        var total = 0F
        for (data in cartItems){
            total += data.Qty!!
        }
        return total
    }

    private fun getWholeSale(qty:Float, wholeSale: String) : Float{
        val gson = Gson()
        val arrayWholeSaleType = object : TypeToken<MutableList<WholeSale>>() {}.type
        val items : MutableList<WholeSale> = gson.fromJson(wholeSale,arrayWholeSaleType)

        for (item in items){
            if (item.MIN_QTY!! <= qty && item.MAX_QTY!! >= qty){
                return item.PRICE!!
            }
        }
        return -1F
    }

    private fun goToNote(){
        startActivity<NoteActivity>()
    }

    private fun goToTax(){
        cvCartTax.visibility = View.VISIBLE
        bgCartTaxDisc.visibility = View.VISIBLE

        srCartTax.isRefreshing = true
        fetchTax()

        var title = ""
        if (selectedTax != -1)
            title = taxItems[selectedTax].NAME.toString() + "/"
        if (tax != 0F)
            title += currencyFormat(getLanguage(this), getCountry(this)).format(tax)

        tvCartSelectedTax.text = title
    }

    private fun initTax(){

        rvCartTax.adapter = taxAdapter
        rvCartTax.layoutManager = LinearLayoutManager(this)

        srCartTax.onRefresh {
            fetchTax()
        }

        btnCartTaxClose.onClick {
            cvCartTax.visibility = View.GONE
            bgCartTaxDisc.visibility = View.GONE
            selectedTax = -1
            tax = 0F
            taxCode = ""
            sumPriceDetail()
            visibleView()
        }

        btnCartTaxDone.onClick {
            cvCartTax.visibility = View.GONE
            bgCartTaxDisc.visibility = View.GONE
        }

        btnCartTaxManual.onClick {
            btnCartTaxManual.startAnimation(normalClickAnimation())

            startActivity(intentFor<DiscountActivity>("action" to 2))
            cvCartTax.visibility = View.GONE
            bgCartTaxDisc.visibility = View.GONE
        }

        fbCartAddTax.onClick {
            fbCartAddTax.startAnimation(normalClickAnimation())
            cvCartTax.visibility = View.GONE
            cvCartAddTax.visibility = View.VISIBLE
            initAddTax()
        }
    }

    private fun initDisc(){

        rvCartDisc.adapter = discountAdapter
        rvCartDisc.layoutManager = LinearLayoutManager(this)

        srCartDisc.onRefresh {
            fetchDisc()
        }

        btnCartDiscClose.onClick {
            cvCartDisc.visibility = View.GONE
            bgCartTaxDisc.visibility = View.GONE
            selectedDiscount = -1
            discount = 0F
            discountCode = ""
            sumPriceDetail()
            visibleView()
        }

        btnCartDiscDone.onClick {
            cvCartDisc.visibility = View.GONE
            bgCartTaxDisc.visibility = View.GONE
        }

        btnCartDiscManual.onClick {
            btnCartDiscManual.startAnimation(normalClickAnimation())

            startActivity(intentFor<DiscountActivity>("action" to 1))
            cvCartDisc.visibility = View.GONE
            bgCartTaxDisc.visibility = View.GONE
        }

        fbCartAddDisc.onClick {
            fbCartAddDisc.startAnimation(normalClickAnimation())
            cvCartDisc.visibility = View.GONE
            cvCartAddDisc.visibility = View.VISIBLE
            initAddDiscount()
        }
    }

    private fun fetchTax(){
        GlobalScope.launch (Dispatchers.Main){
            val dataSnapshot = presenter.retrieveTax()
            if (dataSnapshot != null) {
                if (dataSnapshot.exists()){
                    taxItems.clear()

                    for (data in dataSnapshot.children){
                        val item = data.getValue(Tax::class.java)
                        if (item != null && item.STATUS_CODE == EStatusCode.ACTIVE.toString()){
                            taxItems.add(item)
                        }
                    }
                }
                taxAdapter.notifyDataSetChanged()
                if (taxItems.size == 0){
                    rvCartTax.visibility = View.GONE
                    layoutCartTaxNoData.visibility = View.VISIBLE
                }else{
                    rvCartTax.visibility = View.VISIBLE
                    layoutCartTaxNoData.visibility = View.GONE
                }
            }
            srCartTax.isRefreshing = false
        }
    }

    private fun fetchDisc(){
        GlobalScope.launch (Dispatchers.Main){
            val dataSnapshot = presenter.retrieveDiscount()
            if (dataSnapshot != null) {
                if (dataSnapshot.exists()){
                    discountItems.clear()

                    for (data in dataSnapshot.children){
                        val item = data.getValue(Discount::class.java)
                        if (item != null && item.STATUS_CODE == EStatusCode.ACTIVE.toString()){
                            discountItems.add(item)
                        }
                    }
                }
                discountAdapter.notifyDataSetChanged()
                if (discountItems.size == 0){
                    rvCartDisc.visibility = View.GONE
                    layoutCartDiscNoData.visibility = View.VISIBLE
                }else{
                    rvCartDisc.visibility = View.VISIBLE
                    layoutCartDiscNoData.visibility = View.GONE
                }
            }
            srCartDisc.isRefreshing = false
        }
    }

    private fun goToDiscount(){
        cvCartDisc.visibility = View.VISIBLE
        bgCartTaxDisc.visibility = View.VISIBLE

        srCartDisc.isRefreshing = true
        fetchDisc()

        var title = ""
        if (selectedDiscount != -1)
            title = discountItems[selectedDiscount].NAME.toString() + "/"
        if (discount != 0F)
            title += currencyFormat(getLanguage(this), getCountry(this)).format(discount)

        tvCartSelectedDisc.text = title
    }

    private fun goToTableNumber(){
        Thread.sleep(100)
        cvCartCustTable.visibility = View.VISIBLE
        bgCartProdDetail.visibility = View.VISIBLE
        val qty = peopleNo
        etCartCustTablePeopleNumber.setText((if (isInt(qty)) qty.toInt() else qty).toString())
        etCartCustTableNumber.setText(tableNo)
    }

    private fun deleteCart(){
        alert ("Do You Want to Remove Cart?"){
            title = "Delete Cart"
            yesButton {
                cartItems.clear()
                imageItems.clear()
                totalQty = 0F
                totalPrice = 0F
                PostCheckOutActivity().clearCartData()
                finish()
            }
            noButton {

            }
        }.show()
    }

    private fun initAddTax(){

        if (selectedUpdateTax == -1)
            tvCartAddTaxTitle.text = "Add Tax"
        else{
            tvCartAddTaxTitle.text = "Update Tax"
            etAddTaxName.setText(taxItems[selectedUpdateTax].NAME.toString())
            etAddTaxAmount.setText(taxItems[selectedUpdateTax].PERCENT.toString())
        }

        btnCartAddTaxClose.onClick {
            cvCartAddTax.visibility = View.GONE
            bgCartTaxDisc.visibility = View.GONE
            selectedUpdateTax = -1
        }

        btnCartAddTaxDone.onClick {
            val name = etAddTaxName.text.toString()
            val amount = etAddTaxAmount.text.toString()

            if (name == "" || amount == ""){
                toast("Please Fill Following Field!")
            }else{
                pbCart.visibility = View.VISIBLE
                cvCartAddTax.visibility = View.GONE
                bgCartTaxDisc.visibility = View.GONE

                val code = generateCode("TAX")
                GlobalScope.launch (Dispatchers.Main){

                    val message =
                        when (selectedUpdateTax) {
                            -1 -> presenter.saveTax(Tax(name,amount.toFloat(),code, EStatusCode.ACTIVE.toString(),
                                dateFormat().format(Date()),dateFormat().format(Date()),
                                mAuth.currentUser!!.uid,mAuth.currentUser!!.uid))
                            else-> presenter.updateTax(taxItems[selectedUpdateTax].CODE.toString(),
                                Tax(name,
                                    amount.toFloat(),
                                    taxItems[selectedUpdateTax].CODE,
                                    taxItems[selectedUpdateTax].STATUS_CODE,
                                    taxItems[selectedUpdateTax].CREATED_DATE,
                                    dateFormat().format(Date()),
                                    taxItems[selectedUpdateTax].CREATED_BY,mAuth.currentUser!!.uid))
                        }

                    presenter.saveActivityLogs(ActivityLogs("${if (selectedUpdateTax == -1) "Save" else "Update"} Tax $name"
                            ,mAuth.currentUser!!.uid,dateFormat().format(Date())))
                    toast(message)

                    selectedUpdateTax = -1
                    etAddTaxName.setText("")
                    etAddTaxAmount.setText("")
                }
            }
        }
    }

    private fun initAddDiscount(){

        if (selectedUpdateDiscount == -1)
            tvCartAddDiscTitle.text = "Add Discount"
        else{
            tvCartAddDiscTitle.text = "Update Discount"
            etAddDiscName.setText(discountItems[selectedUpdateDiscount].NAME.toString())
            etAddDiscAmount.setText(discountItems[selectedUpdateDiscount].PERCENT.toString())
        }

        btnCartAddDiscClose.onClick {
            etAddDiscName.setText("")
            etAddDiscAmount.setText("")
            cvCartAddDisc.visibility = View.GONE
            bgCartTaxDisc.visibility = View.GONE
            selectedUpdateDiscount = -1
        }

        btnCartAddDiscDone.onClick {
            val name = etAddDiscName.text.toString()
            val amount = etAddDiscAmount.text.toString()

            if (name == "" || amount == ""){
                toast("Please Fill Following Field!")
            }else{
                cvCartAddDisc.visibility = View.GONE
                bgCartTaxDisc.visibility = View.GONE

                val code = generateCode("DISC")
                GlobalScope.launch (Dispatchers.Main){

                    val message =
                        when (selectedUpdateDiscount) {
                            -1 -> {
                                presenter.saveDiscount(
                                    Discount(name,amount.toFloat(),code, EStatusCode.ACTIVE.toString(),
                                        dateFormat().format(Date()),dateFormat().format(Date()),
                                        mAuth.currentUser!!.uid,mAuth.currentUser!!.uid)
                                )
                            }
                            else-> presenter.updateDiscount(discountItems[selectedUpdateDiscount].CODE.toString(),
                                Discount(name,
                                    amount.toFloat(),
                                    discountItems[selectedUpdateDiscount].CODE,
                                    discountItems[selectedUpdateDiscount].STATUS_CODE,
                                    discountItems[selectedUpdateDiscount].CREATED_DATE,
                                    dateFormat().format(Date()),
                                    discountItems[selectedUpdateDiscount].CREATED_BY,mAuth.currentUser!!.uid))
                        }

                    presenter.saveActivityLogs(ActivityLogs("${if (selectedUpdateDiscount == -1) "Save" else "Update"} Discount $name"
                        ,mAuth.currentUser!!.uid,dateFormat().format(Date())))

                    toast(message)

                    selectedUpdateDiscount = -1
                    etAddDiscName.setText("")
                    etAddDiscAmount.setText("")
                }
            }
        }
    }

    private fun generateCode(name:String) : String{
        return "${name}${mDatabase.push().key.toString()}"
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {

    }

    override fun response(message: String) {
        toast(message)
    }
}
