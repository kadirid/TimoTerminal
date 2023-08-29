package com.timo.timoterminal.worker

import android.content.Context
import android.util.Log
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
        Log.d("WORKER", "doWork: HI WORKER")
        return Result.success()
    }

}