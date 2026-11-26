package com.jeonyu.aoimsg

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat

class AlarmService : Service() {

    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    companion object {
        const val CH_ID = "aoi_alarm_v1"
        const val NOTIF_ID = 7001
        const val ACTION_STOP = "com.jeonyu.aoimsg.STOP_ALARM"
        @Volatile var isRinging = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopEverything()
            return START_NOT_STICKY
        }
        val line = intent?.getStringExtra("line") ?: "AOI 쪽지"
        val text = intent?.getStringExtra("text") ?: "새 쪽지"
        startForeground(NOTIF_ID, buildNotification(line, text))
        startRinging()
        return START_STICKY
    }

    private fun buildNotification(line: String, text: String): Notification {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CH_ID, "AOI 호출 알람", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "라인 호출 알람"
                setSound(null, null)          // 소리는 서비스가 직접(알람 볼륨) 재생
                enableVibration(false)        // 진동도 서비스가 직접
                setBypassDnd(true)            // 방해금지 무시
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(ch)
        }

        val fullIntent = Intent(this, AlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("line", line)
            putExtra("text", text)
        }
        val piFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val fullPi = PendingIntent.getActivity(this, 0, fullIntent, piFlags)

        val stopIntent = Intent(this, AlarmService::class.java).apply { action = ACTION_STOP }
        val stopPi = PendingIntent.getService(this, 1, stopIntent, piFlags)

        return NotificationCompat.Builder(this, CH_ID)
            .setSmallIcon(R.drawable.ic_stat_notify)
            .setContentTitle(line)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullPi, true)
            .setContentIntent(fullPi)
            .addAction(0, "확인 (알람 끄기)", stopPi)
            .build()
    }

    private fun startRinging() {
        if (isRinging) return
        isRinging = true
        try {
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                val afd = resources.openRawResourceFd(R.raw.siren)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            // ignore
        }
        try {
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val pattern = longArrayOf(0, 600, 400)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun stopEverything() {
        isRinging = false
        try { player?.stop() } catch (e: Exception) {}
        try { player?.release() } catch (e: Exception) {}
        player = null
        try { vibrator?.cancel() } catch (e: Exception) {}
        vibrator = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        try { player?.release() } catch (e: Exception) {}
        try { vibrator?.cancel() } catch (e: Exception) {}
        isRinging = false
    }
}
