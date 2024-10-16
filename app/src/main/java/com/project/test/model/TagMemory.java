package com.project.test.model;

public class TagMemory {

    private static final String TAG = "TagMemory"; // Tag for logging purposes

    private String tagId;
    private String tagName;
    private double latitude;
    private double longitude;
    private boolean isCheck;
    private int order;
    private String image;

    public TagMemory() {
    }

    public TagMemory(String tagId, String tagName, double latitude, double longitude, boolean isCheck, int order, String image) {
        this.tagId = tagId;
        this.tagName = tagName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isCheck = isCheck;
        this.order = order;
        this.image = image;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
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

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
