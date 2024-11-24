package dte.masteriot.mdp.asteroidconspiracist.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.entity.Observation;
import dte.masteriot.mdp.asteroidconspiracist.entity.Shelter;

public class MapsViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isFullScreen = new MutableLiveData<>(false);
    private final MutableLiveData<List<Observation>> observationLocations = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Shelter>> shelterLocations = new MutableLiveData<>(new ArrayList<>());

    public LiveData<Boolean> getIsFullScreen() {
        return isFullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        isFullScreen.setValue(fullScreen);
    }

    public LiveData<List<Observation>> getObservationLocations() {
        return observationLocations;
    }

    public LiveData<List<Shelter>> getShelterLocations() {
        return shelterLocations;
    }

    public void addObservation(Observation observation) {
        List<Observation> current = new ArrayList<>(observationLocations.getValue());
        current.add(observation);

        // Force LiveData to notify observers with new data
        observationLocations.setValue(new ArrayList<>(current));
        Log.d("MapsViewModel", "Observations updated. Total count: " + current.size());
    }

    public void addShelter(Shelter shelter) {
        List<Shelter> current = new ArrayList<>(shelterLocations.getValue());
        current.add(shelter);
        shelterLocations.setValue(current);
    }
}
