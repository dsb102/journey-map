package com.project.test;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MessageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message); // Set the layout for this activity

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_menu);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        bottomNav.setSelectedItemId(R.id.bottom_message);
    }
    // Navigation item selected listener function
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                    switch (item.getItemId()) {
                        case R.id.bottom_message:
                            return true;
                        case R.id.bottom_home:
                            startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish(); // Finish current activity
                            return true;
                        case R.id.bottom_imagesearch:
                            startActivity(new Intent(getApplicationContext(), ImageSearchActivity.class));
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish(); // Finish current activity
                            return true;
                        case R.id.bottom_journey:
                            startActivity(new Intent(getApplicationContext(), JourneyManagementActivity.class));
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish(); // Finish current activity
                            return true;
                        default:
                            return false;
                    }
                }
            };
}
