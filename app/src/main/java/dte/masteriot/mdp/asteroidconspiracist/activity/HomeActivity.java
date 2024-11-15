package dte.masteriot.mdp.asteroidconspiracist.activities;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
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
import dte.masteriot.mdp.asteroidconspiracist.entities.Asteroid;
import dte.masteriot.mdp.asteroidconspiracist.activities.recyclerview.legend.LegendAdapter;
import dte.masteriot.mdp.asteroidconspiracist.activities.recyclerview.legend.LegendItem;
import dte.masteriot.mdp.asteroidconspiracist.repos.AsteroidRepository;
import dte.masteriot.mdp.asteroidconspiracist.services.NeoWsAPIService;
import dte.masteriot.mdp.asteroidconspiracist.utils.AsteroidCalculationHelper;
import dte.masteriot.mdp.asteroidconspiracist.utils.LoadingStateManager;
import dte.masteriot.mdp.asteroidconspiracist.utils.network.NetworkHelper;

public class HomeActivity extends BaseActivity {

    private PieChart pieChart;
    private BarChart barChart;
    private TextView closeApproachDetails, barChartHeading, loadingMessage;
    private ProgressBar loadingSpinner;

    private List<Asteroid> asteroids = new ArrayList<>();

    private RecyclerView legendRecyclerView;
    private LegendAdapter legendAdapter;

    private final AsteroidCalculationHelper calculationHelper = new AsteroidCalculationHelper();
    private NetworkHelper networkHelper;
    private LoadingStateManager loadingStateManager;

    private boolean isHighlighting = false;
    private boolean isDataLoading = true;

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = getLayoutInflater().inflate(R.layout.activity_home, null);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(contentView);

        // Initialize UI components
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        closeApproachDetails = findViewById(R.id.closeApproachDetails);
        barChartHeading = findViewById(R.id.barChartHeading);
        loadingMessage = findViewById(R.id.loadingMessage);
        loadingSpinner = findViewById(R.id.loadingSpinner);

        networkHelper = new NetworkHelper(this);
        loadingStateManager = new LoadingStateManager(loadingMessage, loadingSpinner, pieChart, barChart);

        loadingStateManager.showLoadingScreen();
        registerNetworkReceiver();

        fetchAsteroidData();
    }

    private void registerNetworkReceiver() {
        networkHelper.registerNetworkReceiver(() -> {
            if (!isDataLoading) {
                loadingMessage.setText(R.string.LOADING_MESSAGE);
                isDataLoading = true;
                fetchAsteroidData();
            }
        });
    }

    private void fetchAsteroidData() {
        Log.d("HomeActivity", "Fetching asteroid data...");
        List<Asteroid> asteroidList = AsteroidRepository.getInstance().getAsteroidList();

        if (!asteroidList.isEmpty()) {
            this.asteroids = asteroidList;
            Log.d("HomeActivity", "Data loaded from cache. Loading charts...");
            loadChartData();
        } else if (networkHelper.isNetworkAvailable()) {
            NeoWsAPIService apiService = new NeoWsAPIService();
            apiService.fetchAndStoreAsteroids(this, new NeoWsAPIService.NeoWsAPIResponse() {
                @Override
                public void onResponse(boolean isFromCache) {
                    asteroids = AsteroidRepository.getInstance().getAsteroidList();
                    if (!asteroids.isEmpty()) {
                        Log.d("HomeActivity", "Data fetched from API. Loading charts...");
                        loadChartData();
                    } else {
                        loadingStateManager.showNetworkError("No data available.");
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e("HomeActivity", "Error fetching data: " + error);
                    loadingStateManager.showNetworkError("Failed to fetch data.");
                }
            });
        } else {
            loadingStateManager.showNetworkError("Waiting for an internet connection...");
        }
    }

    private void loadChartData() {
        if (!asteroids.isEmpty()) {
            setupPieChart();
            setupBarChart();
            loadingStateManager.hideLoadingScreen();
            Log.d("HomeActivity", "Charts loaded and screen displayed.");
        } else {
            Log.w("HomeActivity", "Asteroid data is empty; showing network error.");
            loadingStateManager.showNetworkError("Waiting for an internet connection...");
        }
    }

    private void setupPieChart() {
        Typeface spaceMonoTypeface = ResourcesCompat.getFont(this, R.font.spacemonoregular);
        ArrayList<PieEntry> entries = new ArrayList<>();
        Map<Asteroid, Float> threatPercentages = calculationHelper.calculateThreatPercentages(asteroids);

        List<LegendItem> legendItems = new ArrayList<>();
        int[] colors = getResources().getIntArray(R.array.earth_tones);

        int i = 0;
        for (Asteroid asteroid : asteroids) {
            float percentage = threatPercentages.get(asteroid);
            entries.add(new PieEntry(percentage, asteroid.getName()));

            String simplifiedName = asteroid.getName().split(" ")[1];
            legendItems.add(new LegendItem(colors[i % colors.length], simplifiedName + " (" + String.format("%.1f", percentage) + "%)"));
            i++;
        }

        PieDataSet dataSet = getPieDataSet(entries, colors, spaceMonoTypeface, this);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setDrawHoleEnabled(false);
        pieChart.setUsePercentValues(true);

        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setEnabled(false);

        // Set description and entry label colors
        int colorOnSurface = ResourcesCompat.getColor(getResources(), R.color.colorOnSurface, null);
        pieChart.setEntryLabelColor(colorOnSurface);
        pieChart.setEntryLabelTypeface(spaceMonoTypeface);
        pieChart.getDescription().setTypeface(spaceMonoTypeface);
        pieChart.getLegend().setTypeface(spaceMonoTypeface);

        legendRecyclerView = findViewById(R.id.legendRecyclerView);
        legendAdapter = new LegendAdapter(legendItems, this::onLegendItemSelected);
        legendRecyclerView.setAdapter(legendAdapter);
        legendRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (!isHighlighting) {
                    isHighlighting = true;
                    int index = (int) h.getX();
                    onLegendItemSelected(index);
                    updateBarChartForSelectedAsteroid(asteroids.get(index));
                    isHighlighting = false;
                }
            }

            @Override
            public void onNothingSelected() {
                // Reset selection if needed
            }
        });

        pieChart.invalidate();
    }

    private static @NonNull PieDataSet getPieDataSet(ArrayList<PieEntry> entries, int[] colors, Typeface spaceMonoTypeface, Context context) {
        PieDataSet dataSet = new PieDataSet(entries, "Asteroid Impact Threat");
        dataSet.setColors(colors);
        // Use context to access the color resource
        dataSet.setValueTextColor(ResourcesCompat.getColor(context.getResources(), R.color.colorOnPrimary, null));
        dataSet.setValueTextSize(16f);
        dataSet.setValueTypeface(spaceMonoTypeface);

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value < 5.0f ? "" : String.format("%.1f%%", value);
            }
        });
        return dataSet;
    }

    private void onLegendItemSelected(int index) {
        if (!isHighlighting) {
            isHighlighting = true;
            pieChart.highlightValue(index, 0);
            legendRecyclerView.smoothScrollToPosition(index);
            legendAdapter.notifyDataSetChanged();
            updateBarChartForSelectedAsteroid(asteroids.get(index));
            isHighlighting = false;
        }
    }

    private void setupBarChart() {
        Typeface spaceMonoTypeface = ResourcesCompat.getFont(this, R.font.spacemonoregular);
        int[] colors = getResources().getIntArray(R.array.earth_tones);
        int colorOnSurfaceVariant = ResourcesCompat.getColor(getResources(), R.color.colorOnSurfaceVariant, null);

        barChart.getDescription().setEnabled(false);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setDrawAxisLine(true);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        // Set color and font for X-axis labels
        barChart.getXAxis().setTextColor(colorOnSurfaceVariant);
        barChart.getXAxis().setTypeface(spaceMonoTypeface);

        barChart.getLegend().setEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setScaleEnabled(false);

        // Set color and font for description text
        barChart.getDescription().setTextColor(colorOnSurfaceVariant);
        barChart.getDescription().setTypeface(spaceMonoTypeface);

        BarDataSet dataSet = new BarDataSet(new ArrayList<>(), "Next Close Approaches");
        dataSet.setColors(colors);

        // Set color and font for bar values
        dataSet.setValueTextColor(colorOnSurfaceVariant);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTypeface(spaceMonoTypeface);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);
        barChart.setData(barData);

        // Display default asteroid's data if available
        Asteroid defaultAsteroid = getHighestThreatAsteroid();
        updateBarChartForSelectedAsteroid(defaultAsteroid);

        if (defaultAsteroid != null && !defaultAsteroid.getCloseApproachData().isEmpty()) {
            displayCloseApproachDetails(defaultAsteroid, defaultAsteroid.getCloseApproachData().get(0));
        }

        barChart.invalidate(); // Refresh the chart to apply settings
    }

    private void updateBarChartForSelectedAsteroid(Asteroid asteroid) {
        if (asteroid == null) {
            Toast.makeText(HomeActivity.this, "No asteroid data to display in bar chart.", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        List<String> closeApproachDates = new ArrayList<>();
        int[] colors = getResources().getIntArray(R.array.earth_tones);
        int colorOnSurfaceVariant = ResourcesCompat.getColor(getResources(), R.color.colorOnSurfaceVariant, null);

        String simplifiedName = asteroid.getName().split(" ")[1];
        barChartHeading.setText(String.format("Upcoming for %s", simplifiedName));

        Date today = new Date();
        List<Asteroid.CloseApproachData> upcomingApproaches = asteroid.getCloseApproachData().stream()
                .filter(approachData -> {
                    try {
                        return new SimpleDateFormat("yyyy-MM-dd").parse(approachData.getDate()).after(today);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .limit(3)
                .collect(Collectors.toList());

        int i = 0;
        for (Asteroid.CloseApproachData approachData : upcomingApproaches) {
            entries.add(new BarEntry(i, (float) approachData.getMissDistanceKilometers()));
            closeApproachDates.add(formatDate(approachData.getDate()));  // Ensure only needed dates are added
            i++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Next Close Approaches");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(colorOnSurfaceVariant);  // Set color for the distance values
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(true);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);
        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(closeApproachDates));  // Match exactly
        barChart.getXAxis().setLabelCount(entries.size());  // Ensure label count matches entry count
        barChart.getXAxis().setTextColor(colorOnSurfaceVariant);  // Set color for date labels
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
                    Asteroid.CloseApproachData defaultCloseApproach = upcomingApproaches.get(0);
                    displayCloseApproachDetails(asteroid, defaultCloseApproach);
                } else {
                    closeApproachDetails.setText("No close approaches available for the selected asteroid.");
                }
            }
        });
    }

    private void displayCloseApproachDetails(Asteroid asteroid, Asteroid.CloseApproachData closeApproachData) {
        String details = "Close Approach of " + asteroid.getName() + ":\n\n" +
                "On " + formatDate(closeApproachData.getDateFull()) + ", this asteroid will approach Earth with a closest miss distance of approximately " +
                abbreviateNumber(closeApproachData.getMissDistanceKilometers()) + " kilometers, which is about " +
                String.format("%.3f", closeApproachData.getMissDistanceAstronomical()) + " Astronomical Units or " +
                String.format("%.3f", closeApproachData.getMissDistanceLunar()) + " times the distance between Earth and the Moon.\n\n" +
                "The asteroid will be traveling at a relative speed of " + String.format("%.3f", closeApproachData.getRelativeVelocityKmPerSec()) +
                " kilometers per second (" + String.format("%.3f", closeApproachData.getRelativeVelocityMilesPerHour()) +
                " miles per hour) as it passes by.\n\n" +
                "This close approach will occur with " + closeApproachData.getOrbitingBody() +
                " as the primary orbiting body in relation to this asteroid.";

        closeApproachDetails.setText(details);
    }


    private String formatDate(String date) {
        try {
            return dateFormatter.format(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date));
        } catch (Exception e) {
            return date;
        }
    }

    private String abbreviateNumber(double value) {
        if (value >= 1_000_000_000) return String.format("%.1fB", value / 1_000_000_000);
        if (value >= 1_000_000) return String.format("%.1fM", value / 1_000_000);
        if (value >= 1_000) return String.format("%.1fK", value / 1_000);
        return String.format("%.1f", value);
    }

    private Asteroid getHighestThreatAsteroid() {
        return asteroids.isEmpty() ? null : asteroids.stream().max((a, b) -> Integer.compare(calculationHelper.calculateThreatScore(a), calculationHelper.calculateThreatScore(b))).orElse(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkHelper.unregisterNetworkReceiver();

    }
}
