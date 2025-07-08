package com.calltrackerpro.calltracker;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.calltrackerpro.calltracker.services.ApiService;
import com.calltrackerpro.calltracker.models.AuthResponse;
import com.calltrackerpro.calltracker.utils.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAccountViewModel extends ViewModel {

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<RegistrationResult> registrationResult = new MutableLiveData<>();

    private ApiService apiService;

    public CreateAccountViewModel() {
        // Use your existing RetrofitClient pattern
        apiService = RetrofitClient.getApiService();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<RegistrationResult> getRegistrationResult() {
        return registrationResult;
    }

    public void createAccount(String firstName, String lastName, String email, String phone, String organizationName, String password) {
        isLoading.setValue(true);

        ApiService.CreateAccountRequest request = new ApiService.CreateAccountRequest(
                firstName, lastName, email, phone, organizationName, password
        );
        Call<AuthResponse> call = apiService.register(request);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                isLoading.setValue(false);

                if (response.isSuccessful()) {
                    AuthResponse authResponse = response.body();
                    if (authResponse != null && authResponse.isSuccess()) {
                        registrationResult.setValue(
                                new RegistrationResult.Success("Account created successfully!")
                        );
                    } else {
                        String errorMessage = authResponse != null ?
                                authResponse.getMessage() : "Unknown error occurred";
                        registrationResult.setValue(new RegistrationResult.Error(errorMessage));
                    }
                } else {
                    handleErrorResponse(response.code(), response.message());
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                isLoading.setValue(false);

                String errorMessage;
                if (t instanceof java.net.UnknownHostException ||
                        t instanceof java.net.ConnectException) {
                    errorMessage = "Network error. Please check your connection and try again.";
                } else {
                    Log.e("CreateAccountViewModel", "Unexpected error", t);
                    errorMessage = "An unexpected error occurred. Please try again.";
                }

                registrationResult.setValue(new RegistrationResult.Error(errorMessage));
            }
        });
    }

    private void handleErrorResponse(int code, String message) {
        String errorMessage;
        switch (code) {
            case 400:
                errorMessage = "Invalid email or password format";
                break;
            case 409:
                errorMessage = "This email is already registered";
                break;
            case 422:
                errorMessage = "Please check your input and try again";
                break;
            case 429:
                errorMessage = "Too many requests. Please try again later.";
                break;
            case 500:
                errorMessage = "Server error. Please try again later.";
                break;
            default:
                errorMessage = "Registration failed: " + message;
                break;
        }
        registrationResult.setValue(new RegistrationResult.Error(errorMessage));
    }

    // Sealed class equivalent using abstract class and inheritance
    public static abstract class RegistrationResult {
        public static class Success extends RegistrationResult {
            private String message;

            public Success(String message) {
                this.message = message;
            }

            public String getMessage() {
                return message;
            }
        }

        public static class Error extends RegistrationResult {
            private String message;

            public Error(String message) {
                this.message = message;
            }

            public String getMessage() {
                return message;
            }
        }
    }
}