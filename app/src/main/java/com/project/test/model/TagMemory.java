package com.project.test.model;

public class TagMemory {

    private static final String TAG = "TagMemory"; // Tag for logging purposes

    private String tagId;
    private String tagName;
    private String note;
    private double latitude;
    private double longitude;
    private boolean check;
    private int order;
    private String image;

    public TagMemory() {
    }

    public TagMemory(String tagId, String tagName, String note, double latitude, double longitude, boolean check, int order, String image) {
        this.tagId = tagId;
        this.tagName = tagName;
        this.note = note;
        this.latitude = latitude;
        this.longitude = longitude;
        this.check = check;
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
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public static TagMemory tagToTagMemory(Tag tag) {
        return new TagMemory(tag.getTagId(), tag.getTagName(), "", tag.getLatitude(), tag.getLongitude(), false, tag.getOrder(), "");
    }
}
