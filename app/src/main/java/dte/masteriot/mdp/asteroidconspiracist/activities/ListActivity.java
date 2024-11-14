package dte.masteriot.mdp.asteroidconspiracist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import dte.masteriot.mdp.asteroidconspiracist.recyclerview.list.ListAdapter;
import dte.masteriot.mdp.asteroidconspiracist.recyclerview.list.ListItemDetailsLookup;
import dte.masteriot.mdp.asteroidconspiracist.recyclerview.list.helpers.ItemKeyProvider;
import dte.masteriot.mdp.asteroidconspiracist.services.MqttService;
import dte.masteriot.mdp.asteroidconspiracist.recyclerview.list.helpers.OnItemActivatedListener;
import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.models.Asteroid;
import dte.masteriot.mdp.asteroidconspiracist.services.NeoWsAPIService;
import dte.masteriot.mdp.asteroidconspiracist.utils.AsteroidParser;

public class ListActivity extends BaseActivity {

    private static final NeoWsAPIService NEO_WS_API_CLIENT = new NeoWsAPIService();
    private RecyclerView recyclerView;
    private ListAdapter listAdapter;
    private SelectionTracker<Long> tracker;
    private final OnItemActivatedListener onItemActivatedListener =
            new OnItemActivatedListener(this, NEO_WS_API_CLIENT);
    private ExecutorService executorService = Executors.newSingleThreadExecutor(); // Executor for background tasks

    String TAG ="DENZEL CURRY";
    MqttService mqttService =new MqttService();
    boolean bBrokerConnected=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = getLayoutInflater().inflate(R.layout.activity_list, null);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(contentView);

        recyclerView = findViewById(R.id.recyclerView);
        listAdapter = new ListAdapter(new ArrayList<>()); // Initialize with an empty list
        recyclerView.setAdapter(listAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tracker = new SelectionTracker.Builder<>(
                "my-selection-id",
                recyclerView,
                new ItemKeyProvider(androidx.recyclerview.selection.ItemKeyProvider.SCOPE_MAPPED, recyclerView),
                new ListItemDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage())
                .withOnItemActivatedListener(onItemActivatedListener)
                .build();
        listAdapter.setSelectionTracker(tracker);

        if (savedInstanceState != null) {
            tracker.onRestoreInstanceState(savedInstanceState);
        }

        // Fetch data from the NeoWs API
        fetchAsteroids();

        //MQTT Connection AG
        mqttService.createMQTTclient();
        mqttService.connectToBroker().thenAccept(isConnected -> {
            if (isConnected) {
                Log.d(TAG, "Successfully connected to the broker.");
                bBrokerConnected=true;
            } else {
                Log.d(TAG, "Failed to connect to the broker.");
                bBrokerConnected=false;
            }
        });
        //
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        tracker.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mqttService.disconnectFromBroker();
    }

    // Fetch asteroids from the API
    private void fetchAsteroids() {
        NEO_WS_API_CLIENT.getAsteroids(new NeoWsAPIService.NeoWsAPIResponse() {
            @Override
            public void onResponse(String jsonResponse) {
                List<Asteroid> asteroids = AsteroidParser.parseAsteroids(jsonResponse);

                // Update RecyclerView on the main thread
                runOnUiThread(() -> {
                    listAdapter.updateData(asteroids);
                    if (bBrokerConnected) {
                        mqttService.PublishAsteroidInfo(asteroids);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(ListActivity.this, "Failed to fetch asteroids", Toast.LENGTH_SHORT).show());
                Log.e(TAG, "Error fetching asteroids: " + error);
            }
        });
    }

    public void listLayout(View view) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void gridLayout(View view) {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
    }

    public void seeCurrentSelection(View view) {
        Iterator<Long> iteratorSelectedItemsKeys = tracker.getSelection().iterator();

        while (iteratorSelectedItemsKeys.hasNext()) {
            Long selectedKey = iteratorSelectedItemsKeys.next();
            Asteroid selectedAsteroid = listAdapter.getAsteroidByKey(selectedKey);
            if (selectedAsteroid != null) {
                Intent i = new Intent(this, ItemDetailsActivity.class);
                i.putExtra("asteroid_name", selectedAsteroid.getName());
                i.putExtra("asteroid_distance", selectedAsteroid.getDistance());
                i.putExtra("asteroid_max_diameter", selectedAsteroid.getMaxDiameter());
                i.putExtra("asteroid_min_diameter", selectedAsteroid.getMinDiameter());
                i.putExtra("asteroid_absolute_magnitude", selectedAsteroid.getAbsoluteMagnitude());
                i.putExtra("asteroid_orbit_id", selectedAsteroid.getOrbitId());
                i.putExtra("asteroid_semi_major_axis", selectedAsteroid.getSemiMajorAxis());
                startActivity(i);
            }
        }
    }


    public void UFOmap (View view)
    {
        String text = "third activity pressing button\n";
        double latitude = 37.422;
        double longitude = -122.084;
        Intent intent = new Intent(this, MapsActivity.class);

        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("text", text);
        startActivity(intent);

    }
}
