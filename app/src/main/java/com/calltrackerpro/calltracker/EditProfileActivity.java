package com.calltrackerpro.calltracker;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.calltrackerpro.calltracker.models.ApiResponse;
import com.calltrackerpro.calltracker.models.User;
import com.calltrackerpro.calltracker.services.ApiService;
import com.calltrackerpro.calltracker.utils.RetrofitClient;
import com.calltrackerpro.calltracker.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfile";
    
    private TokenManager tokenManager;
    private ApiService apiService;
    private User currentUser;
    
    // UI Components
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etPhone;
    private Button btnSaveChanges;
    private Button btnCancel;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        
        // Initialize services
        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService();
        currentUser = tokenManager.getUser();
        
        if (currentUser == null) {
            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initializeViews();
        setupToolbar();
        populateUserData();
        setupClickListeners();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void populateUserData() {
        etFirstName.setText(currentUser.getFirstName() != null ? currentUser.getFirstName() : "");
        etLastName.setText(currentUser.getLastName() != null ? currentUser.getLastName() : "");
        etEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        etPhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        
        // Email should be read-only for security reasons
        etEmail.setEnabled(false);
        etEmail.setAlpha(0.6f);
    }
    
    private void setupClickListeners() {
        btnSaveChanges.setOnClickListener(v -> validateAndSaveProfile());
        btnCancel.setOnClickListener(v -> onBackPressed());
    }
    
    private void validateAndSaveProfile() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        
        // Validation
        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(lastName)) {
            etLastName.setError("Last name is required");
            etLastName.requestFocus();
            return;
        }
        
        if (!TextUtils.isEmpty(phone) && !isValidPhoneNumber(phone)) {
            etPhone.setError("Please enter a valid phone number");
            etPhone.requestFocus();
            return;
        }
        
        // Create updated user object
        User updatedUser = new User();
        updatedUser.setId(currentUser.getId());
        updatedUser.setFirstName(firstName);
        updatedUser.setLastName(lastName);
        updatedUser.setPhone(phone);
        updatedUser.setEmail(currentUser.getEmail()); // Keep original email
        
        // Save to backend
        updateProfileOnServer(updatedUser);
    }
    
    private boolean isValidPhoneNumber(String phone) {
        // Simple phone validation - adjust regex as needed
        return phone.matches("^[+]?[0-9]{10,15}$");
    }
    
    private void updateProfileOnServer(User updatedUser) {
        showLoading(true);
        
        String authHeader = tokenManager.getAuthHeader();
        if (authHeader == null) {
            showLoading(false);
            Toast.makeText(this, "Authentication error. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create update request
        ApiService.UpdateProfileRequest request = new ApiService.UpdateProfileRequest(
            updatedUser.getFirstName(),
            updatedUser.getLastName(),
            updatedUser.getPhone()
        );
        
        Call<ApiResponse<User>> call = apiService.updateProfile(authHeader, currentUser.getId(), request);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        handleUpdateSuccess(apiResponse.getData());
                    } else {
                        handleUpdateError(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Update failed");
                    }
                } else {
                    handleUpdateError("Server error. Please try again.");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                showLoading(false);
                handleUpdateError("Network error. Please check your connection.");
                Log.e(TAG, "Profile update failed: " + t.getMessage());
            }
        });
    }
    
    private void handleUpdateSuccess(User updatedUser) {
        // Update local user data
        currentUser.setFirstName(updatedUser.getFirstName());
        currentUser.setLastName(updatedUser.getLastName());
        currentUser.setPhone(updatedUser.getPhone());
        
        // Save updated user to TokenManager
        tokenManager.updateUser(currentUser);
        
        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Profile updated successfully for user: " + currentUser.getEmail());
        
        // Return to previous screen
        setResult(RESULT_OK);
        finish();
    }
    
    private void handleUpdateError(String errorMessage) {
        Toast.makeText(this, "Update failed: " + errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Profile update error: " + errorMessage);
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSaveChanges.setEnabled(!show);
        btnCancel.setEnabled(!show);
        
        // Disable input fields during loading
        etFirstName.setEnabled(!show);
        etLastName.setEnabled(!show);
        etPhone.setEnabled(!show);
    }
    
    @Override
    public void onBackPressed() {
        // Check if user made changes
        if (hasUnsavedChanges()) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Unsaved Changes")
                    .setMessage("You have unsaved changes. Are you sure you want to go back?")
                    .setPositiveButton("Discard", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
    
    private boolean hasUnsavedChanges() {
        String currentFirstName = etFirstName.getText().toString().trim();
        String currentLastName = etLastName.getText().toString().trim();
        String currentPhone = etPhone.getText().toString().trim();
        
        String originalFirstName = currentUser.getFirstName() != null ? currentUser.getFirstName() : "";
        String originalLastName = currentUser.getLastName() != null ? currentUser.getLastName() : "";
        String originalPhone = currentUser.getPhone() != null ? currentUser.getPhone() : "";
        
        return !currentFirstName.equals(originalFirstName) ||
               !currentLastName.equals(originalLastName) ||
               !currentPhone.equals(originalPhone);
    }
}