package com.project.test;

import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;

import java.util.Arrays;
import java.util.List;

public class AddImageSearchActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String detectedClass;
    private static final String TAG = "MapsActivity";
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addimage);

        // Retrieve the detected class from the intent
        detectedClass = getIntent().getStringExtra("DETECTED_CLASS");

        // Initialize the Places API
        Places.initialize(getApplicationContext(), "AIzaSyBZvoWYwyeOmhAjoX5xuXcas6RIL79dbJQ");
        placesClient = Places.createClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Search for places related to the detected class
        searchForPlaces(detectedClass);
    }

    // Method to search for places related to the detected class using the Places API
    private void searchForPlaces(String query) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query) // Use the detected class as the query
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            for (int i = 0; i < response.getAutocompletePredictions().size(); i++) {
                // Get the place ID of the first few predictions
                String placeId = response.getAutocompletePredictions().get(i).getPlaceId();

                // Fetch detailed information about the place
                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();

                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(fetchPlaceResponse -> {
                    Place place = fetchPlaceResponse.getPlace();
                    Log.i(TAG, "Place found: " + place.getName());

                    // Add marker for each place found
                    LatLng placeLatLng = place.getLatLng();
                    if (placeLatLng != null) {
                        mMap.addMarker(new MarkerOptions().position(placeLatLng).title(place.getName()));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, 12)); // Adjust zoom level as needed
                    }
                }).addOnFailureListener(e -> Log.e(TAG, "Place not found: " + e.getMessage()));
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Autocomplete predictions failed: " + e.getMessage()));
    }
}