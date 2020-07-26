package com.chcreation.pointofsale

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_error.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import kotlin.system.exitProcess


class ErrorActivity : AppCompatActivity() {

    companion object{
        var errorMessage = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)

        supportActionBar?.hide()

        tvErrorMessage.text = errorMessage

        btnErrorExit.onClick {
            errorMessage = ""
            setResult(RESULT_CLOSE_ALL)
            exitProcess(0)
        }

        btnErrorRelaunch.onClick {
            errorMessage = ""
            val intent = Intent(applicationContext, SplashActivity::class.java)
            val mPendingIntentId: Int = 1
            val mPendingIntent = PendingIntent.getActivity(
                applicationContext,
                mPendingIntentId,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
            val mgr =
                applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            mgr[AlarmManager.RTC, System.currentTimeMillis() + 50] = mPendingIntent
            exitProcess(0)
        }
    }
}
