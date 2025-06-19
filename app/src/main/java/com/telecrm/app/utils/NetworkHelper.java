package com.telecrm.app.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.telecrm.app.models.ApiResponse;
import com.telecrm.app.models.CallLog;
import com.telecrm.app.services.ApiService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NetworkHelper {

    private static final String TAG = "NetworkHelper";
    private static ApiService apiService;
    private static Retrofit retrofit;

    // Initialize Retrofit instance
    public static void initialize() {
        if (retrofit == null) {
            // Create logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create OkHttp client
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            // Create Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiService.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
            Log.d(TAG, "🌐 NetworkHelper initialized with base URL: " + ApiService.BASE_URL);
        }
    }

    // Get API service instance
    public static ApiService getApiService() {
        if (apiService == null) {
            initialize();
        }
        return apiService;
    }

    // Check if network is available
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    // Test API connection
    public static void testApiConnection(ApiCallback<String> callback) {
        Log.d(TAG, "🔍 Testing API connection...");

        Call<ApiResponse<String>> call = getApiService().testConnection();
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "✅ API connection successful");
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "❌ API returned error: " + apiResponse.getError());
                        callback.onError("API Error: " + apiResponse.getError());
                    }
                } else {
                    Log.e(TAG, "❌ API response not successful: " + response.code());
                    callback.onError("HTTP Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "❌ API connection failed: " + t.getMessage());
                callback.onError("Connection failed: " + t.getMessage());
            }
        });
    }

    // Send call log to API
    public static void sendCallLog(CallLog callLog, ApiCallback<CallLog> callback) {
        Log.d(TAG, "📤 Sending call log: " + callLog.getPhoneNumber());

        Call<ApiResponse<CallLog>> call = getApiService().createCallLog(callLog);
        call.enqueue(new Callback<ApiResponse<CallLog>>() {
            @Override
            public void onResponse(Call<ApiResponse<CallLog>> call, Response<ApiResponse<CallLog>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<CallLog> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "✅ Call log sent successfully");
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "❌ API returned error: " + apiResponse.getError());
                        callback.onError("API Error: " + apiResponse.getError());
                    }
                } else {
                    Log.e(TAG, "❌ Failed to send call log: " + response.code());
                    callback.onError("HTTP Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CallLog>> call, Throwable t) {
                Log.e(TAG, "❌ Failed to send call log: " + t.getMessage());
                callback.onError("Connection failed: " + t.getMessage());
            }
        });
    }

    // Get all call logs
    public static void getCallLogs(ApiCallback<List<CallLog>> callback) {
        Log.d(TAG, "📥 Fetching call logs...");

        Call<ApiResponse<List<CallLog>>> call = getApiService().getCallLogs();
        call.enqueue(new Callback<ApiResponse<List<CallLog>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CallLog>>> call, Response<ApiResponse<List<CallLog>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<CallLog>> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "✅ Call logs fetched successfully: " + apiResponse.getData().size() + " items");
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "❌ API returned error: " + apiResponse.getError());
                        callback.onError("API Error: " + apiResponse.getError());
                    }
                } else {
                    Log.e(TAG, "❌ Failed to fetch call logs: " + response.code());
                    callback.onError("HTTP Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CallLog>>> call, Throwable t) {
                Log.e(TAG, "❌ Failed to fetch call logs: " + t.getMessage());
                callback.onError("Connection failed: " + t.getMessage());
            }
        });
    }

    // Bulk sync call logs
    public static void bulkSyncCallLogs(List<CallLog> callLogs, ApiCallback<List<CallLog>> callback) {
        Log.d(TAG, "📤 Bulk syncing " + callLogs.size() + " call logs...");

        Call<ApiResponse<List<CallLog>>> call = getApiService().bulkSyncCallLogs(callLogs);
        call.enqueue(new Callback<ApiResponse<List<CallLog>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CallLog>>> call, Response<ApiResponse<List<CallLog>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<CallLog>> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "✅ Bulk sync successful");
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "❌ Bulk sync API error: " + apiResponse.getError());
                        callback.onError("API Error: " + apiResponse.getError());
                    }
                } else {
                    Log.e(TAG, "❌ Bulk sync failed: " + response.code());
                    callback.onError("HTTP Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CallLog>>> call, Throwable t) {
                Log.e(TAG, "❌ Bulk sync failed: " + t.getMessage());
                callback.onError("Connection failed: " + t.getMessage());
            }
        });
    }

    // Callback interface for API operations
    public interface ApiCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }
}