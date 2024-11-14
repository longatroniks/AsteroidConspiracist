package dte.masteriot.mdp.asteroidconspiracist.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.Random;
import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.services.MqttService;
import dte.masteriot.mdp.asteroidconspiracist.utils.LocationCallback;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback, OnMapClickListener {

    private GoogleMap mMap;
    private Circle mapCircle;
    private FusedLocationProviderClient fusedLocationClient;
    private MqttService UFOMqtt = new MqttService();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean bBrokerConnected = false;
    private static ArrayList<LatLng> UfoLocationArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate activity_maps.xml within BaseActivity's content frame
        View contentView = getLayoutInflater().inflate(R.layout.activity_maps, null);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(contentView);

        // Set up toolbar and navigation drawer
        setSupportActionBar(toolbar);
        enableBackButton(false); // Enable drawer toggle instead of back button

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        UFOMqtt.createMQTTclient();
        connectToBroker();
    }

    private void connectToBroker() {
        UFOMqtt.connectToBroker("Publishing UFO").thenAccept(isConnected -> {
            bBrokerConnected = isConnected;
            Log.d("TAG_MDPMQTT", isConnected ? "Successfully connected to the broker." : "Failed to connect to the broker.");
        });
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
        drawLocation(point, 300000); // Draw a 300 km radius circle
    }

    public void settingUFOLocation(View view) {
        getCurrentLocation(location -> {
            drawLocation(location, 100); // Draw a 100 m radius circle
            publishLocationMQTT(location);
            UfoLocationArray.add(location);
        });
    }

    private void drawLocation(LatLng point, double radius) {
        if (mMap != null) {
            CircleOptions circleOptions = new CircleOptions().center(point).radius(radius)
                    .strokeWidth(2f).strokeColor(0xFFFF0000).fillColor(0x44FF0000);
            mapCircle = mMap.addCircle(circleOptions);
        } else {
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
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY).build();
            fusedLocationClient.getCurrentLocation(request, null).addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    callback.onLocationRetrieved(currentLocation);
                }
            }).addOnFailureListener(e -> Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UFOMqtt.disconnectFromBroker();
    }

    public void listingUFOLocation(View view) {
        for (LatLng location : UfoLocationArray) {
            drawLocation(location, 100); // Draw each saved location with a 100-meter radius
        }
        Toast.makeText(this, "Listing all UFO locations.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
