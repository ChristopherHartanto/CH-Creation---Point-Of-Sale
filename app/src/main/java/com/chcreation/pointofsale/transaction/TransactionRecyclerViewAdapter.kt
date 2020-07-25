package com.chcreation.pointofsale.transaction

import com.chcreation.pointofsale.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.model.Transaction
import com.squareup.picasso.Picasso
import org.jetbrains.anko.sdk27.coroutines.onClick

class TransactionRecyclerViewAdapter(private val context: Context, private val items: MutableList<Transaction>,
                                     private val customerList: List<String>,
                                     private val transCodeList: List<String>,
                                     private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<TransactionRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_transaction, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position],transCodeList[position],customerList[position],listener, position)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val code = view.findViewById<TextView>(R.id.tvRowTransactionCode)
        private val customer = view.findViewById<TextView>(R.id.tvRowTransactionCustomer)
        private val date = view.findViewById<TextView>(R.id.tvRowTransactionDate)
        private val totalPrice = view.findViewById<TextView>(R.id.tvRowTransactionTotalPrice)
        private val customerLayout = view.findViewById<LinearLayout>(R.id.layoutRowTransactionCustomer)

        fun bindItem(item: Transaction, transCode: String, custName: String, listener: (position: Int) -> Unit, position: Int) {
            code.text = "#$transCode"
            customer.text = custName
            date.text = item.CREATED_DATE
            totalPrice.text = "Rp ${item.TOTAL_PRICE},00"

            if (custName == "")
                customerLayout.visibility = View.GONE
            else
                customerLayout.visibility = View.VISIBLE

            itemView.onClick {
                listener(position)
            }
        }

    }
}