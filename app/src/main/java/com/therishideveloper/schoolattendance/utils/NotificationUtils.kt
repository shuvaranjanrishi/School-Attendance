package com.therishideveloper.schoolattendance.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import java.io.File

fun showDownloadNotification(
    context: Context,
    file: File,
    title: String,
    description: String,
    mimeType: String
) {
    val channelId = "file_download_channel"
    val notificationId = System.currentTimeMillis().toInt()

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val channel = NotificationChannel(
        channelId,
        "Downloads",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        this.description = "Notifications for downloaded reports and profiles"
    }
    notificationManager.createNotificationChannel(channel)

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        notificationId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.stat_sys_download_done)
        .setContentTitle(title)
        .setContentText(description)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    notificationManager.notify(notificationId, notification)
}