package dte.masteriot.mdp.asteroidconspiracist.entity;

import com.google.android.gms.maps.model.LatLng;

import java.util.Objects;

public class Shelter {
    private LatLng location;
    private String name;
    private String city;

    public Shelter(String name, String city, LatLng location) {
        this.name = name;
        this.city = city;
        this.location = location;
    }

    public LatLng getLocation() {
        return location;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shelter shelter = (Shelter) o;
        return location.equals(shelter.location) && name.equals(shelter.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, name);
    }

    @Override
    public String toString() {
        return "Shelter{" +
                "location=" + location +
                ", name='" + name + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}
