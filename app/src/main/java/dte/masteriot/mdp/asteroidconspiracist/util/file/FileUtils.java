package dte.masteriot.mdp.asteroidconspiracist.utils.file;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.entities.Observation;

public class FileUtils {

    private static final String FILE_NAME = "observations.json";

    // Save observation to file
    public static void saveObservationToFile(Context context, LatLng location, String description, String timestamp, String cityName) {
        try {
            List<Observation> observations = loadObservationsFromFile(context);
            observations.add(new Observation(location, description, timestamp, cityName));

            JSONArray jsonArray = new JSONArray();
            for (Observation obs : observations) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("latitude", obs.getLocation().latitude);
                jsonObject.put("longitude", obs.getLocation().longitude);
                jsonObject.put("description", obs.getDescription());
                jsonObject.put("timestamp", obs.getTimestamp());
                jsonObject.put("cityName", obs.getCityName());
                jsonArray.put(jsonObject);
            }

            try (OutputStream os = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
                os.write(jsonArray.toString().getBytes());
                Log.d("FileUtils", "Observation saved to file.");
            }
        } catch (Exception e) {
            Log.e("FileUtils", "Error saving observation", e);
        }
    }

    // Load observations from file
    public static List<Observation> loadObservationsFromFile(Context context) {
        List<Observation> observations = new ArrayList<>();

        try (InputStream is = context.openFileInput(FILE_NAME)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);

            String jsonString = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                double latitude = jsonObject.getDouble("latitude");
                double longitude = jsonObject.getDouble("longitude");
                String description = jsonObject.getString("description");
                String timestamp = jsonObject.getString("timestamp");
                String cityName = jsonObject.getString("cityName");

                LatLng location = new LatLng(latitude, longitude);
                Observation obs = new Observation(location, description, timestamp, cityName);
                observations.add(obs);
            }
            Log.d("FileUtils", "Loaded observations from file.");
        } catch (Exception e) {
            Log.e("FileUtils", "Error loading observations", e);
        }

        return observations;
    }
}
