package com.firstrowria.pushnotificationtester.services

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.firstrowria.pushnotificationtester.activities.MainActivity
import com.firstrowria.pushnotificationtester.manager.TextNotificationManager
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.concurrent.fixedRateTimer


class FCMMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {

        Log.d(MainActivity.TAG, "Notification arrived from ${message.from} with data ${message.data}")

        sendSuccessIntent(MainActivity.BROADCAST_ACTION_NOTIFICATION_ARRIVED)

        val notificationManager = TextNotificationManager(this, getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        notificationManager.showTestNotification(message.data.get(MESSAGE_DATA_TITLE)!!, message.data.get(MESSAGE_DATA_SERVER_TIME)!!.toLong(), message.sentTime, message.data.get(MESSAGE_DATA_PRIORITIZATION)!!)

        sendSuccessIntent(MainActivity.BROADCAST_ACTION_NOTIFICATION_SHOWN)
        Log.d(MainActivity.TAG, "Notification shown")
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
    }

}

class FCMInstanceIDListenerService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        super.onTokenRefresh()
        Log.d(MainActivity.TAG, "Received new token ${FirebaseInstanceId.getInstance().token}")
    }

    companion object {

        private const val TIMER_RUNS = 10
        private const val TIMER_PERIOD = 1000L

        fun deleteInstanceId(context: Context) {
            object : AsyncTask<Void, Void, Boolean>() {
                override fun doInBackground(vararg params: Void): Boolean? {
                    return try {
                        FirebaseInstanceId.getInstance().deleteInstanceId()
                        true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                }

                override fun onPostExecute(unregistered: Boolean?) {
                    val intent = Intent(MainActivity.BROADCAST_ACTION_PUSH_UNREGISTER)
                    if (unregistered == true) {
                        Log.d(MainActivity.TAG, "Successfully unregistered")
                        intent.putExtra(MainActivity.BROADCAST_SUCCESS, true)
                    } else {
                        Log.d(MainActivity.TAG, "Error while unregistering")
                        intent.putExtra(MainActivity.BROADCAST_SUCCESS, false)
                    }

                    LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent)
                }


            }.execute(null, null, null)
        }

        fun register(context: Context) {
            FirebaseInstanceId.getInstance().let {
                if (it.token.isNullOrBlank()) {
                    FirebaseMessaging.getInstance().isAutoInitEnabled = true

                    var runs = 0
                    fixedRateTimer(period = TIMER_PERIOD) {
                        runs++
                        Log.d(MainActivity.TAG, "check token availability")
                        if (!it.token.isNullOrBlank() || runs == TIMER_RUNS) {
                            cancel()
                            sendPushRegisterIntent(context, it.token)
                        }
                    }
                } else
                    sendPushRegisterIntent(context, it.token)
            }
        }

        private fun sendPushRegisterIntent(context: Context, token: String?) {
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