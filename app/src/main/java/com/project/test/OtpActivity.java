package com.project.test;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpActivity extends AppCompatActivity {
    private String phoneNumber;
    private String verificationId;
    private EditText otpBox1, otpBox2, otpBox3, otpBox4, otpBox5, otpBox6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        otpBox1 = findViewById(R.id.otp_box_1);
        otpBox2 = findViewById(R.id.otp_box_2);
        otpBox3 = findViewById(R.id.otp_box_3);
        otpBox4 = findViewById(R.id.otp_box_4);
        otpBox5 = findViewById(R.id.otp_box_5);
        otpBox6 = findViewById(R.id.otp_box_6);


        // Retrieve phone number and verification ID from the intent
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        verificationId = getIntent().getStringExtra("verificationId");
        Log.e("LONQUE",phoneNumber);

        setOtpBoxWatcher(otpBox1, otpBox2);
        setOtpBoxWatcher(otpBox2, otpBox3);
        setOtpBoxWatcher(otpBox3, otpBox4);
        setOtpBoxWatcher(otpBox4, otpBox5);
        setOtpBoxWatcher(otpBox5, otpBox6);

        Button buttonVerify = findViewById(R.id.buttonVerifyOtp);
        buttonVerify.setOnClickListener(v -> verifyOtp());
    }
    private void setOtpBoxWatcher(EditText currentBox, EditText nextBox) {
        currentBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Auto-move to next box if one digit is entered
                if (s.length() == 1) {
                    nextBox.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        currentBox.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && currentBox.getText().toString().isEmpty()) {
                // Move focus back to the previous box if current box is empty and backspace is pressed
                if (nextBox != null) {
                    currentBox.clearFocus();
                    if (currentBox == otpBox2) {
                        otpBox1.requestFocus();
                    } else if (currentBox == otpBox3) {
                        otpBox2.requestFocus();
                    } else if (currentBox == otpBox4) {
                        otpBox3.requestFocus();
                    } else if (currentBox == otpBox5) {
                        otpBox4.requestFocus();
                    } else if (currentBox == otpBox6) {
                        otpBox5.requestFocus();
                    }
                }
                return true; // Indicate the key event was handled
            }
            return false;
        });
    }

    private void verifyOtp() {
        String otp = otpBox1.getText().toString() + otpBox2.getText().toString() + otpBox3.getText().toString() + otpBox4.getText().toString() + otpBox5.getText().toString() + otpBox6.getText().toString();


        // Create credential using verification ID and OTP
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        // Your sign-in logic, after successful login
        // Pass the phone number to SuccessActivity
        Intent intent = new Intent(OtpActivity.this, MapsActivity.class);
        intent.putExtra("username", phoneNumber); // Pass phone number to SuccessActivity
        startActivity(intent);
        finish(); // Close OtpActivity
    }
}