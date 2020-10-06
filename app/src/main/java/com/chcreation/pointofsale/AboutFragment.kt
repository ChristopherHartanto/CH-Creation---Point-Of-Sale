package com.chcreation.pointofsale

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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

        Glide.with(context).load("https://instagram.fkno3-1.fna.fbcdn.net/v/t51.2885-19/s150x150/69240049_2456550631286949_8789505908175536128_n.jpg?_nc_ht=instagram.fkno3-1.fna.fbcdn.net&_nc_cat=109&_nc_ohc=Stf4pvmPhwgAX8RgUBq&oh=34737d8a494e6f006525c88016a89764&oe=5FA28AD6").listener(object :
            RequestListener<String, GlideDrawable> {
            override fun onException(
                e: java.lang.Exception?,
                model: String?,
                target: Target<GlideDrawable>?,
                isFirstResource: Boolean
            ): Boolean {
                pbAboutCustomer1.visibility = View.GONE
                return false
            }

            override fun onResourceReady(
                resource: GlideDrawable?,
                model: String?,
                target: Target<GlideDrawable>?,
                isFromMemoryCache: Boolean,
                isFirstResource: Boolean
            ): Boolean {
                pbAboutCustomer1.visibility = View.GONE
                return false
            }

        }).into(ivAboutCustomer1)
        ivAboutCustomer1.onClick {
            openWebsite("https://instagram.com/william_hartanto999?igshid=1sp4iu6eo925c")
        }

        Glide.with(context).load("https://instagram.fkno3-1.fna.fbcdn.net/v/t51.2885-19/s150x150/118308208_854890601710435_8773245668964029193_n.jpg?_nc_ht=instagram.fkno3-1.fna.fbcdn.net&_nc_cat=107&_nc_ohc=mq6G_n5d7G4AX8H4crt&oh=ac91c744eb71a8aba00dade7ec35f8f7&oe=5FA4666E").listener(object :
            RequestListener<String, GlideDrawable> {
            override fun onException(
                e: java.lang.Exception?,
                model: String?,
                target: Target<GlideDrawable>?,
                isFirstResource: Boolean
            ): Boolean {
                pbAboutCustomer2.visibility = View.GONE
                return false
            }

            override fun onResourceReady(
                resource: GlideDrawable?,
                model: String?,
                target: Target<GlideDrawable>?,
                isFromMemoryCache: Boolean,
                isFirstResource: Boolean
            ): Boolean {
                pbAboutCustomer2.visibility = View.GONE
                return false
            }

        }).into(ivAboutCustomer2)
        ivAboutCustomer2.onClick {
            openWebsite("https://instagram.com/mrbearbaketory.id?igshid=a5757t5rl2ze")
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
