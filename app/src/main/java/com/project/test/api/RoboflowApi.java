package com.project.test.api;

import com.nothing.firestoreup.model.DetectionResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RoboflowApi {

    @GET("landmark-detection-jqu66/1")
    Call<DetectionResponse> getPredictions(
            @Query("api_key") String apiKey,
            @Query("image") String imageUrl
    );
}