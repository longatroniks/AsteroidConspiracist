package dte.masteriot.mdp.asteroidconspiracist.activities;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.R;

public class AddShelterActivity extends BaseActivity {

    private static final String TAG = "AddShelterActivity";
    private EditText shelterNameEditText;
    private EditText cityNameEditText;
    private Button publishButton;
    private Button showListButton;
    private ListView sheltersListView;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private List<String> sheltersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate activity_add_shelter.xml within the content_frame of BaseActivity
        View contentView = getLayoutInflater().inflate(R.layout.activity_add_shelter, null);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(contentView);

        // Set up the toolbar and drawer toggle as provided by BaseActivity
        setSupportActionBar(toolbar);
        enableBackButton(false);

        // Initialize the UI elements
        shelterNameEditText = findViewById(R.id.shelter_name_edit_text);
        cityNameEditText = findViewById(R.id.city_name_edit_text);
        publishButton = findViewById(R.id.publish_button);
        showListButton = findViewById(R.id.show_list_button);
        sheltersListView = findViewById(R.id.shelters_list_view);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }

        publishButton.setOnClickListener(v -> publishShelter());
        showListButton.setOnClickListener(v -> showShelterList());
    }

    private void publishShelter() {
        String shelterName = shelterNameEditText.getText().toString();
        String cityName = cityNameEditText.getText().toString();

        if (shelterName.isEmpty() || cityName.isEmpty() || currentLocation == null) {
            Toast.makeText(this, "Please fill all fields and ensure location is available", Toast.LENGTH_SHORT).show();
            return;
        }

        double latitude = currentLocation.getLatitude();
        double longitude = currentLocation.getLongitude();
        String shelterEntry = shelterName + ", " + cityName + ", " + latitude + ", " + longitude;

        SharedPreferences sharedPreferences = getSharedPreferences("ShelterData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        sheltersList.add(shelterEntry);
        editor.putString("shelter_" + sheltersList.size(), shelterEntry);
        editor.putInt("shelter_count", sheltersList.size());
        editor.apply();

        Log.d(TAG, "Shelter saved: " + shelterEntry);
        Toast.makeText(this, "Shelter saved successfully", Toast.LENGTH_SHORT).show();
    }

    private void showShelterList() {
        loadSheltersFromPreferences();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sheltersList);
        sheltersListView.setAdapter(adapter);
    }

    private void loadSheltersFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("ShelterData", MODE_PRIVATE);
        int shelterCount = sharedPreferences.getInt("shelter_count", 0);
        sheltersList.clear();
        for (int i = 1; i <= shelterCount; i++) {
            String shelterEntry = sharedPreferences.getString("shelter_" + i, "No Data");
            sheltersList.add(shelterEntry);
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) return;
                    for (Location location : locationResult.getLocations()) {
                        currentLocation = location;
                        Log.d(TAG, "Updated location: Lat = " + currentLocation.getLatitude() + ", Lng = " + currentLocation.getLongitude());
                    }
                }
            }, getMainLooper());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
