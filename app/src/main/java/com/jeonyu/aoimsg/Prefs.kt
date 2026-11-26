package com.jeonyu.aoimsg

import android.content.Context

object Prefs {
    private const val FILE = "aoi_prefs"
    private const val KEY_MUTED = "muted"

    fun isMuted(ctx: Context): Boolean =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_MUTED, false)

    fun setMuted(ctx: Context, muted: Boolean) {
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_MUTED, muted).apply()
    }
}
