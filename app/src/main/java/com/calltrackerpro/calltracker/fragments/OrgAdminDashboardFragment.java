package com.calltrackerpro.calltracker.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.models.User;
import com.calltrackerpro.calltracker.models.Organization;
import com.calltrackerpro.calltracker.models.ApiResponse;
import com.calltrackerpro.calltracker.services.ApiService;
import com.calltrackerpro.calltracker.utils.PermissionManager;
import com.calltrackerpro.calltracker.activities.UnifiedDashboardActivity;
import com.calltrackerpro.calltracker.utils.RetrofitClient;
import com.calltrackerpro.calltracker.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class OrgAdminDashboardFragment extends Fragment {
    private static final String TAG = "OrgAdminDashboard";
    
    private TokenManager tokenManager;
    private ApiService apiService;
    private PermissionManager permissionManager;
    private User currentUser;
    
    // UI Components
    private TextView welcomeTextView;
    private TextView organizationStatsTextView;
    private TextView subscriptionStatusTextView;
    private TextView userLimitTextView;
    private TextView callLimitTextView;
    private Button manageUsersButton;
    private Button manageTeamsButton;
    private Button viewAnalyticsButton;
    private Button organizationSettingsButton;
    private Button inviteManagerButton;
    private RecyclerView recentActivitiesRecyclerView;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize services
        tokenManager = new TokenManager(getContext());
        apiService = RetrofitClient.getApiService();
        currentUser = tokenManager.getUser();
        
        if (currentUser != null) {
            permissionManager = new PermissionManager(currentUser);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_org_admin_dashboard, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupClickListeners();
        loadDashboardData();
        updateUIBasedOnPermissions();
    }
    
    private void initializeViews(View view) {
        welcomeTextView = view.findViewById(R.id.tvWelcome);
        organizationStatsTextView = view.findViewById(R.id.tvOrganizationStats);
        subscriptionStatusTextView = view.findViewById(R.id.tvSubscriptionStatus);
        userLimitTextView = view.findViewById(R.id.tvUserLimit);
        callLimitTextView = view.findViewById(R.id.tvCallLimit);
        manageUsersButton = view.findViewById(R.id.btnManageUsers);
        manageTeamsButton = view.findViewById(R.id.btnManageTeams);
        viewAnalyticsButton = view.findViewById(R.id.btnViewAnalytics);
        organizationSettingsButton = view.findViewById(R.id.btnOrganizationSettings);
        inviteManagerButton = view.findViewById(R.id.btnInviteManager);
        recentActivitiesRecyclerView = view.findViewById(R.id.recyclerRecentActivities);
        
        // Setup RecyclerView
        recentActivitiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
    
    private void setupClickListeners() {
        manageUsersButton.setOnClickListener(v -> {
            if (permissionManager.canManageUsers()) {
                navigateToUserManagement();
            } else {
                showPermissionError("manage users");
            }
        });
        
        manageTeamsButton.setOnClickListener(v -> {
            if (permissionManager.canManageTeams()) {
                navigateToTeamManagement();
            } else {
                showPermissionError("manage teams");
            }
        });
        
        viewAnalyticsButton.setOnClickListener(v -> {
            if (permissionManager.canViewOrgAnalytics()) {
                navigateToOrganizationAnalytics();
            } else {
                showPermissionError("view organization analytics");
            }
        });
        
        organizationSettingsButton.setOnClickListener(v -> {
            if (permissionManager.canManageOrgSettings()) {
                navigateToOrganizationSettings();
            } else {
                showPermissionError("manage organization settings");
            }
        });
        
        inviteManagerButton.setOnClickListener(v -> {
            if (permissionManager.canInviteUsers()) {
                showInviteManagerDialog();
            } else {
                showPermissionError("invite users");
            }
        });
    }
    
    private void updateUIBasedOnPermissions() {
        if (currentUser != null) {
            // Set welcome message with organization name
            String orgName = "CallTracker Pro"; // Default organization name
            if (currentUser.getCurrentOrganization() != null && 
                currentUser.getCurrentOrganization().getName() != null && 
                !currentUser.getCurrentOrganization().getName().trim().isEmpty()) {
                orgName = currentUser.getCurrentOrganization().getName();
            }
            String welcomeMessage = "Welcome to " + orgName + ", " + currentUser.getFirstName() + "! 👑";
            welcomeTextView.setText(welcomeMessage);
            
            // Show/hide buttons based on permissions
            manageUsersButton.setVisibility(permissionManager.canManageUsers() ? View.VISIBLE : View.GONE);
            manageTeamsButton.setVisibility(permissionManager.canManageTeams() ? View.VISIBLE : View.GONE);
            viewAnalyticsButton.setVisibility(permissionManager.canViewOrgAnalytics() ? View.VISIBLE : View.GONE);
            organizationSettingsButton.setVisibility(permissionManager.canManageOrgSettings() ? View.VISIBLE : View.GONE);
            inviteManagerButton.setVisibility(permissionManager.canInviteUsers() ? View.VISIBLE : View.GONE);
        }
    }
    
    private void loadDashboardData() {
        if (currentUser == null) return;
        
        // Load organization data
        loadOrganizationData();
        
        // Load organization analytics
        loadOrganizationAnalytics();
        
        // Load dashboard summary
        loadDashboardSummary();
    }
    
    private void loadOrganizationData() {
        String authHeader = tokenManager.getAuthHeader();
        String organizationId = currentUser.getOrganizationId();
        
        if (organizationId == null) {
            Log.w(TAG, "No organization ID found");
            return;
        }
        
        Call<ApiResponse<Organization>> call = apiService.getOrganization(authHeader, organizationId);
        
        call.enqueue(new Callback<ApiResponse<Organization>>() {
            @Override
            public void onResponse(Call<ApiResponse<Organization>> call, Response<ApiResponse<Organization>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Organization> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Organization organization = apiResponse.getData();
                        Log.d(TAG, "Loaded organization data: " + organization.getName());
                        updateOrganizationUI(organization);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Organization>> call, Throwable t) {
                Log.e(TAG, "Failed to load organization data: " + t.getMessage());
            }
        });
    }
    
    private void loadOrganizationAnalytics() {
        String authHeader = tokenManager.getAuthHeader();
        String organizationId = currentUser.getOrganizationId();
        
        if (organizationId == null) return;
        
        Call<ApiResponse<Organization.OrganizationAnalytics>> call = apiService.getOrganizationAnalytics(authHeader, organizationId);
        
        call.enqueue(new Callback<ApiResponse<Organization.OrganizationAnalytics>>() {
            @Override
            public void onResponse(Call<ApiResponse<Organization.OrganizationAnalytics>> call, Response<ApiResponse<Organization.OrganizationAnalytics>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Organization.OrganizationAnalytics> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Organization.OrganizationAnalytics analytics = apiResponse.getData();
                        updateAnalyticsUI(analytics);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Organization.OrganizationAnalytics>> call, Throwable t) {
                Log.e(TAG, "Failed to load organization analytics: " + t.getMessage());
            }
        });
    }
    
    private void loadDashboardSummary() {
        String authHeader = tokenManager.getAuthHeader();
        String organizationId = currentUser.getOrganizationId();
        
        if (organizationId == null) return;
        
        Call<ApiResponse<ApiService.DashboardSummary>> call = apiService.getDashboardSummary(authHeader, organizationId);
        
        call.enqueue(new Callback<ApiResponse<ApiService.DashboardSummary>>() {
            @Override
            public void onResponse(Call<ApiResponse<ApiService.DashboardSummary>> call, Response<ApiResponse<ApiService.DashboardSummary>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ApiService.DashboardSummary> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        ApiService.DashboardSummary summary = apiResponse.getData();
                        updateDashboardSummaryUI(summary);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<ApiService.DashboardSummary>> call, Throwable t) {
                Log.e(TAG, "Failed to load dashboard summary: " + t.getMessage());
            }
        });
    }
    
    private void updateOrganizationUI(Organization organization) {
        if (getContext() == null) return;
        
        getActivity().runOnUiThread(() -> {
            // Update subscription status
            if (organization.getSubscription() != null) {
                Organization.Subscription subscription = organization.getSubscription();
                
                String statusText = "📋 Plan: " + subscription.getPlan() + " (" + subscription.getStatus() + ")";
                subscriptionStatusTextView.setText(statusText);
                
                // Set status color
                if (subscription.isActive()) {
                    subscriptionStatusTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                } else {
                    subscriptionStatusTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
                
                // Update limits
                String userLimitText = "👥 Users: " + " / " + 
                    (subscription.getUserLimit() == 0 ? "Unlimited" : subscription.getUserLimit());
                userLimitTextView.setText(userLimitText);
                
                String callLimitText = "📞 Calls: " + " / " + 
                    (subscription.getCallLimit() == 0 ? "Unlimited" : subscription.getCallLimit());
                callLimitTextView.setText(callLimitText);
            }
        });
    }
    
    private void updateAnalyticsUI(Organization.OrganizationAnalytics analytics) {
        if (getContext() == null) return;
        
        getActivity().runOnUiThread(() -> {
            String statsText = "📊 " + analytics.getTotalUsers() + " users, " + 
                              analytics.getTotalCalls() + " total calls, " + 
                              analytics.getActiveUsers() + " active users";
            organizationStatsTextView.setText(statsText);
        });
    }
    
    private void updateDashboardSummaryUI(ApiService.DashboardSummary summary) {
        if (getContext() == null) return;
        
        getActivity().runOnUiThread(() -> {
            // TODO: Update recent activities RecyclerView
            Log.d(TAG, "Updating dashboard summary UI");
        });
    }
    
    private void navigateToUserManagement() {
        Log.d(TAG, "Navigating to user management");
        if (getActivity() instanceof UnifiedDashboardActivity) {
            ((UnifiedDashboardActivity) getActivity()).replaceFragment(new UserManagementFragment());
        }
    }
    
    private void navigateToTeamManagement() {
        Log.d(TAG, "Navigating to team management");
        // TODO: Navigate to team management fragment/activity
    }
    
    private void navigateToOrganizationAnalytics() {
        Log.d(TAG, "Navigating to organization analytics");
        if (getActivity() instanceof UnifiedDashboardActivity) {
            ((UnifiedDashboardActivity) getActivity()).replaceFragment(new OrganizationAnalyticsFragment());
        }
    }
    
    private void navigateToOrganizationSettings() {
        Log.d(TAG, "Navigating to organization settings");
        // TODO: Navigate to organization settings fragment/activity
    }
    
    private void showInviteManagerDialog() {
        Log.d(TAG, "Showing invite manager dialog");
        // TODO: Show dialog for inviting new managers
    }
    
    private void showPermissionError(String action) {
        String message = "You don't have permission to " + action;
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
        Log.w(TAG, "Permission denied: " + action);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        loadDashboardData();
    }
}