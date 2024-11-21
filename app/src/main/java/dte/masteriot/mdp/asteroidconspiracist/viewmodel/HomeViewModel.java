package dte.masteriot.mdp.asteroidconspiracist.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.entity.Asteroid;
import dte.masteriot.mdp.asteroidconspiracist.repo.AsteroidRepository;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<Asteroid>> asteroidsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
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

    public boolean isDataLoaded() {
        return isDataLoaded;
    }

    public void fetchAsteroids(Context context, boolean isNetworkAvailable) {
        if (isDataLoaded || Boolean.TRUE.equals(isLoadingLiveData.getValue())) return;

        Log.d("HomeViewModel", "Starting asteroid data fetch...");
        isLoadingLiveData.postValue(true); // Ensure loading state is active

        asteroidRepository.fetchAsteroids(context, isNetworkAvailable, new AsteroidRepository.FetchCallback() {
            @Override
            public void onSuccess() {
                Log.d("HomeViewModel", "Asteroid data fetch successful.");
                if (!isDataLoaded) {
                    asteroidsLiveData.setValue(asteroidRepository.getAsteroidList());
                    isDataLoaded = true;
                }
                isLoadingLiveData.postValue(false); // Loading is complete
            }

            @Override
            public void onFailure(String error) {
                Log.d("HomeViewModel", "Asteroid data fetch failed: " + error);
                errorMessageLiveData.setValue(error);
                isLoadingLiveData.postValue(false); // Ensure loading is hidden even on failure
            }
        });
    }

}
