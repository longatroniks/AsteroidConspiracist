package dte.masteriot.mdp.asteroidconspiracist.util.file;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.entity.Observation;
import dte.masteriot.mdp.asteroidconspiracist.entity.Shelter;

public class FileUtils {

    private static final String TAG = "FileUtils";
    private static final String OBSERVATION_FILE_NAME = "observations.json";
    private static final String SHELTER_FILE_NAME = "shelters.json";

    public static void saveObservationToFile(Context context, LatLng location, String description, String timestamp, String cityName) {
        try {
            List<Observation> observations = loadObservationsFromFile(context);

            Observation newObservation = new Observation(location, description, timestamp, cityName);
            if (observations.contains(newObservation)) {
                Log.d(TAG, "Duplicate observation detected. Skipping save.");
                return;
            }

            observations.add(newObservation);

            writeJsonToFile(context, OBSERVATION_FILE_NAME, convertObservationsToJsonArray(observations));
            Log.d(TAG, "Observation saved to file.");
        } catch (Exception e) {
            Log.e(TAG, "Error saving observation", e);
        }
    }

    public static List<Observation> loadObservationsFromFile(Context context) {
        List<Observation> observations = new ArrayList<>();
        try {
            JSONArray jsonArray = readJsonArrayFromFile(context, OBSERVATION_FILE_NAME);
            if (jsonArray == null) return observations;

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                double latitude = jsonObject.getDouble("latitude");
                double longitude = jsonObject.getDouble("longitude");
                String description = jsonObject.getString("description");
                String timestamp = jsonObject.getString("timestamp");
                String cityName = jsonObject.getString("cityName");

                observations.add(new Observation(new LatLng(latitude, longitude), description, timestamp, cityName));
            }
            Log.d(TAG, "Loaded observations from file.");
        } catch (Exception e) {
            Log.e(TAG, "Error loading observations", e);
        }
        return observations;
    }

    public static void saveShelterToFile(Context context, LatLng location, String name, String city) {
        try {
            List<Shelter> shelters = loadSheltersFromFile(context);

            Shelter newShelter = new Shelter(name, city, location);
            if (shelters.contains(newShelter)) {
                Log.d(TAG, "Duplicate shelter detected. Skipping save.");
                return;
            }

            shelters.add(newShelter);

            writeJsonToFile(context, SHELTER_FILE_NAME, convertSheltersToJsonArray(shelters));
            Log.d(TAG, "Shelter saved to file.");
        } catch (Exception e) {
            Log.e(TAG, "Error saving shelter", e);
        }
    }

    public static List<Shelter> loadSheltersFromFile(Context context) {
        List<Shelter> shelters = new ArrayList<>();
        try {
            JSONArray jsonArray = readJsonArrayFromFile(context, SHELTER_FILE_NAME);
            if (jsonArray == null) return shelters;

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                double latitude = jsonObject.getDouble("latitude");
                double longitude = jsonObject.getDouble("longitude");
                String name = jsonObject.getString("name");
                String city = jsonObject.getString("city");

                shelters.add(new Shelter(name, city, new LatLng(latitude, longitude)));
            }
            Log.d(TAG, "Loaded shelters from file.");
        } catch (Exception e) {
            Log.e(TAG, "Error loading shelters", e);
        }
        return shelters;
    }

    private static JSONArray convertObservationsToJsonArray(List<Observation> observations) throws JSONException {
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
        return jsonArray;
    }

    private static JSONArray convertSheltersToJsonArray(List<Shelter> shelters) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (Shelter shelter : shelters) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("latitude", shelter.getLocation().latitude);
            jsonObject.put("longitude", shelter.getLocation().longitude);
            jsonObject.put("name", shelter.getName());
            jsonObject.put("city", shelter.getCity());
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    private static void writeJsonToFile(Context context, String fileName, JSONArray jsonArray) throws Exception {
        try (OutputStream os = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            os.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    private static JSONArray readJsonArrayFromFile(Context context, String fileName) {
        try (InputStream is = context.openFileInput(fileName)) {
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            return new JSONArray(jsonString);
        } catch (Exception e) {
            Log.e(TAG, "Error reading JSON array from file: " + fileName, e);
            return null;
        }
    }
}
