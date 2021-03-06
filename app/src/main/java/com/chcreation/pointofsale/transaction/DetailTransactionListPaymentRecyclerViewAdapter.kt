package com.chcreation.pointofsale.transaction

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.Payment
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick

class DetailTransactionListPaymentRecyclerViewAdapter(private val context: Context, private val items: MutableList<Payment>,
                                                private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<DetailTransactionListPaymentRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_payment_list, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(context,items[position],listener, position)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val note = view.findViewById<TextView>(R.id.tvRowListPaymentNote)
        private val date = view.findViewById<TextView>(R.id.tvRowListPaymentDate)
        private val totalPriceReceived = view.findViewById<TextView>(R.id.tvRowListPaymentTotalReceived)
        //private val createdBy = view.findViewById<TextView>(R.id.tvRowListPaymentCreatedBy)
        private val paymentMethod = view.findViewById<ImageView>(R.id.ivRowListPayment)

        fun bindItem(context: Context,item: Payment, listener: (position: Int) -> Unit, position: Int) {
            //createdBy.text = "C : ${name}"
            date.text = parseDateFormatFull(item.CREATED_DATE.toString())

            totalPriceReceived.text = currencyFormat(getLanguage(context),
                getCountry(context)).format(item.TOTAL_RECEIVED)

            if (item.PAYMENT_METHOD == EPaymentMethod.CASH.toString())
                paymentMethod.imageResource = R.drawable.cash
            else if (item.PAYMENT_METHOD == EPaymentMethod.CARD.toString())
                paymentMethod.imageResource = R.drawable.card

            if (item.NOTE == "")
                note.text = "${item.PAYMENT_METHOD}"
            else
                note.text = "${item.NOTE}"

            itemView.onClick {
                itemView.startAnimation(normalClickAnimation())
                listener(position)
            }
        }

    }
}