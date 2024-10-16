package com.project.test.model;

public class Tag {

    private static final String TAG = "Tag"; // Tag for logging purposes

    private String tagId;  // Changed from id to TagId
    private String tagName; // Changed from name to TagName
    private double latitude;
    private double longitude;
    private int order;

    public Tag() {}

    public Tag(String TagId, String TagName, double latitude, double longitude, int order) {
        this.tagId = TagId;
        this.tagName = TagName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.order = order; // Cập nhật constructor
    }

    // Getter and setter for TagId
    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    // Getter and setter for TagName
    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    // Getter and setter for latitude
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    // Getter and setter for longitude
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}