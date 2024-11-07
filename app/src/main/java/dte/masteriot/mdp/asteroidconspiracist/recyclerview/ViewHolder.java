package dte.masteriot.mdp.asteroidconspiracist.recyclerview;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.models.Asteroid;

public class ViewHolder extends RecyclerView.ViewHolder {

    TextView title;
    TextView subtitle;

    private static final String TAG = "TAGAsteroidConspiracist, AsteroidViewHolder";

    public ViewHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
        subtitle = itemView.findViewById(R.id.subtitle);
    }

    void bindValues(Asteroid asteroid, Boolean isSelected) {
        title.setText(asteroid.getName());
        subtitle.setText(String.format(Locale.ENGLISH,"%f",asteroid.getDiameter()));
        if(isSelected) {
            title.setTextColor(Color.BLUE);
        } else {
            title.setTextColor(Color.BLACK);
        }
    }

}