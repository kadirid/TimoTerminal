package com.timo.timoterminal

import androidx.room.Room
import com.timo.timoterminal.database.DemoDatabase
import com.timo.timoterminal.repositories.DemoRepository
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.WebSocketService
import com.timo.timoterminal.viewModel.MainActivityViewModel
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

    single { get<DemoDatabase>().demoDao() }

    single {DemoRepository(get())}

    single {HttpService()}
    single {WebSocketService()}
    single {WorkerService(get())}

    viewModel { MainActivityViewModel() }

    worker(named<HeartbeatWorker>()) {
        HeartbeatWorker(
            context = androidContext(),
            workerParameters = get(),
        )
    }

}