package dte.masteriot.mdp.asteroidconspiracist.recyclerview.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.ItemDetailsLookup;

import dte.masteriot.mdp.asteroidconspiracist.activities.SecondActivity;
import dte.masteriot.mdp.asteroidconspiracist.services.NeoWsAPIService;

public class OnItemActivatedListener implements androidx.recyclerview.selection.OnItemActivatedListener<Long> {

    private static final String TAG = "TAGAsteroidConspiracist, AsteroidOnItemActivatedListener";

    private final Context context;
    private NeoWsAPIService neoWsAPIClient;

    public OnItemActivatedListener(Context context, NeoWsAPIService ds) {
        this.context = context;
        this.neoWsAPIClient = ds;
    }

    @SuppressLint("LongLogTag")
    @Override
    public boolean onItemActivated(@NonNull ItemDetailsLookup.ItemDetails itemdetails,
                                   @NonNull MotionEvent e) {

        Log.d(TAG, "Clicked item with position = " + itemdetails.getPosition()
                + " and key = " + itemdetails.getSelectionKey());

        Intent i = new Intent(context, SecondActivity.class);
        i.putExtra("text", "Clicked item with position = " + itemdetails.getPosition()
                + " and key = " + itemdetails.getSelectionKey());
        context.startActivity(i);
        return true;
    }
}
