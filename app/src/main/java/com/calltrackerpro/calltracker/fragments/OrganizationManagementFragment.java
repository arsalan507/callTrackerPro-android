package com.calltrackerpro.calltracker.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.activities.UnifiedDashboardActivity;
import com.calltrackerpro.calltracker.adapters.OrganizationManagementAdapter;
import com.calltrackerpro.calltracker.models.ApiResponse;
import com.calltrackerpro.calltracker.models.Organization;
import com.calltrackerpro.calltracker.models.User;
import com.calltrackerpro.calltracker.services.ApiService;
import com.calltrackerpro.calltracker.utils.TokenManager;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class OrganizationManagementFragment extends Fragment implements UnifiedDashboardActivity.RefreshableFragment {
    
    private static final String TAG = "OrgManagement";
    
    private RecyclerView recyclerOrganizations;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAddOrganization;
    private TextInputEditText etSearchOrganizations;
    private ChipGroup chipGroupStatus;
    private OrganizationManagementAdapter organizationAdapter;
    
    private ApiService apiService;
    private TokenManager tokenManager;
    private User currentUser;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organization_management, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeComponents();
        setupRecyclerView();
        setupSwipeRefresh();
        setupFloatingActionButton();
        setupSearchAndFilters();
        
        // Load organizations for the first time
        loadOrganizations();
    }
    
    private void initializeComponents() {
        recyclerOrganizations = getView().findViewById(R.id.recyclerOrganizations);
        swipeRefreshLayout = getView().findViewById(R.id.swipeRefreshLayout);
        fabAddOrganization = getView().findViewById(R.id.fabAddOrganization);
        etSearchOrganizations = getView().findViewById(R.id.etSearchOrganizations);
        chipGroupStatus = getView().findViewById(R.id.chipGroupStatus);
        
        // Initialize services
        apiService = ApiService.getInstance();
        tokenManager = new TokenManager(requireContext());
        currentUser = tokenManager.getUser();
    }
    
    private void setupRecyclerView() {
        organizationAdapter = new OrganizationManagementAdapter(requireContext());
        recyclerOrganizations.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerOrganizations.setAdapter(organizationAdapter);
        
        organizationAdapter.setOnOrganizationClickListener(new OrganizationManagementAdapter.OnOrganizationClickListener() {
            @Override
            public void onOrganizationClick(Organization organization) {
                showOrganizationDetails(organization);
            }
            
            @Override
            public void onOrganizationMenuClick(Organization organization, View anchorView) {
                showOrganizationMenu(organization, anchorView);
            }
        });
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadOrganizations);
        swipeRefreshLayout.setColorSchemeResources(
            R.color.primary_color,
            R.color.secondary_color,
            R.color.info_color
        );
    }
    
    private void setupFloatingActionButton() {
        // FAB is hidden by default for organization management
        // Only super admins would be able to create organizations (typically done via web interface)
        fabAddOrganization.setVisibility(View.GONE);
    }
    
    private void setupSearchAndFilters() {
        // Setup search functionality
        etSearchOrganizations.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (organizationAdapter != null) {
                    organizationAdapter.filter(s.toString());
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Setup status filter chips
        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int checkedId = checkedIds.get(0);
            String statusFilter = "all";
            
            if (checkedId == R.id.chipActive) {
                statusFilter = "active";
            } else if (checkedId == R.id.chipTrial) {
                statusFilter = "trial";
            } else if (checkedId == R.id.chipSuspended) {
                statusFilter = "suspended";
            }
            
            if (organizationAdapter != null) {
                organizationAdapter.filterByStatus(statusFilter);
            }
        });
    }
    
    private void loadOrganizations() {
        if (currentUser == null || !currentUser.getRole().equals("super_admin")) {
            showError("Access denied: Super admin role required");
            return;
        }
        
        swipeRefreshLayout.setRefreshing(true);
        
        String authToken = "Bearer " + tokenManager.getToken();
        
        Call<ApiResponse<List<Organization>>> call = apiService.getSuperAdminOrganizations(
            authToken, 1, 100, "all"
        );
        
        call.enqueue(new Callback<ApiResponse<List<Organization>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Organization>>> call, Response<ApiResponse<List<Organization>>> response) {
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Organization>> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        organizationAdapter.setOrganizations(apiResponse.getData());
                        Log.d(TAG, "Loaded " + apiResponse.getData().size() + " organizations");
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? 
                            apiResponse.getMessage() : "Failed to load organizations";
                        showError(errorMsg);
                    }
                } else {
                    String errorMsg = "Error: " + response.code();
                    if (response.message() != null) {
                        errorMsg += " - " + response.message();
                    }
                    showError(errorMsg);
                    Log.e(TAG, "API Error: " + response.code() + " - " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<Organization>>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                showError("Network error: " + t.getMessage());
                Log.e(TAG, "Network error loading organizations", t);
            }
        });
    }
    
    private void showOrganizationDetails(Organization organization) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(organization.getName());
        
        StringBuilder details = new StringBuilder();
        details.append("ID: ").append(organization.getId()).append("\n");
        
        if (organization.getDomain() != null) {
            details.append("Domain: ").append(organization.getDomain()).append("\n");
        }
        
        details.append("Status: ").append(organization.getSubscriptionStatus()).append("\n");
        
        if (organization.getSubscriptionPlan() != null) {
            details.append("Plan: ").append(organization.getSubscriptionPlan()).append("\n");
        }
        
        details.append("Users: ").append(organization.getUserCount()).append("\n");
        details.append("Tickets: ").append(organization.getTicketCount()).append("\n");
        
        if (organization.getCreatedAt() != null) {
            details.append("Created: ").append(organization.getCreatedAt()).append("\n");
        }
        
        if (organization.getLastActivity() != null) {
            details.append("Last Activity: ").append(organization.getLastActivity()).append("\n");
        }
        
        builder.setMessage(details.toString());
        builder.setPositiveButton("OK", null);
        builder.setNeutralButton("View Users", (dialog, which) -> {
            // TODO: Navigate to organization users view
            Toast.makeText(requireContext(), "View organization users - Coming soon", Toast.LENGTH_SHORT).show();
        });
        
        builder.show();
    }
    
    private void showOrganizationMenu(Organization organization, View anchorView) {
        PopupMenu popup = new PopupMenu(requireContext(), anchorView);
        popup.getMenuInflater().inflate(R.menu.menu_organization_management, popup.getMenu());
        
        // Show/hide menu items based on organization status
        if ("active".equals(organization.getSubscriptionStatus())) {
            popup.getMenu().findItem(R.id.action_activate_org).setVisible(false);
            popup.getMenu().findItem(R.id.action_suspend_org).setVisible(true);
        } else {
            popup.getMenu().findItem(R.id.action_activate_org).setVisible(true);
            popup.getMenu().findItem(R.id.action_suspend_org).setVisible(false);
        }
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.action_view_details) {
                showOrganizationDetails(organization);
                return true;
            } else if (itemId == R.id.action_view_users) {
                viewOrganizationUsers(organization);
                return true;
            } else if (itemId == R.id.action_activate_org) {
                activateOrganization(organization);
                return true;
            } else if (itemId == R.id.action_suspend_org) {
                suspendOrganization(organization);
                return true;
            } else if (itemId == R.id.action_view_analytics) {
                viewOrganizationAnalytics(organization);
                return true;
            }
            
            return false;
        });
        
        popup.show();
    }
    
    private void viewOrganizationUsers(Organization organization) {
        // TODO: Navigate to organization-specific user management
        Toast.makeText(requireContext(), 
            "View users for " + organization.getName() + " - Coming soon", 
            Toast.LENGTH_SHORT).show();
    }
    
    private void activateOrganization(Organization organization) {
        // TODO: Implement organization activation
        updateOrganizationStatus(organization, "active", "Organization activated");
    }
    
    private void suspendOrganization(Organization organization) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Suspend Organization")
            .setMessage("Are you sure you want to suspend " + organization.getName() + "?\\n\\nThis will disable access for all users in this organization.")
            .setPositiveButton("Suspend", (dialog, which) -> {
                updateOrganizationStatus(organization, "suspended", "Organization suspended");
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void updateOrganizationStatus(Organization organization, String newStatus, String successMessage) {
        // TODO: Implement API call to update organization status
        String authToken = "Bearer " + tokenManager.getToken();
        
        ApiService.OrganizationStatusRequest request = new ApiService.OrganizationStatusRequest(newStatus);
        
        Call<ApiResponse<Organization>> call = apiService.updateOrganizationStatus(
            authToken, organization.getId(), request
        );
        
        call.enqueue(new Callback<ApiResponse<Organization>>() {
            @Override
            public void onResponse(Call<ApiResponse<Organization>> call, Response<ApiResponse<Organization>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Organization> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        organizationAdapter.updateOrganization(apiResponse.getData());
                        Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show();
                    } else {
                        showError(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Update failed");
                    }
                } else {
                    showError("Error updating organization status");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Organization>> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void viewOrganizationAnalytics(Organization organization) {
        // TODO: Navigate to organization analytics
        Toast.makeText(requireContext(), 
            "Analytics for " + organization.getName() + " - Coming soon", 
            Toast.LENGTH_SHORT).show();
    }
    
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
        Log.e(TAG, message);
    }
    
    @Override
    public void refreshData() {
        if (isAdded() && getView() != null) {
            loadOrganizations();
        }
    }
    
    // Public method to filter organizations (can be called from parent activity)
    public void filterOrganizations(String query) {
        if (organizationAdapter != null) {
            organizationAdapter.filter(query);
        }
    }
    
    // Public method to filter by status
    public void filterByStatus(String status) {
        if (organizationAdapter != null) {
            organizationAdapter.filterByStatus(status);
        }
    }
}