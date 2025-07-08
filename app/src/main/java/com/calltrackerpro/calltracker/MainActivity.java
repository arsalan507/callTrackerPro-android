package com.calltrackerpro.calltracker;

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
import androidx.recyclerview.widget.RecyclerView;
import com.calltrackerpro.calltracker.models.ApiResponse;
import com.calltrackerpro.calltracker.models.CallLog;
import com.calltrackerpro.calltracker.models.User;
import com.calltrackerpro.calltracker.services.ApiService;
import com.calltrackerpro.calltracker.utils.RetrofitClient;
import com.calltrackerpro.calltracker.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CallTracker Pro";
    private static final int PERMISSION_REQUEST_CODE = 100;

    // UI Components - matching your existing layout
    private TextView statusText;
    private TextView callCountText;
    private TextView lastSyncText;
    private Button syncButton;
    private Button viewLogsButton;
    private Button settingsButton;
    private Switch autoSyncSwitch;
    private RecyclerView recentCallsRecycler;

    // Authentication & API
    private TokenManager tokenManager;
    private ApiService apiService;
    private int totalCalls = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize components
        initializeComponents();
        initializeUI();
        checkPermissions();
        updateUI();
    }

    private void initializeComponents() {
        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService();
        Log.d(TAG, "🚀 CallTracker Pro initialized");
    }

    private void initializeUI() {
        // Initialize UI components - matching your layout IDs
        statusText = findViewById(R.id.tvStatus);
        callCountText = findViewById(R.id.tvCallCount);
        lastSyncText = findViewById(R.id.tvLastSync);
        syncButton = findViewById(R.id.btnManualSync);
        viewLogsButton = findViewById(R.id.btnViewLogs);
        settingsButton = findViewById(R.id.btnSettings);
        autoSyncSwitch = findViewById(R.id.switchAutoSync);
        recentCallsRecycler = findViewById(R.id.recyclerRecentCalls);

        // Set click listeners
        syncButton.setOnClickListener(v -> {
            if (tokenManager.isLoggedIn()) {
                syncCallLogs();
            } else {
                testApiConnection(); // Test API if not logged in
            }
        });

        viewLogsButton.setOnClickListener(v -> {
            if (tokenManager.isLoggedIn()) {
                fetchCallLogs();
            } else {
                testCallLogsEndpoint(); // Test call logs endpoint if not logged in
            }
        });

        settingsButton.setOnClickListener(v -> {
            showToast("Settings - Coming soon!");
            // TODO: Open settings activity
        });

        autoSyncSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showToast("Auto-sync " + (isChecked ? "enabled" : "disabled"));
            // TODO: Implement auto-sync functionality
        });
    }

    private void updateUI() {
        User user = tokenManager.getUser();
        if (user != null) {
            // User is logged in
            statusText.setText("✅ Ready - " + user.getFullName() + " (" + user.getRole() + ")");
            syncButton.setText("📤 Sync Call Logs");
            syncButton.setEnabled(true);
            viewLogsButton.setText("📥 Fetch Call Logs");
            viewLogsButton.setEnabled(true);

            // Update call statistics
            callCountText.setText("📞 Total: " + user.getCallCount() + "/" +
                    (user.getCallLimit() == 0 ? "∞" : user.getCallLimit()));
        } else {
            // User not logged in - show as demo mode
            statusText.setText("🔐 Demo Mode - Testing available endpoints");
            syncButton.setText("🔍 Test API Connection");
            syncButton.setEnabled(true);
            viewLogsButton.setText("🧪 Test Call Logs Endpoint");
            viewLogsButton.setEnabled(true);
            callCountText.setText("📞 Total: " + totalCalls);
        }

        // Update last sync time
        updateLastSyncTime();
    }

    private void updateLastSyncTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        lastSyncText.setText("🕐 Last: " + currentTime);
    }

    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG
        };

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "✅ All permissions granted");
        }
    }

    private void testApiConnection() {
        statusText.setText("🔍 Testing main API connection...");
        Log.d(TAG, "Testing API connection to: https://calltrackerpro-backend.vercel.app/api/test");

        Call<ApiResponse<String>> call = apiService.testConnection();
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        statusText.setText("✅ Main API working! Message: " + apiResponse.getData());
                        showToast("✅ API connection successful!");
                        Log.d(TAG, "✅ API test successful: " + apiResponse.getMessage());
                    } else {
                        statusText.setText("❌ API error: " + apiResponse.getErrorMessage());
                        showToast("❌ API error: " + apiResponse.getErrorMessage());
                        Log.e(TAG, "❌ API test failed: " + apiResponse.getErrorMessage());
                    }
                } else {
                    statusText.setText("❌ HTTP error: " + response.code() + " - " + response.message());
                    showToast("❌ HTTP error: " + response.code());
                    Log.e(TAG, "❌ HTTP error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                statusText.setText("❌ Network error: " + t.getMessage());
                showToast("❌ Network error: " + t.getMessage());
                Log.e(TAG, "❌ API test failed: " + t.getMessage());
            }
        });
    }

    private void testCallLogsEndpoint() {
        statusText.setText("🧪 Testing call logs endpoint...");
        Log.d(TAG, "Testing call logs endpoint: https://calltrackerpro-backend.vercel.app/api/call-logs/test");

        Call<ApiResponse<String>> call = apiService.testCallLogs();
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        statusText.setText("✅ Call logs endpoint working! " + apiResponse.getMessage());
                        showToast("✅ Call logs API working!");
                        Log.d(TAG, "✅ Call logs test successful: " + apiResponse.getMessage());
                    } else {
                        statusText.setText("❌ Call logs error: " + apiResponse.getErrorMessage());
                        showToast("❌ Call logs error: " + apiResponse.getErrorMessage());
                        Log.e(TAG, "❌ Call logs test failed: " + apiResponse.getErrorMessage());
                    }
                } else {
                    statusText.setText("❌ HTTP error: " + response.code() + " - " + response.message());
                    showToast("❌ HTTP error: " + response.code());
                    Log.e(TAG, "❌ HTTP error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                statusText.setText("❌ Network error: " + t.getMessage());
                showToast("❌ Network error: " + t.getMessage());
                Log.e(TAG, "❌ Call logs test failed: " + t.getMessage());
            }
        });
    }

    private void syncCallLogs() {
        if (!tokenManager.isLoggedIn()) {
            showToast("Please login first");
            return;
        }

        statusText.setText("📤 Syncing call logs...");

        // Create a test call log
        CallLog testCallLog = new CallLog(
                "+1234567890",
                "outgoing",
                120,
                System.currentTimeMillis()
        );
        testCallLog.setContactName("Test Contact");
        testCallLog.setCallStatus("completed");

        String authHeader = tokenManager.getAuthHeader();
        Call<ApiResponse<CallLog>> call = apiService.createCallLog(authHeader, testCallLog);

        call.enqueue(new Callback<ApiResponse<CallLog>>() {
            @Override
            public void onResponse(Call<ApiResponse<CallLog>> call, Response<ApiResponse<CallLog>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<CallLog> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        statusText.setText("✅ Call log synced successfully");
                        showToast("Call log synced!");
                        totalCalls++;
                        updateUI();
                        Log.d(TAG, "✅ Call log synced: " + apiResponse.getData().getPhoneNumber());
                    } else {
                        statusText.setText("❌ Sync error: " + apiResponse.getErrorMessage());
                        showToast("Sync error: " + apiResponse.getErrorMessage());
                        Log.e(TAG, "❌ Sync failed: " + apiResponse.getErrorMessage());
                    }
                } else {
                    statusText.setText("❌ HTTP error: " + response.code());
                    showToast("HTTP error: " + response.code());
                    Log.e(TAG, "❌ HTTP error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CallLog>> call, Throwable t) {
                statusText.setText("❌ Network error: " + t.getMessage());
                showToast("Network error: " + t.getMessage());
                Log.e(TAG, "❌ Sync failed: " + t.getMessage());
            }
        });
    }

    private void fetchCallLogs() {
        if (!tokenManager.isLoggedIn()) {
            testCallLogsEndpoint(); // Fall back to testing if not logged in
            return;
        }

        statusText.setText("📥 Fetching call logs...");

        String authHeader = tokenManager.getAuthHeader();
        Call<ApiResponse<java.util.List<CallLog>>> call = apiService.getCallLogs(authHeader);

        call.enqueue(new Callback<ApiResponse<java.util.List<CallLog>>>() {
            @Override
            public void onResponse(Call<ApiResponse<java.util.List<CallLog>>> call, Response<ApiResponse<java.util.List<CallLog>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<java.util.List<CallLog>> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        java.util.List<CallLog> callLogs = apiResponse.getData();
                        int count = callLogs != null ? callLogs.size() : 0;
                        statusText.setText("✅ Fetched " + count + " call logs");
                        showToast("Fetched " + count + " call logs");
                        // TODO: Update RecyclerView with call logs
                        Log.d(TAG, "✅ Fetched " + count + " call logs");
                    } else {
                        statusText.setText("❌ Fetch error: " + apiResponse.getErrorMessage());
                        showToast("Fetch error: " + apiResponse.getErrorMessage());
                        Log.e(TAG, "❌ Fetch failed: " + apiResponse.getErrorMessage());
                    }
                } else {
                    statusText.setText("❌ HTTP error: " + response.code());
                    showToast("HTTP error: " + response.code());
                    Log.e(TAG, "❌ HTTP error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<java.util.List<CallLog>>> call, Throwable t) {
                statusText.setText("❌ Network error: " + t.getMessage());
                showToast("Network error: " + t.getMessage());
                Log.e(TAG, "❌ Fetch failed: " + t.getMessage());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Log.d(TAG, "✅ All permissions granted");
                showToast("Permissions granted");
            } else {
                Log.w(TAG, "⚠ Some permissions denied");
                showToast("Some permissions denied - app may not work properly");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
}