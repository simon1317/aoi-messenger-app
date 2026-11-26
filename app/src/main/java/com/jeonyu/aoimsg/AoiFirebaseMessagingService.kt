package com.jeonyu.aoimsg

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AoiFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        TokenRegistrar.save(applicationContext, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val line = data["line"] ?: data["title"] ?: message.notification?.title ?: "AOI 쪽지"
        val text = data["body"] ?: data["text"] ?: message.notification?.body ?: "새 쪽지가 왔습니다"

        if (Prefs.isMuted(applicationContext)) {
            // 무음 모드: 사이렌 없이 조용한 알림만 표시
            showSilentNotification(line, text)
            return
        }

        // 일반 모드: 알람 서비스 시작 -> 확인 누를 때까지 사이렌 반복
        val intent = Intent(this, AlarmService::class.java).apply {
            putExtra("line", line)
            putExtra("text", text)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun showSilentNotification(line: String, text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val chId = "aoi_silent_v1"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(chId, "AOI 쪽지 (무음)", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "무음 모드 알림"
            }
            nm.createNotificationChannel(ch)
        }
        val openIntent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(this, chId)
            .setSmallIcon(R.drawable.ic_stat_notify)
            .setContentTitle(line)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        nm.notify(System.currentTimeMillis().toInt(), n)
    }
}
