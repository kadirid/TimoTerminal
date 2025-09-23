package com.timo.timoterminal.entityAdaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.timo.timoterminal.R
import com.timo.timoterminal.entityClasses.ProjectTimeEntity
import com.timo.timoterminal.utils.Utils

class ProjectTimeEntityAdapter(
    private val entities: List<ProjectTimeEntity>,
    private val userIdMap: Map<String, String>,
    private val projectMap: Map<String, String>,
    private val taskMap: Map<String, String>,
    private val customerMap: Map<String, String>,
    private val listener: OnItemClickListener,
    private val manDaysName: String,
    private val projectLetter: String,
    private val taskLetter: String,
    private val customerLetter: String
) : RecyclerView.Adapter<ProjectTimeEntityAdapter.ProjectTimeEntityViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ProjectTimeEntityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_project_time,
            parent,
            false
        )
        return ProjectTimeEntityViewHolder(view)
    }

    override fun getItemCount(): Int {
        return entities.size
    }

    override fun onBindViewHolder(
        holder: ProjectTimeEntityViewHolder,
        position: Int
    ) {
        val entity = entities[position]
        val userName = userIdMap[entity.userId] ?: ""
        val projectName = "${projectLetter}: ${projectMap[entity.projectId] ?: ""}"
        val taskName = "${taskLetter}: ${taskMap[entity.taskId] ?: ""}"
        val customerName = "${customerLetter}: ${customerMap[entity.customerId] ?: ""}"
        holder.bind(entity, listener, userName, projectName, taskName, customerName, manDaysName)
    }

    class ProjectTimeEntityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val dateTimeView: TextView = itemView.findViewById(R.id.text_view_project_time_id)
        private val userNameView: TextView =
            itemView.findViewById(R.id.text_view_project_time_user_name)
        private val projectView: TextView =
            itemView.findViewById(R.id.text_view_project_time_project)
        private val taskView: TextView = itemView.findViewById(R.id.text_view_project_time_task)
        private val customerView: TextView =
            itemView.findViewById(R.id.text_view_project_time_customer)

        fun bind(
            entity: ProjectTimeEntity,
            listener: OnItemClickListener,
            userName: String,
            projectName: String,
            taskName: String,
            customerName: String,
            manDaysName: String
        ) {
            userNameView.text = userName
            projectView.text = projectName
            taskView.text = taskName
            customerView.text = customerName

            var dateAndTime: String =
                Utils.getDateFromGC(Utils.parseDateFromTransfer(entity.date)) + " "
            if (entity.from.isNotEmpty()) {
                dateAndTime += entity.from
            }
            if (entity.dateTo.isNotEmpty() || entity.to.isNotEmpty()) {
                dateAndTime += " - "
            }
            if (entity.dateTo.isNotEmpty()) {
                dateAndTime += Utils.getDateFromGC(Utils.parseDateFromTransfer(entity.dateTo)) + " "
            }
            if (entity.to.isNotEmpty()) {
                dateAndTime += entity.to
            }
            if (entity.hours.isNotEmpty() && !entity.manDays.isNotEmpty()) {
                dateAndTime += entity.hours
            }
            if (entity.manDays.isNotEmpty()) {
                dateAndTime += "${entity.manDays} $manDaysName"
            }

            dateTimeView.text = dateAndTime
            itemView.setOnClickListener {
                listener.onItemClick(entity)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(entity: ProjectTimeEntity)
    }
}