package com.calltrackerpro.calltracker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.ui.login.LoginActivity;
import com.calltrackerpro.calltracker.utils.TokenManager;

public class SettingsFragment extends Fragment {
    
    private TokenManager tokenManager;
    private Button btnLogout;
    private Button btnEditProfile;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenManager = new TokenManager(getContext());
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupClickListeners();
    }
    
    private void initializeViews(View view) {
        btnLogout = view.findViewById(R.id.btnLogout);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
    }
    
    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.calltrackerpro.calltracker.EditProfileActivity.class);
            startActivityForResult(intent, 1001);
        });
    }
    
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void performLogout() {
        // Clear all authentication data
        tokenManager.clearAuthData();
        
        // Show logout message
        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        
        // Navigate to login activity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        // Finish current activity
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == getActivity().RESULT_OK) {
            // Profile was updated successfully
            Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        }
    }
}