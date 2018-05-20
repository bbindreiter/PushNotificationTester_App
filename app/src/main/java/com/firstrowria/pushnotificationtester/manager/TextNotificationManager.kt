package com.firstrowria.pushnotificationtester.manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.firstrowria.pushnotificationtester.R
import java.text.SimpleDateFormat
import java.util.*


class TextNotificationManager(private val context: Context, private val manager: NotificationManager) {

    private fun getNotificationBuilder(title: String, serverTime: Long, sentTime:Long, prio: String): NotificationCompat.Builder {
        val dateFormat = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
        val text = "server trigger time ${dateFormat.format(Date(serverTime))}\n" +
                "firebase sent time ${dateFormat.format(Date(sentTime))}"

        val builder = NotificationCompat.Builder(context, context.getString(R.string.default_channel_id))
            .setTicker(title)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setSmallIcon(R.drawable.ic_message_white_36dp)
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.primary))

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.priority = when (prio) {
                "0" -> NotificationCompat.PRIORITY_MIN
                "1" -> NotificationCompat.PRIORITY_LOW
                "2" -> NotificationCompat.PRIORITY_DEFAULT
                "3" -> NotificationCompat.PRIORITY_HIGH
                "4" -> NotificationCompat.PRIORITY_MAX
                else -> NotificationCompat.PRIORITY_DEFAULT
            }
        }

        return builder
    }

    private fun createNotificationChannel(prio: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getDefaultNotificationChannel(context) == null) {
            val channel = NotificationChannel(context.getString(R.string.default_channel_id), context.getString(R.string.channel_name), when (prio) {
                "0" -> NotificationManager.IMPORTANCE_MIN
                "1" -> NotificationManager.IMPORTANCE_LOW
                "2" -> NotificationManager.IMPORTANCE_DEFAULT
                "3" -> NotificationManager.IMPORTANCE_HIGH
                "4" -> NotificationManager.IMPORTANCE_MAX
                else -> NotificationManager.IMPORTANCE_DEFAULT
            })
            channel.description = context.getString(R.string.channel_description)

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showTestNotification(title: String, serverTime: Long, sentTime:Long, prio: String) {
        createNotificationChannel(prio)
        manager.notify(Random().nextInt(), getNotificationBuilder(title, serverTime, sentTime, prio).build())
    }

    companion object {

        private const val DATE_PATTERN = "HH:mm:ss"


        fun getDefaultNotificationChannel(context:Context): NotificationChannel? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.getNotificationChannel(context.getString(R.string.default_channel_id))
            } else
                null
        }
    }

}
