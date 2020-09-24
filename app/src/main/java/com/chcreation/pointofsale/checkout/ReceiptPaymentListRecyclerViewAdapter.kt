package com.chcreation.pointofsale.checkout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.currencyFormat
import com.chcreation.pointofsale.getCountry
import com.chcreation.pointofsale.getLanguage
import com.chcreation.pointofsale.model.Payment

class ReceiptPaymentListRecyclerViewAdapter(private val context: Context, private val items: List<Payment>)
    : RecyclerView.Adapter<ReceiptPaymentListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_receipt_payment_list,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(context,items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val price = view.findViewById<TextView>(R.id.tvRowReceiptPaymentListPrice)

        fun bindItem(context: Context,item: Payment) {

            price.text = currencyFormat(
                getLanguage(context),
                getCountry(context)
            ).format(item.TOTAL_RECEIVED).toString()

        }

    }

}