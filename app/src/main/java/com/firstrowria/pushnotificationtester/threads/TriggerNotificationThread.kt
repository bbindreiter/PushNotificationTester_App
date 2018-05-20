package com.firstrowria.pushnotificationtester.threads

import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.content.LocalBroadcastManager
import android.util.Log

import com.firstrowria.pushnotificationtester.activities.MainActivity

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder

import javax.net.ssl.HttpsURLConnection

class TriggerNotificationThread(private val context: Context, private val pushId: String, private val delay: Int, private val deliveryPrio: Int, private val notificationPrio: Int) : Thread() {

    override fun run() {
        val intent = Intent(MainActivity.BROADCAST_ACTION_NOTIFICATION_REQUESTED)

        try {
            val connection = URL("https://pushnotificationtester-fcm.appspot.com/notification?delay=" + delay +
                    "&deliveryPrio=" + deliveryPrio +
                    "&notificationPrio=" + notificationPrio +
                    "&pushId=" + URLEncoder.encode(pushId, "UTF-8"))
                    .openConnection() as HttpsURLConnection
            connection.setRequestProperty("User-Agent", Build.MANUFACTURER + "/" + Build.MODEL + "/" + Build.VERSION.RELEASE + "/" + Build.VERSION.SDK_INT + "/2.0")

            val inputStreamReader = InputStreamReader(connection.inputStream, "UTF-8")

            val br = BufferedReader(inputStreamReader)
            val s = br.readLine()

            inputStreamReader.close()
            connection.disconnect()

            //server just returns "1"
            intent.putExtra(MainActivity.BROADCAST_SUCCESS, s == "1")
        } catch (e: Exception) {
            e.printStackTrace()

            intent.putExtra(MainActivity.BROADCAST_SUCCESS, false)
            Log.e(MainActivity.TAG, "Cannot connect to server or response is wrong")

        }

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

}
