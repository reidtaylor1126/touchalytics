package com.example.touchalyticsapp;

public class SwipePoint {
    float x;
    float y;
    float pressure;
    float size;
    long timestamp;

    SwipePoint(float x, float y, float pressure, float size, long timestamp) {
        this.x = x;
        this.y = y;
        this.pressure = pressure;
        this.size = size;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("TouchPoint{(%.2f, %.2f) at %ld}", this.x, this.y, this.timestamp);
    }

    public static SwipePoint mean(SwipePoint a, SwipePoint b) {
        return new SwipePoint(
                (a.x + b.x) / 2,
                (a.y + b.y) / 2,
                (a.pressure + b.pressure) / 2,
                (a.size + b.size) / 2,
                (a.timestamp + b.timestamp) / 2
        );
    }
}
