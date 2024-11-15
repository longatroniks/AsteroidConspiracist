package dte.masteriot.mdp.asteroidconspiracist.recyclerview.list.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.ItemDetailsLookup;

import dte.masteriot.mdp.asteroidconspiracist.activities.ItemDetailsActivity;
import dte.masteriot.mdp.asteroidconspiracist.entities.Asteroid;
import dte.masteriot.mdp.asteroidconspiracist.recyclerview.list.ListAdapter;

public class OnItemActivatedListener implements androidx.recyclerview.selection.OnItemActivatedListener<Long> {

    private static final String TAG = "TAGAsteroidConspiracist, OnItemActivatedListener";
    private final Context context;
    private final ListAdapter listAdapter;

    public OnItemActivatedListener(Context context, ListAdapter listAdapter) {
        this.context = context;
        this.listAdapter = listAdapter;
    }

    @SuppressLint("LongLogTag")
    @Override
    public boolean onItemActivated(@NonNull ItemDetailsLookup.ItemDetails<Long> itemDetails,
                                   @NonNull MotionEvent e) {

        Log.d(TAG, "Item clicked at position: " + itemDetails.getPosition());

        Asteroid asteroid = listAdapter.getAsteroidByKey(itemDetails.getSelectionKey());

        if (asteroid != null) {
            Intent intent = new Intent(context, ItemDetailsActivity.class);
            // Add all asteroid details to the intent
            intent.putExtra("asteroid_name", asteroid.getName());
            intent.putExtra("asteroid_distance", asteroid.getDistance());
            intent.putExtra("asteroid_max_diameter", asteroid.getMaxDiameter());
            intent.putExtra("asteroid_min_diameter", asteroid.getMinDiameter());
            intent.putExtra("asteroid_max_diameter_meters", asteroid.getMaxDiameterMeters());
            intent.putExtra("asteroid_min_diameter_meters", asteroid.getMinDiameterMeters());
            intent.putExtra("asteroid_velocity", asteroid.getVelocity());
            intent.putExtra("asteroid_absolute_magnitude", asteroid.getAbsoluteMagnitude());
            intent.putExtra("asteroid_is_hazardous", asteroid.isPotentiallyHazardous());
            intent.putExtra("asteroid_orbit_id", asteroid.getOrbitId());
            intent.putExtra("asteroid_semi_major_axis", asteroid.getSemiMajorAxis());
            intent.putExtra("asteroid_nasa_jpl_url", asteroid.getNasaJplUrl());

            context.startActivity(intent);
        }

        return true;
    }
}
