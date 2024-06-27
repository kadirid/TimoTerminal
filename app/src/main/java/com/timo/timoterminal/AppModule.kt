package com.timo.timoterminal

import androidx.room.Room
import com.timo.timoterminal.database.BookingBUDatabase
import com.timo.timoterminal.database.BookingDatabase
import com.timo.timoterminal.database.ConfigDatabase
import com.timo.timoterminal.database.LanguageDatabase
import com.timo.timoterminal.database.UserDatabase
import com.timo.timoterminal.repositories.BookingBURepository
import com.timo.timoterminal.repositories.BookingRepository
import com.timo.timoterminal.repositories.ConfigRepository
import com.timo.timoterminal.repositories.LanguageRepository
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.BookingService
import com.timo.timoterminal.service.HeartbeatService
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.LoginService
import com.timo.timoterminal.service.PropertyService
import com.timo.timoterminal.service.SettingsService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.service.UserService
import com.timo.timoterminal.service.WebSocketService
import com.timo.timoterminal.service.WorkerService
import com.timo.timoterminal.utils.classes.SoundSource
import com.timo.timoterminal.viewModel.AttendanceFragmentViewModel
import com.timo.timoterminal.viewModel.InfoFragmentViewModel
import com.timo.timoterminal.viewModel.InternalTerminalSettingsFragmentViewModel
import com.timo.timoterminal.viewModel.LoginActivityViewModel
import com.timo.timoterminal.viewModel.LoginFragmentViewModel
import com.timo.timoterminal.viewModel.MBRemoteRegisterSheetViewModel
import com.timo.timoterminal.viewModel.MBSheetFingerprintCardReaderViewModel
import com.timo.timoterminal.viewModel.MBUserWaitSheetViewModel
import com.timo.timoterminal.viewModel.MainActivityViewModel
import com.timo.timoterminal.viewModel.SettingsFragmentViewModel
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

    single {
        Room.databaseBuilder(
            androidContext(),
            LanguageDatabase::class.java,
            "language_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            BookingDatabase::class.java,
            "booking_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            BookingBUDatabase::class.java,
            "booking_bu_entity"
        ).build()
    }

    single { get<UserDatabase>().userDao() }
    single { get<ConfigDatabase>().configDao() }
    single { get<LanguageDatabase>().languageDao() }
    single { get<BookingDatabase>().bookingDao() }
    single { get<BookingBUDatabase>().bookingBUDao() }

    single { UserRepository(get()) }
    single { ConfigRepository(get()) }
    single { LanguageRepository(get()) }
    single { BookingRepository(get()) }
    single { BookingBURepository(get()) }

    single { HttpService() }
    single { WebSocketService() }
    single { HeartbeatService() }
    single { SharedPrefService(androidContext()) }
    single { PropertyService(androidContext()) }
    single { WorkerService(get()) }
    single { SoundSource(get(), androidContext()) }
    single { SettingsService(get(), get()) }
    single { LanguageService(get(), get(), get()) }
    single { UserService(get(), get(), get()) }
    single { BookingService(get(), get(), get(), get()) }
    single { LoginService(get(), get(), get(), get(), get(), get(), get(), get()) }


    viewModel { InternalTerminalSettingsFragmentViewModel(get()) }
    viewModel { MainActivityViewModel(get(), get(), get(), get()) }
    viewModel { InfoFragmentViewModel(get(), get(), get(), get()) }
    viewModel { LoginActivityViewModel(get(), get(), get(), get()) }
    viewModel { LoginFragmentViewModel(get(), get(), get(), get()) }
    viewModel { SettingsFragmentViewModel(get(), get(), get(), get()) }
    viewModel { AttendanceFragmentViewModel(get(), get(), get(), get()) }
    viewModel { MBUserWaitSheetViewModel(get(), get(), get(), get(), get()) }
    viewModel { UserSettingsFragmentViewModel(get(), get(), get(), get(), get()) }
    viewModel { MBRemoteRegisterSheetViewModel(get(), get(), get(), get(), get()) }
    viewModel {
        MBSheetFingerprintCardReaderViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    worker(named<HeartbeatWorker>()) {
        HeartbeatWorker(
            context = androidContext(),
            workerParameters = get()
        )
    }

}