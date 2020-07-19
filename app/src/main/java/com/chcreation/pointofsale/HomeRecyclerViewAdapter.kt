package com.chcreation.pointofsale

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chcreation.pointofsale.model.Product
import com.squareup.picasso.Picasso

class HomeRecyclerViewAdapter(private val context: Context, private val items: List<Product>)
    : RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_product, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val image = view.findViewById<ImageView>(R.id.ivRowProductImage)
        private val name = view.findViewById<TextView>(R.id.tvRowProductName)
        private val price = view.findViewById<TextView>(R.id.tvRowProductPrice)
        private val stock = view.findViewById<TextView>(R.id.tvRowProductStock)

        fun bindItem(product: Product) {
            if (product.IMAGE != "")
                Picasso.get().load(product.IMAGE).fit().into(image)

            name.text = product.NAME
            price.text = "Rp ${product.PRICE},00"
            stock.text = "${product.STOCK} qty"
        }

    }
}