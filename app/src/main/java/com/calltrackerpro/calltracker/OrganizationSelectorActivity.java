package com.calltrackerpro.calltracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.calltrackerpro.calltracker.adapters.OrganizationAdapter;
import com.calltrackerpro.calltracker.models.ApiResponse;
import com.calltrackerpro.calltracker.models.Organization;
import com.calltrackerpro.calltracker.models.User;
import com.calltrackerpro.calltracker.services.ApiService;
import com.calltrackerpro.calltracker.utils.RetrofitClient;
import com.calltrackerpro.calltracker.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class OrganizationSelectorActivity extends AppCompatActivity implements OrganizationAdapter.OnOrganizationClickListener {
    private static final String TAG = "OrganizationSelector";
    
    private TokenManager tokenManager;
    private ApiService apiService;
    private User currentUser;
    
    private RecyclerView organizationsRecyclerView;
    private OrganizationAdapter organizationAdapter;
    private ProgressBar loadingProgressBar;
    private Button logoutButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_selector);
        
        // Initialize services
        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService();
        currentUser = tokenManager.getUser();
        
        if (currentUser == null) {
            Log.e(TAG, "No user found, redirecting to login");
            redirectToLogin();
            return;
        }
        
        initializeViews();
        setupRecyclerView();
        loadOrganizations();
    }
    
    private void initializeViews() {
        organizationsRecyclerView = findViewById(R.id.recyclerOrganizations);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        logoutButton = findViewById(R.id.btnLogout);
        
        logoutButton.setOnClickListener(v -> logout());
    }
    
    private void setupRecyclerView() {
        organizationAdapter = new OrganizationAdapter(this);
        organizationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        organizationsRecyclerView.setAdapter(organizationAdapter);
    }
    
    private void loadOrganizations() {
        // First, try to load from user object if available
        if (currentUser.getOrganizations() != null && !currentUser.getOrganizations().isEmpty()) {
            Log.d(TAG, "Loading organizations from user object");
            organizationAdapter.setOrganizations(currentUser.getOrganizations());
            showContent();
            return;
        }
        
        // If not available in user object, fetch from API
        showLoading();
        Log.d(TAG, "Fetching organizations from API");
        
        String authHeader = tokenManager.getAuthHeader();
        Call<ApiResponse<List<Organization>>> call = apiService.getUserOrganizations(authHeader);
        
        call.enqueue(new Callback<ApiResponse<List<Organization>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Organization>>> call, Response<ApiResponse<List<Organization>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Organization>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Organization> organizations = apiResponse.getData();
                        Log.d(TAG, "Loaded " + organizations.size() + " organizations");
                        
                        // Update user object with organizations
                        currentUser.setOrganizations(organizations);
                        tokenManager.updateUser(currentUser);
                        
                        organizationAdapter.setOrganizations(organizations);
                        showContent();
                    } else {
                        handleError("Failed to load organizations: " + apiResponse.getErrorMessage());
                    }
                } else {
                    handleError("Failed to load organizations: HTTP " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<Organization>>> call, Throwable t) {
                handleError("Network error: " + t.getMessage());
            }
        });
    }
    
    @Override
    public void onOrganizationClick(Organization organization) {
        Log.d(TAG, "Organization selected: " + organization.getName());
        
        // Return selected organization to calling activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_organization_id", organization.getId());
        resultIntent.putExtra("selected_organization_name", organization.getName());
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    
    private void showLoading() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        organizationsRecyclerView.setVisibility(View.GONE);
    }
    
    private void showContent() {
        loadingProgressBar.setVisibility(View.GONE);
        organizationsRecyclerView.setVisibility(View.VISIBLE);
    }
    
    private void handleError(String errorMessage) {
        Log.e(TAG, errorMessage);
        showContent(); // Show content area
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        
        // If we have any organizations in the user object, show them
        if (currentUser.getOrganizations() != null && !currentUser.getOrganizations().isEmpty()) {
            organizationAdapter.setOrganizations(currentUser.getOrganizations());
        }
    }
    
    private void logout() {
        Log.d(TAG, "User logging out from organization selector");
        tokenManager.clearAuthData();
        redirectToLogin();
    }
    
    private void redirectToLogin() {
        Intent intent = new Intent(this, com.calltrackerpro.calltracker.ui.login.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        // Don't allow back navigation, user must select an organization or logout
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Organization Required")
                .setMessage("Please select an organization to continue, or logout to return to login screen.")
                .setPositiveButton("OK", null)
                .setNegativeButton("Logout", (dialog, which) -> logout())
                .show();
    }
}