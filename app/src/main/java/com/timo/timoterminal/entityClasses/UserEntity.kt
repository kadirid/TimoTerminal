package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity
class UserEntity(
    @ColumnInfo("firstname") var firstName: String,
    @ColumnInfo("lastname") var lastName: String,
    @ColumnInfo("card") var card: String,
    @ColumnInfo("pin") var pin: String,
    @ColumnInfo("hireDate") var hireDate: Long,
    @PrimaryKey(autoGenerate = false) var id : Long
) {

    companion object {
        fun parseJsonToUserEntity(obj: JSONObject) : UserEntity{
            val id = obj.getLong("id")
            val firstName = obj.getString("firstName")
            val lastName = obj.getString("lastName")
            val card = obj.getString("cardNumber")
            val pin = obj.getString("pin")
            val hireDate = obj.getLong("einstellungsDatum")
            return UserEntity(id, firstName, lastName, card, pin, hireDate)
        }
    }

    constructor(id : Long, firstName: String, lastName: String, card: String, pin: String, hireDate: Long) : this(firstName, lastName, card, pin, hireDate, id)



    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserEntity

        if (id != other.id) return false
        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (card != other.card) return false
        if (pin != other.pin) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + card.hashCode()
        result = 31 * result + pin.hashCode()
        return result
    }

    fun setId(sid: String?){
        if(!sid.isNullOrEmpty()){
            id = sid.toLong()
        }
    }
}
