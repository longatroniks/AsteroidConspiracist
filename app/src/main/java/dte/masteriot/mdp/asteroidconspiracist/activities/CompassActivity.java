package dte.masteriot.mdp.asteroidconspiracist.activities;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import dte.masteriot.mdp.asteroidconspiracist.R;

public class CompassActivity extends BaseActivity implements SensorEventListener {
    private static final String TAG = "CompassActivity";
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private ImageView compassImage;
    private float[] gravity;
    private float[] geomagnetic;
    private float azimuth = 0f;
    private float azimuthFix = 0f;
    private final float targetLatitude = 37.7749f; // Ejemplo: latitud destino
    private final float targetLongitude = -122.4194f; // Ejemplo: longitud destino

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = getLayoutInflater().inflate(R.layout.activity_compass, null);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(contentView);

        compassImage = findViewById(R.id.compass_image);

        // Set up sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        Log.d(TAG, "onResume: Sensors registered");
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        Log.d(TAG, "onPause: Sensors unregistered");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values;
            Log.d(TAG, "onSensorChanged: Accelerometer data - X: " + gravity[0] + " Y: " + gravity[1] + " Z: " + gravity[2]);
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values;
            Log.d(TAG, "onSensorChanged: Magnetic field data - X: " + geomagnetic[0] + " Y: " + geomagnetic[1] + " Z: " + geomagnetic[2]);
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

                Log.d(TAG, "onSensorChanged: Azimuth calculated - " + azimuth);
                updateCompass();
            }
        }
    }

    private float calculateBearingToTarget() {
        float bearing = (float) Math.toDegrees(Math.atan2(targetLongitude, targetLatitude));
        Log.d(TAG, "calculateBearingToTarget: Calculated bearing to target - " + bearing);
        return (bearing + 360) % 360;
    }

    private void updateCompass() {
        float rotation = azimuthFix - azimuth;
        compassImage.setRotation(rotation);
        Log.d(TAG, "updateCompass: Compass rotated to - " + rotation + " degrees");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Este método no es necesario para esta implementación
    }
}
