package com.chcreation.pointofsale.product

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.normalClickAnimation
import com.squareup.picasso.Picasso
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.lang.Exception

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
        private val progressBar = view.findViewById<ProgressBar>(R.id.pbRowProductList)

        fun bindItem(item: String, listener: (position: Int) -> Unit, position: Int,context: Context) {

            if (item != ""){
                Glide.with(context).load(item).listener(object :
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

                }).into(image)
            }
            else{
                image.imageResource = R.drawable.default_image
                progressBar.visibility = View.GONE
            }

            itemView.onClick {
                itemView.startAnimation(normalClickAnimation())
                listener(position)
            }
//            val size = calculateSizeOfView(context)
//            itemView.layoutParams = ViewGroup.LayoutParams(size,size)

        }
        private fun calculateSizeOfView(context: Context): Int {

            val displayMetrics = context.resources.displayMetrics
            val dpWidth = displayMetrics.widthPixels
            return (dpWidth / 2)
        }
    }
}