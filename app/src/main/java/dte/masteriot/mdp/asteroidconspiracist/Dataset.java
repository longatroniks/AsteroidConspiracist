package dte.masteriot.mdp.asteroidconspiracist;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Dataset {

    // This dataset is a list of Items

    private static final String TAG = "TAGListOfItems, Dataset";
    private List<Item> listofitems;
    private final int ITEM_COUNT = 50;

    Dataset() {
        Log.d(TAG, "Dataset() called");
        listofitems = new ArrayList<>();
        for (int i = 0; i < ITEM_COUNT; ++i) {
            listofitems.add(new Item("Item " + i, "This is the item number " + i, (long) i));
        }
    }

    int getSize() {
        return listofitems.size();
    }

    Item getItemAtPosition(int pos) {
        return listofitems.get(pos);
    }

    Long getKeyAtPosition(int pos) {
        return (listofitems.get(pos).getKey());
    }

    public int getPositionOfKey(Long searchedkey) {
        // Look for the position of the Item with key = searchedkey.
        // The following works because in Item, the method "equals" is overriden to compare only keys:
        int position = listofitems.indexOf(new Item("placeholder", "placeholder", searchedkey));
        //Log.d(TAG, "getPositionOfKey() called for key " + searchedkey + ", returns " + position);
        return position;
    }

    void removeItemAtPosition(int i) {
        listofitems.remove(i);
    }

    void removeItemWithKey(Long key) {
        removeItemAtPosition(getPositionOfKey(key));
    }

}
