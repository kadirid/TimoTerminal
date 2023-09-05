package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.DemoDAO
import com.timo.timoterminal.entityClasses.DemoEntity
import kotlinx.coroutines.flow.Flow

class DemoRepository(private val demoDAO: DemoDAO) {

    val getAllEntities : Flow<List<DemoEntity>> = demoDAO.getAll()

    @WorkerThread
    suspend fun insertDemoEntity(entity : DemoEntity) {
        demoDAO.insertAll(entity)
    }

}