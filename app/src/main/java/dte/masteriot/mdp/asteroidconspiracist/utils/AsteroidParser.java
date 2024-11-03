package dte.masteriot.mdp.asteroidconspiracist.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.models.Asteroid;

public class AsteroidParser {

    public static List<Asteroid> parseAsteroids(String jsonResponse) {
        List<Asteroid> asteroidList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray nearEarthObjects = jsonObject.getJSONArray("near_earth_objects");

            for (int i = 0; i < nearEarthObjects.length(); i++) {
                JSONObject asteroidJson = nearEarthObjects.getJSONObject(i);
                Asteroid asteroid = parseAsteroid(asteroidJson);
                if (asteroid != null) {
                    asteroidList.add(asteroid);
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing JSON. Response was: " + jsonResponse);
            e.printStackTrace();
        }
        return asteroidList;
    }

    private static Asteroid parseAsteroid(JSONObject asteroidJson) {
        try {
            Long id = Long.parseLong(asteroidJson.getString("id"));
            String name = asteroidJson.getString("name");

            // Get the estimated diameter
            double maxDiameter = getMaxDiameter(asteroidJson);

            // Get the closest approach distance
            double distance = getClosestApproachDistance(asteroidJson);

            return new Asteroid(id, name, distance, maxDiameter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static double getMaxDiameter(JSONObject asteroidJson) {
        try {
            JSONObject diameter = asteroidJson.getJSONObject("estimated_diameter")
                    .getJSONObject("kilometers");
            return diameter.getDouble("estimated_diameter_max");
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private static double getClosestApproachDistance(JSONObject asteroidJson) {
        try {
            JSONArray approachData = asteroidJson.getJSONArray("close_approach_data");
            if (approachData.length() > 0) {
                return approachData.getJSONObject(0)
                        .getJSONObject("miss_distance")
                        .getDouble("kilometers");
            }
            return 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}