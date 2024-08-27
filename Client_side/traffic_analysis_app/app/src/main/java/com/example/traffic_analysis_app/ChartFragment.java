package com.example.traffic_analysis_app;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Parcelable;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.apache.commons.math3.stat.inference.TTest;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartFragment extends Fragment {

    private ArrayList<GeoPoint> selectedGeoPoints;
    TextView warningText, postHocTest1, testStatisticsPostHoc1, pValuePostHoc1, interpretationPostHoc1, postHocTest2, testStatisticsPostHoc2, pValuePostHoc2, interpretationPostHoc2;
    TextView usedStatTest, testStatistics, pValue, interpretationMain, postHocTest3, testStatisticsPostHoc3, pValuePostHoc3, interpretationPostHoc3;
    public boolean descriptive_stats = true;
    CustomBarChart barChart;
    LinearLayout statTestLayout, postHocLayout;
    ImageButton typeOfStats;
    ArrayList<String> labels = new ArrayList<>();
    private boolean foundData = false;

    // Add a method to pass data to the fragment
    public static ChartFragment newInstance(List<GeoPoint> geoPoints) {
        ChartFragment fragment = new ChartFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList("geoPoints", (ArrayList<? extends Parcelable>) geoPoints);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            selectedGeoPoints = getArguments().getParcelableArrayList("geoPoints");
            //typeOfStats.setImageResource(R.drawable.graph_icon);
            //if(selectedGeoPoints.size() > 0) { getGeoPointData(selectedGeoPoints); }
        } else {
            //typeOfStats.setImageResource(R.drawable.select_icon);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        // Find elements in the fragment layout
        warningText = view.findViewById(R.id.warningText);
        barChart = view.findViewById(R.id.barChart);
        typeOfStats = view.findViewById(R.id.typeOfStats);
        statTestLayout = view.findViewById(R.id.statTest);
        usedStatTest = view.findViewById(R.id.usedStatTest);
        testStatistics = view.findViewById(R.id.testStatistics);
        pValue = view.findViewById(R.id.pValue);
        interpretationMain = view.findViewById(R.id.interpretationMain);

        // Post-HOC elements
        postHocLayout = view.findViewById(R.id.postHocLayout);
        postHocTest1 = view.findViewById(R.id.postHocTest1);
        testStatisticsPostHoc1 = view.findViewById(R.id.testStatisticsPostHoc1);
        pValuePostHoc1 = view.findViewById(R.id.pValuePostHoc1);
        interpretationPostHoc1 = view.findViewById(R.id.interpretationPostHoc1);
        postHocTest2 = view.findViewById(R.id.postHocTest2);
        testStatisticsPostHoc2 = view.findViewById(R.id.testStatisticsPostHoc2);
        pValuePostHoc2 = view.findViewById(R.id.pValuePostHoc2);
        interpretationPostHoc2 = view.findViewById(R.id.interpretationPostHoc2);
        postHocTest3 = view.findViewById(R.id.postHocTest3);
        testStatisticsPostHoc3 = view.findViewById(R.id.testStatisticsPostHoc3);
        pValuePostHoc3 = view.findViewById(R.id.pValuePostHoc3);
        interpretationPostHoc3 = view.findViewById(R.id.interpretationPostHoc3);

        if(selectedGeoPoints != null){
            warningText.setVisibility(View.GONE);
            getGeoPointData(selectedGeoPoints);
            //barChart.setVisibility(View.VISIBLE);
        }

        /*ArrayList<Float> means = new ArrayList<>();
        means.add(4.5f);
        means.add(2.3f);
        means.add(13f);
        means.add(1.5f);
        means.add(5.3f);
        means.add(2.7f);
        means.add(1.8f);*/

        labels.add("Bicikl");
        labels.add("Bus");
        labels.add("Auto");
        labels.add("Motor");
        labels.add("Ljudi");
        labels.add("Vlak");
        labels.add("Kamion");

        /*ArrayList<Float> deviations = new ArrayList<>();
        deviations.add(0.7f);
        deviations.add(1.2f);
        deviations.add(3.5f);
        deviations.add(1.0f);
        deviations.add(2.3f);
        deviations.add(0.6f);
        deviations.add(0.5f);*/

        typeOfStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedGeoPoints == null){
                    typeOfStats.setImageResource(R.drawable.select_icon);
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).hideChartFragment();
                    }
                } else {
                    getGeoPointData(selectedGeoPoints);
                    if(foundData) {
                        if (descriptive_stats) {
                            typeOfStats.setImageResource(R.drawable.compare_icon);
                            if (foundData) {
                                warningText.setVisibility(View.GONE);
                                barChart.setVisibility(View.VISIBLE);
                            }
                            descriptive_stats = false;
                        } else {
                            typeOfStats.setImageResource(R.drawable.graph_icon);
                            if (foundData) {
                                warningText.setVisibility(View.GONE);
                                statTestLayout.setVisibility(View.VISIBLE);
                                inferentialStatistics(usedStatTest, testStatistics, pValue, interpretationMain);
                            }
                            descriptive_stats = true;
                        }
                    } else {
                        typeOfStats.setImageResource(R.drawable.select_icon);
                    }
                }
            }
        });

        return view;
    }

    private void inferentialStatistics(TextView usedStatTest, TextView testStatistics, TextView pValue, TextView interpretationMain) {
        List<GeoPointData> geoPointData = new ArrayList<>();

        for(GeoPoint geoPoint : selectedGeoPoints) {
            getTestData(geoPoint, geoPointData);
        }

        if(foundData && geoPointData != null){
            warningText.setVisibility(View.GONE);
            barChart.setVisibility(View.GONE);
            statTestLayout.setVisibility(View.VISIBLE);

            // 2 groups - Wilcoxon Signed Rank test or paired t-test
            if(geoPointData.size() == 2){
                boolean normalityCars = true, normalityBikes = true, normalityPeople = true;
                for(GeoPointData data : geoPointData){
                    if(data.car_normality < 0.05){ normalityCars = false; }
                    if(data.bike_normality < 0.05){ normalityBikes = false; }
                    if(data.people_normality < 0.05){ normalityPeople = false; }
                }

                if(normalityCars){
                    double[] group1 = new double[geoPointData.get(0).cars.size()];
                    double[] group2 = new double[geoPointData.get(1).cars.size()];

                    for (int i = 0; i < geoPointData.get(0).cars.size(); i++) {
                        group1[i] = geoPointData.get(0).cars.get(i);
                    }
                    for (int i = 0; i < geoPointData.get(1).cars.size(); i++) {
                        group2[i] = geoPointData.get(1).cars.get(i);
                    }

                    double pValueTTest = 0.0, fValueTTest = 0.0;

                    performPairedTTest(group1, group2, pValueTTest, fValueTTest);
                    usedStatTest.setText("Upareni t-test");
                    testStatistics.setText(String.valueOf(fValueTTest));
                    pValue.setText(String.valueOf(pValueTTest));
                    if(pValueTTest < 0.05) {
                        interpretationMain.setText("Postoji statistički značajna razlika između srednjih prosječnih vrijednosti dviju uspoređenih grupa.");
                    } else {
                        interpretationMain.setText("Nema statistički značajne razlike između srednjih prosječnih vrijednosti dviju uspoređenih grupa.");
                    }
                } else {
                    double[] group1 = new double[geoPointData.get(0).cars.size()];
                    double[] group2 = new double[geoPointData.get(1).cars.size()];

                    for (int i = 0; i < geoPointData.get(0).cars.size(); i++) {
                        group1[i] = geoPointData.get(0).cars.get(i);
                    }
                    for (int i = 0; i < geoPointData.get(1).cars.size(); i++) {
                        group2[i] = geoPointData.get(1).cars.get(i);
                    }

                    double pValueWilcoxon = 0.0, fValueWilcoxon = 0.0;

                    performWilcoxonSignedRankTest(group1, group2, pValueWilcoxon, fValueWilcoxon);
                    if(pValueWilcoxon < 0.05) {
                        interpretationMain.setText("Postoji statistički značajna razlika između medijana dviju uspoređenih grupa.");
                    } else {
                        interpretationMain.setText("Nema statistički značajne razlike između medijana dviju uspoređenih grupa.");
                    }
                }

                if(normalityBikes){
                    double[] group1 = new double[geoPointData.get(0).bikes.size()];
                    double[] group2 = new double[geoPointData.get(1).bikes.size()];

                    for (int i = 0; i < geoPointData.get(0).bikes.size(); i++) {
                        group1[i] = geoPointData.get(0).bikes.get(i);
                    }
                    for (int i = 0; i < geoPointData.get(1).bikes.size(); i++) {
                        group2[i] = geoPointData.get(1).bikes.get(i);
                    }

                    double pValueTTest = 0.0, fValueTTest = 0.0;

                    performPairedTTest(group1, group2, pValueTTest, fValueTTest);

                    if(pValueTTest < 0.05) {
                        interpretationMain.setText("Postoji statistički značajna razlika između srednjih prosječnih vrijednosti dviju uspoređenih grupa.");
                    } else {
                        interpretationMain.setText("Nema statistički značajne razlike između srednjih prosječnih vrijednosti dviju uspoređenih grupa.");
                    }
                } else {
                    double[] group1 = new double[geoPointData.get(0).bikes.size()];
                    double[] group2 = new double[geoPointData.get(1).bikes.size()];

                    for (int i = 0; i < geoPointData.get(0).bikes.size(); i++) {
                        group1[i] = geoPointData.get(0).bikes.get(i);
                    }
                    for (int i = 0; i < geoPointData.get(1).bikes.size(); i++) {
                        group2[i] = geoPointData.get(1).bikes.get(i);
                    }

                    double pValueWilcoxon = 0.0, fValueWilcoxon = 0.0;

                    performWilcoxonSignedRankTest(group1, group2, pValueWilcoxon, fValueWilcoxon);
                    if(pValueWilcoxon < 0.05) {
                        interpretationMain.setText("Postoji statistički značajna razlika između medijana dviju uspoređenih grupa.");
                    } else {
                        interpretationMain.setText("Nema statistički značajne razlike između medijana dviju uspoređenih grupa.");
                    }
                }

                if(normalityPeople){
                    double[] group1 = new double[geoPointData.get(0).people.size()];
                    double[] group2 = new double[geoPointData.get(1).people.size()];

                    for (int i = 0; i < geoPointData.get(0).people.size(); i++) {
                        group1[i] = geoPointData.get(0).people.get(i);
                    }
                    for (int i = 0; i < geoPointData.get(1).people.size(); i++) {
                        group2[i] = geoPointData.get(1).people.get(i);
                    }

                    double pValueTTest = 0.0, fValueTTest = 0.0;

                    performPairedTTest(group1, group2, pValueTTest, fValueTTest);

                    if(pValueTTest < 0.05) {
                        interpretationMain.setText("Postoji statistički značajna razlika između srednjih prosječnih vrijednosti dviju uspoređenih grupa.");
                    } else {
                        interpretationMain.setText("Nema statistički značajne razlike između srednjih prosječnih vrijednosti dviju uspoređenih grupa.");
                    }
                } else {
                    double[] group1 = new double[geoPointData.get(0).people.size()];
                    double[] group2 = new double[geoPointData.get(1).people.size()];

                    for (int i = 0; i < geoPointData.get(0).people.size(); i++) {
                        group1[i] = geoPointData.get(0).people.get(i);
                    }
                    for (int i = 0; i < geoPointData.get(1).people.size(); i++) {
                        group2[i] = geoPointData.get(1).people.get(i);
                    }

                    double pValueWilcoxon = 0.0, fValueWilcoxon = 0.0;

                    performWilcoxonSignedRankTest(group1, group2, pValueWilcoxon, fValueWilcoxon);
                    if(pValueWilcoxon < 0.05) {
                        interpretationMain.setText("Postoji statistički značajna razlika između medijana dviju uspoređenih grupa.");
                    } else {
                        interpretationMain.setText("Nema statistički značajne razlike između medijana dviju uspoređenih grupa.");
                    }
                }
            } // more than 2 groups - ANOVA test or Friedman test  (with post-HOC analysis if needed)
             else if(geoPointData.size() > 2){
                boolean anovaCheckCars = true, anovaCheckBikes = true, anovaCheckPeople = true;
                for(GeoPointData data : geoPointData){
                    if(data.car_normality < 0.05){
                        anovaCheckCars = false;
                    }
                    if(data.bike_normality < 0.05){
                        anovaCheckBikes = false;
                    }
                    if(data.people_normality < 0.05){
                        anovaCheckPeople = false;
                    }
                }

                // Check for sphericity using Mauchly's test
                if(anovaCheckCars){
                    for(GeoPointData data : geoPointData){
                        double[][] mauchlyData = convertSingleListToDoubleArray(data.cars);
                        if(performMauchlyTest(mauchlyData) < 0.05){
                            anovaCheckCars = false;
                        }
                    }
                }
                if(anovaCheckBikes){
                    for(GeoPointData data : geoPointData){
                        double[][] mauchlyData = convertSingleListToDoubleArray(data.bikes);
                        if(performMauchlyTest(mauchlyData) < 0.05){
                            anovaCheckBikes = false;
                        }
                    }
                }
                if(anovaCheckPeople){
                    for(GeoPointData data : geoPointData){
                        double[][] mauchlyData = convertSingleListToDoubleArray(data.cars);
                        if(performMauchlyTest(mauchlyData) < 0.05){
                            anovaCheckPeople = false;
                        }
                    }
                }

                // Check for homogeneity using Levene test
                if(anovaCheckCars){
                    for(GeoPointData data : geoPointData){
                        List<double[]> leveneData = convertListToMultipleDoubleArrays(data.cars, 3);
                        if(performLeveneTest(leveneData) < 0.05){
                            anovaCheckCars = false;
                        }
                    }
                }
                if(anovaCheckBikes){
                    for(GeoPointData data : geoPointData){
                        List<double[]> leveneData = convertListToMultipleDoubleArrays(data.bikes, 3);
                        if(performLeveneTest(leveneData) < 0.05){
                            anovaCheckBikes = false;
                        }
                    }
                }
                if(anovaCheckPeople){
                    for(GeoPointData data : geoPointData){
                        List<double[]> leveneData = convertListToMultipleDoubleArrays(data.people, 3);
                        if(performLeveneTest(leveneData) < 0.05){
                            anovaCheckPeople = false;
                        }
                    }
                }

                // Finally perform ANOVA test of Friedman test based on previous test results
                if(anovaCheckCars){
                    double pValueCars = 0.0;
                    double fValueCars = 0.0;
                    List<double[]> anovaData = new ArrayList<>();
                    for(GeoPointData data : geoPointData){
                        double[] convertedData = convertListToDoubleArray(data.cars);
                        anovaData.add(convertedData);
                    }
                    performAnova(anovaData, pValueCars, fValueCars);
                    usedStatTest.setText("ANOVA test");
                    testStatistics.setText(String.valueOf(fValueCars));
                    pValue.setText(String.valueOf(pValueCars));

                    // Start post-HOC analysis
                    if(pValueCars < 0.05){
                        for(int i = 1; i < anovaData.size(); i++){
                            double pValueHOC = 0.0, fValueHOC = 0.0;
                            double[] group1 = anovaData.get(i - 1);
                            double[] group2 = anovaData.get(i);
                            postHOC(group1, group2, true, pValueHOC, fValueHOC);
                        }
                    }
                } else {
                    double pValueCars = 0.0;
                    double fValueCars = 0.0;
                    List<double[]> friedmanData = new ArrayList<>();
                    for(GeoPointData data : geoPointData){
                        double[] convertedData = convertListToDoubleArray(data.cars);
                        friedmanData.add(convertedData);
                    }
                    performFriedmanTest(friedmanData, pValueCars, fValueCars);
                    usedStatTest.setText("Friedman test");
                    testStatistics.setText(String.valueOf(fValueCars));
                    pValue.setText(String.valueOf(pValueCars));

                    // Start post-HOC analysis
                    if(pValueCars < 0.05){
                        for(int i = 1; i < friedmanData.size(); i++){
                            double pValueHOC = 0.0, fValueHOC = 0.0;
                            double[] group1 = friedmanData.get(i - 1);
                            double[] group2 = friedmanData.get(i);
                            postHOC(group1, group2, false, pValueHOC, fValueHOC);
                        }
                    }
                }

                if(anovaCheckBikes){
                    double pValueBikes = 0.0;
                    double fValueBikes = 0.0;
                    List<double[]> anovaData = new ArrayList<>();
                    for(GeoPointData data : geoPointData){
                        double[] convertedData = convertListToDoubleArray(data.bikes);
                        anovaData.add(convertedData);
                    }
                    performAnova(anovaData, pValueBikes, fValueBikes);
                    usedStatTest.setText("ANOVA test");
                    testStatistics.setText(String.valueOf(fValueBikes));
                    pValue.setText(String.valueOf(pValueBikes));

                    // Start post-HOC analysis
                    if(pValueBikes < 0.05){
                        for(int i = 1; i < anovaData.size(); i++){
                            double pValueHOC = 0.0, fValueHOC = 0.0;
                            double[] group1 = anovaData.get(i - 1);
                            double[] group2 = anovaData.get(i);
                            postHOC(group1, group2, true, pValueHOC, fValueHOC);
                        }
                    }
                } else {
                    double pValueBikes = 0.0;
                    double fValueBikes = 0.0;
                    List<double[]> friedmanData = new ArrayList<>();
                    for(GeoPointData data : geoPointData){
                        double[] convertedData = convertListToDoubleArray(data.bikes);
                        friedmanData.add(convertedData);
                    }
                    performFriedmanTest(friedmanData, pValueBikes, fValueBikes);
                    usedStatTest.setText("Friedman test");
                    testStatistics.setText(String.valueOf(fValueBikes));
                    pValue.setText(String.valueOf(pValueBikes));

                    // Start post-HOC analysis
                    if(pValueBikes < 0.05){
                        for(int i = 1; i < friedmanData.size(); i++){
                            double pValueHOC = 0.0, fValueHOC = 0.0;
                            double[] group1 = friedmanData.get(i - 1);
                            double[] group2 = friedmanData.get(i);
                            postHOC(group1, group2, false, pValueHOC, fValueHOC);
                        }
                    }
                }

                if(anovaCheckPeople){
                    double pValuePeople= 0.0;
                    double fValuePeople = 0.0;
                    List<double[]> anovaData = new ArrayList<>();
                    for(GeoPointData data : geoPointData){
                        double[] convertedData = convertListToDoubleArray(data.people);
                        anovaData.add(convertedData);
                    }
                    performAnova(anovaData, pValuePeople, fValuePeople);
                    usedStatTest.setText("ANOVA test");
                    testStatistics.setText(String.valueOf(fValuePeople));
                    pValue.setText(String.valueOf(pValuePeople));

                    // Start post-HOC analysis
                    if(pValuePeople < 0.05){
                        for(int i = 1; i < anovaData.size(); i++){
                            double pValueHOC = 0.0, fValueHOC = 0.0;
                            double[] group1 = anovaData.get(i - 1);
                            double[] group2 = anovaData.get(i);
                            postHOC(group1, group2, true, pValueHOC, fValueHOC);
                        }
                    }
                } else {
                    double pValuePeople = 0.0;
                    double fValuePeople = 0.0;
                    List<double[]> friedmanData = new ArrayList<>();
                    for(GeoPointData data : geoPointData){
                        double[] convertedData = convertListToDoubleArray(data.people);
                        friedmanData.add(convertedData);
                    }
                    performFriedmanTest(friedmanData, pValuePeople, fValuePeople);
                    usedStatTest.setText("Friedman test");
                    testStatistics.setText(String.valueOf(fValuePeople));
                    pValue.setText(String.valueOf(pValuePeople));

                    // Start post-HOC analysis
                    if(pValuePeople < 0.05){
                        for(int i = 1; i < friedmanData.size(); i++){
                            double pValueHOC = 0.0, fValueHOC = 0.0;
                            double[] group1 = friedmanData.get(i - 1);
                            double[] group2 = friedmanData.get(i);
                            postHOC(group1, group2, false, pValueHOC, fValueHOC);
                        }
                    }
                }
            }
        }
    }

    // Function that checks which color bar in the barchart should be
    private int getColorForValue(float value) {
        float threshold_lower = 6.0f;
        float threshold_upper = 9.0f;

        if (value < threshold_lower) {
            return Color.parseColor("#FFA500"); // Orange
        } else if (value >= threshold_lower && value <= threshold_upper) {
            return Color.parseColor("#FAC710"); // Yellow
        } else {
            return Color.parseColor("#88ab4b"); // Green
        }
    }

    public void updateGeoPoints(ArrayList<GeoPoint> geoPoints) {
        this.selectedGeoPoints = geoPoints;
        Log.d("GETGEOPOINT", "Geopoints received");
        if(geoPoints.size() > 0) {
            typeOfStats.setImageResource(R.drawable.compare_icon);
            ArrayList<GeoPoint> barData = new ArrayList<>();
            barData.add(geoPoints.get(0));
            getGeoPointData(barData);
        }
    }

    public void setupBarChartWithErrorBars(CustomBarChart chart, ArrayList<Float> means, ArrayList<Float> deviations, ArrayList<String> labels) {
        if (means.size() != deviations.size() || means.size() != labels.size()) {
            throw new IllegalArgumentException("The size of means, deviations, and labels must be the same.");
        }

        // Create a list of entries for the chart
        List<BarEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        for (int i = 0; i < means.size(); i++) {
            float mean = means.get(i);
            float deviation = deviations.get(i);
            entries.add(new BarEntry(i, mean)); // Mean values
            colors.add(getColorForValue(mean)); // Color based on value
        }

        // Create BarDataSet for means
        BarDataSet meanDataSet = new BarDataSet(entries, "Means");
        meanDataSet.setColors(colors);
        meanDataSet.setDrawValues(true);

        // Create BarData object and add the dataset to it
        BarData barData = new BarData(meanDataSet);
        chart.setData(barData);

        // Set up the X-axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setDrawGridLines(false);

        // Customize the Y-axis
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        chart.setFitBars(true); // Make the bars fit into the x-axis
        chart.invalidate(); // Refresh the chart

        chart.updateChartData(barData, deviations);
    }

    private void getGeoPointData(ArrayList<GeoPoint> geoPoints){
        for(GeoPoint geoPoint : geoPoints){
            Double latitude = geoPoint.getLatitude();
            Double longitude = geoPoint.getLongitude();

            // Round to 2 decimals
            latitude = Math.round(latitude*100.0)/100.0;
            longitude = Math.round(longitude*100.0)/100.0;

            String latitude_str = String.valueOf(latitude);
            String longitude_str = String.valueOf(longitude);

            latitude_str = latitude_str.replace(".", "_");
            longitude_str = longitude_str.replace(".", "_");

            String path = latitude_str + "-" + longitude_str;

            SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            Date now = new Date();

            String time = timeFormat.format(now);
            String dayOfWeek = dayOfWeekFormat.format(now);

            String[] times = time.split(":");
            String time_path = new String();
            if(Integer.parseInt(times[1]) < 30){
                time_path = times[0] + "_00-" + times[0] +"_30";
            } else {
                int later_hour = Integer.parseInt(times[0]) + 1;
                time_path = times[0] + "_30-" + later_hour + "_00";
            }

            path = path + "/" + dayOfWeek + "/" + time_path;

            Log.d("GET DATA", path);

            ArrayList<Float> mean = new ArrayList<>();
            ArrayList<Float> deviations = new ArrayList<>();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(path);

            // Attach a listener to read the data at our path
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again whenever data at this location is updated.
                    if (dataSnapshot.exists()) {
                        // Retrieve and parse the data
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            String key = childSnapshot.getKey();
                            Object value = childSnapshot.getValue();

                            if (value instanceof Number) {
                                float numericValue = ((Number) value).floatValue();

                                if (key != null && key.startsWith("mean_")) {
                                    mean.add(numericValue);
                                } else if (key != null && key.startsWith("std_")) {
                                    deviations.add(numericValue);
                                }

                                Log.d("GET DATA", key + ": " + numericValue);
                            } else {
                                Log.d("GET DATA", key + ": " + value);
                            }
                        }
                    } else {
                        Log.d("GET DATA", "No data found at this path.");
                    }
                    checkCompletion(new int[]{geoPoints.size()}, mean, deviations);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting data failed, log a message
                    Log.d("GET DATA", "Failed to read value: " + databaseError.getMessage());
                }
            });

            if(mean.size() > 0 && deviations.size() > 0) {
                setupBarChartWithErrorBars(barChart, mean, deviations, labels);
                foundData = true;
            } else {
                warningText.setText("Nema podataka o ovoj lokaciji u traženom vremenskom intervalu.");
                warningText.setVisibility(View.VISIBLE);
                statTestLayout.setVisibility(View.GONE);
                barChart.setVisibility(View.GONE);
                foundData = false;
                //Toast.makeText(getContext(), "Nema podataka o ovoj lokaciji u traženom vremenskom intervalu.", Toast.LENGTH_SHORT);
            }
        }
    }

    // Method to check if all requests are completed
    private void checkCompletion(int[] pendingRequests, ArrayList<Float> means, ArrayList<Float> deviations) {
        pendingRequests[0]--;
        if (pendingRequests[0] == 0) {
            // All requests are done, now handle the results
            if (means.size() > 0 && deviations.size() > 0) {
                foundData = true;
                // Assume `labels` is defined elsewhere in your code
                setupBarChartWithErrorBars(barChart, means, deviations, labels);
            } else {
                //Toast.makeText(getContext(), "Nema podataka o ovoj lokaciji u traženom vremenskom intervalu.", Toast.LENGTH_SHORT).show();
                warningText.setVisibility(View.VISIBLE);
                warningText.setText("Nema podataka za ovu lokaciju u traženom vremenskom intervalu.");
                statTestLayout.setVisibility(View.GONE);
                barChart.setVisibility(View.GONE);
                foundData = false;
            }
        }
    }

    private void getTestData(GeoPoint geoPoint, List<GeoPointData> geoPointData){
        Double latitude = geoPoint.getLatitude();
        Double longitude = geoPoint.getLongitude();

        // Round to 3 decimals
        latitude = Math.round(latitude * 1000.0) / 1000.0;
        longitude = Math.round(longitude * 1000.0) / 1000.0;

        String latitude_str = String.valueOf(latitude);
        String longitude_str = String.valueOf(longitude);

        String path = "Overall_detections_days/";

        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        Date now = new Date();

        String time = timeFormat.format(now);
        String dayOfWeek = dayOfWeekFormat.format(now);

        path = path + dayOfWeek + "/" + latitude_str + "_" + longitude_str + "/";

        String[] times = time.split(":");
        String time_path = new String();
        if(Integer.parseInt(times[1]) < 30){
            time_path = times[0] + ":00-" + times[0] +":30";
        } else {
            int later_hour = Integer.parseInt(times[0]) + 1;
            time_path = times[0] + ":30-" + later_hour + ":00";
        }

        String filename = time_path + ".csv";
        Log.d("GET DATA", path);
        Log.d("GET DATA", filename);

        // Reference to the Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(path).child(filename);

        // Check if the file exists by getting its metadata
        storageRef.getMetadata().addOnSuccessListener(metadata -> {
            // The file exists, proceed to download it
            try {
                File localFile = File.createTempFile("tempCSV", ".csv");

                // Download the file
                storageRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                    // File downloaded successfully
                    parseCSV(localFile, geoPointData, geoPoint);
                    foundData = true;
                }).addOnFailureListener(exception -> {
                    // Handle download errors
                    exception.printStackTrace();
                    warningText.setVisibility(View.VISIBLE);
                    warningText.setText("Nema podataka za ovu lokaciju u traženom vremenskom intervalu.");
                    statTestLayout.setVisibility(View.GONE);
                    barChart.setVisibility(View.GONE);
                    foundData = false;
                    geoPointData.add(null);
                    //Toast.makeText(getContext(), "Nema podataka za ovu lokaciju u traženom vremenskom intervalu.", Toast.LENGTH_SHORT).show();
                });

            } catch (IOException e) {
                e.printStackTrace();
                warningText.setVisibility(View.VISIBLE);
                warningText.setText("Nema podataka za ovu lokaciju u traženom vremenskom intervalu.");
                statTestLayout.setVisibility(View.GONE);
                barChart.setVisibility(View.GONE);
                foundData = false;
                //Toast.makeText(getContext(), "Nema podataka za ovu lokaciju u traženom vremenskom intervalu.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(exception -> {
            // The file does not exist
            warningText.setVisibility(View.VISIBLE);
            warningText.setText("Nema podataka za ovu lokaciju u traženom vremenskom intervalu.");
            statTestLayout.setVisibility(View.GONE);
            barChart.setVisibility(View.GONE);
            foundData = false;
            //Toast.makeText(getContext(), "Nema podataka za ovu lokaciju u traženom vremenskom intervalu.", Toast.LENGTH_SHORT).show();
        });
    }

    private void parseCSV(File file, List<GeoPointData> geoPointData, GeoPoint geoPoint) {
        // Lists to hold each column's data
        List<Integer> cars = new ArrayList<>();
        List<Integer> buses = new ArrayList<>();
        List<Integer> trucks = new ArrayList<>();
        List<Integer> trains = new ArrayList<>();
        List<Integer> people = new ArrayList<>();
        List<Integer> bikes = new ArrayList<>();
        List<Integer> motors = new ArrayList<>();
        // Add more lists if the CSV has more columns

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Split the line into columns
                String[] values = line.split(",");

                // Assuming the file has three columns, parse each one into an integer
                cars.add(Integer.parseInt(values[0].trim()));
                buses.add(Integer.parseInt(values[1].trim()));
                trucks.add(Integer.parseInt(values[2].trim()));
                trains.add(Integer.parseInt(values[3].trim()));
                people.add(Integer.parseInt(values[4].trim()));
                bikes.add(Integer.parseInt(values[5].trim()));
                motors.add(Integer.parseInt(values[6].trim()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        GeoPointData newGeoPointData = new GeoPointData();
        newGeoPointData.cars = cars;
        newGeoPointData.buses = buses;
        newGeoPointData.bikes = bikes;
        newGeoPointData.people = people;
        newGeoPointData.trains = trains;
        newGeoPointData.trucks = trucks;
        newGeoPointData.motorbikes = motors;

        getNormality(geoPoint, newGeoPointData);

        geoPointData.add(newGeoPointData);

        // Now you have data separated by columns
        Log.d("GET DATA","Column 1: " + cars);
        Log.d("GET DATA","Column 2: " + buses);
        Log.d("GET DATA","Column 5: " + people);
    }

    private void getNormality(GeoPoint geoPoint, GeoPointData geoPointData) {
        Double latitude = geoPoint.getLatitude();
        Double longitude = geoPoint.getLongitude();

        // Round to 3 decimals
        latitude = Math.round(latitude * 1000.0) / 1000.0;
        longitude = Math.round(longitude * 1000.0) / 1000.0;

        String latitude_str = String.valueOf(latitude);
        String longitude_str = String.valueOf(longitude);

        latitude_str = latitude_str.replace(".", "_");
        longitude_str = longitude_str.replace(".", "_");

        String path = latitude_str + "-" + longitude_str;

        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        Date now = new Date();

        String time = timeFormat.format(now);
        String dayOfWeek = dayOfWeekFormat.format(now);

        String[] times = time.split(":");
        String time_path = new String();
        if(Integer.parseInt(times[1]) < 30){
            time_path = times[0] + "_00-" + times[0] +"_30";
        } else {
            int later_hour = Integer.parseInt(times[0]) + 1;
            time_path = times[0] + "_30-" + later_hour + "_00";
        }

        path = path + "/" + dayOfWeek + "/" + time_path;

        Log.d("GET DATA", path);

        ArrayList<Float> mean = new ArrayList<>();
        ArrayList<Float> deviations = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(path);

        // Attach a listener to read the data at our path
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again whenever data at this location is updated.
                if (dataSnapshot.exists()) {
                    // Retrieve and parse the data
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String key = childSnapshot.getKey();
                        Object value = childSnapshot.getValue();

                        if (value instanceof Number) {
                            float numericValue = ((Number) value).floatValue();


                            if(key == "bike_shapiro"){
                                geoPointData.bike_normality = numericValue;
                            } else if(key == "bus_shapiro"){
                                geoPointData.bus_normality = numericValue;
                            } else if(key == "cars_shapiro"){
                                geoPointData.car_normality = numericValue;
                            } else if(key == "ppl_shapiro"){
                                geoPointData.people_normality = numericValue;
                            } else if(key == "motor_shapiro"){
                                geoPointData.motor_normality = numericValue;
                            } else if(key == "train_shapiro"){
                                geoPointData.train_normality = numericValue;
                            } else if(key == "truck_shapiro"){
                                geoPointData.truck_normality = numericValue;
                            }
                        } else {
                            Log.d("GET DATA", key + ": " + value);
                        }
                    }
                } else {
                    Log.d("GET DATA", "No data found at this path.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting data failed, log a message
                Log.d("GET DATA", "Failed to read value: " + databaseError.getMessage());
            }
        });
    }

    public static void performAnova(List<double[]> groups, double pValue, double fValue) {
        OneWayAnova anova = new OneWayAnova();
        pValue = anova.anovaPValue(groups);
        fValue = anova.anovaFValue(groups);
        Log.d("ANOVASTATS","P-value: " + pValue);
    }

    public static void performPairedTTest(double[] group1, double[] group2, double pValue, double fValue) {
        TTest tTest = new TTest();
        pValue = tTest.pairedTTest(group1, group2);
        fValue = tTest.tTest(group1, group2);
        Log.d("TTEST", "Paired T-Test P-value: " + pValue);
    }

    public static void performWilcoxonSignedRankTest(double[] group1, double[] group2, double pValue, double fValue) {
        // Wilcoxon Signed-Rank Test implementation (manual or using another library)
        pValue = wilcoxonSignedRankTest(group1, group2, fValue);
        Log.d("WILCOXON", "Wilcoxon Signed-Rank Test P-value: " + pValue);
    }

    // Manual implementation or using another library
    public static double wilcoxonSignedRankTest(double[] group1, double[] group2, double fValue) {
        // Calculate differences and ranks
        int n = group1.length;
        double[] differences = new double[n];
        double sumOfRanks = 0.0;

        for (int i = 0; i < n; i++) {
            differences[i] = group2[i] - group1[i];
        }

        // Calculate the sum of ranks for positive differences
        for (int i = 0; i < n; i++) {
            double absDiff = Math.abs(differences[i]);
            double rank = i + 1; // Simplified ranking, you may need a more sophisticated ranking
            if (differences[i] > 0) {
                sumOfRanks += rank;
            }
        }

        // Calculate the Wilcoxon statistic
        fValue = sumOfRanks;

        // For now, let's just return a placeholder p-value
        // You should replace this with an actual p-value calculation
        return fValue / (n * (n + 1) / 2);
    }

    public static double performLeveneTest(List<double[]> groups) {
        int k = groups.size();  // Number of groups
        int N = 0;  // Total number of observations
        double grandMean = 0.0;
        double sumSquaredZ = 0.0;
        double sumSquaredGroupMeans = 0.0;
        double[] Z = new double[k];
        Mean mean = new Mean();

        for (double[] group : groups) {
            N += group.length;
        }

        for (int i = 0; i < k; i++) {
            double groupMean = mean.evaluate(groups.get(i));
            for (int j = 0; j < groups.get(i).length; j++) {
                Z[i] += Math.abs(groups.get(i)[j] - groupMean);
            }
            Z[i] /= groups.get(i).length;  // Mean of absolute deviations for group i
            sumSquaredZ += Z[i] * Z[i];
            sumSquaredGroupMeans += Z[i] * Z[i] * groups.get(i).length;
        }

        grandMean = mean.evaluate(Z);

        double numerator = (N - k) * sumSquaredGroupMeans - sumSquaredZ;
        numerator /= (k - 1);

        double denominator = sumSquaredZ - (N * grandMean * grandMean);
        denominator /= (N - k);

        double FValue = numerator / denominator;

        FDistribution fDist = new FDistribution(k - 1, N - k);
        return 1.0 - fDist.cumulativeProbability(FValue);
    }

    private List<double[]> convertListToMultipleDoubleArrays(List<Integer> integerList, int chunkSize) {
        List<double[]> result = new ArrayList<>();

        int n = integerList.size();
        for (int i = 0; i < n; i += chunkSize) {
            int end = Math.min(i + chunkSize, n);  // Calculate the end index for the chunk
            double[] doubleArray = new double[end - i];

            for (int j = i; j < end; j++) {
                doubleArray[j - i] = integerList.get(j);  // Convert Integer to double
            }

            result.add(doubleArray);
        }

        return result;
    }

    public static double performMauchlyTest(double[][] data) {
        int n = data.length;  // Number of samples
        int p = data[0].length;  // Number of repeated measures

        // Calculate covariance matrix
        double[][] covMatrix = new double[p][p];
        double[] means = new double[p];
        Mean meanCalculator = new Mean();

        // Calculate means
        for (int j = 0; j < p; j++) {
            double[] column = new double[n];
            for (int i = 0; i < n; i++) {
                column[i] = data[i][j];
            }
            means[j] = meanCalculator.evaluate(column);
        }

        // Calculate covariance matrix
        for (int i = 0; i < p; i++) {
            for (int j = 0; j < p; j++) {
                double cov = 0.0;
                for (int k = 0; k < n; k++) {
                    cov += (data[k][i] - means[i]) * (data[k][j] - means[j]);
                }
                covMatrix[i][j] = cov / (n - 1);
            }
        }

        // Calculate determinant and trace
        double determinant = covMatrix[0][0] * covMatrix[1][1] - covMatrix[0][1] * covMatrix[1][0];
        double trace = covMatrix[0][0] + covMatrix[1][1];

        // Calculate Mauchly's W statistic
        double W = Math.pow(determinant / Math.pow(trace / p, p), n / 2.0);

        // Calculate Chi-Square
        double chiSquare = -(n - 1) * (p + 1 - 2 / 3.0 * p) * Math.log(W);

        // Degrees of freedom
        int df = (p * (p - 1)) / 2;

        // Chi-square distribution
        ChiSquaredDistribution chiDist = new ChiSquaredDistribution(df);
        return 1.0 - chiDist.cumulativeProbability(chiSquare);
    }

    // Method to convert a single List<Integer> to double[][] for Mauchly's Test
    private double[][] convertSingleListToDoubleArray(List<Integer> integerList) {
        int p = integerList.size();  // Number of repeated measures (data points)

        double[][] data = new double[1][p];  // Single row with 'p' columns

        for (int j = 0; j < p; j++) {
            data[0][j] = integerList.get(j);  // Convert Integer to double
        }

        return data;
    }

    private double[] convertListToDoubleArray(List<Integer> integerList) {
        double[] doubleArray = new double[integerList.size()];

        for (int i = 0; i < integerList.size(); i++) {
            doubleArray[i] = integerList.get(i);  // Convert Integer to double
        }

        return doubleArray;
    }

    // Method to perform Friedman test
    public static void performFriedmanTest(List<double[]> groups, double pValue, double fValue) {
        int k = groups.size();  // Number of groups (treatments)
        int n = groups.get(0).length;  // Number of subjects (blocks)

        // Convert groups to matrix
        double[][] data = new double[n][k];
        for (int i = 0; i < k; i++) {
            double[] group = groups.get(i);
            for (int j = 0; j < n; j++) {
                data[j][i] = group[j];
            }
        }

        // Rank the data
        double[][] ranks = rankData(data, n, k);

        // Calculate Friedman test statistic
        fValue = calculateFriedmanChiSquared(ranks, n, k);

        // Calculate p-value
        int df = k - 1;
        org.apache.commons.math3.distribution.ChiSquaredDistribution chiSquaredDistribution = new org.apache.commons.math3.distribution.ChiSquaredDistribution(df);
        pValue = 1.0 - chiSquaredDistribution.cumulativeProbability(fValue);
    }

    // Method to rank the data
    private static double[][] rankData(double[][] data, int n, int k) {
        double[][] ranks = new double[n][k];
        for (int i = 0; i < n; i++) {
            double[] row = data[i];
            int[] sortedIndices = getSortedIndices(row);

            // Rank assignment
            double rank = 1;
            for (int j : sortedIndices) {
                ranks[i][j] = rank++;
            }
        }
        return ranks;
    }

    // Get indices of sorted array
    private static int[] getSortedIndices(double[] array) {
        Integer[] indices = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            indices[i] = i;
        }
        java.util.Arrays.sort(indices, (i1, i2) -> Double.compare(array[i1], array[i2]));
        return java.util.Arrays.stream(indices).mapToInt(i -> i).toArray();
    }

    // Calculate Friedman Chi-Squared statistic
    private static double calculateFriedmanChiSquared(double[][] ranks, int n, int k) {
        double[] rowSums = new double[n];
        double[] columnSums = new double[k];
        double totalSum = 0;

        // Sum of ranks for each row
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < k; j++) {
                rowSums[i] += ranks[i][j];
                columnSums[j] += ranks[i][j];
                totalSum += ranks[i][j];
            }
        }

        // Calculate Chi-Squared
        double chiSquared = 0;
        for (int i = 0; i < n; i++) {
            chiSquared += Math.pow(rowSums[i] - k * (k + 1) / 2.0, 2);
        }
        chiSquared *= (12.0 / (n * k * (k + 1)));
        chiSquared -= 3 * n * (k + 1);

        return chiSquared;
    }

    private static void postHOC(double[] group1, double[] group2, boolean normality, double pValue, double fValue){
        if(normality){
            performPairedTTest(group1, group2, pValue, fValue);
        } else {
            performWilcoxonSignedRankTest(group1, group2, pValue, fValue);
        }
    }
}