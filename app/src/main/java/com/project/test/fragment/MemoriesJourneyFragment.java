package com.project.test.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import android.graphics.Bitmap;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.project.test.R;
import com.project.test.SignUpActivity;
import com.project.test.adapter.TagMemoryAdapter;
import com.project.test.entity.CustomMarker;
import com.project.test.model.Journey;
import com.project.test.model.Memory;
import com.project.test.model.Tag;
import com.project.test.model.TagMemory;
import com.project.test.repository.JourneyRepository;
import com.project.test.repository.MemoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MemoriesJourneyFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MemoriesJourneyFragment";
    private RecyclerView locationsRecyclerView;
    private Spinner journeySpinner;
    private ArrayAdapter<String> spinnerAdapter;
    private TagMemoryAdapter tagAdapter;
    private List<TagMemory> tags = new ArrayList<>();
    private GoogleMap googleMap;
    private String memoryId;
    private JourneyRepository journeyRepo = new JourneyRepository();
    private MemoryRepository memoryRepo = new MemoryRepository();

    private int positionUploadImage = -1;

    private static final int PICK_IMAGE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memories_journey, container, false);

        // Lấy journeyID từ arguments
        if (getArguments() != null) {
            memoryId = getArguments().getString("journeyID");
        }

        // Khởi tạo danh sách tag và adapter
        tagAdapter = new TagMemoryAdapter();
        tagAdapter.setTagMems(tags,
            tag -> {
//                    onTagClick(tag);
            },
            position -> {
                openImageChooser();
                positionUploadImage = position;
            },
                (check, position) -> {
                tags.get(position).setCheck(check);
                tagAdapter.setTagMems(tags);
                updateTagsOrderInFirestore();
                updateMapMarkers();
            },
            getContext()
        );

        // Thiết lập RecyclerView
        journeySpinner = view.findViewById(R.id.journeySpinner);
        locationsRecyclerView = view.findViewById(R.id.locations_recycler_view);
        locationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        locationsRecyclerView.setAdapter(tagAdapter);

        // Khởi tạo Firebase User
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "unknown_user";

        memoryRepo.fetchMemories(userId, new MemoryRepository.OnMemoryFetchListener() {
            @Override
            public void onSuccess(List<Memory> memories) {
                Log.d("EditJourneyFragment.fetchMemories", "memories: " + new Gson().toJson(memories));
                updateSpinner(memories);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error fetching memories: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        if (memoryId != null && !memoryId.isEmpty()) {
            memoryRepo.getTagForMemory(memoryId, new MemoryRepository.OnTagMemFetchListener() {
                @Override
                public void onSuccess(List<TagMemory> tags) {
                    updateRecyclerView(tags);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Error fetching tags: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Thiết lập map
        setUpMapFragment();

        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        LatLng initialPosition = new LatLng(21.0285, 105.8542); // Hà Nội
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 10));
        updateMapMarkers();
    }

    private void setUpMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this::onMapReady);
        }
    }

    private void updateSpinner(List<Memory> memories) {
        List<String> memoryNames = new ArrayList<>();
        for (Memory memory : memories) {
            memoryNames.add(memory.getMemoryName() != null ? memory.getMemoryName() : "Unnamed Memory");
        }

        spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, memoryNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        journeySpinner.setAdapter(spinnerAdapter);

        journeySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Memory selectedMemory = memories.get(position);
                memoryId = selectedMemory.getMemoryId();
                fetchTagsForJourney(selectedMemory.getMemoryId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không có hành động nào
            }
        });
    }

    private void updateRecyclerView(List<TagMemory> updatedTags) {
        Log.d("EditJourneyFragment.updateRecyclerView", "updatedTags: " + new Gson().toJson(updatedTags));
        tags.clear();
        tags.addAll(updatedTags);
        tagAdapter.setTagMems(tags);
        updateMapMarkers();
    }

    private void fetchTagsForJourney(String memoryId) {
        memoryRepo.getTagForMemory(memoryId, new MemoryRepository.OnTagMemFetchListener() {
            @Override
            public void onSuccess(List<TagMemory> tags) {
                Log.d("EditJourneyFragment.fetchTagsForJourney", "journeyId = " + memoryId + ", tags: " + new Gson().toJson(tags));
                updateRecyclerView(tags);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error fetching tags: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTagsOrderInFirestore() {
        // Lấy danh sách tag đã cập nhật và lưu vào Firestore
        List<TagMemory> updatedTags = tagAdapter.getTagMems();
        memoryRepo.updateTagsOrder(memoryId, updatedTags);
    }

    private void updateMapMarkers() {
        Log.d("EditJourneyFragment.updateMapMarkers", "tags: " + new Gson().toJson(tags));

        if (googleMap != null) {
            googleMap.clear(); // Xóa tất cả markers cũ trước khi thêm mới

            LatLng previousPosition = null;  // Giữ vị trí của điểm trước đó

            for (int i = 0; i < tags.size(); i++) {
                TagMemory tag = tags.get(i);
                LatLng position = new LatLng(tag.getLatitude(), tag.getLongitude());

                // Thêm marker với hình ảnh
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .title(tag.getTagName());

                // Nếu tag có hình ảnh, sử dụng Glide để tải hình ảnh và gán cho marker
                if (tag.getImage() != null && !tag.getImage().isEmpty()) {
                    Glide.with(this)
                            .asBitmap()
                            .load(tag.getImage())
                            .override(100, 100) // Thay đổi kích thước nếu cần
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resource));
                                    googleMap.addMarker(markerOptions);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                    googleMap.addMarker(markerOptions); // Nếu không tải được ảnh
                                }
                            });
                } else {
                    googleMap.addMarker(markerOptions); // Nếu không có hình ảnh, chỉ thêm marker
                }

                // Nếu đã có điểm trước đó, vẽ polyline từ previousPosition đến position
                if (previousPosition != null) {
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .add(previousPosition)  // Điểm đầu
                            .add(position)          // Điểm cuối

                            // Kiểm tra điều kiện `isCheck` của điểm hiện tại để quyết định màu sắc
                            .color(tag.isCheck() ? Color.BLUE : Color.BLACK)
                            .width(tag.isCheck() ? 10 : 5); // Độ dày: true là 10, false là 5

                    googleMap.addPolyline(polylineOptions); // Thêm polyline vào bản đồ
                }

                // Cập nhật previousPosition cho lần lặp tiếp theo
                previousPosition = position;
            }

            // Zoom bản đồ theo các điểm nối lại
            if (!tags.isEmpty()) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (TagMemory tag : tags) {
                    builder.include(new LatLng(tag.getLatitude(), tag.getLongitude()));
                }
                LatLngBounds bounds = builder.build();
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            }
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
    }

    private Uri selectedImageUri;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            // Display the image (optional)
            if (positionUploadImage != -1) {
                uploadImageToFirebase();
            }
        }
    }

    private void uploadImageToFirebase() {
        if (selectedImageUri != null) {
            // Create a reference to the storage
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("uploads/" + System.currentTimeMillis() + ".jpg");

            storageReference.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            // Now call registerUser with the download URL
                            tags.get(positionUploadImage).setImage(downloadUrl);
                            tagAdapter.setTagMems(tags);
                            updateTagsOrderInFirestore();
                            updateMapMarkers();
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
