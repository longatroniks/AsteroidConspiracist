package dte.masteriot.mdp.asteroidconspiracist.activity.recyclerview.observation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.entity.Observation;

import java.util.List;

public class ObservationPagerAdapter extends RecyclerView.Adapter<ObservationPagerAdapter.ObservationViewHolder> {

    private final List<Observation> observations;
    private final OnObservationClickListener clickListener;

    public interface OnObservationClickListener {
        void onObservationClick(Observation observation);
    }

    public ObservationPagerAdapter(List<Observation> observations, OnObservationClickListener clickListener) {
        this.observations = observations;
        this.clickListener = clickListener;
    }

    public void updateObservations(List<Observation> newObservations) {
        Log.d("ObservationPagerAdapter", "Adapter received " + newObservations.size() + " observations.");
        observations.clear();
        observations.addAll(newObservations);

        for (Observation obs : newObservations) {
            Log.d("ObservationPagerAdapter", "Observation: " + obs.getDescription());
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ObservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.observation_item, parent, false);
        return new ObservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ObservationViewHolder holder, int position) {
        Observation observation = observations.get(position);
        holder.bind(observation);
        holder.itemView.setOnClickListener(v -> clickListener.onObservationClick(observation));
    }

    @Override
    public int getItemCount() {
        return observations.size();
    }

    static class ObservationViewHolder extends RecyclerView.ViewHolder {
        private final TextView description;
        private final TextView timestamp;

        public ObservationViewHolder(@NonNull View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.observationDescription);
            timestamp = itemView.findViewById(R.id.observationTimestamp);
        }

        void bind(Observation observation) {
            description.setText(observation.getDescription());
            timestamp.setText(observation.getTimestamp());
        }
    }
}
