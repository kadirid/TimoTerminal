package com.timo.timoterminal.service

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import okhttp3.MediaType.Companion.toMediaType
import java.util.UUID
import java.util.concurrent.TimeUnit

class WorkerService(application: Application) {
    private val workManager : WorkManager = WorkManager.getInstance(application.applicationContext)
    private val requests: HashMap<UUID, WorkRequest> = HashMap()

    companion object {
        @Volatile
        private var INSTANCE: WorkerService? = null
        var mediaType = "application/json; charset=utf-8".toMediaType()

        fun getInstance(application: Application) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: WorkerService(application).also {
                    INSTANCE = it
                }
            }
    }

    /**
     * Adds a periodic task to a specific lifecycle of the application
     * @param workerClass The class defines what should be done periodically. See HearbeatWorker.kt for instance
     * @param repeatInterval Interval, in which the method is called
     * @param timeUnit Tells in which timescale the interval has to be understood
     * @param observeCallback for defining an observer which supervises the worker, can be used to detect finish of the work
     * @param lifecycleOwner tells the worker in which scope this worker has to run. Usually Fragment oder Activity
     */
    fun addPeriodicRequest(
        workerClass: Class<out ListenableWorker?>,
        repeatInterval: Long,
        timeUnit: TimeUnit,
        observeCallback: (workInfo: WorkInfo) -> Unit,
        lifecycleOwner: LifecycleOwner
    ): UUID {
        val workerRequest: WorkRequest =
            PeriodicWorkRequest.Builder(workerClass, repeatInterval, timeUnit).build()
        Log.d("WORKER MANAGER", "addPeriodicTimeRequest: added ${workerRequest.id} to hashmap")
        workManager.enqueue(workerRequest)
        requests[workerRequest.id] = workerRequest
        workManager.getWorkInfoByIdLiveData(workerRequest.id).observe(lifecycleOwner, observeCallback)

        return workerRequest.id
    }


    /**
     * Adds a one time task to a specific lifecycle of the application. Only runs once
     * @param workerClass The class defines what should be done periodically. See HearbeatWorker.kt for instance
     * @param observeCallback for defining an observer which supervises the worker, can be used to detect finish of the work
     * @param lifecycleOwner tells the worker in which scope this worker has to run. Usually Fragment oder Activity
     */
    fun addOneTimeRequest(
        workerClass: Class<out ListenableWorker?>,
        observeCallback: (workInfo: WorkInfo) -> Unit,
        lifecycleOwner: LifecycleOwner
    ): UUID {
        val workerRequest: WorkRequest = OneTimeWorkRequest.Builder(workerClass).build()
        workManager.enqueue(workerRequest)
        Log.d("WORKER MANAGER", "addOneTimeRequest: added ${workerRequest.id} to hashmap")
        workManager.getWorkInfoByIdLiveData(workerRequest.id)
            .observe(lifecycleOwner) {
                observeCallback(it)
                if (it.state.isFinished) {
                    requests.remove(it.id)
                    Log.d("WORKER MANAGER", "addOneTimeRequest: removed ${it.id} from hashmap")
                }
            }

        return workerRequest.id
    }

    fun killAllWorkers(){
        requests.clear()
    }
}