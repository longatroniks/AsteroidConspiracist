package dte.masteriot.mdp.asteroidconspiracist.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import dte.masteriot.mdp.asteroidconspiracist.entity.Asteroid;

public class ItemDetailsViewModel extends ViewModel {

    private final MutableLiveData<Asteroid> asteroid = new MutableLiveData<>();

    public LiveData<Asteroid> getAsteroid() {
        return asteroid;
    }

    public void setAsteroid(Asteroid asteroidData) {
        asteroid.setValue(asteroidData);
    }
}
