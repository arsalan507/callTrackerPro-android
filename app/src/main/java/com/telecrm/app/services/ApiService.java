package com.telecrm.app.services;

import com.telecrm.app.models.ApiResponse;
import com.telecrm.app.models.CallLog;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

public interface ApiService {

    // Base API endpoint for call logs
    String BASE_URL = "http://192.168.0.107:5000/"; // ✅ Fixed with port 5000

    // Test API connection
    @GET("api/call-logs/test")
    Call<ApiResponse<String>> testConnection();

    // Create a new call log
    @POST("api/call-logs")
    Call<ApiResponse<CallLog>> createCallLog(@Body CallLog callLog);

    // Get all call logs
    @GET("api/call-logs")
    Call<ApiResponse<List<CallLog>>> getCallLogs();

    // Get call logs with pagination
    @GET("api/call-logs")
    Call<ApiResponse<List<CallLog>>> getCallLogs(
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Get call logs with filters
    @GET("api/call-logs")
    Call<ApiResponse<List<CallLog>>> getCallLogs(
            @Query("phoneNumber") String phoneNumber,
            @Query("callType") String callType,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    // Get single call log by ID
    @GET("api/call-logs/{id}")
    Call<ApiResponse<CallLog>> getCallLog(@Path("id") String id);

    // Update call log
    @PUT("api/call-logs/{id}")
    Call<ApiResponse<CallLog>> updateCallLog(@Path("id") String id, @Body CallLog callLog);

    // Delete call log
    @DELETE("api/call-logs/{id}")
    Call<ApiResponse<String>> deleteCallLog(@Path("id") String id);

    // Bulk sync call logs
    @POST("api/call-logs/bulk")
    Call<ApiResponse<List<CallLog>>> bulkSyncCallLogs(@Body List<CallLog> callLogs);

    // Get analytics
    @GET("api/call-logs/analytics/stats")
    Call<ApiResponse<Object>> getAnalytics();

    // Search call logs
    @GET("api/call-logs/search")
    Call<ApiResponse<List<CallLog>>> searchCallLogs(@Query("q") String query);
}