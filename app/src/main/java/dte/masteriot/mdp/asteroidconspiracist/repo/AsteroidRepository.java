package dte.masteriot.mdp.asteroidconspiracist.repo;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.entity.Asteroid;
import dte.masteriot.mdp.asteroidconspiracist.service.NeoWsAPIService;
import dte.masteriot.mdp.asteroidconspiracist.util.json.AsteroidParser;

public class AsteroidRepository {

    private static final String TAG = "AsteroidRepository";
    private static AsteroidRepository instance;
    private final NeoWsAPIService apiService; // Centralized API service
    private List<Asteroid> asteroidList; // Null indicates data not loaded
    private static final String ASTEROID_CACHE_FILE = "asteroids_cache.json";

    public interface FetchCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // Private constructor for singleton
    private AsteroidRepository() {
        this.apiService = new NeoWsAPIService(); // Instantiate the API service here
        this.asteroidList = null; // Data is not loaded initially
    }

    // Singleton instance
    public static synchronized AsteroidRepository getInstance() {
        if (instance == null) {
            instance = new AsteroidRepository();
        }
        return instance;
    }

    // Set asteroid list (typically after fetching)
    public void setAsteroidList(List<Asteroid> asteroids) {
        this.asteroidList = asteroids;
    }

    // Get asteroid list
    public List<Asteroid> getAsteroidList() {
        return asteroidList;
    }

    // Save asteroid data to local cache
    public void saveAsteroidsToLocalCache(Context context, String jsonData) {
        try (FileOutputStream fos = context.openFileOutput(ASTEROID_CACHE_FILE, Context.MODE_PRIVATE)) {
            fos.write(jsonData.getBytes());
            Log.d(TAG, "Asteroid data cached to local storage.");
        } catch (IOException e) {
            Log.e(TAG, "Error caching asteroid data: " + e.getMessage());
        }
    }

    // Load asteroid data from local cache
    public List<Asteroid> loadAsteroidsFromLocalCache(Context context) {
        File file = new File(context.getFilesDir(), ASTEROID_CACHE_FILE);
        if (!file.exists()) {
            Log.d(TAG, "No cached asteroid data found.");
            return new ArrayList<>();
        }

        try (FileInputStream fis = context.openFileInput(ASTEROID_CACHE_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            Log.d(TAG, "Asteroid data loaded from local cache.");
            return AsteroidParser.parseAsteroids(jsonContent.toString());
        } catch (IOException e) {
            Log.e(TAG, "Error loading asteroid data from cache: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Fetch asteroids from API or cache based on network availability
    public void fetchAsteroids(Context context, boolean isNetworkAvailable, FetchCallback callback) {
        if (isNetworkAvailable) {
            apiService.fetchAsteroids(new NeoWsAPIService.NeoWsAPIResponse() {
                @Override
                public void onResponse(String jsonResponse) {
                    List<Asteroid> asteroidList = AsteroidParser.parseAsteroids(jsonResponse);
                    if (!asteroidList.isEmpty()) {
                        setAsteroidList(asteroidList); // Save to in-memory list
                        saveAsteroidsToLocalCache(context, jsonResponse); // Save to cache
                        callback.onSuccess(); // Notify the caller
                    } else {
                        callback.onFailure("No asteroid data available from API.");
                    }
                }

                @Override
                public void onError(String error) {
                    callback.onFailure(error); // Forward the error
                }
            });
        } else {
            List<Asteroid> cachedAsteroids = loadAsteroidsFromLocalCache(context);
            if (cachedAsteroids != null && !cachedAsteroids.isEmpty()) {
                setAsteroidList(cachedAsteroids); // Save to in-memory list
                callback.onSuccess(); // Notify the caller
            } else {
                callback.onFailure("No internet connection and no local cache available.");
            }
        }
    }
}
