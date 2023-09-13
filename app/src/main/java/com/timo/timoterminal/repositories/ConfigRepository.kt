package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.ConfigDAO
import com.timo.timoterminal.entityClasses.ConfigEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class ConfigRepository(private val configDao: ConfigDAO){
    val getAllEntities: Flow<List<ConfigEntity>> = configDao.getAll()
    private val permissionMap: MutableMap<String, String> = mutableMapOf()
    private var url: String = ""
    private var company: String = ""

    private suspend fun initMap() {
        val list = configDao.getAllAsList()
        for (item in list){
            if (item.type == TYPE_PERMISSION) {
                permissionMap[item.name] = item.value
            }
        }
    }

    @WorkerThread
    suspend fun insertConfigEntity(entity: ConfigEntity) = configDao.insertAll(entity)

    suspend fun getCompany(): ConfigEntity? = configDao.getCompany()

    suspend fun getCompanyString(): String {
        if(company.isEmpty()){
            val config = configDao.getCompany()
            if(config != null) {
                company = config.value
            }
        }
        return company
    }

    suspend fun getUrl(): ConfigEntity? = configDao.getUrl()

    suspend fun getUrlString(): String {
        if(url.isEmpty()){
            val config = configDao.getUrl()
            if(config != null) {
                url = config.value
            }
        }
        return url
    }

    suspend fun clearCompanyAndURL(){
        val url = getUrl()
        val comp = getCompany()
        if(url != null) configDao.delete(url)
        if(comp != null) configDao.delete(comp)
    }

    companion object {
        const val TYPE_COMPANY: Int = 0
        const val TYPE_URL: Int = 1
        const val TYPE_PERMISSION: Int = 2
    }

    suspend fun getPermissionValue(name: String):String {
        if(permissionMap.isEmpty()){
            initMap()
        }
        return permissionMap[name] ?: ""
    }
}