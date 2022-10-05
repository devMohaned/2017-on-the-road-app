package com.ride.travel.models;

/**
 * Created by bestway on 03/07/2018.
 */

public class LatLng {
    private double latitude,longitude;

    public LatLng(){}

    public LatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
