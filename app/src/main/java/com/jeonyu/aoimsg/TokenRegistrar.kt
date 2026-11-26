package com.jeonyu.aoimsg

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object TokenRegistrar {
    private const val ROOM = "jeonyu-aoi"

    fun save(ctx: Context, token: String) {
        val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { return }

        val write = {
            val data = hashMapOf(
                "token" to token,
                "ts" to System.currentTimeMillis(),
                "platform" to "android",
                "model" to (android.os.Build.MODEL ?: ""),
                "appVer" to "1.0"
            )
            try {
                FirebaseFirestore.getInstance()
                    .collection("rooms").document(ROOM)
                    .collection("android_tokens").document(token)
                    .set(data)
            } catch (e: Exception) { /* ignore */ }
        }

        try {
            if (auth.currentUser == null) {
                auth.signInAnonymously().addOnSuccessListener { write() }
            } else {
                write()
            }
        } catch (e: Exception) { /* ignore */ }
    }
}
