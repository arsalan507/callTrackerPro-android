package com.calltrackerpro.calltracker.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.calltrackerpro.calltracker.CreateAccountActivity;
import com.calltrackerpro.calltracker.activities.UnifiedDashboardActivity;
import com.calltrackerpro.calltracker.MainActivity;
import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.SignupStep1Activity;
import com.calltrackerpro.calltracker.models.AuthResponse;
import com.calltrackerpro.calltracker.models.User;
import com.calltrackerpro.calltracker.services.ApiService;
import com.calltrackerpro.calltracker.utils.RetrofitClient;
import com.calltrackerpro.calltracker.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private LoginViewModel loginViewModel;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private ProgressBar loadingProgressBar;

    // CallTracker Pro components
    private TokenManager tokenManager;
    private ApiService apiService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize CallTracker Pro services
        tokenManager = new TokenManager(this);
        apiService = RetrofitClient.getApiService();

        // Check if user is already logged in
        if (tokenManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        loadingProgressBar = findViewById(R.id.loading);

        // Initialize Create Account TextView (already exists in your layout!)
        TextView tvCreateAccount = findViewById(R.id.tvCreateAccount);

        // Check if email was passed from CreateAccountActivity or SignupStep2Activity
        handlePrefilledEmail();

        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(loginFormState.getUsernameError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult == null) {
                return;
            }
            loadingProgressBar.setVisibility(View.GONE);
            if (loginResult.getError() != null) {
                showLoginFailed(loginResult.getError());
            }
            if (loginResult.getSuccess() != null) {
                updateUiWithUser(loginResult.getSuccess());
            }
            setResult(RESULT_OK);
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        loginButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            attemptLogin();
        });

        // UPDATED: Create Account navigation - Now goes to SignupStep1Activity
        if (tvCreateAccount != null) {
            tvCreateAccount.setOnClickListener(v -> {
                Log.d(TAG, "Create Account clicked - navigating to SignupStep1Activity");
                Intent intent = new Intent(this, SignupStep1Activity.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "tvCreateAccount not found! Check if strings.xml has action_create_account");
        }
        
        // Test Login Button - Uses working credentials
        Button testLoginButton = findViewById(R.id.demo_mode);
        if (testLoginButton != null) {
            testLoginButton.setOnClickListener(v -> {
                Log.d(TAG, "Test Login clicked - using working credentials");
                usernameEditText.setText("anas@anas.com");
                passwordEditText.setText("Anas@1234");
                loadingProgressBar.setVisibility(View.VISIBLE);
                attemptLogin();
            });
        }
    }

    // UPDATED: Handle pre-filled email from both CreateAccountActivity and SignupStep2Activity
    private void handlePrefilledEmail() {
        String prefilledEmail = getIntent().getStringExtra("email");
        if (prefilledEmail != null && !prefilledEmail.isEmpty()) {
            usernameEditText.setText(prefilledEmail);
            passwordEditText.requestFocus(); // Focus on password field

            // Show different messages based on the source
            String source = getIntent().getStringExtra("source");
            if ("signup_success".equals(source)) {
                showToast("Account created successfully! Please log in with your new credentials.");
            } else {
                showToast("Welcome back! Please enter your password.");
            }
        }
    }

    private void attemptLogin() {
        String email = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate input
        if (email.isEmpty()) {
            usernameEditText.setError("Email is required");
            loadingProgressBar.setVisibility(View.GONE);
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            loadingProgressBar.setVisibility(View.GONE);
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            usernameEditText.setError("Please enter a valid email");
            loadingProgressBar.setVisibility(View.GONE);
            return;
        }

        // Create LoginRequest object
        ApiService.LoginRequest loginRequest = new ApiService.LoginRequest(email, password);

        // Make API call to CallTracker Pro backend
        Call<AuthResponse> call = apiService.login(loginRequest);
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                loadingProgressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    if (authResponse.isSuccess()) {
                        handleLoginSuccess(authResponse);
                    } else {
                        handleLoginError(authResponse.getMessage());
                    }
                } else {
                    // Handle different HTTP error codes
                    if (response.code() == 401) {
                        handleLoginError("Invalid email or password. Please try again.");
                    } else if (response.code() == 429) {
                        handleLoginError("Too many login attempts. Please try again later.");
                    } else if (response.code() == 500) {
                        handleLoginError("Server error. Please try again later.");
                    } else {
                        handleLoginError("Login failed. Please check your credentials and try again.");
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                loadingProgressBar.setVisibility(View.GONE);
                
                // Enhanced error logging and handling
                String errorMessage = "Network error. Please check your connection and try again.";
                Log.e(TAG, "Login failed: " + t.getMessage());
                Log.e(TAG, "Error class: " + t.getClass().getSimpleName());
                Log.e(TAG, "Full stack trace: ", t);
                
                // Provide more specific error messages and diagnostics
                if (t instanceof java.net.UnknownHostException) {
                    errorMessage = "Cannot connect to server. Please check your internet connection.";
                    Log.e(TAG, "DNS Resolution failed. Backend URL: https://calltrackerpro-backend.vercel.app");
                    
                    // Run network diagnostics and offer offline mode
                    new Thread(() -> {
                        try {
                            Log.d(TAG, "Running network diagnostics...");
                            boolean hasInternet = com.calltrackerpro.calltracker.utils.NetworkHelper.isNetworkAvailable(LoginActivity.this);
                            Log.d(TAG, "Network available: " + hasInternet);
                            
                            if (hasInternet) {
                                com.calltrackerpro.calltracker.utils.NetworkHelper.testBackendConnectivity();
                                
                                // After DNS failure, suggest offline mode for any email
                                runOnUiThread(() -> {
                                    // Show dialog for any DNS failure during login attempts
                                    showOfflineModeDialog();
                                });
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Diagnostics failed: " + e.getMessage());
                        }
                    }).start();
                    
                } else if (t instanceof java.net.ConnectException) {
                    errorMessage = "Server is unreachable. Please try again later.";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage = "Connection timeout. Please check your internet and try again.";
                }
                
                handleLoginError(errorMessage);
            }
        });
    }

    private void handleLoginSuccess(AuthResponse authResponse) {
        // Save authentication data using TokenManager
        long expiresIn = authResponse.getExpiresIn() > 0 ? authResponse.getExpiresIn() : 86400; // Default 24h
        tokenManager.saveAuthData(
                authResponse.getToken(),
                authResponse.getUser(),
                expiresIn
        );

        // Show success message
        String welcomeMessage = "Welcome " + authResponse.getUser().getFirstName() + "!";
        showToast(welcomeMessage);

        // Update UI with user info
        updateUiWithUser(new LoggedInUserView(authResponse.getUser().getFullName()));

        // Navigate to main activity
        navigateToMain();

        Log.d(TAG, "✅ Login successful for user: " + authResponse.getUser().getEmail());
    }

    private void handleLoginError(String errorMessage) {
        showLoginFailed(R.string.login_failed);
        showToast(errorMessage);

        // Clear password field for security
        passwordEditText.setText("");

        Log.e(TAG, "❌ Login failed: " + errorMessage);
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        showToast(welcome);
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, UnifiedDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void showOfflineModeDialog() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("DNS Resolution Issue")
            .setMessage("Cannot connect to server due to DNS issues.\n\nThis is common with Android emulators.\n\nSuggested fixes:\n1. Cold boot your emulator\n2. Use a physical device\n3. Try demo mode for testing")
            .setPositiveButton("Try Demo Mode", (dialog, which) -> {
                // Create a mock successful login for testing
                createMockLogin();
            })
            .setNegativeButton("Restart Emulator", (dialog, which) -> {
                Toast.makeText(this, "Please cold boot your emulator:\nAVD Manager → Cold Boot Now", Toast.LENGTH_LONG).show();
            })
            .setNeutralButton("Cancel", null)
            .show();
    }
    
    private void createMockLogin() {
        // Create a mock user for testing when backend is unreachable
        Log.d(TAG, "Creating mock login for testing...");
        
        String userEmail = usernameEditText.getText().toString().trim();
        
        com.calltrackerpro.calltracker.models.User mockUser = new com.calltrackerpro.calltracker.models.User();
        mockUser.setId("mock-user-123");
        mockUser.setEmail(userEmail);
        mockUser.setFirstName("Demo");
        mockUser.setLastName("User");
        mockUser.setName("Demo User");
        mockUser.setRole("org_admin");
        mockUser.setOrganizationId("demo-org-123");
        
        com.calltrackerpro.calltracker.models.AuthResponse mockResponse = new com.calltrackerpro.calltracker.models.AuthResponse();
        mockResponse.setSuccess(true);
        mockResponse.setMessage("Mock login successful");
        mockResponse.setToken("mock-token-for-testing");
        mockResponse.setUser(mockUser);
        
        handleLoginSuccess(mockResponse);
        
        Toast.makeText(this, "Using demo mode - limited functionality", Toast.LENGTH_LONG).show();
    }
}