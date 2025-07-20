package com.calltrackerpro.calltracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.calltrackerpro.calltracker.fragments.*;
import com.calltrackerpro.calltracker.models.User;
import com.calltrackerpro.calltracker.models.Organization;
import com.calltrackerpro.calltracker.utils.PermissionManager;
import com.calltrackerpro.calltracker.utils.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class DashboardRouterActivity extends AppCompatActivity {
    private static final String TAG = "DashboardRouter";
    
    private TokenManager tokenManager;
    private PermissionManager permissionManager;
    private User currentUser;
    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize token manager and check authentication
        tokenManager = new TokenManager(this);
        
        if (!tokenManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }
        
        currentUser = tokenManager.getUser();
        if (currentUser == null) {
            Log.e(TAG, "User data not found, redirecting to login");
            redirectToLogin();
            return;
        }
        
        try {
            permissionManager = new PermissionManager(currentUser);
        } catch (Exception e) {
            Log.e(TAG, "Error creating PermissionManager: " + e.getMessage());
            redirectToLogin();
            return;
        }
        
        // Check if user has multiple organizations and needs to select one
        try {
            if (currentUser.hasMultipleOrganizations() && currentUser.getCurrentOrganization() == null) {
                showOrganizationSelector();
                return;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error checking organization status: " + e.getMessage());
            // Continue with single organization setup
        }
        
        // Set appropriate layout based on user role
        try {
            setupDashboardLayout();
            
            // Initialize fragment manager
            fragmentManager = getSupportFragmentManager();
            
            // Load initial dashboard fragment
            loadInitialDashboard();
        } catch (Exception e) {
            Log.e(TAG, "Error setting up dashboard: " + e.getMessage());
            showError("Failed to load dashboard: " + e.getMessage());
            redirectToLogin();
            return;
        }
        
        Log.d(TAG, "Dashboard initialized for user: " + currentUser.getEmail() + 
                   " with role: " + currentUser.getRole());
    }
    
    private void setupDashboardLayout() {
        // Set the appropriate layout based on user role
        String dashboardType = permissionManager.getPrimaryDashboardType();
        
        switch (dashboardType) {
            case "org_admin":
                setContentView(R.layout.activity_dashboard_org_admin);
                setupOrgAdminNavigation();
                break;
            case "manager":
                setContentView(R.layout.activity_dashboard_manager);
                setupManagerNavigation();
                break;
            case "agent":
                setContentView(R.layout.activity_dashboard_agent);
                setupAgentNavigation();
                break;
            case "viewer":
                setContentView(R.layout.activity_dashboard_viewer);
                setupViewerNavigation();
                break;
            default:
                Log.w(TAG, "Unknown dashboard type: " + dashboardType + ", using agent dashboard");
                setContentView(R.layout.activity_dashboard_agent);
                setupAgentNavigation();
                break;
        }
        
        bottomNavigation = findViewById(R.id.bottom_navigation);
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(navigationItemSelectedListener);
        }
    }
    
    private void setupOrgAdminNavigation() {
        // Organization Admin gets access to all features
        Log.d(TAG, "Setting up Organization Admin navigation");
    }
    
    private void setupManagerNavigation() {
        // Manager gets team management and analytics features
        Log.d(TAG, "Setting up Manager navigation");
    }
    
    private void setupAgentNavigation() {
        // Agent gets individual call tracking and lead management
        Log.d(TAG, "Setting up Agent navigation");
    }
    
    private void setupViewerNavigation() {
        // Viewer gets read-only access to assigned data
        Log.d(TAG, "Setting up Viewer navigation");
    }
    
    private void loadInitialDashboard() {
        Fragment initialFragment = null;
        String dashboardType = permissionManager.getPrimaryDashboardType();
        
        switch (dashboardType) {
            case "org_admin":
                initialFragment = new OrgAdminDashboardFragment();
                break;
            case "manager":
                initialFragment = new ManagerDashboardFragment();
                break;
            case "agent":
                initialFragment = new AgentDashboardFragment();
                break;
            case "viewer":
                initialFragment = new ViewerDashboardFragment();
                break;
        }
        
        if (initialFragment != null) {
            loadFragment(initialFragment);
        } else {
            Log.e(TAG, "Failed to create initial dashboard fragment");
            showError("Failed to load dashboard");
        }
    }
    
    private final NavigationBarView.OnItemSelectedListener navigationItemSelectedListener = 
            item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                
                // Handle navigation based on role and permissions
                if (itemId == R.id.nav_dashboard) {
                    selectedFragment = getDashboardFragment();
                } else if (itemId == R.id.nav_calls) {
                    if (permissionManager.canViewCalls()) {
                        selectedFragment = new CallLogsFragment();
                    } else {
                        showPermissionError("view calls");
                        return false;
                    }
                } else if (itemId == R.id.nav_contacts) {
                    if (permissionManager.canViewContacts()) {
                        selectedFragment = new ContactsFragment();
                    } else {
                        showPermissionError("view contacts");
                        return false;
                    }
                } else if (itemId == R.id.nav_analytics) {
                    if (permissionManager.canViewAnalytics()) {
                        selectedFragment = getAnalyticsFragment();
                    } else {
                        showPermissionError("view analytics");
                        return false;
                    }
                } else if (itemId == R.id.nav_users) {
                    if (permissionManager.canManageUsers()) {
                        selectedFragment = new UserManagementFragment();
                    } else {
                        showPermissionError("manage users");
                        return false;
                    }
                } else if (itemId == R.id.nav_teams) {
                    if (permissionManager.canManageTeams() || permissionManager.canViewTeamAnalytics()) {
                        selectedFragment = new TeamManagementFragment();
                    } else {
                        showPermissionError("manage teams");
                        return false;
                    }
                } else if (itemId == R.id.nav_settings) {
                    selectedFragment = new SettingsFragment();
                }
                
                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }
                
                return false;
            };
    
    private Fragment getDashboardFragment() {
        String dashboardType = permissionManager.getPrimaryDashboardType();
        switch (dashboardType) {
            case "org_admin": return new OrgAdminDashboardFragment();
            case "manager": return new ManagerDashboardFragment();
            case "agent": return new AgentDashboardFragment();
            case "viewer": return new ViewerDashboardFragment();
            default: return new AgentDashboardFragment();
        }
    }
    
    private Fragment getAnalyticsFragment() {
        if (permissionManager.canViewOrgAnalytics()) {
            return new OrganizationAnalyticsFragment();
        } else if (permissionManager.canViewTeamAnalytics()) {
            return new TeamAnalyticsFragment();
        } else {
            return new IndividualAnalyticsFragment();
        }
    }
    
    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        }
    }
    
    private void showOrganizationSelector() {
        try {
            // For now, skip organization selection and use the first organization
            if (currentUser.getOrganizations() != null && !currentUser.getOrganizations().isEmpty()) {
                Organization firstOrg = currentUser.getOrganizations().get(0);
                currentUser.setCurrentOrganization(firstOrg);
                currentUser.setOrganizationId(firstOrg.getId());
                tokenManager.updateUser(currentUser);
                Log.d(TAG, "Auto-selected first organization: " + firstOrg.getName());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling organization selection: " + e.getMessage());
            redirectToLogin();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001) { // Organization selection
            if (resultCode == RESULT_OK && data != null) {
                String selectedOrgId = data.getStringExtra("selected_organization_id");
                if (selectedOrgId != null) {
                    updateCurrentOrganization(selectedOrgId);
                    recreate(); // Restart activity with new organization context
                } else {
                    redirectToLogin();
                }
            } else {
                redirectToLogin();
            }
        }
    }
    
    private void updateCurrentOrganization(String organizationId) {
        // Update user's current organization context
        if (currentUser.getOrganizations() != null) {
            for (Organization org : currentUser.getOrganizations()) {
                if (organizationId.equals(org.getId())) {
                    currentUser.setCurrentOrganization(org);
                    currentUser.setOrganizationId(organizationId);
                    tokenManager.updateUser(currentUser);
                    break;
                }
            }
        }
    }
    
    private void redirectToLogin() {
        Intent intent = new Intent(this, com.calltrackerpro.calltracker.ui.login.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void showPermissionError(String action) {
        String message = "You don't have permission to " + action;
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.w(TAG, "Permission denied: " + action + " for user: " + currentUser.getEmail());
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, message);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Check if token is still valid
        if (!tokenManager.isLoggedIn()) {
            redirectToLogin();
        }
    }
    
    @Override
    public void onBackPressed() {
        // Handle back button - maybe show confirmation dialog for logout
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof AgentDashboardFragment ||
            currentFragment instanceof ManagerDashboardFragment ||
            currentFragment instanceof OrgAdminDashboardFragment ||
            currentFragment instanceof ViewerDashboardFragment) {
            // If on main dashboard, show exit confirmation
            showExitConfirmation();
        } else {
            // Return to dashboard
            bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
        }
    }
    
    private void showExitConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Exit CallTracker Pro")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Exit", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}