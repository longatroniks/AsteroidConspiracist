package dte.masteriot.mdp.asteroidconspiracist.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.activity.modal.ObservationModal;
import dte.masteriot.mdp.asteroidconspiracist.entity.Observation;
import dte.masteriot.mdp.asteroidconspiracist.activity.recyclerview.observation.ObservationPagerAdapter;
import dte.masteriot.mdp.asteroidconspiracist.service.MqttService;
import dte.masteriot.mdp.asteroidconspiracist.util.file.FileUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private MqttService mqttService = new MqttService();
    private Marker currentObservationMarker;
    private List<Observation> observationLocations = new ArrayList<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean isConnectedToBroker = false;
    private boolean subscribedToObservations = true;  // Default subscription to Observations
    private boolean subscribedToShelters = false;

    private ObservationPagerAdapter adapter;
    private GoogleMap fullScreenMap;
    private ImageButton btnToggleFullScreen;
    private ImageButton btnExitFullScreen;
    private FrameLayout fullScreenMapOverlay;
    private boolean isFullScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the MapsActivity layout and add it to the BaseActivity content frame
        View contentView = getLayoutInflater().inflate(R.layout.activity_maps, null);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(contentView);

        // Initialize ViewPager2 and Adapter with click listener to move map
        ViewPager2 observationViewPager = findViewById(R.id.observationViewPager);
        adapter = new ObservationPagerAdapter(observationLocations, this::moveToObservationLocation);
        observationViewPager.setAdapter(adapter);  // Set adapter initially

        btnToggleFullScreen = findViewById(R.id.btnToggleFullScreen);
        btnExitFullScreen = findViewById(R.id.btnExitFullScreen);
        fullScreenMapOverlay = findViewById(R.id.fullScreenMapOverlay);

        // Set up Full Screen Toggle
        btnToggleFullScreen.setOnClickListener(v -> enterFullScreenMode());
        btnExitFullScreen.setOnClickListener(v -> exitFullScreenMode());

        // Set up the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up full-screen map (initialize once and reuse)
        SupportMapFragment fullScreenMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fullScreenMap);
        if (fullScreenMapFragment != null) {
            fullScreenMapFragment.getMapAsync(googleMap -> {
                fullScreenMap = googleMap;
                fullScreenMap.setOnMapClickListener(MapsActivity.this);  // Same behavior as regular map
            });
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mqttService.createMQTTclient();
        connectToBroker();

        setupSubscriptionButtons();
    }

    private void enterFullScreenMode() {
        fullScreenMapOverlay.setVisibility(View.VISIBLE);
        isFullScreen = true;

        if (mMap != null && fullScreenMap != null) {
            fullScreenMap.moveCamera(CameraUpdateFactory.newCameraPosition(mMap.getCameraPosition()));
            syncMapMarkers();  // Sync markers when entering full screen
        }
    }

    private void exitFullScreenMode() {
        fullScreenMapOverlay.setVisibility(View.GONE);
        isFullScreen = false;

        if (mMap != null && fullScreenMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(fullScreenMap.getCameraPosition()));
        }
    }

    // Move the map camera to the specified observation's location
    private void moveToObservationLocation(Observation observation) {
        if (mMap != null && observation != null) {
            LatLng location = observation.getLocation();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        }
    }


    private void connectToBroker() {
        mqttService.connectToBroker("Observations", message -> handleNewObservation(message, "Observations"))
                .thenAccept(isConnected -> {
                    isConnectedToBroker = isConnected;
                    if (isConnected) {
                        Log.d("MQTT", "Connected to broker and subscribed to Observations topic");
                        publishSavedObservations();
                        if (subscribedToObservations) {
                            loadSavedObservations();
                        }
                    } else {
                        Log.d("MQTT", "Failed to connect to broker");
                    }
                });
        mqttService.connectToBroker("Shelters", message -> handleNewObservation(message, "Shelters"));
    }

    private void setupSubscriptionButtons() {
        Button btnToggleObservations = findViewById(R.id.btnToggleObservations);

        btnToggleObservations.setOnClickListener(v -> {
            subscribedToObservations = !subscribedToObservations;
            toggleSubscription("Observations", subscribedToObservations);
        });
    }

    private void toggleSubscription(String topic, boolean subscribe) {
        if (subscribe) {
            mqttService.subscribeToTopic(topic, message -> handleNewObservation(message, topic));
            Toast.makeText(this, "Subscribed to " + topic, Toast.LENGTH_SHORT).show();
            if (topic.equals("Observations")) {
                loadSavedObservations();
            }
        } else {
            mqttService.unsubscribeFromTopic(topic);
            clearMarkersForTopic(topic);
            Toast.makeText(this, "Unsubscribed from " + topic, Toast.LENGTH_SHORT).show();
        }
    }


    private void clearMarkersForTopic(String topic) {
        if (mMap != null) {
            mMap.clear();
            if (subscribedToObservations) loadSavedObservations();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);

        checkLocationPermission();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                }
            }).addOnFailureListener(e -> Log.e("LocationError", "Failed to get user's location", e));
        }

        if (subscribedToObservations) {
            loadSavedObservations();
        }
    }


    @Override
    public void onMapClick(LatLng point) {
        if (currentObservationMarker != null) currentObservationMarker.remove();

        currentObservationMarker = mMap.addMarker(new MarkerOptions()
                .position(point)
                .title("New Observation")
                .snippet("Tap to confirm"));

        currentObservationMarker.showInfoWindow();
        mMap.setOnInfoWindowClickListener(marker -> {
            if (marker.equals(currentObservationMarker)) {
                openObservationModal(point);
            }
        });
    }

    private void openObservationModal(LatLng location) {
        ObservationModal modal = new ObservationModal(this, description -> {
            saveObservation(location, description);
            publishObservation(location, description);
        });
        modal.show();
    }

    private void saveObservation(LatLng location, String description) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String cityName = getCityFromLocation(location);

        Observation observation = new Observation(location, description, timestamp, cityName);
        observationLocations.add(observation);

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(location)
                .title(description)
                .snippet("Time: " + timestamp + "\nLocation: " + cityName));

        saveToFile(observation);
    }

    private String getCityFromLocation(LatLng location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getLocality();
            }
        } catch (Exception e) {
            Log.e("GeocoderError", "Error retrieving city name", e);
        }
        return "Unknown Location";
    }

    private void publishObservation(LatLng location, String description) {
        if (isConnectedToBroker) {
            mqttService.publishObservation(location, description);
            Log.d("MQTT", "Published observation via MQTT: " + location.toString());
        } else {
            Log.d("MQTT", "Broker connection not established. Observation not published.");
        }
    }

    private void publishSavedObservations() {
        List<Observation> savedObservations = FileUtils.loadObservationsFromFile(this);
        if (savedObservations != null) {
            for (Observation observation : savedObservations) {
                mqttService.publishObservation(observation.getLocation(), observation.getDescription());
                Log.d("MQTT", "Published saved observation via MQTT: " + observation.getLocation().toString());
            }
        }
    }

    private void saveToFile(Observation observation) {
        try {
            FileUtils.saveObservationToFile(this, observation.getLocation(), observation.getDescription(),
                    observation.getTimestamp(), observation.getCityName());
            Log.d("FileUtils", "Observation saved to file");
        } catch (Exception e) {
            Log.e("FileUtils", "Error saving observation to file", e);
        }
    }

    private void loadSavedObservations() {
        if (mMap == null) {
            Log.w("MapsActivity", "Map is not ready. Skipping loading observations.");
            return;
        }

        List<Observation> savedObservations = FileUtils.loadObservationsFromFile(this);
        observationLocations.addAll(savedObservations);

        // Update adapter data after loading observations
        adapter.notifyDataSetChanged();

        // Add markers to the small map and sync them with the full-screen map
        for (Observation obs : savedObservations) {
            LatLng location = obs.getLocation();
            String description = obs.getDescription();
            String timestamp = obs.getTimestamp();
            String cityName = obs.getCityName();

            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(description)
                    .snippet("Time: " + timestamp + "\nLocation: " + cityName));
        }

        syncMapMarkers();  // Ensure both maps display the same markers
    }

    private void handleNewObservation(String message, String topic) {
        if ((topic.equals("Observations") && subscribedToObservations) ||
                (topic.equals("Shelters") && subscribedToShelters)) {
            LatLng location = parseLocationFromMessage(message);
            if (location != null) {
                mMap.addMarker(new MarkerOptions().position(location).title(topic + " Observation"));
                Log.d("MQTT", "New " + topic + " observation received from MQTT: " + location.toString());
            }
        }
    }

    private LatLng parseLocationFromMessage(String message) {
        try {
            String[] parts = message.split(",");
            double lat = Double.parseDouble(parts[0]);
            double lng = Double.parseDouble(parts[1]);
            return new LatLng(lat, lng);
        } catch (Exception e) {
            Log.e("MQTT", "Error parsing MQTT message", e);
            return null;
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                }
            });
        }
    }

    private void syncMapMarkers() {
        if (fullScreenMap != null) {
            fullScreenMap.clear();  // Clear existing markers in full-screen map
            for (Observation obs : observationLocations) {
                LatLng location = obs.getLocation();
                String description = obs.getDescription();
                String timestamp = obs.getTimestamp();
                String cityName = obs.getCityName();

                fullScreenMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(description)
                        .snippet("Time: " + timestamp + "\nLocation: " + cityName));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
                checkLocationPermission();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
