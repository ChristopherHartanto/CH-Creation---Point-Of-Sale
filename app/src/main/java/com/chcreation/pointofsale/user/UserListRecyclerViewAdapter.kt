package com.chcreation.pointofsale.user

import android.app.ActionBar
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chcreation.pointofsale.*
import com.chcreation.pointofsale.model.Product
import com.chcreation.pointofsale.model.Transaction
import com.chcreation.pointofsale.model.UserList
import com.squareup.picasso.Picasso
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColorResource
import java.text.SimpleDateFormat
import java.util.*

class UserListRecyclerViewAdapter(private val context: Context,
                                  private val userNames: MutableList<String>,
                                  private val userGroups: MutableList<UserList>,
                                     private val listener: (position: Int) -> Unit)
    : RecyclerView.Adapter<UserListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_user_list, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(userNames[position],userGroups[position],listener, position)
    }

    override fun getItemCount(): Int = userGroups.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val firstName = view.findViewById<TextView>(R.id.tvRowUserListFirstName)
        private val name = view.findViewById<TextView>(R.id.tvRowUserListName)
        private val group = view.findViewById<TextView>(R.id.tvRowUserListGroup)

        fun bindItem(userName: String, userGroup: UserList, listener: (position: Int) -> Unit, position: Int) {
            if (userName != ""){
                firstName.text = userName.first().toString().toUpperCase(Locale.getDefault())
                name.text = userName
            }
            else{
                firstName.text = ""
                name.text = ""
            }

            group.text = userGroup.USER_GROUP

            itemView.onClick {
                itemView.startAnimation(normalClickAnimation())
                listener(position)
            }
        }

    }
}