package dte.masteriot.mdp.asteroidconspiracist.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import dte.masteriot.mdp.asteroidconspiracist.R;
import dte.masteriot.mdp.asteroidconspiracist.models.Asteroid;
import dte.masteriot.mdp.asteroidconspiracist.recyclerview.legend.LegendAdapter;
import dte.masteriot.mdp.asteroidconspiracist.recyclerview.legend.LegendItem;
import dte.masteriot.mdp.asteroidconspiracist.services.NeoWsAPIService;
import dte.masteriot.mdp.asteroidconspiracist.utils.AsteroidParser;

public class HomeActivity extends BaseActivity {

    private PieChart pieChart;
    private BarChart barChart;
    private TextView closeApproachDetails;
    private List<Asteroid> asteroids = new ArrayList<>();
    private RecyclerView legendRecyclerView;
    private LegendAdapter legendAdapter;
    private boolean isHighlighting = false;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = getLayoutInflater().inflate(R.layout.activity_home, null);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(contentView);

        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        closeApproachDetails = findViewById(R.id.closeApproachDetails);

        fetchAsteroidData();
    }

    private void fetchAsteroidData() {
        NeoWsAPIService apiService = new NeoWsAPIService();
        apiService.getAsteroids(new NeoWsAPIService.NeoWsAPIResponse() {
            @Override
            public void onResponse(String response) {
                asteroids = AsteroidParser.parseAsteroids(response);
                if (asteroids.isEmpty()) {
                    Toast.makeText(HomeActivity.this, "No asteroid data available.", Toast.LENGTH_LONG).show();
                    return;
                }
                setupPieChart();
                setupBarChart();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HomeActivity.this, "Error fetching asteroid data: " + error + ". Please check your connection or try again later.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupPieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        Map<Asteroid, Float> threatPercentages = calculateThreatPercentages(asteroids);

        List<LegendItem> legendItems = new ArrayList<>();
        int[] colors = generateColors(asteroids.size());

        int i = 0;
        for (Asteroid asteroid : asteroids) {
            float percentage = threatPercentages.get(asteroid);
            entries.add(new PieEntry(percentage, asteroid.getName()));

            String simplifiedName = asteroid.getName().split(" ")[1];
            legendItems.add(new LegendItem(colors[i], simplifiedName + " (" + (int) percentage + "%)"));
            i++;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Asteroid Impact Threat");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setEnabled(false);

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
        barChart.getDescription().setEnabled(false);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setDrawAxisLine(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setScaleEnabled(false);

        updateBarChartForSelectedAsteroid(getHighestThreatAsteroid());
    }

    private void updateBarChartForSelectedAsteroid(Asteroid asteroid) {
        if (asteroid == null) {
            Toast.makeText(HomeActivity.this, "No asteroid data to display in bar chart.", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        List<String> closeApproachDates = new ArrayList<>();

        // Today's date
        Date today = new Date();

        // Filter to get only future close approaches, then sort by date in ascending order to get the next three
        List<Asteroid.CloseApproachData> upcomingApproaches = asteroid.getCloseApproachData().stream()
                .filter(approachData -> {
                    try {
                        return new SimpleDateFormat("yyyy-MM-dd").parse(approachData.getDate()).after(today);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))  // Sort by ascending date
                .limit(3)  // Get the next three
                .collect(Collectors.toList());

        int i = 0;
        for (Asteroid.CloseApproachData approachData : upcomingApproaches) {
            entries.add(new BarEntry(i, (float) approachData.getMissDistanceKilometers()));
            closeApproachDates.add(formatDate(approachData.getDate()));
            i++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Next Close Approaches");
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(true);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);
        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(closeApproachDates));
        barChart.setDragEnabled(true);
        barChart.setVisibleXRangeMaximum(3);
        barChart.invalidate();

        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int entryIndex = (int) e.getX();
                if (entryIndex < asteroid.getCloseApproachData().size()) {
                    Asteroid.CloseApproachData selectedData = asteroid.getCloseApproachData().get(entryIndex);
                    displayCloseApproachDetails(selectedData);
                }
            }

            @Override
            public void onNothingSelected() {
                closeApproachDetails.setText("Select a bar to see close approach details");
            }
        });
    }


    private void displayCloseApproachDetails(Asteroid.CloseApproachData closeApproachData) {
        String details = "Close Approach Summary:\n\n" +
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

    // Remaining methods (calculateThreatScore, calculateThreatPercentages, etc.) stay the same.
    private int calculateThreatScore(Asteroid asteroid) {
        double distanceWeight = 0.3;
        double velocityWeight = 0.2;
        double diameterWeight = 0.2;
        double magnitudeWeight = 0.1;
        double hazardWeight = 0.1;
        double closeApproachWeight = 0.1;

        double distanceScore = 1 - normalize(asteroid.getDistance(), 0, 10000000);
        double velocityScore = normalize(asteroid.getVelocity(), 0, 50000);
        double diameterScore = normalize(asteroid.getMaxDiameterMeters(), 0, 10000);
        double magnitudeScore = normalize(30 - asteroid.getAbsoluteMagnitude(), 0, 30);
        double hazardScore = asteroid.isPotentiallyHazardous() ? 0.1 : 0;

        double closestApproachDistance = Double.MAX_VALUE;
        double highestRelativeVelocity = 0;
        for (Asteroid.CloseApproachData approachData : asteroid.getCloseApproachData()) {
            closestApproachDistance = Math.min(closestApproachDistance, approachData.getMissDistanceKilometers());
            highestRelativeVelocity = Math.max(highestRelativeVelocity, approachData.getRelativeVelocityKmPerSec());
        }

        double closeApproachScore = (1 - normalize(closestApproachDistance, 0, 10000000)) * 0.5 +
                normalize(highestRelativeVelocity, 0, 50000) * 0.5;

        double rawScore = (distanceScore * distanceWeight) + (velocityScore * velocityWeight) +
                (diameterScore * diameterWeight) + (magnitudeScore * magnitudeWeight) +
                (hazardScore * hazardWeight) + (closeApproachScore * closeApproachWeight);

        return (int) Math.min(100, Math.max(1, rawScore * 100));
    }

    private Map<Asteroid, Float> calculateThreatPercentages(List<Asteroid> asteroids) {
        Map<Asteroid, Float> threatPercentages = new HashMap<>();
        int totalScore = 0;

        for (Asteroid asteroid : asteroids) {
            int threatScore = calculateThreatScore(asteroid);
            totalScore += threatScore;
            threatPercentages.put(asteroid, (float) threatScore);
        }

        for (Asteroid asteroid : threatPercentages.keySet()) {
            float percentage = (threatPercentages.get(asteroid) / totalScore) * 100;
            threatPercentages.put(asteroid, percentage);
        }

        return threatPercentages;
    }

    private double normalize(double value, double min, double max) {
        if (max == min) return 1;
        return (value - min) / (max - min);
    }

    private int[] generateColors(int count) {
        int[] colors = new int[count];
        for (int i = 0; i < count; i++) {
            colors[i] = Color.HSVToColor(new float[]{(i * 360f / count), 0.8f, 0.9f});
        }
        return colors;
    }

    private Asteroid getHighestThreatAsteroid() {
        return asteroids.isEmpty() ? null : asteroids.stream().max((a, b) -> Integer.compare(calculateThreatScore(a), calculateThreatScore(b))).orElse(null);
    }
}
