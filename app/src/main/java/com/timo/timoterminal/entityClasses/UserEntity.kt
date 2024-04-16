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
    @ColumnInfo("login") var login: String,
    @ColumnInfo("hireDate") var hireDate: Long,
    @ColumnInfo("seeMenu") var seeMenu: Boolean,
    @ColumnInfo("assignedToTerminal") var assignedToTerminal: Boolean,
    @PrimaryKey(autoGenerate = false) var id: Long
) {

    companion object {
        fun parseJsonToUserEntity(obj: JSONObject): UserEntity {
            val id = obj.getLong("id")
            val firstName = obj.getString("firstName")
            val lastName = obj.getString("lastName")
            val card = obj.getString("cardNumber")
            val pin = obj.getString("pin")
            val login = obj.getString("login")
            val hireDate = obj.getLong("einstellungsDatum")
            val seeMenu = obj.getBoolean("seeMenu")
            val assignedToTerminal = obj.getBoolean("assignedToTerminal")
            return UserEntity(id, firstName, lastName, card, pin, login, hireDate, seeMenu, assignedToTerminal)
        }
    }

    constructor(
        id: Long,
        firstName: String,
        lastName: String,
        card: String,
        pin: String,
        login: String,
        hireDate: Long,
        seeMenu: Boolean,
        assignedToTerminal: Boolean
    ) : this(firstName, lastName, card, pin, login, hireDate, seeMenu, assignedToTerminal, id)

    fun name(): String = "$firstName $lastName"

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as UserEntity

        if (id != other.id) return false
        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (card != other.card) return false
        if (pin != other.pin) return false
        if (login != other.login) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + card.hashCode()
        result = 31 * result + pin.hashCode()
        result = 31 * result + firstName.hashCode()
        result = 31 * result + login.hashCode()
        result = 31 * result + hireDate.hashCode()
        result = 31 * result + seeMenu.hashCode()
        result = 31 * result + assignedToTerminal.hashCode()
        return result
    }
}
