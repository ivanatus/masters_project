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
import android.os.Handler;
import android.os.Looper;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements LocationListener {
    MapView mapView;
    private static final int REQUEST_LOCATION_PERMISSION = 1; // Define your request code here
    private static final long MIN_TIME_BW_UPDATES = 60000; // 1 minute in milliseconds
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    BottomNavigationView bottom_navigation;
    private NominatimService nominatimService;
    private RoutingService routingService;
    EditText address;
    ImageButton search, analytics;
    GeoPoint current_location;
    boolean directions = false;
    boolean showToolbarMenu = false;
    public boolean isChartFragmentVisible = false, selectingGeopoints = false;

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

        // Add a long-click listener to the map to capture long clicks
        addLongClickListenerToMap();

        // Initialize the Nominatim service
        nominatimService = new NominatimService();
        address = findViewById(R.id.address_search);
        search = findViewById(R.id.search_address);
        analytics = findViewById(R.id.analyticsButton);

        if (mapView != null) {
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(true);
        } else {
            Log.e("MAPVIEW", "MapView is null");
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
            if(mapView != null) {
                // Permissions already granted, proceed with location access
                startLocationUpdates();
            }
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

        ChartFragment chartFragment = ChartFragment.newInstance(null);

        fragmentTransaction.replace(R.id.fragmentContainer, chartFragment);
        fragmentTransaction.commit();

        analytics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameLayout fragment = findViewById(R.id.fragmentContainer);
                if(fragment.getVisibility() == View.GONE) {
                    isChartFragmentVisible = true;
                    fragment.setVisibility(View.VISIBLE);
                } else {
                    isChartFragmentVisible = false;
                    fragment.setVisibility(View.GONE);
                }
            }
        });

        //graphPlotting(this, 4.5f, 0.7f, 2.3f, 0.2f,13f, 3.6f, 1.5f, 0.9f,5.3f, 2.7f, 0.8f, 1f,2.3f, 1.8f);
    }

    private void startLocationUpdates() {
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
            if (location.hasBearing()) {
                Log.d("Directions", "location.hasBearing");
                mapView.setMapOrientation(-location.getBearing());
            }
        }
    }

    private void addLongClickListenerToMap() {

        if(!selectingGeopoints) {
            Log.d("GETGEOPOINTS", "In addLongClickListenerToMap");
            MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
                @Override
                public boolean singleTapConfirmedHelper(GeoPoint p) {
                    // Do nothing on single tap
                    return false;
                }

                @Override
                public boolean longPressHelper(GeoPoint p) {
                    // This is where the long-press logic goes
                    addMarkerAtLocation(p);
                    return true;
                }
            };

            MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(getApplicationContext(), mapEventsReceiver);
            mapView.getOverlays().add(mapEventsOverlay);
        }
    }

    private void addMarkerAtLocation(GeoPoint geoPoint) {
        Marker marker = new Marker(mapView);
        marker.setPosition(geoPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Set a custom marker icon if you have one
        Drawable markerIcon = ContextCompat.getDrawable(this, R.drawable.destination_location);
        marker.setIcon(markerIcon);

        mapView.getOverlays().add(marker);
        mapView.invalidate();  // Refresh the map to display the marker

        // Introduce a delay before adding the click listener to the marker
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                addOnClickListener(marker, geoPoint);
            }
        }, 1000);  // Delay in milliseconds (1000ms = 1 second)
    }

    private void addOnClickListener(Marker marker, GeoPoint geoPoint){
        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                showPopupMenu(marker, geoPoint);
                return false;
            }
        });
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
                Toast.makeText(MainActivity.this, "Neuspješno preuzimanje koordinata: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                //Toast.makeText(MainActivity.this, "Save location clicked", Toast.LENGTH_SHORT).show();
                showSaveLocationDialog(marker.getPosition());
                popupWindow.dismiss();
            }
        });

        // Show the popup window next to the marker
        // Calculate the offset in pixels
        int xOffset = dpToPx(50);
        int yOffset = dpToPx(100);
        //popupWindow.showAsDropDown(mapView, (int) marker.getPosition().getLongitude(), (int) marker.getPosition().getLatitude(), Gravity.TOP);
        // Ensure the popup is shown above the bottom navigation
        popupWindow.showAtLocation(mapView, Gravity.TOP | Gravity.START, xOffset, yOffset);
        // Adjust position of the PopupWindow
        //popupWindow.showAtLocation(mapView, Gravity.TOP, xOffset, yOffset);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public void getDirections(GeoPoint start, GeoPoint end) {
        if (start == null) {
            Toast.makeText(this, "Nevaljana početna lokacija.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (end == null) {
            Toast.makeText(this, "Nevaljana lokacija destinacije.", Toast.LENGTH_SHORT).show();
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
                    analytics.setVisibility(View.VISIBLE);
                } else {
                    Log.d("GPS_ROUTES", "Failed to get routes");
                    Toast.makeText(MainActivity.this, "Neuspješno preuzimanje ruta do destinacije.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RoutingResponse> call, Throwable t) {
                Log.d("GPS_ROUTES", t.getMessage());
                Toast.makeText(MainActivity.this, "Neuspješno preuzimanje ruta do destinacije.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawRoute(List<List<GeoPoint>> allRoutes) {//List<GeoPoint> routePoints) {
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

        Toast.makeText(this, "Lokacija spremljena pod oznakom: " + label, Toast.LENGTH_SHORT).show();
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
                markerResource = R.drawable.work_place;
                break;
            case "home":
                markerResource = R.drawable.home_location;
                break;
            case "posao":
                markerResource = R.drawable.work_place;
                break;
            case "dom":
                markerResource = R.drawable.home_location;
                break;
            default:
                markerResource = R.drawable.destination_location; // Default marker drawable
                break;
        }
        return ContextCompat.getDrawable(this, markerResource);
    }

    public void hideChartFragment() {
        Log.d("GETGEOPOINTS", "In hideChartFragment");
        FrameLayout fragmentContainer = findViewById(R.id.fragmentContainer);
        fragmentContainer.setVisibility(View.GONE);
        selectingGeopoints = true;

        // Start selecting geopoints on the MapView
        startSelectingGeoPoints();
    }

    private void startSelectingGeoPoints() {
        // Logic for selecting GeoPoints on MapView
        addGeoPointListenerToMap();
    }

    // Add a method to pass the selected GeoPoints back to ChartFragment
    public void onGeoPointsSelected(ArrayList<GeoPoint> selectedGeoPoints) {
        ChartFragment chartFragment = (ChartFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        selectingGeopoints = false;
        if (chartFragment != null) {
            chartFragment.updateGeoPoints(selectedGeoPoints);
        }
    }

    private void toggleChartFragmentVisibility() {
        Log.d("GETGEOPOINTS", "In toggleChartFragmentVisibility");
        FrameLayout fragmentContainer = findViewById(R.id.fragmentContainer);
        if (isChartFragmentVisible) {
            fragmentContainer.setVisibility(View.GONE);
            isChartFragmentVisible = false;
            Log.d("GETGEOPOINTS", "In toggleChartFragmentVisibility, gone");
        } else {
            fragmentContainer.setVisibility(View.VISIBLE);
            isChartFragmentVisible = true;
            Log.d("GETGEOPOINTS", "In toggleChartFragmentVisibility, visible");
        }
    }

    private void addGeoPointListenerToMap() {
        if(selectingGeopoints) {
            Log.d("GETGEOPOINTS", "In addGeoPointListenerToMap");
            MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
                @Override
                public boolean singleTapConfirmedHelper(GeoPoint p) {
                    return false;
                }

                @Override
                public boolean longPressHelper(GeoPoint p) {
                    // Store selected geopoints in an ArrayList
                    ArrayList<GeoPoint> selectedGeoPoints = new ArrayList<>();
                    selectedGeoPoints.add(p);

                    // Pass selected points back to the ChartFragment
                    analytics.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onGeoPointsSelected(selectedGeoPoints);
                            toggleChartFragmentVisibility();
                        }
                    });
                    return true;
                }
            };

            MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(getApplicationContext(), mapEventsReceiver);
            mapView.getOverlays().add(mapEventsOverlay);
        }
    }
}