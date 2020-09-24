package com.chcreation.pointofsale.product

import com.chcreation.pointofsale.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.model.WholeSale
import com.chcreation.pointofsale.normalClickAnimation
import com.squareup.picasso.Picasso
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick

class WholeSaleRecyclerViewAdapter(private val context: Context,
                                 private val wholeSaleItems: List<WholeSale>,
                                   private val onEditTextChanged: onEditTextChanged,
                                 private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<WholeSaleRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_wholesale_list, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(wholeSaleItems[position],listener, position, context,onEditTextChanged)
    }

    override fun getItemCount(): Int = wholeSaleItems.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val remove = view.findViewById<ImageView>(R.id.ivWholeSaleRemove)
        private val min = view.findViewById<EditText>(R.id.etRowWholeSaleMinQty)
        private val max = view.findViewById<EditText>(R.id.etRowWholeSaleMaxQty)
        private val price = view.findViewById<EditText>(R.id.etRowWholeSalePrice)

        fun bindItem(item: WholeSale,listener: (position: Int) -> Unit, position: Int, context: Context,onEditTextChanged:onEditTextChanged) {
            if (item.MIN_QTY!! > 0)
                item.MIN_QTY.toString().let { min.setText(it) }
            else
                min.setText("")

            if (item.MAX_QTY!! > 0)
                item.MAX_QTY.toString().let { max.setText(it) }
            else
                max.setText("")

            if (item.PRICE!! > 0)
                item.PRICE.toString().let { price.setText(it) }
            else
                price.setText("")

            remove.onClick {
                remove.startAnimation(normalClickAnimation())
                listener(position)
            }
            min.doOnTextChanged { text, start, before, count ->
                if (min.hasFocus() && min.text.toString() != "") {
                    onEditTextChanged.onTextChanged(position,"min",text.toString())
                    min.requestFocus()
                }
            }
            max.doOnTextChanged { text, start, before, count ->
                if (max.hasFocus() && max.text.toString() != "") {
                    onEditTextChanged.onTextChanged(position,"max",text.toString())
                    max.requestFocus()
                }
            }
            price.doOnTextChanged { text, start, before, count ->
                if (price.hasFocus() && price.text.toString() != "") {
                    onEditTextChanged.onTextChanged(position,"price",text.toString())
                    price.requestFocus()
                }
            }
        }
    }
}