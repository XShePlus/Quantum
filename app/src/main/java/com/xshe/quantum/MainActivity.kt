package com.xshe.quantum

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.graphicsLayer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuantumTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val setting = getSharedPreferences("com.xshe.quantum", 0)
                    var isFirst by remember { mutableStateOf(setting.getBoolean("FIRST", true)) }

                    if (isFirst) {
                        FirstComposeView(
                            modifier = Modifier
                                .padding(innerPadding)
                                .background(color = MaterialTheme.colorScheme.background),
                            setting,
                            onConfirm = {
                                setting.edit().putBoolean("FIRST", false).apply()
                                isFirst = false
                            })
                    } else {
                        MainComposeView(
                            modifier = Modifier
                                .padding(innerPadding)
                                .background(color = MaterialTheme.colorScheme.background),
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
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
    )
    val plusButtonModifier = Modifier.padding(5.dp)
    val plusButtonShape = MaterialTheme.shapes.small

    Column(
        modifier = modifier
            .fillMaxSize() // 占满全屏
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = name,
            onValueChange = { newText ->
                name = newText
            },
            label = { Text("来个名头") },
            enabled = true,
            readOnly = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Button(
            onClick = {
                setting.edit().putString("User", name).commit()
                setting.edit().putString("history_host", "").commit()
                onConfirm()
            },
            colors = buttonColors,
            shape = plusButtonShape,
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
    val userName = setting.getString("User", "") ?: "User"
    val mediaPlayer = remember { MediaPlayer() }
    var globalIsPlaying by remember { mutableStateOf(false) }
    var currentPlayingTrack by remember { mutableStateOf("") }
    var roomNumbers by remember { mutableStateOf(Values.RoomNumbers()) }
    val mContext = LocalContext.current
    var lastManualActionTime by remember { mutableLongStateOf(0L) }

    values.historyHost = setting.getString("history_host", "暂无历史连接主机").toString()

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
    // 核心轮询同步逻辑
    LaunchedEffect(values.roomName, savedHost) {
        while (true) {
            if (savedHost.isNotBlank() && !values.roomName.isNullOrEmpty()) {
                InternetHelper().getMusicStatus(
                    savedHost,
                    values.roomName,
                    object : InternetHelper.RequestCallback {
                        override fun onSuccess(responseBody: String) {
                            // 如果刚手动操作过，跳过本次同步
                            if (System.currentTimeMillis() - lastManualActionTime < 3000) return

                            val json = JSONObject(responseBody)
                            val sPause = json.optBoolean("is_music_pause", true)
                            val sTime = json.optInt("current_music_time", 0)
                            val sMusic = json.optString("current_music", "")

                            // 同步曲目
                            if (sMusic.isNotBlank() && sMusic != currentPlayingTrack) {
                                currentPlayingTrack = sMusic
                                val playUrl = InternetHelper().getStreamUrl(
                                    savedHost,
                                    values.roomName,
                                    sMusic
                                )
                                mediaPlayer.reset()
                                mediaPlayer.setDataSource(playUrl)
                                mediaPlayer.prepareAsync()
                                mediaPlayer.setOnPreparedListener {
                                    it.seekTo(sTime * 1000)
                                    if (!sPause) it.start()
                                    globalIsPlaying = !sPause
                                }
                            } else if (sMusic.isNotBlank()) {
                                if (!sPause != globalIsPlaying) {
                                    if (sPause) mediaPlayer.pause() else mediaPlayer.start()
                                    globalIsPlaying = !sPause
                                }
                                val localSec = mediaPlayer.currentPosition / 1000
                                if (sPause) {
                                    // 若服务端已暂停
                                    if (Math.abs(localSec - sTime) > 2) {
                                        mediaPlayer.seekTo(sTime * 1000)
                                    }
                                } else {
                                    // 若服务端在播，只有当本地进度落后超过 3 秒才追赶
                                    // 如果本地进度比服务端快，则跳过覆盖，等待本地主动同步
                                    if (sTime > localSec + 3) {
                                        mediaPlayer.seekTo(sTime * 1000)
                                    }
                                }
                            }
                        }

                        override fun onFailure() {}
                    })
            }
            delay(1000) // 1秒轮询一次
        }
    }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer.release() }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val tabs = listOf(
                    Triple(stringResource(R.string.hostButton), 0, Icons.Default.Home),
                    Triple(stringResource(R.string.chatButton), 1, Icons.AutoMirrored.Filled.Chat),
                    Triple(stringResource(R.string.musicButton), 2, Icons.Default.MusicNote)
                )

                tabs.forEach { (label, index, icon) ->
                    val isTabDisabled = (index == 1 || index == 2) && !values.isInRoom

                    NavigationBarItem(
                        selected = i == index,
                        onClick = {
                            if (!isTabDisabled) {
                                i = index
                            } else {
                                tools.showToast(mContext, "请先进入一个房间")
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isTabDisabled) {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                } else {
                                    if (i == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                color = if (isTabDisabled) {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                } else Color.Unspecified
                            )
                        },
                        enabled = true
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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                //📍
                Text(
                    text = if (i == 0) "👋 Hi, $userName" else {
                        if (values.roomName.isNullOrEmpty()) "null" else "\uD83D\uDCCD ${values.roomName}(${roomNumbers.present}/${roomNumbers.max})"
                    },
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
            }

            key(savedHost) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (i) {
                        0 -> HostList(
                            buttonColors = ButtonDefaults.buttonColors(),
                            tools = tools, values = values, itemList = itemList,
                            host = savedHost, hostNameInput = hostInputText,
                            onExitRoomSuccess = { i = 0 },
                            onHostNameChange = { hostInputText = it },
                            onConnectSuccess = { newHost ->
                                savedHost = newHost; hostInputText = ""
                                setting.edit().putString("history_host", newHost).apply()
                            }
                        )

                        1 -> ChatView(
                            ButtonDefaults.buttonColors(),
                            tools,
                            values,
                            savedHost,
                            setting
                        )

                        2 -> MusicView(
                            savedHost, values.roomName, tools, mediaPlayer,
                            globalIsPlaying, currentPlayingTrack
                        ) { globalIsPlaying = it }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostList(
    buttonColors: ButtonColors,
    tools: Tools,
    values: Values,
    itemList: SnapshotStateList<Values.ListItem>,
    host: String,
    hostNameInput: String,
    onExitRoomSuccess: () -> Unit,
    onHostNameChange: (String) -> Unit,
    onConnectSuccess: (String) -> Unit
) {
    var showPlusRoomDialog by remember { mutableStateOf(false) }
    val mContext = LocalContext.current
    var expanded by remember { mutableStateOf(false) } // 下拉列表显示的状态
    val nplusButtonShape = MaterialTheme.shapes.extraSmall
    val plusButtonShape = MaterialTheme.shapes.small
    var showPasswordDialog by remember { mutableStateOf(false) }
    var pendingRoomItem by remember { mutableStateOf<Values.ListItem?>(null) }

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
                    mContext, host, roomName, password,
                    object : Tools.gacCallback {
                        override fun onSuccess() {
                            // 找到并更新列表项
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
                        .weight(1f) // 占一半高度
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加",
                        tint = MaterialTheme.colorScheme.primary
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
                        .weight(1f) // 占一半高度
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }


            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.weight(1f) // 占据中间剩余空间
            ) {
                TextField(
                    value = hostNameInput,
                    onValueChange = onHostNameChange,
                    label = { Text(stringResource(R.string.host_inputer)) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.4f
                        ),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                // 下拉列表
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(values.historyHost)
                        },
                        onClick = {
                            onHostNameChange(values.historyHost)
                            expanded = false // 点击后关闭菜单
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
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
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxHeight(),
                contentPadding = PaddingValues(horizontal = 16.dp)
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
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
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
                                    object : Tools.gacCallback {
                                        override fun onSuccess() {
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
            shape = RoundedCornerShape(16.dp),
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
                        label = { Text("房间名称") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = MaterialTheme.shapes.small
                    )

                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("房间密码") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = MaterialTheme.shapes.small
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
                    horizontalArrangement = Arrangement.End, // 靠右对齐
                ) {
                    TextButton(onClick = { onDismissRequest() }) { Text("取消") }
                    TextButton(onClick = {
                        onConfirmation(roomName, maxNumberTrue, cancelTimeTrue, password)
                    }) {
                        Text("确认添加")
                    }
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
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {

                // --- 可滑动区域 ---
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
                        label = { Text("房间密码") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = { onDismissRequest() }) { Text("取消") }
                    TextButton(onClick = { onConfirmation(roomName, password) }) {
                        Text("加入")
                    }
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
            .clickable(onClick = onSelectClick),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = listItem.itemHost,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isSelected) "当前所在房间" else "点击加入",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Text(
                text = listItem.itemStatus,
                fontSize = 20.sp,
                modifier = Modifier
                    .background(
                        color = if (listItem.itemStatus == "√")
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun ChatView(
    buttonColors: ButtonColors,
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

    // 轮询刷新消息列表
    LaunchedEffect(values.roomName) {
        while (true) {
            if (host.isNotBlank() && values.roomName.isNotBlank()) {
                InternetHelper().getMessages(
                    url,
                    values.roomName,
                    object : InternetHelper.RequestCallback {
                        override fun onSuccess(responseBody: String) {
                            try {
                                val jsonArray = JSONArray(responseBody)
                                // 只有当数量不一致时更新，避免频繁刷新导致动画异常
                                if (jsonArray.length() != values.messageList.size) {
                                    val newList = mutableListOf<String>()
                                    for (i in 0 until jsonArray.length()) {
                                        newList.add(jsonArray.getString(i))
                                    }
                                    values.messageList.clear()
                                    values.messageList.addAll(newList)
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
            reverseLayout = true, // 底部往上排，新消息在最下面弹出
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(
                items = values.messageList.reversed(),
                key = { msg -> msg.hashCode() + values.messageList.indexOf(msg) }
            ) { msg ->
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
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 0.dp,
                            bottomEnd = if (isMe) 0.dp else 16.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (!isMe) {
                                Text(
                                    text = msg.substringBefore(":"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = displayMsg,
                                color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // 输入
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
                placeholder = { Text("说点什么...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
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
                modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
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
    mediaPlayer: MediaPlayer,
    isPlaying: Boolean,
    currentPlayingTrack: String,
    onPlayingStateChange: (Boolean) -> Unit
) {
    val mcontext = LocalContext.current
    val musicList = remember { mutableStateListOf<String>() }
    var currentPos by remember { mutableFloatStateOf(0f) }
    val duration = if (mediaPlayer.duration > 0) mediaPlayer.duration.toFloat() else 1f

    // 获取列表
    LaunchedEffect(roomName) {
        if (roomName != "null") {
            tools.fetchMusicList(hostName, roomName) { list ->
                musicList.clear()
                musicList.addAll(list)
            }
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPos = mediaPlayer.currentPosition.toFloat()
            delay(1000) // 每1秒更新一次 UI
        }
    }

    // 本地进度快时上传至服务端
    LaunchedEffect(isPlaying, currentPlayingTrack) {
        while (isPlaying) {
            delay(5000) // 每5秒同步一次
            val localTime = mediaPlayer.currentPosition / 1000
            if (localTime > 0 && currentPlayingTrack.isNotBlank()) {
                InternetHelper().updateMusicStatus(
                    hostName, roomName, false, localTime, currentPlayingTrack,
                    object : InternetHelper.RoomRequestCallback {
                        override fun onSuccess() {}
                        override fun onFailure() {}
                    }
                )
            }
        }
    }

    // 文件上传
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            uris.forEach { uri ->
                tools.uploadMusicFile(
                    mcontext,
                    hostName,
                    roomName,
                    uri,
                    object : Tools.gacCallback {
                        override fun onSuccess() {
                            tools.fetchMusicList(hostName, roomName) { list ->
                                musicList.clear(); musicList.addAll(list)
                            }
                        }

                        override fun onFailure() {}
                    })
            }
        }

    Column(modifier = Modifier.fillMaxSize()) {
        // 音乐列表
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(musicList) { fileName ->
                val isThisTrackPlaying = isPlaying && currentPlayingTrack == fileName
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(
                            if (isThisTrackPlaying) MaterialTheme.colorScheme.primaryContainer.copy(
                                alpha = 0.3f
                            )
                            else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fileName,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = {
                        val playUrl = InternetHelper().getStreamUrl(hostName, roomName, fileName)
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(playUrl)
                        mediaPlayer.prepareAsync()
                        mediaPlayer.setOnPreparedListener {
                            it.start()
                            onPlayingStateChange(true)
                            // 切歌瞬间同步给服务端进度0
                            InternetHelper().updateMusicStatus(
                                hostName, roomName, false, 0, fileName,
                                object : InternetHelper.RoomRequestCallback {
                                    override fun onSuccess() {}
                                    override fun onFailure() {}
                                })
                        }
                    }) { Icon(Icons.Default.PlayArrow, null) }
                }
            }
        }

        // 底部控制区
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { launcher.launch(arrayOf("audio/mpeg", "audio/flac", "audio/aac")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("选择并上传音乐")
                }

                Spacer(Modifier.height(12.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = currentPos,
                        onValueChange = {
                            currentPos = it
                        },
                        onValueChangeFinished = {
                            mediaPlayer.seekTo(currentPos.toInt())
                            // 手动拖动后立即同步到服务器
                            InternetHelper().updateMusicStatus(
                                hostName,
                                roomName,
                                !isPlaying,
                                (currentPos / 1000).toInt(),
                                currentPlayingTrack,
                                object : InternetHelper.RoomRequestCallback {
                                    override fun onSuccess() {}
                                    override fun onFailure() {}
                                }
                            )
                        },
                        valueRange = 0f..duration,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = tools.formatTime(currentPos.toInt()),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = tools.formatTime(duration.toInt()),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentPlayingTrack.isNotBlank()) currentPlayingTrack else "未选择曲目",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isPlaying) "正在播放" else "暂停中",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    FilledIconButton(
                        onClick = {
                            if (currentPlayingTrack.isBlank()) return@FilledIconButton

                            val nextPauseState = isPlaying // 如果当前在播，下一步即是暂停(True)
                            if (isPlaying) {
                                mediaPlayer.pause()
                                onPlayingStateChange(false)
                            } else {
                                mediaPlayer.start()
                                onPlayingStateChange(true)
                            }

                            //点击瞬间上交进度与状态
                            InternetHelper().updateMusicStatus(
                                hostName,
                                roomName,
                                nextPauseState,
                                (mediaPlayer.currentPosition / 1000),
                                currentPlayingTrack,
                                object : InternetHelper.RoomRequestCallback {
                                    override fun onSuccess() {}
                                    override fun onFailure() {}
                                }
                            )
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}