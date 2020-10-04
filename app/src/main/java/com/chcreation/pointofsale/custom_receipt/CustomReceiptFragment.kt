package com.chcreation.pointofsale.custom_receipt

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_custom_receipt.*
import kotlinx.android.synthetic.main.fragment_custom_receipt.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast

/**
 * A simple [Fragment] subclass.
 */
class CustomReceiptFragment : Fragment(),MainView {

    private var sincere = "Thank You"
    private var template = ""
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var presenter: Homepresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_custom_receipt, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreference =  ctx.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        presenter = Homepresenter(this,mAuth,mDatabase,ctx)

        etPrinterDpi.setText(getPrintDpi(ctx).toString())
        etPrinterWidth.setText(getPrintWidth(ctx).toString())
        etPrinterCharLine.setText(getPrintCharLine(ctx).toString())

        cbCustomReceiptCustAddress.isChecked = getMerchantReceiptCustAddress(ctx)
        cbCustomReceiptCustName.isChecked = getMerchantReceiptCustName(ctx)
        cbCustomReceiptCustNoTel.isChecked = getMerchantReceiptCustNoTel(ctx)
        cbCustomReceiptShowMerchantImage.isChecked = getMerchantReceiptImage(ctx)
        cbCustomReceiptShowNote.isChecked = getReceiptNote(ctx)
        cbCustomReceiptShowReceiptNo.isChecked = getReceiptNo(ctx)
        cbCustomReceiptShowDate.isChecked = getReceiptDate(ctx)

        btnCustomReceiptSave.onClick {
            btnCustomReceiptSave.startAnimation(normalClickAnimation())

            val sincere = etCustomReceiptSincere.text.toString()

            val success = presenter.saveSincere(sincere)

            if (success){
                val editor = sharedPreference.edit()
                editor.putString(ESharedPreference.CUSTOM_RECEIPT.toString(),template)
                editor.putString(ESharedPreference.SINCERE.toString(),sincere)

                editor.putInt(ESharedPreference.PRINTER_DPI.toString()
                    ,if (etPrinterDpi.text.toString() == "") 0 else etPrinterDpi.text.toString().toInt())
                editor.putFloat(ESharedPreference.PRINTER_WIDTH.toString()
                    ,if (etPrinterDpi.text.toString() == "") 0F else etPrinterWidth.text.toString().toFloat())
                editor.putInt(ESharedPreference.PRINTER_CHAR_LINE.toString()
                    ,if (etPrinterDpi.text.toString() == "") 0 else etPrinterCharLine.text.toString().toInt())

                editor.putBoolean(ESharedPreference.CUSTOMER_NAME.toString(),cbCustomReceiptCustName.isChecked)
                editor.putBoolean(ESharedPreference.CUSTOMER_ADDRESS.toString(),cbCustomReceiptCustAddress.isChecked)
                editor.putBoolean(ESharedPreference.CUSTOMER_NO_TEL.toString(),cbCustomReceiptCustNoTel.isChecked)
                editor.putBoolean(ESharedPreference.RECEIPT_MERCHANT_ICON.toString(),cbCustomReceiptShowMerchantImage.isChecked)
                editor.putBoolean(ESharedPreference.RECEIPT_NOTE.toString(),cbCustomReceiptShowNote.isChecked)
                editor.putBoolean(ESharedPreference.RECEIPT_NO.toString(),cbCustomReceiptShowReceiptNo.isChecked)
                editor.putBoolean(ESharedPreference.RECEIPT_DATE.toString(),cbCustomReceiptShowDate.isChecked)
                editor.apply()
                toast("Save Success")
            }else
                toast("Failed to Save")
        }

        GlobalScope.launch(Dispatchers.Main){
            val sincere = presenter.getSincere()
            etCustomReceiptSincere.setText(sincere)
        }

    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

data class Sincere(
    var SINCERE: String? = ""
)