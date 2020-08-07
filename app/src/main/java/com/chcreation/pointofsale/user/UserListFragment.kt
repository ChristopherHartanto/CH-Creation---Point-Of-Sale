package com.chcreation.pointofsale.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.EMessageResult

import com.chcreation.pointofsale.R
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
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.startActivity

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
            startActivity<AddUserActivity>()
        }

    }

    override fun onStart() {
        super.onStart()

        presenter.retrieveUserLists()
        rvUserList.adapter = adapter
        rvUserList.layoutManager = LinearLayoutManager(ctx)
    }


    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_USER_LIST_SUCCESS.toString()){
            if(dataSnapshot.exists() && dataSnapshot.value != null){
                userGroups.clear()
                userNames.clear()
                val gson = Gson()
                val arrayUserListType = object : TypeToken<MutableList<UserList>>() {}.type
                val items : MutableList<UserList> = gson.fromJson(dataSnapshot.value.toString(),arrayUserListType)

                userGroups.addAll(items)
                for (data in userGroups){
                    presenter.retrieveUser(data.USER_CODE.toString())
                }
            }
        }
        if (response == EMessageResult.FETCH_USER_SUCCESS.toString()){
            if(dataSnapshot.exists()){
                val item = dataSnapshot.getValue(User::class.java)
                userNames.add(item!!.NAME.toString())
            }
            if (userGroups.size == userNames.size){
                adapter.notifyDataSetChanged()
                pbUserList.visibility = View.GONE
                srUserList.isRefreshing = false
            }
        }
    }

    override fun response(message: String) {
    }

}
