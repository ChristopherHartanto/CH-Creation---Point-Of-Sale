package com.chcreation.pointofsale

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_about.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.ctx

/**
 * A simple [Fragment] subclass.
 */
class AboutFragment : Fragment(), MainView {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: Homepresenter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onStart() {
        super.onStart()

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = Homepresenter(this,mAuth,mDatabase,ctx)

        val version = ctx.packageManager.getPackageInfo(ctx.packageName,0).versionName
        tvAboutVersion.text = version

        presenter.retrieveAbout(){
            if (isVisible && isResumed){
                tvAboutPrivacyPolicy.text = it.PRIVACY_POLICY
                tvAboutTermsCondition.text = it.TERMS_CONDITION
                tvAboutCreatorDesc.text = it.CREATOR_DESC
                tvAboutAppDesc.text = it.APP_DESC

                if (it.IMAGE.toString() != "")
                    Glide.with(ctx).load(it.IMAGE.toString()).into(ivAbout)

                if (it.TERMS_CONDITION == ""){
                    tvAboutTermsConditionTitle.visibility = View.GONE
                }
                if (it.PRIVACY_POLICY == ""){
                    tvAboutPrivacyPolicyTitle.visibility = View.GONE
                }
            }
        }

        tvAboutTermsCondition.onClick {
            openWebsite(tvAboutTermsCondition.text.toString())
        }

        tvAboutPrivacyPolicy.onClick {
            openWebsite(tvAboutPrivacyPolicy.text.toString())
        }
    }

    private fun openWebsite(url: String){
        try {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            requireActivity().startActivity(i)
        }catch (e:Exception){
            e.printStackTrace()
            showError(ctx,e.message.toString())
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {

    }

    override fun response(message: String) {

    }
}
