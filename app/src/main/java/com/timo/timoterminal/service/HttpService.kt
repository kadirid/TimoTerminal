package com.timo.timoterminal.service

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import com.timo.timoterminal.service.utils.ProgressListener
import com.timo.timoterminal.service.utils.ProgressResponseBody
import com.timo.timoterminal.worker.HeartbeatWorker
import com.zkteco.android.core.sdk.service.FingerprintService
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
import org.koin.core.component.inject
import java.io.IOException
import java.util.concurrent.TimeUnit


class HttpService : KoinComponent {
    val fingerPrintService : FingerprintService by inject()

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

    fun get(
        url: String,
        parameters: Map<String, String>?,
        successCallback: (objResponse: JSONObject?, arrResponse : JSONArray?, strResponse : String?) -> Unit?,
        errorCallback: (e: Exception) -> Unit?
    ) {
        val route = appendParametersToUrl(url, parameters)
        val request = Request.Builder().url(route).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseStr = response.body!!.string()
                if (responseStr.startsWith("{")) {
                    val obj = JSONObject(responseStr)
                    successCallback( obj, null, null)
                } else if (responseStr.startsWith("[")) {
                    val obj = JSONArray(responseStr)
                    successCallback( null, obj, null)
                } else {
                    successCallback(null, null, responseStr)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                errorCallback(e)
            }
        })
    }

    fun post(
        url: String,
        parameters: Map<String, String>,
        successCallback: (objResponse: JSONObject?, arrResponse : JSONArray?, strResponse : String?) -> Unit?,
        errorCallback: (e: Exception) -> Unit?
    ) {
        val formBody: RequestBody = createRequestBody(parameters)
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseStr = response.body!!.string()
                if (responseStr.startsWith("{")) {
                    val obj = JSONObject(responseStr)
                    successCallback( obj, null, null)
                } else if (responseStr.startsWith("[")) {
                    val obj = JSONArray(responseStr)
                    successCallback( null, obj, null)
                } else {
                    successCallback(null, null, responseStr)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                errorCallback(e)
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