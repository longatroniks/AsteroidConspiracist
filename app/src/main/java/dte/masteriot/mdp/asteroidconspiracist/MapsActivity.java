package dte.masteriot.mdp.asteroidconspiracist;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.annotation.NonNull;

import android.Manifest;
import android.content.pm.PackageManager;
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

import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Random;

import dte.masteriot.mdp.asteroidconspiracist.databinding.ActivityMapsBinding;

//api key: AIzaSyDG9MYlpjvE6Z7GQznwQbRoT9SD5zhcpdQ
//SHA1: CE:77:53:71:14:FA:EE:5F:BC:99:75:F0:BD:A3:5B:33:54:7C:C0:E6

//GoogleMap.OnMapClickListener: This means that the activity will handle map click events via the onMapClick method
//, OnMapClickListener
//improvements
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    String TAG="TAG_MDPMQTT";

    private GoogleMap mMap;
    private Circle mapCircle;
    boolean bBrokerConnected=false;

    // It is used in Android applications to define a unique request code for permission requests.
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // To access location services using Google’s Fused Location Provider API.
    private FusedLocationProviderClient fusedLocationClient;
    // Store the User Location
    private LatLng currentLatLngUser;

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

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //MQTT Connection for UFO AG
        UFOMqtt.createMQTTclient();

        // When MQTT connection is Successfully, topics can be published and subscribed. AG
        // CompletableFuture to manage the asynchronous connection instead of a callback interface
        UFOMqtt.connectToBroker("Publishing UFO").thenAccept(isConnected -> {
            if (isConnected) {
                Log.d(TAG, "Successfully connected to the broker.");
                bBrokerConnected=true;
            } else {
                Log.d(TAG, "Failed to connect to the broker.");
                bBrokerConnected=true;
            }
        });
        //
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
        //mMap.setOnMapClickListener( this );
    }


    public void settingUFOLocation (View view)
    {
        String TAG="TAG_MDPMQTT";

        //Get User Location where UFO was watched
        getCurrentLocation(new  LocationCallback()
        {
            @Override
            public void onLocationRetrieved(LatLng latLng) {
                // Once location is retrieved, execute the following methods:
                //
                //Draw Location
                drawLocation(currentLatLngUser);
                //Publish the User location
                publishLocationMQTT(currentLatLngUser);
            }
        });
    }
    public void publishLocationMQTT(LatLng location)
    {
        String publishingTopicCurrentLocation;
        String messageTopic;
        Random random=new Random();
        int startIndex,endIndex;
        int randomId=random.nextInt(1000)+1;//(1-1000)

        publishingTopicCurrentLocation="UFO/"+randomId+"/Location/"; //"UFO/Id/Location"

        //messageTopic="34,15";
        startIndex = location.toString().indexOf("(")+1;
        endIndex =  location.toString().indexOf(")");
        messageTopic ="LatLng:"+location.toString().substring(startIndex, endIndex);

        if(bBrokerConnected)
            UFOMqtt.publishMessage(publishingTopicCurrentLocation, messageTopic);
        else
            Toast.makeText(this, "Broker Connection is not Ready. Try again!: ", Toast.LENGTH_SHORT).show();



    }
    // This method will be called when the user clicks on the map
    //@Override
    //public void onMapClick( LatLng point )
    void drawLocation(LatLng point)
    {
        Toast.makeText(this, "Map Click: "+ point.toString(), Toast.LENGTH_SHORT).show();

        // Draw a circle with a radius of 300km at the long-clicked location
        CircleOptions circleOptions = new CircleOptions()
                .center(point)
                .radius(100)  // Radius in meters (1km)
                .strokeWidth(2f)  // Circle stroke width
                .strokeColor(0xFFFF0000)  // Red outline (ARGB format)
                .fillColor(0x44FF0000);  // Transparent red fill (ARGB format)
        mapCircle = mMap.addCircle(circleOptions);
    }

    // Method to check location permissions and request them if necessary
    private void checkLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            // Request location permission.
            // Call the method onRequestPermissionsResult() automatically to request permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        else
        {
            // Permission is already granted, get the current location
            Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
        }
    }
    //The onRequestPermissionsResult method is called automatically by the Android framework
    // when your app requests permissions at runtime, and the user responds to that permission request.
    // It's triggered when you call the ActivityCompat.requestPermissions() method.
    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission granted
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            }
            else
            {
                // Permission denied
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Get current location and place a marker on the map
    public void getCurrentLocation(LocationCallback callback)
    {
        // Check permissions and request permission if it has not been granted
        checkLocationPermission();

        //check if the permission was granted to get the current location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            // Build the CurrentLocationRequest
            CurrentLocationRequest currentLocationRequest = new CurrentLocationRequest.Builder()
                    .setDurationMillis(5000)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .build();

            // Get the current location using FusedLocationProviderClient
            fusedLocationClient.getCurrentLocation(currentLocationRequest, null)
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Place a marker on the map at the current location
                                //LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                currentLatLngUser=new LatLng(location.getLatitude(), location.getLongitude());

                                // Trigger the callback after location is retrieved
                                if (callback!=null)
                                {
                                    callback.onLocationRetrieved(currentLatLngUser);
                                }

                                mMap.addMarker(new MarkerOptions().position(currentLatLngUser).title("Current Location"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLngUser, 15));
                            } else {
                                Toast.makeText(MapsActivity.this, "Failed to get current location", Toast.LENGTH_SHORT).show();

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MapsActivity.this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //MQTT Disconnection
        UFOMqtt.disconnectFromBroker();
        //
    }
}