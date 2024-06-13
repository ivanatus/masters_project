package com.example.traffic_analysis_app;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RoutingService {
    @GET("route/v1/driving/{start};{end}")
    Call<RoutingResponse> getRoute(
            @Path("start") String start,
            @Path("end") String end,
            @Query("overview") String overview,
            @Query("geometries") String geometries
    );
}
