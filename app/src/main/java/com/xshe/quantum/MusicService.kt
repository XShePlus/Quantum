// MusicService.kt
package com.xshe.quantum

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat

class MusicService : Service() {
    private val binder = MusicBinder()
    var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "MUSIC_CHANNEL")
            .setContentTitle("Quantum一起听中")
            .setContentText("一起听音乐!\uD83D\uDE18")
            .setSmallIcon(R.drawable.quantum_icon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        // 启动前台服务
        startForeground(1, notification)

        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "MUSIC_CHANNEL", "音乐播放服务",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}