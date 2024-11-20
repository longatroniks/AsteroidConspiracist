package dte.masteriot.mdp.asteroidconspiracist.entity;

import com.google.android.gms.maps.model.LatLng;

import java.util.Objects;

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

    // Equals method: Compare Observations based on location and description
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Observation that = (Observation) o;
        return location.equals(that.location) && description.equals(that.description);
    }

    // HashCode method: Generate hash code based on location and description
    @Override
    public int hashCode() {
        return Objects.hash(location, description);
    }
}
