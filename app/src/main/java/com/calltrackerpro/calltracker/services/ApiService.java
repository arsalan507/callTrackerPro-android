package com.calltrackerpro.calltracker.services;

import com.calltrackerpro.calltracker.models.AuthResponse;
import com.calltrackerpro.calltracker.models.User;
import com.calltrackerpro.calltracker.models.ApiResponse;
import com.calltrackerpro.calltracker.models.CallLog;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

import java.util.concurrent.TimeUnit;

public interface ApiService {

    // ========== BASE URL - UPDATE THIS TO YOUR BACKEND ==========
    String BASE_URL = "https://calltrackerpro-backend.vercel.app/"; // For Android emulator
    // String BASE_URL = "http://192.168.1.XXX:5000/"; // For real device - replace XXX with your IP

    /**
     * Test API connection - WORKING ✅
     */
    @GET("test")
    Call<ApiResponse<String>> testConnection();

    /**
     * Test call logs endpoint - WORKING ✅
     */
    @GET("call-logs/test")
    Call<ApiResponse<String>> testCallLogs();

    /**
     * Get call logs - WORKING ✅
     */
    @GET("call-logs")
    Call<ApiResponse<java.util.List<CallLog>>> getCallLogs(@Header("Authorization") String token);

    /**
     * Create/sync a call log - WORKING ✅
     */
    @POST("call-logs")
    Call<ApiResponse<CallLog>> createCallLog(@Header("Authorization") String token, @Body CallLog callLog);

    // ========== AUTH ENDPOINTS ==========

    /**
     * Login endpoint - Enhanced for CreateAccount flow ✅
     */
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);

    /**
     * Register endpoint - Enhanced for CreateAccount flow ✅
     */
    @POST("auth/register")
    Call<AuthResponse> register(@Body CreateAccountRequest createAccountRequest);

    /**
     * Logout endpoint - NEEDS BACKEND IMPLEMENTATION ⚠️
     */
    @POST("auth/logout")
    Call<ApiResponse<String>> logout(@Header("Authorization") String token);

    /**
     * Get current user profile - NEEDS BACKEND IMPLEMENTATION ⚠️
     */
    @GET("auth/user")
    Call<ApiResponse<User>> getUser(@Header("Authorization") String token);

    /**
     * Refresh token endpoint - NEEDS BACKEND IMPLEMENTATION ⚠️
     */
    @POST("auth/refresh")
    Call<AuthResponse> refreshToken(@Header("Authorization") String token);

    /**
     * Password reset request - NEEDS BACKEND IMPLEMENTATION ⚠️
     */
    @POST("auth/forgot-password")
    Call<ApiResponse<String>> forgotPassword(@Body ForgotPasswordRequest request);

    /**
     * Verify email endpoint - NEEDS BACKEND IMPLEMENTATION ⚠️
     */
    @POST("auth/verify-email")
    Call<ApiResponse<String>> verifyEmail(@Header("Authorization") String token, @Body VerifyEmailRequest request);

    // ========== RETROFIT CLIENT BUILDER ==========

    class ApiClient {
        private static ApiService instance;

        public static ApiService getInstance() {
            if (instance == null) {
                synchronized (ApiService.class) {
                    if (instance == null) {
                        instance = createApiService();
                    }
                }
            }
            return instance;
        }

        private static ApiService createApiService() {
            // Create logging interceptor for debugging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create OkHttp client with logging
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            // Create Retrofit instance
            return new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService.class);
        }
    }

    // Static method for easy access
    static ApiService getInstance() {
        return ApiClient.getInstance();
    }

    // ========== REQUEST/RESPONSE CLASSES ==========

    // NEW: Create Account Request
    class CreateAccountRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String organizationName;
        private String password;

        public CreateAccountRequest(String firstName, String lastName, String email,
                                    String phone, String organizationName, String password) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
            this.organizationName = organizationName;
            this.password = password;
        }

        // Getters
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getOrganizationName() { return organizationName; }
        public String getPassword() { return password; }

        // Setters
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public void setEmail(String email) { this.email = email; }
        public void setPhone(String phone) { this.phone = phone; }
        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
        public void setPassword(String password) { this.password = password; }
    }

    // NEW: Login Request
    class LoginRequest {
        private String email;
        private String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public void setEmail(String email) { this.email = email; }
        public void setPassword(String password) { this.password = password; }
    }

    // Existing inner classes
    class ForgotPasswordRequest {
        private String email;

        public ForgotPasswordRequest(String email) {
            this.email = email;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    class VerifyEmailRequest {
        private String code;

        public VerifyEmailRequest(String code) {
            this.code = code;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
}