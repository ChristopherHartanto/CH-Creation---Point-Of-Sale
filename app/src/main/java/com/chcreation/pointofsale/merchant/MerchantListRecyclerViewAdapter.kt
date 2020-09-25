package com.chcreation.pointofsale.merchant

import android.app.ActionBar
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.Merchant
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.model.Transaction
import com.chcreation.pointofsale.model.UserList
import com.squareup.picasso.Picasso
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColorResource
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MerchantListRecyclerViewAdapter(private val context: Context,
                                  private val merchantList: MutableList<Merchant>,
                                  private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<MerchantListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_merchant_list, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(merchantList[position],listener, position)
    }

    override fun getItemCount(): Int = merchantList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val name = view.findViewById<TextView>(R.id.tvMerchantListName)
        private val firstName = view.findViewById<TextView>(R.id.tvMerchantListFirstName)
        private val layoutFirstName = view.findViewById<FrameLayout>(R.id.layoutMerchantListFirstName)
        private val image = view.findViewById<ImageView>(R.id.ivRowMerchantList)
        private val layoutMerchantListImage = view.findViewById<FrameLayout>(R.id.layoutMerchantListImage)
        private val progressBar = view.findViewById<ProgressBar>(R.id.pbRowMerchantList)

        fun bindItem(merchant: Merchant, listener: (position: Int) -> Unit, position: Int) {

            if (merchant.IMAGE != ""){
                layoutMerchantListImage.visibility = View.VISIBLE
                layoutFirstName.visibility = View.GONE

                Glide.with(context).load(merchant.IMAGE).listener(object :
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
            }else{
                layoutMerchantListImage.visibility = View.GONE
                layoutFirstName.visibility = View.VISIBLE
                firstName.text = merchant.NAME?.first().toString().toUpperCase(Locale.getDefault())
            }

            name.text = merchant.NAME

            itemView.onClick {
                itemView.startAnimation(normalClickAnimation())
                listener(position)
            }
        }

    }
}