package dte.masteriot.mdp.asteroidconspiracist.services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.models.Asteroid;
import dte.masteriot.mdp.asteroidconspiracist.utils.AsteroidParser;
import dte.masteriot.mdp.asteroidconspiracist.repos.AsteroidRepository;

public class NeoWsAPIService {
    private static final String TAG = "NeoWsAPIClient";
    private static final String API_KEY = "18EanFtDWajsNEJbRjwXJdVKF96O0t1N0MshB3UF";
    private static final String API_URL = "https://api.nasa.gov/neo/rest/v1/neo/browse";
    private static final String JSON_FILE_NAME = "asteroids_data.json";
    private static final int PAGE_SIZE = 10;
    private static final int MAX_RETRIES = 3;

    public interface NeoWsAPIResponse {
        void onResponse(boolean isFromCache);  // Modify callback to indicate data source
        void onError(String error);
    }

    public void fetchAndStoreAsteroids(Context context, NeoWsAPIResponse callback) {
        new FetchAsteroidsTask(context, callback).execute();
    }

    private static class FetchAsteroidsTask extends AsyncTask<Void, Void, List<Asteroid>> {
        private final Context context;
        private final NeoWsAPIResponse callback;
        private int retryCount = 0;

        public FetchAsteroidsTask(Context context, NeoWsAPIResponse callback) {
            this.context = context;
            this.callback = callback;
        }

        @Override
        protected List<Asteroid> doInBackground(Void... voids) {
            while (retryCount < MAX_RETRIES) {
                try {
                    String urlWithParams = String.format("%s?api_key=%s&size=%d", API_URL, API_KEY, PAGE_SIZE);
                    URL url = new URL(urlWithParams);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.connect();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder content = new StringBuilder();
                        String inputLine;

                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }

                        in.close();
                        conn.disconnect();
                        Log.d(TAG, "Successfully fetched " + PAGE_SIZE + " asteroids");

                        // Save JSON to file
                        saveJsonToFile(context, content.toString());

                        // Parse and return asteroid data
                        return AsteroidParser.parseAsteroids(content.toString());
                    } else {
                        Log.e(TAG, "Error: Unable to fetch data. Response Code: " + responseCode);
                    }
                } catch (Exception e) {
                    retryCount++;
                    Log.e(TAG, "Retry " + retryCount + " due to error: " + e.getMessage());
                    if (retryCount == MAX_RETRIES) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Asteroid> asteroids) {
            if (asteroids != null && !asteroids.isEmpty()) {
                AsteroidRepository.getInstance().setAsteroidList(asteroids);
                callback.onResponse(false);  // Indicate data is from online fetch
            } else {
                List<Asteroid> storedAsteroids = loadAsteroidsFromJsonFile(context);
                if (storedAsteroids != null && !storedAsteroids.isEmpty()) {
                    AsteroidRepository.getInstance().setAsteroidList(storedAsteroids);
                    callback.onResponse(true);  // Indicate data is from cache
                } else {
                    callback.onError("Failed to fetch asteroids and no cached data available.");
                }
            }
        }

        // Method to save JSON data to a file
        private void saveJsonToFile(Context context, String jsonString) {
            try (FileOutputStream fos = context.openFileOutput(JSON_FILE_NAME, Context.MODE_PRIVATE)) {
                fos.write(jsonString.getBytes());
                Log.d(TAG, "Asteroid data saved to file.");
            } catch (Exception e) {
                Log.e(TAG, "Error saving JSON to file: " + e.getMessage());
            }
        }

        // Method to load JSON data from a file
        private List<Asteroid> loadAsteroidsFromJsonFile(Context context) {
            File file = new File(context.getFilesDir(), JSON_FILE_NAME);
            if (!file.exists()) {
                Log.d(TAG, "No cached JSON file found.");
                return null;
            }

            try (FileInputStream fis = context.openFileInput(JSON_FILE_NAME);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
                StringBuilder jsonContent = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }

                Log.d(TAG, "Asteroid data loaded from file.");
                return AsteroidParser.parseAsteroids(jsonContent.toString());
            } catch (Exception e) {
                Log.e(TAG, "Error reading JSON from file: " + e.getMessage());
                return null;
            }
        }
    }
}
