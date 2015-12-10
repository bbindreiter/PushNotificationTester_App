package com.firstrowria.pushnotificationtester.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.firstrowria.pushnotificationtester.activities.MainActivity;
import com.firstrowria.pushnotificationtester.manager.TextNotificationManager;

public class C2DMNotificationService extends Service {
    private TextNotificationManager notificationManager;
    private WakeLock wakeLock;

    /**
     * not using ipc... dont care about this method
     */
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(MainActivity.TAG, "Notification service started");

        // for > android 2.01
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.firstrowria.pushnotificationtester.services.C2DMNotificationService");
        wakeLock.setReferenceCounted(false);
        wakeLock.acquire();

        Log.d(MainActivity.TAG, "Wakelock acquired");

        notificationManager = new TextNotificationManager(this, (NotificationManager) getSystemService(NOTIFICATION_SERVICE));
    }


    // > Android 2.0
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();

        String title = bundle.getString("title");
        String serverTime = bundle.getString("serverTime");

        notificationManager.showTestNotification(title, serverTime);

        Intent successIntent = new Intent(MainActivity.BROADCAST_ACTION_NOTIFICATION_SHOWN);
        successIntent.putExtra(MainActivity.BROADCAST_SUCCESS, true);
        LocalBroadcastManager.getInstance(this).sendBroadcastSync(successIntent);

        Log.d(MainActivity.TAG, "Notification shown and service stop requested");

        stopSelf();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }

        Intent successIntent = new Intent(MainActivity.BROADCAST_ACTION_NOTIFICATION_SERVICE_STOP);
        successIntent.putExtra(MainActivity.BROADCAST_SUCCESS, true);
        LocalBroadcastManager.getInstance(this).sendBroadcastSync(successIntent);

        Log.d(MainActivity.TAG, "Notification service stopped and wakeLock released");

        super.onDestroy();
    }


}
