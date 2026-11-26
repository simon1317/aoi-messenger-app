package com.jeonyu.aoimsg

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class AoiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // google-services.json 없이 코드로 Firebase 초기화
        if (FirebaseApp.getApps(this).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApplicationId(BuildConfig.FB_APP_ID)
                .setApiKey(BuildConfig.FB_API_KEY)
                .setProjectId(BuildConfig.FB_PROJECT_ID)
                .setGcmSenderId(BuildConfig.FB_SENDER_ID)
                .setStorageBucket(BuildConfig.FB_STORAGE_BUCKET)
                .build()
            try {
                FirebaseApp.initializeApp(this, options)
            } catch (e: Exception) {
                // 설정값이 아직 placeholder면 조용히 넘어감 (빌드/실행 자체는 됨)
            }
        }
    }
}
