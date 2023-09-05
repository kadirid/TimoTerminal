package com.timo.timoterminal.entityAdaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.timo.timoterminal.R
import com.timo.timoterminal.entityClasses.UserEntity

class UserEntityAdaptor(private val userEntities: List<UserEntity>, private val listener: OnItemClickListener) : RecyclerView.Adapter<UserEntityAdaptor.UserEntityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserEntityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserEntityViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userEntities.size
    }

    override fun onBindViewHolder(holder: UserEntityViewHolder, position: Int) {
        holder.bind(userEntities[position], listener)
    }

    class UserEntityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val idView : TextView = itemView.findViewById(R.id.text_view_user_id)
        private val nameView : TextView = itemView.findViewById(R.id.text_view_user_name)
        private val cardView : TextView = itemView.findViewById(R.id.text_view_user_card)
        private val pinView : TextView = itemView.findViewById(R.id.text_view_user_pin)

        fun bind(user: UserEntity, listener : OnItemClickListener) {
            idView.text = "${user.id}"
            nameView.text = user.name
            cardView.text = user.card
            pinView.text = user.pin
            itemView.setOnClickListener {
                listener.onItemClick(user)
            }
        }
    }

    interface OnItemClickListener{
        fun onItemClick(user:UserEntity)
    }

}