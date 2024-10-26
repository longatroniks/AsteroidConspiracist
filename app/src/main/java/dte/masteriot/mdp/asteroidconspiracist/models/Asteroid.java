package dte.masteriot.mdp.asteroidconspiracist.models;

public class Asteroid {
    private Long key; // Unique identifier for the asteroid
    private String name; // Name of the asteroid
    private double distance; // Distance from Earth (in kilometers or miles)
    private double diameter; // Estimated diameter of the asteroid

    // Constructor
    public Asteroid(Long key, String name, double distance, double diameter) {
        this.key = key;
        this.name = name;
        this.distance = distance;
        this.diameter = diameter;
    }

    // Getters
    public Long getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public double getDistance() {
        return distance;
    }

    public double getDiameter() {
        return diameter;
    }
}
