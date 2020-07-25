package com.chcreation.pointofsale.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.indonesiaCurrencyFormat
import com.chcreation.pointofsale.model.Product
import com.squareup.picasso.Picasso
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColorResource

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
        holder.bindItem(items[position],listener, position)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val image = view.findViewById<ImageView>(R.id.ivRowProductImage)
        private val name = view.findViewById<TextView>(R.id.tvRowProductName)
        private val price = view.findViewById<TextView>(R.id.tvRowProductPrice)
        private val stock = view.findViewById<TextView>(R.id.tvRowProductStock)

        fun bindItem(product: Product, listener: (position: Int) -> Unit, position: Int) {
            if (product.IMAGE != "")
                Picasso.get().load(product.IMAGE).fit().into(image)
            else
                image.imageResource = R.drawable.default_image

            name.text = product.NAME
            price.text = indonesiaCurrencyFormat().format(product.PRICE)
            stock.text = "${product.STOCK} qty"

            if (product.STOCK!! <= 0)
                stock.textColorResource = R.color.colorRed
            else
                stock.textColorResource = R.color.colorBlack

            itemView.onClick {
                listener(position)
            }
        }

    }
}