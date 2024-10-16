package com.project.test.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.project.test.R;
import com.project.test.model.Tag;

import java.util.Arrays;
import java.util.UUID;

public class SearchFragment extends Fragment {
    private static final String TAG = "SearchFragment";
    private TagSelectedListener tagSelectedListener;

    public interface TagSelectedListener {
        void onTagSelected(Tag tag);
    }

    public void setTagSelectedListener(TagSelectedListener listener) {
        this.tagSelectedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false); // Đảm bảo layout đúng

        // Khởi tạo Places API
        Places.initialize(getContext(), "AIzaSyBZvoWYwyeOmhAjoX5xuXcas6RIL79dbJQ");

        // Khởi tạo AutocompleteSupportFragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment); // Thay đổi ID theo layout của bạn

        if (autocompleteFragment == null) {
            Log.e(TAG, "AutocompleteSupportFragment is null");
            return view;
        }

        // Xác định các loại dữ liệu địa điểm cần trả về
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.LOCATION));

        // Thiết lập PlaceSelectionListener để xử lý phản hồi
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());

                LatLng selectedLocation = place.getLatLng();
                if (selectedLocation != null) {
                    // Tạo Tag từ địa điểm được chọn
                    Tag tag = new Tag(UUID.randomUUID().toString(), place.getDisplayName(), selectedLocation.latitude, selectedLocation.longitude, 0); // Thêm giá trị order mặc định là 0

                    // Gọi phương thức để truyền Tag về CreateJourneyFragment
                    if (tagSelectedListener != null) {
                        tagSelectedListener.onTagSelected(tag);
                        Log.d(TAG, "Tag selected: " + tag.getTagName());
                    }
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        return view;
    }
}
