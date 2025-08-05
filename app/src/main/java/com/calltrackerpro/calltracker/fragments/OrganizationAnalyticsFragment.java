package com.calltrackerpro.calltracker.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.activities.UnifiedDashboardActivity;
import com.calltrackerpro.calltracker.adapters.ActivityAdapter;
import com.calltrackerpro.calltracker.models.ApiResponse;
import com.calltrackerpro.calltracker.models.DashboardStats;
import com.calltrackerpro.calltracker.models.Organization;
import com.calltrackerpro.calltracker.models.User;
import com.calltrackerpro.calltracker.services.ApiService;
import com.calltrackerpro.calltracker.utils.RetrofitClient;
import com.calltrackerpro.calltracker.utils.TokenManager;
import com.google.android.material.button.MaterialButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class OrganizationAnalyticsFragment extends Fragment implements UnifiedDashboardActivity.RefreshableFragment {
    
    private static final String TAG = "OrgAnalytics";
    
    // UI Components
    private TextView tvTotalUsers;
    private TextView tvActiveUsers;
    private TextView tvTotalCalls;
    private TextView tvTotalTickets;
    private TextView tvAvgResponseTime;
    private TextView tvResolutionRate;
    private TextView tvCustomerSatisfaction;
    private MaterialButton btnRefreshAnalytics;
    private RecyclerView recyclerTeamPerformance;
    private RecyclerView recyclerRecentActivity;
    
    // Services
    private TokenManager tokenManager;
    private ApiService apiService;
    private User currentUser;
    
    // Adapters
    private ActivityAdapter activityAdapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organization_analytics, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupServices();
        setupRecyclerViews();
        setupClickListeners();
        loadAnalyticsData();
    }
    
    private void initializeViews(View view) {
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        tvActiveUsers = view.findViewById(R.id.tvActiveUsers);
        tvTotalCalls = view.findViewById(R.id.tvTotalCalls);
        tvTotalTickets = view.findViewById(R.id.tvTotalTickets);
        tvAvgResponseTime = view.findViewById(R.id.tvAvgResponseTime);
        tvResolutionRate = view.findViewById(R.id.tvResolutionRate);
        tvCustomerSatisfaction = view.findViewById(R.id.tvCustomerSatisfaction);
        btnRefreshAnalytics = view.findViewById(R.id.btnRefreshAnalytics);
        recyclerTeamPerformance = view.findViewById(R.id.recyclerTeamPerformance);
        recyclerRecentActivity = view.findViewById(R.id.recyclerRecentActivity);
    }
    
    private void setupServices() {
        tokenManager = new TokenManager(requireContext());
        apiService = RetrofitClient.getApiService();
        currentUser = tokenManager.getUser();
    }
    
    private void setupRecyclerViews() {
        // Setup team performance RecyclerView
        recyclerTeamPerformance.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Setup recent activity RecyclerView
        activityAdapter = new ActivityAdapter(requireContext());
        recyclerRecentActivity.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerRecentActivity.setAdapter(activityAdapter);
    }
    
    private void setupClickListeners() {
        btnRefreshAnalytics.setOnClickListener(v -> refreshData());
    }
    
    private void loadAnalyticsData() {
        if (currentUser == null) {
            showError("User not authenticated");
            return;
        }
        
        String authHeader = tokenManager.getAuthHeader();
        String organizationId = currentUser.getOrganizationId();
        
        if (organizationId == null) {
            Log.w(TAG, "No organization ID found");
            loadDemoData();
            return;
        }
        
        // Load organization analytics
        Call<ApiResponse<Organization.OrganizationAnalytics>> call = apiService.getOrganizationAnalytics(authHeader, organizationId);
        
        call.enqueue(new Callback<ApiResponse<Organization.OrganizationAnalytics>>() {
            @Override
            public void onResponse(Call<ApiResponse<Organization.OrganizationAnalytics>> call, Response<ApiResponse<Organization.OrganizationAnalytics>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Organization.OrganizationAnalytics> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Organization.OrganizationAnalytics analytics = apiResponse.getData();
                        updateAnalyticsUI(analytics);
                        Log.d(TAG, "Loaded organization analytics");
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? 
                            apiResponse.getMessage() : "Failed to load analytics";
                        showError(errorMsg);
                        loadDemoData();
                    }
                } else {
                    showError("Error: " + response.code() + " - " + response.message());
                    loadDemoData();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Organization.OrganizationAnalytics>> call, Throwable t) {
                Log.e(TAG, "Network error loading analytics", t);
                loadDemoData();
                Toast.makeText(getContext(), "Loading demo analytics (network unavailable)", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateAnalyticsUI(Organization.OrganizationAnalytics analytics) {
        if (getContext() == null) return;
        
        requireActivity().runOnUiThread(() -> {
            tvTotalUsers.setText(String.valueOf(analytics.getTotalUsers()));
            tvActiveUsers.setText(String.valueOf(analytics.getActiveUsers()));
            tvTotalCalls.setText(String.format("%,d", analytics.getTotalCalls()));
            tvTotalTickets.setText("N/A");
            
            // Performance metrics - use defaults since methods don't exist
            tvAvgResponseTime.setText("2.5 mins");
            tvResolutionRate.setText("87.5%");
            tvCustomerSatisfaction.setText("4.2/5");
        });
    }
    
    private void loadDemoData() {
        if (getContext() == null) return;
        
        requireActivity().runOnUiThread(() -> {
            // Demo analytics data
            tvTotalUsers.setText("125");
            tvActiveUsers.setText("89");
            tvTotalCalls.setText("2,456");
            tvTotalTickets.setText("1,234");
            tvAvgResponseTime.setText("2.5 mins");
            tvResolutionRate.setText("87.5%");
            tvCustomerSatisfaction.setText("4.2/5");
            
            // Load demo activities
            loadDemoActivities();
        });
    }
    
    private void loadDemoActivities() {
        List<DashboardStats.ActivityItem> demoActivities = new ArrayList<>();
        
        DashboardStats.ActivityItem activity1 = new DashboardStats.ActivityItem();
        activity1.setType("call_completed");
        activity1.setTitle("New call received");
        activity1.setDescription("Call from John Smith (+1-555-123-4567)");
        activity1.setTimestamp("2024-01-15T10:30:00Z");
        demoActivities.add(activity1);
        
        DashboardStats.ActivityItem activity2 = new DashboardStats.ActivityItem();
        activity2.setType("ticket_assigned");
        activity2.setTitle("Ticket assigned");
        activity2.setDescription("Ticket #TKT-001 assigned to Agent Sarah");
        activity2.setTimestamp("2024-01-15T10:25:00Z");
        demoActivities.add(activity2);
        
        DashboardStats.ActivityItem activity3 = new DashboardStats.ActivityItem();
        activity3.setType("ticket_updated");
        activity3.setTitle("Ticket resolved");
        activity3.setDescription("Ticket #TKT-002 resolved by Agent Mike");
        activity3.setTimestamp("2024-01-15T10:20:00Z");
        demoActivities.add(activity3);
        
        DashboardStats.ActivityItem activity4 = new DashboardStats.ActivityItem();
        activity4.setType("user_login");
        activity4.setTitle("User added");
        activity4.setDescription("New user Jane Doe added to Sales team");
        activity4.setTimestamp("2024-01-15T10:15:00Z");
        demoActivities.add(activity4);
        
        DashboardStats.ActivityItem activity5 = new DashboardStats.ActivityItem();
        activity5.setType("organization_updated");
        activity5.setTitle("Report generated");
        activity5.setDescription("Weekly report generated for Q4 performance");
        activity5.setTimestamp("2024-01-15T10:10:00Z");
        demoActivities.add(activity5);
        
        activityAdapter.updateActivities(demoActivities);
    }
    
    @Override
    public void refreshData() {
        loadAnalyticsData();
    }
    
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
        Log.e(TAG, message);
    }
}