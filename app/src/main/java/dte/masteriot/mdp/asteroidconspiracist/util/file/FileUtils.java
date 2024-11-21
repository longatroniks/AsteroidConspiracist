package dte.masteriot.mdp.asteroidconspiracist.util.file;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.entity.Observation;
import dte.masteriot.mdp.asteroidconspiracist.entity.Shelter;

public class FileUtils {

    private static final String OBSERVATION_FILE_NAME = "observations.json";
    private static final String SHELTER_FILE_NAME = "shelters.json";

    // Save observation to file
    public static void saveObservationToFile(Context context, LatLng location, String description, String timestamp, String cityName) {
        try {
            List<Observation> observations = loadObservationsFromFile(context);

            // Check for duplicates using Observation's equals method
            Observation newObservation = new Observation(location, description, timestamp, cityName);
            if (observations.contains(newObservation)) {
                Log.d("FileUtils", "Duplicate observation detected. Skipping save.");
                return;
            }

            // Add the new observation
            observations.add(newObservation);

            // Save updated list back to file
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

            try (OutputStream os = context.openFileOutput(OBSERVATION_FILE_NAME, Context.MODE_PRIVATE)) {
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

        try (InputStream is = context.openFileInput(OBSERVATION_FILE_NAME)) {
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

    // Save shelter to file
    public static void saveShelterToFile(Context context, LatLng location, String name, String city) {
        try {
            List<Shelter> shelters = loadSheltersFromFile(context);

            // Check for duplicates using Shelter's equals method
            Shelter newShelter = new Shelter(name, city, location);
            if (shelters.contains(newShelter)) {
                Log.d("FileUtils", "Duplicate shelter detected. Skipping save.");
                return;
            }

            // Add the new shelter
            shelters.add(newShelter);

            // Save updated list back to file
            JSONArray jsonArray = new JSONArray();
            for (Shelter shelter : shelters) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("latitude", shelter.getLocation().latitude);
                jsonObject.put("longitude", shelter.getLocation().longitude);
                jsonObject.put("name", shelter.getName());
                jsonObject.put("city", shelter.getCity());
                jsonArray.put(jsonObject);
            }

            try (OutputStream os = context.openFileOutput(SHELTER_FILE_NAME, Context.MODE_PRIVATE)) {
                os.write(jsonArray.toString().getBytes());
                Log.d("FileUtils", "Shelter saved to file.");
            }
        } catch (Exception e) {
            Log.e("FileUtils", "Error saving shelter", e);
        }
    }

    // Load shelters from file
    public static List<Shelter> loadSheltersFromFile(Context context) {
        List<Shelter> shelters = new ArrayList<>();

        try (InputStream is = context.openFileInput(SHELTER_FILE_NAME)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);

            String jsonString = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                double latitude = jsonObject.getDouble("latitude");
                double longitude = jsonObject.getDouble("longitude");
                String name = jsonObject.getString("name");
                String city = jsonObject.getString("city");

                LatLng location = new LatLng(latitude, longitude);
                Shelter shelter = new Shelter(name, city, location);
                shelters.add(shelter);
            }
            Log.d("FileUtils", "Loaded shelters from file.");
        } catch (Exception e) {
            Log.e("FileUtils", "Error loading shelters", e);
        }

        return shelters;
    }
}
