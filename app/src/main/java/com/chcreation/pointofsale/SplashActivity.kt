package com.chcreation.pointofsale

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.chcreation.pointofsale.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_splash.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.ctx

class SplashActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var sharedPreference: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //pbSplash.visibility = View.VISIBLE

        supportActionBar!!.hide()
        mAuth = FirebaseAuth.getInstance()

        val version = packageManager.getPackageInfo(packageName,0).versionName
        tvsplashVersion.text = version

        val timer = object: CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {

                if (mAuth.currentUser == null || getMerchant(this@SplashActivity) == "")
                    startActivity<LoginActivity>()
                else
                    startActivity<MainActivity>()

                finish()

                overridePendingTransition(
                    R.anim.fade_in,
                    R.anim.fade_out
                )
            }
        }
        timer.start()
    }
}
