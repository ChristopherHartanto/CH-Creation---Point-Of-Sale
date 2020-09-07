package com.chcreation.pointofsale.custom_receipt

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.chcreation.pointofsale.ECustomReceipt
import com.chcreation.pointofsale.ESharedPreference
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.normalClickAnimation
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_custom_receipt.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.toast

class CustomReceiptActivity : AppCompatActivity(), MainView {

    private var sincere = "Thank You"
    private var template = ""
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var presenter: Homepresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_receipt)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        sharedPreference =  getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)
        presenter = Homepresenter(this,mAuth,mDatabase,ctx)
        supportActionBar?.title = "Receipt Template"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        template = intent.extras!!.getString(ESharedPreference.CUSTOM_RECEIPT.toString()).toString()

        if (template == ECustomReceipt.RECEIPT1.toString())
            ivCustomReceiptImage.imageResource = R.drawable.receipt1
        else if (template == ECustomReceipt.RECEIPT2.toString())
            ivCustomReceiptImage.imageResource = R.drawable.receipt2


        btnCustomReceiptSave.onClick {
            btnCustomReceiptSave.startAnimation(normalClickAnimation())

            if (template == "")
                toast("Please Try Again Later")
            else{
                val sincere = etCustomReceiptSincere.text.toString()
                presenter.saveSincere(sincere){
                    if (it){
                        val editor = sharedPreference.edit()
                        editor.putString(ESharedPreference.CUSTOM_RECEIPT.toString(),template)
                        editor.putString(ESharedPreference.SINCERE.toString(),sincere)
                        editor.apply()
                        toast("Success")
                        finish()
                    }else
                        toast("Failed to Save")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (sharedPreference.getString(ESharedPreference.SINCERE.toString(),"") != "")
            sincere  = sharedPreference.getString(ESharedPreference.SINCERE.toString(),"").toString()

        etCustomReceiptSincere.setText(sincere)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
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