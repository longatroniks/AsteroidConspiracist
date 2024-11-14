package dte.masteriot.mdp.asteroidconspiracist.services;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NeoWsAPIService {

    private static final String TAG = "NeoWsAPIClient";
    private static final String API_KEY = "18EanFtDWajsNEJbRjwXJdVKF96O0t1N0MshB3UF";
    private static final String API_URL = "https://api.nasa.gov/neo/rest/v1/neo/browse";
    private static final int PAGE_SIZE = 10;
    private static final int MAX_RETRIES = 3;

    public interface NeoWsAPIResponse {
        void onResponse(String response);
        void onError(String error);
    }

    public void getAsteroids(NeoWsAPIResponse callback) {
        new FetchAsteroidsTask(callback).execute();
    }

    private static class FetchAsteroidsTask extends AsyncTask<Void, Void, String> {
        private final NeoWsAPIResponse callback;
        private int retryCount = 0;

        public FetchAsteroidsTask(NeoWsAPIResponse callback) {
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... voids) {
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
                        return content.toString();
                    } else {
                        Log.e(TAG, "Error: Unable to fetch data. Response Code: " + responseCode);
                        return null;
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
        protected void onPostExecute(String result) {
            if (result != null) {
                callback.onResponse(result);
            } else {
                callback.onError("Failed to fetch asteroids after " + MAX_RETRIES + " attempts");
            }
        }
    }
}
