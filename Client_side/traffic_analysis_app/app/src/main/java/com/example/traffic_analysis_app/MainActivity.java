package com.example.traffic_analysis_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottom_navigation = findViewById(R.id.bottom_navigation);
        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if(id == R.id.recording){
                    Intent recording = new Intent(getApplicationContext(), RecordingActivity.class);
                    startActivity(recording);
                    finish();
                } else if(id == R.id.info){
                    /*Intent info = new Intent(getApplicationContext(), .class);
                    startActivity(info);
                    finish();*/
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
        marker.setPosition(new GeoPoint(latitude, longitude));
        mapView.getOverlays().add(marker);

        // Zoom to the new location
        mapView.getController().setZoom(18); // Adjust the zoom level as needed
        mapView.getController().setCenter(currentLocation);
    }

    public void getCoordinatesForAddress(String address) {
        nominatimService.getCoordinates(address, new NominatimService.GeocodingCallback() {
            @Override
            public void onSuccess(NominatimResult result) {
                // Update the map with the obtained coordinates
                GeoPoint geoPoint = new GeoPoint(result.lat, result.lon);
                mapView.getController().setCenter(geoPoint);
                Marker marker = new Marker(mapView);
                marker.setPosition(geoPoint);
                mapView.getOverlays().add(marker);
                mapView.getController().setZoom(18);

                marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView) {
                        getDirections(current_location, geoPoint);
                        return false;
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                // Handle the error
                Toast.makeText(MainActivity.this, "Failed to get coordinates", Toast.LENGTH_SHORT).show();
            }
        });
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

        String startCoords = start.getLongitude() + "," + start.getLatitude();
        String endCoords = end.getLongitude() + "," + end.getLatitude();

        Call<RoutingResponse> call = routingService.getRoute(startCoords, endCoords, "full", "geojson");
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
        });
    }

    private void drawRoute(List<GeoPoint> routePoints) {
        Polyline routeLine = new Polyline();
        routeLine.setPoints(routePoints);
        mapView.getOverlays().add(routeLine);
        mapView.invalidate();
        mapView.getController().setZoom(15);
    }
}