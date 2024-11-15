package dte.masteriot.mdp.asteroidconspiracist.recyclerview.list;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.entities.Asteroid;

public class ListViewHolder extends RecyclerView.ViewHolder {

    TextView title;
    TextView subtitle;
    TextView diameter;
    TextView distance;

    public ListViewHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
        subtitle = itemView.findViewById(R.id.subtitle);
        diameter = itemView.findViewById(R.id.diameter);
        distance = itemView.findViewById(R.id.distance);
    }

    void bindValues(Asteroid asteroid, Boolean isSelected) {
        title.setText(asteroid.getName());
        subtitle.setText("Orbit ID: " + asteroid.getOrbitId());
        diameter.setText(String.format(Locale.ENGLISH, "Diameter: %.2f - %.2f km", asteroid.getMinDiameter(), asteroid.getMaxDiameter()));
        distance.setText(String.format(Locale.ENGLISH, "Distance: %.2f km", asteroid.getDistance()));

        if (isSelected) {
            title.setTextColor(Color.BLUE);
        }
    }
}
