package com.firstrowria.pushnotificationtester.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.firstrowria.pushnotificationtester.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


public class TextNotificationManager {
    private NotificationManager manager;
    private Context context;

    public TextNotificationManager(Context context, NotificationManager manager) {
        this.manager = manager;
        this.context = context;
    }

    private NotificationCompat.Builder getNotificationBuilder(String title, String serverTime) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        String serverTimeFormatted = "";

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
            serverTimeFormatted = simpleDateFormat.format(new Date(Long.parseLong(serverTime)));
        } catch (Exception e) {
            serverTimeFormatted = serverTime + " (timestamp)";
        }

        builder.setTicker(title).setContentTitle(title).setContentText("sent on " + serverTimeFormatted).setSmallIcon(R.drawable.ic_message_white_36dp).setColor(context.getResources().getColor(R.color.primary));
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setAutoCancel(true);

        return builder;
    }

    public void showTestNotification(String title, String serverTime) {
        NotificationCompat.Builder builder = getNotificationBuilder(title, serverTime);

        manager.notify(new Random().nextInt(), builder.build());
    }

}
