package com.chcreation.pointofsale.login

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.merchant.MerchantActivity
import com.chcreation.pointofsale.merchant.ManageMerchantActivity
import com.chcreation.pointofsale.model.User
import com.chcreation.pointofsale.presenter.MerchantPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

class SignInActivity : AppCompatActivity(), MainView {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var presenter: MerchantPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = MerchantPresenter(this,mAuth,mDatabase, this)
        sharedPreference =  this.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)
    }

    override fun onStart() {
        super.onStart()

        btnSignIn.onClick {
            btnSignIn.startAnimation(normalClickAnimation())
            login()
        }
    }

    private fun login () {

        val email = etSignInEmail.text.toString()
        val password = etSignInPassword.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, OnCompleteListener { task ->
                if (task.isSuccessful) {
                    GlobalScope.launch(Dispatchers.Main) {
                        presenter.retrieveUserName()
                        presenter.retrieveMerchants()
                    }
                    Toast.makeText(this, "Login Success ", Toast.LENGTH_LONG).show()
                }else {
                    Toast.makeText(this, "Error Success, try again later ", Toast.LENGTH_LONG).show()
                }
            })
        }else {
            etSignInEmail.setText("")
            etSignInPassword.setText("")
            Toast.makeText(this,"Please fill up the Credentials", Toast.LENGTH_LONG).show()
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_AVAIL_MERCHANT_SUCCESS.toString())
        {
            if (dataSnapshot.exists())
            {
                if (sharedPreference.getString("merchant",null) == null)
                    startActivity<MerchantActivity>()
                else
                    startActivity<MainActivity>()
            }
            else
                startActivity<ManageMerchantActivity>()

            finish()
        }
        if (response == EMessageResult.FETCH_USER_SUCCESS.toString()){
            if (dataSnapshot.exists()){

                val item = dataSnapshot.getValue(User::class.java)
                val editor = sharedPreference.edit()
                editor.putString(ESharedPreference.NAME.toString(), item?.NAME)
                editor.apply()
            }
        }
    }

    override fun response(message: String) {
    }
}
