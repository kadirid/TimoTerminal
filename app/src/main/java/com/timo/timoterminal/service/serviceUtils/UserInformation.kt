package com.timo.timoterminal.service.serviceUtils

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
    val kommen: String,
    val pause: String,
    val gehen: String,
    val overtime: String,
    val vacation: String,
    val gVacation: String,
    val bVacation: String,
    val rVacation: String
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
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(user)
        parcel.writeString(soll)
        parcel.writeString(ist)
        parcel.writeInt(zeitTyp)
        parcel.writeLong(zeitLB.time)
        parcel.writeString(kommen)
        parcel.writeString(pause)
        parcel.writeString(gehen)
        parcel.writeString(overtime)
        parcel.writeString(vacation)
        parcel.writeString(gVacation)
        parcel.writeString(bVacation)
        parcel.writeString(rVacation)
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
            val zeitTyp = jsonObject.getInt("zeitTyp")

            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            var zeitLB = Date();
            try {
                zeitLB = formatter.parse(jsonObject.getString("zeitLB"))!!
            } catch (e : Exception) {
                e.printStackTrace()
            }

            val kommen = jsonObject.getString("kommen")
            val pause = jsonObject.getString("pause")
            val gehen = jsonObject.getString("gehen")
            val overtime = jsonObject.getString("overtime")
            val vacation = jsonObject.getString("vacation")
            val gVacation = jsonObject.getString("gVacation")
            val bVacation = jsonObject.getString("bVacation")
            val rVacation = jsonObject.getString("rVacation")
            return UserInformation(
                user,
                soll,
                ist,
                zeitTyp,
                zeitLB,
                kommen,
                pause,
                gehen,
                overtime,
                vacation,
                gVacation,
                bVacation,
                rVacation
            )
        }
    }
}