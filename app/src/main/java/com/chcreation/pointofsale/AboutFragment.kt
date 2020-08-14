package com.chcreation.pointofsale

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

        presenter.retrieveAbout(){
            tvAboutHeader1.text = it.TEXT1
            tvAboutHeader2.text = it.TEXT2
            tvAboutHeader3.text = it.TEXT3

            if (it.IMAGE.toString() != "")
                Glide.with(ctx).load(it.IMAGE).into(ivAbout)
        }
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {

    }

    override fun response(message: String) {

    }
}
