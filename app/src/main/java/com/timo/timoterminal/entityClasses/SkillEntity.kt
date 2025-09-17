package com.timo.timoterminal.entityClasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class SkillEntity (
    @PrimaryKey @ColumnInfo(name = "skill_id") var skillId: Long,
    @ColumnInfo(name = "skill_name") var skillName: String
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as SkillEntity

        if (skillId != other.skillId) return false
        if (skillName != other.skillName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = skillId.hashCode()
        result = 31 * result + skillName.hashCode()
        return result
    }

    override fun toString(): String {
        return skillName
    }

    companion object {
        fun parseFromJson(obj: org.json.JSONObject): List<SkillEntity> {
            val skills = mutableListOf<SkillEntity>()
            val skillArray = obj.getJSONArray("skills")
            for (i in 0 until skillArray.length()) {
                val skillObj = skillArray.getJSONObject(i)
                val skillId = skillObj.getLong("id")
                val skillName = skillObj.getString("name")
                skills.add(SkillEntity(skillId, skillName))
            }
            return skills
        }
    }
}