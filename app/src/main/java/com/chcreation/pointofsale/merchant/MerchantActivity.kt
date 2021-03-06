package com.chcreation.pointofsale.merchant

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.login.LoginActivity
import com.chcreation.pointofsale.model.AvailableMerchant
import com.chcreation.pointofsale.model.Merchant
import com.chcreation.pointofsale.presenter.MerchantPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_merchant.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class MerchantActivity : AppCompatActivity() , MainView, AdapterView.OnItemSelectedListener {

    private lateinit var sharedPreference: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: MerchantPresenter
    private var selectedMerchant = 0
    private var merchantItems : MutableList<AvailableMerchant> = mutableListOf()
    private var merchantNameItems : MutableList<String> = mutableListOf()
    private var merchantUserGroupItems : MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merchant)

        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = MerchantPresenter(this,mAuth,mDatabase, this)
        sharedPreference =  this.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

        GlobalScope.launch {
            presenter.retrieveMerchants()
        }
    }

    override fun onStart() {
        super.onStart()

        btnMerchant.onClick {
            pbMerchant.visibility = View.VISIBLE
            btnMerchant.startAnimation(normalClickAnimation())
            val merchantCode = if(merchantItems[selectedMerchant].MERCHANT_CODE == "") merchantItems[selectedMerchant].NAME.toString()
                                    else merchantItems[selectedMerchant].MERCHANT_CODE.toString()
            presenter.retrieveMerchantInfo(merchantItems[selectedMerchant].CREDENTIAL.toString(),merchantCode)
        }
    }

    override fun onBackPressed() {
        alert("Are You Want to Log Out?"){
            title = "Log Out"
            yesButton {
                startActivity<LoginActivity>()
                finish()
            }
            noButton {

            }
        }.show()
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_AVAIL_MERCHANT_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                merchantItems.clear()
                merchantUserGroupItems.clear()
                merchantNameItems.clear()
                for (data in dataSnapshot.children) {
                    val item = data.getValue(AvailableMerchant::class.java)
                    if (item!!.STATUS == EStatusCode.ACTIVE.toString())
                        merchantItems.add(item)
                }
                for (data in merchantItems){
                    merchantNameItems.add("${data.NAME} / ${data.USER_GROUP}")
                }

                val spAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,merchantNameItems)
                spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                spMerchant.adapter = spAdapter
                spMerchant.onItemSelectedListener = this
                spMerchant.gravity = Gravity.CENTER

                //checking name region
                btnMerchant.isEnabled = false
                for ((index,data) in merchantItems.withIndex()){
                    val merchantCode = if (data.MERCHANT_CODE == "") data.NAME else data.MERCHANT_CODE
                    presenter.getMerchantName(data.CREDENTIAL.toString(),
                        merchantCode.toString(),index){success, merchantName, key ->
                        if (success){
                            if (merchantName != merchantItems[key].NAME){
                                merchantItems[key].NAME = merchantName
                                merchantNameItems[key] = "$merchantName / ${merchantItems[key].USER_GROUP}"
                                spAdapter.notifyDataSetChanged()
                            }
                        }
                        if (key == merchantItems.size-1){
                            pbMerchant.visibility = View.GONE
                            btnMerchant.isEnabled = true
                        }
                    }
                }
                //checking name endregion
            }else
                pbMerchant.visibility = View.GONE
        }else if (response == EMessageResult.FETCH_MERCHANT_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                val item = dataSnapshot.getValue(Merchant::class.java)
                editor = sharedPreference.edit()
                editor.putString(ESharedPreference.USER_GROUP.toString(), merchantItems[selectedMerchant].USER_GROUP.toString())
                editor.putString(ESharedPreference.MERCHANT_CREDENTIAL.toString(), merchantItems[selectedMerchant].CREDENTIAL.toString())
                editor.putString(ESharedPreference.MERCHANT_IMAGE.toString(), item!!.IMAGE)
                editor.putString(ESharedPreference.MERCHANT_CODE.toString(), if (item.MERCHANT_CODE == "") item.NAME else item.MERCHANT_CODE)
                editor.putString(ESharedPreference.MERCHANT_NAME.toString(), item.NAME)
                editor.putString(ESharedPreference.ADDRESS.toString(), item.ADDRESS)
                editor.putString(ESharedPreference.NO_TELP.toString(), item.NO_TELP)
                editor.putString(ESharedPreference.MERCHANT_MEMBER_STATUS.toString(),item.MEMBER_STATUS.toString())
                editor.putString(ESharedPreference.COUNTRY.toString(),item.COUNTRY)
                editor.putString(ESharedPreference.LANGUAGE.toString(),item.LANGUAGE)
                editor.apply()

                startActivity<MainActivity>()
                finish()
            }else
                toast("Error")
            pbMerchant.visibility = View.GONE
        }
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedMerchant = position
    }
}
