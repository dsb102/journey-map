package com.project.test.entity;

import com.google.android.gms.maps.model.LatLng;

public class CustomMarker {
    private LatLng position;
    private String title;
    private String info;


    public CustomMarker(LatLng position, String title, String info) {
        this.position = position;
        this.title = title;
        this.info = info;
    }

    public LatLng getPosition() {
        return position;
    }

    public String getTitle() {
        return title;
    }

    public String getInfo() {
        return info;
    }

}
