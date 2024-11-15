package dte.masteriot.mdp.asteroidconspiracist.util;

import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoadingStateManager {

    private final TextView loadingMessage;
    private final ProgressBar loadingSpinner;
    private final View[] chartViews;

    public LoadingStateManager(TextView loadingMessage, ProgressBar loadingSpinner, View... chartViews) {
        this.loadingMessage = loadingMessage;
        this.loadingSpinner = loadingSpinner;
        this.chartViews = chartViews;
    }

    public void showLoadingScreen() {
        loadingMessage.setVisibility(View.VISIBLE);
        loadingSpinner.setVisibility(View.VISIBLE);
        for (View view : chartViews) {
            view.setVisibility(View.GONE);
        }
    }

    public void hideLoadingScreen() {
        Log.d("LoadingStateManager", "Hiding loading screen");
        loadingMessage.setVisibility(View.GONE);
        loadingSpinner.setVisibility(View.GONE);
        for (View view : chartViews) {
            Log.d("LoadingStateManager", "Making view visible: " + view.getId());
            view.setVisibility(View.VISIBLE);
        }
    }

}
