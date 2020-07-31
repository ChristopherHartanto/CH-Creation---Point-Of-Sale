package com.chcreation.pointofsale.product

import com.chcreation.pointofsale.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chcreation.pointofsale.EStatusStock
import com.chcreation.pointofsale.dateFormat
import com.chcreation.pointofsale.model.StockMovement
import com.chcreation.pointofsale.parseDateFormat
import org.jetbrains.anko.imageResource

class StockMovementListRecyclerView(private val context: Context, private val items: List<StockMovement>,
                                   private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<StockMovementListRecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_stock_movement_list, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(items[position],listener, position, context)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val image = view.findViewById<ImageView>(R.id.ivRowStockMovementList)
        private val desc = view.findViewById<TextView>(R.id.tvRowStockMovementListDesc)
        private val date = view.findViewById<TextView>(R.id.tvRowStockMovementListDate)

        fun bindItem(item: StockMovement, listener: (position: Int) -> Unit, position: Int, context: Context) {
            if (item.STATUS == EStatusStock.INBOUND.toString()){
                image.imageResource = R.drawable.inbound
                desc.text = "${item.QTY} Qty"
            }
            else if (item.STATUS == EStatusStock.OUTBOUND.toString()){
                image.imageResource = R.drawable.outbound
                desc.text = "${item.QTY} Qty"
            }
            date.text = parseDateFormat(item.UPDATED_DATE.toString())
        }
    }
}