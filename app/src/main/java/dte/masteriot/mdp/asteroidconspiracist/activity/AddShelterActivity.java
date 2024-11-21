package dte.masteriot.mdp.asteroidconspiracist.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.entity.Shelter;
import dte.masteriot.mdp.asteroidconspiracist.service.MqttService;
import dte.masteriot.mdp.asteroidconspiracist.util.file.FileUtils;

public class AddShelterActivity extends BaseActivity {

    private static final String TAG = "AddShelterActivity";
    private static final String SHELTER_TOPIC = "AsteroidShelter";

    private EditText shelterNameEditText;
    private EditText cityNameEditText;
    private Button publishButton;
    private ListView sheltersListView;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private List<Shelter> shelterLocations = new ArrayList<>();
    private final MqttService mqttService = new MqttService();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate activity_add_shelter.xml within the content_frame of BaseActivity
        View contentView = getLayoutInflater().inflate(R.layout.activity_add_shelter, null);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(contentView);

        // Initialize the UI elements
        shelterNameEditText = findViewById(R.id.shelter_name_edit_text);
        cityNameEditText = findViewById(R.id.city_name_edit_text);
        publishButton = findViewById(R.id.publish_button);
        sheltersListView = findViewById(R.id.shelters_list_view);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }

        mqttService.createMQTTClient("add-shelter-client");

        // Automatically load shelters and display them
        loadSavedShelters();

        // Attach listener for the publish button
        publishButton.setOnClickListener(v -> addShelter());
    }

    private void addShelter() {
        String shelterName = shelterNameEditText.getText().toString();
        String cityName = cityNameEditText.getText().toString();

        if (shelterName.isEmpty() || currentLocation == null) {
            Toast.makeText(this, "Please fill all fields and ensure location is available", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng location = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        if (cityName.isEmpty()) {
            cityName = getCityFromLocation(location);
        }

        saveShelter(location, shelterName, cityName);
        publishShelter(location, shelterName);
        Toast.makeText(this, "Shelter saved and published", Toast.LENGTH_SHORT).show();
    }

    private void saveShelter(LatLng location, String name, String city) {
        Shelter newShelter = new Shelter(name, city, location);
        shelterLocations.add(newShelter);
        FileUtils.saveShelterToFile(this, location, name, city); // Save to file
        Log.d(TAG, "Shelter saved locally: " + newShelter);
    }

    private void publishShelter(LatLng location, String name) {
        String message = location.latitude + "," + location.longitude + "," + name;
        mqttService.publishMessage(SHELTER_TOPIC, message);
        Log.d(TAG, "Shelter published: " + message);
    }

    private void loadSavedShelters() {
        // Load shelters from the file
        List<Shelter> savedShelters = FileUtils.loadSheltersFromFile(this);
        shelterLocations.clear();
        shelterLocations.addAll(savedShelters);

        // Prepare shelter information for display
        List<String> shelterDetails = new ArrayList<>();
        for (Shelter shelter : savedShelters) {
            String details = "Name: " + shelter.getName() +
                    "\nCity: " + shelter.getCity() +
                    "\nLocation: (" + shelter.getLocation().latitude + ", " + shelter.getLocation().longitude + ")";
            shelterDetails.add(details);
        }

        // Set up the adapter to display shelter details in the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, shelterDetails);
        sheltersListView.setAdapter(adapter);

        Log.d(TAG, "Loaded and displayed " + savedShelters.size() + " saved shelters.");
    }

    private void getLastLocation() {
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
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;
                Log.d(TAG, "Current location: " + location.getLatitude() + ", " + location.getLongitude());
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Failed to get last location", e));
    }

    private String getCityFromLocation(LatLng location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getLocality();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving city name", e);
        }
        return "Unknown Location";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mqttService.disconnect();
    }
}
