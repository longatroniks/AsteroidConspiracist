package dte.masteriot.mdp.asteroidconspiracist.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dte.masteriot.mdp.asteroidconspiracist.entity.Asteroid;

public class AsteroidCalculationHelper {

    public int calculateThreatScore(Asteroid asteroid) {
        double distanceWeight = 0.3;
        double velocityWeight = 0.2;
        double diameterWeight = 0.2;
        double magnitudeWeight = 0.1;
        double hazardWeight = 0.1;
        double closeApproachWeight = 0.1;

        double distanceScore = 1 - normalize(asteroid.getDistance(), 0, 10_000_000);
        double velocityScore = normalize(asteroid.getVelocity(), 0, 50_000);
        double diameterScore = normalize(asteroid.getMaxDiameterMeters(), 0, 10_000);
        double magnitudeScore = normalize(30 - asteroid.getAbsoluteMagnitude(), 0, 30);
        double hazardScore = asteroid.isPotentiallyHazardous() ? 0.1 : 0;

        double closestApproachDistance = Double.MAX_VALUE;
        double highestRelativeVelocity = 0;
        for (Asteroid.CloseApproachData approachData : asteroid.getCloseApproachData()) {
            closestApproachDistance = Math.min(closestApproachDistance, approachData.getMissDistanceKilometers());
            highestRelativeVelocity = Math.max(highestRelativeVelocity, approachData.getRelativeVelocityKmPerSec());
        }

        double closeApproachScore = (1 - normalize(closestApproachDistance, 0, 10_000_000)) * 0.5 +
                normalize(highestRelativeVelocity, 0, 50_000) * 0.5;

        double rawScore = (distanceScore * distanceWeight) + (velocityScore * velocityWeight) +
                (diameterScore * diameterWeight) + (magnitudeScore * magnitudeWeight) +
                (hazardScore * hazardWeight) + (closeApproachScore * closeApproachWeight);

        return (int) Math.min(100, Math.max(1, rawScore * 100));
    }

    public Map<Asteroid, Float> calculateThreatPercentages(List<Asteroid> asteroids) {
        Map<Asteroid, Float> threatPercentages = new HashMap<>();
        int totalScore = 0;

        for (Asteroid asteroid : asteroids) {
            int threatScore = calculateThreatScore(asteroid);
            totalScore += threatScore;
            threatPercentages.put(asteroid, (float) threatScore);
        }

        float totalPercentage = 0f;
        for (Asteroid asteroid : threatPercentages.keySet()) {
            float percentage = (threatPercentages.get(asteroid) / totalScore) * 100;
            percentage = Math.round(percentage * 10) / 10f;
            threatPercentages.put(asteroid, percentage);
            totalPercentage += percentage;
        }

        float roundingDifference = 100f - totalPercentage;
        if (roundingDifference != 0) {
            Asteroid maxAsteroid = threatPercentages.entrySet().stream()
                    .max(Map.Entry.comparingByValue()).get().getKey();
            threatPercentages.put(maxAsteroid, threatPercentages.get(maxAsteroid) + roundingDifference);
        }

        return threatPercentages;
    }

    public double normalize(double value, double min, double max) {
        if (max == min) return 1;
        return (value - min) / (max - min);
    }

    public Asteroid getHighestThreatAsteroid(List<Asteroid> asteroids) {
        return asteroids.isEmpty() ? null : asteroids.stream().max((a, b) -> Integer.compare(calculateThreatScore(a), calculateThreatScore(b))).orElse(null);
    }
}
