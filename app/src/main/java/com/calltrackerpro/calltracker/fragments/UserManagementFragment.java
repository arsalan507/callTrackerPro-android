package com.calltrackerpro.calltracker.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.activities.UnifiedDashboardActivity;
import com.calltrackerpro.calltracker.adapters.UserAdapter;
import com.calltrackerpro.calltracker.models.ApiResponse;
import com.calltrackerpro.calltracker.models.User;
import com.calltrackerpro.calltracker.services.ApiService;
import com.calltrackerpro.calltracker.utils.PermissionManager;
import com.calltrackerpro.calltracker.utils.TokenManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class UserManagementFragment extends Fragment implements UnifiedDashboardActivity.RefreshableFragment {
    
    private static final String TAG = "UserManagement";
    
    private RecyclerView recyclerUsers;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAddUser;
    private TextInputEditText etSearchUsers;
    private ChipGroup chipGroupRoles;
    private UserAdapter userAdapter;
    
    private ApiService apiService;
    private TokenManager tokenManager;
    private PermissionManager permissionManager;
    private User currentUser;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_management, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeComponents();
        setupRecyclerView();
        setupSwipeRefresh();
        setupFloatingActionButton();
        setupSearchAndFilters();
        
        // Load users for the first time
        loadUsers();
    }
    
    private void initializeComponents() {
        recyclerUsers = getView().findViewById(R.id.recyclerUsers);
        swipeRefreshLayout = getView().findViewById(R.id.swipeRefreshLayout);
        fabAddUser = getView().findViewById(R.id.fabAddUser);
        etSearchUsers = getView().findViewById(R.id.etSearchUsers);
        chipGroupRoles = getView().findViewById(R.id.chipGroupRoles);
        
        // Initialize services
        apiService = ApiService.getInstance();
        tokenManager = new TokenManager(requireContext());
        currentUser = tokenManager.getUser();
        
        if (currentUser != null) {
            permissionManager = new PermissionManager(currentUser);
        }
    }
    
    private void setupRecyclerView() {
        userAdapter = new UserAdapter(requireContext());
        recyclerUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerUsers.setAdapter(userAdapter);
        
        userAdapter.setOnUserClickListener(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                showUserDetails(user);
            }
            
            @Override
            public void onUserLongClick(User user) {
                showUserActionDialog(user);
            }
        });
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadUsers);
        swipeRefreshLayout.setColorSchemeResources(
            R.color.primary_color,
            R.color.secondary_color,
            R.color.info_color
        );
    }
    
    private void setupFloatingActionButton() {
        // Only show FAB if user can manage users
        if (permissionManager != null && permissionManager.canManageUsers()) {
            fabAddUser.setVisibility(View.VISIBLE);
            fabAddUser.setOnClickListener(v -> showInviteUserDialog());
        } else {
            fabAddUser.setVisibility(View.GONE);
        }
    }
    
    private void setupSearchAndFilters() {
        // Setup search functionality
        etSearchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (userAdapter != null) {
                    userAdapter.filter(s.toString());
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Setup role filter chips
        chipGroupRoles.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int checkedId = checkedIds.get(0);
            String roleFilter = "all";
            
            if (checkedId == R.id.chipAgents) {
                roleFilter = "agent";
            } else if (checkedId == R.id.chipManagers) {
                roleFilter = "manager";
            } else if (checkedId == R.id.chipAdmins) {
                roleFilter = getAdminRoleFilter();
            }
            
            if (userAdapter != null) {
                userAdapter.filterByRole(roleFilter);
            }
        });
    }
    
    private String getAdminRoleFilter() {
        // For super admin, show both org_admin and super_admin
        // For org_admin, only show org_admin
        if (currentUser != null && "super_admin".equals(currentUser.getRole())) {
            return "admin"; // Will be handled specially in adapter
        } else {
            return "org_admin";
        }
    }
    
    private void loadUsers() {
        if (currentUser == null) {
            Log.e(TAG, "Current user is null");
            return;
        }
        
        swipeRefreshLayout.setRefreshing(true);
        
        String authToken = "Bearer " + tokenManager.getToken();
        
        Call<ApiResponse<List<User>>> call;
        
        // Determine which API endpoint to use based on user role
        if ("super_admin".equals(currentUser.getRole())) {
            // Super admin gets all users
            call = apiService.getSuperAdminUsers(authToken, 1, 100, "");
        } else {
            // Other roles get organization users
            call = apiService.getOrganizationUsers(authToken, currentUser.getOrganizationId());
        }
        
        call.enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<User>> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        userAdapter.setUsers(apiResponse.getData());
                        Log.d(TAG, "Loaded " + apiResponse.getData().size() + " users");
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? 
                            apiResponse.getMessage() : "Failed to load users";
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
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                showError("Network error: " + t.getMessage());
                Log.e(TAG, "Network error loading users", t);
            }
        });
    }
    
    private void showUserDetails(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(user.getFullName());
        
        StringBuilder details = new StringBuilder();
        details.append("Email: ").append(user.getEmail()).append("\n");
        details.append("Role: ").append(user.getRoleDisplayName()).append("\n");
        details.append("Status: ").append(user.isActive() ? "Active" : "Inactive").append("\n");
        
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            details.append("Phone: ").append(user.getPhone()).append("\n");
        }
        
        if (user.getLastLogin() != null) {
            details.append("Last Login: ").append(user.getLastLogin()).append("\n");
        }
        
        if (user.getOrganizationId() != null) {
            details.append("Organization: ").append(user.getOrganizationId()).append("\n");
        }
        
        builder.setMessage(details.toString());
        builder.setPositiveButton("OK", null);
        
        // Add action buttons for users with management permissions
        if (permissionManager != null && permissionManager.canManageUsers() && !user.getId().equals(currentUser.getId())) {
            builder.setNeutralButton("Edit", (dialog, which) -> showEditUserDialog(user));
            
            if (user.isActive()) {
                builder.setNegativeButton("Deactivate", (dialog, which) -> deactivateUser(user));
            } else {
                builder.setNegativeButton("Activate", (dialog, which) -> activateUser(user));
            }
        }
        
        builder.show();
    }
    
    private void showUserActionDialog(User user) {
        if (permissionManager == null || !permissionManager.canManageUsers()) {
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("User Actions");
        
        String[] actions;
        if (user.isActive()) {
            actions = new String[]{"View Details", "Edit User", "Deactivate User"};
        } else {
            actions = new String[]{"View Details", "Edit User", "Activate User"};
        }
        
        builder.setItems(actions, (dialog, which) -> {
            switch (which) {
                case 0:
                    showUserDetails(user);
                    break;
                case 1:
                    showEditUserDialog(user);
                    break;
                case 2:
                    if (user.isActive()) {
                        deactivateUser(user);
                    } else {
                        activateUser(user);
                    }
                    break;
            }
        });
        
        builder.show();
    }
    
    private void showEditUserDialog(User user) {
        // TODO: Implement user editing dialog
        Toast.makeText(requireContext(), "Edit user functionality coming soon", Toast.LENGTH_SHORT).show();
    }
    
    private void showInviteUserDialog() {
        // TODO: Implement invite user dialog
        Toast.makeText(requireContext(), "Invite user functionality coming soon", Toast.LENGTH_SHORT).show();
    }
    
    private void deactivateUser(User user) {
        // TODO: Implement user deactivation
        Toast.makeText(requireContext(), "Deactivate user functionality coming soon", Toast.LENGTH_SHORT).show();
    }
    
    private void activateUser(User user) {
        // TODO: Implement user activation
        Toast.makeText(requireContext(), "Activate user functionality coming soon", Toast.LENGTH_SHORT).show();
    }
    
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
        Log.e(TAG, message);
    }
    
    public void refreshUsers() {
        // This method is called from UnifiedDashboardActivity when real-time user events occur
        if (isAdded() && getView() != null) {
            loadUsers();
        }
    }
    
    @Override
    public void refreshData() {
        refreshUsers();
    }
    
    // Public method to filter users (can be called from parent activity)
    public void filterUsers(String query) {
        if (userAdapter != null) {
            userAdapter.filter(query);
        }
    }
    
    // Public method to filter by role
    public void filterByRole(String role) {
        if (userAdapter != null) {
            userAdapter.filterByRole(role);
        }
    }
}