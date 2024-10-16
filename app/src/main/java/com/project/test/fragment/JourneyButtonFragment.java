package com.project.test.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.project.test.R;

public class JourneyButtonFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap googleMap;

    public JourneyButtonFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journey_buttons, container, false);

        // Tìm các nút floating button
        FloatingActionButton createButton = view.findViewById(R.id.fab_create);
        FloatingActionButton editButton = view.findViewById(R.id.fab_edit);
        FloatingActionButton memoriesButton = view.findViewById(R.id.fab_memories);

        // Thiết lập sự kiện nhấn cho các nút
        createButton.setOnClickListener(v -> loadFragment(new CreateJourneyFragment()));
        editButton.setOnClickListener(v -> {
//            Toast.makeText(getContext(), "Tính năng Chỉnh sửa sắp có! (editButton.setOnClickListener)", Toast.LENGTH_SHORT).show();
                loadFragment(new EditJourneyFragment());
            }
        );
        memoriesButton.setOnClickListener(v ->
                Toast.makeText(getContext(), "Tính năng Kỷ niệm sắp có!", Toast.LENGTH_SHORT).show()
        );

        // Khởi tạo SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    private void loadFragment(Fragment fragment) {
        // Chuyển sang fragment khác
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, fragment)
                    .addToBackStack(null) // Cho phép quay lại fragment trước đó nếu cần
                    .commit();
        }
    }
    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;
        // Đặt vị trí camera ban đầu
        LatLng initialPosition = new LatLng(21.0285, 105.8542); // Hà Nội
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 10));
    }
}
