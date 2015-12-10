package com.firstrowria.pushnotificationtester.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import com.firstrowria.pushnotificationtester.activities.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class PlayServicesUtil {

    public static boolean checkPlayServices(Context context) {

        boolean success = false;

        int isGooglePlayServicesAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (isGooglePlayServicesAvailable == ConnectionResult.SUCCESS)
            success = true;
        else if (isGooglePlayServicesAvailable == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            success = true;
            Log.d(MainActivity.TAG, "Play Services found but newer version available");
        } else
            Log.e(MainActivity.TAG, "Cannot find proper Play Services: " + isGooglePlayServicesAvailable);

        return success;
    }

    public static boolean checkInternetConnection(Context context) {

        boolean success = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null && cm.getActiveNetworkInfo() != null) {
            Log.d(MainActivity.TAG, "Connected to Internet: " + cm.getActiveNetworkInfo().getTypeName());
            success = true;
        } else
            Log.e(MainActivity.TAG, "Not connected to Internet");

        return success;
    }
}
