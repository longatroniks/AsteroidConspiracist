package dte.masteriot.mdp.asteroidconspiracist.models;

import java.util.List;

public class Asteroid {

    private final Long key;
    private final String name;
    private final String nameLimited;
    private final String designation;
    private final String nasaJplUrl;
    private final boolean isPotentiallyHazardous;
    private final double distance;
    private final double maxDiameter;
    private final double minDiameter;
    private final double maxDiameterMeters;
    private final double minDiameterMeters;
    private final double maxDiameterMiles;
    private final double minDiameterMiles;
    private final double maxDiameterFeet;
    private final double minDiameterFeet;
    private final double absoluteMagnitude;
    private final String orbitId;
    private final double semiMajorAxis;
    private final double velocity;
    private final List<CloseApproachData> closeApproachDataList;

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

    public static class CloseApproachData {
        private final String date;
        private final String dateFull;
        private final long epochDateCloseApproach;
        private final double missDistanceKilometers;
        private final double missDistanceAstronomical;
        private final double missDistanceLunar;
        private final double relativeVelocityKmPerSec;
        private final double relativeVelocityMilesPerHour;
        private final String orbitingBody;

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

    }
}
