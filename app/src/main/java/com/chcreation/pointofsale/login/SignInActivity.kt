package com.chcreation.pointofsale.login

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.StrictMode
import android.provider.Settings.Secure
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.merchant.ManageMerchantActivity
import com.chcreation.pointofsale.merchant.MerchantActivity
import com.chcreation.pointofsale.model.User
import com.chcreation.pointofsale.model.UserAcceptance
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
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.security.AccessController.getContext
import java.util.*


class SignInActivity : AppCompatActivity(), MainView {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var presenter: MerchantPresenter
    private var email = ""
    private var instanceId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

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
            btnSignIn.isEnabled = false
            pbSignIn.visibility = View.VISIBLE

            login()
        }

        tvForgotPassword.onClick {
            tvForgotPassword.startAnimation(normalClickAnimation())
            email = etSignInEmail.text.toString()
            if (email != ""){
                alert ("Reset Password will be Send to $email"){
                    title = "Reset Password"
                    yesButton {
                        mAuth.sendPasswordResetEmail(email).addOnCompleteListener {
                            alert ("Please Check Your Email"){
                                title = "Reset Password"
                                yesButton {  }
                            }.show()
                        }.addOnFailureListener {
                            toast("Failed to Send")
                        }
                    }
                    noButton {  }
                }.show()
            }
            else
                toast("Please Fill Your Email Address")
        }
    }

    override fun onBackPressed() {
       startActivity<LoginActivity>()
        finish()
    }

    private fun login () {

        email = etSignInEmail.text.toString()
        val password = etSignInPassword.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {

//            val gMailSender = GMailSender(EMAIL, PASSWORD)
//            gMailSender.sendMail("Registration Success","Welcome to CH Creation Point of Sale","CHCreation","christopherhartanto999@gmail.com")

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, OnCompleteListener { task ->
                if (task.isSuccessful) {
                    instanceId = UUID.randomUUID().toString()
                    GlobalScope.launch(Dispatchers.Main) {
                        presenter.retrieveUserName()
                        presenter.setDeviceId(instanceId)
                    }

                    Toast.makeText(this, "Login Success ", Toast.LENGTH_LONG).show()
                }else {
                    Toast.makeText(this, "Email or Password Wrong", Toast.LENGTH_LONG).show()
                    btnSignIn.isEnabled = true
                    pbSignIn.visibility = View.GONE
                }
            }).addOnFailureListener {
                Toast.makeText(this, it.message.toString(), Toast.LENGTH_LONG).show()
                btnSignIn.isEnabled = true
                pbSignIn.visibility = View.GONE
            }
        }else {
            etSignInEmail.setText("")
            etSignInPassword.setText("")
            Toast.makeText(this,"Please fill up the Credentials", Toast.LENGTH_LONG).show()
            btnSignIn.isEnabled = true
            pbSignIn.visibility = View.GONE
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_INVITATION_SUCCESS.toString())
        {
            if (dataSnapshot.exists() && dataSnapshot.value != null)
            {
                val item = dataSnapshot.getValue(UserAcceptance::class.java)
                alert ("You're Invite as ${item!!.USER_GROUP} in ${item.NAME}"){
                    title = "Accept"
                    yesButton {
                        presenter.acceptInvitation(encodeEmail(email),item)
                    }
                    noButton {
                        GlobalScope.launch {
                            presenter.removeInvitation(encodeEmail(email))
                            presenter.retrieveMerchants()
                        }
                    }
                }.show()
            }else
                GlobalScope.launch {
                    presenter.retrieveMerchants()
                }
        }
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
                editor.putString(ESharedPreference.EMAIL.toString(), item?.EMAIL)
                editor.putString(ESharedPreference.DEVICE_ID.toString(), instanceId)
                editor.apply()

                if (item != null) {
                    if (item.ACTIVE == EStatusUser.SUSPEND.toString() || item.ACTIVE == EStatusUser.DE_ACTIVE.toString()){
                        val status = if (item.ACTIVE == EStatusUser.SUSPEND.toString()) "Your Account Has Been Suspended"
                        else "Your Account Was Disable by Admin"
                        alert ("$status!\nPlease Contact Administrator for Further Information!"){
                            title = "Error"

                            yesButton {
                                mAuth.signOut()
                                sendEmail("$status - $email","",this@SignInActivity)
                            }
                            noButton { mAuth.signOut() }
                        }.show()
                        pbSignIn.visibility = View.GONE
                        btnSignIn.isEnabled = true
                    }
                    else if (item.ACTIVE == EStatusUser.ACTIVE.toString()){
                        GlobalScope.launch {
                            presenter.retrieveInvitation(encodeEmail(email))
                        }
                    }
                }
            }else{
                alert ("Failed to Retrieve User Data\nPlease Try Again"){
                    title = "Error"

                    yesButton {
                        mAuth.signOut()
                    }
                }.show()
            }
        }

    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString()){
            startActivity<MerchantActivity>()
            finish()
        }else{
            toast(message)
            pbSignIn.visibility = View.GONE
            btnSignIn.isEnabled = true
        }
    }
}
