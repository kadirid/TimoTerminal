package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity
class UserEntity(
    @ColumnInfo("name") var name: String,
    @ColumnInfo("card") var card: String,
    @ColumnInfo("pin") var pin: String,
) {
    @PrimaryKey(autoGenerate = true)
    var id : Long? = null

    companion object {
        fun parseJsonToUserEntity(obj: JSONObject) : UserEntity{
            val id = obj.getInt("id")
            val name = obj.getString("lastName")
            //TODO: Therefore a real infrastructure must be set
            val card = "123"
            val pin = "0"
            return UserEntity(id.toLong(), name, card, pin )
        }
    }

    constructor(id : Long, name: String, card: String, pin: String) : this(name, card, pin) {
        this.id = id
    }



    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserEntity

        if (id != other.id) return false
        if (name != other.name) return false
        if (card != other.card) return false
        if (pin != other.pin) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
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
