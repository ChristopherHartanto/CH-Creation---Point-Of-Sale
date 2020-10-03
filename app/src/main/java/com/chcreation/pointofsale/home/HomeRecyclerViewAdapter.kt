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

class HomeRecyclerViewAdapter(private val context: Context,
                              private val items: List<Product>,
                              private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        if (getProductView(context) == EProductView.LIST.toString())
        ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_product,
                parent,
                false
            )
        )
        else
            ViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.row_product_grid,
                    parent,
                    false
                )
            )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getProductView(context) == EProductView.LIST.toString())
            holder.bindItem(items[position],listener, position,context)
        else
            holder.bindItemGrid(items[position],listener, position,context)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view){

        fun bindItem(product: Product, listener: (position: Int) -> Unit, position: Int,context: Context) {
            val wholeSale = view.findViewById<ImageView>(R.id.ivRowProductWholeSale)
            val image = view.findViewById<ImageView>(R.id.ivRowProductImage)
            val name = view.findViewById<TextView>(R.id.tvRowProductName)
            val price = view.findViewById<TextView>(R.id.tvRowProductPrice)
            val stock = view.findViewById<TextView>(R.id.tvRowProductStock)
            val firstName = view.findViewById<TextView>(R.id.tvRowProductFirstName)
            val layoutDefaultImage = view.findViewById<FrameLayout>(R.id.layoutRowProductDefaultImage)

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
            stock.text = "${if(isInt(product.STOCK!!)) product.STOCK!!.toInt() else String.format("%.2f",product.STOCK) } qty"

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


        fun bindItemGrid(product: Product, listener: (position: Int) -> Unit, position: Int,context: Context) {
            val gWholeSale = view.findViewById<ImageView>(R.id.ivRowProductGridWholeSale)
            val gImage = view.findViewById<ImageView>(R.id.ivRowProductGridImage)
            val gName = view.findViewById<TextView>(R.id.tvRowProductGridName)
            val gFirstName = view.findViewById<TextView>(R.id.tvRowProductGridFirstName)
            val gLayoutDefaultImage = view.findViewById<FrameLayout>(R.id.layoutRowProductGridDefaultImage)

            if (product.IMAGE != ""){
                gImage.visibility = View.VISIBLE
                gLayoutDefaultImage.visibility = View.GONE

                Glide.with(context).load(product.IMAGE).into(gImage)
            }
            else{
                gImage.visibility = View.GONE
                gLayoutDefaultImage.visibility = View.VISIBLE

                gFirstName.text = product.NAME!!.first().toString().toUpperCase(Locale.getDefault())
            }
            if (product.WHOLE_SALE == ""){
                gWholeSale.visibility = View.GONE
            }else{
                val gson = Gson()
                val arrayWholeSaleType = object : TypeToken<MutableList<WholeSale>>() {}.type
                val wholeSaleItems : MutableList<WholeSale> = gson.fromJson(product.WHOLE_SALE,arrayWholeSaleType)

                if (wholeSaleItems.size == 0)
                    gWholeSale.visibility = View.GONE
                else
                    gWholeSale.visibility = View.VISIBLE
            }

            gName.text = product.NAME

            if (product.STOCK!! <= 0)
                gName.textColorResource = R.color.colorRed
            else
                gName.textColorResource = R.color.colorBlack

//            val tvNameHeight = gName.lineCount * gName.lineHeight
//            if (tvNameHeight > gName.lineHeight){
//                val name = if (product.NAME!!.length > 13) "${product.NAME!!.substring(0,13)}.."
//                else product.NAME
//
//                gName.text = name
//            }



            itemView.onClick {
                itemView.startAnimation(normalClickAnimation())
                listener(position)
            }
        }

    }
}