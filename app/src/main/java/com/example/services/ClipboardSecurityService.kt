package com.example.services

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle

class ClipboardSecurityService(private val context: Context) {

    fun copySecureText(label: String, text: String, isSensitive: Boolean = false) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return

        val clip = ClipData.newPlainText(label, text)

        if (isSensitive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            clip.description.extras = PersistableBundle().apply {
                putBoolean("android.content.extra.IS_SENSITIVE", true)
            }
        }

        clipboard.setPrimaryClip(clip)
    }

    fun clearClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            clipboard.clearPrimaryClip()
        } else {
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
        }
    }
}
