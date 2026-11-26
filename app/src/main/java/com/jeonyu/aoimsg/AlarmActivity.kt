package com.jeonyu.aoimsg

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AlarmActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 잠금화면 위로 표시 + 화면 켜기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        setContentView(R.layout.activity_alarm)

        val line = intent.getStringExtra("line") ?: "AOI 쪽지"
        val text = intent.getStringExtra("text") ?: "새 쪽지"
        findViewById<TextView>(R.id.tvLine).text = line
        findViewById<TextView>(R.id.tvText).text = text

        findViewById<Button>(R.id.btnOk).setOnClickListener {
            stopAlarm()
            finish()
        }
        findViewById<Button>(R.id.btnOpen).setOnClickListener {
            stopAlarm()
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
            finish()
        }
    }

    private fun stopAlarm() {
        startService(Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP
        })
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // 뒤로가기로는 안 꺼지게 (실수 방지) — 확인 버튼으로만
    }
}
