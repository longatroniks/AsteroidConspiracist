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

    public LatLng getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Observation that = (Observation) o;
        return location.equals(that.location) && description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, description);
    }
}
