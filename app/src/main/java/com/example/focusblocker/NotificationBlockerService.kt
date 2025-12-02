package com.example.focusblocker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class NotificationBlockerService : NotificationListenerService() {

    data class SavedNotification(
        val originalPkg: String,
        val title: String,
        val text: String,
        val contentIntent: PendingIntent?,
        val timestamp: Long
    )

    companion object {
        var isBlockingActive = false
        val blockedPackages = mutableSetOf<String>()
        val savedNotifications = mutableListOf<SavedNotification>()

        private const val PREFS_NAME = "FocusBlockerPrefs"
        private const val KEY_BLOCKED_SET = "BlockedSet"
        private const val RESTORE_CHANNEL_ID = "restored_notifications"

        fun saveList(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putStringSet(KEY_BLOCKED_SET, blockedPackages).apply()
        }

        fun loadList(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedSet = prefs.getStringSet(KEY_BLOCKED_SET, emptySet())
            blockedPackages.clear()
            if (savedSet != null) {
                blockedPackages.addAll(savedSet)
            }
        }

        fun restoreNow(context: Context) {
            if (savedNotifications.isEmpty()) return

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    RESTORE_CHANNEL_ID,
                    "Restored Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                manager.createNotificationChannel(channel)
            }

            // FIX: Check for Permission on Android 13+ before notifying
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("FocusBlocker", "Cannot restore: Missing POST_NOTIFICATIONS permission")
                    return
                }
            }

            for ((index, note) in savedNotifications.withIndex()) {
                val builder = NotificationCompat.Builder(context, RESTORE_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("${note.title} (Missed)")
                    .setContentText(note.text)
                    .setAutoCancel(true)
                    .setWhen(note.timestamp)

                if (note.contentIntent != null) {
                    builder.setContentIntent(note.contentIntent)
                }

                manager.notify(index + 1000, builder.build())
            }
            savedNotifications.clear()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return
        if (sbn.packageName == packageName) return

        if (!isBlockingActive) return

        val pkg = sbn.packageName.lowercase()

        if (blockedPackages.contains(pkg)) {
            val extras = sbn.notification.extras
            val title = extras.getString("android.title") ?: "Notification"
            val text = extras.getString("android.text") ?: ""
            val intent = sbn.notification.contentIntent

            val saved = SavedNotification(
                originalPkg = sbn.packageName,
                title = title,
                text = text.toString(),
                contentIntent = intent,
                timestamp = System.currentTimeMillis()
            )
            savedNotifications.add(saved)

            cancelNotification(sbn.key)
            Log.d("FocusBlocker", "Blocked and saved: $pkg")
        }
    }
}