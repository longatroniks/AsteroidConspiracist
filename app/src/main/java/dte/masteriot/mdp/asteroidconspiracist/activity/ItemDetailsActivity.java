package dte.masteriot.mdp.asteroidconspiracist.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.entity.Asteroid;
import dte.masteriot.mdp.asteroidconspiracist.viewmodel.ItemDetailsViewModel;

public class ItemDetailsActivity extends AppCompatActivity {

    private TextView nameView, distanceView, maxDiameterView, minDiameterView,
            velocityView, magnitudeView, hazardousView, orbitIdView,
            semiMajorAxisView, nasaJplUrlView;

    private ItemDetailsViewModel viewModel;

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

        ExtendedFloatingActionButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(ItemDetailsViewModel.class);

        viewModel.getAsteroid().observe(this, asteroid -> {
            if (asteroid != null) {
                updateUIWithAsteroidDetails(asteroid);
            }
        });

        loadAsteroidDataFromIntent();
    }

    private void loadAsteroidDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            Asteroid asteroid = new Asteroid();
            asteroid.setName(intent.getStringExtra("asteroid_name"));
            asteroid.setDistance(intent.getDoubleExtra("asteroid_distance", 0));
            asteroid.setMaxDiameter(intent.getDoubleExtra("asteroid_max_diameter", 0));
            asteroid.setMinDiameter(intent.getDoubleExtra("asteroid_min_diameter", 0));
            asteroid.setVelocity(intent.getDoubleExtra("asteroid_velocity", 0));
            asteroid.setAbsoluteMagnitude(intent.getDoubleExtra("asteroid_absolute_magnitude", 0));
            asteroid.setPotentiallyHazardous(intent.getBooleanExtra("asteroid_is_hazardous", false));
            asteroid.setOrbitId(intent.getStringExtra("asteroid_orbit_id"));
            asteroid.setSemiMajorAxis(intent.getDoubleExtra("asteroid_semi_major_axis", 0));
            asteroid.setNasaJplUrl(intent.getStringExtra("asteroid_nasa_jpl_url"));

            viewModel.setAsteroid(asteroid);
        }
    }

    private void updateUIWithAsteroidDetails(Asteroid asteroid) {
        nameView.setText(asteroid.getName());
        distanceView.setText(String.format("Distance: %.3f km", asteroid.getDistance()));
        maxDiameterView.setText(String.format("Max Diameter: %.3f km", asteroid.getMaxDiameter()));
        minDiameterView.setText(String.format("Min Diameter: %.3f km", asteroid.getMinDiameter()));
        velocityView.setText(String.format("Velocity: %.3f km/h", asteroid.getVelocity()));
        magnitudeView.setText(String.format("Absolute Magnitude: %.3f", asteroid.getAbsoluteMagnitude()));
        hazardousView.setText("Potentially Hazardous: " + (asteroid.isPotentiallyHazardous() ? "Yes" : "No"));
        orbitIdView.setText("Orbit ID: " + asteroid.getOrbitId());
        semiMajorAxisView.setText(String.format("Semi-Major Axis: %.3f", asteroid.getSemiMajorAxis()));
        nasaJplUrlView.setText("NASA JPL URL: " + asteroid.getNasaJplUrl());
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
