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

import org.osmdroid.util.GeoPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ChartFragment extends Fragment {

    private ArrayList<GeoPoint> selectedGeoPoints;
    public boolean descriptive_stats = true;
    CustomBarChart barChart;
    ArrayList<String> labels = new ArrayList<>();

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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        // Find elements in the fragment layout
        barChart = view.findViewById(R.id.barChart);
        ImageButton typeOfStats = view.findViewById(R.id.typeOfStats);
        LinearLayout statTest = view.findViewById(R.id.statTest);
        TextView usedStatTest = view.findViewById(R.id.usedStatTest);
        TextView testStatistics = view.findViewById(R.id.testStatistics);
        TextView pValue = view.findViewById(R.id.pValue);
        TextView interpretationMain = view.findViewById(R.id.interpretationMain);

        // Post-HOC elements

        ArrayList<Float> means = new ArrayList<>();
        means.add(4.5f);
        means.add(2.3f);
        means.add(13f);
        means.add(1.5f);
        means.add(5.3f);
        means.add(2.7f);
        means.add(1.8f);

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

        typeOfStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(descriptive_stats){
                    descriptive_stats = false;
                    typeOfStats.setImageResource(R.drawable.graph_icon);
                    if(selectedGeoPoints == null) {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).hideChartFragment();
                        }
                    } else {
                        barChart.setVisibility(View.GONE);
                        statTest.setVisibility(View.VISIBLE);
                        inferentialStatistics(usedStatTest, testStatistics, pValue, interpretationMain);
                    }
                } else {
                    descriptive_stats = true;
                    typeOfStats.setImageResource(R.drawable.compare_icon);
                }
            }
        });

        return view;
    }

    private void inferentialStatistics(TextView usedStatTest, TextView testStatistics, TextView pValue, TextView interpretationMain) {
        Toast.makeText(getContext(), "In inferentialStatistics", Toast.LENGTH_SHORT).show();
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
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting data failed, log a message
                    Log.d("GET DATA", "Failed to read value: " + databaseError.getMessage());
                }
            });

            if(mean.size() > 0 || deviations.size() > 0) {
                setupBarChartWithErrorBars(barChart, mean, deviations, labels);
            } else {
                Toast.makeText(getContext(), "Nema podataka o ovoj lokaciji u traženom vremenskom intervalu.", Toast.LENGTH_SHORT);
            }
        }
    }

    private void getRouteData(List<List<GeoPoint>> allRoutes) {
        for(List<GeoPoint> route : allRoutes){
            for(GeoPoint geoPoint : route){
                Double latitude = geoPoint.getLatitude();
                Double longitude = geoPoint.getLongitude();

                // Round to 2 decimals
                //latitude = Math.round(latitude*100.0)/100.0;
                //longitude = Math.round(longitude*100.0)/100.0;

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

                                // You can parse the values as needed
                                if (value instanceof Long) {
                                    Long numericValue = (Long) value;
                                    Log.d("GET DATA", key + ": " + numericValue);
                                } else if (value instanceof Double) {
                                    Double numericValue = (Double) value;
                                    Log.d("GET DATA", key + ": " + numericValue);
                                } else if (value instanceof String) {
                                    String stringValue = (String) value;
                                    Log.d("GET DATA", key + ": " + stringValue);
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
        }
    }
}
