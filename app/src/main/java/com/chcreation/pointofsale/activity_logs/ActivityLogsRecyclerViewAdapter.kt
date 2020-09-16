package com.chcreation.pointofsale.activity_logs

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chcreation.pointofsale.R
import com.chcreation.pointofsale.indonesiaCurrencyFormat
import com.chcreation.pointofsale.model.ActivityLogs
import com.chcreation.pointofsale.model.Cart
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.normalClickAnimation
import com.chcreation.pointofsale.parseDateFormatFull
import com.squareup.picasso.Picasso
import org.jetbrains.anko.sdk27.coroutines.onClick

class ActivityLogsRecyclerViewAdapter(private val context: Context, private val items: List<ActivityLogs>,private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<ActivityLogsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_activity_logs_list,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position],listener, position)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val desc = view.findViewById<TextView>(R.id.tvRowActivityLogs)

        fun bindItem(log: ActivityLogs, listener: (position: Int) -> Unit, position: Int) {

            desc.text = "${log.LOG}\n${parseDateFormatFull(log.CREATED_DATE.toString())}"

            itemView.onClick {
                listener(position)
            }
        }

    }
}