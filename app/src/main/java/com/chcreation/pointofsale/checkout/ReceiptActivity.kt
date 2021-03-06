package com.chcreation.pointofsale.checkout

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.PixelCopy
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.ErrorActivity.Companion.errorMessage
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.transCode
import com.chcreation.pointofsale.model.Cart
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.model.Payment
import com.chcreation.pointofsale.model.User
import com.chcreation.pointofsale.presenter.TransactionPresenter
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transCodeItems
import com.chcreation.pointofsale.transaction.TransactionFragment.Companion.transPosition
import com.chcreation.pointofsale.view.MainView
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.EscPosPrinterSize
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.exceptions.EscPosConnectionException
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_receipt.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.selector
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class ReceiptActivity : AppCompatActivity(), MainView {

    private lateinit var adapter: CartRecyclerViewAdapter
    private lateinit var adapterPaymentList : ReceiptPaymentListRecyclerViewAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter : TransactionPresenter
    private var paymentLists : MutableList<Payment> = mutableListOf()
    private var boughtList =  com.chcreation.pointofsale.model.Transaction()
    private lateinit var user: User
    private var taxName = ""
    private var discountName = ""
    private var receiptCode = 0
    private var screenShotPath : Uri? = null
    private var sincere = ""
    private var receiptTemplate = ECustomReceipt.RECEIPT1.toString()
    private var customer = Customer()
    private var purchasedItems = mutableListOf<Cart>()
    private var BLUETOOTH_PERMISSION  = 301
    private var merchantImageBitmap : Bitmap? = null

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

        tvReceiptMerchantName.text = getMerchantName(this).toUpperCase(Locale.ENGLISH)
        if (getMerchantCode(this).length >= 18)
            tvReceiptMerchantName.textSize = 20F
        tvReceiptMerchantAddress.text = getMerchantAddress(this)
        tvReceiptMerchantTel.text = getMerchantNoTel(this)

        tvReceiptTransCode.text = "Receipt: ${receiptFormat(transCode)}"

        GlobalScope.launch{
            sincere = presenter.getSincere()
            merchantImageBitmap = BitmapFactory.decodeStream(URL(getMerchantImage(this@ReceiptActivity)).openConnection().getInputStream())
        }

        btnReceiptShare.onClick {
            btnReceiptShare.startAnimation(normalClickAnimation())
            loading()
            selector("Share", arrayListOf("Other Apps")){dialogInterface, i ->
                getBitmapFromView(layoutReceipt.rootView,this@ReceiptActivity){bitmap,uri->
                    shareImage(uri)
                    endLoading()
                }
            }
            Handler().postDelayed({endLoading()},2000)

        }

        btnReceiptPrint.onClick {
            if (ContextCompat.checkSelfPermission(this@ReceiptActivity, android.Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this@ReceiptActivity,
                    arrayOf(android.Manifest.permission.BLUETOOTH),BLUETOOTH_PERMISSION
                )
            else{
                selector("Print", arrayListOf("Receipt","Kitchen Ticket")){dialogInterface, i ->
                    selectPrinter(i)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (transCode != 0){ // from post check out
            GlobalScope.launch {
                presenter.retrieveTransactionListPayments(transCode)
                presenter.retrieveTransaction(transCode)
            }
        }
        else if (transCodeItems.elementAtOrNull(transPosition) != null){ // from transaction
            GlobalScope.launch {
                presenter.retrieveTransactionListPayments(transCodeItems[transPosition])
                presenter.retrieveTransaction(transCodeItems[transPosition])
            }
        }else
            finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == BLUETOOTH_PERMISSION){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selector("Print", arrayListOf("Receipt","Kitchen Ticket")){dialogInterface, i ->
                    selectPrinter(i)
                }
            }
            else
                toast("Permission Denied")
        }
    }

    private fun selectPrinter(type: Int){
        val bPrinter = BluetoothPrintersConnections()
        val printerList = arrayListOf<String>()
        if (bPrinter.list == null){
            toast("Please Open Bluetooth!")
        }else{
            bPrinter.list.forEach { printerList.add("${it.device.name} - ${it.device}") }
            val title = if (bPrinter.list.isEmpty()) "No Device Available" else "Select Printer"
            selector(title, printerList){dialogInterface, i ->
                if (bPrinter.list != null && purchasedItems.size > 0){
                    cvReceiptPrint.visibility = View.VISIBLE
                    tvReceiptPrinterName.text = bPrinter.list[i].device.name
                    GlobalScope.launch (Dispatchers.Main){
                        try {
                            if (bPrinter.list[i] == null){
                                toast("Please Check Your Printer!")
                            }else{
                                val cPrinter = if (bPrinter.list[i].isConnected) bPrinter.list[i]
                                else bPrinter.list[i].connect()
                                val message = sysPrintReceipt(cPrinter,type,purchasedItems)
                                cvReceiptPrint.visibility = View.GONE
                                bPrinter.list[i].disconnect()
                                toast(message)
                            }
                        }catch (e: EscPosConnectionException){
                            toast(e.message.toString())
                            cvReceiptPrint.visibility = View.GONE
                        }
                    }
                }
                else if (purchasedItems.size == 0){
                    if (transCode != 0){ // from post check out
                        GlobalScope.launch {
                            presenter.retrieveTransaction(transCode)
                        }
                    }
                    else{ // from transaction
                        GlobalScope.launch {
                            presenter.retrieveTransaction(transCodeItems[transPosition])
                        }
                    }
                    toast("Please Try Again!")
                }
                else
                    toast("Please Refresh Your Bluetooth Connection!")
            }
        }
        endLoading()
    }

    private fun defaultReceipt(bluetoothConnection: BluetoothConnection,purchasedItems: MutableList<Cart>,callback: (message:String) -> Unit){
        try{
            val printer = EscPosPrinter(bluetoothConnection, getPrintDpi(this), getPrintWidth(this),
                getPrintCharLine(this))
            var textReceipt = ""
            val discount = boughtList.DISCOUNT!!
            val tax = boughtList.TAX!!
            val totalPrice = boughtList.TOTAL_PRICE!!
            val totalOutstanding = boughtList.TOTAL_OUTSTANDING!!

            val totalPayment = totalPrice - discount + tax

            val a = ivReceiptMerchantImage.drawable

            textReceipt += if (getMerchantImage(this) == "" || !getMerchantReceiptImage(this) || merchantImageBitmap == null) ""
            else "[C]<img>"+PrinterTextParserImg.bitmapToHexadecimalString(printer,merchantImageBitmap) +"</img>\n"

            textReceipt += "[C]<b><font size=\"big\">${getMerchantName(this)}</font></b>\n" +
                    "[C]${getMerchantAddress(this)}\n" +
                    "[C]${getMerchantNoTel(this)}\n"

            textReceipt += "[L]\n[L]Cashier : ${user.NAME}\n"

            if (getMerchantReceiptCustName(this))
                textReceipt += "[L]Customer: ${customer.NAME}\n"

            if (getMerchantReceiptCustAddress(this))
                textReceipt += "[L]Address : ${customer.ADDRESS}\n"

            if (getMerchantReceiptCustNoTel(this))
                textReceipt += "[L]No Tel  : ${customer.PHONE}\n"

            if (getReceiptTableNo(this))
                textReceipt += "[L]Table No: ${boughtList.TABLE_NO}\n"

            if (getReceiptPeopleNo(this))
                textReceipt += "[L]Guests  : ${if (isInt(boughtList.PEOPLE_NO!!)) boughtList.PEOPLE_NO!!.toInt() else boughtList.PEOPLE_NO}\n"

            textReceipt += "[C]"
            for (data in 1..getPrintCharLine(this)){
                textReceipt += "-"
            }
            textReceipt += "\n"

            for (data in purchasedItems){
                textReceipt += "[L]<b>${data.NAME}</b>[R]${currencyFormat(getLanguage(this), getCountry(this))
                    .format((if (data.WHOLE_SALE_PRICE == -1F) data.PRICE!! else data.WHOLE_SALE_PRICE!!) * data.Qty!!)}\n"

                textReceipt +="[L]  ${if (isInt(data.Qty!!)) data.Qty!!.toInt() else data.Qty}x${currencyFormat(getLanguage(this), getCountry(this))
                    .format((if (data.WHOLE_SALE_PRICE == -1F) data.PRICE!! else data.WHOLE_SALE_PRICE!!) )}\n"
            }

            textReceipt += "[C]"
            for (data in 1..getPrintCharLine(this)){
                textReceipt += "-"
            }
            textReceipt += "\n"

            if (tax != 0F || discount != 0F)
                textReceipt += "[L]<b>SubTotal:</b>[R]<b>${currencyFormat(getLanguage(this), getCountry(this)).format(totalPrice)}</b>\n"
            if (discount != 0F)
                textReceipt += "[L]<b>${if (discountName == "") "Discount" else discountName}:</b>[R]${currencyFormat(getLanguage(this), getCountry(this)).format(discount)}\n"
            if (tax != 0F){
                textReceipt += "[L]<b>${if (taxName == "") "Tax" else taxName}:</b>[R]${currencyFormat(getLanguage(this), getCountry(this)).format(tax)}\n"
            }

            textReceipt += "[L]<b>Total:</b>[R]<b>${currencyFormat(getLanguage(this), getCountry(this)).format(totalPayment)}</b>\n"

            textReceipt += "[C]"
            for (data in 1..getPrintCharLine(this)){
                textReceipt += "-"
            }
            textReceipt += "\n"

            textReceipt += "[L]<b>Amount:</b>"

            for (payment in paymentLists){
                textReceipt += "[R]${currencyFormat(getLanguage(this), getCountry(this)).format(payment.TOTAL_RECEIVED)}\n"
            }

            textReceipt += "[C]"
            for (data in 1..getPrintCharLine(this)){
                textReceipt += "-"
            }
            textReceipt += "\n"

            if (totalOutstanding > 0){
                textReceipt += "[L]Pending:[R]<b>${currencyFormat(getLanguage(this), getCountry(this)).format(totalOutstanding)}</b>\n"
            }else{
                var totalPaid = 0F
                for (data in paymentLists){
                    totalPaid += data.TOTAL_RECEIVED!!
                }
                textReceipt += "[L]Changes:[R]<b>${currencyFormat(getLanguage(this), getCountry(this)).format(totalPaid - totalPayment)}</b>\n"
            }

            if (getReceiptNote(this))
                textReceipt += "[L]Note:${boughtList.NOTE}\n[L]\n"

            if (getReceiptNo(this))
                textReceipt += "[C]Receipt:${receiptFormat(receiptCode)}\n"

            if (getReceiptDate(this))
                textReceipt += "[C]${parseDateFormatFull(boughtList.UPDATED_DATE.toString())}\n"

            textReceipt +=  "[L]\n[C]${sincere}\n"

            printer.printFormattedTextAndCut(textReceipt)
            callback("Print End")
//            printer.printFormattedText("[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer,
//                this.resources
//                    .getDrawableForDensity(R.drawable.default_image, DisplayMetrics.DENSITY_MEDIUM))+"</img>\n" +
//                    "[L]\n" +
//                    "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
//                    "[L]\n" +
//                    "[C]================================\n" +
//                    "[L]\n" +
//                    "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99e\n" +
//                    "[L]  + Size : S\n" +
//                    "[L]\n" +
//                    "[L]<b>AWESOME HAT</b>[R]24.99e\n" +
//                    "[L]  + Size : 57/58\n" +
//                    "[L]\n" +
//                    "[C]--------------------------------\n" +
//                    "[R]TOTAL PRICE :[R]34.98e\n" +
//                    "[R]TAX :[R]4.23e\n" +
//                    "[L]\n" +
//                    "[C]================================\n" +
//                    "[L]\n" +
//                    "[L]<font size='tall'>Customer :</font>\n" +
//                    "[L]Raymond DUPONT\n" +
//                    "[L]5 rue des girafes\n" +
//                    "[L]31547 PERPETES\n" +
//                    "[L]Tel : +33801201456\n" +
//                    "[L]\n" +
//                    "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
//                    "[C]<qrcode size='20'>http://www.developpeur-web.dantsu.com/</qrcode>")
        }catch (e: java.lang.Exception){
            callback(e.message.toString())
            e.printStackTrace()
        }
    }

    private fun backToSchoolReceipt(bluetoothConnection: BluetoothConnection,purchasedItems: MutableList<Cart>,callback: (message:String) -> Unit){ // Back To School Store 🎒🏫
        try{
            val printer = EscPosPrinter(bluetoothConnection, getPrintDpi(this), getPrintWidth(this),
                getPrintCharLine(this))
            var textReceipt = ""
            val discount = boughtList.DISCOUNT!!
            val tax = boughtList.TAX!!
            val totalPrice = boughtList.TOTAL_PRICE!!
            val totalOutstanding = boughtList.TOTAL_OUTSTANDING!!

            val totalPayment = totalPrice - discount + tax

            val a = ivReceiptMerchantImage.drawable

            textReceipt += if (getMerchantImage(this) == "" || !getMerchantReceiptImage(this) || merchantImageBitmap == null) ""
            else "[C]<img>"+PrinterTextParserImg.bitmapToHexadecimalString(printer,merchantImageBitmap) +"</img>\n"

            textReceipt += "[C]<b><font size=\"big\">${getMerchantName(this)}</font></b>\n" +
                    "[C]${getMerchantAddress(this)}\n" +
                    "[C]${getMerchantNoTel(this)}\n"

            textReceipt += "[L]\n[L]Cashier : ${user.NAME}\n"

            if (getMerchantReceiptCustName(this))
                textReceipt += "[L]Customer: ${customer.NAME}\n"

            if (getMerchantReceiptCustAddress(this))
                textReceipt += "[L]Address : ${customer.ADDRESS}\n"

            if (getMerchantReceiptCustNoTel(this))
                textReceipt += "[L]No Tel  : ${customer.PHONE}\n"

            if (getReceiptTableNo(this))
                textReceipt += "[L]Table No: ${boughtList.TABLE_NO}\n"

            if (getReceiptPeopleNo(this))
                textReceipt += "[L]Guests  : ${if (isInt(boughtList.PEOPLE_NO!!)) boughtList.PEOPLE_NO!!.toInt() else boughtList.PEOPLE_NO}\n"

            textReceipt += "[C]"
            for (data in 1..getPrintCharLine(this)){
                textReceipt += "-"
            }
            textReceipt += "\n"

            for (data in purchasedItems){
                textReceipt += "[L]<b>${data.NAME}</b>[R]${currencyFormat(getLanguage(this), getCountry(this))
                    .format((if (data.WHOLE_SALE_PRICE == -1F) data.PRICE!! else data.WHOLE_SALE_PRICE!!) * data.Qty!!)}\n"

                textReceipt +="[L]  ${if (isInt(data.Qty!!)) data.Qty!!.toInt() else data.Qty}x${currencyFormat(getLanguage(this), getCountry(this))
                    .format((if (data.WHOLE_SALE_PRICE == -1F) data.PRICE!! else data.WHOLE_SALE_PRICE!!) )}\n"
            }

            textReceipt += "[C]"
            for (data in 1..getPrintCharLine(this)){
                textReceipt += "-"
            }
            textReceipt += "\n"

            if (tax != 0F || discount != 0F)
                textReceipt += "[L]<b>SubTotal:</b>[R]<b>${currencyFormat(getLanguage(this), getCountry(this)).format(totalPrice)}</b>\n"
            if (discount != 0F)
                textReceipt += "[L]<b>${if (discountName == "") "Discount" else discountName}:</b>[R]${currencyFormat(getLanguage(this), getCountry(this)).format(discount)}\n"
            if (tax != 0F)
                textReceipt += "[L]<b>${if (taxName == "") "Tax" else taxName}:</b>[R]${currencyFormat(getLanguage(this), getCountry(this)).format(tax)}\n"

            textReceipt += "[L]<b>Total:</b>[R]<b>${currencyFormat(getLanguage(this), getCountry(this)).format(totalPayment)}</b>\n"

            textReceipt += "[C]"
            for (data in 1..getPrintCharLine(this)){
                textReceipt += "-"
            }
            textReceipt += "\n"

            textReceipt += "[L]<b>Amount:</b>"

            for (payment in paymentLists){
                textReceipt += "[R]${currencyFormat(getLanguage(this), getCountry(this)).format(payment.TOTAL_RECEIVED)}\n"
            }

            textReceipt += "[C]"
            for (data in 1..getPrintCharLine(this)){
                textReceipt += "-"
            }
            textReceipt += "\n"

            if (totalOutstanding > 0){
                textReceipt += "[L]Pending:[R]<b>${currencyFormat(getLanguage(this), getCountry(this)).format(totalOutstanding)}</b>\n"
            }else{
                var totalPaid = 0F
                for (data in paymentLists){
                    totalPaid += data.TOTAL_RECEIVED!!
                }
                textReceipt += "[L]Changes:[R]<b>${currencyFormat(getLanguage(this), getCountry(this)).format(totalPaid - totalPayment)}</b>\n"
            }

            if (getReceiptNote(this))
                textReceipt += "[L]Note:${boughtList.NOTE}\n[L]\n"

            if (getReceiptNo(this))
                textReceipt += "[C]Receipt:${receiptFormat(receiptCode)}\n"

            if (getReceiptDate(this))
                textReceipt += "[C]${parseDateFormatFull(boughtList.UPDATED_DATE.toString())}\n"

            textReceipt +=  "[L]\n[C]${sincere}\n"

            textReceipt += "[L]\n[C]<qrcode size='20'>https://instagram.com/william_hartanto999?igshid=1sp4iu6eo925c</qrcode>\n"

            printer.printFormattedTextAndCut(textReceipt)

            callback("Print End")
        }catch (e: java.lang.Exception){
            callback(e.message.toString())
            e.printStackTrace()
        }
    }

    private fun printKitchen(bluetoothConnection: BluetoothConnection,callback: (message:String) -> Unit){
        try{
            val printer = EscPosPrinter(bluetoothConnection, getPrintDpi(this), getPrintWidth(this),
                getPrintCharLine(this))

            var textReceipt = ""

            textReceipt += "[C]ORDER\n[L]\n" +
                    "[L]Date    : ${parseDateFormatFull(boughtList.UPDATED_DATE.toString())}\n"

            if (getReceiptNo(this))
                textReceipt += "[L]Receipt : ${receiptFormat(receiptCode)}\n"

            if (getReceiptTableNo(this))
                textReceipt += "[L]Table No: ${boughtList.TABLE_NO}\n"

            if (getReceiptPeopleNo(this))
                textReceipt += "[L]Guests  : ${if (isInt(boughtList.PEOPLE_NO!!)) boughtList.PEOPLE_NO!!.toInt() else boughtList.PEOPLE_NO}\n"

            textReceipt += "[L]Cashier : ${user.NAME}\n"

            textReceipt += "[C]"
            for (data in 1..getPrintCharLine(this)){
                textReceipt += "-"
            }
            textReceipt += "\n[L]\n"

            for (data in purchasedItems){
                textReceipt +="[L]${data.NAME}" +
                        "[R]x ${if (isInt(data.Qty!!)) data.Qty!!.toInt() else data.Qty}\n"
            }
            textReceipt += "[L]\n[C]"
            for (data in 1..getPrintCharLine(this)){
                textReceipt += "-"
            }

            printer.printFormattedText(textReceipt)
            callback("Print End")
        }catch (e: java.lang.Exception){
            callback(e.message.toString())
            e.printStackTrace()
        }
    }

    private suspend fun sysPrintReceipt(bluetoothConnection: BluetoothConnection
                                ,type: Int
                                ,purchasedItems: MutableList<Cart>) : String{
        return suspendCoroutine {ctx->

            when {
                type == 1 -> {
                    printKitchen(bluetoothConnection){
                        ctx.resume(it)
                    }
                }
                getMerchantCode(this) == "Back To School Store \uD83C\uDF92\uD83C\uDFEB" -> backToSchoolReceipt(bluetoothConnection,purchasedItems){
                    ctx.resume(it)
                }
                else -> defaultReceipt(bluetoothConnection,purchasedItems){
                    ctx.resume(it)
                }
            }
        }
    }


//    private fun fetchData(){
//        if (getMerchantReceiptImage(this))
//            layoutReceiptMerchantImage.visibility = View.VISIBLE
//        else
//            layoutReceiptMerchantImage.visibility = View.GONE
//
//        layoutReceiptCustomer.visibility = View.GONE
//        layoutReceiptCustomerAddress.visibility = View.GONE
//        layoutReceiptCustomerNoTel.visibility = View.GONE
//
//        layoutReceiptNote.visibility = View.GONE
//
//        GlobalScope.launch {
//            presenter.retrieveCashier(boughtList.CREATED_BY.toString())
//        }
//        val gson = Gson()
//        val arrayCartType = object : TypeToken<MutableList<Cart>>() {}.type
//        purchasedItems = gson.fromJson(boughtList.DETAIL,arrayCartType)
//
//        adapter = CartRecyclerViewAdapter(this,purchasedItems){
//
//        }
//        rvReceipt.adapter = adapter
//        rvReceipt.layoutManager = LinearLayoutManager(this)
//
//        if (getMerchantImage(this) == "")
//            layoutReceiptMerchantImage.visibility = View.GONE
//        else
//            Glide.with(this).load(getMerchantImage(this)).listener(object :
//                RequestListener<String, GlideDrawable> {
//                override fun onException(
//                    e: java.lang.Exception?,
//                    model: String?,
//                    target: Target<GlideDrawable>?,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    pbReceiptMerchantImage.visibility = View.GONE
//                    return false
//                }
//
//                override fun onResourceReady(
//                    resource: GlideDrawable?,
//                    model: String?,
//                    target: Target<GlideDrawable>?,
//                    isFromMemoryCache: Boolean,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    pbReceiptMerchantImage.visibility = View.GONE
//                    ivReceiptMerchantImage.visibility = View.VISIBLE
//                    return false
//                }
//
//            }).into(ivReceiptMerchantImage)
//        tvReceiptCashier.text = mAuth.currentUser?.displayName
//
//        val discount = boughtList.DISCOUNT!!
//        val tax = boughtList.TAX!!
//        val totalPrice = boughtList.TOTAL_PRICE!!
//        val totalOutstanding = boughtList.TOTAL_OUTSTANDING!!
//
//        val totalPayment = totalPrice - discount + tax
//
//        if (discount != 0F || tax != 0F){
//            tvReceiptSubTotal.text = currencyFormat(getLanguage(this), getCountry(this)).format(totalPrice)
//            tvReceiptSubTotalTitle.visibility = View.VISIBLE
//            tvReceiptSubTotal.visibility = View.VISIBLE
//        }
//        else{
//            tvReceiptSubTotalTitle.visibility = View.GONE
//            tvReceiptSubTotal.visibility = View.GONE
//        }
//
//        if (discount == 0F){
//            tvReceiptDiscount.visibility = View.GONE
//            tvReceiptDiscountTitle.visibility = View.GONE
//        }
//        if (tax == 0F){
//            tvReceiptTax.visibility = View.GONE
//            tvReceiptTaxTitle.visibility = View.GONE
//        }
//
//        tvReceiptDiscount.text = currencyFormat(getLanguage(this), getCountry(this)).format(discount)
//        tvReceiptTax.text = currencyFormat(getLanguage(this), getCountry(this)).format(tax)
//        tvReceiptTotal.text = currencyFormat(getLanguage(this), getCountry(this)).format(totalPayment)
////
////        if (note != "")
////            tvReceiptNote.text = "$note"
////        else{
////            layoutReceiptNote.visibility = View.GONE
////        }
////        if (transCode != 0){ // 0 berarti new receipt
////            if (totalReceived >= totalPayment)
////                tvReceiptChanges.text = indonesiaCurrencyFormat().format(totalReceived-totalPayment)
////            else{
////                tvReceiptAmountReceivedTitle.text = "Pending:"
////                tvReceiptChanges.text = indonesiaCurrencyFormat().format(totalPayment-totalReceived)
////            }
////        }
//
//        if (totalOutstanding > 0F){
//            tvReceiptAmountReceivedTitle.text = "Pending:"
//            tvReceiptChanges.text = currencyFormat(getLanguage(this), getCountry(this)).format(totalOutstanding)
//        }else{
//            var totalPaid = 0F
//            for (data in paymentLists){
//                totalPaid += data.TOTAL_RECEIVED!!
//            }
//            tvReceiptChanges.text = currencyFormat(getLanguage(this), getCountry(this)).format(totalPaid - totalPayment)
//        }
//        tvReceiptTransCode.text = "Receipt: ${receiptFormat(receiptCode)}"
//        tvReceiptDate.text = parseDateFormatFull(boughtList.UPDATED_DATE.toString())
//        tvReceiptSincere.text = sincere
//    }


    private fun fetchData(){
        if (getMerchantReceiptImage(this))
            layoutReceiptMerchantImage.visibility = View.VISIBLE
        else
            layoutReceiptMerchantImage.visibility = View.GONE

        layoutReceiptNote.visibility = View.VISIBLE

        if (getMerchantReceiptCustName(this))
            layoutReceiptCustomer.visibility = View.VISIBLE
        else
            layoutReceiptCustomer.visibility = View.GONE

        if (getMerchantReceiptCustAddress(this))
            layoutReceiptCustomerAddress.visibility = View.VISIBLE
        else
            layoutReceiptCustomerAddress.visibility = View.GONE

        if (getMerchantReceiptCustNoTel(this))
            layoutReceiptCustomerNoTel.visibility = View.VISIBLE
        else
            layoutReceiptCustomerNoTel.visibility = View.GONE

        if (getReceiptTableNo(this))
            layoutReceiptTableNo.visibility = View.VISIBLE
        else
            layoutReceiptTableNo.visibility = View.GONE

        if (getReceiptPeopleNo(this))
            layoutReceiptPeopleNo.visibility = View.VISIBLE
        else
            layoutReceiptPeopleNo.visibility = View.GONE

        tvReceiptPeopleNo.text = (if (isInt(boughtList.PEOPLE_NO!!)) boughtList.PEOPLE_NO!!.toInt() else boughtList.PEOPLE_NO).toString()
        tvReceiptTableNo.text = boughtList.TABLE_NO.toString()

        GlobalScope.launch {
            presenter.retrieveCashier(boughtList.CREATED_BY.toString())
            taxName = presenter.retrieveTax(boughtList.TAX_CODE.toString())
            discountName = presenter.retrieveDiscount(boughtList.DISCOUNT_CODE.toString())

            tvReceiptDiscountTitle.text = if (discountName == "") "Discount:" else "${discountName}:"
            tvReceiptTaxTitle.text = if (taxName == "") "Tax:" else "${taxName}:"
        }
        val gson = Gson()
        val arrayCartType = object : TypeToken<MutableList<Cart>>() {}.type
        purchasedItems = gson.fromJson(boughtList.DETAIL,arrayCartType)

        adapter = CartRecyclerViewAdapter(this,purchasedItems){

        }
        rvReceipt.adapter = adapter
        rvReceipt.layoutManager = LinearLayoutManager(this)

        if (getMerchantImage(this) == "")
            layoutReceiptMerchantImage.visibility = View.GONE
        else
            Glide.with(this).load(getMerchantImage(this)).listener(object :
                RequestListener<String, GlideDrawable> {
                override fun onException(
                    e: java.lang.Exception?,
                    model: String?,
                    target: Target<GlideDrawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    pbReceiptMerchantImage.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: GlideDrawable?,
                    model: String?,
                    target: Target<GlideDrawable>?,
                    isFromMemoryCache: Boolean,
                    isFirstResource: Boolean
                ): Boolean {
                    pbReceiptMerchantImage.visibility = View.GONE
                    ivReceiptMerchantImage.visibility = View.VISIBLE
                    return false
                }

            }).into(ivReceiptMerchantImage)
        tvReceiptCashier.text = mAuth.currentUser?.displayName

        val discount = boughtList.DISCOUNT!!
        val tax = boughtList.TAX!!
        val totalPrice = boughtList.TOTAL_PRICE!!
        val totalOutstanding = boughtList.TOTAL_OUTSTANDING!!

        val totalPayment = totalPrice - discount + tax

        if (discount != 0F || tax != 0F){
            tvReceiptSubTotal.text = currencyFormat(getLanguage(this), getCountry(this)).format(totalPrice)
            tvReceiptSubTotalTitle.visibility = View.VISIBLE
            tvReceiptSubTotal.visibility = View.VISIBLE
        }
        else{
            tvReceiptSubTotalTitle.visibility = View.GONE
            tvReceiptSubTotal.visibility = View.GONE
        }

        if (discount == 0F){
            tvReceiptDiscount.visibility = View.GONE
            tvReceiptDiscountTitle.visibility = View.GONE
        }
        if (tax == 0F){
            tvReceiptTax.visibility = View.GONE
            tvReceiptTaxTitle.visibility = View.GONE
        }

        tvReceiptDiscount.text = currencyFormat(getLanguage(this), getCountry(this)).format(discount)
        tvReceiptTax.text = currencyFormat(getLanguage(this), getCountry(this)).format(tax)
        tvReceiptTotal.text = currencyFormat(getLanguage(this), getCountry(this)).format(totalPayment)
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

        if (totalOutstanding > 0F){
            tvReceiptAmountReceivedTitle.text = "Pending:"
            tvReceiptChanges.text = currencyFormat(getLanguage(this), getCountry(this)).format(totalOutstanding)
        }else{
            var totalPaid = 0F
            for (data in paymentLists){
                totalPaid += data.TOTAL_RECEIVED!!
            }
            tvReceiptChanges.text = currencyFormat(getLanguage(this), getCountry(this)).format(totalPaid - totalPayment)
        }

        if (!getReceiptDate(this))
            tvReceiptDate.visibility = View.GONE
        if (!getReceiptNo(this))
            tvReceiptTransCode.visibility = View.GONE
        if (!getReceiptNote(this)){
            tvReceiptNoteTitle.visibility = View.GONE
            tvReceiptNote.visibility = View.GONE
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
            val width = view.measuredWidth

            view.rootView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            val bitmap = Bitmap.createBitmap(width, view.measuredHeight, Bitmap.Config.ARGB_8888)
            val locationOfViewInWindow = IntArray(2)
            view.getLocationInWindow(locationOfViewInWindow)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PixelCopy.request(window,
                        Rect(
                            locationOfViewInWindow[0],
                            locationOfViewInWindow[1],
                            locationOfViewInWindow[0] + width,
                            locationOfViewInWindow[1] + view.measuredHeight - 150
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
                }else{
                    endLoading()
                    toast("Screenshot is not support for this device")
                }
            } catch (e: IllegalArgumentException) {
                // PixelCopy may throw IllegalArgumentException, make sure to handle it
                toast(e.message.toString())
                endLoading()
                e.printStackTrace()
            }
        }
    }

    fun store(bm: Bitmap, fileName: String?): File {
        val dirPath: String =
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath.toString()
        val dir = File(dirPath)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dirPath, "receipt.jpg")
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
        intent.putExtra(Intent.EXTRA_TEXT, receiptFormat(receiptCode))
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
        btnReceiptPrint.visibility = View.GONE
    }

    private fun endLoading(){
        btnReceiptShare.visibility = View.VISIBLE
        btnReceiptPrint.visibility = View.VISIBLE
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

                if (boughtList.CUST_CODE != "") {
                    layoutReceiptCustomer.visibility = View.VISIBLE
                    presenter.retrieveCustomerByCode(boughtList.CUST_CODE.toString()) { success, customer ->
                        if (success){
                            tvReceiptCustomer.text = customer!!.NAME.toString()
                            tvReceiptCustomerAddress.text = customer.ADDRESS.toString()
                            tvReceiptCustomerNoTel.text = customer.PHONE.toString()
                            this@ReceiptActivity.customer = customer
                        }
                    }
                }
                fetchData()
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
