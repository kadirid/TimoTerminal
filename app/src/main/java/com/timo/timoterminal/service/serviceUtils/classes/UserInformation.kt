package com.timo.timoterminal.service.serviceUtils.classes

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
    val rVacation: String,
    val event: ArrayList<Event>
)  {
    var card: String = ""

    companion object  {
        fun convertJSONToObject(json: String): UserInformation {
            val jsonObject = JSONObject(json)
            val user = jsonObject.getString("user")
            val soll = jsonObject.getString("soll")
            val ist = jsonObject.getString("ist")
            val zeitTyp = if (jsonObject.has("zeitTyp")) jsonObject.getInt("zeitTyp") else -1


            val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            var zeitLB = Date()
            if (jsonObject.has("zeitLB")) {
                try {
                    zeitLB = formatter.parse(jsonObject.getString("zeitLB"))!!
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val kommen = jsonObject.getString("kommen")
            val pause = jsonObject.getString("pause")
            val gehen = jsonObject.getString("gehen")
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
                kommen,
                pause,
                gehen,
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