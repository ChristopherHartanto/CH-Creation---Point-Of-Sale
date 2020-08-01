package com.chcreation.pointofsale.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.merchant.ManageMerchant
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

class SignUpActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

    }

    override fun onStart() {
        super.onStart()

        btnSignUp.onClick {
            registerUser()
        }
    }

    private fun registerUser () {

        val email = etSignUpEmail.text.toString()
        val name = etSignUpName.text.toString()
        val password = etSignUpPassword.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, OnCompleteListener { task ->
                if (task.isSuccessful) {
                    startActivity<ManageMerchant>()
                    finish()
                    Toast.makeText(this, "Successfully registered ", Toast.LENGTH_LONG).show()
                }else {
                    Toast.makeText(this, "Error registering, try again later ", Toast.LENGTH_LONG).show()
                }
            })
        }else {
            etSignUpEmail.setText("")
            etSignUpPassword.setText("")
            Toast.makeText(this,"Please fill up the Credentials", Toast.LENGTH_LONG).show()
        }
    }
}
