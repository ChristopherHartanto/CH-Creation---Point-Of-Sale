package com.chcreation.pointofsale.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AlphaAnimation
import com.chcreation.pointofsale.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.yesButton

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private val clickAnimation = AlphaAnimation(1.2F,0.6F)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar!!.hide()

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        val version = packageManager.getPackageInfo(packageName,0).versionName
        tvLoginVersion.text = "Version $version"
    }

    override fun onStart() {
        super.onStart()

        btnLoginSignIn.onClick {
            btnLoginSignIn.startAnimation(clickAnimation)
            startActivity<SignInActivity>()

            finish()
        }

        btnLoginSignUp.onClick {
            btnLoginSignUp.startAnimation(clickAnimation)
            startActivity<SignUpActivity>()

            finish()
        }
    }

    override fun onBackPressed() {
        alert ("Are You Want to Exit?"){
            title = "Exit"
            yesButton {
                super.onBackPressed()
            }
            noButton {

            }
        }.show()
    }

}
