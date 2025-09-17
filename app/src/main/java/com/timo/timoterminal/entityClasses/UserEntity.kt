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
    @ColumnInfo("customerBasedProjectTime") var customerBasedProjectTime: Boolean = false,
    @ColumnInfo("timeEntryType") var timeEntryType: Long = -1L,
    @ColumnInfo("crossDay") var crossDay: Boolean = false,
    @PrimaryKey var id: Long
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
            val customerBasedProjectTime = obj.getBoolean("customerBasedProjectTime")
            val timeEntryType = obj.optLong("timeEntryType", -1L)
            val crossDay = obj.optBoolean("crossDay", false)
            return UserEntity(
                id,
                firstName,
                lastName,
                card,
                pin,
                login,
                hireDate,
                seeMenu,
                assignedToTerminal,
                customerBasedProjectTime,
                timeEntryType,
                crossDay
            )
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
        assignedToTerminal: Boolean,
        customerBasedProjectTime: Boolean,
        timeEntryType: Long = -1,
        crossDay: Boolean = false
    ) : this(
        firstName,
        lastName,
        card,
        pin,
        login,
        hireDate,
        seeMenu,
        assignedToTerminal,
        customerBasedProjectTime,
        timeEntryType,
        crossDay,
        id
    )

    constructor(
        id: Long,
        firstName: String,
        lastName: String,
        pin: String,
        login: String,
    ) : this(
        firstName,
        lastName,
        card = "",
        pin,
        login,
        hireDate = 1L,
        seeMenu = true,
        assignedToTerminal = true,
        customerBasedProjectTime = false,
        timeEntryType = -1,
        crossDay = false,
        id
    )

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
