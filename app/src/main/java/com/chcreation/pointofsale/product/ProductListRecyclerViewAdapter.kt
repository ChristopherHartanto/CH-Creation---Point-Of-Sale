package com.chcreation.pointofsale.product

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.model.Product
import com.squareup.picasso.Picasso
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick

class ProductListRecyclerViewAdapter(private val context: Context, private val items: List<String>,private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<ProductListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_product_list, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position],listener, position,context)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val image = view.findViewById<ImageView>(R.id.ivRowProductList)

        fun bindItem(item: String, listener: (position: Int) -> Unit, position: Int,context: Context) {

            if (item != "")
                Glide.with(context).load(item).into(image)
            else
                image.imageResource = R.drawable.default_image

            itemView.onClick {
                listener(position)
            }
            val size = calculateSizeOfView(context)
            itemView.layoutParams = ViewGroup.LayoutParams(size,size)

        }
        private fun calculateSizeOfView(context: Context): Int {

            val displayMetrics = context.resources.displayMetrics
            val dpWidth = displayMetrics.widthPixels
            return (dpWidth / 2)
        }
    }
}