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
import com.calltrackerpro.calltracker.MainActivity;
import com.calltrackerpro.calltracker.R;
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
    private Button demoButton;
    // Removed createAccountLink since you already have tvCreateAccount in layout

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
        demoButton = findViewById(R.id.demo_mode);

        // Initialize Create Account TextView (already exists in your layout!)
        TextView tvCreateAccount = findViewById(R.id.tvCreateAccount);

        // Check if email was passed from CreateAccountActivity
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

        demoButton.setOnClickListener(v -> {
            showToast("Entering Demo Mode - Skipping Authentication");
            navigateToMain();
        });

        // Create Account navigation - Using your existing TextView!
        if (tvCreateAccount != null) {
            tvCreateAccount.setOnClickListener(v -> {
                Log.d(TAG, "Create Account clicked - navigating to CreateAccountActivity");
                Intent intent = new Intent(this, CreateAccountActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "tvCreateAccount not found! Check if strings.xml has action_create_account");
        }
    }

    // NEW: Handle pre-filled email from CreateAccountActivity
    private void handlePrefilledEmail() {
        String prefilledEmail = getIntent().getStringExtra("email");
        if (prefilledEmail != null && !prefilledEmail.isEmpty()) {
            usernameEditText.setText(prefilledEmail);
            passwordEditText.requestFocus();
            showToast("Account created! Please log in with your new credentials.");
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

        // ✅ FIXED: Create LoginRequest object instead of User
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
                    handleLoginError("Login failed. Please check your credentials.");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                loadingProgressBar.setVisibility(View.GONE);
                handleLoginError("Network error. Please check your connection.");
                Log.e(TAG, "Login failed: " + t.getMessage());
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
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}