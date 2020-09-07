package com.chcreation.pointofsale.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.UserAcceptance
import com.chcreation.pointofsale.presenter.UserPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_add_user.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import java.util.*

class AddUserActivity : AppCompatActivity(), MainView, AdapterView.OnItemSelectedListener {

    private lateinit var spAdapter: ArrayAdapter<String>
    private var selectedUserGroup = 0
    private var userGroupItems = arrayListOf<String>()
    private lateinit var presenter: UserPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)

        supportActionBar?.title = "Add User"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = UserPresenter(this,mAuth,mDatabase,this)

        userGroupItems = arrayListOf<String>(EUserGroup.MANAGER.toString(),EUserGroup.WAITER.toString())

        spAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,userGroupItems)
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spAddUser.adapter = spAdapter
        spAddUser.onItemSelectedListener = this
        spAddUser.gravity = Gravity.CENTER

        btnAddUser.onClick {
            btnAddUser.startAnimation(normalClickAnimation())

            val email = etAddUserEmail.text.toString()
            if (email.isEmpty())
                toast("Email Must be Fill")
            else{
                alert("Add $email as ${userGroupItems[selectedUserGroup]} ?") {
                    title = "Confirmation"
                    yesButton {
                        presenter.inviteUser(UserAcceptance(getMerchantCredential(this@AddUserActivity),
                            getMerchant(this@AddUserActivity),userGroupItems[selectedUserGroup],
                            dateFormat().format(Date())),encodeEmail(email))
                    }
                    noButton {

                    }
                }.show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedUserGroup = position
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {

    }

    override fun response(message: String) {
        if (message == EMessageResult.CREATE_INVITATION_SUCCESS.toString()){
            toast("Invite Success")
            finish()
        }
    }
}
