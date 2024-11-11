package dte.masteriot.mdp.asteroidconspiracist.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.R;

public class CompassActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "CompassActivity";
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private ImageView compassImage;
    private TextView distanceText;
    private TextView closestCityText;
    private TextView closestShelterText;
    private float[] gravity;
    private float[] geomagnetic;
    private float azimuth = 0f;
    private float azimuthFix = 0f;
    private float targetLatitude;
    private float targetLongitude;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private double currentLatitude;
    private double currentLongitude;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        compassImage = findViewById(R.id.compass_image);
        distanceText = findViewById(R.id.distance_text);
        closestCityText = findViewById(R.id.closest_city_text);
        closestShelterText = findViewById(R.id.closest_shelter_text);

        // Configuración del sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Configuración del cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Configurar el callback de ubicación para actualizaciones periódicas
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    Log.d(TAG, "Ubicación actualizada: Latitud = " + currentLatitude + ", Longitud = " + currentLongitude);

                    // Encuentra la ubicación más cercana y actualiza la distancia
                    Location closestLocation = findClosestLocation(currentLatitude, currentLongitude);
                    if (closestLocation != null) {
                        float distance = location.distanceTo(closestLocation);
                        Log.d(TAG, "Ubicación más cercana encontrada: Latitud = " + closestLocation.getLatitude() + ", Longitud = " + closestLocation.getLongitude());
                        Log.d(TAG, "Distancia a la ubicación más cercana: " + distance + " metros");

                        // Muestra la distancia en el TextView
                        runOnUiThread(() -> distanceText.setText("Distance: " + distance + " meters"));
                    }
                }
            }
        };

        // Solicita permisos de ubicación y comienza las actualizaciones si están concedidos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }

        Log.d(TAG, "onCreate: Sensors and location client initialized");
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // Actualiza cada 5 segundos
        locationRequest.setFastestInterval(2000); // Intervalo más rápido de 2 segundos
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        }
    }

    private Location findClosestLocation(double latitude, double longitude) {
        List<Location> locations = readLocationsFromXML();
        List<String> cities = readCitiesFromXML();
        List<String> shelters = readSheltersFromXML();

        if (locations.isEmpty()) {
            Log.e(TAG, "No se encontraron ubicaciones en el archivo.");
            return null;
        }

        Location currentLocation = new Location("current");
        currentLocation.setLatitude(latitude);
        currentLocation.setLongitude(longitude);

        Location closestLocation = null;
        float minDistance = Float.MAX_VALUE;
        String closestCity = null;
        String closestShelter = null;

        Log.d(TAG, "Comenzando la búsqueda de la ubicación más cercana...");

        for (int i = 0; i < locations.size(); i++) {
            Location loc = locations.get(i);
            float distance = currentLocation.distanceTo(loc);
            Log.d(TAG, "Ubicación leída: Latitud = " + loc.getLatitude() + ", Longitud = " + loc.getLongitude() + ", Distancia = " + distance + " metros");

            if (distance < minDistance) {
                minDistance = distance;
                closestLocation = loc;
                closestCity = cities.get(i);
                closestShelter = shelters.get(i);
                Log.d(TAG, "Nueva ubicación más cercana: Latitud = " + loc.getLatitude() + ", Longitud = " + loc.getLongitude() + ", Distancia = " + minDistance + " metros");
                Log.d(TAG, "Ciudad más cercana: " + closestCity + ", Shelter más cercano: " + closestShelter);
            }
        }

        // Actualizar los TextView en la interfaz
        if (closestCity != null && closestShelter != null) {
            String finalClosestCity = closestCity;
            String finalClosestShelter = closestShelter;
            runOnUiThread(() -> {
                closestCityText.setText("City: " + finalClosestCity);
                closestShelterText.setText("Shelter name: " + finalClosestShelter);
            });
        }

        Log.d(TAG, "Ubicación más cercana final: Latitud = " + (closestLocation != null ? closestLocation.getLatitude() : "N/A") +
                ", Longitud = " + (closestLocation != null ? closestLocation.getLongitude() : "N/A"));
        Log.d(TAG, "Ciudad más cercana final: " + (closestCity != null ? closestCity : "N/A") +
                ", Shelter más cercano final: " + (closestShelter != null ? closestShelter : "N/A"));

        return closestLocation;
    }

    private List<Location> readLocationsFromXML() {
        List<Location> locations = new ArrayList<>();
        String[] locationArray = getResources().getStringArray(R.array.locations);

        for (String loc : locationArray) {
            String[] parts = loc.split(",");
            if (parts.length == 4) { // Verificar que se tengan latitud, longitud, ciudad y shelter
                Location location = new Location("file");
                location.setLatitude(Double.parseDouble(parts[0]));
                location.setLongitude(Double.parseDouble(parts[1]));
                locations.add(location);
                Log.d(TAG, "Ubicación cargada desde XML: Latitud = " + location.getLatitude() + ", Longitud = " + location.getLongitude());
            }
        }
        return locations;
    }

    private List<String> readCitiesFromXML() {
        List<String> cities = new ArrayList<>();
        String[] locationArray = getResources().getStringArray(R.array.locations);

        for (String loc : locationArray) {
            String[] parts = loc.split(",");
            if (parts.length == 4) {
                cities.add(parts[2]); // Agregar la ciudad a la lista
                Log.d(TAG, "Ciudad cargada: " + parts[2]);
            }
        }
        return cities;
    }

    private List<String> readSheltersFromXML() {
        List<String> shelters = new ArrayList<>();
        String[] locationArray = getResources().getStringArray(R.array.locations);

        for (String loc : locationArray) {
            String[] parts = loc.split(",");
            if (parts.length == 4) {
                shelters.add(parts[3]); // Agregar el nombre del shelter a la lista
                Log.d(TAG, "Shelter cargado: " + parts[3]);
            }
        }
        return shelters;
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
        Log.d(TAG, "onResume: Sensors registered");
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        fusedLocationClient.removeLocationUpdates(locationCallback);
        Log.d(TAG, "onPause: Sensors and location updates unregistered");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values;
        }
        if (gravity != null && geomagnetic != null) {
            float[] R = new float[9];
            float[] I = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
            if (success) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;
                azimuthFix = calculateBearingToTarget();
                updateCompass();
            }
        }
    }

    private float calculateBearingToTarget() {
        float bearing = (float) Math.toDegrees(Math.atan2(targetLongitude - currentLongitude, targetLatitude - currentLatitude));
        return (bearing + 360) % 360;
    }

    private void updateCompass() {
        float rotation = azimuthFix - azimuth;
        compassImage.setRotation(rotation);
        Log.d(TAG, "updateCompass: Compass rotated to - " + rotation + " degrees");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Log.e(TAG, "Permiso de ubicación denegado.");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No se necesita implementación
    }
}