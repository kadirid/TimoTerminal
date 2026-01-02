package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class AbsenceTypeFavoriteEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "absence_type_favorite_id") var id: Long = 0,
    @ColumnInfo(name = "user_id") var userId: Long,
    @ColumnInfo(name = "absence_type_id") var absenceTypeId: Long
)

