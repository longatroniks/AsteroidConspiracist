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
    private AsteroidAdapter recyclerViewAdapter;
    private SelectionTracker<Long> tracker;
    private final AsteroidOnItemActivatedListener asteroidOnItemActivatedListener =
            new AsteroidOnItemActivatedListener(this, NEO_WS_API_CLIENT);
    private ExecutorService executorService = Executors.newSingleThreadExecutor(); // Executor for background tasks

    String TAG;
    AsteroidMqtt asteroidMqtt=new AsteroidMqtt();
    boolean bBrokerConnected=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Prepare the RecyclerView:
        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewAdapter = new AsteroidAdapter(new ArrayList<>()); // Initialize with an empty list
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Choose the layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Selection tracker setup
        tracker = new SelectionTracker.Builder<>(
                "my-selection-id",
                recyclerView,
                new AsteroidItemKeyProvider(ItemKeyProvider.SCOPE_MAPPED, recyclerView),
                new AsteroidItemDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage())
                .withOnItemActivatedListener(asteroidOnItemActivatedListener)
                .build();
        recyclerViewAdapter.setSelectionTracker(tracker);

        if (savedInstanceState != null) {
            tracker.onRestoreInstanceState(savedInstanceState);
        }

        // Fetch data from the NeoWs API
        fetchAsteroids();

        //MQTT Connection AG
        asteroidMqtt.createMQTTclient();

        // When MQTT connection is Successfully, topics can be published and subscribed. AG
        // CompletableFuture to manage the asynchronous connection instead of a callback interface
        asteroidMqtt.connectToBroker().thenAccept(isConnected -> {
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
        //MQTT Disconnection
        asteroidMqtt.disconnectFromBroker();
        //
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
                        if (bBrokerConnected)
                            asteroidMqtt.PublishAsteroidInfo(asteroids);// Publishing the Asteroid topic if the broker is connected and there is a new update AG

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

    public void UFOmap (View view)
    {
        String text = "third activity pressing button\n";
        double latitude = 37.422;  // Replace with your desired latitude
        double longitude = -122.084; // Replace with your desired longitude
        Intent intent = new Intent(this, MapsActivity.class);

        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("text", text);
        startActivity(intent);

    }
}
