package com.xshe.quantum

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import org.json.JSONObject
import kotlin.math.abs
import android.os.Handler
import org.json.JSONArray
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * MusicService — 前台音乐播放服务
 *
 * ── 推送架构 ──
 * 使用 OkHttp 建立 /api/sse 长连接，由服务端主动推送以下事件：
 *   • music_status  → 音乐状态变化，触发本地 MediaPlayer 同步
 *   • chat_message  → 新聊天消息，后台时弹出通知
 *   • room_update   → 成员进出（预留扩展）
 * 断线后采用指数退避（2s → 4s → … 上限 30s）自动重连。
 * WorkManager 作为兜底：仅在 SSE 断线期间辅助对齐一次状态，不承担主同步职责。
 *
 * ── MediaSession 接入 ──
 * 通过 [mediaSession] 向系统注册播放状态与曲目元数据，使以下功能开箱即用：
 *   • 国产 UI 灵动岛 / 状态栏音乐卡片（MIUI HyperOS、ColorOS 等）
 *   • 蓝牙耳机 / 有线耳机媒体按键控制
 *   • Android 13+ 系统媒体播放器组件
 *   • 车载 CarPlay 媒体列表
 * 通知样式使用 [androidx.media.app.NotificationCompat.MediaStyle] 并绑定 MediaSession Token。
 *
 * ── 线程模型（修复说明）──
 * 原实现使用 playerExecutor（无 Looper 的线程池）执行 setDataSource，
 * 再通过 mainHandler.post 到主线程设置 Listener 并调 prepareAsync，
 * 两次 post 之间存在竞争窗口：用户快速切歌时 SSE / UI 可触发第二次 reset，
 * 与第一次 prepare 并发导致 IllegalStateException 崩溃。
 *
 * 修复方案：改用 CoroutineScope（playerScope）作为 MediaPlayer 专属协程作用域。
 * 所有播放操作通过 playerScope.launch(Dispatchers.IO) 串行提交：
 *   • job.cancel() 可瞬间取消上一个切歌任务
 *   • setDataSource / setOnPreparedListener / prepareAsync 在同一线程顺序执行，无窗口
 *   • applyMusicStatus（SSE）也投递到同一作用域，与用户操作天然互斥
 *
 * ── 封面图标修复说明 ──
 * 原实现在封面异步加载完成后调 updateNotification（只做 notify），
 * MIUI / 澎湃 OS 对同一 notificationId 的 LargeIcon 更新有缓存，不会刷新灵动岛图标。
 * 修复方案：封面就绪后改调 forceUpdateNotification（先 cancel 再 notify），
 * 强制系统丢弃旧缓存，同时同步更新 MediaSession 元数据（灵动岛从 Metadata 读封面）。
 *
 * ── 保活策略 ──
 * 1. startForeground() 将服务提升为前台服务，系统优先级最高
 * 2. PARTIAL_WAKE_LOCK 防止 CPU 在音乐播放期间进入休眠
 * 3. MediaPlayer.setWakeMode() 由系统管理底层音频 WakeLock
 * 4. START_STICKY 确保服务被系统杀死后自动重建并恢复 SSE 连接
 * 5. foregroundServiceType="mediaPlayback" 在 AndroidManifest 中声明（Android 10+ 必须）
 *
 * ── 生命周期 ──
 * onCreate      → 初始化 CoroutineScope、MediaPlayer、MediaSession、通知渠道、WakeLock，恢复 SSE
 * onStartCommand → 处理通知栏按钮 Intent，调用 startForeground 发布前台通知
 * onDestroy     → 停止 CoroutineScope、取消 SSE、释放 MediaPlayer 和 MediaSession，清除持久化配置
 */
class MusicService : Service() {

    // ── 运行标志 ──────────────────────────────────────────────────────────────

    /** 服务是否仍在运行；onDestroy 时置 false，用于终止所有后台循环 */
    @Volatile private var isRunning = true

    // ── 播放器与会话 ──────────────────────────────────────────────────────────

    private val binder = MusicBinder()

    /** 底层音频播放器，准备 → 播放 → 暂停 → 停止 的完整状态机由各播放方法管理 */
    var mediaPlayer: MediaPlayer? = null

    /**
     * MediaPlayer 专属协程作用域。
     * 使用 Dispatchers.IO，所有播放操作在同一个协程里串行执行，
     * 彻底消除 setDataSource 与 prepareAsync 之间的竞争窗口。
     */
    private val playerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * 系统媒体会话，向 Android 框架暴露播放状态与曲目元数据。
     * 初始化于 onCreate，释放于 onDestroy。
     * 国产 ROM 的灵动岛和 Android 媒体控制均通过此会话获取信息。
     */
    private lateinit var mediaSession: MediaSessionCompat

    // ── 房间与播放状态 ────────────────────────────────────────────────────────

    private var hostName: String = ""
    private var roomName: String = ""
    private var userName: String = ""

    /** 当前正在播放（或最后一次播放）的曲目文件名，空字符串表示未播放 */
    var currentPlayingTrack: String = ""

    /** 当前曲目是否属于"示例音乐库"模式（与用户上传的房间音乐相区分） */
    private var isPlayingExample: Boolean = false

    /**
     * 最近一次手动操作（播放/暂停/切歌）的时间戳（毫秒）。
     * 在 [MANUAL_COOLDOWN] 时间窗口内，服务端推送的状态更新会被忽略，
     * 防止本地操作立刻被服务端覆盖，造成操作"弹回"的体验问题。
     */
    private var lastManualActionTime: Long = 0L
    private val MANUAL_COOLDOWN = 3000L

    /** 当前房间的完整播放列表，用于"下一首"逻辑 */
    var playList: List<String> = emptyList()

    /** playList 对应的示例模式标志 */
    var playListIsExample: Boolean = false

    /**
     * 换曲防重入标志：prepareAsync 期间置 true，
     * 阻止 SSE 并发触发新的 applyMusicStatus，避免 MediaPlayer 状态混乱。
     * 注意：此标志只作为快速拒绝的第一道防线，真正的串行保证由 playerScope 协程作用域提供。
     */
    @Volatile var isSwitchingTrack: Boolean = false

    /** updateMediaSession 节流：避免 SSE 频繁推送时 MediaSession 被反复更新导致卡顿 */
    private var lastUpdateMediaSessionTime: Long = 0L
    private val UPDATE_MEDIA_SESSION_THROTTLE = 200L

    /** 曲目播放完毕时的 UI 回调，由 MusicView 注入，用于更新界面播放状态 */
    var onTrackCompletedCallback: ((String) -> Unit)? = null

    // ── 系统资源 ──────────────────────────────────────────────────────────────

    /**
     * CPU 唤醒锁，防止设备屏幕关闭后 CPU 休眠导致音乐中断或 SSE 连接被冻结。
     * acquire 超时设为 12 小时，基本覆盖任何正常使用场景。
     * onDestroy 时必须 release，否则会造成电量泄漏。
     */
    private var wakeLock: PowerManager.WakeLock? = null

    /** 当前曲目的封面 Bitmap，用于通知栏大图和 MediaSession 元数据 */
    private var currentAlbumArt: Bitmap? = null

    // ── 房间入场标志 ──────────────────────────────────────────────────────────

    /**
     * 用户是否已正式进入房间。
     * 仅在此标志为 true 时，SSE 推送的 music_status 才会触发本地播放，
     * 防止 START_STICKY 重建服务后自动播放上一个房间的音乐。
     */
    @Volatile private var hasJoinedRoom: Boolean = false

    // ── SSE 连接状态 ──────────────────────────────────────────────────────────

    /** 当前活跃的 SSE OkHttp Call，onDestroy 或切换房间时调用 cancel() */
    @Volatile private var sseCall: okhttp3.Call? = null

    /** SSE 重连等待时长（毫秒），每次失败后翻倍，上限 30 秒 */
    @Volatile private var sseReconnectDelay = 2000L

    /** 重连调度 Handler，运行于主线程，避免子线程 post 延迟任务的线程安全问题 */
    private val sseHandler = Handler(Looper.getMainLooper())

    /** SSE 是否已被主动停止（onDestroy 时置 true），防止析构后的野重连 */
    @Volatile private var sseStopped = false

    // ── 静态成员 ──────────────────────────────────────────────────────────────

    companion object {
        const val ACTION_NEXT       = "com.xshe.quantum.NEXT"
        const val ACTION_PREVIOUS   = "com.xshe.quantum.PREVIOUS"
        const val ACTION_PLAY_PAUSE = "com.xshe.quantum.PLAY_PAUSE"
        const val ACTION_STOP       = "com.xshe.quantum.STOP"

        /** 服务单例引用，允许 Activity 直接调用服务方法而无需完整绑定流程 */
        @Volatile
        private var instance: MusicService? = null

        @JvmStatic
        fun getInstance(): MusicService? = instance

        /**
         * Activity 可见性标志，由 MainActivity.onStart / onStop 维护。
         *
         * 替代已弃用的 ActivityManager.getRunningAppProcesses() 判断方案：
         * 后者在 Android 9+ 受限，且前台 Service 自身的进程重要性也是
         * IMPORTANCE_FOREGROUND，会导致 Activity 退后台后误判为仍在前台，
         * 从而漏发消息通知。
         *
         * 此标志由主线程安全写入，MusicService 在子线程的 SSE 回调中仅读取，
         * 使用 @Volatile 保证可见性即可，无需加锁。
         */
        @Volatile
        var isActivityVisible: Boolean = false
    }

    // =========================================================================
    // 生命周期
    // =========================================================================

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 从持久化配置恢复房间信息（START_STICKY 重建时使用）
        val prefs = getSharedPreferences("music_service", MODE_PRIVATE)
        hostName = prefs.getString("host", "") ?: ""
        roomName = prefs.getString("room", "") ?: ""
        userName = prefs.getString("user", "") ?: ""

        // 初始化 MediaPlayer，并委托系统管理底层音频 WakeLock
        mediaPlayer = MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        }

        // 提前创建两条通知渠道，后续 startForeground 时必须已存在
        createNotificationChannel()
        createMessageNotificationChannel()

        // 初始化 MediaSession —— 注册媒体按键和播放控制回调
        initMediaSession()

        // CPU WakeLock：防止设备休眠冻结 SSE 线程和 MediaPlayer 缓冲
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "quantum:MusicSyncWakeLock"
        ).apply {
            setReferenceCounted(false)
            acquire(12 * 60 * 60 * 1000L)
        }

        // START_STICKY 重建场景：若有持久化的房间信息，自动恢复 SSE 长连接，
        // 保证后台仍能收到推送和消息通知。注意：此处仅恢复连接，不自动续播音乐，
        // 播放决策由后续 SSE 推送的 applyMusicStatus 触发。
        if (hostName.isNotBlank() && roomName.isNotBlank() && roomName != "null") {
            hasJoinedRoom = true
            sseStopped = false
            // 延迟 500ms，等待 Service 完全初始化再建连，避免初始化竞争
            sseHandler.postDelayed({ startSSE() }, 500L)
            Log.d("MusicService", "SSE restored: room=$roomName host=$hostName")
        }

        // 启动 WorkManager 兜底任务（SSE 断线期间的周期性状态对齐）
        startWorkManagerSync()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 处理通知栏按钮触发的媒体控制 Intent
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> togglePlayPause()
            ACTION_NEXT       -> playNext()
            ACTION_PREVIOUS   -> playPrevious()
            ACTION_STOP       -> {
                stopMusic()
                stopForeground(Service.STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        // 必须在 onStartCommand 内尽快调用 startForeground，
        // Android 8+ 要求 5 秒内完成，否则系统将抛出 ANR
        startForeground(1, createNotification(mediaPlayer?.isPlaying ?: false))

        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false

        // 停止 SSE 并禁止后续重连
        sseStopped = true
        sseHandler.removeCallbacksAndMessages(null)
        sseCall?.cancel()
        sseCall = null

        // 取消 playerScope 中所有待执行任务，防止 onDestroy 后继续操作 MediaPlayer
        playerScope.cancel()

        // 释放 MediaPlayer 资源
        mediaPlayer?.release()
        mediaPlayer = null

        // 停用并释放 MediaSession
        mediaSession.isActive = false
        mediaSession.release()

        instance = null

        // 释放 CPU WakeLock，防止电量泄漏
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }

        // 清除持久化的房间信息：
        // START_STICKY 会让系统重建服务，若保留旧配置会导致用户未入房时自动播放
        getSharedPreferences("music_service", MODE_PRIVATE).edit()
            .remove("host")
            .remove("room")
            .remove("user")
            .apply()

        super.onDestroy()
    }

    // =========================================================================
    // MediaSession 初始化与状态更新
    // =========================================================================

    /**
     * 初始化 MediaSession 并注册媒体控制回调。
     *
     * Callback 中的各方法会在以下场景被系统调用：
     *   • 用户按下蓝牙耳机的播放/暂停/下一首键
     *   • 有线耳机中键（onMediaButtonEvent → onPlay/onPause）
     *   • 系统媒体控制面板（灵动岛、下拉通知栏、锁屏）的按钮
     *   • Android Auto / CarPlay 播放控制
     *
     * isActive = true 必须设置，否则系统不会向此会话分发媒体按键事件。
     */
    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "QuantumMusicSession").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay()          { togglePlayPause() }
                override fun onPause()         { togglePlayPause() }
                override fun onSkipToNext()    { playNext() }
                override fun onSkipToPrevious() { playPrevious() }
                override fun onSeekTo(pos: Long) {
                    // 拖动进度条（灵动岛展开后可见）
                    mediaPlayer?.seekTo(pos.toInt())
                    updateMediaSession(mediaPlayer?.isPlaying ?: false)
                }
            })
            isActive = true
        }
    }

    /**
     * 向系统同步最新的播放状态和曲目元数据。
     *
     * 必须在每次以下事件发生后调用：
     *   • 曲目切换（新曲目标题、时长、封面均已变化）
     *   • 播放 / 暂停状态切换
     *   • 封面加载完成（封面 Bitmap 更新后需刷新元数据）
     *
     * PlaybackStateCompat 中的 position 单位为毫秒，playbackSpeed 播放时为 1.0f，
     * 暂停时为 0.0f，系统据此推算灵动岛进度条的实时刷新。
     *
     * @param isPlaying 当前是否正在播放
     */
    private fun updateMediaSession(isPlaying: Boolean) {
        // 节流：200ms 内的重复调用直接跳过
        val now = System.currentTimeMillis()
        if (now - lastUpdateMediaSessionTime < UPDATE_MEDIA_SESSION_THROTTLE) return
        lastUpdateMediaSessionTime = now

        Log.d("MusicService", "[updateMediaSession] START, isPlaying=$isPlaying, track=$currentPlayingTrack, hasCover=${currentAlbumArt != null}")

        val state = PlaybackStateCompat.Builder()
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                try { (mediaPlayer?.currentPosition ?: 0).toLong() } catch (e: Exception) { 0L },
                if (isPlaying) 1.0f else 0.0f
            )
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SEEK_TO
            )
            .build()
        mediaSession.setPlaybackState(state)

        val metadataBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentPlayingTrack.ifBlank { "Quantum 一起听" })
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, roomName.ifBlank { "未知房间" })
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, try { mediaPlayer?.duration?.toLong() ?: 0L } catch (e: Exception) { 0L })

        // 关键：如果有封面，则放入元数据（灵动岛从 ALBUM_ART 字段读取封面）
        currentAlbumArt?.let {
            Log.d("MusicService", "[updateMediaSession] setting album art, size=${it.byteCount} bytes")
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, it)
        } ?: run {
            Log.w("MusicService", "[updateMediaSession] NO album art available!")
        }

        mediaSession.setMetadata(metadataBuilder.build())
        Log.d("MusicService", "[updateMediaSession] END")
    }

    // =========================================================================
    // SSE 长连接
    // =========================================================================

    /**
     * 建立与服务端的 SSE 长连接。
     *
     * 网络读取在独立线程（SSEReaderThread）中阻塞执行，readTimeout=0 关闭读超时，
     * 由服务端每隔固定时间发送心跳注释行（":ping"）维持连接活跃。
     *
     * 事件格式遵循 W3C Server-Sent Events 规范：
     *   event: <type>\n
     *   data: <json>\n
     *   \n（空行标识事件结束）
     *
     * 连接中断或异常退出后，通过 [scheduleReconnect] 指数退避重试。
     * [sseStopped] 为 true（onDestroy 已调用）时立即返回，阻止残留重连。
     */
    fun startSSE() {
        if (sseStopped || hostName.isBlank() || roomName.isBlank()) return
        sseCall?.cancel()

        // SSE 专用客户端：读超时必须为 0，否则长连接会在无数据时被系统强制断开
        val sseClient = okhttp3.OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .build()

        val encodedRoom = android.net.Uri.encode(roomName)
        val encodedUser = android.net.Uri.encode(userName)
        val baseUrl = if (hostName.startsWith("http")) hostName else "http://$hostName"
        val url     = "$baseUrl/api/sse?room=$encodedRoom&user=$encodedUser"

        val request = okhttp3.Request.Builder().url(url).get().build()
        val call    = sseClient.newCall(request)
        sseCall     = call

        thread(name = "SSEReaderThread") {
            try {
                call.execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("SSE", "Connection failed: ${response.code}")
                        scheduleReconnect()
                        return@use
                    }
                    Log.d("SSE", "Connected: $url")
                    // 连接成功，重置退避延迟至初始值
                    sseReconnectDelay = 2000L

                    val source = response.body?.source() ?: run {
                        scheduleReconnect(); return@use
                    }

                    var eventType  = "message"
                    val dataBuffer = StringBuilder()

                    // 逐行读取 SSE 流，直到连接取消或服务端关闭
                    while (!call.isCanceled() && isRunning) {
                        val line = source.readUtf8Line() ?: break
                        when {
                            line.startsWith("event:") -> {
                                eventType = line.removePrefix("event:").trim()
                            }
                            line.startsWith("data:") -> {
                                dataBuffer.append(line.removePrefix("data:").trim())
                            }
                            line.startsWith(":") -> {
                                // 服务端心跳注释行，忽略，连接本身已续活
                            }
                            line.isEmpty() -> {
                                // 空行标识一个完整 SSE 事件结束，切到主线程分发
                                val data = dataBuffer.toString()
                                if (data.isNotEmpty()) {
                                    val type = eventType
                                    Handler(Looper.getMainLooper()).post {
                                        handleSSEEvent(type, data)
                                    }
                                }
                                dataBuffer.clear()
                                eventType = "message"
                            }
                        }
                    }
                }
                // 流正常结束（服务端主动关闭），主动触发重连
                if (!sseStopped) scheduleReconnect()
            } catch (e: Exception) {
                if (!call.isCanceled() && !sseStopped) {
                    Log.e("SSE", "Error: ${e.message}")
                    scheduleReconnect()
                }
            }
        }
    }

    /**
     * 指数退避重连调度。
     *
     * 每次失败后等待时间翻倍，上限 30 秒，
     * 避免在服务端宕机期间持续频繁重连消耗流量和电量。
     * 重连成功后 [startSSE] 内部会重置 [sseReconnectDelay] 为 2000。
     */
    private fun scheduleReconnect() {
        if (sseStopped) return
        Log.d("SSE", "Reconnecting in ${sseReconnectDelay}ms")
        sseHandler.postDelayed({ startSSE() }, sseReconnectDelay)
        sseReconnectDelay = minOf(sseReconnectDelay * 2, 30_000L)
    }

    /**
     * 分发 SSE 事件到对应的业务处理逻辑（必须在主线程调用）。
     *
     * 事件类型说明：
     *   music_status — 服务端音乐状态，同步本地 MediaPlayer
     *   chat_message — 房间新消息，App 在后台时弹出通知
     *   room_update  — 成员进出变化（预留扩展，暂时仅打印日志）
     *
     * music_status 的两层过滤：
     *   1. hasJoinedRoom=false → 忽略（防止重建后自动播放上一房间音乐）
     *   2. 处于手动操作冷却期 → 忽略（防止本地操作被服务端状态"弹回"）
     *
     * chat_message 的通知过滤：
     *   仅在 [isActivityVisible]=false（App 不在前台）时弹通知，
     *   前台时消息已显示在 ChatView，无需再次打扰用户。
     */
    private fun handleSSEEvent(type: String, data: String) {
        val json = try {
            JSONObject(data)
        } catch (e: Exception) {
            Log.e("SSE", "JSON parse error: $data"); return
        }

        when (type) {
            "music_status" -> {
                if (!hasJoinedRoom) return
                if (System.currentTimeMillis() - lastManualActionTime >= MANUAL_COOLDOWN) {
                    applyMusicStatus(json)
                }
            }
            "chat_message" -> {
                val sender  = json.optString("sender",  "未知用户")
                val content = json.optString("content", "")
                if (content.isNotBlank() && hasJoinedRoom && !isActivityVisible) {
                    showMessageNotification(sender, content)
                }
            }
            "room_update" -> {
                Log.d("SSE", "Room update: $data")
            }
        }
    }

    // =========================================================================
    // 房间状态管理
    // =========================================================================

    /**
     * 更新服务持有的房间连接信息，并在房间或主机变化时重新建立 SSE 连接。
     * 由 UI 层在进入房间后立即调用，同时将信息持久化以便 START_STICKY 恢复。
     */
    fun setRoomInfo(host: String, room: String, user: String) {
        val changed = host != hostName || room != roomName
        hostName = host
        roomName = room
        userName = user

        getSharedPreferences("music_service", MODE_PRIVATE).edit()
            .putString("host", host)
            .putString("room", room)
            .putString("user", user)
            .apply()

        // 仅在连接目标真正变化时重建 SSE，避免无意义的断连
        if (changed && host.isNotBlank() && room.isNotBlank() && room != "null") {
            sseStopped = false
            sseReconnectDelay = 2000L
            startSSE()
        }
    }

    /**
     * 设置用户是否已正式进入房间。
     *
     * joined=true  → 允许 SSE 推送触发播放，若 SSE 未连接则立即建连
     * joined=false → 取消 SSE 连接，后续推送不会触发任何播放行为
     */
    fun setHasJoinedRoom(joined: Boolean) {
        hasJoinedRoom = joined
        if (joined && hostName.isNotBlank() && roomName.isNotBlank() && sseCall == null) {
            sseStopped = false
            startSSE()
        } else if (!joined) {
            sseCall?.cancel()
            sseCall = null
        }
    }

    fun getHostName(): String = hostName
    fun getRoomName(): String = roomName

    // =========================================================================
    // Binder（Activity 绑定接口）
    // =========================================================================

    /** 内部 Binder，Activity 通过 [getService] 直接访问 Service 实例 */
    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    // =========================================================================
    // 播放控制
    // =========================================================================

    /**
     * 播放列表中的下一首曲目。
     * 若当前曲目不在列表中或已是最后一首，则静默返回。
     */
    fun playNext() {
        if (playList.isEmpty() || currentPlayingTrack.isBlank()) return
        val index = playList.indexOf(currentPlayingTrack)
        if (index == -1 || index >= playList.size - 1) return
        playTrack(playList[index + 1], playListIsExample)
    }

    /**
     * 播放列表中的上一首曲目。
     * 若当前曲目不在列表中或已是第一首，则静默返回。
     */
    fun playPrevious() {
        if (playList.isEmpty() || currentPlayingTrack.isBlank()) return
        val index = playList.indexOf(currentPlayingTrack)
        if (index == -1 || index <= 0) return
        playTrack(playList[index - 1], playListIsExample)
    }

    /**
     * 切换当前曲目的播放/暂停状态。
     *
     * 切换后立即上报状态到服务端，保证多人房间内其他成员同步。
     * 同时刷新通知栏按钮图标和 MediaSession 状态（灵动岛实时响应）。
     *
     * @return 切换后的播放状态，true=正在播放，false=已暂停
     */
    fun togglePlayPause(): Boolean {
        return mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                lastManualActionTime = System.currentTimeMillis()
                updateMusicStatusToServer(isPause = true, time = player.currentPosition / 1000)
                updateNotification(false)
                false
            } else {
                player.start()
                lastManualActionTime = System.currentTimeMillis()
                updateMusicStatusToServer(isPause = false, time = player.currentPosition / 1000)
                updateNotification(true)
                true
            }
        } ?: false
    }

    /** 停止播放并重置 MediaPlayer 状态（不释放，保留实例以便复用） */
    fun stopMusic() {
        // 使用 playerScope.launch 保证与其他播放操作串行，不打断正在进行的 prepare
        playerScope.launch(Dispatchers.IO) {
            mediaPlayer?.let { player ->
                try {
                    if (player.isPlaying) player.stop()
                    player.reset()
                } catch (e: Exception) {
                    Log.w("MusicService", "stopMusic exception: ${e.message}")
                }
            }
            // 通知更新在主线程
            withContext(Dispatchers.Main) {
                isSwitchingTrack = false
                updateNotification(false)
            }
        }
    }

    /**
     * 播放列表中指定曲目（服务端 SSE / 自动播放版，无 UI 回调）。
     *
     * 所有 MediaPlayer 操作均在 playerScope（CoroutineScope）上串行执行：
     *   1. 取消上一个还没执行的协程任务（用户快速连续切歌时只保留最新一次）
     *   2. setDataSource / setOnPreparedListener / prepareAsync 在同一线程顺序完成，无竞争窗口
     *   3. applyMusicStatus 也投递到同一作用域，与用户手动切歌天然互斥
     */
    fun playTrack(fileName: String, isExample: Boolean) {
        lastManualActionTime = System.currentTimeMillis()
        currentPlayingTrack = fileName
        isPlayingExample = isExample
        isSwitchingTrack = true

        // 先尝试从缓存拿封面，立刻更新通知（如有缓存可直接显示）
        currentAlbumArt = getCachedAlbumArt(fileName, isExample)
        // 无论是否有缓存，都异步拉取最新封面；加载完成后调 forceUpdateNotification
        loadAlbumArtAsync(fileName, isExample)

        val playUrl = if (isExample) {
            InternetHelper().getExampleStreamUrl(hostName, fileName)
        } else {
            InternetHelper().getStreamUrl(hostName, roomName, fileName)
        }

        // 取消上一个还没执行的协程任务（用户快速连续切歌时只保留最新一次）
        playerScope.coroutineContext[Job]?.let { job ->
            job.cancel()
        }
        playerScope.launch(Dispatchers.IO) {
            val player = mediaPlayer ?: run {
                withContext(Dispatchers.Main) { isSwitchingTrack = false }
                return@launch
            }
            try {
                if (player.isPlaying) player.stop()
                player.reset()
                player.setDataSource(playUrl)
                // setDataSource 和 prepareAsync 在同一线程顺序执行，无竞争窗口
                player.setOnPreparedListener { mp ->
                    isSwitchingTrack = false
                    mp.start()
                    CoroutineScope(Dispatchers.Main).launch {
                        updateNotification(true)
                        updateMusicStatusToServer(isPause = false, time = 0, musicName = fileName, isExample = isExample)
                    }
                }
                player.setOnErrorListener { _, what, extra ->
                    Log.e("MusicService", "playTrack (no callback) error: what=$what extra=$extra")
                    isSwitchingTrack = false
                    CoroutineScope(Dispatchers.Main).launch { updateNotification(false) }
                    true
                }
                player.setOnCompletionListener {
                    Log.d("MusicService", "Completed: $currentPlayingTrack")
                    CoroutineScope(Dispatchers.Main).launch {
                        updateNotification(false)
                        updateMusicStatusToServer(isPause = true, time = 0, musicName = currentPlayingTrack)
                        onTrackCompletedCallback?.invoke(currentPlayingTrack)
                    }
                }
                player.prepareAsync()
            } catch (e: Exception) {
                Log.e("MusicService", "playTrack (no callback) exception: ${e.message}")
                isSwitchingTrack = false
                CoroutineScope(Dispatchers.Main).launch { updateNotification(false) }
            }
        }
    }

    /**
     * 播放指定曲目（用户交互版，带 UI 回调）。
     *
     * 所有 MediaPlayer 操作均在 playerScope（CoroutineScope）上串行执行：
     *   1. 取消上一个还没执行的协程任务（用户快速连续切歌时只保留最新一次）
     *   2. setDataSource / setOnPreparedListener / prepareAsync 在同一线程顺序完成，无竞争窗口
     *
     * 封面处理：
     *   先从缓存快速获取并更新通知（零延迟），再异步拉取最新封面；
     *   封面就绪后调 forceUpdateNotification 强制刷新灵动岛大图（cancel + notify）。
     */
    fun playTrack(fileName: String, isExample: Boolean, callback: (Boolean) -> Unit) {
        val t0 = System.currentTimeMillis()
        Log.d("MusicService", "[playTrack] START: $fileName, isExample=$isExample")

        lastManualActionTime = System.currentTimeMillis()
        currentPlayingTrack = fileName
        isPlayingExample = isExample
        isSwitchingTrack = true

        // 先从缓存快速获取封面，立即更新通知避免闪烁应用图标
        val t1 = System.currentTimeMillis()
        currentAlbumArt = getCachedAlbumArt(fileName, isExample)
        val t2 = System.currentTimeMillis()
        Log.d("MusicService", "[playTrack] getCachedAlbumArt took ${t2 - t1}ms, hasCover=${currentAlbumArt != null}")

        // 无论是否有缓存，都异步拉取最新封面；完成后 forceUpdateNotification 刷新灵动岛
        Log.d("MusicService", "[playTrack] calling loadAlbumArtAsync")
        loadAlbumArtAsync(fileName, isExample)

        val playUrl = if (isExample) {
            InternetHelper().getExampleStreamUrl(hostName, fileName)
        } else {
            InternetHelper().getStreamUrl(hostName, roomName, fileName)
        }
        Log.d("MusicService", "[playTrack] playUrl=$playUrl")

        // 10 秒超时保护：使用 withTimeoutOrNull 替代 Handler.postDelayed
        val timeoutJob = playerScope.launch(Dispatchers.IO) {
            delay(10_000L)
            if (currentPlayingTrack == fileName && isSwitchingTrack) {
                Log.e("MusicService", "[playTrack] Prepare timeout: $fileName")
                isSwitchingTrack = false
                withContext(Dispatchers.Main) { callback(false) }
            }
        }

        // 取消上一个还没执行的协程任务（用户快速连续切歌时只保留最新一次）
        Log.d("MusicService", "[playTrack] launching in playerScope")
        val t3 = System.currentTimeMillis()
        playerScope.launch(Dispatchers.IO) {
            val t4 = System.currentTimeMillis()
            Log.d("MusicService", "[playTrack] playerScope exec delay: ${t4 - t3}ms")

            val player = mediaPlayer
            if (player == null) {
                Log.e("MusicService", "[playTrack] mediaPlayer is null")
                timeoutJob.cancel()
                withContext(Dispatchers.Main) { isSwitchingTrack = false; callback(false) }
                return@launch
            }
            try {
                Log.d("MusicService", "[playTrack] before stop/reset")
                val t5 = System.currentTimeMillis()
                if (player.isPlaying) player.stop()
                val t6 = System.currentTimeMillis()
                Log.d("MusicService", "[playTrack] stop took ${t6 - t5}ms")

                player.reset()
                val t7 = System.currentTimeMillis()
                Log.d("MusicService", "[playTrack] reset took ${t7 - t6}ms")

                Log.d("MusicService", "[playTrack] calling setDataSource")
                val t8 = System.currentTimeMillis()
                player.setDataSource(playUrl)
                val t9 = System.currentTimeMillis()
                Log.d("MusicService", "[playTrack] setDataSource took ${t9 - t8}ms")

                // setDataSource 和 prepareAsync 在同一线程顺序执行，无竞争窗口
                player.setOnPreparedListener { mp ->
                    val t10 = System.currentTimeMillis()
                    Log.d("MusicService", "[playTrack] OnPreparedListener called")
                    Log.d("MusicService", "[playTrack] total prepare time: ${t10 - t8}ms (setDataSource to prepared)")
                    Log.d("MusicService", "[playTrack] player duration: ${mp.duration}ms, isPlaying: ${mp.isPlaying}")

                    timeoutJob.cancel()
                    isSwitchingTrack = false

                    Log.d("MusicService", "[playTrack] calling mp.start()")
                    val t11 = System.currentTimeMillis()
                    mp.start()
                    val t12 = System.currentTimeMillis()
                    Log.d("MusicService", "[playTrack] mp.start() took ${t12 - t11}ms")

                    CoroutineScope(Dispatchers.Main).launch {
                        Log.d("MusicService", "[playTrack] calling forceUpdateNotification")
                        forceUpdateNotification(true)
                        updateMusicStatusToServer(isPause = false, time = 0, musicName = fileName, isExample = isExample)
                        Log.d("MusicService", "[playTrack] COMPLETE, total time: ${System.currentTimeMillis() - t0}ms")
                        callback(true)
                    }
                }
                player.setOnErrorListener { _, what, extra ->
                    timeoutJob.cancel()
                    Log.e("MusicService", "[playTrack] MediaPlayer error: what=$what extra=$extra")
                    isSwitchingTrack = false
                    CoroutineScope(Dispatchers.Main).launch { updateNotification(false); callback(false) }
                    true
                }
                player.setOnCompletionListener {
                    Log.d("MusicService", "[playTrack] Completed: $currentPlayingTrack")
                    CoroutineScope(Dispatchers.Main).launch {
                        updateNotification(false)
                        updateMusicStatusToServer(isPause = true, time = 0, musicName = currentPlayingTrack)
                        onTrackCompletedCallback?.invoke(currentPlayingTrack)
                    }
                }
                Log.d("MusicService", "[playTrack] calling prepareAsync")
                val t13 = System.currentTimeMillis()
                player.prepareAsync()
                val t14 = System.currentTimeMillis()
                Log.d("MusicService", "[playTrack] prepareAsync took ${t14 - t13}ms")
            } catch (e: Exception) {
                timeoutJob.cancel()
                Log.e("MusicService", "[playTrack] exception: ${e.message}", e)
                isSwitchingTrack = false
                CoroutineScope(Dispatchers.Main).launch { callback(false) }
            }
        }
    }

    // =========================================================================
    // 服务端状态同步
    // =========================================================================

    /** 最近一次 SSE applyMusicStatus 的时间戳，用于节流 */
    @Volatile private var lastApplyMusicStatusTime = 0L
    /** SSE 状态同步的最小间隔（毫秒），防止频繁操作导致 MediaPlayer 状态混乱 */
    private val APPLY_MUSIC_THROTTLE = 2000L

    /**
     * 将服务端下发的音乐状态应用到本地 MediaPlayer。
     *
     * 由 SSE 事件处理器（主线程）或 WorkManager 兜底任务（切主线程后）调用。
     * 执行前已通过 [handleSSEEvent] 完成 hasJoinedRoom 和 MANUAL_COOLDOWN 过滤。
     *
     * 所有 MediaPlayer 操作投递到 playerScope 协程作用域，与用户手动切歌操作串行互斥。
     */
    fun applyMusicStatus(json: JSONObject) {
        val sPause       = json.optBoolean("is_music_pause",     true)
        val sTime        = json.optInt("current_music_time",     0)
        val sMusic       = json.optString("current_music",       "")
        val sExampleMode = json.optBoolean("is_playing_example", false)

        Values.isInExample = sExampleMode

        if (sMusic.isBlank()) return
        if (isSwitchingTrack) return
        if (!hasJoinedRoom) return
        if (System.currentTimeMillis() - lastManualActionTime < MANUAL_COOLDOWN) return

        // 节流：防止 SSE 频繁推送导致 MediaPlayer 状态混乱
        val now = System.currentTimeMillis()
        if (now - lastApplyMusicStatusTime < APPLY_MUSIC_THROTTLE) return
        lastApplyMusicStatusTime = now

        playerScope.launch(Dispatchers.IO) {
            val player = mediaPlayer ?: return@launch

            // 曲目发生变化
            if (sMusic != currentPlayingTrack || sExampleMode != isPlayingExample) {
                Log.d("MusicService", "Track change (SSE): $sMusic")

                // 二次检查：防止在等待执行期间用户手动操作
                if (System.currentTimeMillis() - lastManualActionTime < MANUAL_COOLDOWN) {
                    Log.w("MusicService", "Manual action detected during SSE track change, ignoring")
                    return@launch
                }

                currentAlbumArt = null
                currentPlayingTrack = sMusic
                isPlayingExample    = sExampleMode
                isSwitchingTrack    = true

                // 异步加载封面（在后台线程拉取，完成后 forceUpdateNotification）
                loadAlbumArtAsync(sMusic, sExampleMode)

                val playUrl = if (sExampleMode) {
                    InternetHelper().getExampleStreamUrl(hostName, sMusic)
                } else {
                    InternetHelper().getStreamUrl(hostName, roomName, sMusic)
                }

                try {
                    player.reset()
                    player.setDataSource(playUrl)
                    player.setOnPreparedListener { mp ->
                        isSwitchingTrack = false
                        mp.seekTo(sTime * 1000)
                        if (!sPause) mp.start()
                        CoroutineScope(Dispatchers.Main).launch {
                            forceUpdateNotification(!sPause)
                        }
                    }
                    player.setOnErrorListener { _, what, extra ->
                        Log.e("MusicService", "applyMusicStatus error: what=$what extra=$extra")
                        isSwitchingTrack = false
                        CoroutineScope(Dispatchers.Main).launch { updateNotification(false) }
                        true
                    }
                    player.setOnCompletionListener {
                        Log.d("MusicService", "Completed: $currentPlayingTrack")
                        CoroutineScope(Dispatchers.Main).launch {
                            updateNotification(false)
                            updateMusicStatusToServer(isPause = true, time = 0, musicName = currentPlayingTrack)
                            onTrackCompletedCallback?.invoke(currentPlayingTrack)
                        }
                    }
                    player.prepareAsync()
                } catch (e: Exception) {
                    Log.e("MusicService", "applyMusicStatus setDataSource failed: ${e.message}")
                    isSwitchingTrack = false
                    CoroutineScope(Dispatchers.Main).launch { updateNotification(false) }
                }
            } else {
                // 同一曲目：仅调整播放状态和进度
                val durationMs  = try { player.duration } catch (e: Exception) { 0 }
                val positionMs  = try { player.currentPosition } catch (e: Exception) { 0 }
                val isCompleted = !player.isPlaying && durationMs > 0 && positionMs >= durationMs - 500
                if (isCompleted) return@launch

                withContext(Dispatchers.Main) {
                    if (!sPause != player.isPlaying) {
                        if (sPause) player.pause() else player.start()
                        updateNotification(!sPause)
                    }
                    val localSec = try { player.currentPosition / 1000 } catch (e: Exception) { 0 }
                    if (sPause) {
                        if (abs(localSec - sTime) > 2) player.seekTo(sTime * 1000)
                    } else {
                        if (sTime > localSec + 3) player.seekTo(sTime * 1000)
                    }
                }
            }
        }
    }

    // =========================================================================
    // 上报本地状态到服务器
    // =========================================================================

    /**
     * 将指定播放状态上报给服务端，触发全房间同步。
     *
     * 切换到主线程执行，避免在 prepareAsync 回调的子线程中直接发起网络请求
     * 导致的 NetworkOnMainThreadException（OkHttp 的 enqueue 已自动切后台，
     * 但此方法作为防御性保护仍显式 post 到主线程）。
     */
    fun updateMusicStatusToServer(
        isPause: Boolean,
        time: Int,
        musicName: String  = currentPlayingTrack,
        isExample: Boolean = isPlayingExample,
        updateTime: Long   = System.currentTimeMillis()
    ) {
        if (hostName.isBlank() || roomName.isBlank()) return
        Handler(Looper.getMainLooper()).post {
            val player = mediaPlayer
            if (player != null && currentPlayingTrack.isNotBlank()) {
                InternetHelper().updateMusicStatus(
                    hostName, roomName, userName,
                    isPause, time, musicName, isExample, updateTime,
                    object : InternetHelper.RoomRequestCallback {
                        override fun onSuccess() {}
                        override fun onFailure() {
                            Log.e("MusicService", "Update status failed")
                        }
                    }
                )
            }
        }
    }

    /**
     * 上报本地当前播放状态（用于 WorkManager 兜底对齐）。
     *
     * 若处于手动操作冷却期内则跳过，防止与用户刚完成的操作冲突。
     */
    fun reportLocalStatus() {
        if (System.currentTimeMillis() - lastManualActionTime < MANUAL_COOLDOWN) return
        val player = mediaPlayer ?: return
        if (currentPlayingTrack.isNotBlank()) {
            updateMusicStatusToServer(
                isPause    = !player.isPlaying,
                time       = player.currentPosition / 1000,
                musicName  = currentPlayingTrack,
                isExample  = isPlayingExample,
                updateTime = System.currentTimeMillis()
            )
        }
    }

    // =========================================================================
    // WorkManager 兜底
    // =========================================================================

    /**
     * 注册周期性 WorkManager 任务作为 SSE 断线时的状态兜底。
     *
     * 周期最短为 15 分钟（WorkManager 平台限制），仅在 SSE 失联超过 15 分钟后
     * 才会产生实质补偿作用。正常情况下 SSE 在 30s 内完成重连，此任务不会成为主同步路径。
     * ExistingPeriodicWorkPolicy.KEEP 保证重复启动时不重置计划时间。
     */
    private fun startWorkManagerSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = PeriodicWorkRequestBuilder<MusicSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            MusicSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    // =========================================================================
    // 封面加载
    // =========================================================================

    /**
     * 异步加载曲目封面图片。
     *
     * 加载完成后调 [forceUpdateNotification]（先 cancel 再 notify）而非普通 notify，
     * 强制 MIUI / 澎湃 OS 等国产 ROM 丢弃旧通知的 LargeIcon 缓存，刷新灵动岛大图。
     * 同时更新 MediaSession 元数据，灵动岛从 METADATA_KEY_ALBUM_ART 字段读取封面。
     */
    private fun loadAlbumArtAsync(trackName: String, isExample: Boolean) {
        val t0 = System.currentTimeMillis()
        Log.d("MusicService", "[loadAlbumArtAsync] START: $trackName, isExample=$isExample")

        val coverUrl = if (isExample) {
            InternetHelper().getExampleCoverUrl(hostName, trackName)
        } else {
            InternetHelper().getRoomCoverUrl(hostName, roomName, trackName)
        }
        Log.d("MusicService", "[loadAlbumArtAsync] coverUrl=$coverUrl")

        // 如果内存缓存已有，直接更新封面并强制刷新通知
        val t1 = System.currentTimeMillis()
        Tools.ImageCache.get(coverUrl)?.let { cached ->
            Log.d("MusicService", "[loadAlbumArtAsync] cache HIT, took ${t1 - t0}ms")
            if (currentPlayingTrack == trackName) {
                currentAlbumArt = cached
                // 已有缓存也要 forceUpdate，确保灵动岛刷新
                Handler(Looper.getMainLooper()).post {
                    Log.d("MusicService", "[loadAlbumArtAsync] calling forceUpdateNotification (cache HIT)")
                    forceUpdateNotification(mediaPlayer?.isPlaying ?: false)
                }
            }
            return
        }
        Log.d("MusicService", "[loadAlbumArtAsync] cache MISS, loading from disk/network")

        thread(name = "AlbumArtLoader") {
            try {
                // 先检查磁盘缓存
                val t2 = System.currentTimeMillis()
                val cacheFile = File(cacheDir, "covers/${coverUrl.md5()}.jpg")
                Log.d("MusicService", "[loadAlbumArtAsync] checking disk cache: ${cacheFile.absolutePath}")

                val bitmap: Bitmap? = if (cacheFile.exists()) {
                    Log.d("MusicService", "[loadAlbumArtAsync] disk cache HIT")
                    BitmapFactory.decodeFile(cacheFile.absolutePath)
                } else {
                    Log.d("MusicService", "[loadAlbumArtAsync] disk cache MISS, fetching from network")
                    val t3 = System.currentTimeMillis()
                    // 网络加载
                    val client = okhttp3.OkHttpClient.Builder()
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .build()
                    val response = client.newCall(
                        okhttp3.Request.Builder().url(coverUrl).build()
                    ).execute()

                    val t4 = System.currentTimeMillis()
                    Log.d("MusicService", "[loadAlbumArtAsync] network request took ${t4 - t3}ms")

                    if (response.isSuccessful) {
                        val bytes = response.body?.bytes() ?: null
                        if (bytes != null) {
                            Log.d("MusicService", "[loadAlbumArtAsync] got ${bytes.size} bytes from network")
                            Log.d("MusicService", "[loadAlbumArtAsync] first 20 bytes: ${bytes.take(20).joinToString(" ") { String.format("%02x", it) }}")
                            val decodedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            if (decodedBitmap != null) {
                                Log.d("MusicService", "[loadAlbumArtAsync] bitmap decoded: ${decodedBitmap.width}x${decodedBitmap.height}")
                                // 写入内存缓存
                                Tools.ImageCache.put(coverUrl, decodedBitmap)
                                // 写入磁盘缓存（已在后台线程，直接写）
                                try {
                                    val dir = File(cacheDir, "covers").also { it.mkdirs() }
                                    FileOutputStream(File(dir, "${coverUrl.md5()}.jpg")).use {
                                        decodedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
                                    }
                                } catch (e: Exception) {
                                    Log.w("MusicService", "[loadAlbumArtAsync] Disk cache write failed: ${e.message}")
                                }
                                decodedBitmap
                            } else {
                                Log.w("MusicService", "[loadAlbumArtAsync] failed to decode bitmap, bytes=${bytes.size}")
                                null
                            }
                        } else {
                            Log.w("MusicService", "[loadAlbumArtAsync] response body is null")
                            null
                        }
                    } else {
                        Log.w("MusicService", "[loadAlbumArtAsync] network response failed: ${response.code}")
                        null
                    }
                }

                val t5 = System.currentTimeMillis()
                if (bitmap != null) {
                    Log.d("MusicService", "[loadAlbumArtAsync] bitmap decoded, total load time: ${t5 - t2}ms")
                    Tools.ImageCache.put(coverUrl, bitmap)
                    // 切回主线程更新封面并强制刷新通知
                    // forceUpdateNotification = cancel(1) + notify(1)，强制系统刷新 LargeIcon 缓存
                    Handler(Looper.getMainLooper()).post {
                        val t6 = System.currentTimeMillis()
                        Log.d("MusicService", "[loadAlbumArtAsync] on main thread, updating cover")
                        if (currentPlayingTrack == trackName) {
                            currentAlbumArt = bitmap
                            Log.d("MusicService", "[loadAlbumArtAsync] calling forceUpdateNotification")
                            forceUpdateNotification(mediaPlayer?.isPlaying ?: false)
                            Log.d("MusicService", "[loadAlbumArtAsync] COMPLETE, total time: ${System.currentTimeMillis() - t0}ms")
                        } else {
                            Log.w("MusicService", "[loadAlbumArtAsync] track changed, skipping update")
                        }
                    }
                } else {
                    Log.w("MusicService", "[loadAlbumArtAsync] failed to decode bitmap")
                }
            } catch (e: Exception) {
                Log.e("MusicService", "[loadAlbumArtAsync] exception: ${e.message}", e)
            }
        }
    }

    /**
     * 强制更新通知：先取消旧通知再重新发布，确保 LargeIcon 被系统刷新。
     *
     * 普通 notify() 对同一 notificationId 只做更新，部分 ROM（MIUI、澎湃 OS）
     * 会缓存旧通知的 LargeIcon，导致灵动岛封面不更新。
     * cancel + notify 的组合强制系统视为全新通知，丢弃图标缓存。
     *
     * 注意：同时调用 updateMediaSession 更新 METADATA_KEY_ALBUM_ART，
     * 灵动岛也从 MediaSession 元数据读取封面，两者同步确保全面覆盖。
     */
    private fun forceUpdateNotification(isPlaying: Boolean) {
        Log.d("MusicService", "[forceUpdateNotification] START, isPlaying=$isPlaying, hasCover=${currentAlbumArt != null}")
        val notificationManager = getSystemService(NotificationManager::class.java)
        Log.d("MusicService", "[forceUpdateNotification] canceling notification")
        notificationManager?.cancel(1)          // 取消旧通知，让系统丢弃 LargeIcon 缓存
        Log.d("MusicService", "[forceUpdateNotification] calling updateMediaSession")
        updateMediaSession(isPlaying)            // 同步更新 MediaSession 封面元数据
        Log.d("MusicService", "[forceUpdateNotification] calling notify")
        notificationManager?.notify(1, createNotification(isPlaying))
        Log.d("MusicService", "[forceUpdateNotification] END")
    }

    /**
     * 尝试从内存缓存或磁盘缓存同步获取封面 Bitmap。
     * @return Bitmap 或 null
     */
    private fun getCachedAlbumArt(trackName: String, isExample: Boolean): Bitmap? {
        val t0 = System.currentTimeMillis()
        val coverUrl = if (isExample) {
            InternetHelper().getExampleCoverUrl(hostName, trackName)
        } else {
            InternetHelper().getRoomCoverUrl(hostName, roomName, trackName)
        }
        Log.d("MusicService", "[getCachedAlbumArt] track=$trackName, coverUrl=$coverUrl")

        // 内存缓存
        val t1 = System.currentTimeMillis()
        val memCache = Tools.ImageCache.get(coverUrl)
        val t2 = System.currentTimeMillis()
        if (memCache != null) {
            Log.d("MusicService", "[getCachedAlbumArt] memory cache HIT in ${t2 - t1}ms")
            return memCache
        }
        Log.d("MusicService", "[getCachedAlbumArt] memory cache MISS in ${t2 - t1}ms")

        // 磁盘缓存（使用 URL 的 MD5 作为文件名，确保唯一）
        val t3 = System.currentTimeMillis()
        val cacheFile = File(cacheDir, "covers/${coverUrl.md5()}.jpg")
        val t4 = System.currentTimeMillis()
        Log.d("MusicService", "[getCachedAlbumArt] cacheFile=${cacheFile.absolutePath}, exists=${cacheFile.exists()}, check took ${t4 - t3}ms")

        return if (cacheFile.exists()) {
            val t5 = System.currentTimeMillis()
            val bitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)?.also {
                Tools.ImageCache.put(coverUrl, it)
            }
            val t6 = System.currentTimeMillis()
            if (bitmap != null) {
                Log.d("MusicService", "[getCachedAlbumArt] disk cache HIT, decode took ${t6 - t5}ms, total ${t6 - t0}ms")
            } else {
                Log.w("MusicService", "[getCachedAlbumArt] disk cache exists but decode failed")
            }
            bitmap
        } else {
            Log.d("MusicService", "[getCachedAlbumArt] disk cache MISS, total ${t4 - t0}ms")
            null
        }
    }

    // =========================================================================
    // 通知构建
    // =========================================================================

    /**
     * 刷新前台服务通知，同步更新通知栏媒体控件和 MediaSession 状态。
     *
     * 必须在每次播放状态或曲目变化后调用，使通知栏按钮图标、
     * 灵动岛进度条和系统媒体控制面板保持与实际播放状态一致。
     *
     * @param isPlaying 当前是否正在播放
     */
    internal fun updateNotification(isPlaying: Boolean) {
        updateMediaSession(isPlaying)
        val nm = getSystemService(NotificationManager::class.java)
        nm?.notify(1, createNotification(isPlaying))
    }

    /**
     * 构建前台服务通知。
     *
     * 使用 [androidx.media.app.NotificationCompat.MediaStyle] 并绑定 MediaSession Token，
     * 系统凭此识别该通知为"媒体播放通知"，从而：
     *   • MIUI HyperOS / 澎湃 OS → 接入灵动岛音乐卡片
     *   • ColorOS / OriginOS     → 接入状态栏歌词和控制栏
     *   • Android 13+            → 出现在"媒体播放"快捷设置区域
     *   • 锁屏界面               → 显示媒体控制（VISIBILITY_PUBLIC）
     *
     * setShowActionsInCompactView(0, 1, 2) 指定折叠状态下显示前 3 个 Action（上一首、播放/暂停、下一首）。
     *
     * @param isPlaying 当前是否正在播放（决定中间按钮图标）
     */
    private fun createNotification(isPlaying: Boolean): Notification {
        val mainIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val playPauseIntent = PendingIntent.getService(
            this, 1,
            Intent(this, MusicService::class.java).setAction(ACTION_PLAY_PAUSE),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val nextIntent = PendingIntent.getService(
            this, 2,
            Intent(this, MusicService::class.java).setAction(ACTION_NEXT),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val previousIntent = PendingIntent.getService(
            this, 3,
            Intent(this, MusicService::class.java).setAction(ACTION_PREVIOUS),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, "MUSIC_CHANNEL")
            .setContentTitle(currentPlayingTrack.ifBlank { "Quantum 一起听" })
            .setContentText(if (isPlaying) "正在播放" else "已暂停")
            .setSmallIcon(R.drawable.quantum_icon)
            .setContentIntent(mainIntent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)  // 锁屏下可见
            .setOnlyAlertOnce(true)                               // 状态更新时不重复提示音
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_media_previous,
                "上一首",
                previousIntent
            )
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause
                else           android.R.drawable.ic_media_play,
                if (isPlaying) "暂停" else "播放",
                playPauseIntent
            )
            .addAction(android.R.drawable.ic_media_next, "下一首", nextIntent)
            // ── 关键：MediaStyle 绑定 MediaSession Token ──
            // 系统凭此 Token 将通知与 MediaSession 关联，灵动岛才能识别并接管控制
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)  // 折叠时显示全部 3 个按钮
                    .setShowCancelButton(false)
            )

        // 优先使用专辑封面作为大图标，没有封面时回退到默认音乐图标
        // 注意：如果服务端封面 API 持续 404，这里会显示默认图标
        val largeIcon = currentAlbumArt
            ?: BitmapFactory.decodeResource(resources, android.R.drawable.ic_media_play)
        builder.setLargeIcon(largeIcon)

        return builder.build()
    }

    /**
     * 创建音乐播放服务的通知渠道（IMPORTANCE_LOW）。
     *
     * LOW 级别：无提示音、无震动、但在通知栏持续可见。
     * 适合持续性的媒体控制通知，不打扰用户。
     * Android 8.0+ 必须预先创建渠道，否则通知无法显示。
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "MUSIC_CHANNEL", "音乐播放服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示当前播放曲目和媒体控制按钮"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    /**
     * 创建房间消息的通知渠道（IMPORTANCE_HIGH）。
     *
     * HIGH 级别：弹出横幅、发出提示音、震动。
     * 适合实时聊天消息通知，确保用户不错过重要消息。
     */
    private fun createMessageNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "MESSAGE_CHANNEL", "房间消息通知",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "显示房间内的聊天消息通知"
                setShowBadge(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 100, 200)
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    // =========================================================================
    // 消息通知
    // =========================================================================

    /**
     * 弹出房间聊天消息通知（仅在 App 不在前台时调用）。
     *
     * 使用独立的 notifId（时间戳取模）确保每条消息都是独立通知，
     * 不会因 requestCode 相同而被 FLAG_UPDATE_CURRENT 合并或覆盖。
     *
     * 注意：音乐通知（ID=1）和消息通知（动态 ID）使用不同的 ID 空间，
     * 两者不会互相干扰。
     *
     * @param sender  消息发送者昵称
     * @param message 消息内容
     */
    private fun showMessageNotification(sender: String, message: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        // 使用 notifId 作为 requestCode，保证每条消息的 PendingIntent 相互独立，
        // 避免与 requestCode=0 的音乐通知 PendingIntent 发生 FLAG_UPDATE_CURRENT 覆盖
        val notifId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val pendingIntent = PendingIntent.getActivity(
            this, notifId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, "MESSAGE_CHANNEL")
            .setSmallIcon(R.drawable.quantum_icon)
            .setContentTitle(sender)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)  // 每条消息都触发提示音和震动
            .build()

        getSystemService(NotificationManager::class.java)?.notify(notifId, notification)
    }
}