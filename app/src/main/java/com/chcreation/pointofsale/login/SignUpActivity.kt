package com.chcreation.pointofsale.login

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.chcreation.pointofsale.EMessageResult
import com.chcreation.pointofsale.ESharedPreference
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.merchant.ManageMerchantActivity
import com.chcreation.pointofsale.normalClickAnimation
import com.chcreation.pointofsale.presenter.MerchantPresenter
import com.chcreation.pointofsale.presenter.UserPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.*

class SignUpActivity : AppCompatActivity(), MainView {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: UserPresenter
    private lateinit var sharedPreference: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = UserPresenter(this,mAuth,mDatabase,this)
        sharedPreference =  this.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

    }

    override fun onStart() {
        super.onStart()

        btnSignUp.onClick {
            btnSignUp.startAnimation(normalClickAnimation())
            btnSignUp.isEnabled = false
            registerUser()
        }
    }

    override fun onBackPressed() {
        startActivity<LoginActivity>()
        finish()
    }

    private fun registerUser () {

        val email = etSignUpEmail.text.toString()
        val name = etSignUpName.text.toString()
        val password = etSignUpPassword.text.toString()

        if (password.length < 6){
            etSignUpPassword.error = "Password Minimum 6 Length !!"
            btnSignUp.isEnabled = true
            pbSignUp.visibility = View.GONE
        }
        else if (name.isEmpty()){
            etSignUpName.error = "Name Must be Fill !!"
            btnSignUp.isEnabled = true
            pbSignUp.visibility = View.GONE
        }
        else if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
            pbSignUp.visibility = View.VISIBLE
            val instanceId = UUID.randomUUID().toString()
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, OnCompleteListener { task ->
                if (task.isSuccessful) {
                    val editor = sharedPreference.edit()
                    editor.putString(ESharedPreference.NAME.toString(),name)
                    editor.putString(ESharedPreference.EMAIL.toString(), email)
                    editor.putString(ESharedPreference.DEVICE_ID.toString(), instanceId)
                    editor.apply()
                    presenter.saveUser(name,email,instanceId)
                }else {
                    btnSignUp.isEnabled = true
                    pbSignUp.visibility = View.GONE
                    Toast.makeText(this, "Error registering, try again later ", Toast.LENGTH_LONG).show()
                }
            })
        }else {
            etSignUpEmail.setText("")
            etSignUpPassword.setText("")
            Toast.makeText(this,"Please fill up the Credentials", Toast.LENGTH_LONG).show()
            btnSignUp.isEnabled = true
            pbSignUp.visibility = View.GONE
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {

    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString()){
            btnSignUp.isEnabled = true
            pbSignUp.visibility = View.GONE

            startActivity<ManageMerchantActivity>()
            finish()
            Toast.makeText(this, "Successfully registered ", Toast.LENGTH_LONG).show()
        }
    }
}
