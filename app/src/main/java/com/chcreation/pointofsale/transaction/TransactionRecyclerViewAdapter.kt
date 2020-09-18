package com.chcreation.pointofsale.transaction

import android.app.ActionBar
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.customer.CustomerDetailActivity
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.model.Transaction
import com.squareup.picasso.Picasso
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textColorResource
import java.text.SimpleDateFormat

class TransactionRecyclerViewAdapter(private val context: Context,
                                     private val fragmentActivity: FragmentActivity,
                                     private val items: MutableList<Transaction>,
                                     private val customerList: List<String>,
                                     private val transCodeList: List<Int>,
                                     private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<TransactionRecyclerViewAdapter.ViewHolder>() {

    var transactionList = items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_transaction, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(fragmentActivity,items[position],transCodeList[position],customerList[position],listener, position)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val ivStatus = view.findViewById<ImageView>(R.id.ivRowTransactionStatus)
        private val code = view.findViewById<TextView>(R.id.tvRowTransactionCode)
        private val customer = view.findViewById<TextView>(R.id.tvRowTransactionCustomer)
        private val date = view.findViewById<TextView>(R.id.tvRowTransactionDate)
        private val totalPrice = view.findViewById<TextView>(R.id.tvRowTransactionTotalPrice)
        private val header = view.findViewById<TextView>(R.id.tvRowTransactionHeader)
        private val ivCustomer = view.findViewById<ImageView>(R.id.ivRowTransactionCustomer)
        private val layoutCustomer = view.findViewById<LinearLayout>(R.id.layoutRowTransactionCustomer)

        fun bindItem(fragmentActivity: FragmentActivity,item: Transaction, transCode: Int, custName: String, listener: (position: Int) -> Unit, position: Int) {
            code.text = receiptFormat(transCode)
            if (custName != "")
                customer.text = custName
            date.text = parseTimeFormat(item.CREATED_DATE.toString())
            totalPrice.text = indonesiaCurrencyFormat().format(item.TOTAL_PRICE!! - item.DISCOUNT!! + item.TAX!!)
            header.text = parseDateFormat(item.CREATED_DATE.toString())

            var dateBefore = parseDateFormat(transactionList[position].CREATED_DATE.toString())
            if (position > 0)
                dateBefore = parseDateFormat(transactionList[position - 1].CREATED_DATE.toString())
            val currentDate = parseDateFormat(transactionList[position].CREATED_DATE.toString())
            if (position > 0 && dateBefore == currentDate)
                header.visibility = View.GONE
            else
                header.visibility = View.VISIBLE

            if (custName == ""){
                totalPrice.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            else{
                totalPrice.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            when {
                item.TOTAL_OUTSTANDING!! > 0 && item.STATUS_CODE != EStatusCode.CANCEL.toString()-> {
                    ivStatus.imageResource = R.drawable.pending
                }
                item.STATUS_CODE == EStatusCode.DONE.toString() -> {
                    ivStatus.imageResource = R.drawable.success
                }
                item.STATUS_CODE == EStatusCode.CANCEL.toString() -> {
                    ivStatus.imageResource = R.drawable.error
                }
            }

            itemView.onClick {
                itemView.startAnimation(normalClickAnimation())
                listener(position)
            }

            customer.onClick {
                customer.startAnimation(normalClickAnimation())
                fragmentActivity.startActivity(fragmentActivity.intentFor<CustomerDetailActivity>(ECustomer.CODE.toString()
                        to item.CUST_CODE))
            }
        }

    }
}