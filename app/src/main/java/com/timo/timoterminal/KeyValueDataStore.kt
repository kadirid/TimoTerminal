package com.timo.timoterminal

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStoreFile
import com.timo.timoterminal.utils.enums.DataStoreKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent


//This is a persistent key value store for smaller information
class KeyValueDataStore(private val context: Context) : KoinComponent {
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile("key_value_store")
    }

    suspend fun saveKeyValue(key: DataStoreKeys, value: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key.name)] = value
        }
    }

    suspend fun getExampleValueAsStatic(key: DataStoreKeys): String? {
        val preferences = dataStore.data.first()
        return preferences[stringPreferencesKey(key.name)]
    }

    fun getValueAsFlow(key: DataStoreKeys) : Flow<String?> {
        return dataStore.data.map { value: Preferences ->
            value[stringPreferencesKey(key.name)]
        }
    }

}