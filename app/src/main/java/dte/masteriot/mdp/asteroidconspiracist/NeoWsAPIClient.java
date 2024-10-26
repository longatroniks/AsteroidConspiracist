package dte.masteriot.mdp.asteroidconspiracist;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NeoWsAPIClient {
    private static final String TAG = "NeoWsAPIClient";
    private static final String API_KEY = "18EanFtDWajsNEJbRjwXJdVKF96O0t1N0MshB3UF";
    private static final String API_URL = "https://api.nasa.gov/neo/rest/v1/neo/browse";
    private static final int PAGE_SIZE = 10;

    public String getAsteroids() {
        try {
            // Build URL with parameters
            String urlWithParams = String.format("%s?api_key=%s&size=%d", API_URL, API_KEY, PAGE_SIZE);
            URL url = new URL(urlWithParams);

            // Configure connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000); // 5 second timeout for connection
            conn.setReadTimeout(5000);    // 5 second timeout for reading
            conn.connect();

            // Check for successful response
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Error: Unable to fetch data. Response Code: " + responseCode);
                return null;
            }

            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            // Close resources
            in.close();
            conn.disconnect();

            // Log response for debugging (consider removing in production)
            Log.d(TAG, "Successfully fetched " + PAGE_SIZE + " asteroids");

            return content.toString();

        } catch (Exception e) {
            Log.e(TAG, "Error fetching asteroids: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
