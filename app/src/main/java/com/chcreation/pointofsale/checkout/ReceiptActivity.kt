package com.chcreation.pointofsale.checkout

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.chcreation.pointofsale.model.Payment
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
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileOutputStream
import java.util.*


class ReceiptActivity : AppCompatActivity(), MainView {

    private lateinit var adapter: CartRecyclerViewAdapter
    private lateinit var adapterPaymentList : ReceiptPaymentListRecyclerViewAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter : TransactionPresenter
    private var paymentLists : MutableList<Payment> = mutableListOf()
    private lateinit var boughtList: com.chcreation.pointofsale.model.Transaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = TransactionPresenter(this,mAuth,mDatabase,this)

        adapterPaymentList = ReceiptPaymentListRecyclerViewAdapter(this,paymentLists)

        rvReceiptPaymentList.adapter = adapterPaymentList
        rvReceiptPaymentList.layoutManager = LinearLayoutManager(this)

        tvReceiptMerchantName.text = getMerchant(this).toUpperCase(Locale.ENGLISH)

        tvReceiptTransCode.text = "Receipt: ${receiptFormat(transCode)}"

        btnReceiptShare.onClick {
            btnReceiptShare.startAnimation(normalClickAnimation())
            pbReceipt.visibility = View.VISIBLE
            //layoutReceipt.alpha = 0.3F

            val bitmap = getScreenShot(window.decorView.findViewById(R.id.layoutReceipt))
            store(bitmap,"${receiptFormat(transCode)}")

            pbReceipt.visibility = View.GONE
            layoutReceipt.alpha = 1F
        }
    }

    override fun onStart() {
        super.onStart()

        if (transCode != 0){
            presenter.retrieveTransactionListPayments(transCode)
        }
        else{
            presenter.retrieveTransactionListPayments(transCodeItems[transPosition])
        }
    }

    private fun fetchData(){
        val gson = Gson()
        val arrayCartType = object : TypeToken<MutableList<Cart>>() {}.type
        val purchasedItems : MutableList<Cart> = gson.fromJson(boughtList.DETAIL,arrayCartType)

        adapter = CartRecyclerViewAdapter(this,purchasedItems){

        }
        rvReceipt.adapter = adapter
        rvReceipt.layoutManager = LinearLayoutManager(this)

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

        tvReceiptDate.text = boughtList.UPDATED_DATE.toString()
    }

    private fun getScreenShot(view: View): Bitmap {
//        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(returnedBitmap)
//        val bgDrawable = view.background
//        if (bgDrawable != null) bgDrawable.draw(canvas)
//        else canvas.drawColor(Color.WHITE)
//        view.draw(canvas)
        val screenView = view.rootView
        screenView.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(screenView.drawingCache)
        screenView.isDrawingCacheEnabled = false
        return bitmap
    }

    fun store(bm: Bitmap, fileName: String?) {
        val dirPath: String =
            Environment.getExternalStorageDirectory().absolutePath.toString() + "/Screenshots"
        val dir = File(dirPath)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dirPath, fileName)
        try {
            val fOut = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.PNG, 85, fOut)
            fOut.flush()
            fOut.close()

            shareImage(file)
        } catch (e: Exception) {
            errorMessage = e.message.toString()
            startActivity<ErrorActivity>()
            e.printStackTrace()
        }
    }

    private fun shareImage(file: File) {
        val uri: Uri = Uri.fromFile(file)
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

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_TRANS_LIST_PAYMENT_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                paymentLists.clear()
                for (data in dataSnapshot.children){
                    val item = data.getValue(Payment::class.java)
                    paymentLists.add(item!!)

                    adapterPaymentList.notifyDataSetChanged()
                }
                if (transCode != 0){
                    presenter.retrieveTransaction(transCode)
                }
                else{
                    presenter.retrieveTransaction(transCodeItems[transPosition])
                }
            }
        }else if (response == EMessageResult.FETCH_TRANS_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                val item = dataSnapshot.getValue(com.chcreation.pointofsale.model.Transaction::class.java)
                this.boughtList = item!!
                fetchData()
            }
        }
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
