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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.activities.UnifiedDashboardActivity;
import com.calltrackerpro.calltracker.adapters.ActivityAdapter;
import com.calltrackerpro.calltracker.models.ApiResponse;
import com.calltrackerpro.calltracker.models.DashboardStats;
import com.calltrackerpro.calltracker.models.User;
import com.calltrackerpro.calltracker.services.ApiService;
import com.calltrackerpro.calltracker.utils.TokenManager;
import com.calltrackerpro.calltracker.utils.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EnhancedDashboardFragment extends Fragment implements UnifiedDashboardActivity.DashboardFragment {
    private static final String TAG = "EnhancedDashboard";

    // UI Components
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView welcomeText;
    private TextView roleText;
    
    // Quick Stats Cards
    private TextView callsCountText;
    private TextView ticketsCountText;
    private TextView successRateText;
    private TextView activeHoursText;
    
    // Organization Stats (for admins)
    private View organizationStatsContainer;
    private TextView totalUsersText;
    private TextView activeUsersText;
    private TextView totalTicketsText;
    private TextView subscriptionStatusText;
    
    // Recent Activity
    private RecyclerView recentActivityRecycler;
    private ActivityAdapter activityAdapter;
    private View emptyStateView;
    
    // Performance Metrics
    private TextView conversionRateText;
    private TextView avgCallDurationText;
    private TextView responseTimeText;
    private TextView customerSatisfactionText;
    
    // Data & Services
    private TokenManager tokenManager;
    private ApiService apiService;
    private User currentUser;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_enhanced_dashboard, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeServices();
        initializeViews(view);
        setupRecyclerView();
        loadDashboardData();
    }
    
    private void initializeServices() {
        tokenManager = new TokenManager(requireContext());
        apiService = RetrofitClient.getApiService();
        currentUser = tokenManager.getUser();
    }
    
    private void initializeViews(View view) {
        // Main components
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        welcomeText = view.findViewById(R.id.tv_welcome);
        roleText = view.findViewById(R.id.tv_role);
        
        // Quick stats
        callsCountText = view.findViewById(R.id.tv_calls_count);
        ticketsCountText = view.findViewById(R.id.tv_tickets_count);
        successRateText = view.findViewById(R.id.tv_success_rate);
        activeHoursText = view.findViewById(R.id.tv_active_hours);
        
        // Organization stats
        organizationStatsContainer = view.findViewById(R.id.organization_stats_container);
        totalUsersText = view.findViewById(R.id.tv_total_users);
        activeUsersText = view.findViewById(R.id.tv_active_users);
        totalTicketsText = view.findViewById(R.id.tv_total_tickets);
        subscriptionStatusText = view.findViewById(R.id.tv_subscription_status);
        
        // Recent activity
        recentActivityRecycler = view.findViewById(R.id.recycler_recent_activity);
        emptyStateView = view.findViewById(R.id.empty_state_view);
        
        // Performance metrics
        conversionRateText = view.findViewById(R.id.tv_conversion_rate);
        avgCallDurationText = view.findViewById(R.id.tv_avg_call_duration);
        responseTimeText = view.findViewById(R.id.tv_response_time);
        customerSatisfactionText = view.findViewById(R.id.tv_customer_satisfaction);
        
        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
        
        // Set welcome text
        if (currentUser != null) {
            welcomeText.setText("Welcome back, " + currentUser.getFullName());
            roleText.setText("Role: " + currentUser.getRoleDisplayName());
        }
        
        // Show/hide organization stats based on role
        boolean showOrgStats = currentUser != null && 
                              (currentUser.isOrganizationAdmin() || currentUser.getRole().equals("super_admin"));
        organizationStatsContainer.setVisibility(showOrgStats ? View.VISIBLE : View.GONE);
    }
    
    private void setupRecyclerView() {
        activityAdapter = new ActivityAdapter(requireContext());
        recentActivityRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recentActivityRecycler.setAdapter(activityAdapter);
        
        activityAdapter.setOnItemClickListener(this::handleActivityItemClick);
    }
    
    private void loadDashboardData() {
        if (!isAdded() || getContext() == null) {
            return;
        }
        
        if (!tokenManager.isLoggedIn() || currentUser == null) {
            showError("Authentication required");
            return;
        }
        
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        
        String token = tokenManager.getAuthHeader();
        String organizationId = currentUser.getOrganizationId();
        
        Call<ApiResponse<DashboardStats>> call = apiService.getDashboardStats(token, organizationId, "today");
        
        call.enqueue(new Callback<ApiResponse<DashboardStats>>() {
            @Override
            public void onResponse(Call<ApiResponse<DashboardStats>> call, Response<ApiResponse<DashboardStats>> response) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<DashboardStats> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        updateDashboardUI(apiResponse.getData());
                    } else {
                        showError("Failed to load dashboard: " + apiResponse.getErrorMessage());
                    }
                } else {
                    showError("Server error: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<DashboardStats>> call, Throwable t) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                showError("Network error: " + t.getMessage());
                Log.e(TAG, "Dashboard load failed", t);
            }
        });
    }
    
    private void updateDashboardUI(DashboardStats stats) {
        if (stats == null) {
            showError("No dashboard data available");
            return;
        }
        
        // Update user stats
        DashboardStats.UserStats userStats = stats.getUserStats();
        if (userStats != null) {
            callsCountText.setText(String.valueOf(userStats.getTotalCalls()));
            ticketsCountText.setText(String.valueOf(userStats.getTicketsAssigned()));
            successRateText.setText(String.format(Locale.getDefault(), "%.1f%%", userStats.getSuccessRate()));
            activeHoursText.setText(userStats.getActiveHours() != null ? userStats.getActiveHours() : "0 hr");
        }
        
        // Update organization stats (if visible)
        if (organizationStatsContainer.getVisibility() == View.VISIBLE) {
            DashboardStats.OrganizationStats orgStats = stats.getOrganizationStats();
            if (orgStats != null) {
                totalUsersText.setText(String.valueOf(orgStats.getTotalUsers()));
                activeUsersText.setText(String.valueOf(orgStats.getActiveUsers()));
                totalTicketsText.setText(String.valueOf(orgStats.getTotalTickets()));
                subscriptionStatusText.setText(orgStats.getSubscriptionStatus() != null ? 
                    orgStats.getSubscriptionStatus() : "Unknown");
            }
        }
        
        // Update performance metrics
        DashboardStats.PerformanceMetrics metrics = stats.getPerformanceMetrics();
        if (metrics != null) {
            conversionRateText.setText(String.format(Locale.getDefault(), "%.1f%%", metrics.getConversionRate()));
            avgCallDurationText.setText(String.format(Locale.getDefault(), "%.1f min", metrics.getAverageCallDuration()));
            responseTimeText.setText(String.format(Locale.getDefault(), "%.1f min", metrics.getResponseTime()));
            customerSatisfactionText.setText(String.format(Locale.getDefault(), "%.1f/5", metrics.getCustomerSatisfaction()));
        }
        
        // Update recent activity
        List<DashboardStats.ActivityItem> recentActivity = stats.getRecentActivity();
        if (recentActivity != null && !recentActivity.isEmpty()) {
            activityAdapter.updateActivities(recentActivity);
            recentActivityRecycler.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        } else {
            recentActivityRecycler.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        }
        
        Log.d(TAG, "Dashboard UI updated successfully");
    }
    
    private void handleActivityItemClick(DashboardStats.ActivityItem activity) {
        // Handle click on activity item
        switch (activity.getType()) {
            case "ticket_created":
            case "ticket_updated":
            case "ticket_assigned":
                // Navigate to ticket details
                navigateToTicketDetails(activity.getId());
                break;
            case "call_completed":
                // Navigate to call details
                navigateToCallDetails(activity.getId());
                break;
            case "user_login":
            case "user_logout":
                // Show user info
                showUserInfo(activity.getUserName());
                break;
            default:
                // Show activity details
                showActivityDetails(activity);
                break;
        }
    }
    
    private void navigateToTicketDetails(String ticketId) {
        // TODO: Navigate to ticket details activity/fragment
        Toast.makeText(requireContext(), "Navigate to ticket: " + ticketId, Toast.LENGTH_SHORT).show();
    }
    
    private void navigateToCallDetails(String callId) {
        // TODO: Navigate to call details activity/fragment
        Toast.makeText(requireContext(), "Navigate to call: " + callId, Toast.LENGTH_SHORT).show();
    }
    
    private void showUserInfo(String userName) {
        Toast.makeText(requireContext(), "User: " + userName, Toast.LENGTH_SHORT).show();
    }
    
    private void showActivityDetails(DashboardStats.ActivityItem activity) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(activity.getTitle())
                .setMessage(activity.getDescription())
                .setPositiveButton("OK", null)
                .show();
    }
    
    @Override
    public void refreshData() {
        Log.d(TAG, "Refreshing dashboard data");
        loadDashboardData();
    }
    
    private void showError(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
        Log.e(TAG, message);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        if (isAdded() && getContext() != null && swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            loadDashboardData();
        }
    }
}