package com.chcreation.pointofsale.transaction

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.Payment
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.model.Transaction
import com.squareup.picasso.Picasso
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColorResource

class DetailTransactionListPaymentRecyclerViewAdapter(private val context: Context, private val items: MutableList<Payment>,
                                     private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<DetailTransactionListPaymentRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_payment_list, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position],listener, position)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val note = view.findViewById<TextView>(R.id.tvRowListPaymentNote)
        private val date = view.findViewById<TextView>(R.id.tvRowListPaymentDate)
        private val totalPriceReceived = view.findViewById<TextView>(R.id.tvRowListPaymentTotalReceived)
        private val paymentMethod = view.findViewById<ImageView>(R.id.ivRowListPayment)

        fun bindItem(item: Payment, listener: (position: Int) -> Unit, position: Int) {
            note.text = item.NOTE
            date.text = item.CREATED_DATE

            totalPriceReceived.text = indonesiaCurrencyFormat().format(item.TOTAL_RECEIVED)

            if (item.PAYMENT_METHOD == EPaymentMethod.CASH.toString())
                paymentMethod.imageResource = R.drawable.cash
            else if (item.PAYMENT_METHOD == EPaymentMethod.CARD.toString())
                paymentMethod.imageResource = R.drawable.card

            if (item.NOTE == "")
                note.visibility = View.GONE
            else
                note.visibility = View.VISIBLE

            itemView.onClick {
                listener(position)
            }
        }

    }
}