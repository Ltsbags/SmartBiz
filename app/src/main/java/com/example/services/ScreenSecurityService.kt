package com.example.services

import android.app.Activity
import android.view.WindowManager

class ScreenSecurityService {

    private var isScreenSecurityEnabled = true

    fun setScreenSecurityEnabled(enabled: Boolean) {
        isScreenSecurityEnabled = enabled
    }

    fun isScreenSecurityEnabled(): Boolean = isScreenSecurityEnabled

    fun applyScreenProtection(activity: Activity?) {
        if (activity == null) return
        activity.runOnUiThread {
            if (isScreenSecurityEnabled) {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }

    fun clearScreenProtection(activity: Activity?) {
        if (activity == null) return
        activity.runOnUiThread {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
