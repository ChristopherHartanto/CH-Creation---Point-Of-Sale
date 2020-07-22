package com.chcreation.pointofsale.customer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.model.Product
import com.squareup.picasso.Picasso
import org.jetbrains.anko.sdk27.coroutines.onClick

class CustomerRecyclerViewAdapter(private val context: Context, private val items: List<Customer>,private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<CustomerRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_customer,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position],listener, position)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

       // private val image = view.findViewById<ImageView>(R.id.ivRowProductImage)
        private val name = view.findViewById<TextView>(R.id.tvRowCustomerName)

        fun bindItem(customer: Customer, listener: (position: Int) -> Unit, position: Int) {

            name.text = customer.NAME

            itemView.onClick {
                listener(position)
            }
        }

    }
}