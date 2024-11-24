package dte.masteriot.mdp.asteroidconspiracist.activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.entity.Asteroid;
import dte.masteriot.mdp.asteroidconspiracist.activity.recyclerview.legend.LegendAdapter;
import dte.masteriot.mdp.asteroidconspiracist.activity.recyclerview.legend.LegendItem;
import dte.masteriot.mdp.asteroidconspiracist.util.AsteroidCalculationHelper;
import dte.masteriot.mdp.asteroidconspiracist.util.LoadingStateManager;
import dte.masteriot.mdp.asteroidconspiracist.util.network.NetworkHelper;
import dte.masteriot.mdp.asteroidconspiracist.viewmodel.HomeViewModel;

public class HomeActivity extends BaseActivity {

    private PieChart pieChart;
    private BarChart barChart;
    private TextView closeApproachDetails, barChartHeading, loadingMessage;
    private ProgressBar loadingSpinner;

    private RecyclerView legendRecyclerView;
    private LegendAdapter legendAdapter;

    private AsteroidCalculationHelper calculationHelper;
    private LoadingStateManager loadingStateManager;
    private NetworkHelper networkHelper;
    private HomeViewModel homeViewModel;

    private boolean isHighlighting = false;
    private Typeface spaceMonoTypeface;
    private Toast currentToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = getLayoutInflater().inflate(R.layout.activity_home, null);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(contentView);

        setupViewModel();
        observeViewModel();
        initializeComponents();
        fetchDataIfNeeded();
        registerNetworkReceiver();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (pieChart.getData() != null) {
            Highlight[] highlights = pieChart.getHighlighted();
            if (highlights != null && highlights.length > 0) {
                outState.putInt("highlightedPieIndex", (int) highlights[0].getX());
            }
        }
        if (barChartHeading.getText() != null) {
            outState.putString("barChartHeadingText", barChartHeading.getText().toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int highlightedPieIndex = savedInstanceState.getInt("highlightedPieIndex", -1);
        String barChartHeadingText = savedInstanceState.getString("barChartHeadingText", null);

        if (highlightedPieIndex >= 0 && pieChart.getData() != null) {
            pieChart.highlightValue(highlightedPieIndex, 0);
        }
        if (barChartHeadingText != null) {
            barChartHeading.setText(barChartHeadingText);
        }
    }

    private void reloadCharts() {
        List<Asteroid> asteroids = homeViewModel.getAsteroidsLiveData().getValue();
        if (asteroids != null && !asteroids.isEmpty()) {
            loadChartData(asteroids);
        }
    }


    private void fetchDataIfNeeded() {
        if (!homeViewModel.isDataLoaded()) {
            homeViewModel.fetchAsteroids(this, networkHelper.isNetworkAvailable());
        }
    }

    private void initializeComponents() {
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        closeApproachDetails = findViewById(R.id.closeApproachDetails);
        barChartHeading = findViewById(R.id.barChartHeading);
        loadingMessage = findViewById(R.id.loadingMessage);
        loadingSpinner = findViewById(R.id.loadingSpinner);

        spaceMonoTypeface = ResourcesCompat.getFont(this, R.font.spacemonoregular);
        calculationHelper = new AsteroidCalculationHelper();
        networkHelper = new NetworkHelper(this);
        loadingStateManager = new LoadingStateManager(loadingMessage, loadingSpinner, pieChart, barChart, closeApproachDetails, barChartHeading);

        loadingStateManager.showLoadingScreen();
    }

    private void setupViewModel() {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    private void observeViewModel() {
        homeViewModel.getAsteroidsLiveData().observe(this, asteroids -> {
            if (asteroids != null && !asteroids.isEmpty()) {
                reloadCharts();
            }
        });

        homeViewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            if (isLoading != null) {
                Log.d("HomeActivity", "Loading state changed: " + isLoading);
                if (isLoading) {
                    loadingStateManager.showLoadingScreen();
                } else {
                    loadingStateManager.hideLoadingScreen();
                }
            }
        });

        homeViewModel.getErrorMessageLiveData().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Log.d("HomeActivity", "Displaying toast for error message: " + errorMessage);
                showToast(errorMessage);
            }
        });

        homeViewModel.getDataSourceLiveData().observe(this, dataSource -> {
            if (dataSource != null) {
                String message = dataSource.equals("network")
                        ? "Asteroid data fetched from the network."
                        : "Asteroid data loaded from a local file.";
                Log.d("HomeActivity", "Displaying toast for data source: " + message);
                showToast(message);
            }
        });
    }

    private void showToast(String message) {
        if (message == null || message.isEmpty()) return;

        if (currentToast != null) {
            currentToast.cancel();
        }

        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    private void loadChartData(List<Asteroid> asteroids) {
        Log.d("HomeActivity", "Loading chart data");
        setupPieChart(asteroids);
        setupBarChart(asteroids);

        findViewById(R.id.pieChartCard).setVisibility(View.VISIBLE);
        findViewById(R.id.barChartCard).setVisibility(View.VISIBLE);
        findViewById(R.id.detailsCard).setVisibility(View.VISIBLE);
    }

    private void setupPieChart(List<Asteroid> asteroids) {
        List<Integer> colors = new ArrayList<>();
        colors.add(getColorByMode(R.color.primary_light, R.color.accent_blue_dark));
        colors.add(getColorByMode(R.color.earth_green_light, R.color.highlight_green));
        colors.add(getColorByMode(R.color.earth_green_dark, R.color.highlight_red));
        colors.add(getColorByMode(R.color.primary_variant, R.color.highlight_pink));
        colors.add(getColorByMode(R.color.earth_yellow_dark, R.color.tertiary_light));
        colors.add(getColorByMode(R.color.earth_muted, R.color.highlight_orange));

        List<PieEntry> entries = new ArrayList<>();
        List<LegendItem> legendItems = new ArrayList<>();
        Map<Asteroid, Float> threatPercentages = calculationHelper.calculateThreatPercentages(asteroids);

        if (threatPercentages == null || threatPercentages.isEmpty()) {
            Log.e("HomeActivity", "Threat percentages map is empty. PieChart cannot be populated.");
            return;
        }

        int i = 0;
        for (Asteroid asteroid : asteroids) {
            float percentage = threatPercentages.get(asteroid);
            entries.add(new PieEntry(percentage, asteroid.getName()));

            String simplifiedName = asteroid.getName().contains(" ") ? asteroid.getName().split(" ")[1] : asteroid.getName();

            legendItems.add(new LegendItem(
                    colors.get(i % colors.size()),
                    simplifiedName + " (" + String.format(Locale.getDefault(), "%.1f%%", percentage) + ")"
            ));
            i++;
        }

        if (entries.isEmpty()) {
            Log.e("HomeActivity", "No data to populate PieChart");
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Asteroid Threat");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(getResources().getColor(R.color.colorOnPrimary, null));
        dataSet.setValueTypeface(spaceMonoTypeface);

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value >= 5.0f ? String.format(Locale.getDefault(), "%.1f%%", value) : "";
            }
        });

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        pieChart.setDrawHoleEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setTouchEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        int colorOnSurface = ResourcesCompat.getColor(getResources(), R.color.colorOnSurface, null);
        pieChart.setEntryLabelColor(colorOnSurface);
        pieChart.setEntryLabelTypeface(spaceMonoTypeface);

        legendRecyclerView = findViewById(R.id.legendRecyclerView);
        legendAdapter = new LegendAdapter(legendItems, index -> {
            if (!isHighlighting) {
                isHighlighting = true;

                pieChart.highlightValue(index, 0);

                updateBarChartForSelectedAsteroid(asteroids.get(index));
                legendRecyclerView.smoothScrollToPosition(index);

                isHighlighting = false;
            }
        });

        legendRecyclerView.setAdapter(legendAdapter);
        legendRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (!isHighlighting) {
                    isHighlighting = true;

                    int selectedIndex = (int) h.getX();
                    updateBarChartForSelectedAsteroid(asteroids.get(selectedIndex));

                    legendRecyclerView.smoothScrollToPosition(selectedIndex);
                    legendAdapter.notifyDataSetChanged();

                    isHighlighting = false;
                }
            }

            @Override
            public void onNothingSelected() {
                // Reset logic if nothing is selected
            }
        });

        pieChart.invalidate();
    }


    private void setupBarChart(List<Asteroid> asteroids) {
        Typeface spaceMonoTypeface = ResourcesCompat.getFont(this, R.font.spacemonoregular);

        Asteroid defaultAsteroid = calculationHelper.getHighestThreatAsteroid(asteroids);

        barChart.getDescription().setEnabled(false);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setDrawAxisLine(true);
        barChart.getXAxis().setTypeface(spaceMonoTypeface);
        barChart.getXAxis().setTextColor(getColorByMode(R.color.colorOnSurface, R.color.colorOnPrimary));

        barChart.getLegend().setEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setDragEnabled(true);
        barChart.setVisibleXRangeMaximum(3);

        updateBarChartForSelectedAsteroid(defaultAsteroid);

        if (defaultAsteroid != null && !defaultAsteroid.getCloseApproachData().isEmpty()) {
            displayCloseApproachDetails(defaultAsteroid, defaultAsteroid.getCloseApproachData().get(0));
        }

        barChart.invalidate();
    }

    private String formatDate(String date) {
        try {
            return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    .format(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date));
        } catch (Exception e) {
            return date;
        }
    }

    private void updateBarChartForSelectedAsteroid(Asteroid asteroid) {
        List<Integer> colors = new ArrayList<>();
        colors.add(getColorByMode(R.color.primary_light, R.color.accent_blue_dark));
        colors.add(getColorByMode(R.color.earth_green_light, R.color.highlight_green));
        colors.add(getColorByMode(R.color.earth_green_dark, R.color.highlight_red));
        colors.add(getColorByMode(R.color.primary_variant, R.color.highlight_pink));
        colors.add(getColorByMode(R.color.earth_yellow_dark, R.color.tertiary_light));
        colors.add(getColorByMode(R.color.earth_muted, R.color.highlight_orange));

        if (asteroid == null) {
            Toast.makeText(this, "No asteroid data to display in bar chart.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        List<Asteroid.CloseApproachData> upcomingApproaches = asteroid.getCloseApproachData().stream()
                .filter(approachData -> {
                    try {
                        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .parse(approachData.getDate()).after(new Date());
                    } catch (Exception e) {
                        return false;
                    }
                })
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .limit(3) // Display only the closest 3 approaches
                .collect(Collectors.toList());

        for (int i = 0; i < upcomingApproaches.size(); i++) {
            Asteroid.CloseApproachData approachData = upcomingApproaches.get(i);

            entries.add(new BarEntry(i, (float) approachData.getMissDistanceKilometers()));
            labels.add(formatDate(approachData.getDate()));
        }

        if (entries.isEmpty()) {
            Log.e("HomeActivity", "No valid close approach data for BarChart");
            barChart.clear();
            return;
        }

        String simplifiedName = asteroid.getName().contains(" ") ? asteroid.getName().split(" ")[1] : asteroid.getName();
        barChartHeading.setText(String.format("Upcoming Close Approaches for %s", simplifiedName));

        BarDataSet dataSet = new BarDataSet(entries, "Miss Distances");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(getColorByMode(R.color.colorOnSurface, R.color.colorOnPrimary));
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(true);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);
        barChart.setData(barData);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setLabelCount(labels.size());
        barChart.getXAxis().setTextColor(getColorByMode(R.color.colorOnSurface, R.color.colorOnPrimary));

        barChart.setDragEnabled(true);
        barChart.setVisibleXRangeMaximum(3);

        barChart.invalidate();

        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int entryIndex = (int) e.getX();
                if (entryIndex < upcomingApproaches.size()) {
                    Asteroid.CloseApproachData selectedData = upcomingApproaches.get(entryIndex);
                    displayCloseApproachDetails(asteroid, selectedData);
                }
            }

            @Override
            public void onNothingSelected() {
                if (!upcomingApproaches.isEmpty()) {
                    displayCloseApproachDetails(asteroid, upcomingApproaches.get(0));
                } else {
                    closeApproachDetails.setText("No close approaches available for the selected asteroid.");
                }
            }
        });
    }

    private void displayCloseApproachDetails(Asteroid asteroid, Asteroid.CloseApproachData closeApproachData) {
        @SuppressLint("DefaultLocale") String details = String.format(
                "Close Approach of %s:\n\nOn %s, this asteroid will approach Earth with a miss distance of approximately %.1f kilometers (%.3f AU, %.3f lunar distances).\n\n" +
                        "The asteroid will be traveling at a speed of %.1f km/s (%.1f mph) relative to Earth.\n\n" +
                        "Orbiting Body: %s.",
                asteroid.getName(),
                formatDate(closeApproachData.getDate()),
                closeApproachData.getMissDistanceKilometers(),
                closeApproachData.getMissDistanceAstronomical(),
                closeApproachData.getMissDistanceLunar(),
                closeApproachData.getRelativeVelocityKmPerSec(),
                closeApproachData.getRelativeVelocityMilesPerHour(),
                closeApproachData.getOrbitingBody()
        );

        closeApproachDetails.setText(details);
    }

    private void registerNetworkReceiver() {
        networkHelper.registerNetworkReceiver(() -> {
            networkHelper.registerNetworkReceiver(() -> {
                if (!homeViewModel.isDataLoaded()) {
                    homeViewModel.fetchAsteroids(this, true);
                }
            });
        });
    }

    private int getColorByMode(int normalColorResId, int highContrastColorResId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isHighContrastEnabled = prefs.getBoolean("high_contrast_mode", false);
        return getResources().getColor(isHighContrastEnabled ? highContrastColorResId : normalColorResId, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkHelper.unregisterNetworkReceiver();
    }
}
