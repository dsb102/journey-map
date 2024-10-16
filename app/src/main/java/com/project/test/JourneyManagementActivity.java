package com.project.test;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.test.adapter.CustomInfoWindowAdapter;
import com.project.test.entity.CustomMarker;
import com.project.test.fragment.JourneyButtonFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JourneyManagementActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private PlacesClient placesClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journeys); // Ensure this layout is correctly set up

        // Initialize Google Places API
        Places.initialize(getApplicationContext(), "AIzaSyBZvoWYwyeOmhAjoX5xuXcas6RIL79dbJQ");

        // Initialize SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this); // Wait for the map to be ready
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_menu);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // Initialize JourneyButtonFragment
        if (savedInstanceState == null) {
            displayFragment(new JourneyButtonFragment());
        }
        // Set up listener for BottomNavigationView
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.bottom_home:
                            startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish();
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

    // Method to display the fragment
    private void displayFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainerView, fragment);
        transaction.commit();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        // Set the custom info window adapter
        googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(getLayoutInflater(), new ArrayList<>()));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        // Example of adding a marker for demonstration
        LatLng hanoi = new LatLng(21.0285, 105.8542);
        CustomMarker customMarker = new CustomMarker(hanoi, "Marker in Hanoi", "Some info about Hanoi");
        googleMap.addMarker(new MarkerOptions().position(hanoi).title(customMarker.getTitle()));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hanoi, 10));

        // Call the function to find current places
        findCurrentPlaces();

    }
    public GoogleMap getGoogleMap() {
        return googleMap; // Getter for the GoogleMap instance
    }
    private void findCurrentPlaces() {
        // Define the place fields to return
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.LOCATION);

        // Create a request for current place
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        // Call the Places API
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<FindCurrentPlaceResponse> task = placesClient.findCurrentPlace(request);
        task.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (PlaceLikelihood placeLikelihood : task.getResult().getPlaceLikelihoods()) {
                        // Add markers for each place
                        LatLng latLng = placeLikelihood.getPlace().getLatLng();
                        if (latLng != null) {
                            googleMap.addMarker(new MarkerOptions().position(latLng).title(placeLikelihood.getPlace().getName()));
                        }
                    }
                }
            }
        });
    }
}
