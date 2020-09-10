package com.chcreation.pointofsale.product

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.StockMovement
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick

class CategoryRecyclerViewAdapter(private val context: Context, private val items: List<String>,
                                    private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<CategoryRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_category_list, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position],listener, position, context)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val delete = view.findViewById<ImageView>(R.id.ivRowCatDelete)
        private val name = view.findViewById<TextView>(R.id.tvRowCatName)

        fun bindItem(catName: String, listener: (position: Int) -> Unit, position: Int, context: Context) {
            name.text = catName
            itemView.onClick {
                itemView.startAnimation(normalClickAnimation())
                listener(position)
            }
        }
    }
}