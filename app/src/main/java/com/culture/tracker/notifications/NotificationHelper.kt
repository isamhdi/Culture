package com.culture.tracker.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.culture.tracker.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ACTIONS = "actions_reminders"
        const val NOTIF_WATERING_ID = 1001
        const val NOTIF_FERTILIZING_ID = 1002
    }

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ACTIONS,
                context.getString(R.string.notification_channel_actions_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notification_channel_actions_desc)
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun hasPostPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    fun notifyWateringDue(plantNames: List<String>) {
        if (plantNames.isEmpty() || !hasPostPermission()) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ACTIONS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Arroser ${plantNames.size} plante(s)")
            .setContentText(plantNames.joinToString(", "))
            .setStyle(NotificationCompat.BigTextStyle().bigText(plantNames.joinToString(", ")))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        androidx.core.app.NotificationManagerCompat.from(context).notify(NOTIF_WATERING_ID, notification)
    }

    fun notifyFertilizingDue(plantNames: List<String>) {
        if (plantNames.isEmpty() || !hasPostPermission()) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ACTIONS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Mettre de l'engrais pour ${plantNames.size} plante(s)")
            .setContentText(plantNames.joinToString(", "))
            .setStyle(NotificationCompat.BigTextStyle().bigText(plantNames.joinToString(", ")))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        androidx.core.app.NotificationManagerCompat.from(context).notify(NOTIF_FERTILIZING_ID, notification)
    }
}
