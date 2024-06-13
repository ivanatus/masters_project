package com.example.traffic_analysis_app;

import java.util.List;

public class RoutingResponse {
    public List<Route> routes;

    public class Route {
        public Geometry geometry;
    }

    public class Geometry {
        public List<List<Double>> coordinates;
    }
}
