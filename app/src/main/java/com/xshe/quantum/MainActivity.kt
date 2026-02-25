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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuantumTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val setting = getSharedPreferences("com.xshe.quantum", 0)
                    var isFirst by remember { mutableStateOf(setting.getBoolean("FIRST", true)) }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            101
                        )
                    }
                    if (isFirst) {
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

    fun applyMusicStatus(json: JSONObject, player: MediaPlayer) {
        if (System.currentTimeMillis() - lastManualActionTime < MANUAL_COOLDOWN) return

        val sPause = json.optBoolean("is_music_pause", true)
        val sTime = json.optInt("current_music_time", 0)
        val sMusic = json.optString("current_music", "")
        val sExampleMode = json.optBoolean("is_playing_example", false)

        serverExampleMode = sExampleMode

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
            NavigationBar {
                val tabs = listOf(
                    Triple(stringResource(R.string.hostButton), 0, Icons.Default.Home),
                    Triple(stringResource(R.string.chatButton), 1, Icons.AutoMirrored.Filled.Chat),
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

                    if (updateVersionName.isNotBlank() && updateUrl.isNotBlank()) {
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
                            onUiModeChange = { newMode -> uiExampleMode = newMode },
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

    val updateRoomList = {
        itemList.clear()
        if (tools.roomNames.isNotEmpty()) {
            for (idx in tools.roomNames.indices) {
                val roomName = tools.roomNames[idx]
                val statusText = if (tools.roomStatuses[idx]) "√" else "×"
                val isCurrentSelected = roomName == values.roomName && !values.isCanSelected
                itemList.add(Values.ListItem(roomName, statusText, isCurrentSelected))
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
                                            musicService?.mediaPlayer?.let { player ->
                                                if (player.isPlaying) {
                                                    player.stop()
                                                }
                                                player.reset()
                                            }

                                            itemList[index] = item.copy(isSelected = false)
                                            values.isCanSelected = true
                                            values.roomName = ""
                                            onExitRoomSuccess()
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
                    horizontalArrangement = Arrangement.End,
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
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = { onDismissRequest() }, text = "取消")
                    TextButton(onClick = { onConfirmation(roomName, password) }, text = "加入")
                }
            }
        }
    }
}

@Composable
fun ConnectListItem(
    listItem: Values.ListItem,
    values: Values,
    onSelectClick: () -> Unit
) {
    val isSelected = listItem.isSelected
    val cardShape = RoundedCornerShape(16.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .graphicsLayer {
                shadowElevation = if (isSelected) 20f else 0f
                shape = cardShape
                clip = true
            }
            .background(
                color = if (isSelected) MiuixTheme.colorScheme.primaryContainer
                else MiuixTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = cardShape
            )
            .clickable(onClick = onSelectClick),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = listItem.itemHost,
                    style = MiuixTheme.textStyles.body1,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected)
                        MiuixTheme.colorScheme.onPrimaryContainer   // 主题修改后将自动变深
                    else
                        MiuixTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isSelected) "当前所在房间" else "点击加入",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.outline
                )
            }

            Text(
                text = listItem.itemStatus,
                fontSize = 20.sp,
                color = if (isSelected) MiuixTheme.colorScheme.onPrimaryContainer else Color.Unspecified, 
                modifier = Modifier
                    .background(
                        color = if (listItem.itemStatus == "√")
                            (if (isSelected) MiuixTheme.colorScheme.primary.copy(alpha = 0.3f)
                            else MiuixTheme.colorScheme.primary.copy(alpha = 0.1f))
                        else
                            (if (isSelected) MiuixTheme.colorScheme.error.copy(alpha = 0.3f)
                            else MiuixTheme.colorScheme.error.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

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
                    val displayMsg = msg.substringAfter(":")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(
                                placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                fadeInSpec = tween(300),
                                fadeOutSpec = tween(300)
                            ),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        val bubbleColor = if (isMe) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surface
                        val bubbleShape = RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 0.dp,
                            bottomEnd = if (isMe) 0.dp else 16.dp
                        )
                        Card(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .background(color = bubbleColor, shape = bubbleShape)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (!isMe) {
                                    Text(
                                        text = msg.substringBefore(":"),
                                        style = MiuixTheme.textStyles.body2,
                                        color = MiuixTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = displayMsg,
                                    color = if (isMe) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface,
                                    style = MiuixTheme.textStyles.body2
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

    val currentDisplayList = when {
        uiExampleMode && searchQuery.isNotBlank() -> searchResultList
        uiExampleMode -> exampleMusicList
        else -> roomMusicList
    }

    LaunchedEffect(uiExampleMode, currentPage) {
        if (uiExampleMode && currentPage > 1 && hasMore && !isLoading) {
            isLoading = true
            loadError = false
            try {
                val (songs, total) = tools.fetchExampleMusicListSuspend(hostName, currentPage, 20)
                val newSongs = songs.filter { it !in exampleMusicList }
                exampleMusicList.addAll(newSongs)
                hasMore = exampleMusicList.size < total
            } catch (e: Exception) {
                loadError = true
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
            val (songs, total) = tools.searchExampleMusicSuspend(hostName, searchQuery, 1, 20)
            searchResultList.addAll(songs)
            searchHasMore = searchResultList.size < total
        } catch (e: Exception) {
        } finally {
            searchIsLoading = false
        }
    }

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

    val playTrack: (String) -> Unit = playTrack@{ fileName ->
        if (fileName.isBlank()) return@playTrack
        onCurrentTrackChange(fileName)
        onManualAction()
        val playUrl = if (uiExampleMode) {
            InternetHelper().getExampleStreamUrl(hostName, fileName)
        } else {
            InternetHelper().getStreamUrl(hostName, roomName, fileName)
        }

        mediaPlayer.apply {
            try {
                stop()
                reset()
                setDataSource(playUrl)
                prepareAsync()
                setOnPreparedListener { mp ->
                    mp.start()
                    onPlayingStateChange(true)
                    InternetHelper().updateMusicStatus(
                        hostName,
                        roomName,
                        userName,
                        false,
                        0,
                        fileName,
                        uiExampleMode,
                        updateTime = System.currentTimeMillis(),
                        object : InternetHelper.RoomRequestCallback {
                            override fun onSuccess() {}
                            override fun onFailure() {
                                tools.showToast(mContext, "状态同步失败")
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
                                roomMusicList.clear()
                                roomMusicList.addAll(list)
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

    LaunchedEffect(roomName, uiExampleMode) {
        searchQuery = ""
        isSearching = false
        searchResultList.clear()
        if (uiExampleMode) {
            currentPage = 1
            hasMore = true
            exampleMusicList.clear()
            loadError = false
            isLoading = true
            try {
                val (songs, total) = tools.fetchExampleMusicListSuspend(hostName, 1, 20)
                exampleMusicList.addAll(songs)
                hasMore = exampleMusicList.size < total
            } catch (e: Exception) {
                loadError = true
                hasMore = true
            } finally {
                isLoading = false
            }
        } else {
            if (roomName != "null") {
                tools.fetchMusicList(hostName, roomName) { list ->
                    roomMusicList.clear()
                    roomMusicList.addAll(list)
                }
            }
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPos = mediaPlayer.currentPosition.toFloat()
            delay(1000)
        }
    }

    LaunchedEffect(mediaPlayer) {
        mediaPlayer.setOnCompletionListener {
            mainHandler.post {
                val activeList = when {
                    uiExampleMode && searchQuery.isNotBlank() -> searchResultList
                    uiExampleMode -> exampleMusicList
                    else -> roomMusicList
                }
                val currentIndex = activeList.indexOf(currentPlayingTrack)
                if (currentIndex != -1 && currentIndex < activeList.size - 1) {
                    val nextTrack = activeList[currentIndex + 1]
                    playTrack(nextTrack)
                } else {
                    onPlayingStateChange(false)
                }
            }
        }
    }

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
                            TextButton(onClick = { if (!searchIsLoading) searchPage++ }, text = "加载更多")
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
                            TextButton(onClick = { if (!isLoading && hasMore) currentPage++ }, text = "加载更多")
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
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = MiuixTheme.colorScheme.primary)
                            Spacer(Modifier.width(4.dp))
                            Text("上传音乐", style = MiuixTheme.textStyles.body1, color = MiuixTheme.colorScheme.primary)
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
                                uiExampleMode,
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
                                        uiExampleMode,
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