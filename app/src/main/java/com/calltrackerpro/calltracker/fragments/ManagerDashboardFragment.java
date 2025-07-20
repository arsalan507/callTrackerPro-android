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
import com.calltrackerpro.calltracker.models.Team;
import com.calltrackerpro.calltracker.models.ApiResponse;
import com.calltrackerpro.calltracker.services.ApiService;
import com.calltrackerpro.calltracker.utils.PermissionManager;
import com.calltrackerpro.calltracker.utils.RetrofitClient;
import com.calltrackerpro.calltracker.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class ManagerDashboardFragment extends Fragment {
    private static final String TAG = "ManagerDashboard";
    
    private TokenManager tokenManager;
    private ApiService apiService;
    private PermissionManager permissionManager;
    private User currentUser;
    
    // UI Components
    private TextView welcomeTextView;
    private TextView teamSummaryTextView;
    private TextView teamPerformanceTextView;
    private TextView monthlyTargetTextView;
    private Button manageTeamButton;
    private Button assignLeadsButton;
    private Button viewReportsButton;
    private Button inviteAgentButton;
    private RecyclerView teamMembersRecyclerView;
    
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
        return inflater.inflate(R.layout.fragment_manager_dashboard, container, false);
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
        teamSummaryTextView = view.findViewById(R.id.tvTeamSummary);
        teamPerformanceTextView = view.findViewById(R.id.tvTeamPerformance);
        monthlyTargetTextView = view.findViewById(R.id.tvMonthlyTarget);
        manageTeamButton = view.findViewById(R.id.btnManageTeam);
        assignLeadsButton = view.findViewById(R.id.btnAssignLeads);
        viewReportsButton = view.findViewById(R.id.btnViewReports);
        inviteAgentButton = view.findViewById(R.id.btnInviteAgent);
        teamMembersRecyclerView = view.findViewById(R.id.recyclerTeamMembers);
        
        // Setup RecyclerView
        teamMembersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
    
    private void setupClickListeners() {
        manageTeamButton.setOnClickListener(v -> {
            if (permissionManager.canManageTeamMembers()) {
                navigateToTeamManagement();
            } else {
                showPermissionError("manage team members");
            }
        });
        
        assignLeadsButton.setOnClickListener(v -> {
            if (permissionManager.canAssignLeads()) {
                navigateToLeadAssignment();
            } else {
                showPermissionError("assign leads");
            }
        });
        
        viewReportsButton.setOnClickListener(v -> {
            if (permissionManager.canViewTeamAnalytics()) {
                navigateToTeamReports();
            } else {
                showPermissionError("view team reports");
            }
        });
        
        inviteAgentButton.setOnClickListener(v -> {
            if (permissionManager.canInviteUsers()) {
                showInviteAgentDialog();
            } else {
                showPermissionError("invite users");
            }
        });
    }
    
    private void updateUIBasedOnPermissions() {
        if (currentUser != null) {
            // Set welcome message
            String welcomeMessage = "Welcome, " + currentUser.getFirstName() + " 👨‍💼";
            welcomeTextView.setText(welcomeMessage);
            
            // Show/hide buttons based on permissions
            manageTeamButton.setVisibility(permissionManager.canManageTeamMembers() ? View.VISIBLE : View.GONE);
            assignLeadsButton.setVisibility(permissionManager.canAssignLeads() ? View.VISIBLE : View.GONE);
            viewReportsButton.setVisibility(permissionManager.canViewTeamAnalytics() ? View.VISIBLE : View.GONE);
            inviteAgentButton.setVisibility(permissionManager.canInviteUsers() ? View.VISIBLE : View.GONE);
        }
    }
    
    private void loadDashboardData() {
        if (currentUser == null) return;
        
        // Load team data
        loadTeamData();
        
        // Load team performance analytics
        loadTeamAnalytics();
        
        // Load team members
        loadTeamMembers();
    }
    
    private void loadTeamData() {
        String authHeader = tokenManager.getAuthHeader();
        String organizationId = currentUser.getOrganizationId();
        
        Call<ApiResponse<List<Team>>> call = apiService.getTeams(authHeader, organizationId);
        
        call.enqueue(new Callback<ApiResponse<List<Team>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Team>>> call, Response<ApiResponse<List<Team>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Team>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Team> teams = apiResponse.getData();
                        
                        // Filter teams where current user is manager
                        List<Team> managedTeams = teams.stream()
                                .filter(team -> currentUser.getId().equals(team.getManagerId()))
                                .collect(java.util.stream.Collectors.toList());
                        
                        Log.d(TAG, "Loaded " + managedTeams.size() + " managed teams");
                        updateTeamSummaryUI(managedTeams);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<Team>>> call, Throwable t) {
                Log.e(TAG, "Failed to load teams: " + t.getMessage());
            }
        });
    }
    
    private void loadTeamAnalytics() {
        // Load analytics for each team the manager oversees
        if (currentUser.getTeams() == null) return;
        
        String authHeader = tokenManager.getAuthHeader();
        
        for (Team team : currentUser.getTeams()) {
            if (currentUser.isManagerOfTeam(team.getId())) {
                Call<ApiResponse<Team.TeamAnalytics>> call = apiService.getTeamAnalytics(authHeader, team.getId());
                
                call.enqueue(new Callback<ApiResponse<Team.TeamAnalytics>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Team.TeamAnalytics>> call, Response<ApiResponse<Team.TeamAnalytics>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Team.TeamAnalytics> apiResponse = response.body();
                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                Team.TeamAnalytics analytics = apiResponse.getData();
                                updateTeamPerformanceUI(team.getName(), analytics);
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<ApiResponse<Team.TeamAnalytics>> call, Throwable t) {
                        Log.e(TAG, "Failed to load analytics for team " + team.getName() + ": " + t.getMessage());
                    }
                });
            }
        }
    }
    
    private void loadTeamMembers() {
        if (!permissionManager.canViewTeamData(null)) return;
        
        // Load members for each managed team
        if (currentUser.getTeams() == null) return;
        
        String authHeader = tokenManager.getAuthHeader();
        
        for (Team team : currentUser.getTeams()) {
            if (currentUser.isManagerOfTeam(team.getId())) {
                Call<ApiResponse<List<User>>> call = apiService.getTeamMembers(authHeader, team.getId());
                
                call.enqueue(new Callback<ApiResponse<List<User>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<User>> apiResponse = response.body();
                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                List<User> members = apiResponse.getData();
                                Log.d(TAG, "Loaded " + members.size() + " members for team " + team.getName());
                                updateTeamMembersUI(members);
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                        Log.e(TAG, "Failed to load team members: " + t.getMessage());
                    }
                });
            }
        }
    }
    
    private void updateTeamSummaryUI(List<Team> teams) {
        if (getContext() == null) return;
        
        getActivity().runOnUiThread(() -> {
            int totalMembers = teams.stream()
                    .mapToInt(Team::getMemberCount)
                    .sum();
            
            String summaryText = "📊 Managing " + teams.size() + " team(s) with " + totalMembers + " total members";
            teamSummaryTextView.setText(summaryText);
        });
    }
    
    private void updateTeamPerformanceUI(String teamName, Team.TeamAnalytics analytics) {
        if (getContext() == null) return;
        
        getActivity().runOnUiThread(() -> {
            String performanceText = "📈 " + teamName + ": " + analytics.getMonthlyCalls() + " calls this month";
            if (analytics.getConversionRate() > 0) {
                performanceText += " (" + String.format("%.1f%%", analytics.getConversionRate() * 100) + " conversion)";
            }
            teamPerformanceTextView.setText(performanceText);
            
            // Update monthly target
            monthlyTargetTextView.setText("🎯 Monthly Target: Loading...");
        });
    }
    
    private void updateTeamMembersUI(List<User> members) {
        // TODO: Create TeamMemberAdapter and set to RecyclerView
        Log.d(TAG, "Updating team members UI with " + members.size() + " members");
    }
    
    private void navigateToTeamManagement() {
        Log.d(TAG, "Navigating to team management");
        // TODO: Navigate to team management fragment/activity
    }
    
    private void navigateToLeadAssignment() {
        Log.d(TAG, "Navigating to lead assignment");
        // TODO: Navigate to lead assignment fragment/activity
    }
    
    private void navigateToTeamReports() {
        Log.d(TAG, "Navigating to team reports");
        // TODO: Navigate to team reports fragment/activity
    }
    
    private void showInviteAgentDialog() {
        Log.d(TAG, "Showing invite agent dialog");
        // TODO: Show dialog for inviting new agents
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