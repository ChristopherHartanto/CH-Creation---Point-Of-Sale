package com.chcreation.pointofsale.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.model.WholeSale
import com.chcreation.pointofsale.product.ProductWholeSaleActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.textColorResource
import java.util.*

class HomeRecyclerViewAdapter(private val context: Context, private val items: List<Product>,private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_product,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position],listener, position,context)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val wholeSale = view.findViewById<ImageView>(R.id.ivRowProductWholeSale)
        private val image = view.findViewById<ImageView>(R.id.ivRowProductImage)
        private val name = view.findViewById<TextView>(R.id.tvRowProductName)
        private val price = view.findViewById<TextView>(R.id.tvRowProductPrice)
        private val stock = view.findViewById<TextView>(R.id.tvRowProductStock)
        private val firstName = view.findViewById<TextView>(R.id.tvRowProductFirstName)
        private val layoutDefaultImage = view.findViewById<FrameLayout>(R.id.layoutRowProductDefaultImage)

        fun bindItem(product: Product, listener: (position: Int) -> Unit, position: Int,context: Context) {
            if (product.IMAGE != ""){
                image.visibility = View.VISIBLE
                layoutDefaultImage.visibility = View.GONE

                Glide.with(context).load(product.IMAGE).into(image)
            }
            else{
                image.visibility = View.GONE
                layoutDefaultImage.visibility = View.VISIBLE

                firstName.text = product.NAME!!.first().toString().toUpperCase(Locale.getDefault())
            }
            if (product.WHOLE_SALE == ""){
                wholeSale.visibility = View.GONE
            }else{
                val gson = Gson()
                val arrayWholeSaleType = object : TypeToken<MutableList<WholeSale>>() {}.type
                val wholeSaleItems : MutableList<WholeSale> = gson.fromJson(product.WHOLE_SALE,arrayWholeSaleType)

                if (wholeSaleItems.size == 0)
                    wholeSale.visibility = View.GONE
                else
                    wholeSale.visibility = View.VISIBLE
            }

            name.text = product.NAME
            price.text = currencyFormat(
                getLanguage(context),
                getCountry(context)
            ).format(product.PRICE)
            stock.text = "${product.STOCK} qty"

            if (product.STOCK!! <= 0 && product.MANAGE_STOCK)
                stock.textColorResource = R.color.colorRed
            else if (!product.MANAGE_STOCK)
                stock.textColorResource = R.color.colorPrimary
            else
                stock.textColorResource = R.color.colorBlack

            itemView.onClick {
                itemView.startAnimation(normalClickAnimation())
                listener(position)
            }
        }

    }
}