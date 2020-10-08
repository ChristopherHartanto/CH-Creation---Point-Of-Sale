package com.chcreation.pointofsale.about

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.model.ActivityLogs
import com.chcreation.pointofsale.model.OurCustomer
import com.chcreation.pointofsale.normalClickAnimation
import com.chcreation.pointofsale.parseDateFormatFull
import kotlinx.android.synthetic.main.fragment_about.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class AboutOurCustomerRecyclerViewAdapter(private val context: Context,
                                          private val items: List<OurCustomer>,private val listener: (link: String) -> Unit)
    : RecyclerView.Adapter<AboutOurCustomerRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_about_our_customer_list,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(context,items[position],listener, position)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val imageView = view.findViewById<ImageView>(R.id.ivRowAboutOurCust)
        private val progressBar = view.findViewById<ProgressBar>(R.id.pbRowAboutOurCust)

        fun bindItem(context: Context,item: OurCustomer, listener: (link: String) -> Unit, position: Int) {

            Glide.with(context).load(item.IMAGE).listener(object :
                RequestListener<String, GlideDrawable> {
                override fun onException(
                    e: java.lang.Exception?,
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

            }).into(imageView)

            itemView.onClick {
                itemView.startAnimation(normalClickAnimation())
                listener(item.LINK.toString())
            }
        }

    }
}