package com.chcreation.pointofsale.customer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chcreation.pointofsale.EStatusCode
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.model.Enquiry
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.parseDateFormat
import com.chcreation.pointofsale.receiptFormat
import com.squareup.picasso.Picasso
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.util.*

class CustomerDetailTransactionRecyclerViewAdapter(private val context: Context, private val items: List<Enquiry>,private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<CustomerDetailTransactionRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_customer_transaction,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position],listener, position,context)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val status = view.findViewById<ImageView>(R.id.ivRowCustomerTransaction)
        private val receipt = view.findViewById<TextView>(R.id.tvRowCustomerTransactionReceipt)
        private val date = view.findViewById<TextView>(R.id.tvRowCustomerTransactionDate)

        fun bindItem(item: Enquiry, listener: (position: Int) -> Unit, position: Int,context: Context) {

            when (item.STATUS_CODE) {
                EStatusCode.PENDING.toString() -> status.imageResource = R.drawable.pending
                EStatusCode.DONE.toString() -> status.imageResource = R.drawable.success
                EStatusCode.CANCEL.toString() -> status.imageResource = R.drawable.error
            }

            receipt.text = receiptFormat(item.TRANS_KEY!!.toInt())
            date.text = item.UPDATED_DATE.toString()

            itemView.onClick {
                listener(position)
            }
        }

    }
}