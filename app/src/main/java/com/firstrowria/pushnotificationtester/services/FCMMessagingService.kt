package com.firstrowria.pushnotificationtester.services

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.firstrowria.pushnotificationtester.activities.MainActivity
import com.firstrowria.pushnotificationtester.manager.TextNotificationManager
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class FCMMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {

        Log.d(MainActivity.TAG, "Notification arrived from ${message.from} with data ${message.data}")

        sendSuccessIntent(MainActivity.BROADCAST_ACTION_NOTIFICATION_ARRIVED)

        val notificationManager = TextNotificationManager(this, getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        notificationManager.showTestNotification(message.data[MESSAGE_DATA_TITLE]!!,
                                                 message.data[MESSAGE_DATA_SERVER_TIME]!!.toLong(),
                                                 message.sentTime,
                                                 message.data[MESSAGE_DATA_PRIORITIZATION]!!)

        sendSuccessIntent(MainActivity.BROADCAST_ACTION_NOTIFICATION_SHOWN)
        Log.d(MainActivity.TAG, "Notification shown")
    }

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        Log.d(MainActivity.TAG, "Received new token $token")

    }

    private fun sendSuccessIntent(action: String) {
        val intent = Intent(action)
        intent.putExtra(MainActivity.BROADCAST_SUCCESS, true)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    companion object {
        private const val MESSAGE_DATA_TITLE = "title"
        private const val MESSAGE_DATA_SERVER_TIME = "serverTime"
        private const val MESSAGE_DATA_PRIORITIZATION = "notificationPrio"

        fun unregister(context: Context) {

            Log.d(MainActivity.TAG, "Unregistering from Push Notifications")
            FirebaseMessaging.getInstance().isAutoInitEnabled = false

            GlobalScope.launch {
                FirebaseInstanceId.getInstance().deleteInstanceId()

                Log.d(MainActivity.TAG, "Successfully unregistered")
                val intent = Intent(MainActivity.BROADCAST_ACTION_PUSH_UNREGISTER)
                intent.putExtra(MainActivity.BROADCAST_SUCCESS, true)
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
        }

        fun register(context: Context) {

            FirebaseMessaging.getInstance().isAutoInitEnabled = true

            FirebaseInstanceId.getInstance().instanceId.apply {
                addOnFailureListener { sendPushRegisterIntent(context) }
                addOnSuccessListener { sendPushRegisterIntent(context, it.token) }
            }
        }

        private fun sendPushRegisterIntent(context: Context, token: String? = null) {
            val intent = Intent(MainActivity.BROADCAST_ACTION_PUSH_REGISTER)

            if (token.isNullOrBlank()) {
                Log.e(MainActivity.TAG, "Registering for Push Notifications failed")
                intent.putExtra(MainActivity.BROADCAST_SUCCESS, false)
            } else {
                Log.d(MainActivity.TAG, "Successfully registered, id: $token")
                intent.putExtra(MainActivity.BROADCAST_PUSH_ID, token)
                intent.putExtra(MainActivity.BROADCAST_SUCCESS, true)
            }

            LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent)
        }
    }

}