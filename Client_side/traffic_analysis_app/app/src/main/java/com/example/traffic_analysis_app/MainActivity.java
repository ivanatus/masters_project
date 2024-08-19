package com.example.traffic_analysis_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleDataSet;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class MainActivity extends AppCompatActivity implements LocationListener {
    MapView mapView;
    private static final int REQUEST_LOCATION_PERMISSION = 1; // Define your request code here
    private static final long MIN_TIME_BW_UPDATES = 60000; // 1 minute in milliseconds
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    BottomNavigationView bottom_navigation;
    private NominatimService nominatimService;
    private RoutingService routingService;
    EditText address;
    ImageButton search;
    GeoPoint current_location;
    boolean directions = false;
    boolean showToolbarMenu = false;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) { return true; }

    private void removeOverflowMenu(Menu menu) {
        try {
            Field field = MenuBuilder.class.getDeclaredField("mOptionalIconsVisible");
            field.setAccessible(true);
            field.set(menu, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //adding the navigation bar to the layout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);

        if (!showToolbarMenu)
            return false;
        getMenuInflater().inflate(R.menu.bottom_navigation, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //defining the action that is done after navigation bar/toolbar item selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.recording) {
            Intent recording = new Intent(getApplicationContext(), RecordingActivity.class);
            startActivity(recording);
            finish();
        } else if(id == R.id.info) {
            //userInstructions(this);
            Intent data = new Intent(getApplicationContext(), InfoActivity.class);
            startActivity(data);
            finish();
        } else if(id == R.id.settings){
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            finish();
        } else if(id == R.id.home){
            Intent home = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(home);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottom_navigation = findViewById(R.id.bottom_navigation);
        // Clear any selected item
        bottom_navigation.setOnNavigationItemSelectedListener(null);
        bottom_navigation.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottom_navigation.getMenu().size(); i++) {
            bottom_navigation.getMenu().getItem(i).setChecked(false);
        }
        bottom_navigation.getMenu().setGroupCheckable(0, true, true);
        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if(id == R.id.recording){
                    Intent recording = new Intent(getApplicationContext(), RecordingActivity.class);
                    recording.putExtra("current_location_latitude", current_location.getLatitude());
                    recording.putExtra("current_location_longitude", current_location.getLongitude());
                    startActivity(recording);
                    finish();
                } else if(id == R.id.info){
                    Log.d("ANOVASTATS", "ID recognized");
                    Intent info = new Intent(getApplicationContext(), InfoActivity.class);
                    startActivity(info);
                    finish();
                }
                return false;
            }
        });

        // Initialize the map
        Configuration.getInstance().setUserAgentValue(getPackageName());
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        // Initialize the Nominatim service
        nominatimService = new NominatimService();
        address = findViewById(R.id.address_search);
        search = findViewById(R.id.search_address);

        if (mapView != null) {
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(true);
        } else {
            Log.e("MainActivity", "MapView is null");
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://router.project-osrm.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        routingService = retrofit.create(RoutingService.class);

        // 1. Check and request permissions at runtime (for Android 6.0+)
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, android.Manifest.permission.REQUEST_LOCATION_PERMISSION);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            // Permissions already granted, proceed with location access
            startLocationUpdates();
        }

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!address.getText().equals("")){ getCoordinatesForAddress(String.valueOf(address.getText())); }
            }
        });

        showMarkersFromSharedPreferences();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        ChartFragment chartFragment = ChartFragment.newInstance(4.5f, 0.7f, 2.3f, 0.2f,13f, 3.6f, 1.5f, 0.9f,5.3f, 2.7f, 0.8f, 1f,2.3f, 1.8f);

        fragmentTransaction.replace(R.id.fragmentContainer, chartFragment);
        fragmentTransaction.commit();

        //graphPlotting(this, 4.5f, 0.7f, 2.3f, 0.2f,13f, 3.6f, 1.5f, 0.9f,5.3f, 2.7f, 0.8f, 1f,2.3f, 1.8f);
    }

    private void startLocationUpdates() {
        Log.d("LocationTAG", "startLocationUpdates");
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) MainActivity.this);
        }
    }


    // Implement onRequestPermissionsResult to handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                startLocationUpdates();
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
            }
        }
    }

    // 3. Implement location listener to receive updates
    @Override
    public void onLocationChanged(Location location) {
        // Handle location updates
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // Center the map to the new location
        GeoPoint currentLocation = new GeoPoint(latitude, longitude);
        current_location = currentLocation;
        mapView.getController().setCenter(currentLocation);
        Marker marker = new Marker(mapView);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        Drawable currentLocationMarker = ContextCompat.getDrawable(this, R.drawable.current_location);
        marker.setIcon(currentLocationMarker);
        marker.setPosition(new GeoPoint(latitude, longitude));
        mapView.getOverlays().add(marker);

        // Refresh the map to display the marker
        mapView.invalidate();

        // Zoom to the new location
        mapView.getController().setZoom(18); // Adjust the zoom level as needed
        mapView.getController().setCenter(currentLocation);

        if(directions){
            Log.d("Directions", "Directions true");
            if (location.hasBearing()) {
                Log.d("Directions", "location.hasBearing");
                mapView.setMapOrientation(-location.getBearing());
            }
        }
    }

    public void getCoordinatesForAddress(String address) {
        nominatimService.getCoordinates(address, new NominatimService.GeocodingCallback() {
            @Override
            public void onSuccess(NominatimResult result) {
                // Update the map with the obtained coordinates
                GeoPoint geoPoint = new GeoPoint(result.lat, result.lon);
                mapView.getController().setCenter(geoPoint);
                Marker marker = new Marker(mapView);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setPosition(geoPoint);
                mapView.getController().setZoom(18);
                Drawable destinationLocationMarker = ContextCompat.getDrawable(MainActivity.this, R.drawable.destination_location);
                marker.setIcon(destinationLocationMarker);
                mapView.getOverlays().add(marker);

                marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView) {
                        showPopupMenu(marker, geoPoint);
                        return false;
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                // Handle the error
                Toast.makeText(MainActivity.this, "Failed to get coordinates: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPopupMenu(final Marker marker, GeoPoint markerLocation) {
        // Inflate the popup menu layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.marker_popup_layout, null);

        // Create the PopupWindow
        final PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);

        // Set up the menu items' click listeners
        TextView directionsOption = popupView.findViewById(R.id.directions);
        TextView saveLocationOption = popupView.findViewById(R.id.save_location);

        directionsOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDirections(current_location, markerLocation);
                popupWindow.dismiss();
            }
        });

        saveLocationOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Save location clicked", Toast.LENGTH_SHORT).show();
                showSaveLocationDialog(marker.getPosition());
                popupWindow.dismiss();
            }
        });

        // Show the popup window next to the marker
        // Calculate the offset in pixels
        int xOffset = dpToPx(150);
        int yOffset = dpToPx(100);
        popupWindow.showAsDropDown(mapView, (int) marker.getPosition().getLongitude(), (int) marker.getPosition().getLatitude(), Gravity.NO_GRAVITY);

        // Adjust position of the PopupWindow
        popupWindow.showAtLocation(mapView, Gravity.NO_GRAVITY, xOffset, yOffset);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public void getDirections(GeoPoint start, GeoPoint end) {
        if (start == null) {
            Toast.makeText(this, "Invalid start location", Toast.LENGTH_SHORT).show();
            return;
        }
        if (end == null) {
            Toast.makeText(this, "Invalid end location", Toast.LENGTH_SHORT).show();
            return;
        }
        directions = true;

        String startCoords = start.getLongitude() + "," + start.getLatitude();
        String endCoords = end.getLongitude() + "," + end.getLatitude();

        Call<RoutingResponse> call = routingService.getRoute(
                startCoords,
                endCoords,
                "full",      // Example value for overview, adjust as needed
                "geojson",   // Example value for geometries, adjust as needed
                true         // Request alternatives to get multiple routes
        );

        call.enqueue(new Callback<RoutingResponse>() {
            @Override
            public void onResponse(Call<RoutingResponse> call, Response<RoutingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<List<GeoPoint>> allRoutes = new ArrayList<>();

                    // Assuming the response includes multiple routes
                    for (RoutingResponse.Route route : response.body().routes) {
                        List<GeoPoint> routePoints = new ArrayList<>();
                        for (List<Double> point : route.geometry.coordinates) {
                            routePoints.add(new GeoPoint(point.get(1), point.get(0)));
                        }
                        allRoutes.add(routePoints);
                    }

                    drawRoute(allRoutes);
                } else {
                    Log.d("GPS_ROUTES", "Failed to get routes");
                    Toast.makeText(MainActivity.this, "Failed to get routes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RoutingResponse> call, Throwable t) {
                Log.d("GPS_ROUTES", t.getMessage());
                Toast.makeText(MainActivity.this, "Failed to get routes", Toast.LENGTH_SHORT).show();
            }
        });



        /*Call<RoutingResponse> call = routingService.getRoute(startCoords, endCoords, "full", "geojson");
        call.enqueue(new Callback<RoutingResponse>() {
            @Override
            public void onResponse(Call<RoutingResponse> call, Response<RoutingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GeoPoint> routePoints = new ArrayList<>();
                    for (List<Double> point : response.body().routes.get(0).geometry.coordinates) {
                        routePoints.add(new GeoPoint(point.get(1), point.get(0)));
                    }
                    drawRoute(routePoints);
                } else {
                    Toast.makeText(MainActivity.this, "Failed to get route", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RoutingResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to get route", Toast.LENGTH_SHORT).show();
            }
        });*/
        /*Call<RoutingResponse> call = routingService.getRoute(startCoords, endCoords, "full", "geojson");
        call.enqueue(new Callback<RoutingResponse>() {
            @Override
            public void onResponse(Call<RoutingResponse> call, Response<RoutingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GeoPoint> routePoints = new ArrayList<>();
                    List<String> instructions = new ArrayList<>();

                    for (int i = 0; i < response.body().routes.get(0).geometry.coordinates.size(); i++) {
                        List<Double> point = response.body().routes.get(0).geometry.coordinates.get(i);
                        routePoints.add(new GeoPoint(point.get(1), point.get(0)));
                    }

                    drawRoute(routePoints);
                } else {
                    Toast.makeText(MainActivity.this, "Failed to get route", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RoutingResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to get route", Toast.LENGTH_SHORT).show();
            }
        });*/

        // If the interface expects alternatives as a String
        /*Call<RoutingResponse> call = routingService.getRoute(
                startCoords,
                endCoords,
                "full",      // Example value for overview, adjust as needed
                "geojson"    // Example value for geometries, adjust as needed
        );

        call.enqueue(new Callback<RoutingResponse>() {
            @Override
            public void onResponse(Call<RoutingResponse> call, Response<RoutingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<List<GeoPoint>> allRoutes = new ArrayList<>();

                    // Assuming the response includes multiple routes
                    for (RoutingResponse.Route route : response.body().routes) {
                        List<GeoPoint> routePoints = new ArrayList<>();
                        for (List<Double> point : route.geometry.coordinates) {
                            routePoints.add(new GeoPoint(point.get(1), point.get(0)));
                        }
                        allRoutes.add(routePoints);
                    }

                    drawRoute(allRoutes);
                } else {
                    Log.d("GPS_ROUTES", "IN here");
                    Toast.makeText(MainActivity.this, "Failed to get routes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RoutingResponse> call, Throwable t) {
                Log.d("GPS_ROUTES", t.getMessage());
                Toast.makeText(MainActivity.this, "Failed to get routes", Toast.LENGTH_SHORT).show();
                Log.d("GPS_ROUTES", t.getMessage());
            }
        });*/
    }

    private void drawRoute(List<List<GeoPoint>> allRoutes) {//List<GeoPoint> routePoints) {
        /*
        Polyline routeLine = new Polyline();
        routeLine.setPoints(routePoints);
        mapView.getOverlays().add(routeLine);
        mapView.invalidate();
        mapView.getController().setZoom(15);*/
        // Clear previous routes
        mapView.getOverlays().clear();
        mapView.invalidate();

        for (List<GeoPoint> routePoints : allRoutes) {
            Polyline routeLine = new Polyline();
            routeLine.setPoints(routePoints);
            routeLine.setColor(Color.BLUE); // Set a color for the route
            routeLine.setWidth(5); // Set the width of the route line
            mapView.getOverlays().add(routeLine);
        }
        mapView.invalidate();
        mapView.getController().setZoom(15);
    }

    private void showSaveLocationDialog(final GeoPoint geoPoint) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save Location");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String label = input.getText().toString();
                saveGeoPointToSharedPreferences(label, geoPoint);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void saveGeoPointToSharedPreferences(String label, GeoPoint geoPoint) {
        SharedPreferences sharedPreferences = getSharedPreferences("savedLocations", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Convert GeoPoint to a String
        String geoPointString = geoPoint.getLatitude() + "," + geoPoint.getLongitude();

        // Save the GeoPoint with the label as the key
        editor.putString(label, geoPointString);
        editor.apply();

        Toast.makeText(this, "Location saved with label: " + label, Toast.LENGTH_SHORT).show();
    }

    private void showMarkersFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("savedLocations", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll(); // Retrieve all entries
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String label = entry.getKey();
            String geoPointString = (String) entry.getValue();

            if (geoPointString != null) {
                String[] parts = geoPointString.split(",");
                double latitude = Double.parseDouble(parts[0]);
                double longitude = Double.parseDouble(parts[1]);
                GeoPoint geoPoint = new GeoPoint(latitude, longitude);

                // Create and configure the marker based on the label
                Marker marker = new Marker(mapView);
                marker.setPosition(geoPoint);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                Drawable markerIcon = getMarkerIconForLabel(label);
                marker.setIcon(markerIcon);

                mapView.getOverlays().add(marker);
            }
        }

        // Refresh the map to display the markers
        mapView.invalidate();
    }

    private Drawable getMarkerIconForLabel(String label) {
        int markerResource;
        switch (label) {
            case "work":
                markerResource = R.drawable.work_place; // Your workplace marker drawable
                break;
            case "home":
                markerResource = R.drawable.home_location; // Your home marker drawable
                break;
            default:
                markerResource = R.drawable.destination_location; // Default marker drawable
                break;
        }
        return ContextCompat.getDrawable(this, markerResource);
    }

    /*public static void graphPlotting(Context context, float bike_mean, float bike_std, float bus_mean, float bus_std,
                                     float car_mean, float car_std, float motor_mean, float motor_std,
                                     float people_mean, float people_std, float train_mean, float train_std,
                                     float truck_mean, float truck_std) {

        // Create a list of means and standard deviations
        float[] means = {bike_mean, bus_mean, car_mean, motor_mean, people_mean, train_mean, truck_mean};
        float[] stdDevs = {bike_std, bus_std, car_std, motor_std, people_std, train_std, truck_std};
        String[] labels = {"Bike", "Bus", "Car", "Motor", "People", "Train", "Truck"};

        // Initialize bar entries and error bars
        ArrayList<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < means.length; i++) {
            entries.add(new BarEntry(i, means[i], stdDevs[i])); // x index, mean, std deviation
        }

        BarDataSet barDataSet = new BarDataSet(entries, "Transport Means");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setDrawValues(true);

        BarData barData = new BarData(barDataSet);

        // Create a BarChart
        BarChart barChart = new BarChart(context);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);

        // Customize the x-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setValueFormatter((value, axis) -> labels[(int) value]);
        //xAxis.setValueFormatter((value, axis) -> labels[(int) Math.round(value)]);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        // Customize the y-axis
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        barChart.setFitBars(true); // make the x-axis fit exactly all bars
        barChart.invalidate(); // refresh

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_chart, null);

        // Add the chart to the custom layout
        ((LinearLayout) dialogView.findViewById(R.id.chartContainer)).addView(barChart);

        // Create and show the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        // Set the click listener for the OK button
        Button okButton = dialogView.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }*/

    public static void graphPlotting(Context context, float bike_mean, float bike_std, float bus_mean, float bus_std,
                                     float car_mean, float car_std, float motor_mean, float motor_std,
                                     float people_mean, float people_std, float train_mean, float train_std,
                                     float truck_mean, float truck_std) {

        // Create a list of means and standard deviations
        float[] means = {bike_mean, bus_mean, car_mean, motor_mean, people_mean, train_mean, truck_mean};
        float[] stdDevs = {bike_std, bus_std, car_std, motor_std, people_std, train_std, truck_std};
        String[] labels = {"Bike", "Bus", "Car", "Motor", "People", "Train", "Truck"};

        // Initialize bar entries and error bars
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        for (int i = 0; i < means.length; i++) {
            entries.add(new BarEntry(i, means[i], stdDevs[i])); // x index, mean, std deviation
            colors.add(getColorForValue(means[i])); // Use a method to determine the color based on value
        }

        // Create a BarDataSet from the entries
        BarDataSet barDataSet = new BarDataSet(entries, "Transport Means");
        barDataSet.setColors(colors); // Set colors for each bar
        barDataSet.setDrawValues(true); // Show values on top of bars

        BarData barData = new BarData(barDataSet);

        // Create a BarChart
        BarChart barChart = new BarChart(context);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);

        // Customize the x-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // Use labels as the x-axis labels
        xAxis.setDrawGridLines(false); // Disable grid lines on the x-axis
        xAxis.setGranularity(1f); // Ensure the x-axis labels correspond to each bar
        xAxis.setCenterAxisLabels(false);

        // Customize the y-axis
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // Start y-axis from 0
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false); // Disable the right y-axis

        // Customize the chart appearance
        barChart.setFitBars(true); // Make the x-axis fit exactly all bars
        barChart.invalidate(); // Refresh the chart

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_chart, null);

        // Add the chart to the custom layout
        ((LinearLayout) dialogView.findViewById(R.id.chartContainer)).addView(barChart);

        // Create and show the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        // Set the click listener for the OK button
        Button okButton = dialogView.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // Helper method to get color based on value (adjust logic as needed)
    private static int getColorForValue(float value) {
        if (value < 10) {
            return ColorTemplate.COLORFUL_COLORS[0]; // Example color for low values
        } else if (value < 20) {
            return ColorTemplate.COLORFUL_COLORS[1]; // Example color for medium values
        } else {
            return ColorTemplate.COLORFUL_COLORS[2]; // Example color for high values
        }
    }

}