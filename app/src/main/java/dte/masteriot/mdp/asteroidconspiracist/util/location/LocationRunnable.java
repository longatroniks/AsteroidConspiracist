package dte.masteriot.mdp.asteroidconspiracist.util.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class LocationRunnable implements Runnable {

    private static final String TAG = "LocationRunnable";
    private final FusedLocationProviderClient fusedLocationClient;
    private final Context context;
    private final LocationCallback callback;

    public LocationRunnable(Context context, LocationCallback callback) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.callback = callback;
    }

    @Override
    public void run() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permissions not granted.");
            return;
        }

        fusedLocationClient.getCurrentLocation(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Log.d(TAG, "Location obtained: " + location.getLatitude() + ", " + location.getLongitude());
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        callback.onLocationRetrieved(latLng);
                    } else {
                        Log.e(TAG, "Error: Location is null.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error retrieving location.", e));
    }
}
