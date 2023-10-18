package com.timo.timoterminal.service

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import org.koin.core.component.KoinComponent
import java.util.Properties


class PropertyService(context : Context) : KoinComponent {

    companion object {
        const val TAG = "PropertyService"
    }

    private var properties : Properties = Properties()

    init {
        try {
            val assetManager: AssetManager = context.assets
            val inputStream = assetManager.open("config.properties")
            properties.load(inputStream)
            Log.d(TAG, "Error: CONFIG LOADED")
        } catch (e : Exception) {
            Log.d(TAG, "Error: CONFIG COULD NOT BE INITIALIZED")
        }
    }

    fun getProperties() : Properties {
        return properties
    }
}
