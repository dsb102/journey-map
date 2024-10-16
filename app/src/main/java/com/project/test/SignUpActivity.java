package com.project.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.provider.MediaStore;

public class SignUpActivity extends AppCompatActivity {

    private EditText editTextName, editTextId, editTextPhoneNumber, editTextDateOfBirth;
    private ImageView imageViewAvatar;
    private Uri selectedImageUri;
    private FirebaseFirestore db;

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        editTextName = findViewById(R.id.editTextName);
        editTextId = findViewById(R.id.editTextId);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextDateOfBirth = findViewById(R.id.editTextDateOfBirth);
        imageViewAvatar = findViewById(R.id.imageViewAvatar);

        Button buttonUploadImage = findViewById(R.id.buttonUploadImage);
        Button buttonClearImage = findViewById(R.id.buttonClearImage);
        Button buttonRegister = findViewById(R.id.buttonRegister);
        Button buttonBack = findViewById(R.id.buttonBack);

        // Image upload
        buttonUploadImage.setOnClickListener(v -> openImageChooser());
        buttonClearImage.setOnClickListener(v -> clearImage());

        // Register Button functionality
        buttonRegister.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadImageToFirebase(); // Upload image first before registering the user
            } else {
                Toast.makeText(SignUpActivity.this, "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        });

        // Back Button functionality
        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    // Open Image Chooser to select Avatar
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            // Display the image (optional)
            imageViewAvatar.setImageURI(selectedImageUri);
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
                            registerUser(downloadUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        Toast.makeText(SignUpActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUser(String downloadUrl) {
        String name = editTextName.getText().toString();
        String id = editTextId.getText().toString();
        String phoneNumber = editTextPhoneNumber.getText().toString();
        String dateOfBirth = editTextDateOfBirth.getText().toString();
        ArrayList<String> ppp = new ArrayList<>();
        ppp.add("NguyenVanA");
        ppp.add("NguyenVanB");
        ppp.add("dungsobin102");

        // Validation
        if (name.isEmpty() || id.isEmpty() || phoneNumber.isEmpty() || dateOfBirth.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user data to store in Firestore
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("id", id);
        userData.put("phoneNumber", phoneNumber);
        userData.put("dateOfBirth", dateOfBirth);
        userData.put("avatarUri", downloadUrl); // Store image URI directly here
        userData.put("friends",ppp);

        // Save user data to Firestore
        db.collection("users")
                .document(id)  // Using the User ID as the document ID
                .set(userData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(SignUpActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignUpActivity.this, "Error registering user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearImage() {
        imageViewAvatar.setImageResource(android.R.color.transparent);
        selectedImageUri = null;
    }
}