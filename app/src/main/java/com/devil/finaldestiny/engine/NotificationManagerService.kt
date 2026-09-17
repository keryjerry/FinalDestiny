package com.devil.finaldestiny.engine

import android.content.Context
import android.util.Log
import com.devil.finaldestiny.data.SupabaseAuthClient
import com.devil.finaldestiny.model.AppNotification
import com.devil.finaldestiny.model.NotificationType
import com.devil.finaldestiny.utils.NotificationHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NotificationManagerService {
    private const val TAG = "NotificationManagerService"
    private var pollJob: Job? = null

    private val _realtimeNotifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val realtimeNotifications: StateFlow<List<AppNotification>> = _realtimeNotifications.asStateFlow()

    private val seenNotificationIds = mutableSetOf<String>()

    /**
     * Subscribes to realtime notification inserts & polls Supabase notifications table.
     * Fires system heads-up notifications for Like, Follow, and Comment social events.
     */
    fun startNotificationListener(context: Context, currentUserId: String) {
        if (currentUserId.isBlank()) return
        pollJob?.cancel()
        pollJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            Log.d(TAG, "Starting Notification Manager Realtime Listener for user $currentUserId")
            var isInitialFetch = true

            while (isActive) {
                try {
                    val fetched = SupabaseAuthClient.fetchNotificationsFromSupabase(currentUserId)
                    if (fetched.isNotEmpty()) {
                        if (isInitialFetch) {
                            // Populate seen IDs on first load so cold start doesn't spam old notifications
                            for (n in fetched) {
                                seenNotificationIds.add(n.id)
                            }
                            isInitialFetch = false
                        } else {
                            for (notif in fetched) {
                                if (!notif.isRead && !seenNotificationIds.contains(notif.id)) {
                                    seenNotificationIds.add(notif.id)

                                    val (navTarget, typeKey) = when (notif.type) {
                                        NotificationType.FOLLOW -> Pair("USER_PROFILE", "FOLLOW")
                                        NotificationType.LIKE -> Pair("SECONDARY_FEED", "LIKE")
                                        NotificationType.MATCH -> Pair("SECONDARY_FEED", "COMMENT")
                                        else -> Pair("SECONDARY_FEED", "SOCIAL")
                                    }

                                    Log.d(TAG, "New live social notification detected! Title: '${notif.title}', Message: '${notif.message}'")

                                    NotificationHelper.showHeadsUpPushNotification(
                                        context = context,
                                        notificationId = notif.id.hashCode(),
                                        title = notif.title,
                                        message = notif.message,
                                        targetScreen = navTarget,
                                        senderId = notif.actionTargetScreen,
                                        senderUsername = notif.title,
                                        senderAvatarUrl = notif.senderAvatarUrl,
                                        notificationType = typeKey
                                    )
                                } else {
                                    seenNotificationIds.add(notif.id)
                                }
                            }
                        }
                        _realtimeNotifications.value = fetched
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in notification manager polling loop", e)
                }
                delay(8000) // Poll every 8 seconds for realtime responsiveness
            }
        }
    }

    fun stopListener() {
        pollJob?.cancel()
        pollJob = null
    }
}
