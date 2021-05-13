package com.moka.androidwebapp

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

@SuppressLint("StaticFieldLeak")
object NotificationUtil {

    // region Channel
    private const val CHANNEL_ID_01 = "p_channel_01"
    // endregion

    // region Notification Id
    private const val NOTI_ID_01 = 0xEE0001
    // endregion

    fun init(context: Context) {
        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val importance = NotificationManager.IMPORTANCE_LOW

        // The id of the channel.
        val id = CHANNEL_ID_01
        val name = "CHANNEL"

        val mChannel = NotificationChannel(id, name, importance)
        mChannel.enableLights(true)
        mChannel.lightColor = Color.RED
        mChannel.enableVibration(false)
        mNotificationManager.createNotificationChannel(mChannel)

        val notificationChannel = mNotificationManager.getNotificationChannel(CHANNEL_ID_01)
        notificationChannel.enableVibration(false)
        notificationChannel.importance = NotificationManager.IMPORTANCE_LOW
        notificationChannel.vibrationPattern = longArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0)
    }

    fun notifyNotice(
        context: Context,
        title: String? = "Title",
        content: CharSequence? = "",
        pendingIntent: PendingIntent
    ) {
        @Suppress("DEPRECATION")
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            context,
            CHANNEL_ID_01
        )

        notificationBuilder
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setLights(Color.GREEN, 1000, 2000)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setChannelId(CHANNEL_ID_01)

        NotificationManagerCompat
            .from(context)
            .notify(NOTI_ID_01, notificationBuilder.build())
    }
}
