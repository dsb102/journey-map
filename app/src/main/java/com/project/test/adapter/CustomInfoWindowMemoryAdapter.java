package com.project.test.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.makeramen.roundedimageview.RoundedImageView;
import com.project.test.R;
import com.project.test.model.TagMemory;

import java.util.List;

public class CustomInfoWindowMemoryAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;

    private final List<TagMemory> markers;

    public CustomInfoWindowMemoryAdapter(LayoutInflater inflater, List<TagMemory> markers) {
        this.mWindow = inflater.inflate(R.layout.custom_info_window_memory, null);
        this.markers = markers;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
            return null;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        for (TagMemory tagMemory : markers) {
            if (tagMemory.getTagName().equals(marker.getTitle())) {
                TextView title = mWindow.findViewById(R.id.infoWindowTitle);
                TextView info = mWindow.findViewById(R.id.infoWindowDesc);
                RoundedImageView image = mWindow.findViewById(R.id.infoWindowIv);

                title.setText(tagMemory.getTagName());
                info.setText(tagMemory.getNote());
                Glide.with(mWindow)
                    .load(tagMemory.getImage())  // URL của hình ảnh
                    .placeholder(R.drawable.background_intro)  // Ảnh thay thế khi đang tải
                    .error(R.drawable.background_button2_intro)  // Ảnh thay thế khi có lỗi
                    .into(image);  // Đặt hình ảnh vào ImageView
            }
        }
        return mWindow;
    }
}
