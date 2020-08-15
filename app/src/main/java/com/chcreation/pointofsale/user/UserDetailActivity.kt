package com.chcreation.pointofsale.user

import android.content.Context
import android.content.SharedPreferences
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
import java.util.*

class UserDetailActivity : AppCompatActivity(), MainView {

    private lateinit var spAdapter: ArrayAdapter<String>
    private var selectedUserGroup = 0
    private var userGroupItems = arrayListOf<String>()
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: AnalyticPresenter
    private lateinit var sharedPreference: SharedPreferences
    private var currentUser = User()
    private var isEnabled = true

    companion object{
        var user: UserList = UserList()
        var userName = ""
        var size = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)

        supportActionBar?.title = userName

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = AnalyticPresenter(this,mAuth,mDatabase,this)
        sharedPreference =  this.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

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
            //etUserDetailEmail.setText(getEmail(this))
            //etUserDetailName.setText(getName(this))

            btnUserDetailRemove.visibility = View.GONE
        }
        GlobalScope.launch {
            presenter.retrieveUser(user.USER_CODE.toString())
        }

        val index = userGroupItems.indexOf(user.USER_GROUP)
        spUserDetail.setSelection(index)
        selectedUserGroup = index
    }

    override fun onStart() {
        super.onStart()

        btnUserDetailEdit.onClick {
            btnUserDetailEdit.startAnimation(normalClickAnimation())


            if (selectedUserGroup == 1 && size == 1){
                toast("Manager Must be at Least 1 Person !!")
                spUserDetail.setSelection(0)
            }
            else if (etUserDetailName.text.toString() == "")
                toast("Name Must be Filled !!")
            else if (getMerchantUserGroup(this@UserDetailActivity) == EUserGroup.WAITER.toString()
                && user.USER_CODE != mAuth.currentUser!!.uid)
                toast("Only Manager Can Modify User")
            else if (getMerchantUserGroup(this@UserDetailActivity) == EUserGroup.WAITER.toString()
                && user.USER_CODE == mAuth.currentUser!!.uid && user.USER_GROUP != userGroupItems[selectedUserGroup])
                toast("Only Manager Can Modify User Group")
            else{
                alert ("Are You Sure Want to Update ?"){
                    title = "Update"
                    yesButton {
                        pbuserDetail.visibility = View.VISIBLE
                        GlobalScope.launch {

                            if (user.USER_CODE == mAuth.currentUser!!.uid)
                                presenter.updateUser(User(etUserDetailName.text.toString(),currentUser.EMAIL,currentUser.CREATED_DATE,
                                    dateFormat().format(Date()))){
                                    if (it){
                                        val editor = sharedPreference.edit()
                                        editor.putString(ESharedPreference.NAME.toString(), etUserDetailName.text.toString())
                                        editor.apply()
                                        toast("Update User Name Success")
                                        supportActionBar?.title = etUserDetailName.text.toString()
                                        pbuserDetail.visibility = View.GONE
                                        pbuserDetail.visibility = View.GONE
                                    }
                                }

                            if (getMerchantUserGroup(this@UserDetailActivity) == EUserGroup.MANAGER.toString()  && (size > 1 || userGroupItems[selectedUserGroup] == EUserGroup.MANAGER.toString()))
                                presenter.updateUserList(user.USER_CODE.toString(),userGroupItems[selectedUserGroup])

                        }
                    }
                    noButton {

                    }
                }.show()

            }
        }

        btnUserDetailRemove.onClick {
            btnUserDetailRemove.startAnimation(normalClickAnimation())

            if (getMerchantUserGroup(this@UserDetailActivity) == EUserGroup.WAITER.toString())
                toast("Only Manager Can Remove User")
            else if (user.USER_GROUP== EUserGroup.MANAGER.toString())
                toast("Cannot Remove Manager from User List")
            else{
                alert ("Are You Sure Want to Remove ?"){
                    title = "Remove"
                    yesButton {
                        pbuserDetail.visibility = View.VISIBLE

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
        size = 0
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
            pbuserDetail.visibility = View.GONE
        }
        if (message == EMessageResult.UPDATE.toString()){
            if (user.USER_CODE == mAuth.currentUser!!.uid){
                val editor = sharedPreference.edit()
                editor.putString(ESharedPreference.USER_GROUP.toString(), userGroupItems[selectedUserGroup])
                editor.apply()
            }
            toast("Update Success")
            finish()
            pbuserDetail.visibility = View.GONE
        }
    }
}
