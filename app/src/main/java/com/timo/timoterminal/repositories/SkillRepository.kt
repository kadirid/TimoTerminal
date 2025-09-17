package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.entityClasses.SkillEntity

class SkillRepository (private val skillDAO: com.timo.timoterminal.dao.SkillDAO) {

    fun getAllSkillsFlow() = skillDAO.getAllSkillsFlow()

    suspend fun getAllSkills() = skillDAO.getAllSkills()

    suspend fun getSkillById(id: Long) = skillDAO.getSkillById(id)

    suspend fun countSkills() = skillDAO.countSkills()

    @WorkerThread
    suspend fun insertSkill(skill: SkillEntity): Long {
        return skillDAO.insertSkill(skill)
    }

    @WorkerThread
    suspend fun insertAllSkills(skills: List<SkillEntity>) {
        skillDAO.insertAllSkills(skills)
    }

    suspend fun updateSkill(skill: SkillEntity) {
        skillDAO.updateSkill(skill)
    }

    suspend fun deleteSkill(skill: SkillEntity) {
        skillDAO.deleteSkill(skill)
    }

    suspend fun deleteAllSkills() {
        skillDAO.deleteAllSkills()
    }
}