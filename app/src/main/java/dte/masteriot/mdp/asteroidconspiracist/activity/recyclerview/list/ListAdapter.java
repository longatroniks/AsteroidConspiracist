package dte.masteriot.mdp.asteroidconspiracist.activity.recyclerview.list;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.entity.Asteroid;

public class ListAdapter extends RecyclerView.Adapter<ListViewHolder> {

    private static final String TAG = "TAGAsteroidConspiracist, MyAdapter";
    private final List<Asteroid> asteroids;
    private SelectionTracker<Long> selectionTracker;

    public ListAdapter(List<Asteroid> asteroids) {
        super();
        Log.d(TAG, "AsteroidAdapter() called");
        this.asteroids = asteroids;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new ListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        Asteroid asteroid = asteroids.get(position);
        Long itemKey = asteroid.getKey();
        boolean isItemSelected = selectionTracker != null && selectionTracker.isSelected(itemKey);

        Log.d(TAG, "onBindViewHolder() called for element in position " + position +
                ", Selected? = " + isItemSelected);
        holder.bindValues(asteroid, isItemSelected);
    }

    @Override
    public int getItemCount() {
        return asteroids.size();
    }

    public Long getKeyAtPosition(int pos) {
        return asteroids.get(pos).getKey();
    }

    public int getPositionOfKey(Long searchedKey) {
        for (int i = 0; i < asteroids.size(); i++) {
            if (asteroids.get(i).getKey().equals(searchedKey)) {
                return i;
            }
        }
        return -1;
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    // Method to update the data in the adapter
    public void updateData(List<Asteroid> newAsteroids) {
        this.asteroids.clear();
        this.asteroids.addAll(newAsteroids);
        notifyDataSetChanged();
    }

    public Asteroid getAsteroidByKey(Long key) {
        for (Asteroid asteroid : asteroids) {
            if (asteroid.getKey().equals(key)) {
                return asteroid;
            }
        }
        return null;
    }

}
