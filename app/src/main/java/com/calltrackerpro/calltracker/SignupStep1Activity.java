package com.calltrackerpro.calltracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.calltrackerpro.calltracker.ui.login.LoginActivity;

public class SignupStep1Activity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonNext;
    private TextView textViewSignIn;
    private SignupData signupData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_step1);

        // Initialize signup data model
        signupData = new SignupData();

        // Check if returning from step 2 with existing data
        if (getIntent().hasExtra("signup_data")) {
            signupData = getIntent().getParcelableExtra("signup_data");
        }

        initializeViews();
        setupClickListeners();
        populateExistingData();
    }

    private void initializeViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonNext = findViewById(R.id.buttonNext);
        textViewSignIn = findViewById(R.id.textViewSignIn);
    }

    private void populateExistingData() {
        // If user is coming back from step 2, populate the fields
        if (signupData.getEmail() != null) {
            editTextEmail.setText(signupData.getEmail());
        }
        if (signupData.getPassword() != null) {
            editTextPassword.setText(signupData.getPassword());
            editTextConfirmPassword.setText(signupData.getPassword());
        }
    }

    private void setupClickListeners() {
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndProceed();
            }
        });

        textViewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupStep1Activity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void validateAndProceed() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Basic validation
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email address");
            editTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            editTextPassword.requestFocus();
            return;
        }

        // Check for common weak passwords
        if (isWeakPassword(password)) {
            editTextPassword.setError("Password is too weak. Avoid common passwords like '123456' or 'password'");
            editTextPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            editTextConfirmPassword.setError("Please confirm your password");
            editTextConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            editTextConfirmPassword.requestFocus();
            return;
        }

        // Update signup data
        signupData.setEmail(email);
        signupData.setPassword(password);

        // Show success message
        Toast.makeText(this, "Step 1 completed! Proceeding to personal details...", Toast.LENGTH_SHORT).show();

        // Proceed to step 2 with the data
        Intent intent = new Intent(SignupStep1Activity.this, SignupStep2Activity.class);
        intent.putExtra("signup_data", signupData);
        startActivity(intent);
        finish();
    }

    private boolean isWeakPassword(String password) {
        String[] weakPasswords = {
                "123456", "password", "123456789", "12345678", "12345",
                "1234567", "admin", "qwerty", "abc123", "password123"
        };

        String lowerPassword = password.toLowerCase();
        for (String weak : weakPasswords) {
            if (lowerPassword.equals(weak)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SignupStep1Activity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}