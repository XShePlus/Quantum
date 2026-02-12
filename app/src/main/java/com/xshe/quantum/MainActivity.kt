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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import org.json.JSONArray

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
            if (savedHost.isNotBlank() && values.roomName.isNullOrEmpty() && !values.roomName.equals(
                    "null"
                )
            ) {
                InternetHelper().getMusicStatus(
                    savedHost,
                    values.roomName,
                    object : InternetHelper.RequestCallback {
                        override fun onSuccess(responseBody: String) {
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
            delay(1000) // 3秒轮询一次
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
                    NavigationBarItem(
                        selected = i == index,
                        onClick = { i = index },
                        icon = { Icon(imageVector = icon, contentDescription = label) },
                        label = { Text(label) }
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
                            onHostNameChange = { hostInputText = it },
                            onConnectSuccess = { newHost ->
                                savedHost = newHost; hostInputText = ""
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

@Composable
fun HostList(
    buttonColors: ButtonColors,
    tools: Tools,
    values: Values,
    itemList: SnapshotStateList<Values.ListItem>,
    host: String,
    hostNameInput: String,
    onHostNameChange: (String) -> Unit,
    onConnectSuccess: (String) -> Unit
) {
    var showPlusRoomDialog by remember { mutableStateOf(false) }
    val mContext = LocalContext.current

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
                .padding(12.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showPlusRoomDialog = true },
                colors = buttonColors,
                shape = plusButtonShape,
                modifier = Modifier.padding(5.dp)
            ) {
                Text(stringResource(R.string.plus), fontSize = 22.sp)
            }

            TextField(
                value = hostNameInput,
                onValueChange = onHostNameChange,
                label = { Text(stringResource(R.string.host_inputer)) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Column(
                Modifier.padding(start = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = {
                        if (hostNameInput.isEmpty()) tools.showToast(mContext, "请输入正确URL!")
                        else tools.connectAndCheck(
                            mContext,
                            hostNameInput,
                            object : Tools.gacCallback {
                                override fun onSuccess() {
                                    onConnectSuccess(hostNameInput)
                                }

                                override fun onFailure() {
                                    tools.showToast(mContext, "连接失败")
                                }
                            })
                    },
                    colors = buttonColors,
                    shape = nplusButtonShape,
                    modifier = Modifier
                        .height(32.dp)
                        .padding(0.dp)
                ) { Text("连接", fontSize = 12.sp) }

                Button(
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
                    colors = buttonColors,
                    shape = nplusButtonShape,
                    modifier = Modifier
                        .height(32.dp)
                        .padding(0.dp)
                ) { Text("刷新", fontSize = 12.sp) }
            }
        }

        LazyColumn(Modifier.padding(horizontal = 17.dp, vertical = 8.dp)) {
            items(items = itemList) { item ->
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

@Composable
fun PlusRoomDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (String, Int, Int, String) -> Unit,
) {
    var roomName by remember { mutableStateOf("") }
    var maxNumber by remember { mutableStateOf(2f) }
    var maxNumberTrue = maxNumber.toInt()
    var cancelTime by remember { mutableStateOf(60f) }
    var cancelTimeTrue = cancelTime.toInt()
    var password by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(620.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "添加房间", fontSize = 23.sp, modifier = Modifier.padding(bottom = 32.dp)
                )
                TextField(
                    value = roomName,
                    onValueChange = { newText ->
                        roomName = newText
                    },
                    label = { Text("房间名称") },
                    enabled = true,
                    readOnly = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                TextField(
                    value = password,
                    onValueChange = { newText ->
                        password = newText
                    },
                    label = { Text("房间密码") },
                    enabled = true,
                    readOnly = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Text(
                    text = "最大人数", fontSize = 15.sp, modifier = Modifier.padding(bottom = 3.dp)
                )
                Slider(
                    value = maxNumber,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.primaryFixedDim,
                        inactiveTrackColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier.fillMaxWidth(0.7f),
                    valueRange = 0f..16f,
                    onValueChange = {
                        maxNumber = it
                    },
                )
                Text(
                    text = "取消时间", fontSize = 15.sp, modifier = Modifier.padding(bottom = 3.dp)
                )
                Slider(
                    value = cancelTime,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.primaryFixedDim,
                        inactiveTrackColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier.fillMaxWidth(0.7f),
                    valueRange = 10f..240f,
                    onValueChange = {
                        cancelTime = it
                    },
                )
                Text(
                    "当前选择${maxNumberTrue.toString()}人，持续${cancelTimeTrue.toInt()}分钟",
                    modifier = Modifier.padding(vertical = 5.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("取消")
                    }
                    TextButton(
                        onClick = {
                            onConfirmation(
                                roomName,
                                maxNumberTrue,
                                cancelTimeTrue,
                                password
                            )
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("添加")
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
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "加入房间${roomName}",
                    fontSize = 23.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                TextField(
                    value = password,
                    onValueChange = { newText ->
                        password = newText
                    },
                    label = { Text("房间密码") },
                    enabled = true,
                    readOnly = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("取消")
                    }
                    TextButton(
                        onClick = { onConfirmation(roomName, password) },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("添加")
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
    val animatedElevation by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 0.dp,
        label = "elevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onSelectClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation)
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
                                if (jsonArray.length() != values.messageList.size) {
                                    values.messageList.clear()
                                    for (i in 0 until jsonArray.length()) {
                                        values.messageList.add(jsonArray.getString(i))
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
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(values.messageList.reversed()) { msg ->
                val isMe = msg.startsWith("$userName:")
                val displayMsg = msg.substringAfter(":")
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                        InternetHelper().appendMessage(
                            url, values.roomName, "${userName}:${inputMessage}",
                            object : InternetHelper.RoomRequestCallback {
                                override fun onSuccess() {
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

    // 获取列表
    LaunchedEffect(roomName) {
        if (roomName != "null") {
            tools.fetchMusicList(hostName, roomName) { list ->
                musicList.clear()
                musicList.addAll(list)
            }
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
                    onClick = { launcher.launch(arrayOf("audio/mpeg", "audio/flac")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("选择并上传音乐")
                }

                Spacer(Modifier.height(12.dp))

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