package com.chcreation.pointofsale.checkout

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.Cart
import org.jetbrains.anko.sdk27.coroutines.onClick

class CartRecyclerViewAdapter(private val context: Context, private val items: List<Cart>,private val listener: (position: Int) -> Unit)
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
        holder.bindItem(context,items[position],listener, position)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val discountPrice = view.findViewById<TextView>(R.id.tvRowCartDiscountPrice)
        private val layoutCart = view.findViewById<FrameLayout>(R.id.layoutRowCartDiscount)
        private val discountPriceLine = view.findViewById<TextView>(R.id.tvRowCartDiscountPriceLine)
        private val qty = view.findViewById<TextView>(R.id.tvRowCartQty)
        private val name = view.findViewById<TextView>(R.id.tvRowCartName)
        private val price = view.findViewById<TextView>(R.id.tvRowCartPrice)
        private val totalPrice = view.findViewById<TextView>(R.id.tvRowCartTotalPrice)

        fun bindItem(context: Context,cart: Cart, listener: (position: Int) -> Unit, position: Int) {

            if (cart.WHOLE_SALE_PRICE == -1F){
                layoutCart.visibility = View.GONE
            }else if (cart.WHOLE_SALE_PRICE != -1F){
                layoutCart.visibility = View.VISIBLE
                discountPrice.text = currencyFormat(getLanguage(context), getCountry(context)).format(cart.PRICE)
                discountPriceLine.text = currencyFormat(getLanguage(context), getCountry(context)).format(cart.PRICE)
            }

            name.text = cart.NAME
            price.text = currencyFormat(getLanguage(context), getCountry(context))
                .format((if (cart.WHOLE_SALE_PRICE == -1F) cart.PRICE!! else cart.WHOLE_SALE_PRICE!!) )
            totalPrice.text = currencyFormat(getLanguage(context), getCountry(context))
                .format((if (cart.WHOLE_SALE_PRICE == -1F) cart.PRICE!! else cart.WHOLE_SALE_PRICE!!) * cart.Qty!!)
            qty.text = "${if(isInt(cart.Qty!!)) cart.Qty!!.toInt() else cart.Qty}x"

            itemView.onClick {
                itemView.startAnimation(normalClickAnimation())
                listener(position)
            }
        }

    }
}