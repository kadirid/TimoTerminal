package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class BookingBUEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") override var id: Long?,
    @ColumnInfo(name = "card") override var card: String,
    @ColumnInfo(name = "inputCode") override var inputCode: Int,
    @ColumnInfo(name = "date") override var date: String,
    @ColumnInfo(name = "status") override var status: Int,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP") val createdTime: String,
    @ColumnInfo(name = "isSend", defaultValue = "false") val isSend: Boolean
) : BookingEntity(id, card, inputCode, date, status) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as BookingBUEntity

        if (id != other.id) return false
        if (card != other.card) return false
        if (inputCode != other.inputCode) return false
        if (date != other.date) return false
        if (status != other.status) return false
        if (createdTime != other.createdTime) return false
        if (isSend != other.isSend) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + card.hashCode()
        result = 31 * result + inputCode
        result = 31 * result + date.hashCode()
        result = 31 * result + status
        result = 31 * result + createdTime.hashCode()
        result = 31 * result + isSend.hashCode()
        return result
    }

    override fun toString(): String {
        return "BookingBUEntity(id=$id, card='$card', inputCode=$inputCode, date='$date', status=$status, createdTime='$createdTime', isSend='$isSend')"
    }
}