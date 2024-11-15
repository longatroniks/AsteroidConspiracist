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

    // No-argument constructor
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

    // Constructor with all fields
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

    // Getters
    public Long getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getNameLimited() {
        return nameLimited;
    }

    public String getDesignation() {
        return designation;
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

    public double getMaxDiameterMiles() {
        return maxDiameterMiles;
    }

    public double getMinDiameterMiles() {
        return minDiameterMiles;
    }

    public double getMaxDiameterFeet() {
        return maxDiameterFeet;
    }

    public double getMinDiameterFeet() {
        return minDiameterFeet;
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

    // Setters
    public void setKey(Long key) {
        this.key = key;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNameLimited(String nameLimited) {
        this.nameLimited = nameLimited;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
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

    public void setMaxDiameterMeters(double maxDiameterMeters) {
        this.maxDiameterMeters = maxDiameterMeters;
    }

    public void setMinDiameterMeters(double minDiameterMeters) {
        this.minDiameterMeters = minDiameterMeters;
    }

    public void setMaxDiameterMiles(double maxDiameterMiles) {
        this.maxDiameterMiles = maxDiameterMiles;
    }

    public void setMinDiameterMiles(double minDiameterMiles) {
        this.minDiameterMiles = minDiameterMiles;
    }

    public void setMaxDiameterFeet(double maxDiameterFeet) {
        this.maxDiameterFeet = maxDiameterFeet;
    }

    public void setMinDiameterFeet(double minDiameterFeet) {
        this.minDiameterFeet = minDiameterFeet;
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

    public void setCloseApproachDataList(List<CloseApproachData> closeApproachDataList) {
        this.closeApproachDataList = closeApproachDataList;
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

        // Getters
        public String getDate() {
            return date;
        }

        public String getDateFull() {
            return dateFull;
        }

        public long getEpochDateCloseApproach() {
            return epochDateCloseApproach;
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

        // Setters
        public void setDate(String date) {
            this.date = date;
        }

        public void setDateFull(String dateFull) {
            this.dateFull = dateFull;
        }

        public void setEpochDateCloseApproach(long epochDateCloseApproach) {
            this.epochDateCloseApproach = epochDateCloseApproach;
        }

        public void setMissDistanceKilometers(double missDistanceKilometers) {
            this.missDistanceKilometers = missDistanceKilometers;
        }

        public void setMissDistanceAstronomical(double missDistanceAstronomical) {
            this.missDistanceAstronomical = missDistanceAstronomical;
        }

        public void setMissDistanceLunar(double missDistanceLunar) {
            this.missDistanceLunar = missDistanceLunar;
        }

        public void setRelativeVelocityKmPerSec(double relativeVelocityKmPerSec) {
            this.relativeVelocityKmPerSec = relativeVelocityKmPerSec;
        }

        public void setRelativeVelocityMilesPerHour(double relativeVelocityMilesPerHour) {
            this.relativeVelocityMilesPerHour = relativeVelocityMilesPerHour;
        }

        public void setOrbitingBody(String orbitingBody) {
            this.orbitingBody = orbitingBody;
        }
    }
}
