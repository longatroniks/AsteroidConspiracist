package dte.masteriot.mdp.asteroidconspiracist.repos;

import android.content.Context;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.entities.Asteroid;
import dte.masteriot.mdp.asteroidconspiracist.utils.AsteroidParser;

public class AsteroidRepository {

    private static AsteroidRepository instance;
    private List<Asteroid> asteroidList;
    private static final String ASTEROID_CACHE_FILE = "asteroids_cache.json";

    private AsteroidRepository() {
        asteroidList = new ArrayList<>();
    }

    // Singleton pattern to get a single instance of this class
    public static synchronized AsteroidRepository getInstance() {
        if (instance == null) {
            instance = new AsteroidRepository();
        }
        return instance;
    }

    // Method to update asteroid data
    public void setAsteroidList(List<Asteroid> asteroids) {
        this.asteroidList = asteroids;
    }

    // Method to retrieve asteroid data
    public List<Asteroid> getAsteroidList() {
        return asteroidList;
    }

    // Method to fetch an asteroid by its key
    public Asteroid getAsteroidById(long id) {
        for (Asteroid asteroid : asteroidList) {
            if (asteroid.getKey() == id) {
                return asteroid;
            }
        }
        return null;
    }

    // Save asteroid data as JSON to local storage
    public void saveAsteroidsToLocalCache(Context context, String jsonData) {
        try (FileOutputStream fos = context.openFileOutput(ASTEROID_CACHE_FILE, Context.MODE_PRIVATE)) {
            fos.write(jsonData.getBytes());
            System.out.println("Asteroid data cached to local storage.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load asteroid data from local cache
    public List<Asteroid> loadAsteroidsFromLocalCache(Context context) {
        try (FileInputStream fis = context.openFileInput(ASTEROID_CACHE_FILE)) {
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            String jsonData = new String(buffer);
            System.out.println("Asteroid data loaded from local cache.");
            return AsteroidParser.parseAsteroids(jsonData);  // Parse and return the asteroid list
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();  // Return an empty list if there's an error
        }
    }
}
