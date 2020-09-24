package com.chcreation.pointofsale.checkout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.Cart
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.lang.Exception

class CheckOutRecyclerViewAdapter(private val context: Context,
                                  private val items: List<Cart>,
                                  private val imageItems: List<String>,
                                  private val listener: (type: Int,position: Int) -> Unit)
    : RecyclerView.Adapter<CheckOutRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_check_out_list,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(context,items[position],imageItems[position],listener, position)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val qty = view.findViewById<TextView>(R.id.tvRowCheckOutQty)
        private val name = view.findViewById<TextView>(R.id.tvRowCheckOutProdName)
        private val price = view.findViewById<TextView>(R.id.tvRowCheckOutPrice)
        private val totalPrice = view.findViewById<TextView>(R.id.tvRowCheckOutTotalPrice)
        private val ivProduct = view.findViewById<ImageView>(R.id.ivRowCheckOut)
        private val minus = view.findViewById<ImageView>(R.id.ivRowCheckOutMinus)
        private val add = view.findViewById<ImageView>(R.id.ivRowCheckOutAdd)
        private val layoutDiscount = view.findViewById<FrameLayout>(R.id.layoutRowCheckOutDiscount)
        private val progressBar = view.findViewById<ProgressBar>(R.id.pbRowCheckOut)

        fun bindItem(context: Context,cart: Cart, image: String, listener: (type: Int,position: Int) -> Unit, position: Int) {

            if (image != "")
                Glide.with(context).load(image).listener(object :
                    RequestListener<String, GlideDrawable> {
                    override fun onException(
                        e: Exception?,
                        model: String?,
                        target: Target<GlideDrawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: GlideDrawable?,
                        model: String?,
                        target: Target<GlideDrawable>?,
                        isFromMemoryCache: Boolean,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }

                }).into(ivProduct)
            else{
                progressBar.visibility = View.GONE
                ivProduct.imageResource = R.drawable.default_image
            }

            name.text = cart.NAME
            price.text = currencyFormat(getLanguage(context), getCountry(context)).format(cart.PRICE)
            totalPrice.text = currencyFormat(getLanguage(context), getCountry(context)).format(cart.PRICE!! * cart.Qty!!)
            qty.text = cart.Qty.toString()

            layoutDiscount.visibility = View.GONE

            minus.onClick {
                minus.startAnimation(normalClickAnimation())
                listener(1,position)
            }

            add.onClick {
                add.startAnimation(normalClickAnimation())
                listener(2,position)
            }
        }

    }
}