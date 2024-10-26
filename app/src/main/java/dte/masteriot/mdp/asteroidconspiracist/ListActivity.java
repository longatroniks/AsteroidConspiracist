package dte.masteriot.mdp.asteroidconspiracist;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dte.masteriot.mdp.asteroidconspiracist.models.Asteroid;
import dte.masteriot.mdp.asteroidconspiracist.utils.AsteroidParser;

public class ListActivity extends AppCompatActivity {

    // App-specific dataset:
    private static final NeoWsAPIClient NEO_WS_API_CLIENT = new NeoWsAPIClient();
    private RecyclerView recyclerView;
    private MyAdapter recyclerViewAdapter;
    private SelectionTracker<Long> tracker;
    private final MyOnItemActivatedListener myOnItemActivatedListener =
            new MyOnItemActivatedListener(this, NEO_WS_API_CLIENT);
    private ExecutorService executorService = Executors.newSingleThreadExecutor(); // Executor for background tasks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Prepare the RecyclerView:
        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewAdapter = new MyAdapter(new ArrayList<>()); // Initialize with an empty list
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Choose the layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Selection tracker setup
        tracker = new SelectionTracker.Builder<>(
                "my-selection-id",
                recyclerView,
                new MyItemKeyProvider(ItemKeyProvider.SCOPE_MAPPED, recyclerView),
                new MyItemDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage())
                .withOnItemActivatedListener(myOnItemActivatedListener)
                .build();
        recyclerViewAdapter.setSelectionTracker(tracker);

        if (savedInstanceState != null) {
            tracker.onRestoreInstanceState(savedInstanceState);
        }

        // Fetch data from the NeoWs API
        fetchAsteroids();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        tracker.onSaveInstanceState(outState);
    }

    // Fetch asteroids from the API
    private void fetchAsteroids() {
        executorService.execute(() -> {
            try {
                // Make the API call and parse the response
                String jsonResponse = NEO_WS_API_CLIENT.getAsteroids();
                Log.d(TAG,"JSONResponse right hya: " + jsonResponse);
                List<Asteroid> asteroids = AsteroidParser.parseAsteroids(jsonResponse);

                runOnUiThread(() -> {
                    if (asteroids != null) {
                        recyclerViewAdapter.updateData(asteroids); // Method to update the adapter with new data
                    } else {
                        Toast.makeText(ListActivity.this, "Failed to fetch asteroids", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ListActivity.this, "Failed to fetch asteroids", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // ------ Buttons' on-click listeners ------ //

    public void listLayout(View view) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void gridLayout(View view) {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
    }

    public void seeCurrentSelection(View view) {
        Iterator<Long> iteratorSelectedItemsKeys = tracker.getSelection().iterator();
        StringBuilder text = new StringBuilder();
        while (iteratorSelectedItemsKeys.hasNext()) {
            text.append(iteratorSelectedItemsKeys.next().toString());
            if (iteratorSelectedItemsKeys.hasNext()) {
                text.append(", ");
            }
        }
        text = new StringBuilder("Keys of currently selected items = \n" + text);
        Intent i = new Intent(this, SecondActivity.class);
        i.putExtra("text", text.toString());
        startActivity(i);
    }
}
