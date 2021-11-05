package com.example.sayem;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sayem Mahmud on 11/24/2020.
 * Email : context.sayem@gmail.com
 */

public class LocationUpdatesService extends Service {


    private static final String TAG = LocationUpdatesService.class.getSimpleName();


    private static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;


    private static final String CHANNEL_ID = "channel.sayem";


    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";


    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";


    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME + ".started_from_notification";


    private final IBinder mBinder = new LocalBinder();

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 12345678;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;


    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;


    private Handler mServiceHandler;

    /**
     * The current location.
     */
    private Location mLocation;


    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION, false);

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates();
            stopSelf();
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration) {
            // If targeting O, use the following code.
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, LocationUpdatesService.class));
            }
            String message = "Running location service";
            startForeground(NOTIFICATION_ID, getNotification(message));
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void requestLocationUpdates() {
        startService(new Intent(getApplicationContext(), LocationUpdatesService.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void removeLocationUpdates() {
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            stopSelf();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification(String message) {

        Intent intent = new Intent(this, LocationUpdatesService.class);

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent
                .getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MapsActivity.class), 0);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(message);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .addAction(R.drawable.ic_launcher_background, getString(R.string.launch_activity), activityPendingIntent)
                .addAction(R.drawable.ic_launcher_background, getString(R.string.remove_location_updates), servicePendingIntent)
                .setContentText(message)
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(true)
                .setColor(getColor(R.color.colorPrimary))
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setStyle(bigTextStyle)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }
        return builder.build();
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    mLocation = task.getResult();
                } else {
                    Log.w(TAG, "Failed to get location.");
                }
            });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void onNewLocation(Location location) {

        Log.d("Location Check", ""+location.getLatitude());

//        SharedPreferences sharedPref = getSharedPreferences("USER_PREFERENCE", Context.MODE_PRIVATE);
//        String phoneNo = sharedPref.getString("LOGGED_IN_USER_PHONE", null);

        App apps = ((App)getApplicationContext());
        Map<String, Object> user = new HashMap<>();
                user.put("lat", location.getLatitude());
        user.put("lon", location.getLongitude());

        FirebaseFirestore.getInstance().collection("users").document(apps.loggedInUserPhone).update(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));

        mLocation = location;
        // Notify anyone listening for broadcasts about the new location.
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        notify(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    public void notify(LatLng currentLocation){
        FirestoreUtil.getInstance().getDocumentRef().collection("users")
                .whereEqualTo("status", true)
                .get()
                .addOnCompleteListener(task -> {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    int count = 0;
                    for (DocumentSnapshot snapshot : documents){
                        Double latitude = (Double) snapshot.get("lat");
                        Double longitude = (Double) snapshot.get("lon");
                        if (latitude != null && longitude != null){
                            LatLng latLng = new LatLng(latitude, longitude);
                            float distance = LocationUtil.getInstance().findDistance(currentLocation, latLng);
                            if (distance <= 0.25){
                                count ++;
                                Log.d(TAG, "notify: " + distance);
                            }
                        }
                    }

                    // if count > 0 that means covid positive patient found
                    // then a notification will be showing for notify the user
                    if (count > 0){
                        mNotificationManager.notify(NOTIFICATION_ID,
                                getNotification(String.format("Alert! %s COVID positive patient detect near you", count)));
                    }
                });
    }

    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public  LocationUpdatesService getService() {
            return LocationUpdatesService.this;
        }
    }

    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }
}
