package com.example.traffic_analysis_app;

import java.util.ArrayList;
import java.util.List;

public class GeoPointData {
    // Data for inferential analysis
    public List<Integer> cars = new ArrayList<>();
    public List<Integer> buses = new ArrayList<>();
    public List<Integer> bikes = new ArrayList<>();
    public List<Integer> people = new ArrayList<>();
    public List<Integer> motorbikes = new ArrayList<>();
    public List<Integer> trucks = new ArrayList<>();
    public List<Integer> trains = new ArrayList<>();
    public float car_normality, bus_normality, bike_normality, people_normality, motor_normality, truck_normality, train_normality;
}
