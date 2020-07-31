package com.chcreation.pointofsale.merchant

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.AvailableMerchant
import com.chcreation.pointofsale.model.Merchant
import com.chcreation.pointofsale.presenter.MerchantPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_new_merchant.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*

class NewMerchantActivity : AppCompatActivity(), MainView {

    private lateinit var sharedPreference: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: MerchantPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_merchant)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = MerchantPresenter(this,mAuth,mDatabase)
        sharedPreference =  this.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)
    }

    override fun onStart() {
        super.onStart()

        btnNewMerchant.onClick {

            btnNewMerchant.startAnimation(normalClickAnimation())
            btnNewMerchant.isEnabled = false
            pbMerchant.visibility = View.VISIBLE

            val merchantBusinessInfo = etMerchantBusinessInfo.text.toString()
            val merchantNoTelp = etMerchantNoTelp.text.toString()
            val merchantAddress = etMerchantAddress.text.toString()
            val merchantName = etMerchantName.text.toString()
            val currentDate = dateFormat().format(Date())

            if (merchantName == ""){
                toast("Please Fill Merchant Name !")
                return@onClick
            }

            presenter.createNewMerchant(Merchant(merchantName,merchantBusinessInfo,merchantAddress,merchantNoTelp,
                currentDate,currentDate, mAuth.currentUser!!.uid, mAuth.currentUser!!.uid),
                AvailableMerchant(merchantName,EUserGroup.MANAGER.toString(),currentDate,currentDate,
                    mAuth.currentUser!!.uid,EStatusUser.ACTIVE.toString()))
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {

    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString())
        {
            editor = sharedPreference.edit()
            editor.putString(ESharedPreference.MERCHANT.toString(),etMerchantName.text.toString())
            editor.putString(ESharedPreference.USER_GROUP.toString(),EUserGroup.MANAGER.toString())
            editor.putString(ESharedPreference.MERCHANT_CREDENTIAL.toString(), mAuth.currentUser?.uid)
            editor.apply()

            startActivity<MainActivity>()
            finish()
        }
        btnNewMerchant.isEnabled = true
        pbMerchant.visibility = View.GONE
    }
}
