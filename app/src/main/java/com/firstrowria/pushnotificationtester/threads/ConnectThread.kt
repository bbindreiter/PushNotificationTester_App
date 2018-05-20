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

import javax.net.ssl.HttpsURLConnection

class ConnectThread(private val context: Context) : Thread() {

    override fun run() {
        val intent = Intent(MainActivity.BROADCAST_ACTION_SERVER_CONNECTION)

        try {
            val connection = URL("https://pushnotificationtester-fcm.appspot.com/connect").openConnection() as HttpsURLConnection
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
