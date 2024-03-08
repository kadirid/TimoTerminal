package com.timo.timoterminal.service

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.timo.timoterminal.R
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.service.serviceUtils.ProgressListener
import com.timo.timoterminal.service.serviceUtils.ProgressResponseBody
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.ResponseToJSON
import mcv.facepass.BuildConfig
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException
import java.util.concurrent.TimeUnit


class HttpService() : KoinComponent {
    private val sharedPrefService: SharedPrefService by inject()

    private var client: OkHttpClient = OkHttpClient().newBuilder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
//        .retryOnConnectionFailure(false)
        .build()


    companion object {
        var mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

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

        fun handleGenericRequestError(
            e: Exception?,
            response: Response?,
            context: Context?,
            output: ResponseToJSON?
        ) = handleGenericRequestError(e, response, context, output, null)

        fun handleGenericRequestError(
            e: Exception?,
            response: Response?,
            context: Context?,
            output: ResponseToJSON?,
            msg: String?
        ) {
            //If the context is null, we cannot show a dialogue
            if (context != null) {
                Handler(Looper.getMainLooper()).post {
                    val dialog = MaterialAlertDialogBuilder(context, R.style.MySingleButtonDialog)
                    dialog.setTitle(context.getString(R.string.error))
                    dialog.setIcon(
                        AppCompatResources.getDrawable(
                            context, R.drawable.baseline_error_24
                        )
                    )
                    //If you are running the app on debug mode, the url will appear next to the error message
                    val debugAttachString =
                        if (BuildConfig.DEBUG && response != null) "\n${response.request.url}" else ""
                    if (output != null && response != null) {
                        if (output.obj != null) {
                            val message = output.obj.getString("message")
                            dialog.setMessage(message + debugAttachString)
                        } else if (output.array != null) {
                            dialog.setMessage(output.array.toString())
                        } else {
                            dialog.setMessage("${response.code}: ${output.string}" + debugAttachString)
                        }
                    } else {
                        if (e != null) {
                            dialog.setMessage(e.message)
                        } else {
                            dialog.setMessage(context.getString(R.string.unknown_error) + debugAttachString)
                        }
                    }
                    if (msg != null) {
                        dialog.setMessage(msg)
                    }
                    dialog.setPositiveButton("OK") { dia, _ ->
                        dia.dismiss()
                    }
                    val dia = dialog.create()
                    Utils.hideNavInDialog(dia)
                    dia.setOnShowListener {
                        val textView = dia.findViewById<TextView>(android.R.id.message)
                        textView?.textSize = 40f
                    }
                    dia.show()
                    val positiveButton: Button = dia.getButton(AlertDialog.BUTTON_POSITIVE)

                    val parent = positiveButton.parent as LinearLayout
                    parent.gravity = Gravity.CENTER_HORIZONTAL
                    val leftSpacer = parent.getChildAt(1)
                    leftSpacer.visibility = View.GONE
                }
            }
        }
    }

    fun get(
        url: String,
        parameters: Map<String, String>?,
        context: Context?,
        successCallback: (objResponse: JSONObject?, arrResponse: JSONArray?, strResponse: String?) -> Unit?,
        errorCallback: ((e: Exception?, response: Response?, context: Context?, output: ResponseToJSON?) -> Unit?) = { e, res, context, output ->
            handleGenericRequestError(
                e, res, context, output
            )
        }
    ) {
        val route = appendParametersToUrl(url, parameters)
        val request = Request.Builder().url(route).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                var responseString = response.body!!.string()

                if (responseString.isEmpty()) {
                    responseString = response.message
                }
                val output: ResponseToJSON = Utils.parseResponseToJSON(responseString)

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
        successCallback: (objResponse: JSONObject?, arrResponse: JSONArray?, strResponse: String?) -> Unit?,
        errorCallback: ((e: Exception?, response: Response?, context: Context?, output: ResponseToJSON?) -> Unit) = { e, response, context, output ->
            handleGenericRequestError(
                e, response, context, output
            )
        }
    ) {
        postWithClient(client, url, parameters, context, successCallback, errorCallback)
    }

    fun postWithClient(
        client: OkHttpClient,
        url: String,
        parameters: Map<String, String>,
        context: Context?,
        successCallback: (objResponse: JSONObject?, arrResponse: JSONArray?, strResponse: String?) -> Unit?,
        errorCallback: (e: Exception?, response: Response?, context: Context?, output: ResponseToJSON?) -> Unit
    ) {
        val formBody: RequestBody = createRequestBody(parameters)
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                var responseString = response.body!!.string()

                if (responseString.isEmpty()) {
                    responseString = response.message
                }
                val output: ResponseToJSON = Utils.parseResponseToJSON(responseString)
                if (response.code == 200 || response.isSuccessful) {
                    successCallback(output.obj, output.array, output.string)
                } else {
                    errorCallback(null, response, context, output)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                errorCallback(e, null, context, null)
            }
        })
    }

    fun postJson(
        url: String,
        parametersJson: String,
        context: Context?,
        successCallback: (objResponse: JSONObject?, arrResponse: JSONArray?, strResponse: String?) -> Unit?,
        errorCallback: (e: Exception?, response: Response?, context: Context?, output: ResponseToJSON?) -> Unit = { e, response, context, output ->
            handleGenericRequestError(
                e, response, context, output
            )
        }
    ) {
        val formBody = parametersJson.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                var responseString = response.body!!.string()

                if (responseString.isEmpty()) {
                    responseString = response.message
                }
                val output: ResponseToJSON = Utils.parseResponseToJSON(responseString)
                if (response.code == 200 || response.isSuccessful) {
                    successCallback(output.obj, output.array, output.string)
                } else {
                    errorCallback(null, response, context, output)
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

        val dlClient = client.newBuilder().addNetworkInterceptor(Interceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            originalResponse.newBuilder()
                .body(ProgressResponseBody(originalResponse.body!!, progressListener))
                .build()
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

    fun responseForCommand(unique: String) {
        val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY) ?: ""
        val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL) ?: ""
        val terminalId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
        val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN, "") ?: ""

        if (company.isNotEmpty() && terminalId > 0 && token.isNotEmpty() && unique.isNotEmpty()) {
            post(
                "${url}services/rest/zktecoTerminal/doneCommand",
                mapOf(
                    Pair("company", company),
                    Pair("terminalId", "$terminalId"),
                    Pair("token", token),
                    Pair("unique", unique)
                ), null, { _, _, _ -> }, { _, _, _, _ -> })
        }
    }
}