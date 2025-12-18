package com.timo.timoterminal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.timo.timoterminal.dao.AbsenceDeputyDAO
import com.timo.timoterminal.entityClasses.AbsenceDeputyEntity
import org.json.JSONArray

@Database(entities = [AbsenceDeputyEntity::class], version = 1)
@TypeConverters(JsonArrayConverter::class)
abstract class AbsenceDeputyDatabase: RoomDatabase() {
    abstract fun absenceDeputyDao(): AbsenceDeputyDAO
}

class JsonArrayConverter {
    @TypeConverter
    fun fromJsonArray(jsonArray: JSONArray?): String? {
        return jsonArray?.toString()
    }

    @TypeConverter
    fun toJsonArray(data: String?): JSONArray? {
        return data?.let { JSONArray(it) }
    }
}