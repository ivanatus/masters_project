package com.example.traffic_analysis_app;

import java.util.List;

public class RoutingResponse {
    public List<Route> routes;

    public static class Route {
        public Geometry geometry;
        public String summary;
        public List<Leg> legs;

        public static class Geometry {
            public List<List<Double>> coordinates;
        }

        public static class Leg {
            public String summary;
            public List<Step> steps;

            public static class Step {
                public String maneuver;
            }
        }
    }
}
