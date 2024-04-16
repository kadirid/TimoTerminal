package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
open class BookingEntity(
    @ColumnInfo(name = "card") open var card: String,
    @ColumnInfo(name = "inputCode") open var inputCode: Int,
    @ColumnInfo(name = "date") open var date: String,
    @ColumnInfo(name = "status") open var status: Int
) {
    @PrimaryKey(autoGenerate = true)
    open var id : Long? = null

    constructor(id: Long?, card: String, inputCode: Int, date: String, status: Int) :this(card, inputCode, date, status){
        this.id = id
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as BookingEntity

        if (card != other.card) return false
        if (inputCode != other.inputCode) return false
        if (date != other.date) return false
        if (status != other.status) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = card.hashCode()
        result = 31 * result + inputCode
        result = 31 * result + date.hashCode()
        result = 31 * result + status
        result = 31 * result + (id?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "BookingEntity(id=$id, card='$card', inputCode=$inputCode, date='$date', status=$status)"
    }

}