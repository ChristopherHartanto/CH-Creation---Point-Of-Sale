package com.chcreation.pointofsale.checkout

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.PixelCopy
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.ErrorActivity.Companion.errorMessage
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.totalReceived
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.transCode
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.transDate
import com.chcreation.pointofsale.checkout.DiscountActivity.Companion.discount
import com.chcreation.pointofsale.checkout.DiscountActivity.Companion.tax
import com.chcreation.pointofsale.checkout.NoteActivity.Companion.note
import com.chcreation.pointofsale.home.HomeFragment
import com.chcreation.pointofsale.home.HomeFragment.Companion.cartItems
import com.chcreation.pointofsale.home.HomeFragment.Companion.totalPrice
import com.chcreation.pointofsale.model.Cart
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.model.Payment
import com.chcreation.pointofsale.model.User
import com.chcreation.pointofsale.presenter.TransactionPresenter
import com.chcreation.pointofsale.transaction.TransactionFragment
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transCodeItems
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transPosition
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_receipt.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class ReceiptActivity : AppCompatActivity(), MainView {

    private lateinit var adapter: CartRecyclerViewAdapter
    private lateinit var adapterPaymentList : ReceiptPaymentListRecyclerViewAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter : TransactionPresenter
    private var paymentLists : MutableList<Payment> = mutableListOf()
    private var boughtList =  com.chcreation.pointofsale.model.Transaction()
    private lateinit var user: User
    private var receiptCode = 0
    private var screenShotPath : Uri? = null
    private var sincere = ""
    private var receiptTemplate = ECustomReceipt.RECEIPT1.toString()
    private var customer = Customer()
    private var purchasedItems = mutableListOf<Cart>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = TransactionPresenter(this,mAuth,mDatabase,this)
        sincere = getMerchantSincere(this)
        receiptTemplate = getMerchantReceiptTemplate(this)

        adapterPaymentList = ReceiptPaymentListRecyclerViewAdapter(this,paymentLists)

        rvReceiptPaymentList.adapter = adapterPaymentList
        rvReceiptPaymentList.layoutManager = LinearLayoutManager(this)

        tvReceiptMerchantName.text = getMerchant(this).toUpperCase(Locale.ENGLISH)
        if (getMerchant(this).length >= 18)
            tvReceiptMerchantName.textSize = 20F
        tvReceiptMerchantAddress.text = getMerchantAddress(this)
        tvReceiptMerchantTel.text = getMerchantNoTel(this)

        tvReceiptTransCode.text = "Receipt: ${receiptFormat(transCode)}"

        btnReceiptShare.onClick {
            btnReceiptShare.startAnimation(normalClickAnimation())
            loading()

            selector("Share", arrayListOf("Other Apps")){dialogInterface, i ->
                when(i){
                    0 ->{
                        getBitmapFromView(layoutReceipt.rootView,this@ReceiptActivity){bitmap,uri->
                            shareImage(uri)
                            endLoading()
                            //                store(it,"${receiptFormat(receiptCode)}")
                        }

                    }
                    1->{
                        getBitmapFromView(layoutReceipt.rootView,this@ReceiptActivity){bitmap,uri->
                            shareImage(uri)
                            endLoading()
                        }
                    }
                }
            }


        }
    }

    override fun onStart() {
        super.onStart()

        if (transCode != 0){
            GlobalScope.launch {
                presenter.retrieveTransactionListPayments(transCode)
                presenter.retrieveTransaction(transCode)
            }
        }
        else{
            GlobalScope.launch {
                presenter.retrieveTransactionListPayments(transCodeItems[transPosition])
                presenter.retrieveTransaction(transCodeItems[transPosition])
            }
        }
    }

    private fun fetchData(){
        layoutReceiptCustomer.visibility = View.GONE
        layoutReceiptNote.visibility = View.GONE

        GlobalScope.launch {
            presenter.retrieveCashier(boughtList.CREATED_BY.toString())
        }
        val gson = Gson()
        val arrayCartType = object : TypeToken<MutableList<Cart>>() {}.type
        purchasedItems = gson.fromJson(boughtList.DETAIL,arrayCartType)

        adapter = CartRecyclerViewAdapter(this,purchasedItems){

        }
        rvReceipt.adapter = adapter
        rvReceipt.layoutManager = LinearLayoutManager(this)

        if (getMerchantImage(this) == "")
            ivReceiptMerchantImage.visibility = View.GONE
        else
            Glide.with(this).load(getMerchantImage(this)).into(ivReceiptMerchantImage)
        tvReceiptCashier.text = mAuth.currentUser?.displayName

        val discount = boughtList.DISCOUNT!!
        val tax = boughtList.TAX!!
        val totalPrice = boughtList.TOTAL_PRICE!!
        val totalOutstanding = boughtList.TOTAL_OUTSTANDING!!

        val totalPayment = totalPrice - discount + tax

        if (discount != 0 || tax != 0){
            tvReceiptSubTotal.text = indonesiaCurrencyFormat().format(totalPrice)
            tvReceiptSubTotalTitle.visibility = View.VISIBLE
            tvReceiptSubTotal.visibility = View.VISIBLE
        }
        else{
            tvReceiptSubTotalTitle.visibility = View.GONE
            tvReceiptSubTotal.visibility = View.GONE
        }

        if (discount == 0){
            tvReceiptDiscount.visibility = View.GONE
            tvReceiptDiscountTitle.visibility = View.GONE
        }
        if (tax == 0){
            tvReceiptTax.visibility = View.GONE
            tvReceiptTaxTitle.visibility = View.GONE
        }

        tvReceiptDiscount.text = indonesiaCurrencyFormat().format(discount)
        tvReceiptTax.text = indonesiaCurrencyFormat().format(tax)
        tvReceiptTotal.text = indonesiaCurrencyFormat().format(totalPayment)
//
//        if (note != "")
//            tvReceiptNote.text = "$note"
//        else{
//            layoutReceiptNote.visibility = View.GONE
//        }
//        if (transCode != 0){ // 0 berarti new receipt
//            if (totalReceived >= totalPayment)
//                tvReceiptChanges.text = indonesiaCurrencyFormat().format(totalReceived-totalPayment)
//            else{
//                tvReceiptAmountReceivedTitle.text = "Pending:"
//                tvReceiptChanges.text = indonesiaCurrencyFormat().format(totalPayment-totalReceived)
//            }
//        }

        if (totalOutstanding > 0){
            tvReceiptAmountReceivedTitle.text = "Pending:"
            tvReceiptChanges.text = indonesiaCurrencyFormat().format(totalOutstanding)
        }else{
            var totalPaid = 0
            for (data in paymentLists){
                totalPaid += data.TOTAL_RECEIVED!!
            }
            tvReceiptChanges.text = indonesiaCurrencyFormat().format(totalPaid - totalPayment)
        }
        tvReceiptTransCode.text = "Receipt: ${receiptFormat(receiptCode)}"
        tvReceiptDate.text = parseDateFormatFull(boughtList.UPDATED_DATE.toString())
        tvReceiptSincere.text = sincere
    }


    private fun fetchData2(){
        layoutReceiptNote.visibility = View.VISIBLE

        if (boughtList.CUST_CODE != "") {
            layoutReceiptCustomer.visibility = View.VISIBLE
            GlobalScope.launch {
                presenter.retrieveCustomerByCode(boughtList.CUST_CODE.toString()) { success, customer ->
                    if (success){
                        tvReceiptCustomer.text = customer!!.NAME.toString()
                        this@ReceiptActivity.customer = customer
                    }else
                        layoutReceiptCustomer.visibility = View.GONE
                }
            }
        }

        GlobalScope.launch {
            presenter.retrieveCashier(boughtList.CREATED_BY.toString())
        }
        val gson = Gson()
        val arrayCartType = object : TypeToken<MutableList<Cart>>() {}.type
        val purchasedItems : MutableList<Cart> = gson.fromJson(boughtList.DETAIL,arrayCartType)

        adapter = CartRecyclerViewAdapter(this,purchasedItems){

        }
        rvReceipt.adapter = adapter
        rvReceipt.layoutManager = LinearLayoutManager(this)

        if (getMerchantImage(this) == "")
            ivReceiptMerchantImage.visibility = View.GONE
        else
            Glide.with(this).load(getMerchantImage(this)).into(ivReceiptMerchantImage)
        tvReceiptCashier.text = mAuth.currentUser?.displayName

        val discount = boughtList.DISCOUNT!!
        val tax = boughtList.TAX!!
        val totalPrice = boughtList.TOTAL_PRICE!!
        val totalOutstanding = boughtList.TOTAL_OUTSTANDING!!

        val totalPayment = totalPrice - discount + tax

        if (discount != 0 || tax != 0){
            tvReceiptSubTotal.text = indonesiaCurrencyFormat().format(totalPrice)
            tvReceiptSubTotalTitle.visibility = View.VISIBLE
            tvReceiptSubTotal.visibility = View.VISIBLE
        }
        else{
            tvReceiptSubTotalTitle.visibility = View.GONE
            tvReceiptSubTotal.visibility = View.GONE
        }

        if (discount == 0){
            tvReceiptDiscount.visibility = View.GONE
            tvReceiptDiscountTitle.visibility = View.GONE
        }
        if (tax == 0){
            tvReceiptTax.visibility = View.GONE
            tvReceiptTaxTitle.visibility = View.GONE
        }

        tvReceiptDiscount.text = indonesiaCurrencyFormat().format(discount)
        tvReceiptTax.text = indonesiaCurrencyFormat().format(tax)
        tvReceiptTotal.text = indonesiaCurrencyFormat().format(totalPayment)
//
//        if (note != "")
//            tvReceiptNote.text = "$note"
//        else{
//            layoutReceiptNote.visibility = View.GONE
//        }
//        if (transCode != 0){ // 0 berarti new receipt
//            if (totalReceived >= totalPayment)
//                tvReceiptChanges.text = indonesiaCurrencyFormat().format(totalReceived-totalPayment)
//            else{
//                tvReceiptAmountReceivedTitle.text = "Pending:"
//                tvReceiptChanges.text = indonesiaCurrencyFormat().format(totalPayment-totalReceived)
//            }
//        }

        if (totalOutstanding > 0){
            tvReceiptAmountReceivedTitle.text = "Pending:"
            tvReceiptChanges.text = indonesiaCurrencyFormat().format(totalOutstanding)
        }else{
            var totalPaid = 0
            for (data in paymentLists){
                totalPaid += data.TOTAL_RECEIVED!!
            }
            tvReceiptChanges.text = indonesiaCurrencyFormat().format(totalPaid - totalPayment)
        }
        tvReceiptTransCode.text = "Receipt: ${receiptFormat(receiptCode)}"
        tvReceiptDate.text = parseDateFormatFull(boughtList.UPDATED_DATE.toString())

        tvReceiptNote.text = boughtList.NOTE
        tvReceiptSincere.text = sincere
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = dateFormat().format(Date(1000))
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {

        }

    }

    private fun getScreenShot(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) bgDrawable.draw(canvas)
        else canvas.drawColor(Color.WHITE)
        view.draw(canvas)

        return returnedBitmap
    }

    private fun getScreenBitmap(v: View) : Bitmap {
        v.isDrawingCacheEnabled = true;
       v.measure(
           View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
       v.layout(0, 0, v.measuredWidth, v.measuredHeight);

       v.buildDrawingCache(true);
       val b = Bitmap.createBitmap(v.drawingCache);
       v.setDrawingCacheEnabled(false); // clear drawing cache
       return b;
    }

    private fun getBitmapFromView(view: View, activity: Activity, callback: (Bitmap, Uri) -> Unit) {
        activity.window?.let { window ->
            val height = if (purchasedItems.size > 4) 3000 else view.height

            val bitmap = Bitmap.createBitmap(view.width, height, Bitmap.Config.ARGB_8888)
            val locationOfViewInWindow = IntArray(2)
            view.getLocationInWindow(locationOfViewInWindow)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PixelCopy.request(window,
                        Rect(
                            locationOfViewInWindow[0],
                            locationOfViewInWindow[1],
                            locationOfViewInWindow[0] + view.width,
                            locationOfViewInWindow[1] + height
                        ), bitmap, { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            val photoURI: Uri = FileProvider.getUriForFile(
                                this,
                                "com.example.android.fileprovider",
                                store(bitmap,"${receiptFormat(receiptCode)}")
                            )

                            callback(bitmap,photoURI)
                        }
                        // possible to handle other result codes ...
                    },
                        Handler())
                }else
                    toast("Screenshot is not support for this device")
            } catch (e: IllegalArgumentException) {
                // PixelCopy may throw IllegalArgumentException, make sure to handle it
                toast(e.message.toString())
                e.printStackTrace()
            }
            pbReceipt.visibility = View.GONE
        }
    }

    fun store(bm: Bitmap, fileName: String?): File {
        val dirPath: String =
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath.toString()
        val dir = File(dirPath)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dirPath, "${fileName}.jpg")
        try {
            val fOut = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.PNG, 85, fOut)
            fOut.flush()
            fOut.close()
//
//            val photoURI: Uri = FileProvider.getUriForFile(
//                this,
//                "com.example.android.fileprovider",
//                file
//            )

        } catch (e: Exception) {
            errorMessage = e.message.toString()
            startActivity<ErrorActivity>()
            e.printStackTrace()
        }
        return file
    }

    private fun shareImage(uri: Uri) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No App Available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareImageToWhatsApp(uri: Uri,number: String?) {
        var smsTo = Uri.parse("$number")
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.putExtra("jid:", "$smsTo@s.whatsapp.net")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        intent.setPackage("com.whatsapp")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No App Available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loading(){
        btnReceiptShare.visibility = View.GONE
        //pbReceipt.visibility = View.VISIBLE
    }

    private fun endLoading(){
        btnReceiptShare.visibility = View.VISIBLE
        pbReceipt.visibility = View.GONE
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_TRANS_LIST_PAYMENT_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                paymentLists.clear()
                for (data in dataSnapshot.children){
                    val item = data.getValue(Payment::class.java)
                    paymentLists.add(item!!)

                    adapterPaymentList.notifyDataSetChanged()
                }
//                if (transCode != 0){
//                    presenter.retrieveTransaction(transCode)
//                }
//                else{
//                    presenter.retrieveTransaction(transCodeItems[transPosition])
//                }
            }
        }else if (response == EMessageResult.FETCH_TRANS_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                val item = dataSnapshot.getValue(com.chcreation.pointofsale.model.Transaction::class.java)
                receiptCode = dataSnapshot.key!!.toInt()
                this.boughtList = item!!
                if (receiptTemplate == ECustomReceipt.RECEIPT1.toString())
                    fetchData()
                else if (receiptTemplate == ECustomReceipt.RECEIPT2.toString()){
                    if (boughtList.CUST_CODE != "") {
                        layoutReceiptCustomer.visibility = View.VISIBLE
                        presenter.retrieveCustomerByCode(boughtList.CUST_CODE.toString()) { success, customer ->
                            if (success){
                                tvReceiptCustomer.text = customer!!.NAME.toString()
                                this@ReceiptActivity.customer = customer
                            }else
                                layoutReceiptCustomer.visibility = View.GONE
                        }
                        fetchData2()
                    }

                }
            }
        }else if (response == EMessageResult.FETCH_USER_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                val item = dataSnapshot.getValue(User::class.java)
                user = item!!
                tvReceiptCashier.text = user.NAME
            }
        }
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
