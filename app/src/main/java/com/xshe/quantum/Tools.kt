package com.xshe.quantum

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

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

    fun connectAndCheck(mContext: Context, hostName: String, callback: gacCallback) {
        val helper = InternetHelper()
        helper.testAndGetServer(hostName, object : InternetHelper.RequestCallback {
            override fun onSuccess(responseBody: String) {
                try {
                    val rootJson = JSONObject(responseBody)
                    roomNames.clear()
                    roomStatuses.clear()

                    if (rootJson.has("code") && rootJson.optInt("code") == 900) {
                        callback.onSuccess()
                        return
                    }

                    val roomNameArray = rootJson.optJSONArray("room_name_list")
                    val roomStatusArray = rootJson.optJSONArray("room_status_list")

                    if (roomNameArray != null && roomStatusArray != null) {
                        for (i in 0 until roomNameArray.length()) {
                            roomNames.add(roomNameArray.optString(i, "未知房间"))
                            roomStatuses.add(roomStatusArray.optBoolean(i, false))
                        }
                    }
                    callback.onSuccess()
                } catch (e: JSONException) {
                    callback.onFailure()
                }
            }

            override fun onFailure() {
                callback.onFailure()
            }
        })
    }

    fun enterRoom(mContext: Context, hostName: String, roomName: String, password: String, callback: gacCallback) {
        InternetHelper().enterRoom(mContext,hostName, roomName, password,object : InternetHelper.RoomRequestCallback {
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

    fun exitRoom(mContext: Context, hostName: String, roomName: String, callback: gacCallback) {
        InternetHelper().exitRoom(hostName, roomName, object : InternetHelper.RoomRequestCallback {
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