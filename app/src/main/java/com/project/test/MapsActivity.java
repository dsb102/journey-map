package com.project.test;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.project.test.adapter.CustomInfoWindowAdapter;
import com.project.test.entity.CustomMarker;
import com.project.test.model.Tag;
import com.project.test.repository.TagRepository;
import com.project.test.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String HARD_CODED_USER_ID = "dungsobin103";
    private GoogleMap mMap;
    private List<CustomMarker> markers = new ArrayList<>();
    private static final int PICK_IMAGE_REQUEST = 1; // Request code for picking an image
    private Marker currentMarker;
    private List<LatLng> markerPositions = new ArrayList<>();
    private UserRepository userRepo = new UserRepository();
    private TagRepository tagRepo = new TagRepository();

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
//                showAddLocationDialog(); // Show dialog to add location
                LatLng centerOfMap = mMap.getCameraPosition().target;
                showAddTagDialog(centerOfMap);
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


        // Kiểm tra quyền truy cập vị trí và lấy vị trí hiện tại
        requestLocationPermission();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation(); // lay vi tri hien tai hien thi
        loadLocationFriends(); // load vi tri ban be
        
        loadLocationTaged();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                showAddTagDialog(latLng);
//                drawLine(latLng);
            }
        });
    }

    private void showAddTagDialog(LatLng markerPosition) {
        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate the custom layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_tag, null);

        // InitiuserRepo.getLocationUsers(
        //        "dungsobin103",
        //            userLocations -> {
        //                Log.d("MapsActivity", "User locations: " + new Gson().toJson(userLocations));
        //                for (int i = 0; i < userLocations.size(); i++) {
        //                    CustomMarker customMarker = new CustomMarker(
        //                        new LatLng(userLocations.get(i).getLatitude(), userLocations.get(i).getLongitude()),
        //                        userLocations.get(i).getName(),
        //                        userLocations.get(i).getName() + " is here"
        //                    );
        //                    markers.add(customMarker);
        //                    mMap.addMarker(new MarkerOptions().position(customMarker.getPosition()).title(customMarker.getTitle()));
        //
        //                }
        //
        //                //TODO: Move camera to show all markers
        ////                LatLngBounds.Builder builder = new LatLngBounds.Builder();
        ////                for (CustomMarker mark : markers) {
        ////                    builder.include(mark.getPosition());
        ////                }
        ////                LatLngBounds bounds = builder.build();
        ////                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
        //            }
        //        ); // todo fix namealize EditText and Button views from the layout
        EditText tagNameInput = dialogView.findViewById(R.id.editTextTagName);
        EditText tagNotesInput = dialogView.findViewById(R.id.editTextNotes);
        EditText tagColorInput = dialogView.findViewById(R.id.editTextColor);
        Button saveTagButton = dialogView.findViewById(R.id.buttonSaveTag);
        Button cancelTagButton = dialogView.findViewById(R.id.buttonCancel);

        // Create the AlertDialog and set the custom view
        AlertDialog dialog = builder.setView(dialogView).create();

        // Handle Save button click
        saveTagButton.setOnClickListener(v -> {
            // Get the input values from the EditText fields
            String tagName = tagNameInput.getText().toString();
            String tagNotes = tagNotesInput.getText().toString();
            String tagColor = tagColorInput.getText().toString();

            // Check if the inputs are valid (you can add your own validation)
            if (!tagName.isEmpty() && !tagColor.isEmpty()) {
                Map<String, Object> tagData = new HashMap<>();
                tagData.put("tagName", tagName);
                tagData.put("tagNotes", tagNotes);
                tagData.put("tagColor", tagColor);
                tagData.put("position", new GeoPoint(markerPosition.latitude, markerPosition.longitude));
                tagData.put("createdBy", HARD_CODED_USER_ID); // Use hardcoded user ID
                tagRepo.saveTag(tagData, new TagRepository.OnTagSavedListener() {
                    @Override
                    public void onSuccess() {
                        float[] hsv = new float[3];
                        String tagCol = tagColor != null ? tagColor: "#FF0000";
                        Color.colorToHSV(Color.parseColor(tagCol), hsv);
                        BitmapDescriptor markerColor = BitmapDescriptorFactory.defaultMarker(hsv[0]);
                        mMap.addMarker(new MarkerOptions().position(markerPosition).title(tagName).icon(markerColor));
                        Toast.makeText(getApplicationContext(), "Tag saved", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(getApplicationContext(), "Tag saved error " + message, Toast.LENGTH_SHORT).show();
                    }
                });

                // Dismiss the dialog after saving the tag
                dialog.dismiss();
            } else {
                // Show a message to the user if fields are invalid
                Toast.makeText(this, "Tag name and color cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Cancel button click
        cancelTagButton.setOnClickListener(v -> {
            // Dismiss the dialog without saving anything
            dialog.dismiss();
        });

        // Show the dialog
        dialog.show();
    }

//    tagData.put("tagName", tagName);
//                tagData.put("tagNotes", tagNotes);
//                tagData.put("tagColor", tagColor);
//                tagData.put("position", new GeoPoint(markerPosition.latitude, markerPosition.longitude));
//                tagData.put("createdBy", HARD_CODED_USER_ID); // Use hardcoded user ID
    private void loadLocationTaged() {
        tagRepo.getTags(
                "dungsobin103",
                userLocations -> {
                    Log.d("MapsActivity", "User locations: " + new Gson().toJson(userLocations));
                    for (int i = 0; i < userLocations.size(); i++) {
                        GeoPoint position = (GeoPoint) userLocations.get(i).get("position");
                        if (position == null) continue;
                        float[] hsv = new float[3];
                        String tagColor = userLocations.get(i).get("tagColor") != null ? userLocations.get(i).get("tagColor").toString(): "#FF0000";

                        Color.colorToHSV(Color.parseColor(tagColor), hsv);
                        BitmapDescriptor markerColor = BitmapDescriptorFactory.defaultMarker(hsv[0]);
                        CustomMarker customMarker = new CustomMarker(
                                new LatLng(position.getLatitude(), position.getLongitude()),
                                userLocations.get(i).get("tagName") != null ? userLocations.get(i).get("tagName").toString(): "No name",
                                userLocations.get(i).get("tagNotes") + " is here"
                        );
                        markers.add(customMarker);
                        mMap.addMarker(new MarkerOptions().position(customMarker.getPosition()).title(customMarker.getTitle()).icon(markerColor));
                    }

                    //TODO: Move camera to show all markers
                if (!markers.isEmpty()) {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (CustomMarker mark : markers) {
                        builder.include(mark.getPosition());
                    }
                    LatLngBounds bounds = builder.build();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
                }
                }
        ); // todo fix name
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
