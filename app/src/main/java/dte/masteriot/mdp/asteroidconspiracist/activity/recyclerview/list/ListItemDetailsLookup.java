package dte.masteriot.mdp.asteroidconspiracist.activity.recyclerview.list;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public final class ListItemDetailsLookup extends androidx.recyclerview.selection.ItemDetailsLookup<Long> {

    private static final String TAG = "TAGListOfItems, MyItemDetailsLookup";
    private final RecyclerView mRecyclerView;

    @SuppressLint("LongLogTag")
    public ListItemDetailsLookup(RecyclerView recyclerView) {
        Log.d(TAG, "MyItemDetailsLookup() called");
        mRecyclerView = recyclerView;
    }

    @SuppressLint("LongLogTag")
    @Nullable
    @Override
    public ItemDetails<Long> getItemDetails(@NonNull MotionEvent e) {
        Log.d(TAG, "getItemDetails() called for a given MotionEvent");
        View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
            if (holder instanceof ListViewHolder) {
                int positionOfTheHolder = holder.getAbsoluteAdapterPosition();
                Long keyOfTheHolder = ((ListAdapter) holder.getBindingAdapter()).getKeyAtPosition(positionOfTheHolder);

                ItemDetails<Long> itemDetails = new ItemDetails<Long>() {
                    @Override
                    public int getPosition() {
                        return (positionOfTheHolder);
                    }
                    @Nullable
                    @Override
                    public Long getSelectionKey() {
                        return (keyOfTheHolder);
                    }
                };

                return itemDetails;
            }
        }
        return null;
    }
}