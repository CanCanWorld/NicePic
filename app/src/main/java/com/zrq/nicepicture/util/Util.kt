package com.zrq.nicepicture.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.*
import java.io.IOException

object Util {

    const val TAG = "Util"

    fun httpGet(url: String, callBack: (Boolean, String) -> Unit) {
        Thread {
            val request: Request = Request.Builder()
                .url(url)
                .get()
                .build()
            Log.d(TAG, "httpGet: $url")
            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post {
                        callBack(false, "error1")
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.body != null) {
                        val json = response.body!!.string()
                        val headers = response.networkResponse!!.request.headers
                        try {
                            Handler(Looper.getMainLooper()).post {
                                callBack(true, json)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "httpGet: $e")
                            Handler(Looper.getMainLooper()).post {
                                callBack(false, "error3:$e")
                            }
                        }
                    } else {
                        Log.e(TAG, "httpGet: error2")
                        Handler(Looper.getMainLooper()).post {
                            callBack(false, "error2")
                        }
                    }
                }
            })
        }.start()
    }
}