package com.ride.travel.models;

/**
 * Created by bestway on 30/06/2018.
 */

public class RatingItem {

    String id,ratingText;
    float rating;
    long timestamp;

    public RatingItem()
    {}

    public RatingItem(String id, String ratingText, float rating,long timestamp) {
        this.id = id;
        this.ratingText = ratingText;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    public RatingItem(String id, float rating,long timestamp) {
        this.id = id;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    public RatingItem(String id, float rating) {
        this.id = id;
        this.rating = rating;
    }


    public RatingItem(String id, String ratingText, float rating) {
        this.id = id;
        this.ratingText = ratingText;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRatingText() {
        return ratingText;
    }

    public void setRatingText(String ratingText) {
        this.ratingText = ratingText;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
