package id.tandara.parent.core.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import id.tandara.parent.MainActivity
import id.tandara.parent.R
import id.tandara.parent.domain.model.ParentNotification

class SystemNotificationManager(private val context: Context) {
    companion object { const val CHANNEL_ID = "tandara_updates"; const val EXTRA_DESTINATION = "notification_destination" }
    private val system = context.getSystemService(NotificationManager::class.java)
    private val seen = context.getSharedPreferences("system_notification_dedup", Context.MODE_PRIVATE)

    init {
        system.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Tandara Updates", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Pembaruan kehadiran dan izin siswa"
        })
    }

    fun show(eventType: String, item: ParentNotification) {
        if (eventType !in setOf("STUDENT_CHECK_IN", "STUDENT_CHECK_OUT", "ATTENDANCE_CORRECTED", "LEAVE_APPROVED", "LEAVE_REJECTED")) return
        val key = "$eventType:${item.id}"
        if (seen.getBoolean(key, false)) return
        if (!NotificationPermissionManager(context).hasNotificationPermission()) return
        val destination = if (eventType.startsWith("LEAVE_")) "permission/history" else "reports"
        val intent = Intent(context, MainActivity::class.java).putExtra(EXTRA_DESTINATION, destination).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(context, key.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_tandara).setContentTitle(item.title).setContentText(item.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(item.message)).setAutoCancel(true).setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()
        system.notify(key.hashCode(), notification)
        seen.edit().putBoolean(key, true).apply()
    }
}
