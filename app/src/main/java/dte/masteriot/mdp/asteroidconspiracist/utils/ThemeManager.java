package dte.masteriot.mdp.asteroidconspiracist.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class ThemeManager {

    private static boolean isDarkMode = false;
    private static boolean initialized = false;
    private static SensorManager sensorManager;
    private static Sensor lightSensor;

    private static final String TAG = "ThemeManager";

    public static void initialize(Context context) {
        if (initialized) return;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor != null) {
            // Ejecuta un Runnable en un hilo separado para monitorear el sensor de luz
            new Thread(new LightSensorRunnable()).start();
            initialized = true;
        }
    }

    private static void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
        Log.d(TAG, "Modo actual: " + (isDarkMode ? "Dark Mode" : "Light Mode"));
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    // Runnable que monitorea el sensor de luz en intervalos
    private static class LightSensorRunnable implements Runnable, SensorEventListener {

        @Override
        public void run() {
            // Registra el listener del sensor
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float lightIntensity = event.values[0];
            boolean newMode = lightIntensity < 100; // Si la luz es baja, activa modo oscuro

            // Llama a setDarkMode y registra el estado actual en cada lectura
            setDarkMode(newMode);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // No se utiliza
        }
    }
}
