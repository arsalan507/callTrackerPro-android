package com.calltrackerpro.calltracker.services;

import com.calltrackerpro.calltracker.models.ApiResponse;
import com.calltrackerpro.calltracker.models.AuthResponse;
import com.calltrackerpro.calltracker.models.CallLog;
import com.calltrackerpro.calltracker.models.User;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

public interface ApiService {
    // Base URL - Update this to match your backend
    String BASE_URL = "https://calltrackerpro-backend-nsr4t3eyv-arsalan507s-projects.vercel.app/";

    // Authentication endpoints
    @POST("api/auth/register")
    Call<AuthResponse> register(@Body User user);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body User user);

    @POST("api/auth/logout")
    Call<ApiResponse<String>> logout(@Header("Authorization") String token);

    @GET("api/auth/me")
    Call<ApiResponse<User>> getCurrentUser(@Header("Authorization") String token);

    // Call logs endpoints
    @GET("api/call-logs/test")
    Call<ApiResponse<String>> testConnection();

    @POST("api/call-logs")
    Call<ApiResponse<CallLog>> createCallLog(
            @Header("Authorization") String token,
            @Body CallLog callLog
    );

    @GET("api/call-logs")
    Call<ApiResponse<List<CallLog>>> getCallLogs(
            @Header("Authorization") String token
    );

    @GET("api/call-logs/{id}")
    Call<ApiResponse<CallLog>> getCallLog(
            @Header("Authorization") String token,
            @Path("id") String id
    );

    @PUT("api/call-logs/{id}")
    Call<ApiResponse<CallLog>> updateCallLog(
            @Header("Authorization") String token,
            @Path("id") String id,
            @Body CallLog callLog
    );

    @DELETE("api/call-logs/{id}")
    Call<ApiResponse<String>> deleteCallLog(
            @Header("Authorization") String token,
            @Path("id") String id
    );

    @POST("api/call-logs/bulk")
    Call<ApiResponse<List<CallLog>>> bulkSyncCallLogs(
            @Header("Authorization") String token,
            @Body List<CallLog> callLogs
    );

    // Analytics endpoints
    @GET("api/call-logs/analytics/stats")
    Call<ApiResponse<Object>> getCallStats(
            @Header("Authorization") String token
    );

    @GET("api/call-logs/search")
    Call<ApiResponse<List<CallLog>>> searchCallLogs(
            @Header("Authorization") String token,
            @Query("q") String query
    );

    // Contacts endpoints
    @GET("api/contacts")
    Call<ApiResponse<List<Object>>> getContacts(
            @Header("Authorization") String token
    );

    @POST("api/contacts")
    Call<ApiResponse<Object>> createContact(
            @Header("Authorization") String token,
            @Body Object contact
    );
}