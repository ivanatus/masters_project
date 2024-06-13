package com.example.traffic_analysis_app;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NominatimService {
    private static final String BASE_URL = "https://nominatim.openstreetmap.org/";

    private final NominatimAPI api;

    public NominatimService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(NominatimAPI.class);
    }

    public void getCoordinates(String address, GeocodingCallback callback) {
        api.search(address, "json", 1, 1).enqueue(new retrofit2.Callback<List<NominatimResult>>() {
            @Override
            public void onResponse(Call<List<NominatimResult>> call, retrofit2.Response<List<NominatimResult>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onFailure(new Exception("No results found"));
                }
            }

            @Override
            public void onFailure(Call<List<NominatimResult>> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    public interface GeocodingCallback {
        void onSuccess(NominatimResult result);
        void onFailure(Throwable t);
    }
}
