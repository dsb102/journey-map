package com.project.test.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.project.test.R;
import com.project.test.adapter.CustomInfoWindowAdapter;
import com.project.test.adapter.TagAdapter;
import com.project.test.entity.CustomMarker;
import com.project.test.model.Journey;
import com.project.test.model.Memory;
import com.project.test.model.Tag;
import com.project.test.repository.JourneyRepository;
import com.project.test.databinding.FragmentEditJourneyBinding;
import com.project.test.repository.MemoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditJourneyFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "EditJourneyFragment";
    private RecyclerView locationsRecyclerView;
    private Spinner journeySpinner;
    private ArrayAdapter<String> spinnerAdapter;
    private TagAdapter tagAdapter;
    private List<Tag> tags = new ArrayList<>();
    private GoogleMap googleMap;
    private String journeyID;
    private JourneyRepository journeyRepo = new JourneyRepository();
    private Button saveMemory;
    private Journey currentJourney;
    private MemoryRepository memRepo = new MemoryRepository();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_journey, container, false);

        saveMemory = view.findViewById(R.id.buttonSave);
        // Lấy journeyID từ arguments
        if (getArguments() != null) {
            journeyID = getArguments().getString("journeyID");
        }

        saveMemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentJourney.setTags(tags);
                Memory memory = Memory.journeyToMemory(currentJourney);
                memRepo.saveMemory(memory, new MemoryRepository.OnMemorySaveListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Memory saved successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Error saving memory: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        // Khởi tạo danh sách tag và adapter
        tagAdapter = new TagAdapter(tags, this::onTagClick);

        // Thiết lập RecyclerView
        journeySpinner = view.findViewById(R.id.journeySpinner);
        locationsRecyclerView = view.findViewById(R.id.locations_recycler_view);
        locationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        locationsRecyclerView.setAdapter(tagAdapter);

        // Khởi tạo Firebase User
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "unknown_user";

        journeyRepo.fetchJourneys(userId, new JourneyRepository.OnJourneyFetchListener() {
            @Override
            public void onSuccess(List<Journey> journeyList) {
                Log.d("EditJourneyFragment.fetchJourneys", "journeyList: " + journeyList);
                updateSpinner(journeyList);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error fetching journeys: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        if (journeyID != null && !journeyID.isEmpty()) {
            journeyRepo.getTagForJourney(journeyID, new JourneyRepository.OnTagFetchListener() {
                @Override
                public void onSuccess(List<Tag> tags) {
                    updateRecyclerView(tags);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Error fetching tags: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Thêm swipe để xóa tag
        addSwipeToDelete(locationsRecyclerView);

        // Thêm kéo thả để thay đổi thứ tự tag
        addDragAndDrop(locationsRecyclerView);

        // Thiết lập map
        setUpMapFragment();

        // Khởi tạo và thêm SearchFragment
        setUpSearchFragment();

        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        LatLng initialPosition = new LatLng(21.0285, 105.8542); // Hà Nội
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 10));
        updateMapMarkers();
        googleMap.setOnMapClickListener(this::onMapClick);
    }

    private void setUpMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this::onMapReady);
        }
    }

    private void onMapClick(LatLng latLng) {
        if (isPointInMapBounds(latLng)) {
            Tag newTag = new Tag(UUID.randomUUID().toString(), "Unnamed Location", latLng.latitude, latLng.longitude, 0);
            onTagClick(newTag);
        }
    }

    private void updateSpinner(List<Journey> journeys) {
        List<String> journeyNames = new ArrayList<>();
        for (Journey journey : journeys) {
            journeyNames.add(journey.getJourneyName() != null ? journey.getJourneyName() : "Unnamed Journey");
        }

        spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, journeyNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        journeySpinner.setAdapter(spinnerAdapter);

        journeySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Journey selectedJourney = journeys.get(position);
                currentJourney = selectedJourney;
                journeyID = selectedJourney.getJourneyID();
                fetchTagsForJourney(selectedJourney.getJourneyID());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không có hành động nào
            }
        });
    }

    private void updateRecyclerView(List<Tag> updatedTags) {
        Log.d("EditJourneyFragment.updateRecyclerView", "updatedTags: " + new Gson().toJson(updatedTags));
        tags = updatedTags;
        tagAdapter.setTags(tags);
        tagAdapter.notifyDataSetChanged();
        updateMapMarkers();
    }

    private void fetchTagsForJourney(String journeyId) {
        journeyRepo.getTagForJourney(journeyId, new JourneyRepository.OnTagFetchListener() {
            @Override
            public void onSuccess(List<Tag> tags) {
                Log.d("EditJourneyFragment.fetchTagsForJourney", "journeyId = " + journeyId + ", tags: " + new Gson().toJson(tags));
                updateRecyclerView(tags);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error fetching tags: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onTagClick(Tag tag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter the name for the location");

        final EditText input = new EditText(getContext());
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String tagName = input.getText().toString();
            if (!tagName.isEmpty()) {
                tag.setTagName(tagName);
                tags.add(tag);
                updateMapMarkers();
                tagAdapter.listTags(tags); // Cập nhật danh sách tag
            } else {
                Toast.makeText(getContext(), "The location name can't be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void addDragAndDrop(RecyclerView recyclerView) {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                Log.d(TAG, "onMove: from " + viewHolder.getAdapterPosition() + " to " + target.getAdapterPosition());
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                tagAdapter.moveTag(fromPosition, toPosition);
                updateTagsOrderInFirestore();
                updateMapMarkers();
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Không cần xử lý khi bị vuốt
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void addSwipeToDelete(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    int position = viewHolder.getAdapterPosition();
                    Tag tagToDelete = tagAdapter.getTagAtPosition(position);
                    tags.remove(position);
                    tagAdapter.listTags(tags);
                    journeyRepo.removeTag(journeyID, tagToDelete);
                    updateMapMarkers();
                    Toast.makeText(getContext(), "Tag deleted", Toast.LENGTH_SHORT).show();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void updateTagsOrderInFirestore() {
        // Lấy danh sách tag đã cập nhật và lưu vào Firestore
        List<Tag> updatedTags = tagAdapter.getTags();
        journeyRepo.updateTagsOrder(journeyID, updatedTags);
    }

    private void updateMapMarkers() {
        Log.d("EditJourneyFragment.updateMapMarkers", "tags: " + new Gson().toJson(tags));
        if (googleMap != null) {
            googleMap.clear(); // Xóa tất cả markers cũ trước khi thêm mới

            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(Color.BLUE) // Set the color of the polyline
                    .width(5);

            for (Tag tag : tags) {
                LatLng position = new LatLng(tag.getLatitude(), tag.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(position).title(tag.getTagName()));
                polylineOptions.add(position);
            }
            if (tags.size() > 1) {
                googleMap.addPolyline(polylineOptions);
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
    }

    private boolean isPointInMapBounds(LatLng latLng) {
        LatLng southwest = googleMap.getProjection().getVisibleRegion().latLngBounds.southwest;
        LatLng northeast = googleMap.getProjection().getVisibleRegion().latLngBounds.northeast;

        return latLng.latitude >= southwest.latitude && latLng.latitude <= northeast.latitude
                && latLng.longitude >= southwest.longitude && latLng.longitude <= northeast.longitude;
    }

    private void setUpSearchFragment() {
        SearchFragment searchFragment = new SearchFragment();
        searchFragment.setTagSelectedListener(tag -> {
            tags.add(tag);
            updateRecyclerView(tags);
            updateTagsOrderInFirestore();
            Log.d(TAG, "Tag added from SearchFragment: " + tag.getTagName());
        });
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.autocomplete_fragment, searchFragment);
        transaction.commit();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
