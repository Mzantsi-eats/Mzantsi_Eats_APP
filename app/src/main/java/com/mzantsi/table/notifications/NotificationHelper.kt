package com.mzantsi.table.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mzantsi.table.MainActivity
import com.mzantsi.table.R
import com.mzantsi.table.data.AppNotification

object NotificationHelper {

    const val CHANNEL_ID = "mzantsi_updates"
    const val EXTRA_ROUTE = "open_route"
    private const val GROUP = "com.mzantsi.table.UPDATES"

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Updates and alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Ratings, cheers, comments, reviews and new recipes from cooks you follow"
            }
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun show(n: AppNotification) {
        show(id = n.id, title = n.title, body = n.body, route = n.route)
    }

    fun show(id: String, title: String, body: String, route: String) {
        val context = appContext ?: return
        if (!NotificationStore.markShown(id)) return
        if (!NotificationStore.enabled) return
        if (!hasPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ROUTE, route)
        }
        val pending = PendingIntent.getActivity(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(0xFF1F6B3B.toInt())
            .setAutoCancel(true)
            .setGroup(GROUP)
            .setContentIntent(pending)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id.hashCode(), notification)
        } catch (e: SecurityException) {
        }
    }

    fun clearAll() {
        appContext?.let { NotificationManagerCompat.from(it).cancelAll() }
    }
}
