package com.chcreation.pointofsale.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.chcreation.pointofsale.HomeActivity
import com.chcreation.pointofsale.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

class SignInActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

    }

    override fun onStart() {
        super.onStart()

        btnSignIn.onClick {
            registerUser()
        }
    }

    private fun registerUser () {

        var email = etSignInEmail.text.toString()
        var password = etSignInPassword.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, OnCompleteListener { task ->
                if (task.isSuccessful) {
                    startActivity<HomeActivity>()
                    finish()
                    Toast.makeText(this, "Successfully registered ", Toast.LENGTH_LONG).show()
                }else {
                    Toast.makeText(this, "Error registering, try again later ", Toast.LENGTH_LONG).show()
                }
            })
        }else {
            etSignInEmail.setText("")
            etSignInPassword.setText("")
            Toast.makeText(this,"Please fill up the Credentials", Toast.LENGTH_LONG).show()
        }
    }
}
