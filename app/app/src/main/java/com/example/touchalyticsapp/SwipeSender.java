package com.example.touchalyticsapp;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SwipeSender {
    @POST("/auth")
    Call<ResponseBody> sendSwipe(@Body String featuresJson);
}
