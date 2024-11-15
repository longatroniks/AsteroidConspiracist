package dte.masteriot.mdp.asteroidconspiracist.activity;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.viewmodel.CompassViewModel;

public class CompassActivity extends BaseActivity implements SensorEventListener {
    private static final String TAG = "CompassActivity";

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private ImageView compassImage;
    private TextView distanceText;
    private TextView closestCityText;
    private TextView closestShelterText;

    private float[] gravity;
    private float[] geomagnetic;
    private float azimuth = 0f;
    private float azimuthFix = 0f;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private CompassViewModel viewModel;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private double targetLatitude = 0.0;
    private double targetLongitude = 0.0;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = getLayoutInflater().inflate(R.layout.activity_compass, null);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(contentView);

        compassImage = findViewById(R.id.compass_image);
        distanceText = findViewById(R.id.distance_text);
        closestCityText = findViewById(R.id.closest_city_text);
        closestShelterText = findViewById(R.id.closest_shelter_text);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(CompassViewModel.class);

        // Observe ViewModel LiveData
        observeViewModel();

        // Initialize sensors and location
        setupSensorsAndLocation();
    }

    private void observeViewModel() {
        viewModel.getAzimuthLiveData().observe(this, this::updateCompassImage);
        viewModel.getDistanceLiveData().observe(this, distance -> distanceText.setText("Distance: " + distance + " meters"));
        viewModel.getClosestCityLiveData().observe(this, city -> closestCityText.setText("City: " + city));
        viewModel.getClosestShelterLiveData().observe(this, shelter -> closestShelterText.setText("Shelter name: " + shelter));
        viewModel.getTargetLocationLiveData().observe(this, location -> {
            if (location != null) {
                targetLatitude = location.getLatitude();
                targetLongitude = location.getLongitude();
            }
        });
    }

    private void setupSensorsAndLocation() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    viewModel.updateCurrentLocation(location);
                    List<Object[]> sheltersData = readSheltersDataFromPreferences();
                    viewModel.processClosestLocation(location, sheltersData);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        }
    }

    private List<Object[]> readSheltersDataFromPreferences() {
        List<Object[]> sheltersData = new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences("ShelterData", MODE_PRIVATE);
        int shelterCount = sharedPreferences.getInt("shelter_count", 0);

        for (int i = 1; i <= shelterCount; i++) {
            String shelterEntry = sharedPreferences.getString("shelter_" + i, null);
            if (shelterEntry != null) {
                String[] parts = shelterEntry.split(", ");
                if (parts.length == 4) {
                    try {
                        String shelterName = parts[0].trim();
                        String cityName = parts[1].trim();
                        double latitude = Double.parseDouble(parts[2].trim());
                        double longitude = Double.parseDouble(parts[3].trim());

                        Location location = new Location("shared_prefs");
                        location.setLatitude(latitude);
                        location.setLongitude(longitude);

                        sheltersData.add(new Object[]{shelterName, cityName, location});
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing location data: " + e.getMessage());
                    }
                }
            }
        }
        return sheltersData;
    }

    private void updateCompassImage(float azimuth) {
        compassImage.setRotation(azimuth);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values;
        }
        if (gravity != null && geomagnetic != null) {
            float[] R = new float[9];
            float[] I = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
            if (success) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;
                azimuthFix = calculateBearingToTarget();
                viewModel.updateAzimuth(azimuthFix - azimuth);
            }
        }
    }

    private float calculateBearingToTarget() {
        float bearing = (float) Math.toDegrees(Math.atan2(targetLongitude - currentLongitude, targetLatitude - currentLatitude));
        return (bearing + 360) % 360;
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No implementation required
    }
}
