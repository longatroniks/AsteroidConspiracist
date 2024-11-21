package dte.masteriot.mdp.asteroidconspiracist.activity;

import android.Manifest;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.entity.Shelter;
import dte.masteriot.mdp.asteroidconspiracist.service.MqttService;
import dte.masteriot.mdp.asteroidconspiracist.util.file.FileUtils;

public class CompassActivity extends BaseActivity implements SensorEventListener {

    private static final String TAG = "CompassActivity";
    private static final String SHELTER_TOPIC = "AsteroidShelter";

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private ImageView northArrowImage;
    private ImageView shelterArrowImage;
    private TextView directionText;
    private TextView instructionText;
    private TextView shelterInfoText;

    private float[] gravity;
    private float[] geomagnetic;

    private float azimuth = 0f;
    private float smoothedAzimuth = 0f;

    private LatLng userLocation = null;
    private final List<Shelter> shelters = new ArrayList<>();
    private Shelter nearestShelter;

    private final MqttService mqttService = new MqttService();
    private FusedLocationProviderClient locationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout into BaseActivity's content frame
        View contentView = getLayoutInflater().inflate(R.layout.activity_compass, null);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(contentView);

        // Initialize UI components
        northArrowImage = findViewById(R.id.north_arrow);
        shelterArrowImage = findViewById(R.id.shelter_arrow);
        directionText = findViewById(R.id.direction_text);
        instructionText = findViewById(R.id.instruction_text);
        shelterInfoText = findViewById(R.id.shelter_info_text);

        // Initialize sensor manager and sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Initialize location client
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        // Load saved shelters and connect to MQTT
        loadSavedShelters();
        connectToBroker();

        // Fetch user's location
        fetchUserLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register listeners
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister listeners to save battery
        sensorManager.unregisterListener(this);
    }

    private void connectToBroker() {
        mqttService.createMQTTClient("compass-activity-client");
        mqttService.connectToBroker(SHELTER_TOPIC, this::handleNewShelter);
    }

    private void loadSavedShelters() {
        List<Shelter> savedShelters = FileUtils.loadSheltersFromFile(this);
        if (savedShelters != null) {
            shelters.addAll(savedShelters);
            Log.d(TAG, "Loaded saved shelters: " + savedShelters.size());
        }
        updateNearestShelterInfo();
    }

    private void handleNewShelter(String message) {
        String[] parts = message.split(",");
        if (parts.length < 3) {
            Log.w(TAG, "Invalid shelter message: " + message);
            return;
        }

        try {
            double latitude = Double.parseDouble(parts[0]);
            double longitude = Double.parseDouble(parts[1]);
            String name = parts[2];
            String city = parts.length > 3 ? parts[3] : "Unknown";

            LatLng location = new LatLng(latitude, longitude);
            Shelter newShelter = new Shelter(name, city, location);

            if (!shelters.contains(newShelter)) {
                shelters.add(newShelter);
                Log.d(TAG, "Added shelter from MQTT: " + newShelter);
                updateNearestShelterInfo();
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing shelter message: " + message, e);
        }
    }

    private void fetchUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                Log.d(TAG, "User location set to: " + userLocation);
                updateNearestShelterInfo();
            } else {
                Log.w(TAG, "Failed to fetch user location");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error fetching user location", e));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values;
        }

        if (gravity != null && geomagnetic != null) {
            float[] R = new float[9];
            float[] I = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
            if (success) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);

                // Convert from radians to degrees
                azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360; // Normalize to 0–360 degrees

                // Apply a simple smoothing filter
                smoothedAzimuth = smoothedAzimuth * 0.9f + azimuth * 0.1f;

                // Rotate the north arrow to align with magnetic north
                northArrowImage.setRotation(-smoothedAzimuth);

                // Update nearest shelter arrow
                if (nearestShelter != null && userLocation != null) {
                    float bearingToShelter = calculateBearing(userLocation, nearestShelter.getLocation());
                    float shelterAzimuth = (bearingToShelter - smoothedAzimuth + 360) % 360;
                    shelterArrowImage.setRotation(-shelterAzimuth);
                }

                // Update UI
                updateDirectionLabel(smoothedAzimuth);
            }
        }
    }

    private float calculateBearing(LatLng from, LatLng to) {
        double lat1 = Math.toRadians(from.latitude);
        double lon1 = Math.toRadians(from.longitude);
        double lat2 = Math.toRadians(to.latitude);
        double lon2 = Math.toRadians(to.longitude);

        double dLon = lon2 - lon1;

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        return (float) (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    private void updateNearestShelterInfo() {
        if (userLocation == null || shelters.isEmpty()) return;

        // Find nearest shelter
        nearestShelter = Collections.min(shelters, Comparator.comparingDouble(shelter -> distanceBetween(userLocation, shelter.getLocation())));

        double distance = distanceBetween(userLocation, nearestShelter.getLocation());
        shelterInfoText.setText(String.format("Nearest Shelter: %s (%s)\nDistance: %.1f km",
                nearestShelter.getName(),
                nearestShelter.getCity(),
                distance / 1000.0)); // Convert to kilometers
    }

    private double distanceBetween(LatLng from, LatLng to) {
        double earthRadius = 6371000; // meters
        double dLat = Math.toRadians(to.latitude - from.latitude);
        double dLon = Math.toRadians(to.longitude - from.longitude);
        double lat1 = Math.toRadians(from.latitude);
        double lat2 = Math.toRadians(to.latitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    private void updateDirectionLabel(float azimuth) {
        String direction;
        if (azimuth >= 337.5 || azimuth < 22.5) direction = "N";
        else if (azimuth >= 22.5 && azimuth < 67.5) direction = "NE";
        else if (azimuth >= 67.5 && azimuth < 112.5) direction = "E";
        else if (azimuth >= 112.5 && azimuth < 157.5) direction = "SE";
        else if (azimuth >= 157.5 && azimuth < 202.5) direction = "S";
        else if (azimuth >= 202.5 && azimuth < 247.5) direction = "SW";
        else if (azimuth >= 247.5 && azimuth < 292.5) direction = "W";
        else direction = "NW";

        directionText.setText("Direction: " + direction);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not implemented
    }
}
