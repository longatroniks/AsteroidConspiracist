package dte.masteriot.mdp.asteroidconspiracist;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import dte.masteriot.mdp.asteroidconspiracist.databinding.ActivityMapsBinding;


//GoogleMap.OnMapClickListener: This means that the activity will handle map click events via the onMapClick method
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnMapClickListener {

    private GoogleMap mMap;
    private Circle mapCircle;
    private ActivityMapsBinding binding;
    private AsteroidMqtt UFOMqtt=new AsteroidMqtt();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        float zoomLevel = 15.0f; // Zoom level between 2.0f (world view) to 21.0f (street level)

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, zoomLevel));

        // After the map is loaded, you register the activity as a click listener
        // Register the click listener
        mMap.setOnMapClickListener( this );
    }
    // This method will be called when the user clicks on the map
    @Override
    public void onMapClick( LatLng point ) {
        Toast.makeText(this, "Map Click: "+ point.toString(), Toast.LENGTH_SHORT).show();

        // Draw a circle with a radius of 300km at the long-clicked location
        CircleOptions circleOptions = new CircleOptions()
                .center(point)
                .radius(300000)  // Radius in meters (300km)
                .strokeWidth(2f)  // Circle stroke width
                .strokeColor(0xFFFF0000)  // Red outline (ARGB format)
                .fillColor(0x44FF0000);  // Transparent red fill (ARGB format)
        mapCircle = mMap.addCircle(circleOptions);
    }
    public void settingUFOLocation (View view)
    {
        String TAG="TAG_MDPMQTT";

        String publishingTopicCurrentLocation;
        String messageTopic;

        publishingTopicCurrentLocation="UFO/Location";
        messageTopic="34,15";
        //MQTT Connection AG
        UFOMqtt.createMQTTclient();

        // When MQTT connection is Successfully, topics can be published and subscribed. AG
        // CompletableFuture to manage the asynchronous connection instead of a callback interface
        UFOMqtt.connectToBroker().thenAccept(isConnected -> {
            if (isConnected) {
                Log.d(TAG, "Successfully connected to the broker.");
                UFOMqtt.publishMessage(publishingTopicCurrentLocation,messageTopic);

            } else {
                Log.d(TAG, "Failed to connect to the broker.");

            }
        });
        //



    }
}