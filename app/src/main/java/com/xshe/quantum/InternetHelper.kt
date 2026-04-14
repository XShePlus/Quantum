package com.xshe.quantum

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

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

    fun connectSSE(
        hostName: String,
        roomName: String,
        userName: String,
        onEvent: (type: String, data: String) -> Unit,
        onDisconnect: () -> Unit
    ): okhttp3.Call {
        val sseClient = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.SECONDS) // SSE 必须关闭读超时
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()

        val url = "${formatUrl(hostName)}/api/sse" +
                "?room=${Uri.encode(roomName)}&user=${Uri.encode(userName)}"
        val request = Request.Builder().url(url).get().build()

        val call = sseClient.newCall(request)
        thread(name = "SSEReaderThread") {
            try {
                call.execute().use { response ->
                    val source = response.body?.source() ?: return@thread
                    var eventType = "message"
                    val dataBuffer = StringBuilder()

                    while (!call.isCanceled()) {
                        val line = source.readUtf8Line() ?: break
                        when {
                            line.startsWith("event:") -> eventType = line.removePrefix("event:").trim()
                            line.startsWith("data:") -> dataBuffer.append(line.removePrefix("data:").trim())
                            line.isEmpty() -> { // 空行代表事件结束
                                if (dataBuffer.isNotEmpty()) {
                                    onEvent(eventType, dataBuffer.toString())
                                }
                                dataBuffer.clear()
                                eventType = "message"
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (!call.isCanceled()) {
                    Log.e("SSE", "Disconnected: ${e.message}")
                    onDisconnect()
                }
            }
        }
        return call
    }

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
        val request = Request.Builder().url("$url/api/get_message").post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = callback.onFailure()
            override fun onResponse(call: Call, response: Response) {
                callback.onSuccess(response.body?.string() ?: "")
            }
        })
    }

    fun verifyConnect(hostName: String, callback: RequestCallback) {
        val client = okHttpClient
        val url = formatUrl(hostName)
        val request = Request.Builder().url("$url/api/connect").post("".toRequestBody()).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = callback.onFailure()
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    try {
                        JSONObject(body)
                        callback.onSuccess(body)
                    } catch (e: Exception) {
                        callback.onFailure()
                    }
                } else {
                    callback.onFailure()
                }
            }
        })
    }

    fun getExampleCoverUrl(hostName: String, fileName: String): String {
        val baseUrl = formatUrl(hostName)
        val encodedName = Uri.encode(fileName)
        return "$baseUrl/api/cover/example/$encodedName"
    }

    fun getRoomCoverUrl(hostName: String, roomName: String, fileName: String): String {
        val baseUrl = formatUrl(hostName)
        val encodedRoom = Uri.encode(roomName)
        val encodedFile = Uri.encode(fileName)
        return "$baseUrl/api/cover/$encodedRoom/$encodedFile"
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
            else -> "audio/mpeg".toMediaType()
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
        val json = JSONObject().apply {
            put("room_name", roomName)
            put("user_name", userName)
        }
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val fullUrl = try { "${formatUrl(hostName)}/api/get_music_status" } catch(e:Exception){ "" }
        if(fullUrl.isEmpty()) { callback.onFailure(); return }

        val request = Request.Builder().url(fullUrl).post(body).build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = callback.onFailure()
            override fun onResponse(call: Call, response: Response) {
                val resBody = response.body?.string() ?: ""
                if (response.isSuccessful && resBody.isNotBlank()) {
                    callback.onSuccess(resBody)
                } else {
                    callback.onFailure()
                }
            }
        })
    }

    /**
     * getMusicStatus 的挂起版本，供 MusicSyncWorker（协程环境）调用。
     * 通过 suspendCancellableCoroutine 将回调桥接为 suspend fun，
     * 返回解析好的 JSONObject，失败时返回 null。
     */
    suspend fun getMusicStatusSuspend(
        hostName: String,
        roomName: String,
        userName: String
    ): JSONObject? = suspendCancellableCoroutine { cont ->
        getMusicStatus(hostName, roomName, userName, object : RequestCallback {
            override fun onSuccess(responseBody: String) {
                val result = try { JSONObject(responseBody) } catch (e: Exception) { null }
                cont.resume(result)
            }
            override fun onFailure() {
                cont.resume(null)
            }
        })
    }

    fun searchExampleSongs(hostName: String, keyword: String, page: Int, pageSize: Int, callback: RequestCallback) {
        val url = formatUrl(hostName)
        val encodedKeyword = Uri.encode(keyword)
        val request = Request.Builder()
            .url("$url/api/search_example_songs?q=$encodedKeyword&page=$page&page_size=$pageSize")
            .get()
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = callback.onFailure()
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) callback.onSuccess(body) else callback.onFailure()
            }
        })
    }

    fun getServerVersion(hostName: String, callback: RequestCallback) {
        val request = Request.Builder()
            .url("${formatUrl(hostName)}/api/version")
            .get()
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = callback.onFailure()
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (response.isSuccessful && body.isNotBlank()) callback.onSuccess(body)
                else callback.onFailure()
            }
        })
    }

    fun getPAMNumber(
        hostName: String,
        roomName: String,
        callback: PAMCallback
    ) {
        val url = formatUrl(hostName)
        val json = JSONObject().apply { put("room_name", roomName) }
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder().url("$url/api/get_numbers").post(body).build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { callback.onFailure() }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string() ?: ""
                if (response.isSuccessful && bodyString.isNotBlank()) {
                    try {
                        val jsonObj = JSONObject(bodyString)
                        val p = jsonObj.optInt("present_number")
                        val m = jsonObj.optInt("max_number")
                        // 注意：此回调切回了主线程，与其他方法行为不一致。
                        // 建议调用方统一自行切线程，此处保留原行为以免影响现有调用。
                        Handler(Looper.getMainLooper()).post {
                            callback.onSuccess(p, m)
                        }
                    } catch (e: Exception) {
                        callback.onFailure()
                    }
                } else {
                    callback.onFailure()
                }
            }
        })
    }

    fun checkIsIn(
        hostName: String,
        roomName: String,
        userName: String,
        callback: RoomRequestCallback
    ) {
        val url = formatUrl(hostName)
        val json = JSONObject().apply {
            put("room_name", roomName)
            put("user_name", userName)
        }
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("$url/api/check_is_in")
            .post(body)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = callback.onFailure()
            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    try {
                        val status = JSONObject(bodyStr).optString("status", "")
                        if (status == "in_room") callback.onSuccess()  // 还在
                        else callback.onFailure()                       // need_exit 或其他
                    } catch (e: Exception) {
                        callback.onFailure()
                    }
                } else {
                    callback.onFailure()
                }
            }
        })
    }

    fun updateMusicStatus(
        hostName: String,
        roomName: String,
        userName: String,
        isPause: Boolean,
        time: Int,
        musicName: String,
        isExample: Boolean,
        updateTime: Long = System.currentTimeMillis(),
        callback: RoomRequestCallback
    ) {
        val client = okHttpClient
        val url = formatUrl(hostName)

        val json = JSONObject().apply {
            put("room_name", roomName)
            put("user_name", userName)
            put("is_music_pause", isPause)
            put("current_music_time", time)
            put("current_music", musicName)
            put("is_example", isExample)
            put("update_time", updateTime)
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("$url/api/update_music_status")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = callback.onFailure()
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) callback.onSuccess() else callback.onFailure()
            }
        })
    }

    /**
     * 修复7：原代码 fileName 和 roomName 没有 Uri.encode()，
     * 文件名含空格或中文时 URL 非法，请求会失败。
     * 对比 getRoomCoverUrl 已正确编码，此处补齐。
     */
    fun getStreamUrl(hostName: String, roomName: String, fileName: String): String {
        val url = if (hostName.startsWith("http")) hostName else "http://$hostName"
        val encodedRoom = Uri.encode(roomName)
        val encodedFile = Uri.encode(fileName)
        return "$url/api/stream/$encodedRoom/$encodedFile"
    }

    fun getExampleMusicList(hostName: String, callback: RequestCallback) {
        val url = formatUrl(hostName)
        val request = Request.Builder()
            .url("$url/api/list_example_songs")
            .get()
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = callback.onFailure()
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) callback.onSuccess(body) else callback.onFailure()
            }
        })
    }

    fun getExampleStreamUrl(hostName: String, fileName: String): String {
        val baseUrl = formatUrl(hostName)
        val encodedFile = Uri.encode(fileName)
        return "$baseUrl/api/stream_example/$encodedFile"
    }

    fun setExampleMode(
        hostName: String,
        roomName: String,
        userName: String,
        exampleMode: Boolean,
        callback: RoomRequestCallback
    ) {
        val json = JSONObject().apply {
            put("room_name", roomName)
            put("user_name", userName)
            put("example_mode", exampleMode)
        }
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("${formatUrl(hostName)}/api/set_example_mode")
            .post(body)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = callback.onFailure()
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) callback.onSuccess() else callback.onFailure()
            }
        })
    }
}