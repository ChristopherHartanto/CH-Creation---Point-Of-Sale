package com.chcreation.pointofsale.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.User
import com.chcreation.pointofsale.model.UserList
import com.chcreation.pointofsale.presenter.AnalyticPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_add_user.*
import kotlinx.android.synthetic.main.activity_user_detail.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton

class UserDetailActivity : AppCompatActivity(), MainView {

    private lateinit var spAdapter: ArrayAdapter<String>
    private var selectedUserGroup = 0
    private var userGroupItems = arrayListOf<String>()
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: AnalyticPresenter
    private var currentUser = User()
    private var isEnabled = true

    companion object{
        var user: UserList = UserList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = AnalyticPresenter(this,mAuth,mDatabase,this)

        etUserDetailName.isEnabled = false
        etUserDetailEmail.isEnabled = false

        userGroupItems = arrayListOf<String>(EUserGroup.MANAGER.toString(), EUserGroup.WAITER.toString())

        spAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,userGroupItems)
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spUserDetail.adapter = spAdapter
        spUserDetail.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedUserGroup = position
            }

        }
        spUserDetail.gravity = Gravity.CENTER

        if (user.USER_CODE == mAuth.currentUser!!.uid){
            etUserDetailName.isEnabled = true
            etUserDetailEmail.setText(getEmail(this))
            etUserDetailName.setText(getName(this))
        }
        else{
            GlobalScope.launch {
                presenter.retrieveUser(user.USER_CODE.toString())
            }
        }

        val index = userGroupItems.indexOf(user.USER_GROUP)
        spUserDetail.setSelection(index)
    }

    override fun onStart() {
        super.onStart()

        btnUserDetailRemove.onClick {
            btnUserDetailRemove.startAnimation(normalClickAnimation())

            if (getMerchantUserGroup(this@UserDetailActivity) == EUserGroup.WAITER.toString())
                toast("Only Manager Can Remove User")
            else{
                alert ("Are You Sure Want to Remove ?"){
                    title = "Remove"
                    yesButton {
                        GlobalScope.launch {
                            presenter.removeUserList(user.USER_CODE.toString())
                        }
                    }
                    noButton {

                    }
                }.show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        isEnabled = false
        user = UserList()
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_USER_SUCCESS.toString()){
            if (dataSnapshot.exists()){
                val item = dataSnapshot.getValue(User::class.java)!!
                currentUser = item

                if (isEnabled){
                    etUserDetailName.setText(currentUser.NAME)
                    etUserDetailEmail.setText(currentUser.EMAIL)
                }
            }
        }
    }

    override fun response(message: String) {
        if (message == EMessageResult.SUCCESS.toString()){
            toast("Remove Success")
            finish()
        }
    }
}
