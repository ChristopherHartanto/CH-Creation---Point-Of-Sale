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
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.model.Customer
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.normalClickAnimation
import com.squareup.picasso.Picasso
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.util.*

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
        holder.bindItem(items[position],listener, position,context)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val image = view.findViewById<ImageView>(R.id.ivRowCustomerImage)
        private val layoutDefaultImage = view.findViewById<FrameLayout>(R.id.layoutRowCustomerDefaultImage)
        private val firstName = view.findViewById<TextView>(R.id.tvRowCustomerFirstName)
        private val name = view.findViewById<TextView>(R.id.tvRowCustomerName)

        fun bindItem(customer: Customer, listener: (position: Int) -> Unit, position: Int,context: Context) {

            name.text = customer.NAME

            if (customer.IMAGE == "")
            {
                layoutDefaultImage.visibility = View.VISIBLE
                image.visibility = View.GONE
                firstName.text = customer.NAME!!.first().toString().toUpperCase(Locale.getDefault())
            }else{
                layoutDefaultImage.visibility = View.GONE
                image.visibility = View.VISIBLE

                Glide.with(context).load(customer.IMAGE).into(image)
            }

            itemView.onClick {
                itemView.startAnimation(normalClickAnimation())
                listener(position)
            }
        }

    }
}