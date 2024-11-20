package dte.masteriot.mdp.asteroidconspiracist.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import dte.masteriot.mdp.asteroidconspiracist.util.date.DateUtils;
import dte.masteriot.mdp.asteroidconspiracist.util.file.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {


    private static final String OBSERVATION_TOPIC = "AsteroidObservation";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private FusedLocationProviderClient fusedLocationClient;
    private final MqttService mqttService = new MqttService();
    private ObservationPagerAdapter adapter;
    private GoogleMap mMap;
    private GoogleMap fullScreenMap;
    private MarkerPair currentObservationMarker;
    private ImageButton btnToggleFullScreen;
    private ImageButton btnExitFullScreen;
    private FrameLayout fullScreenMapOverlay;

    private boolean isFullScreen = false;
    private boolean isConnectedToBroker = false;

    private final List<Observation> observationLocations = new ArrayList<>();
    private final Set<String> publishedObservationIds = new HashSet<>();
    private final List<Observation> pendingObservations = new ArrayList<>();
    private final Map<Observation, MarkerPair> observationMarkers = new HashMap<>();

    public static class MarkerPair {
        public Marker normalMarker;
        public Marker fullScreenMarker;

        public MarkerPair(Marker normalMarker, Marker fullScreenMarker) {
            this.normalMarker = normalMarker;
            this.fullScreenMarker = fullScreenMarker;
        }
    }

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
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;
                setupMap(mMap);
            });
        }

        // Set up full-screen map
        SupportMapFragment fullScreenMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fullScreenMap);
        if (fullScreenMapFragment != null) {
            fullScreenMapFragment.getMapAsync(googleMap -> {
                fullScreenMap = googleMap;
                setupMap(fullScreenMap);
            });
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mqttService.createMQTTClient("maps-activity-client");
        connectToBroker();
    }

    private void enterFullScreenMode() {
        fullScreenMapOverlay.setVisibility(View.VISIBLE);
        isFullScreen = true;

        if (mMap != null && fullScreenMap != null) {
            // Sync camera position
            fullScreenMap.moveCamera(CameraUpdateFactory.newCameraPosition(mMap.getCameraPosition()));
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
        mqttService.connectToBroker(OBSERVATION_TOPIC, message -> handleNewObservation(message, OBSERVATION_TOPIC))
                .thenAccept(isConnected -> {
                    isConnectedToBroker = isConnected;
                    if (isConnected) {
                        Log.d("MapsActivity", "Connected to broker and subscribed to topic");
                        publishSavedObservations();
                        loadSavedObservations();

                        // Subscribe to the status topic
                        mqttService.subscribeToTopic("AsteroidObservation/Status", this::handleStatusMessage);
                    } else {
                        Log.d("MapsActivity", "Failed to connect to broker");
                    }
                });
    }

    private void handleStatusMessage(String message) {
        runOnUiThread(() -> {
            Log.d("MapsActivity", "Status message received: " + message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermission();

        setupMap(mMap);

        // Process pending observations
        runOnUiThread(() -> {
            for (Observation observation : pendingObservations) {
                LatLng location = observation.getLocation();
                String description = observation.getDescription();

                mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(description)
                        .snippet("Time: " + observation.getTimestamp() + "\nLocation: " + observation.getCityName()));
            }
            pendingObservations.clear();

            loadSavedObservations();
        });
    }

    private void setupMap(GoogleMap map) {
        map.setOnMapClickListener(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                runOnUiThread(() -> {
                    if (location != null) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        map.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                    }
                });
            }).addOnFailureListener(e -> Log.e("LocationError", "Failed to get user's location", e));
        }
    }

    @Override
    public void onMapClick(LatLng point) {
        if (currentObservationMarker != null) {
            if (currentObservationMarker.normalMarker != null) {
                currentObservationMarker.normalMarker.remove();
            }
            if (currentObservationMarker.fullScreenMarker != null) {
                currentObservationMarker.fullScreenMarker.remove();
            }
        }

        Marker normalMarker = null;
        Marker fullScreenMarker = null;

        if (mMap != null) {
            normalMarker = mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title("New Observation")
                    .snippet("Tap to confirm"));
            normalMarker.showInfoWindow();
        }

        if (fullScreenMap != null) {
            fullScreenMarker = fullScreenMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title("New Observation")
                    .snippet("Tap to confirm"));
            if (isFullScreen) {
                fullScreenMarker.showInfoWindow();
            }
        }

        currentObservationMarker = new MarkerPair(normalMarker, fullScreenMarker);

        // Set info window click listener on both maps
        GoogleMap.OnInfoWindowClickListener listener = marker -> {
            if ((currentObservationMarker.normalMarker != null && marker.equals(currentObservationMarker.normalMarker)) ||
                    (currentObservationMarker.fullScreenMarker != null && marker.equals(currentObservationMarker.fullScreenMarker))) {
                openObservationModal(point);
            }
        };

        if (mMap != null) {
            mMap.setOnInfoWindowClickListener(listener);
        }

        if (fullScreenMap != null) {
            fullScreenMap.setOnInfoWindowClickListener(listener);
        }
    }

    private void openObservationModal(LatLng location) {
        ObservationModal modal = new ObservationModal(this, description -> {
            saveObservation(location, description);
            publishObservation(location, description);
        });
        modal.show();
    }

    private void saveObservation(LatLng location, String description) {
        String timestamp = DateUtils.generateTimestamp();
        String cityName = getCityFromLocation(location);

        FileUtils.saveObservationToFile(this, location, description, timestamp, cityName);

        // Add the new observation to the local list and refresh the UI
        Observation newObservation = new Observation(location, description, timestamp, cityName);
        observationLocations.add(newObservation);

        runOnUiThread(() -> {
            adapter.notifyDataSetChanged();

            // Add markers to both maps
            Marker normalMarker = null;
            Marker fullScreenMarker = null;

            if (mMap != null) {
                normalMarker = mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(description)
                        .snippet("Time: " + timestamp + "\nLocation: " + cityName));
            }

            if (fullScreenMap != null) {
                fullScreenMarker = fullScreenMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(description)
                        .snippet("Time: " + timestamp + "\nLocation: " + cityName));
            }

            observationMarkers.put(newObservation, new MarkerPair(normalMarker, fullScreenMarker));

            Log.d("MapsActivity", "Observation saved and markers added to both maps.");
        });
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

    public void publishObservation(LatLng location, String description) {
        String observationId = generateObservationId(location, description);

        if (publishedObservationIds.contains(observationId)) {
            Log.d("MapsActivity", "Observation already published. Skipping: " + observationId);
            return;
        }

        String topic = OBSERVATION_TOPIC;
        String message = location.latitude + "," + location.longitude + "," + description;

        mqttService.publishMessage(topic, message);
        publishedObservationIds.add(observationId);
        Log.d("MapsActivity", "Published observation: " + observationId);
    }

    private String generateObservationId(LatLng location, String description) {
        return location.latitude + "," + location.longitude + "," + description;
    }

    private void publishSavedObservations() {
        List<Observation> savedObservations = FileUtils.loadObservationsFromFile(this);

        if (savedObservations != null) {
            for (Observation observation : savedObservations) {
                String observationId = generateObservationId(observation.getLocation(), observation.getDescription());

                if (!publishedObservationIds.contains(observationId)) {
                    String topic = OBSERVATION_TOPIC;
                    String message = observation.getLocation().latitude + "," +
                            observation.getLocation().longitude + "," +
                            observation.getDescription();

                    mqttService.publishMessage(topic, message);
                    publishedObservationIds.add(observationId);

                    Log.d("MapsActivity", "Published saved observation: " + observationId);
                } else {
                    Log.d("MapsActivity", "Skipping already published observation: " + observationId);
                }
            }
        } else {
            Log.d("MapsActivity", "No saved observations to publish.");
        }
    }

    private void loadSavedObservations() {
        if (mMap == null || fullScreenMap == null) {
            Log.w("MapsActivity", "Maps are not ready. Skipping loading observations.");
            return;
        }

        List<Observation> savedObservations = FileUtils.loadObservationsFromFile(this);

        runOnUiThread(() -> {
            for (Observation obs : savedObservations) {
                if (!observationLocations.contains(obs)) {
                    observationLocations.add(obs);

                    LatLng location = obs.getLocation();
                    String description = obs.getDescription();
                    String timestamp = obs.getTimestamp();
                    String cityName = obs.getCityName();

                    Marker normalMarker = mMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title(description)
                            .snippet("Time: " + timestamp + "\nLocation: " + cityName));

                    Marker fullScreenMarker = fullScreenMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title(description)
                            .snippet("Time: " + timestamp + "\nLocation: " + cityName));

                    observationMarkers.put(obs, new MarkerPair(normalMarker, fullScreenMarker));

                    Log.d("MapsActivity", "Marker added for saved observation: " + description);
                }
            }

            adapter.notifyDataSetChanged();
        });
    }

    private void handleNewObservation(String message, String topic) {
        runOnUiThread(() -> {
            if (!topic.equals(OBSERVATION_TOPIC)) {
                Log.w("MapsActivity", "Received message for unexpected topic: " + topic);
                return;
            }

            String[] parts = message.split(",");
            if (parts.length < 3) {
                Log.d("MapsActivity", "Non-observation message received: " + message);
                return; // Ignore non-observation messages
            }

            try {
                double latitude = Double.parseDouble(parts[0]);
                double longitude = Double.parseDouble(parts[1]);
                String description = parts[2];

                LatLng location = new LatLng(latitude, longitude);
                Observation observation = new Observation(location, description, DateUtils.generateTimestamp(), getCityFromLocation(location));

                if (!observationLocations.contains(observation)) {
                    observationLocations.add(observation);
                    adapter.notifyDataSetChanged();

                    // Add markers to both maps
                    Marker normalMarker = null;
                    Marker fullScreenMarker = null;

                    if (mMap != null) {
                        normalMarker = mMap.addMarker(new MarkerOptions()
                                .position(location)
                                .title(description)
                                .snippet("Time: " + observation.getTimestamp() + "\nLocation: " + observation.getCityName()));
                    }

                    if (fullScreenMap != null) {
                        fullScreenMarker = fullScreenMap.addMarker(new MarkerOptions()
                                .position(location)
                                .title(description)
                                .snippet("Time: " + observation.getTimestamp() + "\nLocation: " + observation.getCityName()));
                    }

                    observationMarkers.put(observation, new MarkerPair(normalMarker, fullScreenMarker));

                    Log.d("MapsActivity", "Added observation from MQTT: " + observation);

                } else {
                    Log.d("MapsActivity", "Duplicate observation received. Skipping: " + observation);
                }
            } catch (NumberFormatException e) {
                Log.e("MapsActivity", "Error parsing observation message: " + message, e);
            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                runOnUiThread(() -> {
                    if (location != null) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                    }
                });
            });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mqttService.disconnect();
    }

}
