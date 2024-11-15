package dte.masteriot.mdp.asteroidconspiracist.activity.recyclerview.legend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.R;

public class LegendAdapter extends RecyclerView.Adapter<LegendAdapter.LegendViewHolder> {

    private final List<LegendItem> legendItems;
    private final OnLegendItemClickListener clickListener;

    public interface OnLegendItemClickListener {
        void onLegendItemClick(int position);
    }

    public LegendAdapter(List<LegendItem> legendItems, OnLegendItemClickListener clickListener) {
        this.legendItems = legendItems;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public LegendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_legend, parent, false);
        return new LegendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LegendViewHolder holder, int position) {
        LegendItem item = legendItems.get(position);
        holder.colorBox.setBackgroundColor(item.getColor());
        holder.label.setText(item.getLabel());
        holder.itemView.setOnClickListener(v -> clickListener.onLegendItemClick(position));
    }

    @Override
    public int getItemCount() {
        return legendItems.size();
    }

    static class LegendViewHolder extends RecyclerView.ViewHolder {
        View colorBox;
        TextView label;

        LegendViewHolder(View itemView) {
            super(itemView);
            colorBox = itemView.findViewById(R.id.colorBox);
            label = itemView.findViewById(R.id.legendLabel);
        }
    }
}
