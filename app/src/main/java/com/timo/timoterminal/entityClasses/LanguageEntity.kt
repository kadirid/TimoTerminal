package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

//@Entity(primaryKeys = ["keyname","language"]) wouldn't a composite key like in language database be more correct?
@Entity
class LanguageEntity(
    @ColumnInfo(name = "keyname") val keyname: String,
    @ColumnInfo(name = "language") val language: String,
    @ColumnInfo(name = "value") val value: String
) {
    @PrimaryKey(autoGenerate = true)
    var id : Long? = null

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as LanguageEntity

        if (keyname != other.keyname) return false
        if (language != other.language) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyname.hashCode()
        result = 31 * result + language.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    companion object {
        fun convertJSONObjectToLanguageEntity(obj: JSONObject): LanguageEntity {
            return LanguageEntity(
                obj.getString("keyname"),
                obj.getString("language"),
                obj.getString("value")
            )
        }
    }
}