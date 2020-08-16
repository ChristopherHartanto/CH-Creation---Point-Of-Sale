package com.chcreation.pointofsale.product

import com.chcreation.pointofsale.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.normalClickAnimation
import com.squareup.picasso.Picasso
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick

class ProductRecyclerViewAdapter(private val context: Context, private val items: List<Product>,
                                 private val categoryTotalItems: List<Int>,
                                 private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<ProductRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_product_cat, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position],categoryTotalItems[position],listener, position, context)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val layout = view.findViewById<CardView>(R.id.layoutRowProduct)
        private val image = view.findViewById<ImageView>(R.id.ivRowProductCatImage)
        private val name = view.findViewById<TextView>(R.id.tvRowProductCatTitle)
        private val totalProduct = view.findViewById<TextView>(R.id.tvRowProductTotalProduct)

        fun bindItem(item: Product,total: Int, listener: (position: Int) -> Unit, position: Int, context: Context) {
            name.text = item.CAT
            itemView.onClick {
                itemView.startAnimation(normalClickAnimation())
                listener(position)
            }

            totalProduct.text = total.toString()

            if (item.IMAGE != "")
                Glide.with(context).load(item.IMAGE).into(image)
            else
                image.imageResource = R.drawable.default_image
//            val size = calculateSizeOfView(context)
//            itemView.layoutParams = ViewGroup.LayoutParams(size,size)



//            val param = layout.layoutParams as ViewGroup.MarginLayoutParams
//            param.setMargins(10,10,10,10)
//            layout.layoutParams = ViewGroup.MarginLayoutParams().
        }
        private fun calculateSizeOfView(context: Context): Int {

            val displayMetrics = context.resources.displayMetrics
            val dpWidth = displayMetrics.widthPixels
            return (dpWidth / 2)
        }
    }
}