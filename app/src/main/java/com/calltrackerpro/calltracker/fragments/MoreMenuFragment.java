package com.calltrackerpro.calltracker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.activities.UnifiedDashboardActivity;
import com.calltrackerpro.calltracker.models.User;
import com.calltrackerpro.calltracker.utils.TokenManager;

public class MoreMenuFragment extends Fragment implements UnifiedDashboardActivity.RefreshableFragment {
    
    private TokenManager tokenManager;
    private User currentUser;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Create a simple layout programmatically for now
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        
        tokenManager = new TokenManager(requireContext());
        currentUser = tokenManager.getUser();
        
        // Title
        TextView title = new TextView(requireContext());
        title.setText("More Options");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 32);
        layout.addView(title);
        
        // Add menu items based on role
        if (currentUser != null) {
            if (currentUser.getRole().equals("super_admin")) {
                addMenuItem(layout, "Organizations", () -> loadOrganizationsFragment());
                addMenuItem(layout, "All Users", () -> loadAllUsersFragment());
            }
            
            if (currentUser.isOrganizationAdmin() || currentUser.isManager()) {
                addMenuItem(layout, "Users", () -> loadUsersFragment());
                addMenuItem(layout, "Teams", () -> loadTeamsFragment());
                addMenuItem(layout, "Reports", () -> loadReportsFragment());
            }
            
            // Common items
            addMenuItem(layout, "Settings", () -> loadSettingsFragment());
            addMenuItem(layout, "Help & Support", () -> loadHelpFragment());
            addMenuItem(layout, "About", () -> loadAboutFragment());
        }
        
        return layout;
    }
    
    private void addMenuItem(LinearLayout parent, String title, Runnable onClick) {
        TextView menuItem = new TextView(requireContext());
        menuItem.setText(title);
        menuItem.setTextSize(18);
        menuItem.setPadding(16, 16, 16, 16);
        menuItem.setBackgroundResource(android.R.drawable.list_selector_background);
        menuItem.setClickable(true);
        menuItem.setOnClickListener(v -> onClick.run());
        
        parent.addView(menuItem);
    }
    
    private void loadOrganizationsFragment() {
        replaceFragment(new OrganizationManagementFragment());
    }
    
    private void loadAllUsersFragment() {
        // TODO: Navigate to all users management
        replaceFragment(new UserManagementFragment());
    }
    
    private void loadUsersFragment() {
        replaceFragment(new UserManagementFragment());
    }
    
    private void loadTeamsFragment() {
        replaceFragment(new TeamManagementFragment());
    }
    
    private void loadReportsFragment() {
        replaceFragment(new OrganizationAnalyticsFragment());
    }
    
    private void loadSettingsFragment() {
        replaceFragment(new SettingsFragment());
    }
    
    private void loadHelpFragment() {
        // TODO: Create help fragment
        replaceFragment(new SettingsFragment());
    }
    
    private void loadAboutFragment() {
        // TODO: Create about fragment
        replaceFragment(new SettingsFragment());
    }
    
    private void replaceFragment(Fragment fragment) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        }
    }
    
    @Override
    public void refreshData() {
        // Nothing to refresh for static menu
    }
}