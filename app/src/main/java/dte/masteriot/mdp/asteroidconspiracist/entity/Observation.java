package dte.masteriot.mdp.asteroidconspiracist.entity;

import com.google.android.gms.maps.model.LatLng;

public class Observation {
    private LatLng location;
    private String description;
    private String timestamp;
    private String cityName;

    public Observation(LatLng location, String description, String timestamp, String cityName) {
        this.location = location;
        this.description = description;
        this.timestamp = timestamp;
        this.cityName = cityName;
    }

    // Getter and setter methods
    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    // Method to return a formatted string of observation details
    public String getFormattedInfo() {
        return "Description: " + description + "\n" +
                "Time: " + timestamp + "\n" +
                "Location: " + cityName;
    }
}
