package com.chcreation.pointofsale.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.*

import com.chcreation.pointofsale.login.LoginActivity
import com.chcreation.pointofsale.model.Cat
import com.chcreation.pointofsale.model.User
import com.chcreation.pointofsale.model.UserList
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.presenter.UserPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_user_list.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.*
import org.jetbrains.anko.yesButton

class UserListFragment : Fragment(), MainView{

    private lateinit var adapter: UserListRecyclerViewAdapter
    private lateinit var presenter: UserPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private var userGroups: MutableList<UserList> = mutableListOf()
    private var userNames: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = UserPresenter(this,mAuth,mDatabase,ctx)

        adapter = UserListRecyclerViewAdapter(ctx,userNames,userGroups){

        }

        srUserList.onRefresh {
            presenter.retrieveUserLists()
        }

        fbUserList.onClick {
            if (getMerchantUserGroup(ctx) == EUserGroup.WAITER.toString())
                toast("Only Manager Can Invite User")
            else{
                if (userGroups.size > 8)
                    toast("Maximum User 8, Need Contact Administrator to Continue Proceed !!")
                else if (userGroups.size > 0)
                    startActivity<AddUserActivity>()
            }
        }

        btnLogOut.onClick {
            btnLogOut.startAnimation(normalClickAnimation())
            alert ("Are You Want to Log Out?"){
                title = "Log Out"
                yesButton {
                    mAuth.signOut()
                    removeAllSharedPreference(ctx)
                    startActivity<LoginActivity>()
                    requireActivity().finish()
                }
                noButton {

                }
            }.show()
        }
    }

    override fun onStart() {
        super.onStart()

        rvUserList.adapter = adapter
        rvUserList.layoutManager = LinearLayoutManager(ctx)

        presenter.retrieveUserLists()
    }


    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_USER_LIST_SUCCESS.toString()){
            if(dataSnapshot.exists() && dataSnapshot.value != null && dataSnapshot.value != ""){
                userGroups.clear()
                userNames.clear()
                val gson = Gson()
                val arrayUserListType = object : TypeToken<MutableList<UserList>>() {}.type
                val items : MutableList<UserList> = gson.fromJson(dataSnapshot.value.toString(),arrayUserListType)

                items.sortBy { it.USER_GROUP }
                userGroups.addAll(items)
                GlobalScope.launch {
                    for (data in userGroups){
                        presenter.retrieveUser(data.USER_CODE.toString())
                    }
                }
            }
        }
        if (response == EMessageResult.FETCH_USER_SUCCESS.toString()){
            if(dataSnapshot.exists()){
                val item = dataSnapshot.getValue(User::class.java)
                userNames.add(item!!.NAME.toString())
            }
        }
        if (userGroups.size == userNames.size){
            adapter.notifyDataSetChanged()
            pbUserList.visibility = View.GONE
            srUserList.isRefreshing = false
        }
    }

    override fun response(message: String) {
    }

}