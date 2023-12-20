package com.timo.timoterminal.entityAdaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.timo.timoterminal.R
import com.timo.timoterminal.entityClasses.BookingBUEntity
import com.timo.timoterminal.utils.Utils

class BookingBUEntityAdapter(
    private val entities: List<BookingBUEntity>,
    private val userMap: Map<String, String>,
    private val statusMap: Map<Int, String>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<BookingBUEntityAdapter.BookingBUEntityViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BookingBUEntityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_bu_booking,
            parent,
            false
        )
        return BookingBUEntityViewHolder(view)
    }

    override fun getItemCount(): Int {
        return entities.size
    }

    override fun onBindViewHolder(
        holder: BookingBUEntityViewHolder,
        position: Int
    ) {
        val entity = entities[position]
        holder.bind(
            entity,
            listener,
            userMap[entity.card] ?: "",
            statusMap[entity.status] ?: ""
        )
    }

    class BookingBUEntityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val idView: TextView = itemView.findViewById(R.id.text_view_bu_booking_id)
        private val statusView: TextView = itemView.findViewById(R.id.text_view_bu_booking_status)
        private val dateView: TextView = itemView.findViewById(R.id.text_view_bu_booking_date)
        private val imageView: ImageView = itemView.findViewById(R.id.image_view_bu_isSend_icon)
        private val nameView: TextView = itemView.findViewById(R.id.text_view_bu_booking_user_name)

        fun bind(
            entity: BookingBUEntity,
            listener: OnItemClickListener,
            userName: String,
            status: String
        ) {
            nameView.text = userName
            dateView.text = Utils.parseFromDBDate(entity.date)
            idView.text = "${entity.id}"
            statusView.text = status

            imageView.setImageResource(
                if (entity.isSend)
                    R.drawable.terminal_booking_sended
                else
                    R.drawable.terminal_booking_not_send
            )

            itemView.setOnClickListener {
                listener.onItemClick(entity)
            }
        }

    }

    interface OnItemClickListener {
        fun onItemClick(entity: BookingBUEntity)
    }
}