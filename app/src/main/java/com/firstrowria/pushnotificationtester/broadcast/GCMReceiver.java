package com.firstrowria.pushnotificationtester.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.firstrowria.pushnotificationtester.R;
import com.firstrowria.pushnotificationtester.activities.MainActivity;
import com.firstrowria.pushnotificationtester.services.C2DMNotificationService;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMReceiver extends BroadcastReceiver {
    public static void unregisterAsync(final Context context) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {

                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

                try {
                    gcm.unregister();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            protected void onPostExecute(Boolean unregistered) {
                Intent intent = new Intent(MainActivity.BROADCAST_ACTION_PUSH_UNREGISTER);
                if (unregistered.booleanValue()) {
                    Log.d(MainActivity.TAG, "Successfully unregistered");

                    intent.putExtra(MainActivity.BROADCAST_SUCCESS, true);
                } else {
                    Log.d(MainActivity.TAG, "Error while unregistering");

                    intent.putExtra(MainActivity.BROADCAST_SUCCESS, false);
                }

                LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
            }


        }.execute(null, null, null);
    }

    public static void registerAsync(final Context context) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {

                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

                try {
                    return gcm.register(context.getString(R.string.gcm_sender_id));
                } catch (Exception e) {
                    e.printStackTrace();

                    return null;
                }
            }

            protected void onPostExecute(String registrationId) {
                Intent intent = new Intent(MainActivity.BROADCAST_ACTION_PUSH_REGISTER);

                if (registrationId != null && !registrationId.equals("")) {
                    Log.d(MainActivity.TAG, "Successfully registered, id: " + registrationId);

                    intent.putExtra(MainActivity.BROADCAST_PUSH_ID, registrationId);
                    intent.putExtra(MainActivity.BROADCAST_SUCCESS, true);
                } else {
                    Log.e(MainActivity.TAG, "Registering for Push Notifications failed");

                    intent.putExtra(MainActivity.BROADCAST_SUCCESS, false);
                }

                LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
            }

        }.execute(null, null, null);
    }

    @Override
    public final void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE"))
            handleMessage(context, intent);
    }

    private void handleMessage(Context context, Intent intent) {
        Log.d(MainActivity.TAG, "Notification arrived");

        Intent successIntent = new Intent(MainActivity.BROADCAST_ACTION_NOTIFICATION_ARRIVED);
        successIntent.putExtra(MainActivity.BROADCAST_SUCCESS, true);
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(successIntent);

        Intent c2dmNotificationIntent = new Intent(context, C2DMNotificationService.class);
        c2dmNotificationIntent.putExtra("title", intent.getStringExtra("title"));
        c2dmNotificationIntent.putExtra("serverTime", intent.getStringExtra("serverTime"));
        context.startService(c2dmNotificationIntent);
    }


}
