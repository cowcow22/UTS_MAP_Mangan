package com.example.uts_map_mangan

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.*

class PopUpNotification(private val context: Context) {

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Reminder Channel"
            val descriptionText = "Channel for reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("reminder_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun startNotifications() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val drinkTimes = listOf(
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 6); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) },
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 8); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) },
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 10); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) },
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 12); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) },
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 14); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) },
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 16); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) },
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 18); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) },
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 20); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }
        )

        for (time in drinkTimes) {
            if (time.before(Calendar.getInstance())) {
                time.add(Calendar.DAY_OF_MONTH, 1)
            }
            val intent = Intent(context, DrinkReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                time.get(Calendar.HOUR_OF_DAY) + 1000, // Unique request code for drink reminders
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.timeInMillis, pendingIntent)
            Log.d("PopUpNotification", "Scheduled drink reminder for ${time.get(Calendar.HOUR_OF_DAY)}:${time.get(Calendar.MINUTE)}")
        }

        val eatTimes = listOf(
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 10); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) },
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 13); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) },
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 18); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }
        )

        for (time in eatTimes) {
            if (time.before(Calendar.getInstance())) {
                time.add(Calendar.DAY_OF_MONTH, 1)
            }
            val intent = Intent(context, EatReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                time.get(Calendar.HOUR_OF_DAY), // Unique request code for eat reminders
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.timeInMillis, pendingIntent)
            Log.d("PopUpNotification", "Scheduled eat reminder for ${time.get(Calendar.HOUR_OF_DAY)}:${time.get(Calendar.MINUTE)}")
        }
    }

    fun stopNotifications() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val drinkTimes = listOf(6, 8, 10, 12, 14, 16, 18, 20)
        for (hour in drinkTimes) {
            val intent = Intent(context, DrinkReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                hour + 1000, // Unique request code for drink reminders
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel() // Cancel the PendingIntent
                Log.d("PopUpNotification", "Cancelled drink reminder for $hour:00")
            }
        }

        val eatTimes = listOf(10, 13, 18)
        for (hour in eatTimes) {
            val intent = Intent(context, EatReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                hour, // Unique request code for eat reminders
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel() // Cancel the PendingIntent
                Log.d("PopUpNotification", "Cancelled eat reminder for $hour:00")
            }
        }
    }

    fun areNotificationsScheduled(): Boolean {
        val drinkIntent = Intent(context, DrinkReminderReceiver::class.java)

        val drinkTimes = listOf(6, 8, 10, 12, 14, 16, 18, 20)
        for (hour in drinkTimes) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                hour + 1000, // Unique request code for drink reminders
                drinkIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                return true
            }
        }

        val eatIntent = Intent(context, EatReminderReceiver::class.java)
        val eatTimes = listOf(10, 13, 18)
        for (hour in eatTimes) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                hour, // Unique request code for eat reminders
                eatIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                return true
            }
        }

        return false
    }
}