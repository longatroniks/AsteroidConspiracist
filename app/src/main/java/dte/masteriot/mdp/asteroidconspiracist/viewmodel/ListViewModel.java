package dte.masteriot.mdp.asteroidconspiracist.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.entity.Asteroid;
import dte.masteriot.mdp.asteroidconspiracist.repo.AsteroidRepository;

public class ListViewModel extends ViewModel {

    private final MutableLiveData<List<Asteroid>> asteroidsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>(null);

    private final AsteroidRepository asteroidRepository = AsteroidRepository.getInstance();

    public LiveData<List<Asteroid>> getAsteroidsLiveData() {
        return asteroidsLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public void fetchAsteroids(Context context, boolean isNetworkAvailable) {
        isLoadingLiveData.setValue(true);
        errorMessageLiveData.setValue(null);

        asteroidRepository.fetchAsteroids(context, isNetworkAvailable, new AsteroidRepository.FetchCallback() {
            @Override
            public void onSuccess() {
                asteroidsLiveData.setValue(asteroidRepository.getAsteroidList());
                isLoadingLiveData.setValue(false);
            }

            @Override
            public void onFailure(String error) {
                errorMessageLiveData.setValue(error);
                isLoadingLiveData.setValue(false);
            }
        });
    }
}
