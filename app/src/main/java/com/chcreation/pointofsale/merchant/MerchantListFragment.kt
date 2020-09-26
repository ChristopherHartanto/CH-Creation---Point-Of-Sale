package com.chcreation.pointofsale.merchant

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.checkout.PostCheckOutActivity

import com.chcreation.pointofsale.model.AvailableMerchant
import com.chcreation.pointofsale.model.Merchant
import com.chcreation.pointofsale.model.UserList
import com.chcreation.pointofsale.presenter.MerchantPresenter
import com.chcreation.pointofsale.presenter.UserPresenter
import com.chcreation.pointofsale.user.UserListRecyclerViewAdapter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_manage_merchant.*
import kotlinx.android.synthetic.main.fragment_merchant_list.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.yesButton

/**
 * A simple [Fragment] subclass.
 */
class MerchantListFragment : Fragment(), MainView {

    private lateinit var adapter: MerchantListRecyclerViewAdapter
    private lateinit var presenter: MerchantPresenter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var sharedPreference: SharedPreferences
    private var merchantItems: MutableList<Merchant> = mutableListOf()
    private var availableMerchantItems: MutableList<AvailableMerchant> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_merchant_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = MerchantPresenter(this,mAuth,mDatabase,ctx)
        sharedPreference =  ctx.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)

        adapter = MerchantListRecyclerViewAdapter(ctx,merchantItems){
            if (merchantItems.elementAtOrNull(it) != null){
                if (merchantItems[it].NAME == getMerchantName(ctx) &&
                    availableMerchantItems[it].CREDENTIAL == getMerchantCredential(ctx) )
                    alert ("Cannot Switch to Same Merchant!"){
                        title = "Switch Merchant"
                        yesButton {  }
                    }.show()
                else
                    alert ("Switch to ${merchantItems[it].NAME} ?"){
                        title = "Switch"
                        yesButton {a->
                            removeMerchantSharedPreference(ctx)
                            PostCheckOutActivity().clearCartData()
                            val editor = sharedPreference.edit()
                            editor.putString(ESharedPreference.USER_GROUP.toString(), availableMerchantItems[it].USER_GROUP.toString())
                            editor.putString(ESharedPreference.ADDRESS.toString(),merchantItems[it].ADDRESS)
                            editor.putString(ESharedPreference.NO_TELP.toString(),merchantItems[it].NO_TELP)
                            editor.putString(ESharedPreference.MERCHANT_NAME.toString(),merchantItems[it].NAME)
                            editor.putString(ESharedPreference.MERCHANT_MEMBER_STATUS.toString(),merchantItems[it].MEMBER_STATUS)
                            editor.putString(ESharedPreference.MERCHANT_CREDENTIAL.toString(),availableMerchantItems[it].CREDENTIAL)
                            editor.putString(ESharedPreference.MERCHANT_IMAGE.toString(),merchantItems[it].IMAGE)
                            editor.putString(ESharedPreference.MERCHANT_CODE.toString()
                                ,if(merchantItems[it].MERCHANT_CODE == "") merchantItems[it].NAME else merchantItems[it].MERCHANT_CODE)
                            editor.putString(ESharedPreference.COUNTRY.toString(),merchantItems[it].COUNTRY)
                            editor.putString(ESharedPreference.LANGUAGE.toString(),merchantItems[it].LANGUAGE)
                            editor.apply()

                            ctx.startActivity<MainActivity>()
                            requireActivity().finish()
                            toast("Switched to Merchant ${merchantItems[it].NAME}")
                        }
                        noButton {

                        }
                    }.show()
            }else{
                toast("Refreshing Data")
                loading()
                GlobalScope.launch {
                    presenter.retrieveMerchants()
                }
            }
        }

        rvMerchantList.layoutManager = GridLayoutManager(ctx,2)
        rvMerchantList.adapter = adapter

        fbMerchantList.onClick {
            fbMerchantList.startAnimation(normalClickAnimation())
            if (getMerchantMemberStatus(ctx) == EMerchantMemberStatus.FREE_TRIAL.toString()){
                alert ("Upgrade to Premium for Unlimited Merchant"){
                    title = "Oops!"
                    yesButton {
                        sendEmail("Upgrade Premium",
                            "Merchant: ${getMerchantName(ctx)}",ctx)
                    }

                    noButton {  }
                }.show()
            }
            else{
                alert ("You Will Logout From This Merchant Before Create A New One,Continue?"){
                    title = "Create New Merchant"
                    yesButton {
                        removeMerchantSharedPreference(ctx)
                        ctx.startActivity<ManageMerchantActivity>()
                        requireActivity().finish()
                    }

                    noButton {  }
                }.show()
            }
        }

        srMerchantList.onRefresh {
            GlobalScope.launch {
                presenter.retrieveMerchants()
            }
        }

        GlobalScope.launch {
            presenter.retrieveMerchants()
        }
    }

    private fun loading(){
        srMerchantList.isRefreshing = false
        pbMerchantList.visibility = View.GONE
    }

    private fun endLoading(){
        pbMerchantList.visibility = View.GONE
        srMerchantList.isRefreshing = false
    }


    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (isVisible && isResumed){
            if (response == EMessageResult.FETCH_AVAIL_MERCHANT_SUCCESS.toString()){
                if (dataSnapshot.exists()){
                    merchantItems.clear()
                    availableMerchantItems.clear()
                    for (data in dataSnapshot.children) {
                        val item = data.getValue(AvailableMerchant::class.java)
                        if (item!!.STATUS == EStatusCode.ACTIVE.toString())
                            availableMerchantItems.add(item)
                    }

                    for ((index,data) in availableMerchantItems.withIndex()){
                        val merchantCode = if (data.MERCHANT_CODE == "") data.NAME else data.MERCHANT_CODE
                        presenter.retrieveCurrentMerchant(data.CREDENTIAL.toString(),
                            merchantCode.toString(),index){success, merchant, key ->
                            if (success){
                                merchantItems.add(key, merchant!!)
                                adapter.notifyDataSetChanged()
                            }
                            if (key == merchantItems.size-1){
                                endLoading()
                            }
                        }
                    }
                }else
                    endLoading()
            }
            endLoading()
        }
    }

    override fun response(message: String) {

    }

}
