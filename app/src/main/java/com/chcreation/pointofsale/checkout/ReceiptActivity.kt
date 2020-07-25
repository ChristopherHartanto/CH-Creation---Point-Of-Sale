package com.chcreation.pointofsale.checkout

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.totalReceived
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.transCode
import com.chcreation.pointofsale.checkout.CheckOutActivity.Companion.transDate
import com.chcreation.pointofsale.checkout.NoteActivity.Companion.note
import com.chcreation.pointofsale.home.HomeFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_receipt.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileOutputStream
import java.util.*


class ReceiptActivity : AppCompatActivity() {

    private lateinit var adapter: CartRecyclerViewAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        adapter = CartRecyclerViewAdapter(this, HomeFragment.cartItems){

        }
        rvReceipt.adapter = adapter
        rvReceipt.layoutManager = LinearLayoutManager(this)

        tvReceiptMerchantName.text = getMerchant(this).toUpperCase(Locale.ENGLISH)
        tvReceiptDate.text = transDate
        tvReceiptTransCode.text = "Receipt: #${receiptFormat(transCode)}"

        btnReceiptShare.onClick {
            btnReceiptShare.startAnimation(normalClickAnimation())
            pbReceipt.visibility = View.VISIBLE
            layoutReceipt.alpha = 0.3F

            val bitmap = getScreenShot(layoutReceipt)
            store(bitmap,"#${receiptFormat(transCode)}")

            pbReceipt.visibility = View.GONE
            layoutReceipt.alpha = 1F
        }
    }

    override fun onStart() {
        super.onStart()

        val discount = HomeFragment.totalPrice - DiscountActivity.newTotal
        var totalPayment = 0
        totalPayment = if (DiscountActivity.newTotal != 0)
            0
        else
            HomeFragment.totalPrice

        if (DiscountActivity.newTotal != 0){
            tvReceiptDiscount.text = "${indonesiaCurrencyFormat().format(discount)}"

            tvReceiptSubTotal.text ="${indonesiaCurrencyFormat().format(HomeFragment.totalPrice)}"
            tvReceiptSubTotal.visibility = View.VISIBLE
        }
        else
            tvReceiptDiscount.visibility = View.GONE

        if (note != "")
            tvReceiptNote.text = "Note: $note"

        if (DiscountActivity.newTotal == 0)
            tvReceiptTotal.text = indonesiaCurrencyFormat().format(totalPayment)
        else
            tvReceiptTotal.text = indonesiaCurrencyFormat().format(DiscountActivity.newTotal)

        tvReceiptAmountReceived.text = indonesiaCurrencyFormat().format(totalReceived)

        if (totalReceived >= totalPayment)
            tvReceiptChanges.text = indonesiaCurrencyFormat().format(totalReceived-totalPayment)
        else{
            tvReceiptAmountReceivedTitle.text = "Pending:"
            tvReceiptChanges.text = indonesiaCurrencyFormat().format(totalPayment-totalReceived)
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
            toast(e.message.toString())
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
}
