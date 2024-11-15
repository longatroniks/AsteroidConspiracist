package dte.masteriot.mdp.asteroidconspiracist.activity;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = getLayoutInflater().inflate(R.layout.activity_home, null);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(contentView);

        initializeComponents();
        setupViewModel();
        observeViewModel();


        fetchDataIfNeeded();

        registerNetworkReceiver();
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
                loadChartData(asteroids);
            } else {
                Toast.makeText(this, "No asteroid data available.", Toast.LENGTH_SHORT).show();
            }
        });

        homeViewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            if (isLoading) {
                loadingStateManager.showLoadingScreen();
            } else {
                loadingStateManager.hideLoadingScreen();
            }
        });

        homeViewModel.getErrorMessageLiveData().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        homeViewModel.getDataSourceLiveData().observe(this, dataSource -> {
            if (dataSource != null) {
                Toast.makeText(this, "Asteroid data loaded from " + dataSource + ".", Toast.LENGTH_SHORT).show();
            }
        });
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
        Typeface spaceMonoTypeface = ResourcesCompat.getFont(this, R.font.spacemonoregular);

        // Prepare data for PieChart
        List<PieEntry> entries = new ArrayList<>();
        Map<Asteroid, Float> threatPercentages = calculationHelper.calculateThreatPercentages(asteroids);

        // Prepare data for legend
        List<LegendItem> legendItems = new ArrayList<>();
        int[] colors = getResources().getIntArray(R.array.earth_tones);

        int i = 0;
        for (Asteroid asteroid : asteroids) {
            // Add entry for PieChart
            float percentage = threatPercentages.get(asteroid);
            entries.add(new PieEntry(percentage, asteroid.getName()));

            // Simplify name for legend (e.g., use second word or entire name if single-word)
            String simplifiedName = asteroid.getName().contains(" ") ? asteroid.getName().split(" ")[1] : asteroid.getName();

            // Add entry for legend
            legendItems.add(new LegendItem(
                    colors[i % colors.length],
                    simplifiedName + " (" + String.format(Locale.getDefault(), "%.1f%%", percentage) + ")"
            ));
            i++;
        }

        if (entries.isEmpty()) {
            Log.e("HomeActivity", "No data to populate PieChart");
            return;
        }

        // Create PieDataSet
        PieDataSet dataSet = new PieDataSet(entries, "Asteroid Threat");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(getResources().getColor(R.color.colorOnPrimary, null));
        dataSet.setValueTypeface(spaceMonoTypeface);

        // Format percentages
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value >= 5.0f ? String.format(Locale.getDefault(), "%.1f%%", value) : ""; // Display only if >= 5%
            }
        });

        // Create PieData
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // PieChart appearance and settings
        pieChart.setDrawHoleEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false); // No labels on slices
        pieChart.getLegend().setEnabled(false); // Use custom RecyclerView for legend
        pieChart.setTouchEnabled(true); // Enable interaction
        pieChart.setHighlightPerTapEnabled(true); // Enable highlighting on tap

        // Set text and entry label styling
        int colorOnSurface = ResourcesCompat.getColor(getResources(), R.color.colorOnSurface, null);
        pieChart.setEntryLabelColor(colorOnSurface);
        pieChart.setEntryLabelTypeface(spaceMonoTypeface);

        // Setup RecyclerView for the legend
        legendRecyclerView = findViewById(R.id.legendRecyclerView);
        legendAdapter = new LegendAdapter(legendItems, index -> {
            if (!isHighlighting) {
                isHighlighting = true;

                // Highlight the selected PieChart slice
                pieChart.highlightValue(index, 0);

                // Update BarChart and scroll legend
                updateBarChartForSelectedAsteroid(asteroids.get(index));
                legendRecyclerView.smoothScrollToPosition(index);

                isHighlighting = false;
            }
        });

        legendRecyclerView.setAdapter(legendAdapter);
        legendRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Handle PieChart slice selection
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (!isHighlighting) {
                    isHighlighting = true;

                    // Update based on the selected slice
                    int selectedIndex = (int) h.getX();
                    updateBarChartForSelectedAsteroid(asteroids.get(selectedIndex));

                    // Highlight corresponding legend item
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

        // Finalize chart setup
        pieChart.invalidate(); // Refresh chart
    }


    private void setupBarChart(List<Asteroid> asteroids) {
        Typeface spaceMonoTypeface = ResourcesCompat.getFont(this, R.font.spacemonoregular);

        // Get the default asteroid (highest threat) to display initially
        Asteroid defaultAsteroid = calculationHelper.getHighestThreatAsteroid(asteroids);

        // BarChart appearance settings
        int[] colors = getResources().getIntArray(R.array.earth_tones);
        int colorOnSurfaceVariant = ResourcesCompat.getColor(getResources(), R.color.colorOnSurfaceVariant, null);

        barChart.getDescription().setEnabled(false); // Hide description
        barChart.getAxisLeft().setEnabled(false); // Disable left axis
        barChart.getAxisRight().setEnabled(false); // Disable right axis
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM); // X-axis at the bottom
        barChart.getXAxis().setDrawGridLines(false); // No grid lines
        barChart.getXAxis().setDrawAxisLine(true); // Show axis line
        barChart.getXAxis().setTypeface(spaceMonoTypeface); // Use consistent typeface for labels
        barChart.getXAxis().setTextColor(colorOnSurfaceVariant); // Set text color for X-axis

        barChart.getLegend().setEnabled(false); // Disable chart legend
        barChart.setDoubleTapToZoomEnabled(false); // Disable zooming via double-tap
        barChart.setScaleEnabled(false); // Disable scaling
        barChart.setDragEnabled(true); // Enable horizontal drag
        barChart.setVisibleXRangeMaximum(3); // Display up to 3 bars at once

        // Default asteroid data in the BarChart
        updateBarChartForSelectedAsteroid(defaultAsteroid);

        // Display details for the default asteroid's closest approach
        if (defaultAsteroid != null && !defaultAsteroid.getCloseApproachData().isEmpty()) {
            displayCloseApproachDetails(defaultAsteroid, defaultAsteroid.getCloseApproachData().get(0));
        }

        // Refresh chart to apply all settings
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
        if (asteroid == null) {
            Toast.makeText(this, "No asteroid data to display in bar chart.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare BarChart entries and labels for the X-axis
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int[] colors = getResources().getIntArray(R.array.earth_tones);
        int colorOnSurfaceVariant = ResourcesCompat.getColor(getResources(), R.color.colorOnSurfaceVariant, null);

        // Filter and sort the close approach data
        List<Asteroid.CloseApproachData> upcomingApproaches = asteroid.getCloseApproachData().stream()
                .filter(approachData -> {
                    try {
                        // Only include future approaches
                        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .parse(approachData.getDate()).after(new Date());
                    } catch (Exception e) {
                        return false;
                    }
                })
                .sorted((a, b) -> a.getDate().compareTo(b.getDate())) // Sort by date
                .limit(3) // Display only the closest 3 approaches
                .collect(Collectors.toList());

        // Populate entries and labels
        for (int i = 0; i < upcomingApproaches.size(); i++) {
            Asteroid.CloseApproachData approachData = upcomingApproaches.get(i);

            entries.add(new BarEntry(i, (float) approachData.getMissDistanceKilometers()));
            labels.add(formatDate(approachData.getDate())); // Add formatted date as X-axis label
        }

        if (entries.isEmpty()) {
            Log.e("HomeActivity", "No valid close approach data for BarChart");
            barChart.clear(); // Clear chart if no data
            return;
        }

        // Update BarChart heading
        String simplifiedName = asteroid.getName().contains(" ") ? asteroid.getName().split(" ")[1] : asteroid.getName();
        barChartHeading.setText(String.format("Upcoming Close Approaches for %s", simplifiedName));

        // Create BarDataSet for the chart
        BarDataSet dataSet = new BarDataSet(entries, "Miss Distances");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(colorOnSurfaceVariant); // Set color for values
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(true);

        // Create BarData and assign it to the chart
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f); // Set bar width
        barChart.setData(barData);

        // Update X-axis labels
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setLabelCount(labels.size());
        barChart.getXAxis().setTextColor(colorOnSurfaceVariant);

        // Configure the chart for interactivity
        barChart.setDragEnabled(true);
        barChart.setVisibleXRangeMaximum(3); // Show a maximum of 3 bars at a time

        // Refresh the BarChart to apply changes
        barChart.invalidate();

        // Add interaction for displaying close approach details
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int entryIndex = (int) e.getX();
                if (entryIndex < upcomingApproaches.size()) {
                    // Display details for the selected close approach
                    Asteroid.CloseApproachData selectedData = upcomingApproaches.get(entryIndex);
                    displayCloseApproachDetails(asteroid, selectedData);
                }
            }

            @Override
            public void onNothingSelected() {
                if (!upcomingApproaches.isEmpty()) {
                    // Display details for the first close approach if nothing is selected
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
            if (homeViewModel.getAsteroidsLiveData().getValue() == null
                    || homeViewModel.getAsteroidsLiveData().getValue().isEmpty()) {
                // Fetch data when the network becomes available
                homeViewModel.fetchAsteroids(this, true);
            } else {
                Toast.makeText(this, "Network unavailable. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkHelper.unregisterNetworkReceiver();
    }
}
