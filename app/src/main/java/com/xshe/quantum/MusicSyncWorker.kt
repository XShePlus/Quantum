package com.xshe.quantum

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * MusicSyncWorker — WorkManager 兜底同步任务
 *
 * 定位：辅助保障，不是主同步通道。
 *
 * 触发场景：
 * - 每 15 分钟由 WorkManager 调度一次
 * - SSE 长连接断线期间（如手机进入 Doze 模式），此任务作为补偿手段
 *   在 Service 存活时拉取一次服务器状态并对齐本地播放器
 *
 * 如果 MusicService 未运行（已被系统杀死），Worker 直接返回 success，
 * 不尝试重启 Service（重启由 START_STICKY 和系统机制负责）。
 */
class MusicSyncWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "music_sync_work"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val prefs = applicationContext.getSharedPreferences("music_service", Context.MODE_PRIVATE)
            val host = prefs.getString("host", "") ?: ""
            val room = prefs.getString("room", "") ?: ""
            val user = prefs.getString("user", "") ?: ""

            if (host.isBlank() || room.isBlank() || room == "null") {
                return@withContext Result.success()
            }

            val service = MusicService.getInstance()
            if (service == null) {
                Log.d("MusicSyncWorker", "MusicService not running, skip sync")
                return@withContext Result.success()
            }

            // 拉取服务器当前状态，对齐本地播放器
            val status = InternetHelper().getMusicStatusSuspend(host, room, user)
            if (status != null) {
                withContext(Dispatchers.Main) {
                    service.applyMusicStatus(status)
                }
            }

            // 上报本地状态到服务器（确保服务器记录是最新的）
            withContext(Dispatchers.Main) {
                service.reportLocalStatus()
            }

            // 如果 SSE 连接已断开（sseCall 为 null 说明没有活跃连接），尝试重连
            // 这样可以在 Doze 结束后立即恢复长连接，而不是等下一次 Worker 触发
            withContext(Dispatchers.Main) {
                service.startSSE()
            }

            Log.d("MusicSyncWorker", "Fallback sync completed")
            Result.success()
        } catch (e: Exception) {
            Log.e("MusicSyncWorker", "Sync failed", e)
            Result.retry()
        }
    }
}