package com.moka.androidwebapp

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@ExperimentalAnimationApi
class FcmService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    // Foreground 에서만 호출이 된다.
    override fun onMessageReceived(message: RemoteMessage) {
        if (message.data.isNotEmpty() && null != message.notification) {
            val notification = message.notification!!

            @Suppress("UNUSED_VARIABLE")
            val payload = message.data

            val title = notification.title
            val content = notification.body

            val pendingIntent = getPendingIntent()

            NotificationUtil.notifyNotice(
                this,
                title = title,
                content = content,
                pendingIntent = pendingIntent
            )
        }
    }

    private fun getPendingIntent(): PendingIntent {
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(Intent(this@FcmService, MainActivity::class.java))

            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        return resultPendingIntent!!
    }
}
