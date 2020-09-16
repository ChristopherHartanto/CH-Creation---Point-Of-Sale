package com.chcreation.pointofsale.activity_logs

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager

import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.dateFormat
import com.chcreation.pointofsale.model.ActivityLogs
import com.chcreation.pointofsale.parseDateFormat
import com.chcreation.pointofsale.presenter.CheckOutPresenter
import com.chcreation.pointofsale.presenter.Homepresenter
import com.chcreation.pointofsale.presenter.MerchantPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_activity_logs.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.yesButton

/**
 * A simple [Fragment] subclass.
 */
class ActivityLogsFragment : Fragment(), MainView {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var presenter: Homepresenter
    private lateinit var adapter: ActivityLogsRecyclerViewAdapter
    private var logItems = mutableListOf<ActivityLogs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_activity_logs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = Homepresenter(this,mAuth,mDatabase,ctx)
        adapter = ActivityLogsRecyclerViewAdapter(ctx,logItems) {
            logItems[it].CREATED_BY?.let { it1 ->
                presenter.getUserName(it1) { name ->
                    alert("PIC: $name") {
                        title = "Info"
                        yesButton { }
                    }.show()
                }
            }
        }

        rvActivityLogs.apply {
            adapter = this@ActivityLogsFragment.adapter
            layoutManager = LinearLayoutManager(ctx)
        }
    }

    override fun onStart() {
        super.onStart()

        presenter.retrieveActivityLogs()
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (isVisible && isResumed){
            if (dataSnapshot.exists()){
                for (data in dataSnapshot.children){
                    val item = data.getValue(ActivityLogs::class.java)
                    if (item != null){
                        logItems.add(item)
                    }
                }
                logItems.reverse()
                adapter.notifyDataSetChanged()
            }
            pbActivityLogs.visibility = View.GONE
        }
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
