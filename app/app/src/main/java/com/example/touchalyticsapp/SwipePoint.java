package com.example.touchalyticsapp;

public class SwipePoint {
    float x;
    float y;
    long timestamp;

    SwipePoint(float x, float y, long timestamp) {
        this.x = x;
        this.y = y;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("TouchPoint{(%.2f, %.2f) at %ld}", this.x, this.y, this.timestamp);
    }
}
