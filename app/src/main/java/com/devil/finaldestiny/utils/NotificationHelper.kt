package com.devil.finaldestiny.utils

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.devil.finaldestiny.MainActivity
import com.devil.finaldestiny.R

object NotificationHelper {
    const val CHANNEL_ID = "final_destiny_alerts"
    const val CHANNEL_NAME = "Messages & Activity Alerts"
    const val SOCIAL_CHANNEL_ID = "social_notifications"
    const val SOCIAL_CHANNEL_NAME = "Social Notifications (Likes, Follows, Comments)"
    private const val TAG = "NotificationHelper"

    /**
     * Initializes the high-importance Notification Channels for push popups.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (manager != null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for direct messages, follow updates, and activity alerts"
                    enableVibration(true)
                    enableLights(true)
                    setShowBadge(true)
                }
                manager.createNotificationChannel(channel)

                val socialChannel = NotificationChannel(
                    SOCIAL_CHANNEL_ID,
                    SOCIAL_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "High priority notifications for Likes, Follows, and Comments"
                    enableVibration(true)
                    enableLights(true)
                    setShowBadge(true)
                }
                manager.createNotificationChannel(socialChannel)

                Log.d(TAG, "[CHANNEL_INIT] High Importance Notification Channels created successfully")
            }
        }
    }

    /**
     * Checks and requests runtime POST_NOTIFICATIONS permission on Android 13+ (API 33+).
     */
    fun checkAndRequestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                Log.d(TAG, "[PERMISSION] Requesting POST_NOTIFICATIONS runtime permission on Android 13+")
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            } else {
                Log.d(TAG, "[PERMISSION] POST_NOTIFICATIONS permission already granted")
            }
        }
    }

    /**
     * Triggers an Android system-level Heads-Up Push Notification with direct chat navigation support.
     */
    fun showHeadsUpPushNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        targetScreen: String? = null,
        senderId: String? = null,
        senderUsername: String? = null,
        senderAvatarUrl: String? = null,
        notificationType: String? = null
    ) {
        // Verify POST_NOTIFICATIONS permission on API 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "[PUSH_NOTIF] Skipping notification: POST_NOTIFICATIONS permission not granted")
                return
            }
        }

        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                val resolvedTarget = targetScreen ?: if (!senderId.isNullOrBlank()) "CHAT_DETAIL" else "INBOX"
                putExtra("NAV_TARGET", resolvedTarget)
                if (!senderId.isNullOrBlank()) putExtra("OTHER_USER_ID", senderId)
                if (!senderUsername.isNullOrBlank()) putExtra("OTHER_USERNAME", senderUsername)
                if (!senderAvatarUrl.isNullOrBlank()) putExtra("OTHER_AVATAR", senderAvatarUrl)
                if (!targetScreen.isNullOrBlank()) putExtra("target_screen", targetScreen)
                if (!notificationType.isNullOrBlank()) putExtra("NOTIF_TYPE", notificationType)
            }

            val requestCode = if (!senderId.isNullOrBlank()) senderId.hashCode() else notificationId

            val pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val channelId = if (notificationType == "FOLLOW" || notificationType == "LIKE" || notificationType == "COMMENT" || notificationType == "NEW_FOLLOWER" || notificationType == "POST_LIKED") SOCIAL_CHANNEL_ID else CHANNEL_ID
            val smallIconRes = R.mipmap.ic_launcher

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(smallIconRes)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify(notificationId, builder.build())

            Log.d(TAG, "[PUSH_NOTIF] Successfully posted heads-up notification #$notificationId ($channelId): '$title' - '$message'")
        } catch (e: Exception) {
            Log.e(TAG, "[PUSH_NOTIF] Error posting notification #$notificationId", e)
        }
    }
}
