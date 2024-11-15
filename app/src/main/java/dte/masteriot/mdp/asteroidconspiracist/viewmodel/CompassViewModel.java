package dte.masteriot.mdp.asteroidconspiracist.viewmodel;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.Objects;

public class CompassViewModel extends AndroidViewModel {

    private final MutableLiveData<Float> azimuthLiveData = new MutableLiveData<>();
    private final MutableLiveData<Location> currentLocationLiveData = new MutableLiveData<>();
    private final MutableLiveData<Location> targetLocationLiveData = new MutableLiveData<>();
    private final MutableLiveData<Float> distanceLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> closestCityLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> closestShelterLiveData = new MutableLiveData<>();

    public CompassViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Float> getAzimuthLiveData() {
        return azimuthLiveData;
    }

    public LiveData<Location> getCurrentLocationLiveData() {
        return currentLocationLiveData;
    }

    public LiveData<Location> getTargetLocationLiveData() {
        return targetLocationLiveData;
    }

    public LiveData<Float> getDistanceLiveData() {
        return distanceLiveData;
    }

    public LiveData<String> getClosestCityLiveData() {
        return closestCityLiveData;
    }

    public LiveData<String> getClosestShelterLiveData() {
        return closestShelterLiveData;
    }

    public void updateAzimuth(float azimuth) {
        azimuthLiveData.setValue(azimuth);
    }

    public void updateCurrentLocation(Location location) {
        currentLocationLiveData.setValue(location);
    }

    public void updateTargetLocation(Location location) {
        targetLocationLiveData.setValue(location);
    }

    public void updateDistance(float distance) {
        distanceLiveData.setValue(distance);
    }

    public void updateClosestCity(String city) {
        closestCityLiveData.setValue(city);
    }

    public void updateClosestShelter(String shelter) {
        closestShelterLiveData.setValue(shelter);
    }

    public void processClosestLocation(Location currentLocation, List<Object[]> sheltersData) {
        if (sheltersData == null || sheltersData.isEmpty()) {
            return;
        }

        Location closestLocation = null;
        float minDistance = Float.MAX_VALUE;
        String closestCity = null;
        String closestShelter = null;

        for (Object[] shelterInfo : sheltersData) {
            String shelterName = (String) shelterInfo[0];
            String cityName = (String) shelterInfo[1];
            Location location = (Location) shelterInfo[2];

            float distance = currentLocation.distanceTo(location);

            if (distance < minDistance) {
                minDistance = distance;
                closestLocation = location;
                closestCity = cityName;
                closestShelter = shelterName;
            }
        }

        if (closestLocation != null) {
            updateDistance(minDistance);
            updateClosestCity(Objects.requireNonNull(closestCity));
            updateClosestShelter(Objects.requireNonNull(closestShelter));
            updateTargetLocation(closestLocation); // Update the target location
        }
    }
}
