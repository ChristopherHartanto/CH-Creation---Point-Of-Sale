package com.chcreation.pointofsale.product

import com.chcreation.pointofsale.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chcreation.pointofsale.model.Product
import com.squareup.picasso.Picasso
import org.jetbrains.anko.sdk27.coroutines.onClick

class ProductRecyclerViewAdapter(private val context: Context, private val items: List<String>,private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<ProductRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_product_cat, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position],listener, position)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val image = view.findViewById<ImageView>(R.id.ivRowProductCatImage)
        private val name = view.findViewById<TextView>(R.id.tvRowProductCatTitle)

        fun bindItem(item: String, listener: (position: Int) -> Unit, position: Int) {
            name.text = item
            itemView.onClick {
                listener(position)
            }
        }

    }
}