package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {
    private const val CHANNEL_ID = "farmadeu_reminders_channel"
    private const val CHANNEL_NAME = "Rappels et Alertes de Soins"
    private const val CHANNEL_DESC = "Notifications pour les étapes critiques de cultures et d'élevages"

    fun showNotification(context: Context, title: String, message: String, id: Int = System.currentTimeMillis().toInt()) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel for Android O or newer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap action to open MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        // Simple vector icon fallback: uses android.R.drawable.stat_sys_warning or stat_notify_chat or standard system app icon
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            notificationManager.notify(id, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
