package com.timo.timoterminal

import android.app.Application
import android.content.Context
import com.timo.timoterminal.utils.LocaleHelper
import com.zkteco.android.lcdk.ILcdk
import com.zkteco.android.lcdk.LcdkManager
import com.zkteco.android.lcdk.di.lcdkModule
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class MainApplication : Application(){
    companion object {
        lateinit var lcdk: ILcdk
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }

    override fun onCreate() {
        startKoin {
            androidContext(this@MainApplication)
            workManagerFactory()
            modules(lcdkModule + appModule)
        }
        lcdk = LcdkManager().connect()
        super.onCreate()
    }
}