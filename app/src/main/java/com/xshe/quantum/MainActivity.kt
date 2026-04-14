package com.xshe.quantum

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.xshe.quantum.ui.theme.QuantumTheme
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import org.json.JSONArray
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.produceState

class MainActivity : ComponentActivity() {

    /**
     * Activity 入口。
     * 启用沉浸式边到边显示，处理通知点击恢复房间状态，
     * 根据是否首次启动决定展示引导页还是主界面，
     * 同时在 Android 13+ 上动态申请通知权限。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 启用沉浸式边到边显示，让内容延伸到状态栏/导航栏区域
        enableEdgeToEdge()

        // 处理通知点击后的 Intent，恢复房间状态
        handleNotificationIntent(intent)

        setContent {
            QuantumTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // 读取持久化配置（用户名、是否首次启动等）
                    val setting = getSharedPreferences("com.xshe.quantum", 0)
                    // isFirst 控制是否显示初始化引导页
                    var isFirst by remember { mutableStateOf(setting.getBoolean("FIRST", true)) }

                    // 重启应用时清理所有音乐列表缓存
                    // 在 MainActivity.onCreate 中（或 Application 的 onCreate 中）
                    try {
                        // 清除所有音乐缓存文件
                        // 由于缓存文件以主机名哈希命名，需要遍历删除
                        val cacheDir = cacheDir
                        cacheDir.listFiles()?.forEach { file ->
                            if (file.name.startsWith("music_cache_") && file.name.endsWith(".json")) {
                                file.delete()
                            }
                        }
                        Log.d("QuantumCache", "已清空所有音乐列表缓存")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    handleNotificationIntent(intent)

                    // Android 13+ 需要动态申请通知权限，用于前台音乐服务的通知栏展示
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            101
                        )
                    }
                    if (isFirst) {
                        // 首次启动：展示用户名设置页，保存后将 FIRST 置 false
                        FirstComposeView(
                            modifier = Modifier
                                .padding(innerPadding)
                                .background(color = MiuixTheme.colorScheme.background),
                            setting,
                            onConfirm = {
                                setting.edit().putBoolean("FIRST", false).apply()
                                isFirst = false
                            })
                    } else {
                        MainComposeView(
                            modifier = Modifier
                                .padding(innerPadding)
                                .background(color = MiuixTheme.colorScheme.background),
                            setting
                        )
                    }
                }
            }
        }
    }

    /**
     * 当 Activity 已存在于返回栈顶且被重新启动时（如点击通知栏）触发。
     * 将新 Intent 交给 [handleNotificationIntent] 处理，以恢复对应房间状态。
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent?.let {
            handleNotificationIntent(it)
        }
    }

    /**
     * 处理通知点击后携带的 Intent。
     * 从 [MusicService] 读取当前房间名和主机地址，若有效则写入 SharedPreferences，
     * 并设置 should_restore_room 标志，供 [MainComposeView] 在 LaunchedEffect 中恢复房间。
     */
    private fun handleNotificationIntent(intent: Intent) {
        val musicService = MusicService.getInstance()
        musicService?.let { service ->
            val hostName = service.getHostName()
            val roomName = service.getRoomName()

            if (hostName.isNotBlank() && roomName.isNotBlank() && roomName != "null") {
                // 原有逻辑：保存恢复标记
                getSharedPreferences("com.xshe.quantum", MODE_PRIVATE).edit().apply {
                    putString("restored_host", hostName)
                    putString("restored_room", roomName)
                    putBoolean("should_restore_room", true)
                    apply()
                }

                // 新增：检测是否已掉线，掉线则自动重新加入
                val userName = getSharedPreferences("com.xshe.quantum", MODE_PRIVATE)
                    .getString("User", "") ?: ""
                val savedPassword = getSharedPreferences("com.xshe.quantum", MODE_PRIVATE)
                    .getString("room_password_$roomName", "") ?: ""

                InternetHelper().checkIsIn(hostName, roomName, userName,
                    object : InternetHelper.RoomRequestCallback {
                        override fun onSuccess() {
                            // 还在房间里，无需任何操作
                            Log.d("NOTIFICATION_CLICK", "Still in room, no rejoin needed")
                        }
                        override fun onFailure() {
                            // onFailure 表示返回了 need_exit 或网络失败，尝试重新加入
                            Log.d("NOTIFICATION_CLICK", "Not in room, rejoining...")
                            InternetHelper().enterRoom(
                                this@MainActivity,
                                hostName, roomName, savedPassword, userName,
                                object : InternetHelper.RoomRequestCallback {
                                    override fun onSuccess() {
                                        Log.d("NOTIFICATION_CLICK", "Rejoined room: $roomName")

                                        service.setRoomInfo(hostName, roomName, userName)
                                    }
                                    override fun onFailure() {
                                        Log.w("NOTIFICATION_CLICK", "Rejoin failed for: $roomName")
                                        Handler(Looper.getMainLooper()).post {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "已掉线，重新加入房间失败，请手动进入",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}
/**
 * 首次启动引导页。
 *
 * 用户输入昵称后，将其持久化到 SharedPreferences，
 * 同时清空历史主机记录，然后回调 [onConfirm] 跳转主界面。
 * 只在 "FIRST" 标志为 true 时展示，之后不再出现。
 */
@Composable
fun FirstComposeView(modifier: Modifier, setting: SharedPreferences, onConfirm: () -> Unit) {
    var name by remember { mutableStateOf("") }
    val plusButtonModifier = Modifier.padding(5.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = name,
            onValueChange = { newText ->
                name = newText
            },
            label = "来个名头",
            enabled = true,
            readOnly = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        )

        Button(
            onClick = {
                setting.edit().putString("User", name).commit()
                setting.edit().putString("history_host", "").commit()
                onConfirm()
            },
            modifier = Modifier
                .fillMaxWidth()
                .then(plusButtonModifier),
            enabled = name.isNotBlank()
        ) {
            Text(
                text = "确认", fontSize = 16.sp
            )
        }
    }
}

/**
 * 主界面容器，持有全局状态并协调各子页面。
 *
 * 状态说明：
 *  - [i]                  当前选中的导航标签索引（0=主机/房间, 1=聊天, 2=音乐）
 *  - [tools]              工具类，封装网络请求、房间操作等通用逻辑
 *  - [values]             共享数据模型（当前房间名、消息列表等）
 *  - [itemList]           房间列表，增量维护避免频繁全量刷新
 *  - [savedHost]          已成功连接的主机地址
 *  - [globalIsPlaying]    全局音乐播放状态（与 MediaPlayer 同步）
 *  - [currentPlayingTrack] 当前播放的曲目文件名
 *  - [roomNumbers]        当前房间在线人数 / 最大人数
 *  - [musicService]       绑定的前台音乐服务，持有 MediaPlayer 实例
 *  - [uiExampleMode]      UI 侧是否切换到"模板音乐库"模式（本地 Switch 控制）
 *  - [lastManualActionTime] 最后一次手动操作时间戳，用于防止服务端状态覆盖本地操作
 *  - [updateVersionName/Url] 新版本信息，不为空时顶部显示更新提示
 */
@Composable
fun MainComposeView(modifier: Modifier, setting: SharedPreferences) {
    var i by remember { mutableIntStateOf(0) }
    val tools = remember { Tools() }
    val values = remember { Values() }
    val itemList = remember { mutableStateListOf<Values.ListItem>() }
    var savedHost by remember { mutableStateOf("") }
    var hostInputText by remember { mutableStateOf("") }
    tools.userName = setting.getString("User", "") ?: "User"
    var globalIsPlaying by remember { mutableStateOf(false) }
    var currentPlayingTrack by remember { mutableStateOf("") }
    var roomNumbers by remember { mutableStateOf(Values.RoomNumbers()) }
    val mContext = LocalContext.current
    var musicService by remember { mutableStateOf<MusicService?>(null) }
    var lastManualActionTime by remember { mutableLongStateOf(0L) }
    val MANUAL_COOLDOWN = 3000L
    var updateVersionName by remember { mutableStateOf("") }
    var updateUrl by remember { mutableStateOf("") }
    var uiExampleMode by rememberSaveable { mutableStateOf(false) }
    // 模板列表状态提升到 MainComposeView，切换 Tab 不会丢失已加载的列表
    val exampleMusicList = remember { mutableStateListOf<String>() }
    var exampleCurrentPage by rememberSaveable { mutableIntStateOf(1) }
    var exampleHasMore by rememberSaveable { mutableStateOf(true) }

    values.historyHost = setting.getString("history_host", "暂无历史连接主机").toString()

    /**
     * 启动时检查是否需要恢复房间（由通知点击写入的 should_restore_room 标志）。
     * 若标志为 true，则读取已保存的主机和房间名，直接跳转到音乐页（Tab 2），
     * 并在完成后清除恢复标志，防止下次冷启动时重复触发。
     */
    LaunchedEffect(Unit) {
        val shouldRestore = setting.getBoolean("should_restore_room", false)
        if (shouldRestore) {
            val restoredHost = setting.getString("restored_host", "")
            val restoredRoom = setting.getString("restored_room", "")

            if (!restoredHost.isNullOrBlank() && !restoredRoom.isNullOrEmpty() && restoredRoom != "null") {
                savedHost = restoredHost
                values.roomName = restoredRoom
                values.isCanSelected = false
                i = 2 // 切换到音乐标签页

                Log.d("ROOM_RESTORE", "Restored to: $restoredRoom at $restoredHost")

                // 清除恢复标记
                setting.edit().remove("restored_host").remove("restored_room").remove("should_restore_room").apply()
            }
        }
    }

    /**
     * 绑定/解绑前台音乐服务（MusicService）。
     * startForegroundService 确保服务在后台时能持续播放并显示通知栏控制。
     * bindService 获取 MusicBinder，通过它拿到 MediaPlayer 实例供 UI 直接控制。
     * DisposableEffect 在 Composable 离开组合树时自动解绑，防止内存泄漏。
     */
    //启动音乐服务
    DisposableEffect(Unit) {
        val intent = Intent(mContext, MusicService::class.java)
        mContext.startForegroundService(intent)
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as MusicService.MusicBinder
                musicService = binder.getService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                musicService = null
            }
        }
        mContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        onDispose {
            mContext.unbindService(connection)
        }
    }
    /**
     * 每 20 秒轮询一次当前房间的在线人数（present）和最大人数（max）。
     * 仅在已连接主机且已进入房间（i != 0 && roomName 不为空）时才发起请求，
     * 避免无意义的网络消耗。失败时弹 Toast 提示。
     */
    //轮询人数
    LaunchedEffect(i, values.roomName) {
        while (true) {
            if (i != 0 && !values.roomName.isNullOrEmpty() && !savedHost.isNullOrBlank()) {
                InternetHelper().getPAMNumber(
                    hostName = savedHost,
                    roomName = values.roomName,
                    callback = object : InternetHelper.PAMCallback {
                        override fun onSuccess(p: Int, m: Int) {
                            roomNumbers.max = m
                            roomNumbers.present = p
                        }

                        override fun onFailure() {
                            Handler(Looper.getMainLooper()).post {
                                tools.showToast(mContext, "获取房间人数失败")
                            }
                        }
                    }
                )
            }
            delay(20 * 1000)
        }
    }

    /**
     * 启动时检查服务端版本，若服务端 versionCode 高于本地则在顶部展示更新提示。
     * 点击提示可跳转到下载页（updateUrl）。
     * 仅启动一次（key=Unit），不受其他状态变化重触发。
     */
    LaunchedEffect(Unit) {
        InternetHelper().getServerVersion(
            "https://quantum.xshenas.icu:61320",
            object : InternetHelper.RequestCallback {
                override fun onSuccess(responseBody: String) {
                    try {
                        val json = JSONObject(responseBody)
                        val serverCode = json.optInt("versionCode", 0)
                        val serverName = json.optString("versionName", "")
                        val url = json.optString("updateURL", "")
                        val localCode = mContext.packageManager
                            .getPackageInfo(mContext.packageName, 0)
                            .let {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                                    it.longVersionCode.toInt()
                                else
                                    @Suppress("DEPRECATION") it.versionCode
                            }
                        if (serverCode > localCode && serverName.isNotBlank() && url.isNotBlank()) {
                            updateVersionName = serverName
                            updateUrl = url
                        } else {
                            updateVersionName = ""
                            updateUrl = ""
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure() {}
            })
    }

    /**
     * 消息轮询提升到 MainComposeView，无论当前在哪个 Tab 都持续拉取新消息，
     * 切换到聊天页时不需要等待下一次轮询，消息已是最新。
     */
    LaunchedEffect(values.roomName, savedHost) {
        values.messageList.clear()
        while (true) {
            if (savedHost.isNotBlank() && !values.roomName.isNullOrEmpty() && values.roomName != "null") {
                val url = if (savedHost.startsWith("http://") || savedHost.startsWith("https://"))
                    savedHost else "http://$savedHost"
                InternetHelper().getMessages(
                    url,
                    values.roomName,
                    tools.userName,
                    object : InternetHelper.RequestCallback {
                        override fun onSuccess(responseBody: String) {
                            try {
                                val jsonArray = org.json.JSONArray(responseBody)
                                if (jsonArray.length() > values.messageList.size) {
                                    val currentSize = values.messageList.size
                                    val newMessages = mutableListOf<String>()
                                    for (idx in currentSize until jsonArray.length()) {
                                        val jsonObj = jsonArray.optJSONObject(idx)
                                        if (jsonObj != null) {
                                            val s = jsonObj.optString("sender", "")
                                            val c = jsonObj.optString("content", "")
                                            newMessages.add(if (s.isNotEmpty()) "$s:$c" else c)
                                        } else {
                                            newMessages.add(jsonArray.getString(idx))
                                        }
                                    }
                                    if (newMessages.isNotEmpty()) {
                                        Handler(Looper.getMainLooper()).post {
                                            values.messageList.addAll(newMessages)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        override fun onFailure() {}
                    })
            }
            delay(1500)
        }
    }

    /**
     * 监听当前房间名变化：当离开房间（roomName 为空或"null"）时，
     * 立即停止并重置 MediaPlayer，同时清空播放状态，防止残留声音。
     */
    LaunchedEffect(values.roomName) {
        if (values.roomName.isNullOrEmpty() || values.roomName == "null") {
            Log.d("MainComposeView", "Exiting room, clearing music state")
            musicService?.stopMusic()
            musicService?.updateNotification(false)
            // 不要清除回调，只需要停止当前播放即可
            globalIsPlaying = false
            currentPlayingTrack = ""
        }
    }

    /**
     * 首次进入房间时立即拉取一次音乐状态（即时同步），
     * 而不等待轮询定时器触发，减少刚入房时的感知延迟。
     */
    LaunchedEffect(values.roomName, musicService) {
        val service = musicService ?: return@LaunchedEffect

        if (savedHost.isNotBlank() && !values.roomName.isNullOrEmpty()) {
            service.setRoomInfo(savedHost, values.roomName, tools.userName)
            // 明确告知服务用户已进入房间，SSE 推送的 music_status 才允许触发播放
            service.setHasJoinedRoom(true)

            // 先从 Service 恢复当前播放状态
            val currentTrack = service.currentPlayingTrack
            val isPlaying = service.mediaPlayer?.isPlaying ?: false


            if (currentTrack.isNotBlank()) {
                currentPlayingTrack = currentTrack
                globalIsPlaying = isPlaying
            }

            // 然后从服务端同步最新状态
            InternetHelper().getMusicStatus(
                savedHost, values.roomName, tools.userName,
                object : InternetHelper.RequestCallback {
                    override fun onSuccess(responseBody: String) {
                        try {
                            val json = JSONObject(responseBody)
                            Handler(Looper.getMainLooper()).post {
                                service.applyMusicStatus(json)

                                // 再次同步 UI 状态
                                val updatedTrack = service.currentPlayingTrack
                                val updatedPlaying = service.mediaPlayer?.isPlaying ?: false

                                currentPlayingTrack = updatedTrack
                                globalIsPlaying = updatedPlaying
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure() {}
                })
        } else {
            // roomName 为空表示已退出房间，关闭 SSE 推送权限
            service.setHasJoinedRoom(false)
        }
    }

    /**
     * 每 1 秒轮询一次服务端音乐状态，用于持续保持多端同步。
     * 通过防抖逻辑，保证本地手动操作不被立即覆盖。
     */
    LaunchedEffect(values.roomName, savedHost, musicService) {
        val service = musicService ?: return@LaunchedEffect
        val player = service.mediaPlayer ?: return@LaunchedEffect
        while (true) {
            if (savedHost.isNotBlank() && !values.roomName.isNullOrEmpty()) {
                InternetHelper().getMusicStatus(
                    savedHost, values.roomName, tools.userName,
                    object : InternetHelper.RequestCallback {
                        override fun onSuccess(responseBody: String) {
                            try {
                                val json = JSONObject(responseBody)
                                Handler(Looper.getMainLooper()).post {
                                    service.applyMusicStatus(json)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        override fun onFailure() {}
                    })
            }
            delay(1000)
        }
    }


    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            key(values.isCanSelected) {
                NavigationBar {
                    val tabs = listOf(
                        Triple(stringResource(R.string.hostButton), 0, Icons.Default.Home),
                        Triple(
                            stringResource(R.string.chatButton),
                            1,
                            Icons.AutoMirrored.Filled.Chat
                        ),
                        Triple(stringResource(R.string.musicButton), 2, Icons.Default.MusicNote)
                    )

                    tabs.forEach { (label, index, icon) ->
                        val isTabDisabled = (index == 1 || index == 2) && values.isCanSelected

                        NavigationBarItem(
                            selected = i == index,
                            onClick = {
                                if (!isTabDisabled) {
                                    i = index
                                } else {
                                    tools.showToast(mContext, "请先进入一个房间")
                                }
                            },
                            icon = icon,
                            label = label
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MiuixTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (i == 0) "👋 Hi, ${tools.userName}" else {
                            if (values.roomName.isNullOrEmpty()) "null"
                            else "\uD83D\uDED6 ${values.roomName}(${roomNumbers.present}/${roomNumbers.max})"
                        },
                        style = MiuixTheme.textStyles.body1.copy(fontWeight = FontWeight.ExtraBold),
                        color = MiuixTheme.colorScheme.onSurface
                    )

                    if (updateVersionName.isNotBlank() && updateUrl.isNotBlank() && i == 0) {
                        Text(
                            text = "新版本($updateVersionName)",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
                                    mContext.startActivity(intent)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            key(savedHost) {
                AnimatedContent(
                    targetState = i,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    transitionSpec = {
                        val direction = if (targetState > initialState) 1 else -1
                        (slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            initialOffsetX = { fullWidth -> direction * fullWidth / 3 }
                        ) + fadeIn(
                            animationSpec = tween(
                                durationMillis = 200,
                                delayMillis = 30
                            )
                        )).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(
                                    durationMillis = 180,
                                    easing = FastOutSlowInEasing
                                ),
                                targetOffsetX = { fullWidth -> -direction * fullWidth / 3 }
                            ) + fadeOut(
                                animationSpec = tween(durationMillis = 120)
                            )
                        )
                    },
                    label = "TabAnimation"
                ) { targetI ->
                    when (targetI) {
                        0 -> HostList(
                            tools = tools, values = values, itemList = itemList,
                            host = savedHost, hostNameInput = hostInputText,
                            musicService = musicService,
                            onExitRoomSuccess = { i = 0 },
                            onHostNameChange = { hostInputText = it },
                            onConnectSuccess = { newHost ->
                                savedHost = newHost; hostInputText = ""
                                setting.edit().putString("history_host", newHost).apply()
                            }
                        )
                        1 -> ChatView(tools, values, savedHost, setting)
                        2 -> MusicView(
                            savedHost, values.roomName, tools,
                            musicService?.mediaPlayer, globalIsPlaying, currentPlayingTrack,
                            userName = tools.userName,
                            onPlayingStateChange = {
                                globalIsPlaying = it
                                Handler(Looper.getMainLooper()).post {
                                    musicService?.updateNotification(it)
                                }
                            },
                            onCurrentTrackChange = { newTrack -> currentPlayingTrack = newTrack },
                            onManualAction = { lastManualActionTime = System.currentTimeMillis() },
                            musicService = musicService,
                            uiExampleMode = uiExampleMode,
                            onExampleModeChange = { uiExampleMode = it },
                            exampleMusicList = exampleMusicList,
                            exampleCurrentPage = exampleCurrentPage,
                            exampleHasMore = exampleHasMore,
                            onExamplePageChange = { exampleCurrentPage = it },
                            onExampleHasMoreChange = { exampleHasMore = it },
                        )
                        else -> {}
                    }
                }
            }
        }
    }
}

/**
 * 主机与房间管理页面（Tab 0）。
 *
 * 功能：
 *  - 输入主机地址并连接，连接成功后将主机存入历史记录（下拉可选）
 *  - 增量维护房间列表 [itemList]（刷新/连接成功时调用 updateRoomList）
 *  - 点击房间项：
 *    - 未选中状态 → 弹出密码对话框 → 调用 enterRoom 进入
 *    - 已选中状态 → 调用 exitRoom 退出，同时停止音乐播放
 *  - 右上角"+"按钮：弹出 PlusRoomDialog 创建新房间
 *  - 刷新按钮：重新连接并增量更新房间列表
 *
 * @param tools          工具类，封装 connectAndCheck/enterRoom/exitRoom 等操作
 * @param values         共享数据，包含当前 roomName、isCanSelected 等状态
 * @param itemList       房间列表（SnapshotStateList，支持细粒度重组）
 * @param host           当前已连接的主机地址
 * @param hostNameInput  主机输入框的当前文本
 * @param musicService   音乐服务引用，退出房间时用于停止播放
 * @param onExitRoomSuccess 退出房间成功后回调（通常切换回 Tab 0）
 * @param onHostNameChange  主机输入框文本变化回调
 * @param onConnectSuccess  连接主机成功回调，传入新主机地址
 */
@Composable
fun HostList(
    tools: Tools,
    values: Values,
    itemList: SnapshotStateList<Values.ListItem>,
    host: String,
    hostNameInput: String,
    musicService: MusicService?,
    onExitRoomSuccess: () -> Unit,
    onHostNameChange: (String) -> Unit,
    onConnectSuccess: (String) -> Unit
) {
    var showPlusRoomDialog by remember { mutableStateOf(false) }
    val mContext = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var pendingRoomItem by remember { mutableStateOf<Values.ListItem?>(null) }
    val textFieldWidth = remember { mutableStateOf(0) }
    var officialReachable by remember { mutableStateOf<Boolean?>(null) }
    val officialUrl = "https://quantum.xshenas.icu:61320"
    val coroutineScope = rememberCoroutineScope()

    /**
     * 增量更新房间列表，避免全量清空重绘导致的性能损耗与界面闪烁。
     * 策略：
     *  1. 遍历服务器返回的最新房间名列表，对每个房间：
     *     - 若已存在于 itemList，则仅更新状态字段（避免整行重建）
     *     - 若不存在，则追加新项
     *  2. 移除服务器已不存在的旧房间项
     */
    val updateRoomList = {
        if (tools.roomNames.isEmpty()) {
            itemList.clear()
        } else {
            // 构建最新的房间名→状态映射，方便 O(1) 查找
            val latestMap = tools.roomNames.mapIndexed { idx, name ->
                name to (if (tools.roomStatuses[idx]) "√" else "×")
            }.toMap()

            // 移除服务器上已不存在的房间
            itemList.removeAll { it.itemHost !in latestMap }

            // 更新已有项 / 追加新项
            for ((roomName, statusText) in latestMap) {
                val existingIdx = itemList.indexOfFirst { it.itemHost == roomName }
                val isCurrentSelected = roomName == values.roomName && !values.isCanSelected
                if (existingIdx != -1) {
                    // 仅在字段有变化时才替换，减少无效重组
                    val old = itemList[existingIdx]
                    if (old.itemStatus != statusText || old.isSelected != isCurrentSelected) {
                        itemList[existingIdx] =
                            old.copy(itemStatus = statusText, isSelected = isCurrentSelected)
                    }
                } else {
                    itemList.add(Values.ListItem(roomName, statusText, isCurrentSelected))
                }
            }
        }
    }

    LaunchedEffect(host) {
        if (host.isNotBlank()) {
            tools.connectAndCheck(mContext, host, object : Tools.gacCallback {
                override fun onSuccess() {
                    updateRoomList()
                }

                override fun onFailure() {
                    itemList.clear()
                }
            })
        }
    }

    LaunchedEffect(expanded) {
        if (expanded) {
            officialReachable = null // 黄色：检测中
            withContext(Dispatchers.IO) {
                val reachable = try {
                    val socket = java.net.Socket()
                    socket.connect(
                        java.net.InetSocketAddress("quantum.xshenas.icu", 61320),
                        3000 // 超时 3 秒
                    )
                    socket.close()
                    true
                } catch (e: Exception) {
                    false
                }
                withContext(Dispatchers.Main) {
                    officialReachable = reachable
                }
            }
        }
    }

    if (showPasswordDialog && pendingRoomItem != null) {
        RoomPasswordDialog(
            roomName = pendingRoomItem!!.itemHost,
            onDismissRequest = {
                showPasswordDialog = false
                pendingRoomItem = null
            },
            onConfirmation = { roomName, password ->
                showPasswordDialog = false
                tools.enterRoom(
                    mContext, host, roomName, password, tools.userName,
                    object : Tools.gacCallback {
                        override fun onSuccess() {
                            val idx = itemList.indexOf(pendingRoomItem)
                            if (idx != -1) {
                                itemList[idx] = itemList[idx].copy(isSelected = true)
                            }
                            values.isCanSelected = false
                            values.roomName = roomName
                            pendingRoomItem = null
                            (mContext as? android.app.Activity)?.getSharedPreferences("com.xshe.quantum", Context.MODE_PRIVATE)
                                ?.edit()?.putString("room_password_$roomName", password)?.apply()
                        }

                        override fun onFailure() {
                            pendingRoomItem = null
                        }
                    }
                )
            }
        )
    }

    if (showPlusRoomDialog) {
        PlusRoomDialog(
            onDismissRequest = { showPlusRoomDialog = false },
            onConfirmation = { roomName, maxNumber, cancelTime, password ->
                tools.addRoom(
                    mContext,
                    host,
                    roomName,
                    maxNumber,
                    cancelTime, password,
                    object : Tools.gacCallback {
                        override fun onSuccess() {
                            tools.showToast(mContext, "创建成功")
                            updateRoomList()
                        }

                        override fun onFailure() {}
                    })
                showPlusRoomDialog = false
            }
        )
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 15.dp, bottom = 0.dp, end = 12.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { showPlusRoomDialog = true },
                    modifier = Modifier
                        .size(24.dp)
                        .weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加",
                        tint = MiuixTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = {
                        tools.connectAndCheck(mContext, host, object : Tools.gacCallback {
                            override fun onSuccess() {
                                updateRoomList()
                            }

                            override fun onFailure() {
                                tools.showToast(mContext, "刷新失败")
                            }
                        })
                    },
                    modifier = Modifier
                        .size(24.dp)
                        .weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新",
                        tint = MiuixTheme.colorScheme.secondary
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                TextField(
                    value = hostNameInput,
                    onValueChange = onHostNameChange,
                    label = stringResource(R.string.host_inputer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            textFieldWidth.value = coordinates.size.width
                        },
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "展开历史主机",
                                tint = MiuixTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.width(with(LocalDensity.current) { textFieldWidth.value.toDp() })
                ) {
                    // ── 固定默认项：Quantum-Official ──
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = when (officialReachable) {
                                                true  -> Color(0xFF4CAF50)  // 绿色：可连接
                                                false -> Color(0xFFF44336)  // 红色：不可达
                                                null  -> Color(0xFFFFEB3B)  // 黄色：检测中
                                            },
                                            shape = CircleShape
                                        )
                                )
                                Text("Quantum-Official")
                            }
                        },
                        onClick = {
                            onHostNameChange(officialUrl)
                            expanded = false
                        }
                    )
                    // ── 历史记录项（仅在非空且不与官方重复时显示）──
                    if (values.historyHost.isNotBlank() &&
                        values.historyHost != "暂无历史连接主机" &&
                        values.historyHost != officialUrl
                    ) {
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(values.historyHost) },
                            onClick = {
                                onHostNameChange(values.historyHost)
                                expanded = false
                            }
                        )
                    }
                }
            }
            Button(
                onClick = {
                    if (hostNameInput.isEmpty()) tools.showToast(mContext, "请输入正确URL!")
                    else tools.connectAndCheck(mContext, hostNameInput, object : Tools.gacCallback {
                        override fun onSuccess() {
                            onConnectSuccess(hostNameInput)
                        }

                        override fun onFailure() {
                            tools.showToast(mContext, "连接失败")
                        }
                    })
                },
                modifier = Modifier.fillMaxHeight(),
            ) {
                Text("连接")
            }
        }
        if (itemList.isEmpty() && host.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "主机内暂无房间",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(Modifier.padding(horizontal = 17.dp, vertical = 8.dp)) {
                items(
                    items = itemList,
                    key = { it.itemHost }
                ) { item ->
                    ConnectListItem(listItem = item, values = values, onSelectClick = {
                        val index = itemList.indexOf(item)
                        if (index != -1) {
                            if (values.isCanSelected && !item.isSelected) {
                                pendingRoomItem = item
                                showPasswordDialog = true
                            } else if (!values.isCanSelected && item.isSelected) {
                                tools.exitRoom(
                                    mContext,
                                    host,
                                    item.itemHost,
                                    tools.userName,
                                    object : Tools.gacCallback {
                                        override fun onSuccess() {
                                            // 切回主线程操作 MediaPlayer 和 UI 状态，避免子线程竞争
                                            Handler(Looper.getMainLooper()).post {
                                                musicService?.stopMusic()
                                                // 明确告知服务用户已离开房间，阻止 SSE 继续触发播放
                                                musicService?.setHasJoinedRoom(false)
                                                itemList[index] = item.copy(isSelected = false)
                                                values.isCanSelected = true
                                                values.roomName = ""
                                                Log.d("EXIT_ROOM", values.isCanSelected.toString())
                                                onExitRoomSuccess()
                                            }
                                        }

                                        override fun onFailure() {}
                                    })
                            }
                        }
                    })
                }
            }
        }
    }
}

/**
 * 创建新房间的对话框。
 *
 * 提供以下配置项：
 *  - 房间名称（必填文本）
 *  - 房间密码（可选，为空则公开房间）
 *  - 最大人数（Slider，范围 0~16）
 *  - 自动取消时间（Slider，范围 10~240 分钟，无人时自动销毁房间）
 *
 * 点击"确认添加"后调用 [onConfirmation] 将配置传回父级处理网络请求。
 */
@Composable
fun PlusRoomDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (String, Int, Int, String) -> Unit,
) {
    var roomName by remember { mutableStateOf("") }
    var maxNumber by remember { mutableStateOf(2f) }
    val maxNumberTrue = maxNumber.toInt()
    var cancelTime by remember { mutableStateOf(60f) }
    val cancelTimeTrue = cancelTime.toInt()
    var password by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(450.dp)
                .padding(16.dp),

            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "添加房间",
                        fontSize = 23.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    TextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = "房间名称",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                    )

                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "房间密码",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                    )

                    Text(text = "最大人数 (${maxNumberTrue})", fontSize = 15.sp)
                    Slider(
                        value = maxNumber,
                        valueRange = 0f..16f,
                        onValueChange = { maxNumber = it },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )

                    Text(text = "取消时间 (${cancelTimeTrue}分钟)", fontSize = 15.sp)
                    Slider(
                        value = cancelTime,
                        valueRange = 10f..240f,
                        onValueChange = { cancelTime = it },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { onDismissRequest() }, text = "取消")
                    TextButton(onClick = {
                        onConfirmation(roomName, maxNumberTrue, cancelTimeTrue, password)
                    }, text = "确认添加")
                }
            }
        }
    }
}

/**
 * 加入有密码保护的房间时弹出的密码输入对话框。
 *
 * 点击"加入"后调用 [onConfirmation]，将房间名与密码传回父级进行 enterRoom 操作。
 * 点击"取消"或点击对话框外部均会触发 [onDismissRequest]，父级应同时清空 pendingRoomItem。
 */
@Composable
fun RoomPasswordDialog(
    roomName: String,
    onDismissRequest: () -> Unit,
    onConfirmation: (String, String) -> Unit,
) {
    var password by remember { mutableStateOf("") }
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(280.dp)
                .padding(16.dp),

            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "加入房间: $roomName",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "房间密码",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { onDismissRequest() }, text = "取消")
                    TextButton(onClick = { onConfirmation(roomName, password) }, text = "加入")
                }
            }
        }
    }
}


/**
 * 房间列表的单个条目组件。
 *
 * 视觉逻辑：
 *  - 已加入（isSelected=true）：主色边框 + 主色背景 + "已加入此房间"副标题
 *  - 未加入：浅灰边框 + 默认背景 + 右侧显示服务器可用状态（√绿 / ×灰）
 *
 * 点击事件委托给父级 [onSelectClick]，由父级根据 isSelected 决定进入或退出房间。
 */
@Composable
fun ConnectListItem(
    listItem: Values.ListItem,
    values: Values,
    onSelectClick: () -> Unit
) {
    val isSelected = listItem.isSelected
    val borderColor =
        if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.outline.copy(
            alpha = 0.3f
        )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onSelectClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MiuixTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = listItem.itemHost, style = MiuixTheme.textStyles.body1)
                if (isSelected) {
                    Text(
                        text = "已加入此房间",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.primary
                    )
                }
            }

            if (!isSelected) {
                Text(
                    text = listItem.itemStatus,
                    color = if (listItem.itemStatus == "√") Color(0xFF4CAF50) else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 聊天页面（Tab 1）。
 *
 * 消息获取策略（增量更新）：
 *  - 每 1.5 秒轮询服务端消息列表
 *  - 仅当服务端返回的消息数量 > 本地已有数量时，追加新消息
 *  - 切换房间时（LaunchedEffect key=roomName）清空消息列表，重新同步
 *  - 不在循环内执行全量 clear+addAll，避免列表闪烁
 *
 * 消息气泡：
 *  - 自己发送的消息：右对齐，主色背景
 *  - 他人消息：左对齐，surface 背景，顶部显示发送者名称
 *  - reverseLayout=true，最新消息显示在底部，LazyColumn 自动倒序渲染
 *
 * 发送逻辑：
 *  - 本地先追加消息（乐观更新），网络失败时弹 Toast 提示
 *  - 格式为 "userName:消息内容"，解析时以首个":"分割
 */
@Composable
fun ChatView(
    tools: Tools,
    values: Values,
    host: String,
    setting: SharedPreferences
) {
    var inputMessage by remember { mutableStateOf("") }
    val userName = setting.getString("User", "") ?: ""
    val mContext = LocalContext.current
    val url =
        if (host.startsWith("http://") || host.startsWith("https://")) host else "http://$host"

    val lazyListState = rememberLazyListState()
    // 消息数量变化时自动滚到底部（即 reverseLayout 的顶部）
    val messageCount = values.messageList.size
    LaunchedEffect(messageCount) {
        if (messageCount > 0) lazyListState.animateScrollToItem(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = true,
        ) {
            // 使用稳定的 key（消息内容 + 位置），避免全量重组导致闪烁
            itemsIndexed(
                items = values.messageList.reversed(),
                key = { index, msg -> "${values.messageList.size - 1 - index}-${msg.hashCode()}" }
            ) { index, msg ->
                val isMe = msg.startsWith("$userName:")
                val sender = msg.substringBefore(":", "未知用户")
                val displayMsg = if (msg.contains(":")) msg.substringAfter(":") else msg

                // 气泡入场动画：弹性缩放 + 滑入
                val animatedProgress = remember { androidx.compose.animation.core.Animatable(0f) }
                LaunchedEffect(Unit) {
                    animatedProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .graphicsLayer {
                            alpha = animatedProgress.value
                            val offsetX = if (isMe) 30f else -30f
                            translationX = offsetX * (1 - animatedProgress.value)
                            scaleX = 0.8f + (0.2f * animatedProgress.value)
                            scaleY = 0.8f + (0.2f * animatedProgress.value)
                        },
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    val bubbleColor =
                        if (isMe) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceVariant
                    val contentColor =
                        if (isMe) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurfaceContainer

                    val bubbleShape = RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 2.dp,
                        bottomEnd = if (isMe) 2.dp else 16.dp
                    )

                    Surface(
                        modifier = Modifier.widthIn(max = 280.dp),
                        color = bubbleColor,
                        shape = bubbleShape
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (!isMe) {
                                Text(
                                    text = sender,
                                    style = MiuixTheme.textStyles.body2.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    color = MiuixTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(
                                text = displayMsg,
                                color = contentColor,
                                style = MiuixTheme.textStyles.body1
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                label = "说点什么...",
                useLabelAsPlaceholder = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))

            // 发送按钮：点击缩放反馈
            var isSendPressed by remember { mutableStateOf(false) }
            val sendScaleF by animateFloatAsState(
                targetValue = if (isSendPressed) 0.85f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                ),
                label = "sendScaleAnim"
            )
            val coroutineScope = rememberCoroutineScope()
            IconButton(
                onClick = {
                    if (inputMessage.isNotBlank()) {
                        isSendPressed = true
                        coroutineScope.launch {
                            delay(120)
                            isSendPressed = false
                        }
                        val msgText = inputMessage
                        val fullMsg = "${userName}:${msgText}"
                        // 乐观更新：立刻显示，网络失败再回滚
                        inputMessage = ""
                        values.messageList.add(fullMsg)
                        InternetHelper().appendMessage(
                            url, values.roomName, fullMsg,
                            object : InternetHelper.RoomRequestCallback {
                                override fun onSuccess() {
                                    // 消息已在本地，无需二次添加
                                }
                                override fun onFailure() {
                                    // 回滚乐观更新
                                    values.messageList.remove(fullMsg)
                                    tools.showToast(mContext, "发送失败")
                                }
                            }
                        )
                    }
                },
                modifier = Modifier
                    .graphicsLayer { scaleX = sendScaleF; scaleY = sendScaleF }
                    .background(MiuixTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * 音乐播放页面（Tab 2）。
 *
 * 双模式设计：
 *  - 房间模式（uiExampleMode=false）：显示房间内上传的音乐，可上传新文件
 *  - 模板音乐库模式（uiExampleMode=true）：分页加载公共模板音乐，支持关键词搜索
 *
 * 列表管理（增量更新）：
 *  - [roomMusicList]：切换房间或上传完成后增量更新（只增删变化项，不全量刷新）
 *  - [exampleMusicList]：分页追加，不重置已加载页面；下拉到底自动触发加载下一页
 *  - [searchResultList]：搜索词变化时清空后重新加载第一页，向下滚动分页追加
 *
 * 播放控制：
 *  - [playTrack]：统一的播放入口，重置 MediaPlayer 并异步 prepare，
 *    prepare 完成后 seek 到 0 并 start，同时通知服务端同步状态
 *  - 进度条：每秒更新 currentPos；拖动结束后 seek 并同步服务端
 *  - 上/下一首：在 currentDisplayList 中按索引切换
 *  - 播放完成监听：自动播放列表中的下一首
 *
 * 状态同步：
 *  - 每 5 秒向服务端上报一次当前进度（仅在播放时）
 *  - 手动操作（onManualAction）会更新 lastManualActionTime，
 *    使服务端轮询在 3 秒内不覆盖本地状态
 *
 * @param hostName           当前连接的主机地址
 * @param roomName           当前房间名
 * @param tools              工具类
 * @param mediaPlayer        来自 MusicService 的 MediaPlayer 实例，null 时显示"正在连接"
 * @param isPlaying          当前是否在播放（由父级 MainComposeView 维护）
 * @param currentPlayingTrack 当前播放的曲目文件名
 * @param uiExampleMode      本地模式切换状态
 * @param serverExampleMode  服务端推送的模式状态
 * @param userName           当前用户名，上报状态时附带
 * @param onUiModeChange     模式切换回调
 * @param onPlayingStateChange 播放状态变化回调
 * @param onCurrentTrackChange 曲目变化回调
 * @param onManualAction     手动操作时的防抖回调
 */
@Composable
fun MusicView(
    hostName: String,
    roomName: String,
    tools: Tools,
    mediaPlayer: MediaPlayer?,
    isPlaying: Boolean,
    currentPlayingTrack: String,
    userName: String,
    onPlayingStateChange: (Boolean) -> Unit,
    onCurrentTrackChange: (String) -> Unit,
    onManualAction: () -> Unit,
    musicService: MusicService?,
    uiExampleMode: Boolean,
    onExampleModeChange: (Boolean) -> Unit,
    exampleMusicList: androidx.compose.runtime.snapshots.SnapshotStateList<String>,
    exampleCurrentPage: Int,
    exampleHasMore: Boolean,
    onExamplePageChange: (Int) -> Unit,
    onExampleHasMoreChange: (Boolean) -> Unit,
) {
    if (mediaPlayer == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("正在连接播放服务...")
        }
        return
    }

    // 切换 Tab 后状态保留，不会重新加载列表。
    var currentPage by remember { mutableIntStateOf(exampleCurrentPage) }
    var hasMore by remember { mutableStateOf(exampleHasMore) }
    // 同步外部状态到本地便于 LaunchedEffect 内部读写，变化时向上回调
    LaunchedEffect(currentPage) { onExamplePageChange(currentPage) }
    LaunchedEffect(hasMore) { onExampleHasMoreChange(hasMore) }
    val listState = rememberLazyListState()
    val roomMusicList = remember { mutableStateListOf<String>() }
    val mContext = LocalContext.current
    var currentPos by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) } // 新增：拖拽状态标记
    val duration = if (mediaPlayer.duration > 0) mediaPlayer.duration.toFloat() else 1f
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    var isUploading by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val searchResultList = remember { mutableStateListOf<String>() }
    var searchPage by remember { mutableIntStateOf(1) }
    var searchHasMore by remember { mutableStateOf(false) }
    var searchIsLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var retryCount by remember { mutableIntStateOf(0) }
    val MAX_RETRY = 0
    val albumArtSemaphore = remember { Semaphore(3) }
    val currentDisplayList = when {
        uiExampleMode && searchQuery.isNotBlank() -> searchResultList
        uiExampleMode -> exampleMusicList
        else -> roomMusicList
    }

    val timeoutHandler = remember { Handler(Looper.getMainLooper()) }

    fun playTrack(fileName: String, isUserInitiated: Boolean = false) {
        if (fileName.isBlank()) return
        if (isUserInitiated) {
            retryCount = 0
        }
        onCurrentTrackChange(fileName)
        onManualAction()

        Log.d("MusicView", "Playing track: $fileName, isExample=$uiExampleMode")

        musicService?.playTrack(fileName, uiExampleMode) { isSuccess ->
            mainHandler.post {
                if (isSuccess) {
                    onPlayingStateChange(true)
                    Log.d("MusicView", "Track started successfully: $fileName")
                } else {
                    onPlayingStateChange(false)
                    Log.e("MusicView", "Track failed to start: $fileName")
                }
            }
        }
    }

    LaunchedEffect(uiExampleMode, currentPage) {
        val expectedItems = (currentPage - 1) * 20
        if (uiExampleMode && currentPage > 1 && hasMore && !isLoading && exampleMusicList.size <= expectedItems) {
            isLoading = true
            try {
                if (hostName.isBlank()) {
                    isLoading = false
                    return@LaunchedEffect
                }

                val result = tools.fetchExampleMusicListSuspend(hostName, currentPage, 20)
                val songs = result?.first ?: emptyList()
                val total = result?.second ?: 0

                val newSongs = songs.filter { it !in exampleMusicList }
                exampleMusicList.addAll(newSongs)
                hasMore = exampleMusicList.size < total
                musicService?.playList = exampleMusicList.shuffled()
                musicService?.playListIsExample = true

                Tools.MusicCacheManager.saveCache(
                    mContext,
                    hostName,
                    Tools.MusicListCache(
                        songs = exampleMusicList.toList(),
                        currentPage = currentPage,
                        totalSongs = total,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                Log.e("MusicView", "Error loading page $currentPage: ${e.message}")
                currentPage--
                hasMore = true
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(searchQuery, uiExampleMode) {
        if (!uiExampleMode) return@LaunchedEffect
        delay(500)

        searchResultList.clear()
        searchPage = 1
        searchHasMore = false

        if (searchQuery.isBlank()) {
            isSearching = false
            return@LaunchedEffect
        }

        isSearching = true
        searchIsLoading = true
        try {
            if (hostName.isBlank()) {
                isSearching = false
                searchIsLoading = false
                return@LaunchedEffect
            }

            val result = tools.searchExampleMusicSuspend(hostName, searchQuery, 1, 20)
            val songs = result?.first ?: emptyList()
            val total = result?.second ?: 0

            searchResultList.addAll(songs)
            searchHasMore = searchResultList.size < total
        } catch (e: Exception) {
            Log.e("MusicView", "Search error: ${e.message}")
        } finally {
            searchIsLoading = false
        }
    }

    LaunchedEffect(searchPage) {
        if (searchPage <= 1 || !searchHasMore || searchIsLoading || searchQuery.isBlank()) return@LaunchedEffect

        if (hostName.isBlank()) {
            return@LaunchedEffect
        }

        searchIsLoading = true
        try {
            val result = tools.searchExampleMusicSuspend(
                hostName,
                searchQuery,
                searchPage,
                20
            )
            val songs = result?.first ?: emptyList()
            val total = result?.second ?: 0

            val newSongs = songs.filter { it !in searchResultList }
            searchResultList.addAll(newSongs)
            searchHasMore = searchResultList.size < total
        } catch (e: Exception) {
            Log.e("MusicView", "Search pagination error: ${e.message}")
            searchPage--
            searchHasMore = true
        } finally {
            searchIsLoading = false
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex == null) return@collect

                if (uiExampleMode && searchQuery.isNotBlank()) {
                    if (!searchIsLoading && searchHasMore && lastVisibleIndex >= searchResultList.size - 1) {
                        searchPage++
                    }
                } else if (uiExampleMode && searchQuery.isBlank()) {
                    if (!isLoading && hasMore && lastVisibleIndex >= exampleMusicList.size - 1) {
                        currentPage++
                    }
                }
            }
    }

    LaunchedEffect(musicService) {
        val service = musicService ?: return@LaunchedEffect

        Log.d("MusicView", "Setting up track completion callback")

        service.onTrackCompletedCallback = { completedTrack ->
            val index = currentDisplayList.indexOf(completedTrack)
            if (index != -1 && index < currentDisplayList.size - 1) {
                val nextTrack = currentDisplayList[index + 1]
                service.playTrack(nextTrack, uiExampleMode)
            } else {
                service.isSwitchingTrack = false
                service.updateMusicStatusToServer(true, 0)
            }
        }

        val track = service.currentPlayingTrack
        val playing = service.mediaPlayer?.isPlaying ?: false

        Log.d("MusicView", "Initial sync from Service: track=$track, playing=$playing")

        if (track.isNotBlank() && track != currentPlayingTrack) {
            onCurrentTrackChange(track)
        }
        if (playing != isPlaying) {
            onPlayingStateChange(playing)
        }

        var lastKnownTrack = service.currentPlayingTrack
        var lastKnownPlaying = service.mediaPlayer?.isPlaying ?: false

        while (true) {
            delay(1000)
            val currentTrack = service.currentPlayingTrack
            val isPlayingNow = service.mediaPlayer?.isPlaying ?: false

            if (currentTrack.isNotBlank() && currentTrack != lastKnownTrack) {
                Log.d("MusicView", "Track changed detected: $currentTrack")
                onCurrentTrackChange(currentTrack)
                lastKnownTrack = currentTrack
            }
            if (isPlayingNow != lastKnownPlaying) {
                Log.d("MusicView", "Playing state changed: $isPlayingNow")
                onPlayingStateChange(isPlayingNow)
                lastKnownPlaying = isPlayingNow
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            isUploading = true
            uris.forEach { uri ->
                tools.uploadMusicFile(
                    mContext,
                    hostName,
                    roomName,
                    uri,
                    object : Tools.gacCallback {
                        override fun onSuccess() {
                            tools.fetchMusicList(hostName, roomName) { list ->
                                val toAdd = list.filter { it !in roomMusicList }
                                val toRemove = roomMusicList.filter { it !in list }
                                roomMusicList.removeAll(toRemove)
                                roomMusicList.addAll(toAdd)
                                musicService?.playList = roomMusicList.shuffled()
                                musicService?.playListIsExample = false
                                isUploading = false
                            }
                        }

                        override fun onFailure() {
                            Handler(Looper.getMainLooper()).post {
                                isUploading = false
                                tools.showToast(mContext, "上传失败")
                            }
                        }
                    }
                )
            }
        }
    }

    if (isUploading) {
        LoadingDialog()
    }

    LaunchedEffect(roomName, uiExampleMode, hostName) {
        if (uiExampleMode) {
            if (hostName.isBlank()) {
                Log.d("MusicView", "hostName is blank, clearing example list")
                exampleMusicList.clear()
                currentPage = 1
                hasMore = true
                return@LaunchedEffect
            }

            if (exampleMusicList.isNotEmpty()) {
                return@LaunchedEffect
            }

            val cached = withContext(Dispatchers.IO) {
                try {
                    Tools.MusicCacheManager.loadCache(mContext, hostName)
                } catch (e: Exception) {
                    Log.e("MusicView", "Cache load error: ${e.message}")
                    null
                }
            }

            if (cached != null && cached.songs != null) {
                Log.d("MusicView", "Restoring from cache: ${cached.songs.size} songs")
                exampleMusicList.clear()
                exampleMusicList.addAll(cached.songs)
                currentPage = cached.currentPage
                hasMore = exampleMusicList.size < (cached.totalSongs ?: 0)
            } else {
                Log.d("MusicView", "No cache available, resetting state")
                currentPage = 1
                hasMore = true
                exampleMusicList.clear()
            }

            loadError = false
            isLoading = true
            try {
                val result = tools.fetchExampleMusicListSuspend(hostName, 1, 20)
                val songs = result?.first ?: emptyList()
                val total = result?.second ?: 0

                Log.d("MusicView", "Fetched $songs songs, total: $total")

                val newSongs = songs.filter { it !in exampleMusicList }
                if (newSongs.isNotEmpty()) {
                    exampleMusicList.addAll(newSongs)
                } else if (exampleMusicList.isEmpty()) {
                    exampleMusicList.addAll(songs)
                }
                val totalSongs = total
                hasMore = exampleMusicList.size < totalSongs
                musicService?.playList = exampleMusicList.shuffled()
                musicService?.playListIsExample = true
                Tools.MusicCacheManager.saveCache(
                    mContext,
                    hostName,
                    Tools.MusicListCache(
                        songs = exampleMusicList.toList(),
                        currentPage = currentPage,
                        totalSongs = totalSongs,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                Log.e("MusicView", "Initial load error: ${e.message}")
                loadError = true
                if (exampleMusicList.isEmpty()) {
                    exampleMusicList.clear()
                }
            } finally {
                isLoading = false
            }
        } else {
            if (roomName != "null") {
                tools.fetchMusicList(hostName, roomName) { list ->
                    val toAdd = list.filter { it !in roomMusicList }
                    val toRemove = roomMusicList.filter { it !in list }
                    roomMusicList.removeAll(toRemove)
                    roomMusicList.addAll(toAdd)
                    musicService?.playList = roomMusicList.shuffled()
                    musicService?.playListIsExample = false
                }
            }
        }
    }

    // 每5秒同步一次音乐进度到服务器
    LaunchedEffect(isPlaying, currentPlayingTrack) {
        while (isPlaying) {
            delay(5000)
            val localTime = mediaPlayer.currentPosition / 1000
            if (localTime > 0 && currentPlayingTrack.isNotBlank()) {
                InternetHelper().updateMusicStatus(
                    hostName,
                    roomName,
                    userName,
                    false,
                    localTime,
                    currentPlayingTrack,
                    uiExampleMode,
                    updateTime = System.currentTimeMillis() - 10_000L,
                    callback = object : InternetHelper.RoomRequestCallback {
                        override fun onSuccess() {}
                        override fun onFailure() {}
                    }
                )
            }
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            // 修复进度条回弹问题：如果用户正在拖拽，就不要把位置重置过去
            if (!isDragging) {
                currentPos = mediaPlayer.currentPosition.toFloat()
            }
            delay(1000)
        }
    }

    suspend fun refreshExampleList() {
        if (hostName.isBlank()) {
            return
        }

        loadError = false
        isLoading = true
        try {
            val result = tools.fetchExampleMusicListSuspend(hostName, 1, 20)
            val songs = result?.first ?: emptyList()
            val total = result?.second ?: 0

            exampleMusicList.clear()
            exampleMusicList.addAll(songs)
            currentPage = 1
            hasMore = exampleMusicList.size < total
            Tools.MusicCacheManager.saveCache(
                mContext,
                hostName,
                Tools.MusicListCache(
                    songs = exampleMusicList.toList(),
                    currentPage = 1,
                    totalSongs = total,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Log.e("MusicView", "Refresh error: ${e.message}")
            loadError = true
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, end = 16.dp)
                    .combinedClickable(
                        onClick = {onExampleModeChange(!uiExampleMode)},
                        indication = LocalIndication.current,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "模板音乐库",
                    modifier = Modifier.padding(end = 16.dp)
                )
                Switch(
                    checked = uiExampleMode,
                    onCheckedChange = { onExampleModeChange(it) },
                    modifier = Modifier.scale(1.1f),
                )
            }
        }
        if (uiExampleMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    scope.launch {
                        Tools.MusicCacheManager.clearCache(mContext, hostName)
                        currentPage = 1
                        exampleMusicList.clear()
                        refreshExampleList()
                    }
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                }
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "搜索模板歌曲...",
                    useLabelAsPlaceholder = true,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "清除",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                )
            }

            if (isSearching && searchResultList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "未找到「$searchQuery」相关歌曲",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.outline
                    )
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            items(
                items = currentDisplayList,
                key = { it }
            ) { fileName ->
                val trackUrl = if (uiExampleMode) {
                    InternetHelper().getExampleStreamUrl(hostName, fileName)
                } else {
                    InternetHelper().getStreamUrl(hostName, roomName, fileName)
                }
                val coverUrl = if (uiExampleMode) {
                    InternetHelper().getExampleCoverUrl(hostName, fileName)
                } else {
                    InternetHelper().getRoomCoverUrl(hostName, roomName, fileName)
                }
                MusicItem(
                    fileName = fileName,
                    trackUrl = trackUrl,
                    coverUrl = coverUrl,
                    hostName = hostName,
                    roomName = roomName,
                    tools = tools,
                    isThisTrack = currentPlayingTrack == fileName,
                    isPlaying = isPlaying,
                    onPlayClick = { playTrack(fileName) },
                    albumArtSemaphore
                )
            }
            if (uiExampleMode && searchQuery.isNotBlank() && searchHasMore) {
                item(key = "search_load_more") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (searchIsLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            TextButton(
                                onClick = { if (!searchIsLoading) searchPage++ },
                                text = "加载更多"
                            )
                        }
                    }
                }
            } else if (uiExampleMode && searchQuery.isBlank() && hasMore) {
                item(key = "load_more") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            TextButton(
                                onClick = { if (!isLoading && hasMore) currentPage++ },
                                text = "加载更多"
                            )
                        }
                    }
                }
            }
        }
        PlayerControlBar(
            mediaPlayer = mediaPlayer,
            isPlaying = isPlaying,
            currentPlayingTrack = currentPlayingTrack,
            playList = currentDisplayList,
            isExampleMode = uiExampleMode,
            onPlayingStateChange = onPlayingStateChange,
            onManualAction = onManualAction,
            musicService = musicService,
            onPlayTrack = ::playTrack,
            hostName = hostName,
            roomName = roomName,
            userName = userName,
            onUploadClick = { launcher.launch(arrayOf("audio/mpeg", "audio/flac", "audio/aac")) }
        )
    }
}

/**
 * 音乐列表的单个条目组件，负责展示封面、文件名和播放/暂停按钮。
 *
 * 封面加载策略（三级缓存）：
 *  1. 内存缓存（Tools.ImageCache）：最快，直接使用
 *  2. 磁盘缓存（cacheDir/covers/\*.jpg）：避免重复网络请求，以 trackUrl 的 MD5 命名
 *  3. 网络加载（tools.getAudioAlbumArt）：最慢，加载成功后同时写入内存和磁盘缓存
 *
 * 加载状态：
 *  - isLoading=true：显示 CircularProgressIndicator
 *  - loadFailed=true 且 albumArt=null：显示默认音符图标
 *  - 成功：显示封面图片
 *
 * 当前播放项（isThisTrack=true）：背景高亮 + 文件名加粗 + 右侧按钮变为暂停图标。
 */
@Composable
fun MusicItem(
    fileName: String,
    trackUrl: String,
    coverUrl: String,
    hostName: String,
    roomName: String,
    tools: Tools,
    isThisTrack: Boolean,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    albumArtSemaphore: Semaphore
) {
    val context = LocalContext.current
    // 封面缓存 key 改用 coverUrl，不再用 trackUrl
    val coverCacheKey = remember(coverUrl) { coverUrl.md5() }

    var albumArt by remember(coverUrl) { mutableStateOf(Tools.ImageCache.get(coverUrl)) }
    var isLoading by remember { mutableStateOf(false) }
    var loadFailed by remember { mutableStateOf(false) }

    LaunchedEffect(coverUrl) {
        if (albumArt == null && !isLoading && !loadFailed) {
            isLoading = true
            albumArtSemaphore.acquire()
            try {
                // 磁盘缓存（封面已保存在 covers/ 目录）
                val diskBitmap = withContext(Dispatchers.IO) {
                    val cacheFile = File(context.cacheDir, "covers/$coverCacheKey.jpg")
                    if (cacheFile.exists()) BitmapFactory.decodeFile(cacheFile.absolutePath) else null
                }
                if (diskBitmap != null) {
                    Tools.ImageCache.put(coverUrl, diskBitmap)
                    albumArt = diskBitmap
                    return@LaunchedEffect
                }
                // 直接用封面 URL 加载，不用 MediaMetadataRetriever 挖音频流
                val bitmap = withContext(Dispatchers.IO) {
                    try {
                        val client = okhttp3.OkHttpClient.Builder()
                            .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                            .build()
                        val request = okhttp3.Request.Builder().url(coverUrl).build()
                        val response = client.newCall(request).execute()
                        if (response.isSuccessful) {
                            val bytes = response.body?.bytes()
                            if (bytes != null) BitmapFactory.decodeByteArray(bytes, 0, bytes.size) else null
                        } else null
                    } catch (e: Exception) { null }
                }
                if (bitmap != null) {
                    Tools.ImageCache.put(coverUrl, bitmap)
                    withContext(Dispatchers.IO) {
                        val cacheDir = File(context.cacheDir, "covers")
                        cacheDir.mkdirs()
                        FileOutputStream(File(cacheDir, "$coverCacheKey.jpg")).use {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
                        }
                    }
                    albumArt = bitmap
                } else {
                    loadFailed = true
                }
            } finally {
                albumArtSemaphore.release()
                isLoading = false
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                if (isThisTrack) MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable { onPlayClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MiuixTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            when {
                albumArt != null -> {
                    Image(
                        bitmap = albumArt!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = fileName,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MiuixTheme.textStyles.body1,
            fontWeight = if (isThisTrack) FontWeight.Bold else FontWeight.Normal
        )

        IconButton(onClick = onPlayClick) {
            Icon(
                imageVector = if (isThisTrack && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null
            )
        }
    }
}

/**
 * 文件上传中的全屏阻断式 Loading 对话框。
 * dismissOnBackPress 和 dismissOnClickOutside 均设为 false，
 * 防止用户在上传未完成时意外关闭，导致上传中断或状态不一致。
 */
@Composable
fun LoadingDialog() {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .background(MiuixTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "正在上传，请稍候...", style = MiuixTheme.textStyles.body2)
            }
        }
    }
}


@Composable
fun PlayerControlBar(
    mediaPlayer: MediaPlayer?,
    isPlaying: Boolean,
    currentPlayingTrack: String,
    playList: List<String>,
    isExampleMode: Boolean,
    onPlayingStateChange: (Boolean) -> Unit,
    onManualAction: () -> Unit,
    musicService: MusicService?,
    onPlayTrack: (String) -> Unit,
    hostName: String,
    roomName: String,
    userName: String,
    onUploadClick: () -> Unit
) {
    // 使用 remember 隔离进度条状态，避免父级重组影响
    var currentPos by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val duration = remember(mediaPlayer?.duration) {
        (mediaPlayer?.duration ?: 0).toFloat().coerceAtLeast(1f)
    }

    // 播放时每秒更新进度（暂停时不更新，避免无效重组）
    LaunchedEffect(isPlaying, mediaPlayer) {
        if (isPlaying && mediaPlayer != null) {
            while (isPlaying) {
                if (!isDragging) {
                    currentPos = mediaPlayer.currentPosition.toFloat()
                }
                delay(1000)
            }
        } else {
            // 暂停时同步一次当前进度
            mediaPlayer?.let { currentPos = it.currentPosition.toFloat() }
        }
    }

    // 播放按钮脉冲动画 - 播放时持续轻微跳动
    val pulseScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                easing = EaseInOutQuad
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MiuixTheme.colorScheme.surface)
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 28.dp)
    ) {
        // 非模板模式下显示上传按钮
        if (!isExampleMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onUploadClick() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = MiuixTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "上传音乐",
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.primary
                    )
                }
            }
        }

        // 进度条区域
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = currentPos,
                onValueChange = {
                    isDragging = true
                    currentPos = it
                },
                onValueChangeFinished = {
                    isDragging = false
                    onManualAction()
                    mediaPlayer?.seekTo(currentPos.toInt())
                    InternetHelper().updateMusicStatus(
                        hostName,
                        roomName,
                        userName,
                        !isPlaying,
                        (currentPos / 1000).toInt(),
                        currentPlayingTrack,
                        isExampleMode,
                        updateTime = System.currentTimeMillis(),
                        callback = object : InternetHelper.RoomRequestCallback {
                            override fun onSuccess() {}
                            override fun onFailure() {}
                        }
                    )
                },
                valueRange = 0f..duration,
                modifier = Modifier.height(32.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    Tools().formatTime(currentPos.toInt()),
                    style = MiuixTheme.textStyles.body2
                )
                Text(
                    Tools().formatTime(duration.toInt()),
                    style = MiuixTheme.textStyles.body2
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // 曲目标题和播放按钮区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (currentPlayingTrack.isNotBlank()) currentPlayingTrack else "未选择曲目",
                        style = MiuixTheme.textStyles.body1.copy(fontWeight = FontWeight.ExtraBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isPlaying) "正在播放" else "暂停中",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.primary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val currentIndex = if (currentPlayingTrack.isNotBlank()) {
                    playList.indexOf(currentPlayingTrack)
                } else -1

                IconButton(
                    onClick = {
                        if (currentIndex > 0) onPlayTrack(playList[currentIndex - 1])
                    },
                    enabled = currentIndex > 0
                ) {
                    Icon(Icons.Default.SkipPrevious, "上一首", modifier = Modifier.size(30.dp))
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                        .background(MiuixTheme.colorScheme.primary, CircleShape)
                        .clickable {
                            if (currentPlayingTrack.isBlank()) return@clickable
                            onManualAction()
                            val newIsPlaying = musicService?.togglePlayPause() ?: !isPlaying
                            onPlayingStateChange(newIsPlaying)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = {
                        if (currentIndex >= 0 && currentIndex < playList.size - 1) {
                            onPlayTrack(playList[currentIndex + 1])
                        }
                    },
                    enabled = currentIndex >= 0 && currentIndex < playList.size - 1
                ) {
                    Icon(Icons.Default.SkipNext, "下一首", modifier = Modifier.size(30.dp))
                }
            }
        }
    }
}