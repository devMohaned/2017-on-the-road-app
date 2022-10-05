package com.ride.travel.models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by bestway on 08/07/2018.
 */

public class DummyLocation {

    private String g;
    private com.google.android.gms.maps.model.LatLng latLng;
    private long timestamp;

    public DummyLocation(){}

    public DummyLocation(String g, LatLng latLng, long timestamp) {
        this.g = g;
        this.latLng = latLng;
        this.timestamp = timestamp;
    }

    public String getG() {
        return g;
    }

    public void setG(String g) {
        this.g = g;
    }

    public com.google.android.gms.maps.model.LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
