package dte.masteriot.mdp.asteroidconspiracist.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BaseViewModel extends ViewModel {

    private final MutableLiveData<Boolean> highContrastMode = new MutableLiveData<>();

    public LiveData<Boolean> getHighContrastMode() {
        return highContrastMode;
    }

    public void setHighContrastMode(boolean isEnabled) {
        highContrastMode.setValue(isEnabled);
    }
}
