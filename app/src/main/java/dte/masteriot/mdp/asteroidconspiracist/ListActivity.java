// Parts of the code of this example app have ben taken from:
// https://enoent.fr/posts/recyclerview-basics/
// https://developer.android.com/guide/topics/ui/layout/recyclerview

package dte.masteriot.mdp.asteroidconspiracist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Iterator;

public class ListActivity extends AppCompatActivity {

    // App-specific dataset:
    private static final Dataset dataset = new Dataset();

    private RecyclerView recyclerView;
    private SelectionTracker<Long> tracker;
    private final MyOnItemActivatedListener myOnItemActivatedListener =
            new MyOnItemActivatedListener(this, dataset);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Prepare the RecyclerView:
        recyclerView = findViewById(R.id.recyclerView);
        MyAdapter recyclerViewAdapter = new MyAdapter(dataset);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Choose the layout manager to be set.
        // some options for the layout manager:  GridLayoutManager, LinearLayoutManager, StaggeredGridLayoutManager
        // by default, a linear layout is chosen:
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Selection tracker (to allow for selection of items):
        tracker = new SelectionTracker.Builder<>(
                "my-selection-id",
                recyclerView,
                new MyItemKeyProvider(ItemKeyProvider.SCOPE_MAPPED, recyclerView),
//                new StableIdKeyProvider(recyclerView), // This caused the app to crash on long clicks
                new MyItemDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage())
                .withOnItemActivatedListener(myOnItemActivatedListener)
                .build();
        recyclerViewAdapter.setSelectionTracker(tracker);

        if (savedInstanceState != null) {
            // Restore state related to selections previously made
            tracker.onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        tracker.onSaveInstanceState(outState); // Save state about selections.
    }

    // ------ Buttons' on-click listeners ------ //

    public void listLayout(View view) {
        // Button to see in a linear fashion has been clicked:
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void gridLayout(View view) {
        // Button to see in a grid fashion has been clicked:
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
    }

    public void seeCurrentSelection(View view) {
        // Button "see current selection" has been clicked:

        Iterator<Long> iteratorSelectedItemsKeys = tracker.getSelection().iterator();
        // This iterator allows to navigate through the keys of the currently selected items.
        // Complete info on getSelection():
        // https://developer.android.com/reference/androidx/recyclerview/selection/SelectionTracker#getSelection()
        // Complete info on class Selection (getSelection() returns an object of this class):
        // https://developer.android.com/reference/androidx/recyclerview/selection/Selection

        String text = "";
        while (iteratorSelectedItemsKeys.hasNext()) {
            text += iteratorSelectedItemsKeys.next().toString();
            if (iteratorSelectedItemsKeys.hasNext()) {
                text += ", ";
            }
        }
        text = "Keys of currently selected items = \n" + text;
        Intent i = new Intent(this, SecondActivity.class);
        i.putExtra("text", text);
        startActivity(i);
    }

}