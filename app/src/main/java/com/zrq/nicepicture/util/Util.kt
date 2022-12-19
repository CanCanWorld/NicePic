package com.zrq.nicepicture.util

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import okhttp3.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*

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

    fun saveImage(ctx: Context, url: String, callBack: (Boolean, String) -> Unit) {
        var bitmap: Bitmap? = null
        Thread {
            var picUrl: URL? = null
            try {
                picUrl = URL(url)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (picUrl != null) {
                var inputStream: InputStream? = null
                try {
                    val connect: HttpURLConnection = picUrl.openConnection() as HttpURLConnection
                    connect.doInput = true
                    connect.connect()
                    inputStream = connect.inputStream
                    bitmap = BitmapFactory.decodeStream(inputStream)
                } catch (e: IOException) {
                    e.printStackTrace()
                    callBack(false, "图片保存失败: error4")
                } finally {
                    inputStream?.close()
                }
            }

            if (bitmap != null) {
                val sdDir = ctx.getExternalFilesDir(null)
                val filePath = sdDir!!.absolutePath + File.separator + "nice_pic"
                Log.d(TAG, "saveImage: $filePath")
                val appDir = File(filePath)
                Log.d(TAG, "saveImage: ${appDir.exists()}")
                if (!appDir.exists()) {
                    val mkdir = appDir.mkdir()
                    Log.d(TAG, "saveImage: $mkdir")
                }
                val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
                val fileName = "LSP_$time.jpg"

                val typeFor = URLConnection.getFileNameMap().getContentTypeFor(fileName)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    val value = ContentValues()
                    value.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    value.put(MediaStore.MediaColumns.MIME_TYPE, typeFor)
                    value.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)

                    val contentResolver = ctx.contentResolver
                    val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value)
                    if (uri == null) {
                        callBack(false, "图片保存失败：error1")
                        return@Thread
                    }
                    var os: OutputStream? = null
                    try {
                        os = contentResolver.openOutputStream(uri)
                        val success = bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, os)
                        if (success) {
                            callBack(true, "图片保存成功")
                            ctx.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
                        } else {
                            callBack(false, "图片保存失败：error3")
                        }
                    } catch (e: IOException) {
                        callBack(false, "图片保存失败：error2")
                    } finally {
                        os?.flush()
                        os?.close()
                    }

                } else {
                    MediaScannerConnection.scanFile(ctx, arrayOf(filePath), arrayOf(typeFor)) { _, _ ->
                        callBack(true, "图片保存成功")
                    }
                }
            }
        }.start()
    }

}