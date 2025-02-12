package com.example.touchalyticsapp;

import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

public class Swipe {
    int userId;
    List<SwipePoint> points;

    public Swipe(int userId) {
        this.userId = userId;
        this.points = new ArrayList<>();
    }

    public int getUserId() { return userId; }
    public List<SwipePoint> getPoints() { return points; }

    void push(MotionEvent e) {
        points.add(new SwipePoint(e.getX(), e.getY(), e.getPressure(), e.getSize(), e.getEventTime()));
    }
    void push(float x, float y, float pressure, float size, long timestamp) {
        points.add(new SwipePoint(x, y, pressure, size, timestamp));
    }

    public float getStartX() {
        return this.points.get(0).x;
    }
    public float getStartY() {
        return this.points.get(0).y;
    }
    public float getEndX() {
        return this.points.get(this.points.size()-1).x;
    }
    public float getEndY() {
        return this.points.get(this.points.size()-1).y;
    }
    public long getDuration() {
        return this.points.get(this.points.size()-1).timestamp - this.points.get(0).timestamp;
    }

    protected SwipePoint getMidpoint() {
        if(points.size() % 2 == 0) {
            return SwipePoint.mean(points.get(points.size()/2), points.get(points.size()/2 - 1));
        } else {
            return points.get(points.size()/2);
        }
    }
}
