package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class TicketEntity (
    @PrimaryKey @ColumnInfo(name = "ticket_id") var ticketId: Long,
    @ColumnInfo(name = "ticket_name") var ticketName: String,
    @ColumnInfo(name = "ticket_task_id") var taskId: Long = -1
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as TicketEntity

        if (ticketId != other.ticketId) return false
        if (ticketName != other.ticketName) return false
        if (taskId != other.taskId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ticketId.hashCode()
        result = 31 * result + ticketName.hashCode()
        result = 31 * result + taskId.hashCode()
        return result
    }

    override fun toString(): String {
        return ticketName
    }

    companion object {
        fun parseFromJson(obj: org.json.JSONObject): List<TicketEntity> {
            val tickets = mutableListOf<TicketEntity>()
            val ticketArray = obj.getJSONArray("tickets")
            for (i in 0 until ticketArray.length()) {
                val ticketObj = ticketArray.getJSONObject(i)
                val ticketId = ticketObj.getLong("id")
                val ticketName = ticketObj.getString("name")
                val taskId = ticketObj.optLong("task_id", -1)
                tickets.add(TicketEntity(ticketId, ticketName, taskId))
            }
            return tickets
        }
    }
}