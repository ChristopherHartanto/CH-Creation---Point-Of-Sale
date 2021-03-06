package com.chcreation.pointofsale.product

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chcreation.pointofsale.*

import com.chcreation.pointofsale.product.ManageProductUpdateProductFragment.Companion.product
import kotlinx.android.synthetic.main.fragment_manage_product_update_stock.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast

/**
 * A simple [Fragment] subclass.
 */
class ManageProductUpdateStockFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manage_product_update_stock, container, false)
    }

    override fun onResume() {
        super.onResume()
        if (!product.MANAGE_STOCK){
            //layoutManageProductStock.alpha = 0.3F
            tvManageProductStockMovementStock.text = "Inactive"
            //btnManageProductStockMovement.isEnabled = false
            //btnManageProductManageStock.isEnabled = false
        }
        else{
            tvMProdResCapital.text = currencyFormat(getLanguage(ctx),
                getCountry(ctx)).format((product.STOCK!! * product.COST!!))
            tvManageProductStockMovementStock.text = (if (isInt(product.STOCK!!)) product.STOCK!!.toInt() else product.STOCK).toString()
            //layoutManageProductStock.alpha = 1F
            //btnManageProductStockMovement.isEnabled = true
            //btnManageProductManageStock.isEnabled = true
        }

        btnManageProductStockMovement.onClick {
            btnManageProductStockMovement.startAnimation(normalClickAnimation())

            ctx.startActivity<ManageProductStockMovementListActivity>()
        }

        btnManageProductManageStock.onClick {
            btnManageProductManageStock.startAnimation(normalClickAnimation())

            if (!product.MANAGE_STOCK)
                toast("Current Product is Inactive")
            else{
                if (getMerchantUserGroup(ctx) == EUserGroup.WAITER.toString())
                    toast("Only Manager Can Update Stock")
                else
                    ctx.startActivity<ManageProductManageStockActivity>()
            }
        }
    }

}
