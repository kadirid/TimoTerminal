package com.timo.timoterminal

import android.app.Application
import com.zkteco.android.core.sdk.koinModules
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class MainApplication : Application(){

    override fun onCreate() {
        startKoin {
            androidContext(this@MainApplication)
            workManagerFactory()
            modules(koinModules + appModule)
        }
        super.onCreate()
    }

}