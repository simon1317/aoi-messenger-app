package com.jeonyu.aoimsg

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var web: WebView
    private lateinit var btnMute: Button

    private val reqNotifPerm =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<android.widget.TextView>(R.id.tvTitle).text = "AOI 쪽지 v" + BuildConfig.VERSION_NAME

        web = findViewById(R.id.web)
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mediaPlaybackRequiresUserGesture = false
        }
        web.webViewClient = WebViewClient()
        web.loadUrl("https://dashing-khapse-3eefcb.netlify.app/")

        btnMute = findViewById(R.id.btnMute)
        updateMuteLabel()
        btnMute.setOnClickListener {
            val newMuted = !Prefs.isMuted(this)
            Prefs.setMuted(this, newMuted)
            updateMuteLabel()
            Toast.makeText(
                this,
                if (newMuted) "사이렌 꺼짐 — 조용히 알림만 옵니다" else "사이렌 켜짐 — 확인할 때까지 울립니다",
                Toast.LENGTH_SHORT
            ).show()
        }

        // 안드로이드 13+ 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                reqNotifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // FCM 토큰 등록
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { t ->
                if (t.isSuccessful && t.result != null) {
                    TokenRegistrar.save(applicationContext, t.result)
                }
            }
        } catch (e: Exception) { /* 설정 전이면 무시 */ }
    }

    private fun updateMuteLabel() {
        btnMute.text = if (Prefs.isMuted(this)) "🔕 소리 꺼짐" else "🔔 소리 켜짐"
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
