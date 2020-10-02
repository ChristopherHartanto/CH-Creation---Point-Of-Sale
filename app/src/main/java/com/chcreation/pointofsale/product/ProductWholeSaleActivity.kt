package com.chcreation.pointofsale.product

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.home.HomeRecyclerViewAdapter
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.model.WholeSale
import com.chcreation.pointofsale.normalClickAnimation
import com.chcreation.pointofsale.product.ManageProductUpdateProductFragment.Companion.saveWholeSale
import kotlinx.android.synthetic.main.activity_product_whole_sale.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton

class ProductWholeSaleActivity : AppCompatActivity() {

    private lateinit var adapter: WholeSaleRecyclerViewAdapter
    private var tmpWholeSaleItems = mutableListOf<WholeSale>()

    companion object{
        var wholeSaleItems = mutableListOf<WholeSale>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_whole_sale)

        supportActionBar!!.title = "Set Wholesale"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        tmpWholeSaleItems.addAll(wholeSaleItems)

        adapter = WholeSaleRecyclerViewAdapter(this,tmpWholeSaleItems,object : onEditTextChanged{
            override fun onTextChanged(position: Int,type:String, text: String) {
                if (tmpWholeSaleItems.elementAtOrNull(position) != null){
                    if (type == "min")
                        tmpWholeSaleItems[position].MIN_QTY = text.toFloat()
                    if (type == "max")
                        tmpWholeSaleItems[position].MAX_QTY = text.toFloat()
                    else if(type == "price")
                        tmpWholeSaleItems[position].PRICE = text.toFloat()
                }
            }
        }){
            if (tmpWholeSaleItems.elementAtOrNull(it) != null){
                tmpWholeSaleItems.removeAt(it)
                adapter.notifyItemRemoved(it)
            }
        }
        rvWholeSale.apply {
            adapter = this@ProductWholeSaleActivity.adapter
            layoutManager = LinearLayoutManager(this@ProductWholeSaleActivity)
        }

        if (tmpWholeSaleItems.size == 0){
            tmpWholeSaleItems.add(WholeSale())
            adapter.notifyDataSetChanged()
        }

        btnWholeSaleSave.onClick {
            btnWholeSaleSave.startAnimation(normalClickAnimation())

            checkData(){success, message ->
                if (!success){
                    alert (message){
                        title = "Error Message"
                        yesButton {

                        }
                    }.show()
                }else
                {
                    saveWholeSale = true
                    wholeSaleItems.clear()
                    wholeSaleItems.addAll(tmpWholeSaleItems)
                    finish()
                }
            }
        }

        tvWHoleSaleAddNew.onClick {
            tvWHoleSaleAddNew.startAnimation(normalClickAnimation())

            if (tmpWholeSaleItems.size > 3)
                toast("Already Maximum Limits!")
            else{
                tmpWholeSaleItems.add(WholeSale(0F,0F,0F))
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }


    private fun checkData(callback:(success:Boolean,message:String)-> Unit){
//        if (tmpWholeSaleItems.size == 1 && tmpWholeSaleItems[0].MIN_QTY == 0
//            && tmpWholeSaleItems[0].MAX_QTY == 0 && tmpWholeSaleItems[0].PRICE == 0)
//            callback(false,"Please Fill Following Fields!")
        for ((index,data) in tmpWholeSaleItems.withIndex()){
//                for ((i2,i) in tmpWholeSaleItems.withIndex()){
//                    if (data.MIN_QTY!! < i.MAX_QTY!! && index != i2
//                        && index > i2){
//                        callback(false,"Minimal Qty Cannot Bigger Than Other Maximum Qty!")
//                        return
//                    }
//                }
            if (data.MIN_QTY!! > data.MAX_QTY!!){
                callback(false,"Minimal Qty Cannot Bigger Than Maximum Qty!")
                return
            }
            if (data.MIN_QTY!! == data.MAX_QTY!!){
                callback(false,"Minimal Qty Cannot Same with Maximum Qty!")
                return
            }
        }
        callback(true,"")
    }

}

interface onEditTextChanged{
    fun onTextChanged(position:Int,type:String,text:String)
}