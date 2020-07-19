package com.chcreation.pointofsale

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import com.chcreation.pointofsale.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.startActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val timer = object: CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {

                if (mAuth.currentUser == null)
                    startActivity<LoginActivity>()
                else
                    startActivity<HomeActivity>()

                finish()

                overridePendingTransition(
                    R.anim.fade_in,
                    R.anim.fade_out
                )
            }
        }

    }
}
