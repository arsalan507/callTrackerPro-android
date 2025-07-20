package com.calltrackerpro.calltracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.calltrackerpro.calltracker.ui.login.LoginActivity;

public class SignupStep2Activity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextPhone, editTextOrganization;
    private CheckBox checkBoxTerms;
    private Button buttonBack, buttonCreateAccount;
    private TextView textViewSignIn;
    private CreateAccountViewModel viewModel;
    private SignupData signupData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_step2);

        // Get signup data from step 1
        signupData = getIntent().getParcelableExtra("signup_data");
        if (signupData == null) {
            // If no data, go back to step 1
            Intent intent = new Intent(SignupStep2Activity.this, SignupStep1Activity.class);
            startActivity(intent);
            finish();
            return;
        }

        initializeViews();
        setupViewModel();
        setupClickListeners();
        populateExistingData();
    }

    private void initializeViews() {
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextOrganization = findViewById(R.id.editTextOrganization);
        checkBoxTerms = findViewById(R.id.checkBoxTerms);
        buttonBack = findViewById(R.id.buttonBack);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);
        textViewSignIn = findViewById(R.id.textViewSignIn);
    }

    private void populateExistingData() {
        // Populate fields if user previously filled them
        if (signupData.getFirstName() != null) {
            editTextFirstName.setText(signupData.getFirstName());
        }
        if (signupData.getLastName() != null) {
            editTextLastName.setText(signupData.getLastName());
        }
        if (signupData.getPhone() != null) {
            editTextPhone.setText(signupData.getPhone());
        }
        if (signupData.getOrganizationName() != null) {
            editTextOrganization.setText(signupData.getOrganizationName());
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(CreateAccountViewModel.class);

        // Observe registration result
        viewModel.getRegistrationResult().observe(this, result -> {
            if (result != null) {
                if (result instanceof CreateAccountViewModel.RegistrationResult.Success) {
                    Toast.makeText(this, "Account created successfully! Please login.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SignupStep2Activity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("email", signupData.getEmail()); // Pre-fill email on login
                    intent.putExtra("source", "signup_success");
                    startActivity(intent);
                    finish();
                } else if (result instanceof CreateAccountViewModel.RegistrationResult.Error) {
                    CreateAccountViewModel.RegistrationResult.Error error =
                            (CreateAccountViewModel.RegistrationResult.Error) result;
                    String errorMessage = error.getMessage();
                    if (errorMessage.toLowerCase().contains("email")) {
                        Toast.makeText(this, "Email already exists. Please use a different email or try logging in.", Toast.LENGTH_LONG).show();
                        // Go back to step 1 to change email
                        goBackToStep1();
                    } else {
                        Toast.makeText(this, "Registration failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            buttonCreateAccount.setEnabled(!isLoading);
            buttonBack.setEnabled(!isLoading);
            buttonCreateAccount.setText(isLoading ? "Creating Account..." : "CREATE ACCOUNT");
        });
    }

    private void setupClickListeners() {
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackToStep1();
            }
        });

        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndCreateAccount();
            }
        });

        textViewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupStep2Activity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void validateAndCreateAccount() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String organizationName = editTextOrganization.getText().toString().trim();

        // Basic validation
        if (TextUtils.isEmpty(firstName)) {
            editTextFirstName.setError("First name is required");
            editTextFirstName.requestFocus();
            return;
        }

        if (firstName.length() < 2) {
            editTextFirstName.setError("First name must be at least 2 characters");
            editTextFirstName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            editTextLastName.setError("Last name is required");
            editTextLastName.requestFocus();
            return;
        }

        if (lastName.length() < 2) {
            editTextLastName.setError("Last name must be at least 2 characters");
            editTextLastName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            editTextPhone.setError("Phone number is required");
            editTextPhone.requestFocus();
            return;
        }

        // Remove all non-digits for validation
        String cleanPhone = phone.replaceAll("[^\\d]", "");
        if (cleanPhone.length() < 10) {
            editTextPhone.setError("Please enter a valid phone number (at least 10 digits)");
            editTextPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(organizationName)) {
            editTextOrganization.setError("Organization name is required");
            editTextOrganization.requestFocus();
            return;
        }

        if (organizationName.length() < 2) {
            editTextOrganization.setError("Organization name must be at least 2 characters");
            editTextOrganization.requestFocus();
            return;
        }

        if (!checkBoxTerms.isChecked()) {
            Toast.makeText(this, "Please accept the Terms & Conditions to continue", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update signup data with step 2 information
        signupData.setFirstName(firstName);
        signupData.setLastName(lastName);
        signupData.setPhone(cleanPhone); // Store clean phone number
        signupData.setOrganizationName(organizationName);

        // Create the registration request
        CreateAccountRequest request = signupData.toCreateAccountRequest();

        // Call the ViewModel to register
        viewModel.registerUser(request);
    }

    private void goBackToStep1() {
        // Save current data before going back
        signupData.setFirstName(editTextFirstName.getText().toString().trim());
        signupData.setLastName(editTextLastName.getText().toString().trim());
        signupData.setPhone(editTextPhone.getText().toString().trim());
        signupData.setOrganizationName(editTextOrganization.getText().toString().trim());

        Intent intent = new Intent(SignupStep2Activity.this, SignupStep1Activity.class);
        intent.putExtra("signup_data", signupData);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        goBackToStep1();
    }
}