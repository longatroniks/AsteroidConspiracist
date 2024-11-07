package dte.masteriot.mdp.asteroidconspiracist.recyclerview.helpers;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import dte.masteriot.mdp.asteroidconspiracist.recyclerview.Adapter;

public class ItemKeyProvider extends androidx.recyclerview.selection.ItemKeyProvider<Long> {
    private static final String TAG = "TAGAsteroidConspiracist, ItemKeyProvider";
    RecyclerView recView;

    /**
     * Creates a new provider with the given scope.
     *
     * @param scope Scope can't be changed at runtime.
     */
    @SuppressLint("LongLogTag")
    public ItemKeyProvider(int scope, RecyclerView rv) {
        super(scope);
        recView = rv;
        Log.d(TAG, "MyItemKeyProvider() called");
    }

    @SuppressLint("LongLogTag")
    @Nullable
    @Override
    public Long getKey(int position) {
        Log.d(TAG, "getKey() called for position " + position);
        return (((Adapter) recView.getAdapter()).getKeyAtPosition(position));
    }

    @SuppressLint("LongLogTag")
    @Override
    public int getPosition(@NonNull Long key) {
        Log.d(TAG, "getPosition() called for key " + key);
        return (((Adapter) recView.getAdapter()).getPositionOfKey(key));
    }
}
