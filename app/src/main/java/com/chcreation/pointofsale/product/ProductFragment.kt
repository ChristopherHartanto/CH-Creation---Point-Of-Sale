package com.chcreation.pointofsale.product

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.EMessageResult

import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.getMerchant
import com.chcreation.pointofsale.presenter.ProductPresenter
import com.chcreation.pointofsale.view.MainView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_product.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.startActivity

/**
 * A simple [Fragment] subclass.
 */
class ProductFragment : Fragment(), MainView {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase : DatabaseReference
    private lateinit var rvAdapter: ProductRecyclerViewAdapter
    private lateinit var presenter: ProductPresenter
    private var items: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product, container, false)
    }

    override fun onStart() {
        super.onStart()

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        presenter = ProductPresenter(this,mAuth,mDatabase,ctx)

        rvAdapter = ProductRecyclerViewAdapter(ctx,items){
            startActivity(intentFor<ListProductActivity>("category" to it))
        }

        rvProductCat.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(ctx)
        }

        fbProduct.onClick {
            ctx.startActivity<NewProductActivity>()
        }

        presenter.retrieveCategories()
    }

    override fun loadData(dataSnapshot: DataSnapshot, response: String) {
        if (response == EMessageResult.FETCH_CATEGORY_SUCCESS.toString()) {
            items.clear()
            items.add("All")
            if (dataSnapshot.exists()) {
                for (data in dataSnapshot.children) {
                    items.add(data.key.toString())
                }
                rvAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun response(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
