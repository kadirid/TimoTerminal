package com.timo.timoterminal

import androidx.room.Room
import com.timo.timoterminal.database.ConfigDatabase
import com.timo.timoterminal.database.DemoDatabase
import com.timo.timoterminal.database.UserDatabase
import com.timo.timoterminal.repositories.ConfigRepository
import com.timo.timoterminal.repositories.DemoRepository
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.LoginService
import com.timo.timoterminal.service.PropertyService
import com.timo.timoterminal.service.SettingsService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.service.UserService
import com.timo.timoterminal.service.WebSocketService
import com.timo.timoterminal.service.WorkerService
import com.timo.timoterminal.viewModel.AttendanceFragmentViewModel
import com.timo.timoterminal.viewModel.InfoFragmentViewModel
import com.timo.timoterminal.viewModel.LoginActivityViewModel
import com.timo.timoterminal.viewModel.LoginFragmentViewModel
import com.timo.timoterminal.viewModel.MBSheetFingerprintCardReaderViewModel
import com.timo.timoterminal.viewModel.MainActivityViewModel
import com.timo.timoterminal.viewModel.UserSettingsFragmentViewModel
import com.timo.timoterminal.worker.HeartbeatWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.qualifier.named
import org.koin.dsl.module

var appModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            DemoDatabase::class.java,
            "demo_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            UserDatabase::class.java,
            "user_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            ConfigDatabase::class.java,
            "config_entity"
        ).build()
    }

    single { get<DemoDatabase>().demoDao() }
    single { get<UserDatabase>().userDao() }
    single { get<ConfigDatabase>().configDao() }

    single { DemoRepository(get()) }
    single { UserRepository(get()) }
    single { ConfigRepository(get()) }

    single { HttpService() }
    single { WebSocketService() }
    single { WorkerService(get()) }
    single { LoginService(get(), get(), get(), get()) }
    single { UserService(get(), get(), get()) }
    single { SharedPrefService(androidContext()) }
    single { SettingsService(get(), get()) }
    single { PropertyService(androidContext()) }

    viewModel { MainActivityViewModel(get(), get(), get(), get(), get()) }
    viewModel { UserSettingsFragmentViewModel(get(), get()) }
    viewModel { LoginActivityViewModel(get(), get(), get()) }
    viewModel { LoginFragmentViewModel(get(), get()) }
    viewModel { AttendanceFragmentViewModel(get()) }
    viewModel { InfoFragmentViewModel(get(), get(), get()) }

    worker(named<HeartbeatWorker>()) {
        HeartbeatWorker(
            context = androidContext(),
            workerParameters = get(),
        )
    }

}