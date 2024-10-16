package com.project.test;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.project.test.adapter.CustomInfoWindowAdapter;
import com.project.test.entity.CustomMarker;
import com.project.test.model.Tag;
import com.project.test.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<CustomMarker> markers = new ArrayList<>();
    private static final int PICK_IMAGE_REQUEST = 1; // Request code for picking an image
    private Marker currentMarker;
    private List<LatLng> markerPositions = new ArrayList<>();
    private UserRepository userRepo = new UserRepository();

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_menu);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddLocationDialog(); // Show dialog to add location
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
//            ImageView imageViewLocation = findViewById(R.id.imageViewLocation);
//            imageViewLocation.setImageURI(imageUri); // Display selected image
//            imageViewLocation.setVisibility(View.VISIBLE); // Make the ImageView visible
        }
    }

    // Show dialog to add location
    private void showAddLocationDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_location);
        dialog.setCancelable(true);

        // Get references to the dialog's views
        EditText editTextLocationName = dialog.findViewById(R.id.editTextLocationName);
        EditText editTextLocationInfo = dialog.findViewById(R.id.editTextLocationInfo);
        Button buttonAddLocation = dialog.findViewById(R.id.buttonAddLocation);

        buttonAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationName = editTextLocationName.getText().toString();
                String locationInfo = editTextLocationInfo.getText().toString();
                addMarkerWithInfo(locationName, locationInfo);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void addMarkerWithInfo(String locationName, String locationInfo) {
        if (mMap != null) {
            LatLng centerOfMap = mMap.getCameraPosition().target;

            // Create and store the custom marker
            CustomMarker customMarker = new CustomMarker(centerOfMap, locationName, locationInfo);
            markers.add(customMarker);

            // Add marker to the map
            mMap.addMarker(new MarkerOptions().position(centerOfMap).title(locationName));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(centerOfMap));
        }
    }

    // Navigation item selected listener function
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.bottom_home:
                            return true;
                        case R.id.bottom_imagesearch:
                            startActivity(new Intent(getApplicationContext(), ImageSearchActivity.class));
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish();
                            return true;
                        case R.id.bottom_message:
                            startActivity(new Intent(getApplicationContext(), MessageActivity.class));
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish();
                            return true;
                        case R.id.bottom_journey:
                            startActivity(new Intent(getApplicationContext(), JourneyManagementActivity.class));
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish();
                            return true;
                        default:
                            return false;
                    }
                }
            };

    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;

        // Set the custom info window adapter
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(getLayoutInflater(), markers));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        // Example of adding a marker for demonstration
//        LatLng hanoi = new LatLng(21.0285, 105.8542);
//        CustomMarker customMarker = new CustomMarker(hanoi, "Marker in Hanoi", "Some info about Hanoi");
//        markers.add(customMarker);
//        mMap.addMarker(new MarkerOptions().position(hanoi).title(customMarker.getTitle()));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hanoi, 10));


        // Kiểm tra quyền truy cập vị trí và lấy vị trí hiện tại
        requestLocationPermission();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation(); // lay vi tri hien tai hien thi
        loadLocationFriends(); // load vi tri ban be
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
//                Dialog dialog = new Dialog(MapsActivity.this);
//                dialog.setContentView(R.layout.dialog_add_location);
//                dialog.setCancelable(true);
//
//                // Get references to the dialog's views
//                EditText editTextLocationName = dialog.findViewById(R.id.editTextLocationName);
//                EditText editTextLocationInfo = dialog.findViewById(R.id.editTextLocationInfo);
//                Button buttonAddLocation = dialog.findViewById(R.id.buttonAddLocation);
//
//                buttonAddLocation.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        String locationName = editTextLocationName.getText().toString();
//                        String locationInfo = editTextLocationInfo.getText().toString();
//                        CustomMarker customMarker = new CustomMarker(latLng, locationName, locationInfo);
//                        markers.add(customMarker);
//
//                        // Add marker to the map
//                        mMap.addMarker(new MarkerOptions().position(latLng).title(locationName));
//                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
//                        dialog.dismiss();
//                    }
//                });
//
//                dialog.show();


                drawLine(latLng);
            }
        });
    }

    private void updateViTriCuaMinh(LatLng latLng) {
        userRepo.updateLocation("dungsobin103", latLng.latitude, latLng.longitude);
    }

    private void loadLocationFriends() {
        userRepo.getLocationUsers(
        "dungsobin103",
            userLocations -> {
                Log.d("MapsActivity", "User locations: " + new Gson().toJson(userLocations));
                for (int i = 0; i < userLocations.size(); i++) {
                    CustomMarker customMarker = new CustomMarker(
                        new LatLng(userLocations.get(i).getLatitude(), userLocations.get(i).getLongitude()),
                        userLocations.get(i).getName(),
                        userLocations.get(i).getName() + " is here"
                    );
                    markers.add(customMarker);
                    mMap.addMarker(new MarkerOptions().position(customMarker.getPosition()).title(customMarker.getTitle()));

                }

                //TODO: Move camera to show all markers
//                LatLngBounds.Builder builder = new LatLngBounds.Builder();
//                for (CustomMarker mark : markers) {
//                    builder.include(mark.getPosition());
//                }
//                LatLngBounds bounds = builder.build();
//                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
            }
        ); // todo fix name
    }

    private void drawLine(@NonNull LatLng latLng) {
        CustomMarker customMarker = new CustomMarker(latLng, "Marker at clicked position", "Some info here");
        markers.add(customMarker);
        markerPositions.add(latLng);  // Lưu vị trí marker

        // Thêm marker vào bản đồ
        currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(customMarker.getTitle()));

        // Di chuyển camera tới vị trí marker mới
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

        // Nếu có nhiều hơn 1 marker, vẽ đường nối các marker
        if (markerPositions.size() > 1) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(markerPositions)  // Thêm tất cả các vị trí marker vào polyline
                    .clickable(true);         // Để đường có thể nhấn được (nếu muốn)

            // Thêm polyline vào bản đồ
            mMap.addPolyline(polylineOptions);
        }
    }

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp, lấy vị trí
                getCurrentLocation();
            } else {
                // Quyền không được cấp, có thể thông báo cho người dùng
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private FusedLocationProviderClient fusedLocationClient;


    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        // Vị trí hiện tại
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                        mMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        updateViTriCuaMinh(currentLatLng);
                    } else {
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MapsActivity", "Error getting location: " + e.getMessage());
                });
        }
    }

}
