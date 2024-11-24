package dte.masteriot.mdp.asteroidconspiracist.entity;

import java.util.ArrayList;
import java.util.List;

public class Asteroid {

    private Long key;
    private String name;
    private String nameLimited;
    private String designation;
    private String nasaJplUrl;
    private boolean isPotentiallyHazardous;
    private double distance;
    private double maxDiameter;
    private double minDiameter;
    private double maxDiameterMeters;
    private double minDiameterMeters;
    private double maxDiameterMiles;
    private double minDiameterMiles;
    private double maxDiameterFeet;
    private double minDiameterFeet;
    private double absoluteMagnitude;
    private String orbitId;
    private double semiMajorAxis;
    private double velocity;
    private List<CloseApproachData> closeApproachDataList;

    public Asteroid() {
        this.key = null;
        this.name = null;
        this.nameLimited = null;
        this.designation = null;
        this.nasaJplUrl = null;
        this.isPotentiallyHazardous = false;
        this.distance = 0.0;
        this.maxDiameter = 0.0;
        this.minDiameter = 0.0;
        this.maxDiameterMeters = 0.0;
        this.minDiameterMeters = 0.0;
        this.maxDiameterMiles = 0.0;
        this.minDiameterMiles = 0.0;
        this.maxDiameterFeet = 0.0;
        this.minDiameterFeet = 0.0;
        this.absoluteMagnitude = 0.0;
        this.orbitId = null;
        this.semiMajorAxis = 0.0;
        this.velocity = 0.0;
        this.closeApproachDataList = new ArrayList<>();
    }

    public Asteroid(Long key, String name, String nameLimited, String designation, String nasaJplUrl,
                    boolean isPotentiallyHazardous, double distance, double maxDiameter, double minDiameter,
                    double maxDiameterMeters, double minDiameterMeters, double maxDiameterMiles, double minDiameterMiles,
                    double maxDiameterFeet, double minDiameterFeet, double absoluteMagnitude, String orbitId,
                    double semiMajorAxis, double velocity, List<CloseApproachData> closeApproachDataList) {
        this.key = key;
        this.name = name;
        this.nameLimited = nameLimited;
        this.designation = designation;
        this.nasaJplUrl = nasaJplUrl;
        this.isPotentiallyHazardous = isPotentiallyHazardous;
        this.distance = distance;
        this.maxDiameter = maxDiameter;
        this.minDiameter = minDiameter;
        this.maxDiameterMeters = maxDiameterMeters;
        this.minDiameterMeters = minDiameterMeters;
        this.maxDiameterMiles = maxDiameterMiles;
        this.minDiameterMiles = minDiameterMiles;
        this.maxDiameterFeet = maxDiameterFeet;
        this.minDiameterFeet = minDiameterFeet;
        this.absoluteMagnitude = absoluteMagnitude;
        this.orbitId = orbitId;
        this.semiMajorAxis = semiMajorAxis;
        this.velocity = velocity;
        this.closeApproachDataList = closeApproachDataList;
    }

    public Long getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getNasaJplUrl() {
        return nasaJplUrl;
    }

    public boolean isPotentiallyHazardous() {
        return isPotentiallyHazardous;
    }

    public double getDistance() {
        return distance;
    }

    public double getMaxDiameter() {
        return maxDiameter;
    }

    public double getMinDiameter() {
        return minDiameter;
    }

    public double getMaxDiameterMeters() {
        return maxDiameterMeters;
    }

    public double getMinDiameterMeters() {
        return minDiameterMeters;
    }

    public double getAbsoluteMagnitude() {
        return absoluteMagnitude;
    }

    public String getOrbitId() {
        return orbitId;
    }

    public double getSemiMajorAxis() {
        return semiMajorAxis;
    }

    public double getVelocity() {
        return velocity;
    }

    public List<CloseApproachData> getCloseApproachData() {
        return closeApproachDataList;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setNasaJplUrl(String nasaJplUrl) {
        this.nasaJplUrl = nasaJplUrl;
    }

    public void setPotentiallyHazardous(boolean potentiallyHazardous) {
        isPotentiallyHazardous = potentiallyHazardous;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setMaxDiameter(double maxDiameter) {
        this.maxDiameter = maxDiameter;
    }

    public void setMinDiameter(double minDiameter) {
        this.minDiameter = minDiameter;
    }

    public void setAbsoluteMagnitude(double absoluteMagnitude) {
        this.absoluteMagnitude = absoluteMagnitude;
    }

    public void setOrbitId(String orbitId) {
        this.orbitId = orbitId;
    }

    public void setSemiMajorAxis(double semiMajorAxis) {
        this.semiMajorAxis = semiMajorAxis;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    // Nested CloseApproachData class
    public static class CloseApproachData {
        private String date;
        private String dateFull;
        private long epochDateCloseApproach;
        private double missDistanceKilometers;
        private double missDistanceAstronomical;
        private double missDistanceLunar;
        private double relativeVelocityKmPerSec;
        private double relativeVelocityMilesPerHour;
        private String orbitingBody;

        public CloseApproachData(String date, String dateFull, long epochDateCloseApproach, double missDistanceKilometers,
                                 double missDistanceAstronomical, double missDistanceLunar, double relativeVelocityKmPerSec,
                                 double relativeVelocityMilesPerHour, String orbitingBody) {
            this.date = date;
            this.dateFull = dateFull;
            this.epochDateCloseApproach = epochDateCloseApproach;
            this.missDistanceKilometers = missDistanceKilometers;
            this.missDistanceAstronomical = missDistanceAstronomical;
            this.missDistanceLunar = missDistanceLunar;
            this.relativeVelocityKmPerSec = relativeVelocityKmPerSec;
            this.relativeVelocityMilesPerHour = relativeVelocityMilesPerHour;
            this.orbitingBody = orbitingBody;
        }

        public String getDate() {
            return date;
        }

        public double getMissDistanceKilometers() {
            return missDistanceKilometers;
        }

        public double getMissDistanceAstronomical() {
            return missDistanceAstronomical;
        }

        public double getMissDistanceLunar() {
            return missDistanceLunar;
        }

        public double getRelativeVelocityKmPerSec() {
            return relativeVelocityKmPerSec;
        }

        public double getRelativeVelocityMilesPerHour() {
            return relativeVelocityMilesPerHour;
        }

        public String getOrbitingBody() {
            return orbitingBody;
        }
    }
}
