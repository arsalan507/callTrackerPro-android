package com.telecrm.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TeleCRM";
    private static final int PERMISSION_REQUEST_CODE = 100;

    // UI Components
    private Switch switchAutoSync;
    private Button btnManualSync;
    private Button btnViewLogs;
    private Button btnSettings;
    private TextView tvStatus;
    private TextView tvCallCount;
    private TextView tvLastSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "MainActivity onCreate started");

        // Initialize views with error checking
        if (!initializeViews()) {
            Log.e(TAG, "Failed to initialize views - check your layout file!");
            Toast.makeText(this, "Layout error - check logcat", Toast.LENGTH_LONG).show();
            return;
        }

        setupClickListeners();
        checkPermissions();
        updateUI();

        Log.d(TAG, "MainActivity onCreate completed");
    }

    private boolean initializeViews() {
        try {
            switchAutoSync = findViewById(R.id.switchAutoSync);
            btnManualSync = findViewById(R.id.btnManualSync);
            btnViewLogs = findViewById(R.id.btnViewLogs);
            btnSettings = findViewById(R.id.btnSettings);
            tvStatus = findViewById(R.id.tvStatus);
            tvCallCount = findViewById(R.id.tvCallCount);
            tvLastSync = findViewById(R.id.tvLastSync);

            // Check if any views are null
            if (switchAutoSync == null) {
                Log.e(TAG, "switchAutoSync is null - check R.id.switchAutoSync in layout");
                return false;
            }
            if (btnManualSync == null) {
                Log.e(TAG, "btnManualSync is null - check R.id.btnManualSync in layout");
                return false;
            }
            if (tvStatus == null) {
                Log.e(TAG, "tvStatus is null - check R.id.tvStatus in layout");
                return false;
            }
            if (tvCallCount == null) {
                Log.e(TAG, "tvCallCount is null - check R.id.tvCallCount in layout");
                return false;
            }
            if (tvLastSync == null) {
                Log.e(TAG, "tvLastSync is null - check R.id.tvLastSync in layout");
                return false;
            }

            Log.d(TAG, "All views initialized successfully");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            return false;
        }
    }

    private void setupClickListeners() {
        Log.d(TAG, "Setting up click listeners");

        // Auto-sync toggle
        switchAutoSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "Switch toggled: " + isChecked);
            Toast.makeText(this, "Switch clicked: " + isChecked, Toast.LENGTH_SHORT).show();

            if (isChecked) {
                if (hasAllPermissions()) {
                    updateStatus("✅ Auto-sync enabled", true);
                    Toast.makeText(this, "🔄 Auto-sync enabled", Toast.LENGTH_SHORT).show();
                } else {
                    switchAutoSync.setChecked(false);
                    Toast.makeText(this, "⚠️ Permissions needed first", Toast.LENGTH_SHORT).show();
                    requestPermissions();
                }
            } else {
                updateStatus("⏸️ Auto-sync disabled", false);
                Toast.makeText(this, "⏸️ Auto-sync disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Manual sync button
        btnManualSync.setOnClickListener(v -> {
            Log.d(TAG, "Sync button clicked");
            Toast.makeText(this, "🔄 Sync button clicked!", Toast.LENGTH_SHORT).show();

            if (hasAllPermissions()) {
                performTestSync();
            } else {
                Toast.makeText(this, "⚠️ Need permissions first", Toast.LENGTH_SHORT).show();
                requestPermissions();
            }
        });

        // View logs button
        btnViewLogs.setOnClickListener(v -> {
            Log.d(TAG, "View logs button clicked");
            Toast.makeText(this, "📋 View Logs clicked - Coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Settings button
        btnSettings.setOnClickListener(v -> {
            Log.d(TAG, "Settings button clicked");
            Toast.makeText(this, "⚙️ Settings clicked - Coming soon!", Toast.LENGTH_SHORT).show();
        });

        Log.d(TAG, "Click listeners setup completed");
    }

    private void performTestSync() {
        Log.d(TAG, "Starting test sync");
        updateStatus("🔄 Testing sync...", true);
        btnManualSync.setEnabled(false);

        // Simulate a sync operation
        btnManualSync.postDelayed(() -> {
            updateStatus("✅ Test sync successful!", true);
            tvCallCount.setText("📞 Total Calls: 1");
            tvLastSync.setText("🕐 Last Sync: Just now");
            btnManualSync.setEnabled(true);
            Toast.makeText(this, "🎉 Test sync completed!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Test sync completed");
        }, 2000); // 2 second delay
    }

    private void checkPermissions() {
        Log.d(TAG, "Checking permissions");
        if (hasAllPermissions()) {
            updateStatus("✅ All permissions granted", true);
            Log.d(TAG, "All permissions granted");
        } else {
            updateStatus("❌ Some permissions denied", false);
            Log.d(TAG, "Some permissions missing");
        }
    }

    private boolean hasAllPermissions() {
        String[] permissions = {
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG
        };

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Missing permission: " + permission);
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        Log.d(TAG, "Requesting permissions");
        String[] permissions = {
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG
        };

        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "Permission result received");

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                updateStatus("✅ All permissions granted", true);
                Toast.makeText(this, "🎉 Permissions granted!", Toast.LENGTH_LONG).show();
                Log.d(TAG, "All permissions granted");
            } else {
                updateStatus("❌ Some permissions denied", false);
                Toast.makeText(this, "⚠️ Some permissions denied", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Some permissions denied");
            }

            updateUI();
        }
    }

    private void updateStatus(String status, boolean isPositive) {
        if (tvStatus != null) {
            tvStatus.setText(status);
            tvStatus.setTextColor(isPositive ?
                    ContextCompat.getColor(this, android.R.color.holo_green_dark) :
                    ContextCompat.getColor(this, android.R.color.holo_red_dark));
            Log.d(TAG, "Status updated: " + status);
        }
    }

    private void updateUI() {
        Log.d(TAG, "Updating UI");

        if (tvCallCount != null) {
            tvCallCount.setText("📞 Total Calls: 0");
        }
        if (tvLastSync != null) {
            tvLastSync.setText("🕐 Last Sync: Never");
        }

        boolean hasPermissions = hasAllPermissions();
        if (switchAutoSync != null) {
            switchAutoSync.setEnabled(hasPermissions);
        }
        if (btnManualSync != null) {
            btnManualSync.setEnabled(hasPermissions);
        }

        Log.d(TAG, "UI update completed, permissions: " + hasPermissions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        checkPermissions();
        updateUI();
    }
}