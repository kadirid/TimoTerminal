package com.timo.timoterminal.service

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.timo.timoterminal.R
import com.timo.timoterminal.service.serviceUtils.ProgressListener
import com.timo.timoterminal.service.serviceUtils.ProgressResponseBody
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.ResponseToJSON
import com.timo.timoterminal.worker.HeartbeatWorker
import mcv.facepass.BuildConfig
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import java.io.IOException
import java.util.concurrent.TimeUnit


class HttpService : KoinComponent {

    private var client: OkHttpClient = OkHttpClient().newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        var mediaType = "application/json; charset=utf-8".toMediaType()

        fun buildUrl(scheme: String, host: String, params: HashMap<String, String>): HttpUrl {
            val httpBuilder = HttpUrl.Builder()
                .scheme(scheme)
                .host(host)
            for (entity in params) {
                httpBuilder.addQueryParameter(entity.key, entity.value)
            }
            return httpBuilder.build()
        }

        fun appendParametersToUrl(baseUrl: String, parameters: Map<String, String>?): String {
            val uriBuilder = Uri.parse(baseUrl).buildUpon()

            if (parameters != null) {
                for ((key, value) in parameters) {
                    uriBuilder.appendQueryParameter(key, value)
                }
            }

            return uriBuilder.build().toString()
        }

        fun createRequestBody(parameters: Map<String, String>): RequestBody {
            val formBodyBuilder = FormBody.Builder()

            for ((key, value) in parameters) {
                formBodyBuilder.add(key, value)
            }

            return formBodyBuilder.build()
        }
    }

    fun initHearbeatWorker(application: Application, lifecycleOwner: LifecycleOwner) {
        val workerService: WorkerService = WorkerService.getInstance(application)
        //Alle 15 Minuten wird jetzt der Code ausgeführt, der da definiert ist
        workerService.addPeriodicRequest(HeartbeatWorker::class.java, 15, TimeUnit.MINUTES, {} ,lifecycleOwner = lifecycleOwner)

    }

    fun killHeartBeatWorkers(application : Application){
        println("killklkhljkklkljukjh")
        val workerService: WorkerService = WorkerService.getInstance(application)
        workerService.killAllWorkers()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun handleGenericRequestError(e: Exception?, response: Response?, context: Context?, output : ResponseToJSON?) {
        //If the context is null, we cannot show a dialogue
        if (context != null && response != null) {
            Handler(Looper.getMainLooper()).post {
                val dialog = MaterialAlertDialogBuilder(context)
                dialog.setTitle(context.getString(R.string.error))
                dialog.setIcon(context.getDrawable(R.drawable.baseline_error_24))
                //If you are running the app on debug mode, the url will appear next to the error message
                val debugAttachString = if (BuildConfig.DEBUG) "\n${response.request.url}" else ""
                if (output != null) {
                    if (output.obj != null) {
                        val message = output.obj.getString("message")
                        dialog.setMessage(message + debugAttachString)
                    } else if (output.array != null) {
                        dialog.setMessage(output.array.toString())
                    } else {
                        dialog.setMessage("${response.code}: ${output.string}" +  debugAttachString)
                    }
                } else {
                    if (e != null) {
                        dialog.setMessage(e.message)
                    } else {
                        dialog.setMessage(context.getString(R.string.unknown_error)+ debugAttachString)
                    }
                }
                dialog.setPositiveButton("OK") {dia, _ ->
                    dia.dismiss()
                }
                dialog.show()
            }
        }
    }

    fun get(
        url: String,
        parameters: Map<String, String>?,
        context: Context?,
        successCallback: (objResponse: JSONObject?, arrResponse : JSONArray?, strResponse : String?) -> Unit?,
        errorCallback:  ((e: Exception?, response: Response?, context: Context?, output : ResponseToJSON?) -> Unit?) = { e, res, context, output -> handleGenericRequestError(e, res, context, output) }
    ) {
        val route = appendParametersToUrl(url, parameters)
        val request = Request.Builder().url(route).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                var responseString = response.body!!.string()

                if(responseString.isNullOrEmpty()) {
                    responseString = response.message
                }
                val output : ResponseToJSON = Utils.parseResponseToJSON(responseString)

                if (response.code == 200 || response.isSuccessful) {
                    successCallback(output.obj, output.array, output.string)
                } else {
                    errorCallback(null ,response, context, output)
                }

                if (response.isSuccessful || response.code == 200) {
                    successCallback(output.obj, output.array, output.string)
                } else {
                    errorCallback(null, response, context, output)
                    return
                }

            }

            override fun onFailure(call: Call, e: IOException) {
                errorCallback(e, null, context, null)
            }
        })
    }

    fun post(
        url: String,
        parameters: Map<String, String>,
        context: Context?,
        successCallback: (objResponse: JSONObject?, arrResponse : JSONArray?, strResponse : String?) -> Unit?,
        errorCallback:  ((e: Exception?, response: Response?, context: Context?, output: ResponseToJSON?) -> Unit) = { e, response, context, output -> handleGenericRequestError(e, response, context, output) }
    ) {
        val formBody: RequestBody = createRequestBody(parameters)
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                var responseString = response.body!!.string()

                if(responseString.isNullOrEmpty()) {
                    responseString = response.message
                }
                val output : ResponseToJSON = Utils.parseResponseToJSON(responseString)
                if (response.code == 200 || response.isSuccessful) {
                    successCallback(output.obj, output.array, output.string)
                } else {
                    errorCallback(null,response, context, output)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                errorCallback(e, null, context, null)
            }
        })
    }

    fun downloadFile(
        url: String,
        parameters: Map<String, String>,
        progressListener: ProgressListener,
        successCallback: (response: ByteArray) -> Any
    ) {
        val request = Request.Builder()
            .url(url)
            .build()

        val dlClient = client.newBuilder().addNetworkInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val originalResponse = chain.proceed(chain.request())
                return originalResponse.newBuilder()
                    .body(ProgressResponseBody(originalResponse.body!!, progressListener))
                    .build()
            }
        }).build()


        dlClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.body != null && response.code == 200) {
                    val res = response.body!!.bytes()
                    successCallback(res)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
            }
        })
    }
}