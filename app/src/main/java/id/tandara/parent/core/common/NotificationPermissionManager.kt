package id.tandara.parent.core.common

import android.content.Context
import android.os.Build

class NotificationPermissionManager(private val context: Context) {
    /**
     * Checks if notification permission is granted.
     * In Android 13+ (API 33+), POST_NOTIFICATIONS is required.
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
