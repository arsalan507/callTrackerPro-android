package com.calltrackerpro.calltracker.utils;

import android.util.Log;
import com.calltrackerpro.calltracker.services.ApiService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Create logging interceptor for debugging
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create OkHttp client with timeouts and logging
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

            // Create Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiService.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            Log.d(TAG, "🌐 Retrofit client initialized with base URL: " + ApiService.BASE_URL);
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getClient().create(ApiService.class);
            Log.d(TAG, "📡 ApiService created");
        }
        return apiService;
    }

    // Helper method to create authenticated API service calls
    public static class AuthenticatedApiService {
        private final ApiService apiService;
        private final TokenManager tokenManager;

        public AuthenticatedApiService(TokenManager tokenManager) {
            this.apiService = getApiService();
            this.tokenManager = tokenManager;
        }

        public ApiService getService() {
            return apiService;
        }

        public String getAuthHeader() {
            return tokenManager.getAuthHeader();
        }

        public boolean isAuthenticated() {
            return tokenManager.isLoggedIn();
        }
    }

    // Reset client (useful for testing or switching environments)
    public static void resetClient() {
        retrofit = null;
        apiService = null;
        Log.d(TAG, "🔄 Retrofit client reset");
    }

    // Update base URL (if needed for different environments)
    public static void updateBaseUrl(String newBaseUrl) {
        retrofit = null;
        apiService = null;
        // Note: You'd need to modify ApiService.BASE_URL or create a dynamic solution
        Log.d(TAG, "🔄 Base URL updated to: " + newBaseUrl);
    }
}