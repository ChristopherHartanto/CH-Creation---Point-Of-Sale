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
import com.chcreation.pointofsale.model.Product
import com.squareup.picasso.Picasso
import org.jetbrains.anko.sdk27.coroutines.onClick

class CartRecyclerViewAdapter(private val context: Context, private val items: List<Cart>,private val listener: (product: Product) -> Unit)
    : RecyclerView.Adapter<CartRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_cart,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position],listener, position)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val qty = view.findViewById<TextView>(R.id.tvRowCartQty)
        private val name = view.findViewById<TextView>(R.id.tvRowCartName)
        private val price = view.findViewById<TextView>(R.id.tvRowCartPrice)
        private val totalPrice = view.findViewById<TextView>(R.id.tvRowCartTotalPrice)

        fun bindItem(cart: Cart, listener: (listenerProduct: Product) -> Unit, position: Int) {

            name.text = cart.NAME
            price.text = indonesiaCurrencyFormat().format(cart.PRICE)
            totalPrice.text = indonesiaCurrencyFormat().format(cart.PRICE!! * cart.Qty!!)
            qty.text = "${cart.Qty}x"

            itemView.onClick {

            }
        }

    }
}