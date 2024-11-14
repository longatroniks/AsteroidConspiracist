package dte.masteriot.mdp.asteroidconspiracist.recyclerview.list;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.models.Asteroid;

public class ListViewHolder extends RecyclerView.ViewHolder {

    TextView title;
    TextView subtitle;
    TextView diameter;
    TextView distance;

    private static final String TAG = "TAGAsteroidConspiracist, AsteroidViewHolder";

    public ListViewHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
        subtitle = itemView.findViewById(R.id.subtitle);
//        diameter = itemView.findViewById(R.id.diameter);
//        distance = itemView.findViewById(R.id.distance);

    }

    void bindValues(Asteroid asteroid, Boolean isSelected) {
        title.setText(asteroid.getName());
        subtitle.setText(String.format(Locale.ENGLISH,"%f",asteroid.getMinDiameter()));
//        diameter.setText(String.valueOf(asteroid.getDiameter()));
//        distance.setText(String.valueOf(asteroid.getDistance()));
        if(isSelected) {
            title.setTextColor(Color.BLUE);
        } else {
            title.setTextColor(Color.BLACK);
        }
    }

}