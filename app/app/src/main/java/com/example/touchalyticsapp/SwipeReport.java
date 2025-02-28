package com.example.touchalyticsapp;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SwipeReport {
    float startX;
    float startY;
    float endX;
    float endY;
    float length = 0;
    double boundsArea;
    float midPressure;
    long duration;
    float[] quartileVelocities = new float[4];
    float[] quartilePressures = new float[4];
    int userID;

    public SwipeReport(Swipe swipe) {
        startX = swipe.getStartX();
        startY = swipe.getStartY();
        endX = swipe.getEndX();
        endY = swipe.getEndY();
        boundsArea = Math.sqrt((endX-startX)*(endX-startX) + (endY-startY)*(endY-startY));
        midPressure = swipe.getMidpoint().pressure;
        duration = swipe.getDuration();
        userID = swipe.userId;
        int[] quartiles = new int[4];
        quartiles[0] = swipe.points.size()/4;
        quartiles[1] = swipe.points.size()/2;
        quartiles[2] = swipe.points.size() - quartiles[0];
        quartiles[3] = swipe.points.size();
        int currentQ = 0;

        float qSumLength = 0;
        float qSumPressure = swipe.points.get(0).pressure;
        int qCount = 1;

        for(int i = 1; i < swipe.points.size(); i++) {
            if(i > quartiles[currentQ]) {
                quartilePressures[currentQ] = qSumPressure/qCount;
                quartileVelocities[currentQ] = qSumLength/Math.max(qCount-1,1);
                currentQ++;
                qSumLength = 0;
                qSumPressure = 0;
                qCount = 0;
            }
            SwipePoint diff = SwipePoint.difference(swipe.points.get(i-1), swipe.points.get(i));
            qSumLength += diff.magnitude();
            length += diff.magnitude();
            qSumPressure += swipe.points.get(i).pressure;
            qCount++;
        }
        quartilePressures[3] = qSumPressure/qCount;
        quartileVelocities[3] = qSumLength/Math.max(qCount-1,1);
    }

    @SuppressLint("DefaultLocale")
    public void send(DatabaseReference swipeRef) {
        swipeRef.child("StartX").setValue(startX);
        swipeRef.child("StartY").setValue(startY);
        swipeRef.child("EndX").setValue(endX);
        swipeRef.child("EndY").setValue(endY);
        swipeRef.child("Length").setValue(length);
        swipeRef.child("BoundsArea").setValue(boundsArea);
        swipeRef.child("MidpointPressure").setValue(midPressure);
        swipeRef.child("Duration").setValue(duration);
        for(int i = 0; i < 4; i++) {
            swipeRef.child(String.format("Q%dVelocity",i+1)).setValue(quartileVelocities[i]);
            swipeRef.child(String.format("Q%dPressure",i+1)).setValue(quartilePressures[i]);
        }
    }

    public JSONArray asJsonArray() {
        ArrayList<Float> features = new ArrayList<Float>();
        features.add(startX);
        features.add(startY);
        features.add(endX);
        features.add(endY);
        features.add(length);
        features.add((float) duration);
        features.add((float) boundsArea);
        features.add(midPressure);
        for(int i = 0; i < 4; i++) {
            features.add(quartileVelocities[i]);
            features.add(quartilePressures[i]);
        }
        features.add((float) userID);
        return new JSONArray(features);
    }

    public boolean auth(String apiTarget) {
//        String json = "{" +
//                "StartX: " + startX +
//                ", StartY: " + startY +
//                ", endX: " + endX +
//                " ,endY: " + endY +
//                ", Length: " + length +
//                ", BoundsArea: " + boundsArea +
//                ", MidpointPressure: " + midPressure +
//                ", Duration: " + duration +
//                ", Q1Velocity: " + quartileVelocities[0] +
//                ", Q2Velocity: " + quartileVelocities[1] +
//                ", Q3Velocity: " + quartileVelocities[2] +
//                ", Q4Velocity: " + quartileVelocities[3] +
//                ", Q1Pressure: " + quartilePressures[0] +
//                ", Q2Pressure: " + quartilePressures[1] +
//                ", Q3Pressure: " + quartilePressures[2] +
//                ", Q4Pressure: " + quartilePressures[3]
//                ;
//
//        System.out.println(json);

        Retrofit sender = new Retrofit.Builder()
                .baseUrl(apiTarget)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SwipeSender swipeSender = sender.create(SwipeSender.class);
        Call<ResponseBody> response = swipeSender.sendSwipe(asJsonArray());

        response.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    Log.i("Features", response.body().toString());
                } else {
                    assert response.errorBody() != null;
                    Log.e("Features", response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                Log.e("Features", throwable.getMessage());
            }
        });
//        HttpURLConnection conn = null;
//        try {
//            URL apiURL = new URL(apiTarget);
//            conn = (HttpURLConnection) apiURL.openConnection();
//            conn.setDoOutput(true);
//            conn.setDoInput(true);
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json");
//        } catch (MalformedURLException e) {
//            System.out.println("Malformed URL");
//        } catch(IOException e) {
//            System.out.println("Error during connection setup");
//        }
//        if(conn != null) {
//            try {
//                OutputStream output = conn.getOutputStream();
//                OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
//                writer.write(json);
//                writer.flush();
//                writer.close();
//                output.close();
//                conn.connect();
//                System.out.println(conn.getResponseCode());
//            } catch (IOException e) {
//                System.out.println("Error while sending payload");
//                System.out.println(e.getMessage());
//            }
//            try {
//                InputStream input = conn.getInputStream();
//                InputStreamReader reader = new InputStreamReader(input);
//                BufferedReader bReader = new BufferedReader(reader);
//                String response = bReader.readLine();
//                System.out.println(response);
//                bReader.close();
//                reader.close();
//                input.close();
//            } catch (IOException e) {
//                System.out.println("Error while reading response");
//                System.out.println(e.getMessage());
//            }
//        }

        return true;
    }
}
