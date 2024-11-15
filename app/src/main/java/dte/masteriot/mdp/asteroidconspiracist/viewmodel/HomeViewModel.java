package dte.masteriot.mdp.asteroidconspiracist.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.entity.Asteroid;
import dte.masteriot.mdp.asteroidconspiracist.repo.AsteroidRepository;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<Asteroid>> asteroidsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>(null);
    private final MutableLiveData<String> dataSourceLiveData = new MutableLiveData<>();

    private final AsteroidRepository asteroidRepository = AsteroidRepository.getInstance();

    private boolean isDataLoaded = false;

    public LiveData<List<Asteroid>> getAsteroidsLiveData() {
        return asteroidsLiveData; // Expose as LiveData
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public LiveData<String> getDataSourceLiveData() {
        return dataSourceLiveData;
    }

    public void setAsteroidsLiveData(List<Asteroid> asteroids) {
        asteroidsLiveData.setValue(asteroids);
    }

    public boolean isDataLoaded() {
        return isDataLoaded;
    }

    public void fetchAsteroids(Context context, boolean isNetworkAvailable) {
        isLoadingLiveData.setValue(true); // Set loading state to true
        asteroidRepository.fetchAsteroids(context, isNetworkAvailable, new AsteroidRepository.FetchCallback() {
            @Override
            public void onSuccess() {
                asteroidsLiveData.setValue(asteroidRepository.getAsteroidList());
                isLoadingLiveData.setValue(false); // Set loading state to false
            }

            @Override
            public void onFailure(String error) {
                errorMessageLiveData.setValue(error);
                isLoadingLiveData.setValue(false); // Set loading state to false
            }
        });
    }

}
