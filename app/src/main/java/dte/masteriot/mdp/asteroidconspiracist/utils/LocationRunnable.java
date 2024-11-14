package dte.masteriot.mdp.asteroidconspiracist.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationRunnable implements Runnable {

    private static final String TAG = "LocationRunnable";
    private FusedLocationProviderClient fusedLocationClient;
    private Context context;

    public LocationRunnable(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @Override
    public void run() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Permisos de ubicación no concedidos.");
            return;
        }

        fusedLocationClient.getCurrentLocation(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Log.d(TAG, "Ubicación obtenida: " + location.getLatitude() + ", " + location.getLongitude());
                        // Aquí podrías realizar alguna acción con la ubicación obtenida, por ejemplo, devolverla
                    } else {
                        Log.e(TAG, "Error: Ubicación es nula.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener la ubicación.", e));
    }
}