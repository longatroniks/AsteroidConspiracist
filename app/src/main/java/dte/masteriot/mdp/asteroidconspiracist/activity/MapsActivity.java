package dte.masteriot.mdp.asteroidconspiracist.activity;

import android.Manifest;
import android.app.AlertDialog;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.activity.modal.ObservationModal;
import dte.masteriot.mdp.asteroidconspiracist.activity.modal.ShelterModal;
import dte.masteriot.mdp.asteroidconspiracist.entity.Observation;
import dte.masteriot.mdp.asteroidconspiracist.activity.recyclerview.observation.ObservationPagerAdapter;
import dte.masteriot.mdp.asteroidconspiracist.entity.Shelter;
import dte.masteriot.mdp.asteroidconspiracist.service.MqttService;
import dte.masteriot.mdp.asteroidconspiracist.util.date.DateUtils;
import dte.masteriot.mdp.asteroidconspiracist.util.file.FileUtils;
import dte.masteriot.mdp.asteroidconspiracist.viewmodel.MapsViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {


    private static final String OBSERVATION_TOPIC = "AsteroidObservation";
    private static final String SHELTER_TOPIC = "AsteroidShelter";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private MapsViewModel viewModel;

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
    private final List<Shelter> shelterLocations = new ArrayList<>(); // List to hold shelters

    private final Set<String> publishedObservationIds = new HashSet<>();
    private final Set<String> publishedShelterIds = new HashSet<>();

    private final List<Observation> pendingObservations = new ArrayList<>();
    private final List<Shelter> pendingShelters = new ArrayList<>();

    private final Map<Observation, MarkerPair> observationMarkers = new HashMap<>();
    private final Map<Shelter, MarkerPair> shelterMarkers = new HashMap<>();


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

        View contentView = getLayoutInflater().inflate(R.layout.activity_maps, null);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(contentView);

        viewModel = new ViewModelProvider(this).get(MapsViewModel.class);

        ViewPager2 observationViewPager = findViewById(R.id.observationViewPager);
        adapter = new ObservationPagerAdapter(new ArrayList<>(), this::moveToObservationLocation);
        observationViewPager.setAdapter(adapter);

        viewModel.getIsFullScreen().observe(this, fullScreen -> {
            if (fullScreen != null) {
                isFullScreen = fullScreen;
                fullScreenMapOverlay.setVisibility(fullScreen ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getObservationLocations().observe(this, observations -> {
            Log.d("MapsActivity", "Observations updated in ViewPager: " + observations.size());
            if (observations != null) {
                adapter.updateObservations(observations);
            }
        });

        btnToggleFullScreen = findViewById(R.id.btnToggleFullScreen);
        btnExitFullScreen = findViewById(R.id.btnExitFullScreen);
        fullScreenMapOverlay = findViewById(R.id.fullScreenMapOverlay);

        if (btnToggleFullScreen != null) {
            btnToggleFullScreen.setOnClickListener(v -> viewModel.setFullScreen(true));
        }
        if (btnExitFullScreen != null) {
            btnExitFullScreen.setOnClickListener(v -> viewModel.setFullScreen(false));
        }

        initializeMapFragments();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mqttService.createMQTTClient("maps-activity-client");
        connectToBroker();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isFullScreen", isFullScreen);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            isFullScreen = savedInstanceState.getBoolean("isFullScreen", false);
            if (isFullScreen) {
                enterFullScreenMode();
            } else {
                exitFullScreenMode();
            }
        }
    }

    private void initializeMapFragments() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;
                setupMap(mMap);
                Log.d("MapsActivity", "Main map initialized");
            });
        } else {
            Log.e("MapsActivity", "Main map fragment is null");
        }

        SupportMapFragment fullScreenMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fullScreenMap);
        if (fullScreenMapFragment != null) {
            fullScreenMapFragment.getMapAsync(googleMap -> {
                fullScreenMap = googleMap;
                setupMap(fullScreenMap);
                Log.d("MapsActivity", "Fullscreen map initialized");
            });
        } else {
            Log.e("MapsActivity", "Fullscreen map fragment is null");
        }
    }


    private void enterFullScreenMode() {
        fullScreenMapOverlay.setVisibility(View.VISIBLE);
        isFullScreen = true;

        if (mMap != null && fullScreenMap != null) {
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
                .thenCompose(isConnected -> {
                    if (isConnected) {
                        Log.d("MapsActivity", "Subscribed to observation topic.");
                        return mqttService.connectToBroker(SHELTER_TOPIC, message -> handleNewShelter(message, SHELTER_TOPIC));
                    } else {
                        Log.d("MapsActivity", "Failed to connect to observation topic.");
                        return CompletableFuture.completedFuture(false);
                    }
                })
                .thenAccept(isConnected -> {
                    if (isConnected) {
                        Log.d("MapsActivity", "Connected to broker and subscribed to shelter topic.");
                        publishSavedObservations();
                        loadSavedObservations();
                        publishSavedShelters();
                        loadSavedShelters();
                    } else {
                        Log.d("MapsActivity", "Failed to connect to shelter topic.");
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermission();

        setupMap(mMap);

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

            for (Shelter shelter : pendingShelters) {
                LatLng location = shelter.getLocation();
                String name = shelter.getName();

                mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(name)
                        .snippet("City: " + shelter.getCity())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }
            pendingShelters.clear();

            loadSavedObservations();
            loadSavedShelters();
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

        Marker normalMarker;
        Marker fullScreenMarker;

        if (mMap != null) {
            normalMarker = mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title("Choose Action")
                    .snippet("Tap to add Observation or Shelter"));
            normalMarker.showInfoWindow();

            mMap.setOnInfoWindowClickListener(marker -> {
                if (marker.equals(normalMarker)) {
                    showActionDialog(point);
                }
            });
        } else {
            normalMarker = null;
        }

        if (fullScreenMap != null) {
            fullScreenMarker = fullScreenMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title("Choose Action")
                    .snippet("Tap to add Observation or Shelter"));
            if (isFullScreen) {
                fullScreenMarker.showInfoWindow();
            }

            fullScreenMap.setOnInfoWindowClickListener(marker -> {
                if (marker.equals(fullScreenMarker)) {
                    showActionDialog(point);
                }
            });
        } else {
            fullScreenMarker = null;
        }

        currentObservationMarker = new MarkerPair(normalMarker, fullScreenMarker);
    }

    private void showActionDialog(LatLng location) {
        new AlertDialog.Builder(this)
                .setTitle("Add New Entry")
                .setMessage("Do you want to add an Observation or a Shelter?")
                .setPositiveButton("Observation", (dialog, which) -> openObservationModal(location))
                .setNegativeButton("Shelter", (dialog, which) -> openShelterModal(location))
                .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void openObservationModal(LatLng location) {
        ObservationModal modal = new ObservationModal(this, description -> {
            saveObservation(location, description);
            publishObservation(location, description);
        });
        modal.show();
    }

    private void openShelterModal(LatLng location) {
        ShelterModal modal = new ShelterModal(this, (name, city) -> {
            saveShelter(location, name, city);
            publishShelter(location, name);
        });
        modal.show();
    }

    private void saveObservation(LatLng location, String description) {
        String timestamp = DateUtils.generateTimestamp();
        String cityName = getCityFromLocation(location);

        FileUtils.saveObservationToFile(this, location, description, timestamp, cityName);

        Observation newObservation = new Observation(location, description, timestamp, cityName);

        Log.d("MapsActivity", "Saving observation: " + newObservation);
        viewModel.addObservation(newObservation);

        runOnUiThread(() -> {
            adapter.notifyDataSetChanged();

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
            Log.d("MapsActivity", "Observation saved and displayed: " + newObservation);
        });
    }

    private void saveShelter(LatLng location, String name, String city) {
        Shelter newShelter = new Shelter(name, city, location);
        shelterLocations.add(newShelter);
        FileUtils.saveShelterToFile(this, location, name, city);

        runOnUiThread(() -> {
            if (mMap != null) {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(newShelter.getLocation())
                        .title(newShelter.getName())
                        .snippet("City: " + newShelter.getCity())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));


                Marker fullScreenMarker = fullScreenMap.addMarker(new MarkerOptions()
                        .position(newShelter.getLocation())
                        .title(newShelter.getName())
                        .snippet("City: " + newShelter.getCity())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                shelterMarkers.put(newShelter, new MarkerPair(marker, fullScreenMarker));
            }
            Log.d("MapsActivity", "Shelter saved and displayed: " + newShelter);
        });
    }

    private void loadSavedShelters() {
        List<Shelter> savedShelters = FileUtils.loadSheltersFromFile(this);
        runOnUiThread(() -> {
            for (Shelter shelter : savedShelters) {
                if (!shelterLocations.contains(shelter)) {
                    shelterLocations.add(shelter);
                    if (mMap != null) {

                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(shelter.getLocation())
                                .title(shelter.getName())
                                .snippet("City: " + shelter.getCity())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));


                        Marker fullScreenMarker = fullScreenMap.addMarker(new MarkerOptions()
                                .position(shelter.getLocation())
                                .title(shelter.getName())
                                .snippet("City: " + shelter.getCity())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                        shelterMarkers.put(shelter, new MarkerPair(marker, fullScreenMarker));

                    }
                }
            }
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
        String observationId = generateId(location, description);

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

    public void publishShelter(LatLng location, String name) {
        String shelterId = generateId(location, name);

        if (publishedShelterIds.contains(shelterId)) {
            Log.d("MapsActivity", "Shelter already published. Skipping: " + shelterId);
            return;
        }

        String topic = SHELTER_TOPIC;
        String message = location.latitude + "," + location.longitude + "," + name;

        mqttService.publishMessage(topic, message);
        publishedShelterIds.add(shelterId);
        Log.d("MapsActivity", "Published shelter: " + shelterId);
    }

    private String generateId(LatLng location, String description) {
        return location.latitude + "," + location.longitude + "," + description;
    }

    private void publishSavedObservations() {
        List<Observation> savedObservations = FileUtils.loadObservationsFromFile(this);

        if (savedObservations != null) {
            for (Observation observation : savedObservations) {
                String observationId = generateId(observation.getLocation(), observation.getDescription());

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

    private void publishSavedShelters() {
        List<Shelter> savedShelters = FileUtils.loadSheltersFromFile(this);

        if (savedShelters != null) {
            for (Shelter shelter : savedShelters) {
                String shelterId = generateId(shelter.getLocation(), shelter.getName());

                if (!publishedShelterIds.contains(shelterId)) {
                    String message = shelter.getLocation().latitude + "," +
                            shelter.getLocation().longitude + "," +
                            shelter.getName();

                    mqttService.publishMessage(SHELTER_TOPIC, message);
                    publishedShelterIds.add(shelterId);

                    Log.d("MapsActivity", "Published saved shelter: " + shelterId);
                } else {
                    Log.d("MapsActivity", "Skipping already published shelter: " + shelterId);
                }
            }
        } else {
            Log.d("MapsActivity", "No saved shelters to publish.");
        }
    }


    private void loadSavedObservations() {
        if (mMap == null || fullScreenMap == null) {
            Log.w("MapsActivity", "Maps are not ready. Skipping loading observations.");
            return;
        }

        List<Observation> savedObservations = FileUtils.loadObservationsFromFile(this);
        Log.d("MapsActivity", "Loaded saved observations: " + savedObservations.size());
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
        Log.d("MapsActivity", "Received observation message: " + message);
        runOnUiThread(() -> {
            if (!topic.equals(OBSERVATION_TOPIC)) {
                Log.w("MapsActivity", "Received message for unexpected topic: " + topic);
                return;
            }

            String[] parts = message.split(",");
            if (parts.length < 3) {
                Log.d("MapsActivity", "Non-observation message received: " + message);
                return;
            }

            try {
                double latitude = Double.parseDouble(parts[0]);
                double longitude = Double.parseDouble(parts[1]);
                String description = parts[2];

                LatLng location = new LatLng(latitude, longitude);
                Observation observation = new Observation(
                        location,
                        description,
                        DateUtils.generateTimestamp(),
                        getCityFromLocation(location)
                );

                if (!observationLocations.contains(observation)) {
                    observationLocations.add(observation);
                    viewModel.addObservation(observation);

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

    private void handleNewShelter(String message, String topic) {
        runOnUiThread(() -> {
            if (!topic.equals(SHELTER_TOPIC)) {
                Log.w("MapsActivity", "Received message for unexpected topic: " + topic);
                return;
            }

            String[] parts = message.split(",");
            if (parts.length < 3) {
                Log.d("MapsActivity", "Non-shelter message received: " + message);
                return; // Ignore non-shelter messages
            }

            try {
                double latitude = Double.parseDouble(parts[0]);
                double longitude = Double.parseDouble(parts[1]);
                String name = parts[2];

                LatLng location = new LatLng(latitude, longitude);
                Shelter shelter = new Shelter(name, getCityFromLocation(location), location);

                if (!shelterLocations.contains(shelter)) {
                    shelterLocations.add(shelter);

                    runOnUiThread(() -> {
                        if (mMap != null) {
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(shelter.getLocation())
                                    .title(shelter.getName())
                                    .snippet("City: " + shelter.getCity())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));


                            Marker fullScreenMarker = fullScreenMap.addMarker(new MarkerOptions()
                                    .position(shelter.getLocation())
                                    .title(shelter.getName())
                                    .snippet("City: " + shelter.getCity())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                            shelterMarkers.put(shelter, new MarkerPair(marker, fullScreenMarker));
                        }

                        Log.d("MapsActivity", "Added shelter from MQTT: " + shelter);
                    });
                } else {
                    Log.d("MapsActivity", "Duplicate shelter received. Skipping: " + shelter);
                }
            } catch (NumberFormatException e) {
                Log.e("MapsActivity", "Error parsing shelter message: " + message, e);
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
