package com.calltrackerpro.calltracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.calltrackerpro.calltracker.ui.login.LoginActivity;

public class CreateAccountActivity extends AppCompatActivity {

    // Simplified - using only the fields that exist in your current layout
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private TextView tvPasswordStrength;
    private CheckBox cbTerms;
    private Button btnCreateAccount;
    private ProgressBar progressBar;
    private TextView tvBackToLogin;

    private CreateAccountViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        initViews();
        setupViewModel();
        setupListeners();
        observeViewModel();
    }

    private void initViews() {
        // Only initialize the fields that exist in your current layout
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvPasswordStrength = findViewById(R.id.tvPasswordStrength);
        cbTerms = findViewById(R.id.cbTerms);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        progressBar = findViewById(R.id.progressBar);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(CreateAccountViewModel.class);
    }

    private void setupListeners() {
        // Email validation
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                validateEmail();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Password strength checker
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                checkPasswordStrength(s.toString());
                validatePasswordMatch();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Confirm password validation
        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                validatePasswordMatch();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Create account button
        btnCreateAccount.setOnClickListener(v -> {
            if (validateForm()) {
                createAccount();
            }
        });

        // Back to login
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, this::showLoading);

        viewModel.getRegistrationResult().observe(this, result -> {
            if (result instanceof CreateAccountViewModel.RegistrationResult.Success) {
                CreateAccountViewModel.RegistrationResult.Success success =
                        (CreateAccountViewModel.RegistrationResult.Success) result;
                showSuccess("Account created successfully!");
                navigateToLogin();
            } else if (result instanceof CreateAccountViewModel.RegistrationResult.Error) {
                CreateAccountViewModel.RegistrationResult.Error error =
                        (CreateAccountViewModel.RegistrationResult.Error) result;
                showError(error.getMessage());
            }
        });
    }

    private boolean validateEmail() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            return false;
        } else {
            etEmail.setError(null);
            return true;
        }
    }

    private void checkPasswordStrength(String password) {
        if (password.isEmpty()) {
            tvPasswordStrength.setVisibility(View.GONE);
            return;
        }

        tvPasswordStrength.setVisibility(View.VISIBLE);

        String strength;
        int colorRes;

        if (password.length() < 6) {
            strength = "Weak";
            colorRes = android.R.color.holo_red_light;
        } else if (password.length() < 8 || !password.matches(".*[A-Z].*")) {
            strength = "Medium";
            colorRes = android.R.color.holo_orange_light;
        } else if (password.matches(".*[A-Z].*") &&
                password.matches(".*[0-9].*") &&
                password.matches(".*[!@#$%^&*()].*")) {
            strength = "Strong";
            colorRes = android.R.color.holo_green_light;
        } else {
            strength = "Good";
            colorRes = android.R.color.holo_orange_light;
        }

        tvPasswordStrength.setText("Password strength: " + strength);
        tvPasswordStrength.setTextColor(ContextCompat.getColor(this, colorRes));
    }

    private boolean validatePasswordMatch() {
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError(null);
            return true; // Don't show error until user starts typing
        } else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords don't match");
            return false;
        } else {
            etConfirmPassword.setError(null);
            return true;
        }
    }

    private boolean validateForm() {
        boolean isEmailValid = validateEmail();
        boolean isPasswordValid = etPassword.getText().toString().length() >= 6;
        boolean isPasswordMatch = validatePasswordMatch();
        boolean isTermsAccepted = cbTerms.isChecked();

        if (!isPasswordValid) {
            etPassword.setError("Password must be at least 6 characters");
        }

        if (!isTermsAccepted) {
            showError("Please accept the Terms & Conditions");
            return false;
        }

        return isEmailValid && isPasswordValid && isPasswordMatch && isTermsAccepted;
    }

    private void createAccount() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // For now, use placeholder values for the missing fields
        String firstName = email.split("@")[0]; // Use part of email as first name
        String lastName = "User"; // Default last name
        String phone = "1234567890"; // Default phone
        String organizationName = "My Company"; // Default organization

        viewModel.createAccount(firstName, lastName, email, phone, organizationName, password);
    }

    private void showLoading(Boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnCreateAccount.setEnabled(!isLoading);
        btnCreateAccount.setText(isLoading ? "CREATING ACCOUNT..." : "CREATE ACCOUNT");
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("email", etEmail.getText().toString()); // Pre-fill email in login
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}