package dte.masteriot.mdp.asteroidconspiracist.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.annotation.NonNull;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Random;

import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.databinding.ActivityMapsBinding;
import dte.masteriot.mdp.asteroidconspiracist.services.MqttService;
import dte.masteriot.mdp.asteroidconspiracist.utils.LocationCallback;


//GoogleMap.OnMapClickListener: This means that the activity will handle map click events via the onMapClick method
public class MapsActivity extends BaseActivity implements OnMapReadyCallback, OnMapClickListener {

    private GoogleMap mMap;
    private Circle mapCircle;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityMapsBinding binding;
    private MqttService UFOMqtt = new MqttService();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LatLng currentLatLngUser;
    private boolean bBrokerConnected = false;

    public static ArrayList<LatLng> UfoLocationArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = getLayoutInflater().inflate(R.layout.activity_maps, null);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(contentView);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        UFOMqtt.createMQTTclient();
        connectToBroker();
    }

    private void connectToBroker() {
        UFOMqtt.connectToBroker("Publishing UFO").thenAccept(isConnected -> {
            bBrokerConnected = isConnected;
            if (isConnected) {
                Log.d("TAG_MDPMQTT", "Successfully connected to the broker.");
            } else {
                Log.d("TAG_MDPMQTT", "Failed to connect to the broker.");
            }
        });
    }
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Optional: Enable back button in the toolbar if needed
        enableBackButton(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        float zoomLevel = 15.0f;

        getCurrentLocation(location -> {
            mMap.addMarker(new MarkerOptions().position(location).title("User's Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel));
        });

        mMap.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng point) {
        Toast.makeText(this, "Map Click: " + point.toString(), Toast.LENGTH_SHORT).show();
        drawLocation(point, 300000); // Draw circle with 300 km radius
    }

    public void settingUFOLocation(View view) {
        getCurrentLocation(location -> {
            drawLocation(location, 100); // Draw circle with 100 m radius
            publishLocationMQTT(location);
            UfoLocationArray.add(location);
        });
    }

    private void drawLocation(LatLng point, double radius) {
        if (mMap != null) {
            CircleOptions circleOptions = new CircleOptions()
                    .center(point)
                    .radius(radius)
                    .strokeWidth(2f)
                    .strokeColor(0xFFFF0000)
                    .fillColor(0x44FF0000);
            mapCircle = mMap.addCircle(circleOptions);
        } else {
            Log.e("MapsActivity", "GoogleMap (mMap) is not initialized.");
            Toast.makeText(this, "Map is not ready yet. Please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    private void publishLocationMQTT(LatLng location) {
        String topic = "UFO/" + new Random().nextInt(1000) + "/Location/";
        String message = "LatLng:" + location.latitude + "," + location.longitude;
        if (bBrokerConnected) {
            UFOMqtt.publishMessage(topic, message);
        } else {
            Toast.makeText(this, "Broker Connection is not Ready. Try again!", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCurrentLocation(LocationCallback callback) {
        checkLocationPermission();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                    .setDurationMillis(5000)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .build();
            fusedLocationClient.getCurrentLocation(request, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            callback.onLocationRetrieved(currentLocation);
                        } else {
                            Toast.makeText(this, "Failed to get current location", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UFOMqtt.disconnectFromBroker();
    }

    public void listingUFOLocation(View view) {
        for (LatLng location : UfoLocationArray) {
            drawLocation(location, 100); // Draw each saved UFO location with a radius of 100 meters
        }
        Toast.makeText(this, "Listing all UFO locations.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}