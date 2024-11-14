package dte.masteriot.mdp.asteroidconspiracist.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

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

import dte.masteriot.mdp.asteroidconspiracist.services.MqttService;
import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.databinding.ActivityMapsBinding;


//GoogleMap.OnMapClickListener: This means that the activity will handle map click events via the onMapClick method
public class MapsActivity extends BaseActivity implements OnMapReadyCallback, OnMapClickListener {

    private GoogleMap mMap;
    private Circle mapCircle;
    private MqttService UFOMqtt = new MqttService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = getLayoutInflater().inflate(R.layout.activity_maps, null);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(contentView);

        // Obtain the SupportMapFragment and set the map ready callback
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
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

        // Add a marker and move the camera to Sydney
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, zoomLevel));

        // Register the activity as a click listener
        mMap.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng point) {
        Toast.makeText(this, "Map Click: " + point.toString(), Toast.LENGTH_SHORT).show();

        // Draw a circle at the clicked location
        if (mapCircle != null) mapCircle.remove(); // Clear any existing circle
        CircleOptions circleOptions = new CircleOptions()
                .center(point)
                .radius(300000)  // 300 km radius
                .strokeWidth(2f)
                .strokeColor(0xFFFF0000)  // Red outline
                .fillColor(0x44FF0000);  // Transparent red fill
        mapCircle = mMap.addCircle(circleOptions);
    }

    public void settingUFOLocation(View view) {
        String TAG = "TAG_MDPMQTT";

        String publishingTopicCurrentLocation = "UFO/Location";
        String messageTopic = "34,15";

        UFOMqtt.createMQTTclient();
        UFOMqtt.connectToBroker().thenAccept(isConnected -> {
            if (isConnected) {
                Log.d(TAG, "Successfully connected to the broker.");
                UFOMqtt.publishMessage(publishingTopicCurrentLocation, messageTopic);
            } else {
                Log.d(TAG, "Failed to connect to the broker.");
            }
        });
    }
}

