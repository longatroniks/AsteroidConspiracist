package dte.masteriot.mdp.asteroidconspiracist.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import dte.masteriot.mdp.asteroidconspiracist.R;

public class ItemDetailsActivity extends AppCompatActivity {

    private TextView nameView, distanceView, maxDiameterView, minDiameterView,
            velocityView, magnitudeView, hazardousView, orbitIdView,
            semiMajorAxisView, nasaJplUrlView;
    private static final String PREF_HIGH_CONTRAST_MODE = "high_contrast_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyHighContrastTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_detail);

        // Initialize views
        nameView = findViewById(R.id.asteroid_name);
        distanceView = findViewById(R.id.asteroid_distance);
        maxDiameterView = findViewById(R.id.asteroid_max_diameter);
        minDiameterView = findViewById(R.id.asteroid_min_diameter);
        velocityView = findViewById(R.id.asteroid_velocity);
        magnitudeView = findViewById(R.id.asteroid_absolute_magnitude);
        hazardousView = findViewById(R.id.asteroid_is_hazardous);
        orbitIdView = findViewById(R.id.asteroid_orbit_id);
        semiMajorAxisView = findViewById(R.id.asteroid_semi_major_axis);
        nasaJplUrlView = findViewById(R.id.asteroid_nasa_jpl_url);

        // Set up back button
        ExtendedFloatingActionButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish()); // Finish activity to go back

        // Retrieve data from intent and set it in the TextViews
        Intent intent = getIntent();
        if (intent != null) {
            nameView.setText(intent.getStringExtra("asteroid_name"));
            distanceView.setText(String.format("Distance: %.3f km", intent.getDoubleExtra("asteroid_distance", 0)));
            maxDiameterView.setText(String.format("Max Diameter: %.3f km", intent.getDoubleExtra("asteroid_max_diameter", 0)));
            minDiameterView.setText(String.format("Min Diameter: %.3f km", intent.getDoubleExtra("asteroid_min_diameter", 0)));
            velocityView.setText(String.format("Velocity: %.3f km/h", intent.getDoubleExtra("asteroid_velocity", 0)));
            magnitudeView.setText(String.format("Absolute Magnitude: %.3f", intent.getDoubleExtra("asteroid_absolute_magnitude", 0)));
            hazardousView.setText("Potentially Hazardous: " + (intent.getBooleanExtra("asteroid_is_hazardous", false) ? "Yes" : "No"));
            orbitIdView.setText("Orbit ID: " + intent.getStringExtra("asteroid_orbit_id"));
            semiMajorAxisView.setText(String.format("Semi-Major Axis: %.3f", intent.getDoubleExtra("asteroid_semi_major_axis", 0)));
            nasaJplUrlView.setText("NASA JPL URL: " + intent.getStringExtra("asteroid_nasa_jpl_url"));
        }
    }

    private void applyHighContrastTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean highContrastEnabled = prefs.getBoolean(PREF_HIGH_CONTRAST_MODE, false);
        int nightModeFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;

        if (highContrastEnabled) {
            if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                setTheme(R.style.Theme_AsteroidConspiracist_HighContrast_Dark);
            } else {
                setTheme(R.style.Theme_AsteroidConspiracist_HighContrast_Light);
            }
        } else {
            if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                setTheme(R.style.Theme_AsteroidConspiracist_Dark);
            } else {
                setTheme(R.style.Theme_AsteroidConspiracist_Light);
            }
        }
    }
}
