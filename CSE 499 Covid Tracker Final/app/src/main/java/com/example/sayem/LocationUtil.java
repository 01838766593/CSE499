package com.example.sayem;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;

/**
 * Created by Sayem Mahmud on 11/23/2020.
 * Email : context.sayem@gmail.com
 */

public class LocationUtil {

    private static LocationUtil instance;

    public static LocationUtil getInstance(){
        if (instance == null) {
            instance = new LocationUtil();
        }
        return instance;
    }

    public float findDistance(LatLng source, LatLng destination) {
        if (source == null || destination == null){
            return 0f;
        }
        float[] results = new float[1];
        Location.distanceBetween (destination.latitude, destination.longitude,
                source.latitude, source.longitude, results);
        return results[0];
    }

    public String findLocationAddress(Context context, double latitude, double longitude) {
        String address = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 5);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder();
                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                address = strReturnedAddress.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return address;
    }
}
