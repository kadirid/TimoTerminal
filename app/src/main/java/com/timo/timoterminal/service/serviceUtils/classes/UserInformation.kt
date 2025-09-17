package com.timo.timoterminal.service.serviceUtils.classes

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserInformation(
    val user: String,
    val soll: String,
    val ist: String,
    val zeitTyp: Int,
    val zeitLB: Date,
    val overtime: String,
    val vacation: String,
    val gVacation: String,
    val bVacation: String,
    val rVacation: String,
    val event: ArrayList<Event>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        Date(parcel.readLong()),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        ArrayList<Event>().apply {
            parcel.readList(this, Event::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(user)
        parcel.writeString(soll)
        parcel.writeString(ist)
        parcel.writeInt(zeitTyp)
        parcel.writeLong(zeitLB.time)
        parcel.writeString(overtime)
        parcel.writeString(vacation)
        parcel.writeString(gVacation)
        parcel.writeString(bVacation)
        parcel.writeString(rVacation)
        parcel.writeList(event)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserInformation> {
        override fun createFromParcel(parcel: Parcel): UserInformation {
            return UserInformation(parcel)
        }

        override fun newArray(size: Int): Array<UserInformation?> {
            return arrayOfNulls(size)
        }

        fun convertJSONToObject(json: String): UserInformation {
            val jsonObject = JSONObject(json)
            val user = jsonObject.getString("user")
            val soll = jsonObject.getString("soll")
            val ist = jsonObject.getString("ist")
            val zeitTyp = if (jsonObject.has("zeitTyp")) jsonObject.getInt("zeitTyp") else -1


            val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            var zeitLB = Date()
            if(jsonObject.has("zeitLB")) {
                try {
                    zeitLB = formatter.parse(jsonObject.getString("zeitLB"))!!
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val overtime = jsonObject.getString("overtime")
            val vacation = jsonObject.getString("vacation")
            val gVacation = jsonObject.getString("gVacation")
            val bVacation = jsonObject.getString("bVacation")
            val rVacation = jsonObject.getString("rVacation")
            val events = jsonObject.getJSONArray("events").let { i ->
                (0 until i.length()).map { i.getJSONObject(it) }.map {
                    Event.convertJsonToObject(it)
                }.toCollection(ArrayList())
            }

            return UserInformation(
                user,
                soll,
                ist,
                zeitTyp,
                zeitLB,
                overtime,
                vacation,
                gVacation,
                bVacation,
                rVacation,
                events
            )
        }
    }
}