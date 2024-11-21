package dte.masteriot.mdp.asteroidconspiracist.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Switch;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import dte.masteriot.mdp.asteroidconspiracist.R;

public class BaseActivity extends AppCompatActivity {

    private static final String PREF_HIGH_CONTRAST_MODE = "high_contrast_mode";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyThemeBasedOnSettings(); // Apply theme before activity is created
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        setupToolbar();
        setupDrawer();
        setupHighContrastSwitch();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set activity title in the toolbar
        String activityTitle = getActivityTitle();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(activityTitle);
        }
    }

    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Create the toggle (hamburger icon)
        Toolbar toolbar = findViewById(R.id.toolbar);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState(); // Synchronize the drawer toggle state

        // Set navigation menu item click handling
        navigationView.setNavigationItemSelectedListener(item -> {
            handleNavigationItemClick(item);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupHighContrastSwitch() {
        // Find the switch in the navigation menu
        Switch highContrastSwitch = (Switch) navigationView.getMenu()
                .findItem(R.id.switch_high_contrast)
                .getActionView();

        // Load the current preference state
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isHighContrastEnabled = prefs.getBoolean(PREF_HIGH_CONTRAST_MODE, false);
        highContrastSwitch.setChecked(isHighContrastEnabled);

        // Listen for switch toggle events
        highContrastSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d("BaseActivity", "High contrast mode toggled: " + isChecked);
            prefs.edit().putBoolean(PREF_HIGH_CONTRAST_MODE, isChecked).apply();

            // Restart activity to apply the new theme
            restartActivity();
        });
    }

    private void applyThemeBasedOnSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isHighContrastEnabled = prefs.getBoolean(PREF_HIGH_CONTRAST_MODE, false);

        int themeResId = isHighContrastEnabled
                ? R.style.Theme_AsteroidConspiracist_HighContrast_Light
                : R.style.Theme_AsteroidConspiracist_Light;

        setTheme(themeResId);
    }

    private void handleNavigationItemClick(@NonNull MenuItem item) {
        Intent intent = null;

        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            intent = new Intent(BaseActivity.this, HomeActivity.class);
        } else if (itemId == R.id.nav_compass) {
            intent = new Intent(BaseActivity.this, CompassActivity.class);
        } else if (itemId == R.id.nav_asteroid_list) {
            intent = new Intent(BaseActivity.this, ListActivity.class);
        } else if (itemId == R.id.nav_mqtt) {
            intent = new Intent(BaseActivity.this, MapsActivity.class);
        } else if (itemId == R.id.nav_add_shelter) {
            intent = new Intent(BaseActivity.this, AddShelterActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
            finish();
        }
    }

    private String getActivityTitle() {
        // Dynamically set the title based on the activity class name
        String className = getClass().getSimpleName();
        switch (className) {
            case "HomeActivity":
                return "Home";
            case "CompassActivity":
                return "Compass";
            case "ListActivity":
                return "Asteroid List";
            case "MapsActivity":
                return "Maps";
            case "AddShelterActivity":
                return "Add Shelter";
            default:
                return "AsteroidConspiracist"; // Default app title
        }
    }

    private void restartActivity() {
        // Explicitly restart the current activity
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
