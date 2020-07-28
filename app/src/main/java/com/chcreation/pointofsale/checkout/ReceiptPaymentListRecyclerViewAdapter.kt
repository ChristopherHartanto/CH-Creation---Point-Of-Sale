package com.chcreation.pointofsale.checkout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.indonesiaCurrencyFormat
import com.chcreation.pointofsale.model.Cart
import com.chcreation.pointofsale.model.Payment
import com.chcreation.pointofsale.model.Product
import com.squareup.picasso.Picasso
import org.jetbrains.anko.sdk27.coroutines.onClick

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
        holder.bindItem(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val price = view.findViewById<TextView>(R.id.tvRowReceiptPaymentListPrice)

        fun bindItem(item: Payment) {

            price.text = indonesiaCurrencyFormat().format(item.TOTAL_RECEIVED).toString()

        }

    }

}