package com.example.touchalyticsapp;

import android.annotation.SuppressLint;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

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

    public SwipeReport(Swipe swipe) {
        startX = swipe.getStartX();
        startY = swipe.getStartY();
        endX = swipe.getEndX();
        endY = swipe.getEndY();
        boundsArea = Math.sqrt((endX-startX)*(endX-startX) + (endY-startY)*(endY-startY));
        midPressure = swipe.getMidpoint().pressure;
        duration = swipe.getDuration();
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
}
