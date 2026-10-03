package com.luntik.store

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    private val notifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        unlockHighRefreshRate()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        UpdateNotifier.ensureChannel(this)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        LocalFolders.ensureStructure(this)
        setContent { StoreRoot() }
    }

    private fun unlockHighRefreshRate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val d = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    this.display
                } else {
                    @Suppress("DEPRECATION")
                    windowManager.defaultDisplay
                }
                val best = d?.supportedModes?.maxByOrNull { it.refreshRate } ?: return
                val lp = window.attributes
                lp.preferredDisplayModeId = best.modeId
                window.attributes = lp
                window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
            }
        } catch (_: Exception) { }
    }
}
