package dte.masteriot.mdp.asteroidconspiracist.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Switch;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.navigation.NavigationView;
import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.viewmodel.BaseViewModel;

public class BaseActivity extends AppCompatActivity {
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;
    private BaseViewModel baseViewModel;

    private static final String PREF_HIGH_CONTRAST_MODE = "high_contrast_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyThemeBasedOnSettings();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        baseViewModel = new ViewModelProvider(this).get(BaseViewModel.class);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        setupNavigationMenu();
        setupHighContrastSwitch();
        setActivityTitle();

        observeViewModel();
    }

    private void observeViewModel() {
        baseViewModel.getHighContrastMode().observe(this, isEnabled -> {
            // React to high contrast mode changes if needed
            applyThemeBasedOnSettings();
            recreate(); // Restart activity to apply the new theme
        });
    }

    private void setActivityTitle() {
        String className = this.getClass().getSimpleName();
        String title;

        switch (className) {
            case "HomeActivity":
                title = "Home";
                break;
            case "CompassActivity":
                title = "Compass";
                break;
            case "ListActivity":
                title = "Asteroid List";
                break;
            case "MapsActivity":
                title = "Maps";
                break;
            case "AddShelterActivity":
                title = "Add Shelter";
                break;
            default:
                title = className.replace("Activity", ""); // Fallback for unexpected class names
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void setupNavigationMenu() {
        navigationView.setNavigationItemSelectedListener(item -> {
            Intent intent = null;
            if (item.getItemId() == R.id.nav_home) {
                intent = new Intent(this, HomeActivity.class);
            } else if (item.getItemId() == R.id.nav_compass) {
                intent = new Intent(this, CompassActivity.class);
            } else if (item.getItemId() == R.id.nav_asteroid_list) {
                intent = new Intent(this, ListActivity.class);
            } else if (item.getItemId() == R.id.nav_mqtt) {
                intent = new Intent(this, MapsActivity.class);
            } else if (item.getItemId() == R.id.nav_add_shelter) {
                intent = new Intent(this, AddShelterActivity.class);
            }

            if (intent != null) {
                startActivity(intent);
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupHighContrastSwitch() {
        // Find the high contrast switch and set its initial state
        Switch highContrastSwitch = (Switch) navigationView.getMenu().findItem(R.id.switch_high_contrast).getActionView();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean highContrastEnabled = prefs.getBoolean(PREF_HIGH_CONTRAST_MODE, false);
        highContrastSwitch.setChecked(highContrastEnabled);

        // Set listener to toggle high contrast mode
        highContrastSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(PREF_HIGH_CONTRAST_MODE, isChecked).apply();
            baseViewModel.setHighContrastMode(isChecked);
        });
    }

    private void applyThemeBasedOnSettings() {
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

    protected void enableBackButton(boolean enableBack) {
        if (getSupportActionBar() != null) {
            if (enableBack) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            } else {
                ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                        this, drawerLayout, toolbar,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close);
                drawerLayout.addDrawerListener(toggle);
                toggle.syncState();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
