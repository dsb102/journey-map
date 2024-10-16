package com.project.test;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;



    private ImageView imageView;
    private EditText mobileNumberEditText;
    private Button loginButton, signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Ensure this matches your XML layout file name

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        imageView = findViewById(R.id.imageView);
        mobileNumberEditText = findViewById(R.id.login_mobile_number);
        loginButton = findViewById(R.id.button_login);
        signupButton = findViewById(R.id.button_signup);

        // Handle window insets (for edge-to-edge layout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.button_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String phoneNumber = mobileNumberEditText.getText().toString();
//                sendOtp(phoneNumber);

                autoByPassOTP();
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to SignupActivity
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });


    }

    private void autoByPassOTP() {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    private void sendOtp(String phoneNumber) {
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-in success, retrieve the username
                        String phoneNumber = mobileNumberEditText.getText().toString();
                        fetchUsername(phoneNumber);
                    } else {
                        // Sign-in failed
                        Toast.makeText(MainActivity.this, "Sign-in failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    String usname;

    private void fetchUsername(String phoneNumber) {
        Log.e("Startto", "User ID: "); // Log user ID
        db.collection("users")
                .whereEqualTo("phoneNumber", phoneNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Failed to fetch user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (task.getResult().isEmpty()) {
                        Toast.makeText(MainActivity.this, "No user found with this phone number.", Toast.LENGTH_SHORT).show();
                    } else {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String username = document.getString("id");
                            String userId = document.getId();
                            Toast.makeText(MainActivity.this, "Login successful! Welcome, " + username, Toast.LENGTH_LONG).show();
                            Log.e("LoginActivity", "User ID: " + userId);
                            usname = userId;
                            return; // Trả về ngay khi tìm thấy người dùng
                        }
                    }
                });
    }

//    private void fetchUsername(String phoneNumber) {
//        Log.e("Startto", "User ID: " ); // Log the user ID
//        db.collection("users")
//            .get() // Get all documents in the users collection
//            .addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    boolean userFound = false; // Flag to check if user was found
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        // Check if the phone number matches
//                        String docPhoneNumber = document.getString("phoneNumber"); // Adjust the field name accordingly
//                        if (docPhoneNumber != null && docPhoneNumber.equals(phoneNumber)) {
//                            String username = document.getString("id");
//                            String userId = document.getId(); // Get the document ID
//                            // Display success message
//                            Toast.makeText(MainActivity.this, "Login successful! Welcome, " + username, Toast.LENGTH_LONG).show();
//                            Log.e("LoginActivity", "User ID: " + userId); // Log the user ID
//                            usname = userId;
//                            userFound = true; // Set the flag to true
//                            // Optionally, navigate to the next activity here
//                            break; // Exit the loop since user is found
//                        }
//                    }
//                    if (!userFound) {
//                        Toast.makeText(MainActivity.this, "No user found with this phone number.", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(MainActivity.this, "Failed to fetch user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//    }
}
