package dte.masteriot.mdp.asteroidconspiracist.util.json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.entity.Asteroid;

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
            Long id = asteroidJson.getLong("id");
            String name = asteroidJson.getString("name");
            String nameLimited = asteroidJson.optString("name_limited", "");
            String designation = asteroidJson.optString("designation", "");
            String nasaJplUrl = asteroidJson.optString("nasa_jpl_url", "");
            boolean isPotentiallyHazardous = asteroidJson.optBoolean("is_potentially_hazardous_asteroid", false);

            double maxDiameter = getDiameter(asteroidJson, "kilometers", "estimated_diameter_max");
            double minDiameter = getDiameter(asteroidJson, "kilometers", "estimated_diameter_min");
            double maxDiameterMeters = getDiameter(asteroidJson, "meters", "estimated_diameter_max");
            double minDiameterMeters = getDiameter(asteroidJson, "meters", "estimated_diameter_min");
            double maxDiameterMiles = getDiameter(asteroidJson, "miles", "estimated_diameter_max");
            double minDiameterMiles = getDiameter(asteroidJson, "miles", "estimated_diameter_min");
            double maxDiameterFeet = getDiameter(asteroidJson, "feet", "estimated_diameter_max");
            double minDiameterFeet = getDiameter(asteroidJson, "feet", "estimated_diameter_min");

            double absoluteMagnitude = asteroidJson.optDouble("absolute_magnitude_h", 0.0);

            JSONObject orbitalData = asteroidJson.optJSONObject("orbital_data");
            String orbitId = orbitalData != null ? orbitalData.optString("orbit_id") : null;
            double semiMajorAxis = orbitalData != null ? orbitalData.optDouble("semi_major_axis", 0.0) : 0.0;

            double velocity = getVelocity(asteroidJson);
            List<Asteroid.CloseApproachData> closeApproachDataList = getCloseApproachData(asteroidJson);

            return new Asteroid(id, name, nameLimited, designation, nasaJplUrl, isPotentiallyHazardous,
                    closeApproachDataList.get(0).getMissDistanceKilometers(), maxDiameter, minDiameter,
                    maxDiameterMeters, minDiameterMeters, maxDiameterMiles, minDiameterMiles,
                    maxDiameterFeet, minDiameterFeet, absoluteMagnitude, orbitId, semiMajorAxis,
                    velocity, closeApproachDataList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static double getDiameter(JSONObject asteroidJson, String unit, String type) {
        try {
            return asteroidJson.getJSONObject("estimated_diameter").getJSONObject(unit).getDouble(type);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private static double getVelocity(JSONObject asteroidJson) {
        try {
            JSONArray approachData = asteroidJson.getJSONArray("close_approach_data");
            if (approachData.length() > 0) {
                JSONObject relativeVelocity = approachData.getJSONObject(0).getJSONObject("relative_velocity");
                return relativeVelocity.getDouble("kilometers_per_hour");
            }
            return 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private static List<Asteroid.CloseApproachData> getCloseApproachData(JSONObject asteroidJson) {
        List<Asteroid.CloseApproachData> closeApproachDataList = new ArrayList<>();
        try {
            JSONArray approachDataArray = asteroidJson.getJSONArray("close_approach_data");
            for (int i = 0; i < approachDataArray.length(); i++) {
                JSONObject approachData = approachDataArray.getJSONObject(i);
                String date = approachData.optString("close_approach_date", "");
                String dateFull = approachData.optString("close_approach_date_full", "");
                long epochDateCloseApproach = approachData.optLong("epoch_date_close_approach", 0);

                double missDistanceKilometers = approachData.getJSONObject("miss_distance").optDouble("kilometers", 0.0);
                double missDistanceAstronomical = approachData.getJSONObject("miss_distance").optDouble("astronomical", 0.0);
                double missDistanceLunar = approachData.getJSONObject("miss_distance").optDouble("lunar", 0.0);

                double relativeVelocityKmPerSec = approachData.getJSONObject("relative_velocity").optDouble("kilometers_per_second", 0.0);
                double relativeVelocityMilesPerHour = approachData.getJSONObject("relative_velocity").optDouble("miles_per_hour", 0.0);

                String orbitingBody = approachData.optString("orbiting_body", "");

                closeApproachDataList.add(new Asteroid.CloseApproachData(date, dateFull, epochDateCloseApproach,
                        missDistanceKilometers, missDistanceAstronomical, missDistanceLunar,
                        relativeVelocityKmPerSec, relativeVelocityMilesPerHour, orbitingBody));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return closeApproachDataList;
    }
}
