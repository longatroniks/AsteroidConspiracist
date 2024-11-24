package dte.masteriot.mdp.asteroidconspiracist.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.activity.recyclerview.list.ListAdapter;
import dte.masteriot.mdp.asteroidconspiracist.activity.recyclerview.list.ListItemDetailsLookup;
import dte.masteriot.mdp.asteroidconspiracist.activity.recyclerview.list.helpers.ItemKeyProvider;
import dte.masteriot.mdp.asteroidconspiracist.activity.recyclerview.list.helpers.OnItemActivatedListener;
import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.entity.Asteroid;
import dte.masteriot.mdp.asteroidconspiracist.util.network.NetworkHelper;
import dte.masteriot.mdp.asteroidconspiracist.viewmodel.ListViewModel;

public class ListActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private ListAdapter listAdapter;
    private SelectionTracker<Long> tracker;
    private ListViewModel listViewModel;
    private NetworkHelper networkHelper;

    private final String TAG = "ListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = getLayoutInflater().inflate(R.layout.activity_list, null);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(contentView);

        networkHelper = new NetworkHelper(this);
        initializeRecyclerView();
        setupViewModel();
        observeViewModel();

        fetchDataIfNeeded();
    }

    private void initializeRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        listAdapter = new ListAdapter(new ArrayList<>());
        recyclerView.setAdapter(listAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tracker = new SelectionTracker.Builder<>(
                "my-selection-id",
                recyclerView,
                new ItemKeyProvider(androidx.recyclerview.selection.ItemKeyProvider.SCOPE_MAPPED, recyclerView),
                new ListItemDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage())
                .withOnItemActivatedListener(new OnItemActivatedListener(this, listAdapter))
                .build();

        listAdapter.setSelectionTracker(tracker);
    }

    private void setupViewModel() {
        listViewModel = new ViewModelProvider(this).get(ListViewModel.class);
    }

    private void observeViewModel() {
        listViewModel.getAsteroidsLiveData().observe(this, asteroids -> {
            if (asteroids != null) {
                listAdapter.updateData(asteroids);
            } else {
                Toast.makeText(this, "No asteroid data available.", Toast.LENGTH_SHORT).show();
            }
        });

        listViewModel.getErrorMessageLiveData().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error: " + errorMessage);
            }
        });

        listViewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            if (isLoading) {
                Log.d(TAG, "Loading asteroid data...");
            } else {
                Log.d(TAG, "Finished loading asteroid data.");
            }
        });
    }

    private void fetchDataIfNeeded() {
        List<Asteroid> asteroids = listViewModel.getAsteroidsLiveData().getValue();
        if (asteroids == null || asteroids.isEmpty()) {
            listViewModel.fetchAsteroids(this, networkHelper.isNetworkAvailable());
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        tracker.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
