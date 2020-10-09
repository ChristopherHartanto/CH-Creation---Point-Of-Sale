package com.chcreation.pointofsale.checkout

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.Discount
import com.chcreation.pointofsale.model.Payment
import org.jetbrains.anko.sdk27.coroutines.onClick

class DiscountListRecyclerViewAdapter(private val context: Context, private val items: List<Discount>,
                                      private val listener: (type:Int,position: Int) -> Unit)
    : RecyclerView.Adapter<DiscountListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_discount_list,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(context,items[position],position,listener)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val name = view.findViewById<TextView>(R.id.tvRowDiscName)
        private val amount = view.findViewById<TextView>(R.id.tvRowDiscAmount)
        private val edit = view.findViewById<ImageView>(R.id.ivRowDiscEdit)
        private val delete = view.findViewById<ImageView>(R.id.ivRowDiscDelete)

        fun bindItem(context: Context,item: Discount,position: Int,listener: (type:Int,position: Int) -> Unit) {

            name.text = item.NAME.toString()
            amount.text = "${item.PERCENT}%"

            edit.onClick {
                edit.startAnimation(normalClickAnimation())
                listener(1,position)
            }

            delete.onClick {
                delete.startAnimation(normalClickAnimation())
                listener(2,position)
            }

            itemView.onClick {
                itemView.startAnimation(normalClickAnimation())
                listener(0,position)
            }
        }

    }

}