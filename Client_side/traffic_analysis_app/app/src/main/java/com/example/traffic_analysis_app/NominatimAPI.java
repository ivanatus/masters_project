package com.example.traffic_analysis_app;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;
public interface NominatimAPI {
    @GET("search")
    Call<List<NominatimResult>> search(
            @Query("q") String address,
            @Query("format") String format,
            @Query("addressdetails") int addressDetails,
            @Query("limit") int limit
    );
}


