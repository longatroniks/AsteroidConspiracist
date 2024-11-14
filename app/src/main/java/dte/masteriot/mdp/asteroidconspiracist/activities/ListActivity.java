package dte.masteriot.mdp.asteroidconspiracist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import dte.masteriot.mdp.asteroidconspiracist.recyclerview.Adapter;
import dte.masteriot.mdp.asteroidconspiracist.recyclerview.ItemDetailsLookup;
import dte.masteriot.mdp.asteroidconspiracist.recyclerview.helpers.ItemKeyProvider;
import dte.masteriot.mdp.asteroidconspiracist.services.MqttService;
import dte.masteriot.mdp.asteroidconspiracist.recyclerview.helpers.OnItemActivatedListener;
import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.models.Asteroid;
import dte.masteriot.mdp.asteroidconspiracist.services.NeoWsAPIService;
import dte.masteriot.mdp.asteroidconspiracist.utils.AsteroidParser;

public class ListActivity extends AppCompatActivity {

    // App-specific dataset:
    private static final NeoWsAPIService NEO_WS_API_CLIENT = new NeoWsAPIService();
    private RecyclerView recyclerView;
    private Adapter adapter;
    private SelectionTracker<Long> tracker;
    private final OnItemActivatedListener onItemActivatedListener =
            new OnItemActivatedListener(this, NEO_WS_API_CLIENT);
    private ExecutorService executorService = Executors.newSingleThreadExecutor(); // Executor for background tasks

    String TAG;
    MqttService mqttService =new MqttService();
    boolean bBrokerConnected=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Prepare the RecyclerView:
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new Adapter(new ArrayList<>()); // Initialize with an empty list
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Choose the layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Selection tracker setup
        tracker = new SelectionTracker.Builder<>(
                "my-selection-id",
                recyclerView,
                new ItemKeyProvider(androidx.recyclerview.selection.ItemKeyProvider.SCOPE_MAPPED, recyclerView),
                new ItemDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage())
                .withOnItemActivatedListener(onItemActivatedListener)
                .build();
        adapter.setSelectionTracker(tracker);

        if (savedInstanceState != null) {
            tracker.onRestoreInstanceState(savedInstanceState);
        }

        // Fetch data from the NeoWs API
        fetchAsteroids();

        // MQTT Connection
        mqttService.createMQTTclient();
        mqttService.connectToBroker("Publishing UFO").thenAccept(isConnected -> {
            if (isConnected) {
                Log.d(TAG, "Successfully connected to the broker.");
                bBrokerConnected = true;
            } else {
                Log.d(TAG, "Failed to connect to the broker.");
                bBrokerConnected = false;
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
        executorService.execute(() -> {
            try {
                String jsonResponse = NEO_WS_API_CLIENT.getAsteroids();
                Log.d(TAG,"JSONResponse right hya: " + jsonResponse);
                List<Asteroid> asteroids = AsteroidParser.parseAsteroids(jsonResponse);

                runOnUiThread(() -> {
                    adapter.updateData(asteroids);
                    if (bBrokerConnected)
                        mqttService.PublishAsteroidInfo(asteroids);
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ListActivity.this, "Failed to fetch asteroids", Toast.LENGTH_SHORT).show());
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
