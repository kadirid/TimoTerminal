package com.timo.timoterminal.service.serviceUtils.classes

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject
import java.util.Date

data class Buchung(
    val id : Int,
    val employee: Int,
    val date: Date,
    val typ: Int,
    val lon: Double,
    val lat: Double,
    val description: String?,
    val agent: Int,
    val terminalId: String?
    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        Date(parcel.readLong()),
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(employee)
        parcel.writeInt(typ)
        parcel.writeDouble(lon)
        parcel.writeDouble(lat)
        parcel.writeString(description)
        parcel.writeInt(agent)
        parcel.writeString(terminalId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Buchung> {
        override fun createFromParcel(parcel: Parcel): Buchung {
            return Buchung(parcel)
        }

        override fun newArray(size: Int): Array<Buchung?> {
            return arrayOfNulls(size)
        }

        fun convertJsonToObject(json: JSONObject) : Buchung {
            return Buchung(
                json.getInt("id"),
                json.getInt("employee"),
                Date(json.getLong("date")),
                json.getInt("typ"),
                json.getDouble("lon"),
                json.getDouble("lat"),
                json.getString("description"),
                json.getInt("agent"),
                json.getString("terminalId")
            )
        }
    }

}