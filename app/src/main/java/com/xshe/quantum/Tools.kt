package com.xshe.quantum

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.coroutines.resume

class Tools {
    interface gacCallback {
        fun onSuccess()
        fun onFailure()
    }

    data class MusicItem(
        val uri: android.net.Uri,
        val name: String
    )

    val roomNames = mutableListOf<String>()
    val roomStatuses = mutableListOf<Boolean>()
    var userName=""
    var isPlayingExample = false
    fun showToast(context: Context, msg: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun addRoom(
        mContext: Context,
        hostName: String,
        roomName: String,
        maxNumber: Int,
        cancelTime: Int,
        password: String,
        callback: gacCallback
    ) {
        if (roomName.trim().isEmpty() || hostName.trim().isEmpty()) {
            showToast(mContext, "信息不能为空")
            callback.onFailure()
            return
        }

        InternetHelper().createRoom(
            hostName,
            roomName,
            maxNumber,
            cancelTime,
            password,
            object : InternetHelper.RoomRequestCallback {
                override fun onSuccess() {
                    connectAndCheck(mContext, hostName, object : gacCallback {
                        override fun onSuccess() {
                            callback.onSuccess()
                        }

                        override fun onFailure() {
                            callback.onSuccess()
                        }
                    })
                }

                override fun onFailure() {
                    showToast(mContext, "创建失败")
                    callback.onFailure()
                }
            })
    }


    suspend fun searchExampleMusicSuspend(hostName: String, keyword: String, page: Int, pageSize: Int): Pair<List<String>, Int> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { cont ->
                InternetHelper().searchExampleSongs(hostName, keyword, page, pageSize,
                    object : InternetHelper.RequestCallback {
                        override fun onSuccess(responseBody: String) {
                            try {
                                val json = JSONObject(responseBody)
                                val arr = json.getJSONArray("songs")
                                val total = json.optInt("total", 0)
                                val list = (0 until arr.length()).map { arr.getString(it) }
                                cont.resume(Pair(list, total))
                            } catch (e: Exception) {
                                cont.resume(Pair(emptyList(), 0))
                            }
                        }
                        override fun onFailure() {
                            cont.resume(Pair(emptyList(), 0))
                        }
                    }
                )
            }
        }
    }

    fun connectAndCheck(mContext: android.content.Context, hostName: String, callback: gacCallback) {
        InternetHelper().verifyConnect(hostName, object : InternetHelper.RequestCallback {
            override fun onSuccess(responseBody: String) {
                try {
                    val json = JSONObject(responseBody)
                    Handler(Looper.getMainLooper()).post {
                        if (json.has("code") && json.getInt("code") == 900) {
                            resetLocalData()
                        } else {
                            val names = json.getJSONArray("room_name_list")
                            val statuses = json.getJSONArray("room_status_list")
                            roomNames.clear()
                            roomStatuses.clear()
                            for (i in 0 until names.length()) {
                                roomNames.add(names.getString(i))
                                roomStatuses.add(statuses.getBoolean(i))
                            }
                        }
                        callback.onSuccess()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback.onFailure()
                }
            }

            override fun onFailure() {
                callback.onFailure()
            }
        })
    }

    fun fetchExampleMusicList(
        hostName: String,
        page: Int = 1,
        pageSize: Int = 20,
        onResult: (List<String>, total: Int) -> Unit
    ) {
        val url = "${InternetHelper().formatUrl(hostName)}/api/list_example_songs?page=$page&page_size=$pageSize"
        val request = Request.Builder().url(url).get().build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post { onResult(emptyList(), 0) }
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    try {
                        val json = JSONObject(body)
                        val songs = json.getJSONArray("songs").let { arr ->
                            List(arr.length()) { arr.getString(it) }
                        }
                        val total = json.getInt("total")
                        Handler(Looper.getMainLooper()).post { onResult(songs, total) }
                    } catch (e: Exception) {
                        Handler(Looper.getMainLooper()).post { onResult(emptyList(), 0) }
                    }
                } else {
                    Handler(Looper.getMainLooper()).post { onResult(emptyList(), 0) }
                }
            }
        })
    }

    suspend fun fetchMusicListSuspend(hostName: String, roomName: String): List<String> =
        suspendCancellableCoroutine { continuation ->
            fetchMusicList(hostName, roomName) { list ->
                if (continuation.isActive) {
                    continuation.resume(list)
                }
            }
        }

    suspend fun fetchExampleMusicListSuspend(
        hostName: String,
        page: Int = 1,
        pageSize: Int = 20
    ): Pair<List<String>, Int> = suspendCancellableCoroutine { continuation ->
        fetchExampleMusicList(hostName, page, pageSize) { list, total ->
            if (continuation.isActive) {
                continuation.resume(Pair(list, total))
            }
        }
        continuation.invokeOnCancellation {
        }
    }
    fun enterRoom(
        mContext: Context,
        hostName: String,
        roomName: String,
        password: String,
        userName: String,
        callback: gacCallback
    ) {
        InternetHelper().enterRoom(
            mContext,
            hostName,
            roomName,
            password,
            userName,
            object : InternetHelper.RoomRequestCallback {
                override fun onSuccess() {
                    connectAndCheck(mContext, hostName, object : gacCallback {
                        override fun onSuccess() {
                            callback.onSuccess()
                        }

                        override fun onFailure() {
                            callback.onFailure()
                        }
                    })
                }

                override fun onFailure() {
                    callback.onFailure()
                }
            })
    }

    fun exitRoom(mContext: Context, hostName: String, roomName: String, userName: String,callback: gacCallback) {
        InternetHelper().exitRoom(hostName, roomName, userName,object : InternetHelper.RoomRequestCallback {
            override fun onSuccess() {
                connectAndCheck(mContext, hostName, object : gacCallback {
                    override fun onSuccess() {
                        callback.onSuccess()
                    }

                    override fun onFailure() {
                        callback.onSuccess()
                    }
                })
            }

            override fun onFailure() {
                showToast(mContext, "退出失败")
                callback.onFailure()
            }
        })
    }

    fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }

    object ImageCache {
        private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        private val cacheSize = maxMemory / 8

        private val lruCache = object : android.util.LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, value: Bitmap): Int {
                return value.byteCount / 1024
            }
        }

        fun get(url: String): Bitmap? = lruCache.get(url)
        fun put(url: String, bitmap: Bitmap) {
            if (get(url) == null) {
                lruCache.put(url, bitmap)
            }
        }
    }

    fun uploadMusicFile(
        context: Context,
        hostName: String,
        roomName: String,
        uri: Uri,
        callback: gacCallback
    ) {
        val fileName = getFileName(context, uri) ?: "temp_audio"
        val file = File(context.cacheDir, fileName)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
        } catch (e: Exception) {
            callback.onFailure()
            return
        }

        InternetHelper().uploadMusic(
            hostName,
            roomName,
            file,
            object : InternetHelper.RoomRequestCallback {
                override fun onSuccess() {
                    file.delete(); callback.onSuccess()
                }

                override fun onFailure() {
                    file.delete(); callback.onFailure()
                }
            })
    }

    suspend fun getAudioAlbumArt(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(url, HashMap<String, String>())
                val artBytes = retriever.embeddedPicture
                if (artBytes != null) {
                    BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                retriever.release()
            }
        }
    }

    fun fetchMusicList(hostName: String, roomName: String, onResult: (List<String>) -> Unit) {
        InternetHelper().getMusicList(hostName, roomName, object : InternetHelper.RequestCallback {
            override fun onSuccess(responseBody: String) {
                val list = mutableListOf<String>()
                try {
                    val jsonArray = JSONArray(responseBody)
                    for (i in 0 until jsonArray.length()) {
                        list.add(jsonArray.getString(i))
                    }
                    Handler(Looper.getMainLooper()).post { onResult(list) }
                } catch (e: Exception) {
                    onResult(emptyList())
                }
            }

            override fun onFailure() {
                onResult(emptyList())
            }
        })
    }

    fun resetLocalData() {
        roomNames.clear()
        roomStatuses.clear()
    }

    fun formatTime(milliseconds: Int): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}