package dte.masteriot.mdp.asteroidconspiracist.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import android.content.SharedPreferences;

import dte.masteriot.mdp.asteroidconspiracist.R;

import android.content.SharedPreferences;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class AddShelterActivity extends AppCompatActivity {

    private static final String TAG = "AddShelterActivity";
    private EditText shelterNameEditText;
    private EditText cityNameEditText;
    private Button publishButton;
    private Button showListButton;
    private TextView locationTextView;
    private ListView sheltersListView;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private List<String> sheltersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_shelter);

        shelterNameEditText = findViewById(R.id.shelter_name_edit_text);
        cityNameEditText = findViewById(R.id.city_name_edit_text);
        publishButton = findViewById(R.id.publish_button);
        showListButton = findViewById(R.id.show_list_button);
        sheltersListView = findViewById(R.id.shelters_list_view);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shelterName = shelterNameEditText.getText().toString();
                String cityName = cityNameEditText.getText().toString();

                if (shelterName.isEmpty() || cityName.isEmpty() || currentLocation == null) {
                    Toast.makeText(AddShelterActivity.this, "Complete todos los campos y asegúrese de que la ubicación esté disponible", Toast.LENGTH_SHORT).show();
                    return;
                }

                double latitude = currentLocation.getLatitude();
                double longitude = currentLocation.getLongitude();
                //SharedPreferences sharedPreferences = getSharedPreferences("ShelterData", MODE_PRIVATE);
                //sharedPreferences.edit().clear().apply(); // Borra todos los datos guardados

                SharedPreferences sharedPreferences = getSharedPreferences("ShelterData", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                // Guarda solo los valores: "shelterName, cityName, latitude, longitude"
                String shelterEntry = shelterName + ", " + cityName + ", " + latitude + ", " + longitude;
                sheltersList.add(shelterEntry);
                editor.putString("shelter_" + sheltersList.size(), shelterEntry);
                editor.putInt("shelter_count", sheltersList.size());
                editor.apply();



                Log.d(TAG, "Shelter publicado y guardado: " + shelterEntry);
                Toast.makeText(AddShelterActivity.this, "Shelter guardado correctamente", Toast.LENGTH_SHORT).show();
            }
        });

        showListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSheltersFromPreferences();
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddShelterActivity.this, android.R.layout.simple_list_item_1, sheltersList);
                sheltersListView.setAdapter(adapter);
            }
        });
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
        locationRequest.setInterval(5000); // Actualiza cada 5 segundos
        locationRequest.setFastestInterval(2000); // Intervalo más rápido de 2 segundos
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        currentLocation = location;
                        Log.d(TAG, "Ubicación actualizada: Latitud = " + currentLocation.getLatitude() + ", Longitud = " + currentLocation.getLongitude());
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
                Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
