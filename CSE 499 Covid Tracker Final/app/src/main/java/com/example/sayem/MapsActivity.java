package com.example.sayem;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final String TAG = MapsActivity.class.getSimpleName();

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    private LatLng currentLocation = new LatLng(23.746362,90.3885771);

    private EditText latEditText, lngEditText;
    private TextView tvPatientCount;

    private int radius = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        latEditText = findViewById(R.id.editText);
        lngEditText = findViewById(R.id.editText2);
        tvPatientCount = findViewById(R.id.tv_patient_count);

        Spinner spRadius = findViewById(R.id.sp_radius);

        spRadius.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                radius = Integer.parseInt(adapterView.getItemAtPosition(i).toString());
                if (mService != null){
                    mService.requestLocationUpdates();
                }
                Log.d(TAG, "onItemSelected: " + radius);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        new Handler().postDelayed(() -> runOnUiThread(()
                -> mapFragment.getMapAsync(MapsActivity.this)), 1000);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    public void updateButtonOnclick(View view){
        String phone = FirestoreUtil.getInstance().getCurrentUser().getPhoneNumber();
        if (phone != null && !phone.isEmpty()){
            FirestoreUtil.getInstance().getDocumentRef().collection("users").document(phone)
                    .update("lat", currentLocation.latitude);
            FirestoreUtil.getInstance().getDocumentRef().collection("users").document(phone)
                    .update("long", currentLocation.longitude);
        }
    }


    public void countPatient(){
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
                            float distance = LocationUtil.getInstance().findDistance(currentLocation, latLng) / 1000;
                            if (distance < radius){
                                count++;
                                Log.d(TAG, "findPatientCount: " + LocationUtil.getInstance().findDistance(currentLocation, latLng));
                            }
                        }
                    }
                    tvPatientCount.setText(String.format("%s patient found", count));
                });
    }

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    private void callService() {
        // Check that the user hasn't revoked permissions by going to Settings.
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            new Handler().postDelayed(() -> runOnUiThread(() -> {
                if (Util.checkGpsEnabled(MapsActivity.this)){
                    mService.requestLocationUpdates();
                } else
                    showGpsInactiveMessage();
            }), 1000);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
        callService();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.rootView),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                if (Util.checkGpsEnabled(MapsActivity.this)){
                    mService.requestLocationUpdates();
                } else
                    showGpsInactiveMessage();
            } else {
                Snackbar.make(findViewById(R.id.rootView), R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, view -> {
                            // Build intent that displays the App settings screen.
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }).show();
            }
        }
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null & mMap != null) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(currentLocation));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                latEditText.setText(String.format("%s", currentLocation.latitude));
                lngEditText.setText(String.format("%s", currentLocation.longitude));
                countPatient();
            }
        }
    };

    public void showGpsInactiveMessage() {
        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.rootView), getString(R.string.gps_required), Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.enable, view -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);
        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }
}