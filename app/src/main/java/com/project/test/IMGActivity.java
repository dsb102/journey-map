package com.project.test;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.test.api.RetrofitClient;
import com.project.test.api.RoboflowApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IMGActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private static final String TAG = "IMGActivity";
    private static final String API_KEY = "teFKpRyPb5xXB4AteKDP"; // Replace with your API key

    private TextView tvResult;
    private ProgressBar progressBar;
    private Uri selectedImageUri;
    private Button btnChooseImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        tvResult = findViewById(R.id.tv_result);
        progressBar = findViewById(R.id.progress_bar);
        btnChooseImage = findViewById(R.id.btn_choose_image);

        // Set click listener on the button to open the image chooser
        btnChooseImage.setOnClickListener(v -> openImageChooser());
    }

    // Method to open the image chooser
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (selectedImageUri != null) {
            progressBar.setVisibility(View.VISIBLE); // Show the progress bar
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("uploads/" + System.currentTimeMillis() + ".jpg");

            storageReference.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            getPrediction(downloadUrl); // Pass the download URL to the prediction method
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(IMGActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to call the Roboflow API with the uploaded image URL
    private void getPrediction(String imageUrl) {
        RoboflowApi api = RetrofitClient.getClient().create(RoboflowApi.class);
        Call<com.nothing.firestoreup.model.DetectionResponse> call = api.getPredictions(API_KEY, imageUrl);

        call.enqueue(new Callback<com.nothing.firestoreup.model.DetectionResponse>() {
            @Override
            public void onResponse(Call<com.nothing.firestoreup.model.DetectionResponse> call, Response<com.nothing.firestoreup.model.DetectionResponse> response) {
                progressBar.setVisibility(View.GONE); // Hide the ProgressBar
                if (response.isSuccessful() && response.body() != null) {
                    com.nothing.firestoreup.model.DetectionResponse detectionResponse = response.body();
                    if (detectionResponse.getPredictions() != null && !detectionResponse.getPredictions().isEmpty()) {
                        String className = detectionResponse.getPredictions().get(0).getClassName();
                        Log.d(TAG, "Detected Class: " + className);
                        tvResult.setText("Detected Class: " + className); // Update the TextView

                        // Start MapsActivity and pass the detected class
                        Intent intent = new Intent(IMGActivity.this, MapsActivity.class);
                        intent.putExtra("DETECTED_CLASS", className);  // Pass the detected class
                        startActivity(intent);

                    } else {
                        Log.d(TAG, "No predictions found.");
                        tvResult.setText("No predictions found.");
                    }
                } else {
                    Log.e(TAG, "Response not successful: " + response.code());
                    tvResult.setText("Response not successful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<com.nothing.firestoreup.model.DetectionResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error: " + t.getMessage());
                Toast.makeText(IMGActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}