package com.chcreation.pointofsale.customer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.getMerchant
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.normalClickAnimation
import com.chcreation.pointofsale.presenter.CustomerPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_new_customer.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast

class NewCustomerActivity : AppCompatActivity(), MainView {


    private lateinit var presenter: CustomerPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_customer)

        supportActionBar!!.title = "New Customer"

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = CustomerPresenter(this,mAuth,mDatabase,this)

        btnCustomerSave.onClick {

            btnCustomerSave.startAnimation(normalClickAnimation())

            val email = etCustomerEmail.text.toString()
            val name = etCustomerName.text.toString()
            val phone = etCustomerPhone.text.toString()
            val address = etCustomerAddress.text.toString()
            val note = etCustomerNote.text.toString()

            if (name == ""){
                etCustomerName.error = "Please Fill the Field"
                return@onClick
            }

            presenter.saveCustomer(Customer(name,email,"","",phone,address,note))
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString())
            finish()
        toast(message)
    }
}
