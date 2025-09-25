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
    private var entities: List<ProjectTimeEntity>,
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
    private var allEntities: List<ProjectTimeEntity> = entities.toList()
    private val typeItem = 0
    private val typeFooter = 1

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ProjectTimeEntityViewHolder {
        return if (viewType == typeFooter) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.recycler_footer, parent, false)
            FooterViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_project_time,
                parent,
                false
            )
            ProjectTimeEntityViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return entities.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == entities.size) typeFooter else typeItem
    }

    fun sort(sortId: Int, asc: Boolean) {
        if (asc) {
            entities = when (sortId) {
                1 -> entities.sortedBy { it.date }
                2 -> entities.sortedBy { userIdMap[it.userId] ?: "" }
                3 -> entities.sortedBy { projectMap[it.projectId] ?: "" }
                4 -> entities.sortedBy { taskMap[it.taskId] ?: "" }
                5 -> entities.sortedBy { customerMap[it.customerId] ?: "" }
                else -> entities.sortedBy { it.id }
            }
        } else {
            entities = when (sortId) {
                1 -> entities.sortedByDescending { it.date }
                2 -> entities.sortedByDescending { userIdMap[it.userId] ?: "" }
                3 -> entities.sortedByDescending { projectMap[it.projectId] ?: "" }
                4 -> entities.sortedByDescending { taskMap[it.taskId] ?: "" }
                5 -> entities.sortedByDescending { customerMap[it.customerId] ?: "" }
                else -> entities.sortedByDescending { it.id }
            }
        }
        notifyDataSetChanged()
    }

    fun filter(filterId: Int, filterText: String){
        if (filterText.isEmpty()) {
            entities = allEntities
            notifyDataSetChanged()
            return
        }
        entities = when (filterId) {
            2 -> allEntities.filter { (userIdMap[it.userId] ?: "").contains(filterText, true) }
            3 -> allEntities.filter { (projectMap[it.projectId] ?: "").contains(filterText, true) }
            4 -> allEntities.filter { (taskMap[it.taskId] ?: "").contains(filterText, true) }
            5 -> allEntities.filter { (customerMap[it.customerId] ?: "").contains(filterText, true) }
            else -> allEntities
        }
        notifyDataSetChanged()
    }

    fun filter (start: Long, end: Long) {
        // calculate offset for timezone
        val sOffSet = Utils.getCal().timeZone.getOffset(start)
        val eOffSet = Utils.getCal().timeZone.getOffset(end)
        // remove offset from start and end time
        val oStart = start - sOffSet
        val oEnd = end - eOffSet

        entities = allEntities.filter {
            val date = Utils.parseDateFromTransfer(it.date)
            date.timeInMillis in oStart..oEnd
        }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(
        holder: ProjectTimeEntityViewHolder,
        position: Int
    ) {
        if(entities.size == position){
            return
        }
        val entity = entities[position]
        val userName = userIdMap[entity.userId] ?: ""
        val projectName = "${projectLetter}: ${projectMap[entity.projectId] ?: ""}"
        val taskName = "${taskLetter}: ${taskMap[entity.taskId] ?: ""}"
        val customerName = "${customerLetter}: ${customerMap[entity.customerId] ?: ""}"
        holder.bind(entity, listener, userName, projectName, taskName, customerName, manDaysName)
    }

    open class ProjectTimeEntityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        open fun bind(
            entity: ProjectTimeEntity,
            listener: OnItemClickListener,
            userName: String,
            projectName: String,
            taskName: String,
            customerName: String,
            manDaysName: String
        ) {
            val dateTimeView: TextView = itemView.findViewById(R.id.text_view_project_time_id)
            val userNameView: TextView =
                itemView.findViewById(R.id.text_view_project_time_user_name)
            val projectView: TextView = itemView.findViewById(R.id.text_view_project_time_project)
            val taskView: TextView = itemView.findViewById(R.id.text_view_project_time_task)
            val customerView: TextView = itemView.findViewById(R.id.text_view_project_time_customer)

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
            if (entity.hours.isNotEmpty() && entity.manDays.isEmpty()) {
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

    class FooterViewHolder(itemView: View) : ProjectTimeEntityViewHolder(itemView) {
        override fun bind(
            entity: ProjectTimeEntity,
            listener: OnItemClickListener,
            userName: String,
            projectName: String,
            taskName: String,
            customerName: String,
            manDaysName: String
        ) {
        }
    }

    interface OnItemClickListener {
        fun onItemClick(entity: ProjectTimeEntity)
    }
}