package com.lg.permission

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class AppPermissions(private val activity: Activity) {

    fun checkCameraPermission(): Boolean {

        if (ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        return true
    }
}