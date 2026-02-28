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
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 启用沉浸式边到边显示，让内容延伸到状态栏/导航栏区域
        enableEdgeToEdge()
        setContent {
            QuantumTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // 读取持久化配置（用户名、是否首次启动等）
                    val setting = getSharedPreferences("com.xshe.quantum", 0)
                    // isFirst 控制是否显示初始化引导页
                    var isFirst by remember { mutableStateOf(setting.getBoolean("FIRST", true)) }
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
 *  - [serverExampleMode]  服务端推送的模式标志，用于同步其他端的播放源
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
    val mediaPlayer = musicService?.mediaPlayer
    var uiExampleMode by remember { mutableStateOf(false) }
    var serverExampleMode by remember { mutableStateOf(false) }
    var lastManualActionTime by remember { mutableLongStateOf(0L) }
    val MANUAL_COOLDOWN = 3000L
    var updateVersionName by remember { mutableStateOf("") }
    var updateUrl by remember { mutableStateOf("") }

    values.historyHost = setting.getString("history_host", "暂无历史连接主机").toString()

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
                            Handler(Looper.getMainLooper()).post {
                                updateVersionName = serverName
                                updateUrl = url
                            }
                        } else {
                            Handler(Looper.getMainLooper()).post {
                                updateVersionName = ""
                                updateUrl = ""
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure() {}
            })
    }

    /**
     * 监听当前房间名变化：当离开房间（roomName 为空或"null"）时，
     * 立即停止并重置 MediaPlayer，同时清空播放状态，防止残留声音。
     */
    LaunchedEffect(values.roomName) {
        if (values.roomName.isNullOrEmpty() || values.roomName == "null") {
            musicService?.mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                    player.reset()
                }
            }
            globalIsPlaying = false
            currentPlayingTrack = ""
        }
    }


    /**
     * 将服务端推送的音乐状态同步到本地 MediaPlayer。
     *
     * 防抖机制：若距离上次手动操作不足 [MANUAL_COOLDOWN](3秒)，直接跳过，
     * 避免本地刚切歌/暂停就被服务端状态覆盖，造成抖动。
     *
     * 同步逻辑：
     *  - 曲目变化：重置 MediaPlayer，重新加载并 seek 到服务端进度后播放
     *  - 曲目相同：
     *    - 播放/暂停状态不一致时，对齐本地状态
     *    - 进度偏差过大时（暂停>2s、播放>3s）执行 seek 纠偏
     */
    fun applyMusicStatus(json: JSONObject, player: MediaPlayer) {
        if (System.currentTimeMillis() - lastManualActionTime < MANUAL_COOLDOWN) return

        val sPause = json.optBoolean("is_music_pause", true)
        val sTime = json.optInt("current_music_time", 0)
        val sMusic = json.optString("current_music", "")
        val sExampleMode = json.optBoolean("is_playing_example", false)

        val modeChanged = sExampleMode != serverExampleMode
        serverExampleMode = sExampleMode
        if (uiExampleMode != sExampleMode) {
            uiExampleMode = sExampleMode
        }

        if (modeChanged && sMusic.isNotBlank() && sMusic == currentPlayingTrack) {
            val playUrl = if (sExampleMode) {
                InternetHelper().getExampleStreamUrl(savedHost, sMusic)
            } else {
                InternetHelper().getStreamUrl(savedHost, values.roomName, sMusic)
            }
            player.reset()
            player.setDataSource(playUrl)
            player.prepareAsync()
            player.setOnPreparedListener { mp ->
                mp.seekTo(sTime * 1000)
                if (!sPause) mp.start()
                globalIsPlaying = !sPause
            }
            return
        }

        if (sMusic.isNotBlank() && sMusic != currentPlayingTrack) {
            currentPlayingTrack = sMusic
            val playUrl = if (sExampleMode) {
                InternetHelper().getExampleStreamUrl(savedHost, sMusic)
            } else {
                InternetHelper().getStreamUrl(savedHost, values.roomName, sMusic)
            }
            player.reset()
            player.setDataSource(playUrl)
            player.prepareAsync()
            player.setOnPreparedListener { mp ->
                mp.seekTo(sTime * 1000)
                if (!sPause) mp.start()
                globalIsPlaying = !sPause
            }
        } else if (sMusic.isNotBlank()) {
            if (!sPause != globalIsPlaying) {
                if (sPause) player.pause() else player.start()
                globalIsPlaying = !sPause
            }
            val localSec = player.currentPosition / 1000
            if (sPause) {
                if (Math.abs(localSec - sTime) > 2) player.seekTo(sTime * 1000)
            } else {
                if (sTime > localSec + 3) player.seekTo(sTime * 1000)
            }
        }
    }

    /**
     * 首次进入房间时立即拉取一次音乐状态（即时同步），
     * 而不等待轮询定时器触发，减少刚入房时的感知延迟。
     */
    LaunchedEffect(values.roomName, musicService) {
        if (savedHost.isNotBlank() && !values.roomName.isNullOrEmpty() && musicService != null) {
            val player = musicService!!.mediaPlayer ?: return@LaunchedEffect
            InternetHelper().getMusicStatus(
                savedHost, values.roomName, tools.userName,
                object : InternetHelper.RequestCallback {
                    override fun onSuccess(responseBody: String) {
                        try {
                            val json = JSONObject(responseBody)
                            Handler(Looper.getMainLooper()).post {
                                applyMusicStatus(json, player)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure() {}
                })
        }
    }

    /**
     * 每 1 秒轮询一次服务端音乐状态，用于持续保持多端同步。
     * 通过 [applyMusicStatus] 的防抖逻辑，保证本地手动操作不被立即覆盖。
     * 当房间名、主机地址或 musicService 变化时，协程自动重启。
     */
    LaunchedEffect(values.roomName, savedHost, musicService) {
        val player = musicService?.mediaPlayer ?: return@LaunchedEffect
        while (true) {
            if (savedHost.isNotBlank() && !values.roomName.isNullOrEmpty()) {
                InternetHelper().getMusicStatus(
                    savedHost, values.roomName, tools.userName,
                    object : InternetHelper.RequestCallback {
                        override fun onSuccess(responseBody: String) {
                            try {
                                val json = JSONObject(responseBody)
                                Handler(Looper.getMainLooper()).post {
                                    applyMusicStatus(json, player)
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (i) {
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

                        1 -> ChatView(
                            tools,
                            values,
                            savedHost,
                            setting
                        )

                        2 -> MusicView(
                            savedHost, values.roomName, tools, mediaPlayer,
                            globalIsPlaying, currentPlayingTrack,
                            uiExampleMode = uiExampleMode,
                            serverExampleMode = serverExampleMode,
                            userName = tools.userName,
                            onUiModeChange = { newMode ->
                                uiExampleMode = newMode
                                InternetHelper().setExampleMode(
                                    savedHost, values.roomName, tools.userName, newMode,
                                    object : InternetHelper.RoomRequestCallback {
                                        override fun onSuccess() {}
                                        override fun onFailure() {}
                                    })
                            },
                            onPlayingStateChange = { globalIsPlaying = it },
                            onCurrentTrackChange = { newTrack -> currentPlayingTrack = newTrack },
                            onManualAction = { lastManualActionTime = System.currentTimeMillis() }
                        )
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
                    DropdownMenuItem(
                        text = { Text(values.historyHost) },
                        onClick = {
                            onHostNameChange(values.historyHost)
                            expanded = false
                        }
                    )
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
                                                musicService?.mediaPlayer?.let { player ->
                                                    try {
                                                        if (player.isPlaying) player.stop()
                                                        player.reset()
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                }
                                                // 立即清空播放状态，阻断 applyMusicStatus 的轮询恢复播放
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

    LaunchedEffect(values.roomName) {
        values.messageList.clear()
        while (true) {
            if (host.isNotBlank() && values.roomName.isNotBlank()) {
                InternetHelper().getMessages(
                    url,
                    values.roomName,
                    tools.userName,
                    object : InternetHelper.RequestCallback {
                        override fun onSuccess(responseBody: String) {
                            val jsonArray = JSONArray(responseBody)
                            if (jsonArray.length() > values.messageList.size) {
                                val currentSize = values.messageList.size
                                for (i in currentSize until jsonArray.length()) {
                                    values.messageList.add(jsonArray.getString(i))
                                }
                            }
                        }

                        override fun onFailure() {}
                    })
            }
            delay(1500)
        }
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = true,
        ) {
            itemsIndexed(values.messageList.reversed()) { index, msg ->
                key(index) {
                    val isMe = msg.startsWith("$userName:")
                    val sender = msg.substringBefore(":", "未知用户")
                    val displayMsg = if (msg.contains(":")) msg.substringAfter(":") else msg

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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
            IconButton(
                onClick = {
                    if (inputMessage.isNotBlank()) {
                        val fullMsg = "${userName}:${inputMessage}"
                        InternetHelper().appendMessage(
                            url, values.roomName, fullMsg,
                            object : InternetHelper.RoomRequestCallback {
                                override fun onSuccess() {
                                    if (!values.messageList.contains(fullMsg)) {
                                        values.messageList.add(fullMsg)
                                    }
                                    inputMessage = ""
                                }

                                override fun onFailure() {
                                    tools.showToast(mContext, "发送失败")
                                }
                            }
                        )
                    }
                },
                modifier = Modifier.background(MiuixTheme.colorScheme.primary, CircleShape)
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
    uiExampleMode: Boolean,
    serverExampleMode: Boolean,
    userName: String,
    onUiModeChange: (Boolean) -> Unit,
    onPlayingStateChange: (Boolean) -> Unit,
    onCurrentTrackChange: (String) -> Unit,
    onManualAction: () -> Unit
) {
    if (mediaPlayer == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("正在连接播放服务...")
        }
        return
    }

    val exampleMusicList = remember { mutableStateListOf<String>() }
    var currentPage by remember { mutableIntStateOf(1) }
    var hasMore by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val roomMusicList = remember { mutableStateListOf<String>() }
    val mContext = LocalContext.current
    var currentPos by remember { mutableFloatStateOf(0f) }
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
    val MAX_RETRY = 2

    val currentDisplayList = when {
        uiExampleMode && searchQuery.isNotBlank() -> searchResultList
        uiExampleMode -> exampleMusicList
        else -> roomMusicList
    }

    /**
     * 模板音乐库分页加载（第 2 页起）。
     * 当 [currentPage] > 1 且仍有更多数据（[hasMore]=true）时，追加新一页到 [exampleMusicList]。
     * 用 filter 去重，防止网络重试时出现重复条目。
     * 加载失败时回滚 currentPage 并恢复 hasMore，允许用户重试。
     */
    LaunchedEffect(uiExampleMode, currentPage) {
        if (uiExampleMode && currentPage > 1 && hasMore && !isLoading) {
            isLoading = true
            try {
                val (songs, total) = tools.fetchExampleMusicListSuspend(hostName, currentPage, 20)
                val newSongs = songs.filter { it !in exampleMusicList }
                exampleMusicList.addAll(newSongs)
                hasMore = exampleMusicList.size < total

                // 更新缓存
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

            }
        }
    }

    /**
     * 搜索词防抖处理（延迟 500ms 后执行）。
     * 每次搜索词变化时清空旧结果、从第 1 页重新加载。
     * 搜索词为空时退出搜索模式，回到模板列表视图。
     */
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
            val (songs, total) = tools.searchExampleMusicSuspend(hostName, searchQuery, 1, 20)
            searchResultList.addAll(songs)
            searchHasMore = searchResultList.size < total
        } catch (e: Exception) {
        } finally {
            searchIsLoading = false
        }
    }

    /**
     * 搜索结果分页加载（第 2 页起）。
     * 逻辑与模板列表分页相同：追加去重、失败回滚。
     */
    LaunchedEffect(searchPage) {
        if (searchPage <= 1 || !searchHasMore || searchIsLoading || searchQuery.isBlank()) return@LaunchedEffect
        searchIsLoading = true
        try {
            val (songs, total) = tools.searchExampleMusicSuspend(
                hostName,
                searchQuery,
                searchPage,
                20
            )
            val newSongs = songs.filter { it !in searchResultList }
            searchResultList.addAll(newSongs)
            searchHasMore = searchResultList.size < total
        } catch (e: Exception) {
            searchPage--
            searchHasMore = true
        } finally {
            searchIsLoading = false
        }
    }

    /**
     * 无限滚动触发器：监听 LazyColumn 最后可见条目的索引。
     * 当滚动到列表末尾时，根据当前模式（搜索/模板列表）自动递增对应页码，
     * 触发上方的分页 LaunchedEffect 加载下一页数据。
     */
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


    /**
     * 播放指定曲目的统一入口。
     *
     * 流程：
     * 1. 记录操作时间（触发防抖，防止服务端状态在 3s 内覆盖）
     * 2. 根据当前模式（模板/房间）构造流媒体 URL
     * 3. 重置 MediaPlayer 并异步 prepare
     * 4. prepare 完成后立即 start，并将最新状态同步到服务端
     */
    val timeoutHandler = remember { Handler(Looper.getMainLooper()) }

    fun playTrack(fileName: String, isUserInitiated: Boolean = false) {
        if (fileName.isBlank()) return
        if (isUserInitiated) {
            retryCount = 0  // 用户操作时重置重试计数
        }
        onCurrentTrackChange(fileName)
        onManualAction()

        val playUrl = if (serverExampleMode) {
            InternetHelper().getExampleStreamUrl(hostName, fileName)
        } else {
            InternetHelper().getStreamUrl(hostName, roomName, fileName)
        }
        timeoutHandler.removeCallbacksAndMessages(null)

        mediaPlayer.apply {
            try {
                stop()
                reset()
                setDataSource(playUrl)

                setOnErrorListener { mp, what, extra ->
                    timeoutHandler.removeCallbacksAndMessages(null)
                    if (retryCount < MAX_RETRY) {
                        retryCount++
                        mainHandler.postDelayed({
                            playTrack(fileName, isUserInitiated = false)  // 重试时不要重置计数
                        }, 2000)
                    } else {
                        retryCount = 0
                        onPlayingStateChange(false)
                        tools.showToast(mContext, "播放错误 (what=$what, extra=$extra)")
                    }
                    true
                }

                val timeoutRunnable = Runnable {
                    if (!isPlaying) {
                        reset()
                        onPlayingStateChange(false)
                        tools.showToast(mContext, "播放超时，请检查网络")
                    }
                }
                timeoutHandler.postDelayed(timeoutRunnable, 15000)

                setOnCompletionListener {
                    mainHandler.post {
                        val activeList = when {
                            uiExampleMode && searchQuery.isNotBlank() -> searchResultList
                            uiExampleMode -> exampleMusicList
                            else -> roomMusicList
                        }
                        val currentIndex = activeList.indexOf(currentPlayingTrack)
                        if (currentIndex != -1 && currentIndex < activeList.size - 1) {
                            playTrack(activeList[currentIndex + 1], isUserInitiated = false)  // 自动下一首，非用户操作
                        } else {
                            onPlayingStateChange(false)
                        }
                    }
                }

                setOnPreparedListener { mp ->
                    timeoutHandler.removeCallbacksAndMessages(null)
                    mp.start()
                    onPlayingStateChange(true)
                    retryCount = 0  // 成功播放时重置计数

                    InternetHelper().updateMusicStatus(
                        hostName,
                        roomName,
                        userName,
                        false,
                        0,
                        fileName,
                        serverExampleMode,
                        updateTime = System.currentTimeMillis(),
                        callback = object : InternetHelper.RoomRequestCallback {
                            override fun onSuccess() {}
                            override fun onFailure() {
                                Log.e("MusicView", "状态同步失败，但不影响播放")
                            }
                        }
                    )
                }

                prepareAsync()
            } catch (e: Exception) {
                timeoutHandler.removeCallbacksAndMessages(null)
                e.printStackTrace()
                tools.showToast(mContext, "播放失败: ${e.message}")
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
                            // 上传成功后增量刷新房间音乐列表：追加新曲目，不整体清空
                            tools.fetchMusicList(hostName, roomName) { list ->
                                val toAdd = list.filter { it !in roomMusicList }
                                val toRemove = roomMusicList.filter { it !in list }
                                roomMusicList.removeAll(toRemove)
                                roomMusicList.addAll(toAdd)
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

    /**
     * 当房间名或模式切换时，重置/初始化对应的音乐列表。
     * - 切换到模板模式：清空状态、从第 1 页开始异步加载模板音乐
     * - 切换到房间模式：增量拉取房间音乐列表（仅添加新增、删除已移除项）
     * - 同时重置搜索框，避免旧搜索结果残留
     */
    LaunchedEffect(roomName, uiExampleMode) {
        if (uiExampleMode) {
            //尝试从缓存恢复
            val cached = withContext(Dispatchers.IO) {
                Tools.MusicCacheManager.loadCache(mContext, hostName)
            }
            if (cached != null) {
                exampleMusicList.clear()
                exampleMusicList.addAll(cached.songs)
                currentPage = cached.currentPage
                // 根据 totalSongs 和已加载数量判断 hasMore
                hasMore = exampleMusicList.size < cached.totalSongs
            } else {
                // 无缓存，重置状态
                currentPage = 1
                hasMore = true
                exampleMusicList.clear()
            }

            // 发起网络请求获取第一页，更新缓存
            loadError = false
            isLoading = true
            try {
                val (songs, total) = tools.fetchExampleMusicListSuspend(hostName, 1, 20)
                // 如果已有缓存，可能需要检查是否有新歌
                val newSongs = songs.filter { it !in exampleMusicList }
                if (newSongs.isNotEmpty()) {
                    // 如果有新歌且当前列表不为空，可能是服务端更新了，可以提示刷新或直接追加
                    // 这里选择追加到末尾，并更新 totalSongs
                    exampleMusicList.addAll(newSongs)
                } else if (exampleMusicList.isEmpty()) {
                    exampleMusicList.addAll(songs)
                }
                // 更新 total 和 hasMore
                val totalSongs = total
                hasMore = exampleMusicList.size < totalSongs
                // 保存缓存（包括当前所有歌曲和 currentPage）
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
                loadError = true
            } finally {
                isLoading = false
            }
        } else {
            if (roomName != "null") {
                // 房间模式：增量更新，只增删变化的曲目，不整体清空重建列表
                tools.fetchMusicList(hostName, roomName) { list ->
                    val toAdd = list.filter { it !in roomMusicList }
                    val toRemove = roomMusicList.filter { it !in list }
                    roomMusicList.removeAll(toRemove)
                    roomMusicList.addAll(toAdd)
                }
            }
        }
    }

    /**
     * 播放进度实时更新：每秒将 MediaPlayer 的当前进度同步到 [currentPos]，
     * 驱动进度条 Slider 的 UI 更新。仅在 isPlaying=true 时运行，暂停后自动停止轮询。
     */
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPos = mediaPlayer.currentPosition.toFloat()
            delay(1000)
        }
    }


    /**
     * 播放进度定期上报：每 5 秒将本地当前进度同步到服务端。
     * 此处 updateTime 故意设置为 10 秒前（System.currentTimeMillis() - 10_000），
     * 使该上报的优先级低于手动操作，避免定期上报覆盖其他端的手动操作。
     * 仅在播放中（isPlaying=true）时运行。
     */
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
                    serverExampleMode,
                    updateTime = System.currentTimeMillis() - 10_000L,
                    callback = object : InternetHelper.RoomRequestCallback {
                        override fun onSuccess() {}
                        override fun onFailure() {}
                    }
                )
            }
        }
    }


    suspend fun refreshExampleList() {
        loadError = false
        isLoading = true
        try {
            val (songs, total) = tools.fetchExampleMusicListSuspend(hostName, 1, 20)
            exampleMusicList.clear()
            exampleMusicList.addAll(songs)
            currentPage = 1
            hasMore = exampleMusicList.size < total
            // 保存缓存
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
                        onClick = { onUiModeChange(!uiExampleMode) },
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
                    onCheckedChange = null,
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
                    hostName = hostName,
                    roomName = roomName,
                    tools = tools,
                    isThisTrack = currentPlayingTrack == fileName,
                    isPlaying = isPlaying,
                    onPlayClick = { playTrack(fileName) }
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MiuixTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp, bottom = 28.dp)
            ) {
                if (!uiExampleMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    launcher.launch(
                                        arrayOf(
                                            "audio/mpeg",
                                            "audio/flac",
                                            "audio/aac"
                                        )
                                    )
                                }
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

                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = currentPos,
                        onValueChange = { currentPos = it },
                        onValueChangeFinished = {
                            onManualAction()
                            mediaPlayer.seekTo(currentPos.toInt())
                            InternetHelper().updateMusicStatus(
                                hostName,
                                roomName,
                                userName,
                                !isPlaying,
                                (currentPos / 1000).toInt(),
                                currentPlayingTrack,
                                serverExampleMode,
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
                            tools.formatTime(currentPos.toInt()),
                            style = MiuixTheme.textStyles.body2
                        )
                        Text(
                            tools.formatTime(duration.toInt()),
                            style = MiuixTheme.textStyles.body2
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

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
                        IconButton(
                            onClick = {
                                val currentIndex = currentDisplayList.indexOf(currentPlayingTrack)
                                if (currentIndex > 0) playTrack(currentDisplayList[currentIndex - 1])
                            },
                            enabled = currentDisplayList.indexOf(currentPlayingTrack) > 0
                        ) {
                            Icon(
                                Icons.Default.SkipPrevious,
                                "上一首",
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(MiuixTheme.colorScheme.primary, CircleShape)
                                .clickable {
                                    if (currentPlayingTrack.isBlank()) return@clickable
                                    onManualAction()
                                    val nextPauseState = isPlaying
                                    if (isPlaying) {
                                        mediaPlayer.pause(); onPlayingStateChange(false)
                                    } else {
                                        mediaPlayer.start(); onPlayingStateChange(true)
                                    }
                                    InternetHelper().updateMusicStatus(
                                        hostName,
                                        roomName,
                                        userName,
                                        nextPauseState,
                                        (mediaPlayer.currentPosition / 1000),
                                        currentPlayingTrack,
                                        serverExampleMode,
                                        updateTime = System.currentTimeMillis(),
                                        callback = object : InternetHelper.RoomRequestCallback {
                                            override fun onSuccess() {}
                                            override fun onFailure() {}
                                        }
                                    )
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
                                val currentIndex = currentDisplayList.indexOf(currentPlayingTrack)
                                if (currentIndex != -1 && currentIndex < currentDisplayList.size - 1) {
                                    playTrack(currentDisplayList[currentIndex + 1])
                                }
                            },
                            enabled = currentDisplayList.indexOf(currentPlayingTrack) < currentDisplayList.size - 1
                        ) {
                            Icon(Icons.Default.SkipNext, "下一首", modifier = Modifier.size(30.dp))
                        }
                    }
                }
            }
        }
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
    hostName: String,
    roomName: String,
    tools: Tools,
    isThisTrack: Boolean,
    isPlaying: Boolean,
    onPlayClick: () -> Unit
) {
    fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    val context = LocalContext.current
    var albumArt by remember(trackUrl) { mutableStateOf(Tools.ImageCache.get(trackUrl)) }
    var isLoading by remember { mutableStateOf(false) }
    var loadFailed by remember { mutableStateOf(false) }

    LaunchedEffect(trackUrl) {
        if (albumArt == null && !isLoading && !loadFailed) {
            isLoading = true

            val diskBitmap = withContext(Dispatchers.IO) {
                val cacheFile = File(context.cacheDir, "covers/${trackUrl.md5()}.jpg")
                if (cacheFile.exists()) {
                    BitmapFactory.decodeFile(cacheFile.absolutePath)
                } else null
            }
            if (diskBitmap != null) {
                Tools.ImageCache.put(trackUrl, diskBitmap)
                albumArt = diskBitmap
                isLoading = false
                return@LaunchedEffect
            }

            val bitmap = withContext(Dispatchers.IO) {
                try {
                    tools.getAudioAlbumArt(trackUrl)
                } catch (e: Exception) {
                    null
                }
            }
            if (bitmap != null) {
                Tools.ImageCache.put(trackUrl, bitmap)
                withContext(Dispatchers.IO) {
                    val cacheDir = File(context.cacheDir, "covers")
                    cacheDir.mkdirs()
                    val cacheFile = File(cacheDir, "${trackUrl.md5()}.jpg")
                    FileOutputStream(cacheFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                }
                albumArt = bitmap
            } else {
                loadFailed = true
            }
            isLoading = false
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
