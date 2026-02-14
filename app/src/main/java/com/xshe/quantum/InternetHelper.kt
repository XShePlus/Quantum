package com.xshe.quantum

import android.content.Context
import android.os.Handler
import android.os.Looper
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class InternetHelper {
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    interface RequestCallback {
        fun onSuccess(responseBody: String)
        fun onFailure()
    }

    interface PAMCallback {
        fun onSuccess(p: Int, m: Int)
        fun onFailure()
    }

    interface RoomRequestCallback {
        fun onSuccess()
        fun onFailure()
    }

    // 统一处理 URL 格式
    fun formatUrl(host: String): String {
        val trimmed = host.trim()
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "http://$trimmed"
        }
    }

    fun testAndGetServer(url: String, callback: RequestCallback) {
        val mediaType = "text/plain; charset=utf-8".toMediaType()
        val requestBody = "Hello,Server".toRequestBody(mediaType)
        val request =
            Request.Builder().url("${formatUrl(url)}/api/connect").post(requestBody).build()

        thread {
            try {
                val response = okHttpClient.newCall(request).execute()
                val body = response.body?.string() ?: ""
                if (response.isSuccessful && body.isNotEmpty()) {
                    callback.onSuccess(body)
                } else {
                    callback.onFailure()
                }
            } catch (e: Exception) {
                callback.onFailure()
            }
        }
    }

    fun createRoom(
        url: String,
        roomName: String,
        maxNumber: Int,
        cancelTime: Int,
        password: String,
        callback: RoomRequestCallback
    ) {
        val jsonObject = JSONObject().apply {
            put("room_name", roomName)
            put("max_number", maxNumber)
            put("cancel_time", cancelTime)
            put("password", password)
        }
        val requestBody =
            jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request =
            Request.Builder().url("${formatUrl(url)}/api/create_room").post(requestBody).build()
        thread {
            try {
                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) callback.onSuccess() else callback.onFailure()
            } catch (e: Exception) {
                callback.onFailure()
            }
        }
    }

    fun enterRoom(
        mContext: Context,
        url: String,
        roomName: String,
        password: String,
        userName: String,
        callback: RoomRequestCallback
    ) {
        val jsonObject = JSONObject().apply {
            put("room_name", roomName)
            put("password", password)
            put("user_name", userName)
        }
        val requestBody =
            jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request =
            Request.Builder().url("${formatUrl(url)}/api/enter_room").post(requestBody).build()
        val tools = Tools()
        thread {
            try {
                val response = okHttpClient.newCall(request).execute()
                val result = response.body?.string() ?: ""
                // 使用 contains 容错换行符
                if (response.isSuccessful && result.contains("行")) {
                    callback.onSuccess()
                } else {
                    if (response.code == 401) tools.showToast(mContext, "房间已满")
                    else if (response.code == 402) tools.showToast(mContext, "密码错误")
                    callback.onFailure()
                }
            } catch (e: Exception) {
                callback.onFailure()
            }
        }
    }

    fun exitRoom(url: String, roomName: String, userName: String, callback: RoomRequestCallback) {
        val jsonObject = JSONObject().apply {
            put("room_name", roomName)
            put("user_name", userName)
        }
        val requestBody =
            jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request =
            Request.Builder().url("${formatUrl(url)}/api/exit_room").post(requestBody).build()

        thread {
            try {
                val response = okHttpClient.newCall(request).execute()
                val result = response.body?.string() ?: ""
                if (response.isSuccessful && result.contains("行")) {
                    callback.onSuccess()
                } else {
                    callback.onFailure()
                }
            } catch (e: Exception) {
                callback.onFailure()
            }
        }
    }

    fun getMessages(
        hostName: String,
        roomName: String,
        userName: String, // 新增
        callback: RequestCallback
    ) {
        val client = okHttpClient
        val url = formatUrl(hostName)
        val json = JSONObject().apply {
            put("room_name", roomName)
            put("user_name", userName) // 新增
        }
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder().url("$url/api/get_message").post(body).build()
        thread {
            try {
                val response = okHttpClient.newCall(request).execute()
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) callback.onSuccess(body) else callback.onFailure()
            } catch (e: Exception) {
                callback.onFailure()
            }
        }
    }

    fun appendMessage(
        url: String,
        roomName: String,
        message: String,
        callback: RoomRequestCallback
    ) {
        val jsonObject = JSONObject().apply {
            put("room_name", roomName)
            put("message", message)
        }
        val requestBody =
            jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request =
            Request.Builder().url("${formatUrl(url)}/api/append_message").post(requestBody).build()

        thread {
            try {
                val response = okHttpClient.newCall(request).execute()
                val result = response.body?.string() ?: ""
                if (response.isSuccessful && result.contains("行")) {
                    callback.onSuccess()
                } else {
                    callback.onFailure()
                }
            } catch (e: Exception) {
                callback.onFailure()
            }
        }
    }

    fun uploadMusic(url: String, roomName: String, file: File, callback: RoomRequestCallback) {
        val mediaType = when {
            file.name.endsWith(".flac", ignoreCase = true) -> "audio/flac".toMediaType()
            file.name.endsWith(".aac", ignoreCase = true) -> "audio/aac".toMediaType()
            else -> "audio/mpeg".toMediaType() // 默认为 mp3
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("room_name", roomName)
            .addFormDataPart("file", file.name, file.asRequestBody(mediaType))
            .build()

        val request = Request.Builder()
            .url("${formatUrl(url)}/api/upload")
            .post(requestBody)
            .build()

        thread {
            try {
                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) callback.onSuccess() else callback.onFailure()
            } catch (e: Exception) {
                callback.onFailure()
            }
        }
    }

    fun getMusicList(url: String, roomName: String, callback: RequestCallback) {
        val jsonObject = JSONObject().apply { put("room_name", roomName) }
        val requestBody =
            jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request =
            Request.Builder().url("${formatUrl(url)}/api/list_songs").post(requestBody).build()

        thread {
            try {
                val response = okHttpClient.newCall(request).execute()
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) callback.onSuccess(body) else callback.onFailure()
            } catch (e: Exception) {
                callback.onFailure()
            }
        }
    }

    fun getMusicStatus(
        hostName: String,
        roomName: String,
        userName: String,
        callback: RequestCallback
    ) {
        val client = okHttpClient
        val url = formatUrl(hostName)
        val json = JSONObject().apply {
            put("room_name", roomName)
            put("user_name", userName) 
        }
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder().url("$url/api/get_music_status").post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = callback.onFailure()
            override fun onResponse(call: Call, response: Response) {
                callback.onSuccess(response.body?.string() ?: "")
            }
        })
    }

    fun getPAMNumber(
        hostName: String,
        roomName: String,
        callback: PAMCallback
    ) {
        val url = if (hostName.startsWith("http")) hostName else "http://$hostName"
        val json = JSONObject().apply {
            put("room_name", roomName)
        }
        val body =
            json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("$url/api/get_numbers")
            .post(body)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val bodyString = it.body?.string() ?: ""
                        val json = JSONObject(bodyString)
                        val p = json.optInt("present_number")
                        val m = json.optInt("max_number")

                        // 切回主线程更新 UI
                        Handler(Looper.getMainLooper()).post {
                            callback.onSuccess(p = p, m = m)
                        }
                    }

                }
            }
        })
    }

    fun updateMusicStatus(
        hostName: String,
        roomName: String,
        isPause: Boolean,
        time: Int,
        musicName: String,
        callback: RoomRequestCallback
    ) {
        val client = OkHttpClient()
        val url = if (hostName.startsWith("http")) hostName else "http://$hostName"

        val json = JSONObject().apply {
            put("room_name", roomName)
            put("is_music_pause", isPause)
            put("current_music_time", time)
            put("current_music", musicName)
        }

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url("$url/api/update_music_status")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) callback.onSuccess() else callback.onFailure()
            }
        })
    }

    // 获取音频流地址
    fun getStreamUrl(hostName: String, roomName: String, fileName: String): String {
        val url = if (hostName.startsWith("http")) hostName else "http://$hostName"
        return "$url/api/stream/$roomName/$fileName"
    }
}