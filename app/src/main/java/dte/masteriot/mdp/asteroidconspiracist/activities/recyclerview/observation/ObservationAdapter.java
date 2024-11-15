package dte.masteriot.mdp.asteroidconspiracist.activities.recyclerview.observation;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.entities.Observation;

public class ObservationAdapter extends RecyclerView.Adapter<ObservationAdapter.ObservationViewHolder> {

    private List<Observation> observations;
    private OnObservationClickListener clickListener;

    public interface OnObservationClickListener {
        void onObservationClick(Observation observation);
    }

    public ObservationAdapter(List<Observation> observations, OnObservationClickListener clickListener) {
        this.observations = observations;
        this.clickListener = clickListener;
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
    }

    @Override
    public int getItemCount() {
        return observations.size();
    }

    public class ObservationViewHolder extends RecyclerView.ViewHolder {

        public ObservationViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(Observation observation) {
            itemView.setOnClickListener(v -> clickListener.onObservationClick(observation));
        }
    }
}
