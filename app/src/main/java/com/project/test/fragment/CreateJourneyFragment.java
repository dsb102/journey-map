package com.project.test.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.project.test.R;
import com.project.test.adapter.TagAdapter;
import com.project.test.model.Journey;
import com.project.test.model.Tag;
import com.project.test.repository.JourneyRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateJourneyFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "CreateJourneyFragment";
    private TextView journeyNameTextView;
    private SearchView searchView;
    private RecyclerView locationsRecyclerView;
    private Button saveButton;
    private TagAdapter locationAdapter;
    private GoogleMap googleMap; // Declare GoogleMap variable
    private List<Tag> tags = new ArrayList<>();
    private JourneyRepository journeyRepo = new JourneyRepository();
    private String journeyName;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_journey, container, false);

        initializeFragment(view);

        // Khởi tạo và thêm SearchFragment
        SearchFragment searchFragment = new SearchFragment();
        searchFragment.setTagSelectedListener(tag -> {
            tags.add(tag);
            updateMapMarkers();
            updateRecyclerView();
            Log.d(TAG, "Tag added from SearchFragment: " + tag.getTagName());
        });

        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.autocomplete_fragment, searchFragment);
        fragmentTransaction.commit();

        // Set up the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    private void initializeFragment(View view) {
        journeyNameTextView = view.findViewById(R.id.journeyName);
        locationsRecyclerView = view.findViewById(R.id.locations_recycler_view);
        saveButton = view.findViewById(R.id.save_locations_button);

        // Set up RecyclerView
        locationAdapter = new TagAdapter(tags, this::onLocationClick);
        locationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        locationsRecyclerView.setAdapter(locationAdapter);

        // Set up save button listener
        saveButton.setOnClickListener(v -> saveJourney());

        // Add swipe functionality to delete tag
        addSwipeToDelete(locationsRecyclerView);

        // Show journey name dialog when fragment is created
        showJourneyNameDialog();
    }

    private void addSwipeToDelete(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // No move action needed
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Log.d("CreateJourneyFragment", "Swiped to delete tag, direction=" + direction);
                if (direction == ItemTouchHelper.LEFT) {
                    int position = viewHolder.getAdapterPosition();
                    Tag tagToDelete = tags.get(position);
                    tags.remove(tagToDelete);
                    updateRecyclerView();
                    updateMapMarkers();
                    Toast.makeText(getContext(), "Tag deleted", Toast.LENGTH_SHORT).show();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map; // Initialize the GoogleMap
        LatLng initialPosition = new LatLng(21.0285, 105.8542); // Hà Nội
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 10));
        googleMap.setOnMapClickListener(latLng -> {
                Tag newTag = new Tag(UUID.randomUUID().toString(), "Unnamed Location", latLng.latitude, latLng.longitude, 0);
//                tags.add(newTag);
//                updateMapMarkers();
                onLocationClick(newTag);
                Log.d(TAG, "Location clicked and added: " + latLng.toString());
        });
        // Thêm markers cho tất cả các tags đã thêm vào
        updateMapMarkers();

        Log.d(TAG, "GoogleMap is ready");
    }

    private boolean isPointInMapBounds(LatLng latLng) {
        LatLng southwest = googleMap.getProjection().getVisibleRegion().latLngBounds.southwest;
        LatLng northeast = googleMap.getProjection().getVisibleRegion().latLngBounds.northeast;

        return latLng.latitude >= southwest.latitude && latLng.latitude <= northeast.latitude &&
                latLng.longitude >= southwest.longitude && latLng.longitude <= northeast.longitude;
    }

    private void showJourneyNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter Journey Name");

        final EditText input = new EditText(getContext());
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            journeyName = input.getText().toString();
            journeyNameTextView.setText(journeyName);
            Log.d(TAG, "Journey created with name: " + journeyName);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveJourney() {
        if (tags.isEmpty()) {
            Log.e(TAG, "No tags available to create Journey");
            Toast.makeText(getContext(), "No locations added to the journey", Toast.LENGTH_SHORT).show();
            return;
        }

        String userID = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "unknown_user";
        Log.d("CreateJourneyFragment", "User ID: " + userID);
        String journeyID = UUID.randomUUID().toString();
        Journey journey = new Journey(journeyID, journeyName == null || journeyName.isEmpty() ? journeyID : journeyName, userID, tags);

        journeyRepo.saveJourney(journey, new JourneyRepository.OnJourneySaveListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Journey saved successfully", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed(); // Go back to previous fragment
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error saving journey", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error saving journey: " + e.getMessage());
            }
        });
    }

    private void updateMapMarkers() {
        googleMap.clear(); // Clear all old markers
        Log.d("CreateJourneyFragment", "Updating map markers with tags: " + tags.size());
        for (Tag tag : tags) {
            LatLng position = new LatLng(tag.getLatitude(), tag.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(position).title(tag.getTagName()));
        }

        if (!tags.isEmpty()) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Tag tag : tags) {
                builder.include(new LatLng(tag.getLatitude(), tag.getLongitude()));
            }
            LatLngBounds bounds = builder.build();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        }
    }

    private void onLocationClick(Tag tag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter name for the location");

        final EditText input = new EditText(getContext());
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String tagName = input.getText().toString();
            if (!tagName.isEmpty()) {
                tag.setTagName(tagName);
                tags.add(tag);
                updateRecyclerView();
                updateMapMarkers();
                Log.d(TAG, "Added new tag: " + tagName + " at (" + tag.getLatitude() + ", " + tag.getLongitude() + ")");
                Toast.makeText(getActivity(), "Location added with tag " + tagName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Location name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateRecyclerView() {
        Log.d("CreateJourneyFragment", "Updating RecyclerView with tags: " + tags.size());
        locationAdapter.listTags(tags);
        locationAdapter.notifyDataSetChanged();
    }
}
