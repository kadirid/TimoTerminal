package com.timo.timoterminal.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class HeartbeatWorker(context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {

    /*
    You can inject any service like this in here. Notice that you have to add the worker to the module
    to inject it properly!
    private val httpService : HttpService by inject()
     */

    override fun doWork(): Result {
        //....
        return Result.success()
    }

}