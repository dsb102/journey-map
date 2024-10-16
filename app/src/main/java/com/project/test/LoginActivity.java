package com.project.test;


import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText mobileNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mobileNumber = findViewById(R.id.login_mobile_number);

        findViewById(R.id.button_submit_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobile = mobileNumber.getText().toString();
                // Handle login logic here
                if (mobile.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter your mobile number", Toast.LENGTH_SHORT).show();
                } else {
                    // Process login
                    Toast.makeText(LoginActivity.this, "Logged in with mobile: " + mobile, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}