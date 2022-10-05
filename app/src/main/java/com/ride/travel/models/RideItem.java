package com.ride.travel.models;


/**
 * Created by bestway on 02/07/2018.
 */
public class RideItem {

    private String idOfRide,idOfUser, startingPoint,endingPoint, timeToLeave,description,startToEnd,fare;
    private LatLng startingLatLng, endingLatLng;

    public RideItem (){}

    public RideItem(String idOfRide,String idOfUser,String description, String startingPoint, String endingPoint,
                    String timeToLeave, LatLng startingLatLng, LatLng endingLatLng,String startToEnd,String fare) {
        this.idOfRide = idOfRide;
        this.idOfUser = idOfUser;
        this.startingPoint = startingPoint;
        this.endingPoint = endingPoint;
        this.timeToLeave = timeToLeave;
        this.startingLatLng = startingLatLng;
        this.endingLatLng = endingLatLng;
        this.description = description;
        this.startToEnd = startToEnd;
        this.fare = fare;
    }

    public String getIdOfUser() {
        return idOfUser;
    }

    public void setIdOfUser(String idOfUser) {
        this.idOfUser = idOfUser;
    }

    public String getStartingPoint() {
        return startingPoint;
    }

    public void setStartingPoint(String startingPoint) {
        this.startingPoint = startingPoint;
    }

    public String getEndingPoint() {
        return endingPoint;
    }

    public void setEndingPoint(String endingPoint) {
        this.endingPoint = endingPoint;
    }

    public String getTimeToLeave() {
        return timeToLeave;
    }

    public void setTimeToLeave(String timeToLeave) {
        this.timeToLeave = timeToLeave;
    }

    public LatLng getStartingLatLng() {
        return startingLatLng;
    }

    public void setStartingLatLng(LatLng startingLatLng) {
        this.startingLatLng = startingLatLng;
    }

    public LatLng getEndingLatLng() {
        return endingLatLng;
    }

    public void setEndingLatLng(LatLng endingLatLng) {
        this.endingLatLng = endingLatLng;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartToEnd() {
        return startToEnd;
    }

    public void setStartToEnd(String startToEnd) {
        this.startToEnd = startToEnd;
    }

    public String getIdOfRide() {
        return idOfRide;
    }

    public void setIdOfRide(String idOfRide) {
        this.idOfRide = idOfRide;
    }

    public String getFare() {
        return fare;
    }

    public void setFare(String fare) {
        this.fare = fare;
    }
}
