package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ConfigEntity(
    @ColumnInfo(name = "type") var type: Int,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "value") var value: String
) {
    @PrimaryKey(autoGenerate = true)
    var id : Long? = null

    constructor(id:Long,type: Int,name: String,value: String): this(type, name, value){
        this.id = id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConfigEntity

        if (id != other.id) return false
        if (name != other.name) return false
        if (type != other.type) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}
