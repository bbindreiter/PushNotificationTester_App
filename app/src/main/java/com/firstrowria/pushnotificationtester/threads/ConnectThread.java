package com.firstrowria.pushnotificationtester.threads;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.firstrowria.pushnotificationtester.activities.MainActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ConnectThread extends Thread {
    private Context context = null;

    public ConnectThread(Context context) {
        this.context = context;
    }

    public void run() {
        Intent intent = new Intent(MainActivity.BROADCAST_ACTION_SERVER_CONNECTION);

        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL("https://pushnotificationtester.appspot.com/connect").openConnection();
            connection.setRequestProperty("User-Agent", Build.MANUFACTURER + "/" + Build.MODEL + "/" + Build.VERSION.RELEASE + "/" + Build.VERSION.SDK_INT + "/2.0");

            InputStreamReader in = new InputStreamReader(connection.getInputStream(), "UTF-8");

            BufferedReader br = new BufferedReader(in);
            String s = br.readLine();

            in.close();
            connection.disconnect();

            //server just returns "1"
            intent.putExtra(MainActivity.BROADCAST_SUCCESS, s != null && s.equals("1"));
        } catch (Exception e) {
            e.printStackTrace();

            intent.putExtra(MainActivity.BROADCAST_SUCCESS, false);
            Log.e(MainActivity.TAG, "Cannot connect to server or response is wrong");

        }

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


}
