package com.chcreation.pointofsale.custom_receipt

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chcreation.pointofsale.*
import kotlinx.android.synthetic.main.fragment_custom_receipt.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.intentFor

/**
 * A simple [Fragment] subclass.
 */
class CustomReceiptFragment : Fragment() {

    private lateinit var sharedPreference: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_custom_receipt, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreference =  ctx.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE)
        layoutCustomReceipt1.onClick {
            layoutCustomReceipt1.startAnimation(normalClickAnimation())

            startActivity(intentFor<CustomReceiptActivity>(ESharedPreference.CUSTOM_RECEIPT.toString() to ECustomReceipt.RECEIPT1.toString()))
        }

        layoutCustomReceipt2.onClick {
            layoutCustomReceipt2.startAnimation(normalClickAnimation())

            startActivity(intentFor<CustomReceiptActivity>(ESharedPreference.CUSTOM_RECEIPT.toString() to ECustomReceipt.RECEIPT2.toString()))
        }

    }

    override fun onResume() {
        super.onResume()

        val default = getMerchantReceiptTemplate(ctx)
        if (default == ECustomReceipt.RECEIPT1.toString()){
            tvCustomReceiptDefault1.visibility = View.VISIBLE
            tvCustomReceiptDefault2.visibility = View.GONE
        }
        else if (default == ECustomReceipt.RECEIPT2.toString()){
            tvCustomReceiptDefault1.visibility = View.GONE
            tvCustomReceiptDefault2.visibility = View.VISIBLE
        }
    }

}
