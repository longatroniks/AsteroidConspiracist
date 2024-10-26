package dte.masteriot.mdp.asteroidconspiracist;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.models.Asteroid;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private static final String TAG = "TAGListOfItems, MyAdapter";

    private List<Asteroid> asteroids; // List of asteroids
    private SelectionTracker<Long> selectionTracker; // Reference to the selection tracker

    public MyAdapter(List<Asteroid> asteroids) {
        super();
        Log.d(TAG, "MyAdapter() called");
        this.asteroids = asteroids; // Initialize the adapter with the asteroid list
    }

    // ------ Implementation of methods of RecyclerView.Adapter ------ //

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item view and return the view holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // Bind values to the view holder for the item at 'position'
        Asteroid asteroid = asteroids.get(position); // Get the asteroid object
        Long itemKey = asteroid.getKey(); // Assuming Asteroid has a getKey() method
        boolean isItemSelected = selectionTracker != null && selectionTracker.isSelected(itemKey);

        Log.d(TAG, "onBindViewHolder() called for element in position " + position +
                ", Selected? = " + isItemSelected);
        holder.bindValues(asteroid, isItemSelected); // Bind values
    }

    @Override
    public int getItemCount() {
        return asteroids.size(); // Return the size of the asteroid list
    }

    // ------ Other methods useful for the app ------ //

    public Long getKeyAtPosition(int pos) {
        return asteroids.get(pos).getKey(); // Assuming Asteroid has a getKey() method
    }

    public int getPositionOfKey(Long searchedKey) {
        for (int i = 0; i < asteroids.size(); i++) {
            if (asteroids.get(i).getKey().equals(searchedKey)) {
                return i; // Return the position of the key
            }
        }
        return -1; // Return -1 if the key is not found
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker; // Set the selection tracker
    }

    // Method to update the data in the adapter
    public void updateData(List<Asteroid> newAsteroids) {
        this.asteroids.clear(); // Clear the current list
        this.asteroids.addAll(newAsteroids); // Add new asteroids
        notifyDataSetChanged(); // Notify the adapter that the dataset has changed
    }
}
