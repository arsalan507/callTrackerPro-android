package com.calltrackerpro.calltracker.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.models.User;
import com.calltrackerpro.calltracker.models.Contact;
import com.calltrackerpro.calltracker.models.CallLog;
import com.calltrackerpro.calltracker.models.ApiResponse;
import com.calltrackerpro.calltracker.services.ApiService;
import com.calltrackerpro.calltracker.utils.PermissionManager;
import com.calltrackerpro.calltracker.utils.RetrofitClient;
import com.calltrackerpro.calltracker.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class ViewerDashboardFragment extends Fragment {
    private static final String TAG = "ViewerDashboard";
    
    private TokenManager tokenManager;
    private ApiService apiService;
    private PermissionManager permissionManager;
    private User currentUser;
    
    // UI Components
    private TextView welcomeTextView;
    private TextView accessLevelTextView;
    private TextView dataStatsTextView;
    private TextView viewOnlyNoticeTextView;
    private RecyclerView assignedDataRecyclerView;
    private RecyclerView recentCallsRecyclerView;
    
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
        return inflater.inflate(R.layout.fragment_viewer_dashboard, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        loadDashboardData();
        updateUIForViewerRole();
    }
    
    private void initializeViews(View view) {
        welcomeTextView = view.findViewById(R.id.tvWelcome);
        accessLevelTextView = view.findViewById(R.id.tvAccessLevel);
        dataStatsTextView = view.findViewById(R.id.tvDataStats);
        viewOnlyNoticeTextView = view.findViewById(R.id.tvViewOnlyNotice);
        assignedDataRecyclerView = view.findViewById(R.id.recyclerAssignedData);
        recentCallsRecyclerView = view.findViewById(R.id.recyclerRecentCalls);
        
        // Setup RecyclerViews
        assignedDataRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recentCallsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
    
    private void updateUIForViewerRole() {
        if (currentUser != null) {
            // Set welcome message
            String welcomeMessage = "Welcome, " + currentUser.getFirstName() + " 👀";
            welcomeTextView.setText(welcomeMessage);
            
            // Set access level information
            String accessText = "🔐 Access Level: Viewer (Read-Only)";
            accessLevelTextView.setText(accessText);
            
            // Show view-only notice
            String noticeText = "ℹ️ You have read-only access to assigned data. Contact your administrator for additional permissions.";
            viewOnlyNoticeTextView.setText(noticeText);
            viewOnlyNoticeTextView.setVisibility(View.VISIBLE);
        }
    }
    
    private void loadDashboardData() {
        if (currentUser == null) return;
        
        // Load assigned contacts (read-only)
        loadAssignedContacts();
        
        // Load recent calls that viewer has access to
        loadViewableCallLogs();
        
        // Load basic statistics
        loadDataStatistics();
    }
    
    private void loadAssignedContacts() {
        if (!permissionManager.canViewContacts()) {
            Log.d(TAG, "No permission to view contacts");
            return;
        }
        
        String authHeader = tokenManager.getAuthHeader();
        String organizationId = currentUser.getOrganizationId();
        
        // For viewers, we might load contacts they're assigned to view
        // or contacts from their team if they're part of a team
        Call<ApiResponse<List<Contact>>> call = apiService.getContacts(
                authHeader, organizationId, null, null, null, 1, 10
        );
        
        call.enqueue(new Callback<ApiResponse<List<Contact>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Contact>>> call, Response<ApiResponse<List<Contact>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Contact>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Contact> contacts = apiResponse.getData();
                        Log.d(TAG, "Loaded " + contacts.size() + " viewable contacts");
                        updateAssignedDataUI(contacts);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<Contact>>> call, Throwable t) {
                Log.e(TAG, "Failed to load contacts: " + t.getMessage());
            }
        });
    }
    
    private void loadViewableCallLogs() {
        if (!permissionManager.canViewCalls()) {
            Log.d(TAG, "No permission to view calls");
            return;
        }
        
        String authHeader = tokenManager.getAuthHeader();
        String organizationId = currentUser.getOrganizationId();
        
        // For viewers, load calls they have permission to see
        // This might be filtered by team or specific assignments
        Call<ApiResponse<List<CallLog>>> call = apiService.getFilteredCallLogs(
                authHeader, organizationId, null, null, null, null, null, 1, 10
        );
        
        call.enqueue(new Callback<ApiResponse<List<CallLog>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CallLog>>> call, Response<ApiResponse<List<CallLog>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<CallLog>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<CallLog> callLogs = apiResponse.getData();
                        Log.d(TAG, "Loaded " + callLogs.size() + " viewable call logs");
                        updateRecentCallsUI(callLogs);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<CallLog>>> call, Throwable t) {
                Log.e(TAG, "Failed to load call logs: " + t.getMessage());
            }
        });
    }
    
    private void loadDataStatistics() {
        if (!permissionManager.canViewAnalytics()) {
            Log.d(TAG, "No permission to view analytics");
            dataStatsTextView.setText("📊 Analytics: Access Restricted");
            return;
        }
        
        String authHeader = tokenManager.getAuthHeader();
        String userId = currentUser.getId();
        
        Call<ApiResponse<ApiService.UserAnalytics>> call = apiService.getUserAnalytics(authHeader, userId, "month");
        
        call.enqueue(new Callback<ApiResponse<ApiService.UserAnalytics>>() {
            @Override
            public void onResponse(Call<ApiResponse<ApiService.UserAnalytics>> call, Response<ApiResponse<ApiService.UserAnalytics>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ApiService.UserAnalytics> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        ApiService.UserAnalytics analytics = apiResponse.getData();
                        updateDataStatisticsUI(analytics);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<ApiService.UserAnalytics>> call, Throwable t) {
                Log.e(TAG, "Failed to load analytics: " + t.getMessage());
                if (getContext() != null) {
                    getActivity().runOnUiThread(() -> {
                        dataStatsTextView.setText("📊 Data: Unable to load statistics");
                    });
                }
            }
        });
    }
    
    private void updateAssignedDataUI(List<Contact> contacts) {
        if (getContext() == null) return;
        
        // TODO: Create a read-only ContactAdapter for viewers
        Log.d(TAG, "Updating assigned data UI with " + contacts.size() + " contacts");
        
        getActivity().runOnUiThread(() -> {
            // For now, just show a summary
            // TODO: Implement proper adapter
        });
    }
    
    private void updateRecentCallsUI(List<CallLog> callLogs) {
        if (getContext() == null) return;
        
        // TODO: Create a read-only CallLogAdapter for viewers
        Log.d(TAG, "Updating recent calls UI with " + callLogs.size() + " call logs");
        
        getActivity().runOnUiThread(() -> {
            // For now, just show a summary
            // TODO: Implement proper adapter
        });
    }
    
    private void updateDataStatisticsUI(ApiService.UserAnalytics analytics) {
        if (getContext() == null) return;
        
        getActivity().runOnUiThread(() -> {
            String statsText = "📊 Data Access: " + analytics.getContactsCreated() + " contacts viewable, " +
                              analytics.getTotalCalls() + " calls accessible";
            dataStatsTextView.setText(statsText);
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        loadDashboardData();
    }
}