package com.example.traffic_analysis_app;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ChartFragment extends Fragment {

    private float bike_mean, bike_std, bus_mean, bus_std, car_mean, car_std;
    private float motor_mean, motor_std, people_mean, people_std, train_mean, train_std, truck_mean, truck_std;

    // Add a method to pass data to the fragment
    public static ChartFragment newInstance(float bike_mean, float bike_std, float bus_mean, float bus_std,
                                            float car_mean, float car_std, float motor_mean, float motor_std,
                                            float people_mean, float people_std, float train_mean, float train_std,
                                            float truck_mean, float truck_std) {
        ChartFragment fragment = new ChartFragment();

        Bundle args = new Bundle();
        args.putFloat("bike_mean", bike_mean);
        args.putFloat("bike_std", bike_std);
        args.putFloat("bus_mean", bus_mean);
        args.putFloat("bus_std", bus_std);
        args.putFloat("car_mean", car_mean);
        args.putFloat("car_std", car_std);
        args.putFloat("motor_mean", motor_mean);
        args.putFloat("motor_std", motor_std);
        args.putFloat("people_mean", people_mean);
        args.putFloat("people_std", people_std);
        args.putFloat("train_mean", train_mean);
        args.putFloat("train_std", train_std);
        args.putFloat("truck_mean", truck_mean);
        args.putFloat("truck_std", truck_std);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            bike_mean = getArguments().getFloat("bike_mean");
            bike_std = getArguments().getFloat("bike_std");
            bus_mean = getArguments().getFloat("bus_mean");
            bus_std = getArguments().getFloat("bus_std");
            car_mean = getArguments().getFloat("car_mean");
            car_std = getArguments().getFloat("car_std");
            motor_mean = getArguments().getFloat("motor_mean");
            motor_std = getArguments().getFloat("motor_std");
            people_mean = getArguments().getFloat("people_mean");
            people_std = getArguments().getFloat("people_std");
            train_mean = getArguments().getFloat("train_mean");
            train_std = getArguments().getFloat("train_std");
            truck_mean = getArguments().getFloat("truck_mean");
            truck_std = getArguments().getFloat("truck_std");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        // Find the BarChart in the fragment layout
        CustomBarChart barChart = view.findViewById(R.id.barChart);

        ArrayList<Float> means = new ArrayList<>();
        means.add(4.5f);
        means.add(2.3f);
        means.add(13f);
        means.add(1.5f);
        means.add(5.3f);
        means.add(2.7f);
        means.add(1.8f);

        ArrayList<String> labels = new ArrayList<>();
        labels.add("Bicikl");
        labels.add("Bus");
        labels.add("Auto");
        labels.add("Motor");
        labels.add("Ljudi");
        labels.add("Vlak");
        labels.add("Kamion");

        ArrayList<Float> deviations = new ArrayList<>();
        deviations.add(0.7f);
        deviations.add(1.2f);
        deviations.add(3.5f);
        deviations.add(1.0f);
        deviations.add(2.3f);
        deviations.add(0.6f);
        deviations.add(0.5f);

        // Create the chart
        setupBarChartWithErrorBars(barChart, means, deviations, labels);

        return view;
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
}
