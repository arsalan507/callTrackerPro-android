package com.calltrackerpro.calltracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.calltrackerpro.calltracker.activities.UnifiedDashboardActivity;

public class SuperAdminUsersFragment extends Fragment implements UnifiedDashboardActivity.RefreshableFragment {
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // For now, use the regular user management fragment
        // TODO: Create specific super admin users layout
        return new UserManagementFragment().onCreateView(inflater, container, savedInstanceState);
    }
    
    @Override
    public void refreshData() {
        // TODO: Implement super admin users specific refresh logic
    }
}