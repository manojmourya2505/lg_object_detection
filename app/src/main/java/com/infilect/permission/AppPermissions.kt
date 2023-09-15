package com.infilect.permission

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
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