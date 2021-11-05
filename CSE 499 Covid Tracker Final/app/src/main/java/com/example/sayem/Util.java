package com.example.sayem;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Sayem Mahmud on 11/24/2020.
 * Email : context.sayem@gmail.com
 */

public class Util {

    private static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     *
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     *
     * @param location The {@link Location}.
     */
    public static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    public static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated, DateFormat.getDateTimeInstance().format(new Date()));
    }

    public static boolean hasOperator(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager != null && telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;
    }

    public static boolean checkPermissions(Context context, String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkGpsEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (lm != null) {
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } else
            return false;
    }
}