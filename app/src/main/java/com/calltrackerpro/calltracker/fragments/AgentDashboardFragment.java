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
import com.calltrackerpro.calltracker.models.Contact;
import com.calltrackerpro.calltracker.models.ApiResponse;
import com.calltrackerpro.calltracker.services.ApiService;
import com.calltrackerpro.calltracker.utils.PermissionManager;
import com.calltrackerpro.calltracker.utils.RetrofitClient;
import com.calltrackerpro.calltracker.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class AgentDashboardFragment extends Fragment {
    private static final String TAG = "AgentDashboard";
    
    private TokenManager tokenManager;
    private ApiService apiService;
    private PermissionManager permissionManager;
    private User currentUser;
    
    // UI Components
    private TextView welcomeTextView;
    private TextView statsTextView;
    private TextView todayCallsTextView;
    private TextView weeklyTargetTextView;
    private TextView conversionRateTextView;
    private Button recordCallButton;
    private Button viewContactsButton;
    private Button viewAnalyticsButton;
    private RecyclerView recentContactsRecyclerView;
    
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
        return inflater.inflate(R.layout.fragment_agent_dashboard, container, false);
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
        statsTextView = view.findViewById(R.id.tvStats);
        todayCallsTextView = view.findViewById(R.id.tvTodayCalls);
        weeklyTargetTextView = view.findViewById(R.id.tvWeeklyTarget);
        conversionRateTextView = view.findViewById(R.id.tvConversionRate);
        recordCallButton = view.findViewById(R.id.btnRecordCall);
        viewContactsButton = view.findViewById(R.id.btnViewContacts);
        viewAnalyticsButton = view.findViewById(R.id.btnViewAnalytics);
        recentContactsRecyclerView = view.findViewById(R.id.recyclerRecentContacts);
        
        // Setup RecyclerView
        recentContactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
    
    private void setupClickListeners() {
        recordCallButton.setOnClickListener(v -> {
            if (permissionManager.canRecordCalls()) {
                recordNewCall();
            } else {
                showPermissionError("record calls");
            }
        });
        
        viewContactsButton.setOnClickListener(v -> {
            if (permissionManager.canViewContacts()) {
                navigateToContacts();
            } else {
                showPermissionError("view contacts");
            }
        });
        
        viewAnalyticsButton.setOnClickListener(v -> {
            if (permissionManager.canViewAnalytics()) {
                navigateToAnalytics();
            } else {
                showPermissionError("view analytics");
            }
        });
    }
    
    private void updateUIBasedOnPermissions() {
        if (currentUser != null) {
            // Set welcome message
            String welcomeMessage = "Welcome, " + currentUser.getFirstName() + "!";
            welcomeTextView.setText(welcomeMessage);
            
            // Show/hide buttons based on permissions
            recordCallButton.setVisibility(permissionManager.canRecordCalls() ? View.VISIBLE : View.GONE);
            viewContactsButton.setVisibility(permissionManager.canViewContacts() ? View.VISIBLE : View.GONE);
            viewAnalyticsButton.setVisibility(permissionManager.canViewAnalytics() ? View.VISIBLE : View.GONE);
        }
    }
    
    private void loadDashboardData() {
        if (currentUser == null) return;
        
        // Load basic stats from user object
        updateBasicStats();
        
        // Load recent contacts
        loadRecentContacts();
        
        // Load analytics data
        loadAnalytics();
    }
    
    private void updateBasicStats() {
        // Display basic call statistics
        String statsText = "📞 Total Calls: " + currentUser.getCallCount();
        if (currentUser.getCallLimit() > 0) {
            statsText += " / " + currentUser.getCallLimit();
        }
        statsTextView.setText(statsText);
        
        // TODO: Load today's calls, weekly targets, etc. from analytics API
        todayCallsTextView.setText("Today: Loading...");
        weeklyTargetTextView.setText("Weekly Target: Loading...");
        conversionRateTextView.setText("Conversion Rate: Loading...");
    }
    
    private void loadRecentContacts() {
        if (!permissionManager.canViewContacts()) return;
        
        String authHeader = tokenManager.getAuthHeader();
        String organizationId = currentUser.getOrganizationId();
        String agentId = currentUser.getId();
        
        Call<ApiResponse<List<Contact>>> call = apiService.getContacts(
                authHeader, organizationId, null, agentId, null, 1, 5
        );
        
        call.enqueue(new Callback<ApiResponse<List<Contact>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Contact>>> call, Response<ApiResponse<List<Contact>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Contact>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Contact> contacts = apiResponse.getData();
                        Log.d(TAG, "Loaded " + contacts.size() + " recent contacts");
                        updateRecentContactsUI(contacts);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<Contact>>> call, Throwable t) {
                Log.e(TAG, "Failed to load recent contacts: " + t.getMessage());
            }
        });
    }
    
    private void loadAnalytics() {
        if (!permissionManager.canViewAnalytics()) return;
        
        String authHeader = tokenManager.getAuthHeader();
        String userId = currentUser.getId();
        
        Call<ApiResponse<ApiService.UserAnalytics>> call = apiService.getUserAnalytics(authHeader, userId, "week");
        
        call.enqueue(new Callback<ApiResponse<ApiService.UserAnalytics>>() {
            @Override
            public void onResponse(Call<ApiResponse<ApiService.UserAnalytics>> call, Response<ApiResponse<ApiService.UserAnalytics>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ApiService.UserAnalytics> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        ApiService.UserAnalytics analytics = apiResponse.getData();
                        updateAnalyticsUI(analytics);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<ApiService.UserAnalytics>> call, Throwable t) {
                Log.e(TAG, "Failed to load analytics: " + t.getMessage());
            }
        });
    }
    
    private void updateRecentContactsUI(List<Contact> contacts) {
        // TODO: Create ContactAdapter and set to RecyclerView
        Log.d(TAG, "Updating UI with " + contacts.size() + " contacts");
    }
    
    private void updateAnalyticsUI(ApiService.UserAnalytics analytics) {
        if (getContext() == null) return;
        
        getActivity().runOnUiThread(() -> {
            todayCallsTextView.setText("Today: " + analytics.getTotalCalls());
            conversionRateTextView.setText("Conversion Rate: " + String.format("%.1f%%", analytics.getConversionRate() * 100));
            
            // TODO: Update weekly target based on team targets
            weeklyTargetTextView.setText("Weekly Target: N/A");
        });
    }
    
    private void recordNewCall() {
        Log.d(TAG, "Recording new call");
        // TODO: Implement call recording functionality
        // This could open a new activity or dialog for call recording
    }
    
    private void navigateToContacts() {
        Log.d(TAG, "Navigating to contacts");
        // TODO: Navigate to contacts fragment/activity
    }
    
    private void navigateToAnalytics() {
        Log.d(TAG, "Navigating to analytics");
        // TODO: Navigate to analytics fragment/activity
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