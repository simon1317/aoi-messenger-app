package com.jeonyu.aoimsg

import android.content.Intent
import android.os.Build
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

        // 알람 서비스 시작 -> 확인 누를 때까지 사이렌 반복
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
}
