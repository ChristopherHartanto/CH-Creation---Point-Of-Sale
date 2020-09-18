package com.chcreation.pointofsale.user

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.checkout.PostCheckOutActivity

import com.chcreation.pointofsale.login.LoginActivity
import com.chcreation.pointofsale.model.Cat
import com.chcreation.pointofsale.model.User
import com.chcreation.pointofsale.model.UserList
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.presenter.UserPresenter
import com.chcreation.pointofsale.user.UserDetailActivity.Companion.user
import com.chcreation.pointofsale.user.UserDetailActivity.Companion.userName
import com.chcreation.pointofsale.user.UserDetailActivity.Companion.size
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
import org.jetbrains.anko.alert
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

        adapter = UserListRecyclerViewAdapter(ctx,userNames,userGroups){ rvIt ->
            user = userGroups[rvIt]
            userName = userNames[rvIt]
            if (userName != "" && user.USER_CODE != ""){
                for (data in userGroups){
                    if (data.USER_GROUP == EUserGroup.MANAGER.toString())
                        size++
                }
                startActivity<UserDetailActivity>()
            }else{
                srUserList.isRefreshing = true
                presenter.retrieveUserLists()
            }
        }

        srUserList.onRefresh {
            presenter.retrieveUserLists()
        }

        fbUserList.onClick {
            if (getMerchantUserGroup(ctx) == EUserGroup.WAITER.toString())
                toast("Only Manager Can Invite User")
            else{
                if (userGroups.size > 2){
                    alert ("Upgrade to Premium for Unlimited User"){
                        title = "Oops!"
                        yesButton {
                            sendEmail("Upgrade Premium",
                                "Merchant: ${getMerchantName(ctx)}",ctx)
                        }

                        noButton {  }
                    }.show()
                }
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
                    PostCheckOutActivity().clearCartData()
                    startActivity<LoginActivity>()
                    requireActivity().finish()
                }
                noButton {

                }
            }.show()
        }
        rvUserList.adapter = adapter
        rvUserList.layoutManager = LinearLayoutManager(ctx)
        presenter.retrieveUserLists()
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (isVisible && isResumed){
            if (response == EMessageResult.FETCH_USER_LIST_SUCCESS.toString()  && isVisible && isResumed){
                if(dataSnapshot.exists() && dataSnapshot.value != null && dataSnapshot.value != ""){
                    userGroups.clear()
                    userNames.clear()
                    val gson = Gson()
                    val arrayUserListType = object : TypeToken<MutableList<UserList>>() {}.type
                    val items : MutableList<UserList> = gson.fromJson(dataSnapshot.value.toString(),arrayUserListType)

                    items.sortBy { it.USER_GROUP }

                    for (data in items){
                        if (data.STATUS_CODE == EStatusCode.ACTIVE.toString()){
                            userGroups.add(data)
                            userNames.add("")
                        }
                    }
                    for ((index,data) in userGroups.withIndex()){
                        presenter.retrieveUser(data.USER_CODE.toString(),index){success, key, user ->
                            if (success){
                                userNames.add(key,user.NAME.toString())
                                adapter.notifyDataSetChanged()
                                pbUserList.visibility = View.GONE
                                srUserList.isRefreshing = false
//                                if (userGroups.size == userNames.size){
//                                    adapter.notifyDataSetChanged()
//                                    pbUserList.visibility = View.GONE
//                                    srUserList.isRefreshing = false
//                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun response(message: String) {
    }

}
