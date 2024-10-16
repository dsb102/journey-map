package com.project.test.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.project.test.entity.CustomMarker;
import com.project.test.R;

import java.util.List;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final View mWindow;
    private final List<CustomMarker> markers;

    public CustomInfoWindowAdapter(LayoutInflater inflater, List<CustomMarker> markers) {
        mWindow = inflater.inflate(R.layout.custom_info_window, null);
        this.markers = markers;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null; // Use default frame
    }

    @Override
    public View getInfoContents(Marker marker) {
        for (CustomMarker customMarker : markers) {
            if (customMarker.getTitle().equals(marker.getTitle())) {
                TextView title = mWindow.findViewById(R.id.title);
                TextView info = mWindow.findViewById(R.id.info);


                title.setText(customMarker.getTitle());
                info.setText(customMarker.getInfo());

                return mWindow;
            }
        }
        return null;
    }
}
