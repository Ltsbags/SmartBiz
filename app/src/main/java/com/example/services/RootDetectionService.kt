package com.example.services

import java.io.File

class RootDetectionService {

    data class RootCheckResult(
        val isRooted: Boolean,
        val isTestKeysPresent: Boolean,
        val isSuBinaryPresent: Boolean,
        val isBusyBoxPresent: Boolean,
        val isDevModeLikely: Boolean
    )

    private val knownSuPaths = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su"
    )

    fun checkDeviceRootStatus(): RootCheckResult {
        var suFound = false
        for (path in knownSuPaths) {
            if (File(path).exists()) {
                suFound = true
                break
            }
        }

        val buildTags = android.os.Build.TAGS
        val isTestKeys = buildTags != null && buildTags.contains("test-keys")

        val busyboxFound = File("/system/xbin/busybox").exists() || File("/system/bin/busybox").exists()

        val isRooted = suFound || isTestKeys

        return RootCheckResult(
            isRooted = isRooted,
            isTestKeysPresent = isTestKeys,
            isSuBinaryPresent = suFound,
            isBusyBoxPresent = busyboxFound,
            isDevModeLikely = isRooted || busyboxFound
        )
    }
}
