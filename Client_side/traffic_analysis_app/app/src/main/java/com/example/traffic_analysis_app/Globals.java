package com.example.traffic_analysis_app;

import android.app.Application;

public class Globals {
    private double latitude;
    private double longitude;

    private static Globals instance;
    private double globalDouble1;
    private double globalDouble2;

    private Globals() {}

    public static synchronized Globals getInstance() {
        if (instance == null) {
            instance = new Globals();
        }
        return instance;
    }

    public double getGlobalLatitude() {
        return latitude;
    }

    public void setGlobalLatitude(double value) {
        this.latitude = value;
    }

    public double getGlobalLongitude() {
        return longitude;
    }

    public void setGlobalLongitude(double value) {
        this.longitude = value;
    }
}