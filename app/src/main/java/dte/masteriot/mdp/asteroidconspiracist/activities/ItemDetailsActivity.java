package dte.masteriot.mdp.asteroidconspiracist.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Button;

import dte.masteriot.mdp.asteroidconspiracist.R;

public class ItemDetailsActivity extends BaseActivity {

    TextView asteroidName;
    TextView asteroidDistance;
    TextView asteroidMaxDiameter;
    TextView asteroidMinDiameter;
    TextView asteroidAbsoluteMagnitude;
    TextView asteroidOrbitId;
    TextView asteroidSemiMajorAxis;
    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = getLayoutInflater().inflate(R.layout.item_detail, null);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(contentView);

        asteroidName = findViewById(R.id.asteroid_name);
        asteroidDistance = findViewById(R.id.asteroid_distance);
        asteroidMaxDiameter = findViewById(R.id.asteroid_max_diameter);
        asteroidMinDiameter = findViewById(R.id.asteroid_min_diameter);
        asteroidAbsoluteMagnitude = findViewById(R.id.asteroid_absolute_magnitude);
        asteroidOrbitId = findViewById(R.id.asteroid_orbit_id);
        asteroidSemiMajorAxis = findViewById(R.id.asteroid_semi_major_axis);
        backButton = findViewById(R.id.back_button);

        Intent inputIntent = getIntent();
        String name = inputIntent.getStringExtra("asteroid_name");
        double distance = inputIntent.getDoubleExtra("asteroid_distance", 0);
        double maxDiameter = inputIntent.getDoubleExtra("asteroid_max_diameter", 0);
        double minDiameter = inputIntent.getDoubleExtra("asteroid_min_diameter", 0);
        double absoluteMagnitude = inputIntent.getDoubleExtra("asteroid_absolute_magnitude", 0);
        String orbitId = inputIntent.getStringExtra("asteroid_orbit_id");
        double semiMajorAxis = inputIntent.getDoubleExtra("asteroid_semi_major_axis", 0);

        asteroidName.setText(name);
        asteroidDistance.setText("Distance: " + distance + " km");
        asteroidMaxDiameter.setText("Max Diameter: " + maxDiameter + " km");
        asteroidMinDiameter.setText("Min Diameter: " + minDiameter + " km");
        asteroidAbsoluteMagnitude.setText("Absolute Magnitude: " + absoluteMagnitude);
        asteroidOrbitId.setText("Orbit ID: " + orbitId);
        asteroidSemiMajorAxis.setText("Semi-Major Axis: " + semiMajorAxis + " AU");

        backButton.setOnClickListener(view -> {
            finish();
        });
    }
}
