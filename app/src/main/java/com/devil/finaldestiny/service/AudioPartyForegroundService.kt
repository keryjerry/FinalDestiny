package com.devil.finaldestiny.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.devil.finaldestiny.MainActivity

class AudioPartyForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "final_connect_party_audio_channel"
        private const val CHANNEL_NAME = "Final Connect Live Party Rooms"
        private const val NOTIFICATION_ID = 9001

        const val ACTION_START_SERVICE = "ACTION_START_PARTY_SERVICE"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_PARTY_SERVICE"
        const val EXTRA_ROOM_TITLE = "EXTRA_ROOM_TITLE"
        const val EXTRA_HOST_NAME = "EXTRA_HOST_NAME"

        fun startService(context: Context, roomTitle: String, hostName: String) {
            val intent = Intent(context, AudioPartyForegroundService::class.java).apply {
                action = ACTION_START_SERVICE
                putExtra(EXTRA_ROOM_TITLE, roomTitle)
                putExtra(EXTRA_HOST_NAME, hostName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, AudioPartyForegroundService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val roomTitle = intent?.getStringExtra(EXTRA_ROOM_TITLE) ?: "Live Voice Party Room"
        val hostName = intent?.getStringExtra(EXTRA_HOST_NAME) ?: "Host"

        val notification = buildForegroundNotification(roomTitle, hostName)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
            }
            startForeground(NOTIFICATION_ID, notification, serviceType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps live audio room active in the background when app is minimized or locked."
                setSound(null, null)
                enableVibration(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(roomTitle: String, hostName: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val leaveRoomIntent = Intent(this, AudioPartyForegroundService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val leavePendingIntent = PendingIntent.getService(
            this,
            1,
            leaveRoomIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎙️ $roomTitle")
            .setContentText("Connected in Party Room • Host: $hostName")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Leave Room 🚪", leavePendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
