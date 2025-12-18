package com.timo.timoterminal.entityAdaptor

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.timo.timoterminal.databinding.ItemAbsenceEntryBinding
import com.timo.timoterminal.entityClasses.AbsenceEntryEntity
import com.timo.timoterminal.utils.Utils

class AbsenceListAdapter(
    private var entities: List<AbsenceEntryEntity>,
    private val absenceTypeMap : Map<String, String>,
    private val userIdMap: Map<String, String>,
    private val matrixMap: Map<String, String>,
    private val deputyMap: Map<String, String>,
    private val listener: OnItemClickListener,
    private val absenceLetter: String,
    private val matrixLetter: String,
    private val deputyLetter: String,
    private val fullDayText: String
) : RecyclerView.Adapter<AbsenceListAdapter.ViewHolder>() {
    private var allEntities: List<AbsenceEntryEntity> = entities.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            ItemAbsenceEntryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun sort(sortId: Int, asc: Boolean) {
        if (asc) {
            entities = when (sortId) {
                1 -> entities.sortedBy { it.date.ifEmpty { it.dateTo } }
                2 -> entities.sortedBy { userIdMap[it.userId] ?: "" }
                3 -> entities.sortedBy { absenceTypeMap[it.absenceTypeId] ?: "" }
                else -> entities
            }
        } else {
            entities = when (sortId) {
                1 -> entities.sortedByDescending { it.date.ifEmpty { it.dateTo } }
                2 -> entities.sortedByDescending { userIdMap[it.userId] ?: "" }
                3 -> entities.sortedByDescending { absenceTypeMap[it.absenceTypeId] ?: "" }
                else -> entities
            }
        }
        notifyDataSetChanged()
    }

    fun filter(filterId: Int, filterText: String) {
        if (filterText.isEmpty()) {
            entities = allEntities
            notifyDataSetChanged()
            return
        }
        entities = when (filterId) {
            2 -> allEntities.filter {
                val userName = userIdMap[it.userId] ?: ""
                userName.contains(filterText, ignoreCase = true)
            }
            3 -> allEntities.filter {
                val absenceType = absenceTypeMap[it.absenceTypeId] ?: ""
                absenceType.contains(filterText, ignoreCase = true)
            }
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
            val entryStart = Utils.parseDateFromTransfer(it.date).timeInMillis
            val entryEnd = if (it.dateTo.isNotEmpty()) {
                Utils.parseDateFromTransfer(it.dateTo).timeInMillis
            } else {
                entryStart
            }
            // check if entry is within the range
            (entryStart in oStart..oEnd) ||
            (entryEnd in oStart..oEnd) ||
            (entryStart <= oStart && entryEnd >= oEnd)
        }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = entities[position]

        var dateAndTime = ""
        if(item.date.isNotEmpty()) {
            dateAndTime += Utils.getDateFromGC(Utils.parseDateFromTransfer(item.date)) + " "
        }
        if (item.from.isNotEmpty()) {
            dateAndTime += item.from
        }
        if (item.dateTo.isNotEmpty() || item.to.isNotEmpty()) {
            dateAndTime += " - "
        }
        if (item.dateTo.isNotEmpty()) {
            dateAndTime += Utils.getDateFromGC(Utils.parseDateFromTransfer(item.dateTo)) + " "
        }
        if (item.to.isNotEmpty()) {
            dateAndTime += item.to
        }
        if (item.hours.isNotEmpty() && item.fullDay != "1") {
            dateAndTime += "${item.hours} h"
        }
        if (item.fullDay == "1") {
            dateAndTime += " $fullDayText"
        }

        val userText = userIdMap[item.userId] ?: item.userId
        val absenceText = "$absenceLetter: " + (absenceTypeMap[item.absenceTypeId] ?: "")
        val matrixText = "$matrixLetter: " + (matrixMap[item.matrix] ?: "")
        val deputyText = "$deputyLetter: " + (deputyMap[item.deputy] ?: "")

        if(item.isSend) {
            holder.iconView.visibility = View.VISIBLE
        }
        holder.dateTimeView.text = dateAndTime
        holder.userView.text = userText
        holder.absenceView.text = absenceText
        holder.matrixView.text = matrixText
        holder.deputyView.text = deputyText

        holder.rootView.setOnClickListener {
            listener.onItemClick(item)
        }
    }

    override fun getItemCount(): Int = entities.size

    inner class ViewHolder(binding: ItemAbsenceEntryBinding) : RecyclerView.ViewHolder(binding.root) {
        val dateTimeView: TextView = binding.textViewAbsenceListDateTime
        val userView: TextView = binding.textViewAbsenceListUserName
        val absenceView: TextView = binding.textViewAbsenceListAbsenceType
        val matrixView: TextView = binding.textViewAbsenceListMatrix
        val deputyView: TextView = binding.textViewAbsenceListDeputy
        val iconView: ImageView = binding.imageViewAbsenceListInfoIcon
        val rootView: View = binding.absenceListLayout
    }

    interface OnItemClickListener {
        fun onItemClick(entity: AbsenceEntryEntity)
    }

}