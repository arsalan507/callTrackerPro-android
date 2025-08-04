package com.calltrackerpro.calltracker.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.calltrackerpro.calltracker.models.ApiResponse;
import com.calltrackerpro.calltracker.models.Ticket;
import com.calltrackerpro.calltracker.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Call Service that integrates with the new automatic ticket creation system
 */
public class EnhancedCallService extends Service {
    private static final String TAG = "EnhancedCallService";
    
    public static final String ACTION_CALL_ENDED = "com.calltrackerpro.CALL_ENDED";
    public static final String ACTION_CALL_STARTED = "com.calltrackerpro.CALL_STARTED";
    public static final String ACTION_SHOW_TICKET_POPUP = "com.calltrackerpro.SHOW_TICKET_POPUP";
    
    private ApiService apiService;
    private TokenManager tokenManager;
    
    @Override
    public void onCreate() {
        super.onCreate();
        apiService = ApiService.getInstance();
        tokenManager = new TokenManager(this);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            
            if (ACTION_CALL_ENDED.equals(action)) {
                handleCallEnded(intent);
            } else if (ACTION_CALL_STARTED.equals(action)) {
                handleCallStarted(intent);
            }
        }
        
        return START_NOT_STICKY;
    }
    
    private void handleCallStarted(Intent intent) {
        String phoneNumber = intent.getStringExtra("phoneNumber");
        String callType = intent.getStringExtra("callType"); // "inbound" or "outbound"
        
        Log.d(TAG, "Call started: " + phoneNumber + " (" + callType + ")");
        
        // Get call history for this number to show previous interactions
        if (phoneNumber != null && tokenManager.isLoggedIn()) {
            fetchCallHistory(phoneNumber);
        }
    }
    
    private void handleCallEnded(Intent intent) {
        String phoneNumber = intent.getStringExtra("phoneNumber");
        String callType = intent.getStringExtra("callType"); // "inbound" or "outbound"
        int duration = intent.getIntExtra("duration", 0);
        String status = intent.getStringExtra("status"); // "completed", "missed", "busy"
        String contactName = intent.getStringExtra("contactName");
        
        Log.d(TAG, "Call ended: " + phoneNumber + " (" + callType + ") - " + duration + "s - " + status);
        
        // Create call log with automatic ticket creation
        if (phoneNumber != null && tokenManager.isLoggedIn()) {
            createCallLogWithTicket(phoneNumber, callType, duration, status, contactName);
        }
    }
    
    private void createCallLogWithTicket(String phoneNumber, String callType, int duration, String status, String contactName) {
        String authToken = "Bearer " + tokenManager.getToken();
        
        // Create request with automatic ticket creation enabled
        ApiService.CreateCallLogRequest request = new ApiService.CreateCallLogRequest(
            phoneNumber,
            callType, // type parameter
            duration,
            status,
            contactName != null ? contactName : "Unknown", // callerName parameter
            "", // notes parameter
            true // autoCreateTicket parameter
        );
        
        Call<ApiResponse<ApiService.CallLogWithTicketResponse>> call = 
            apiService.createCallLogWithTicket(authToken, request);
        
        call.enqueue(new Callback<ApiResponse<ApiService.CallLogWithTicketResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ApiService.CallLogWithTicketResponse>> call, 
                                   Response<ApiResponse<ApiService.CallLogWithTicketResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ApiService.CallLogWithTicketResponse> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        ApiService.CallLogWithTicketResponse data = apiResponse.getData();
                        
                        Log.d(TAG, "Call logged successfully");
                        
                        // If ticket was auto-created, show ticket popup
                        if (data.getTicket() != null) {
                            showTicketPopup(data.getTicket());
                        }
                        
                        // Broadcast success for UI updates
                        Intent broadcastIntent = new Intent("com.calltrackerpro.CALL_LOGGED");
                        // Call log info
                        if (data.getCall_log() != null) {
                            broadcastIntent.putExtra("callLogId", data.getCall_log().getId());
                            broadcastIntent.putExtra("phoneNumber", data.getCall_log().getPhoneNumber());
                            broadcastIntent.putExtra("callType", data.getCall_log().getCallType());
                            broadcastIntent.putExtra("duration", data.getCall_log().getDuration());
                        }
                        // Ticket info (if ticket was created)
                        if (data.getTicket() != null) {
                            broadcastIntent.putExtra("ticketCreated", true);
                            broadcastIntent.putExtra("ticketId", data.getTicket().getTicketId());
                        } else {
                            broadcastIntent.putExtra("ticketCreated", false);
                        }
                        sendBroadcast(broadcastIntent);
                        
                    } else {
                        Log.e(TAG, "Failed to log call: " + apiResponse.getMessage());
                        Toast.makeText(EnhancedCallService.this, "Failed to log call", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "API Error: " + response.code() + " - " + response.message());
                    Toast.makeText(EnhancedCallService.this, "Error logging call", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<ApiService.CallLogWithTicketResponse>> call, Throwable t) {
                Log.e(TAG, "Network error logging call: " + t.getMessage());
                Toast.makeText(EnhancedCallService.this, "Network error logging call", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void fetchCallHistory(String phoneNumber) {
        String authToken = "Bearer " + tokenManager.getToken();
        
        Call<ApiResponse<ApiService.CallHistoryResponse>> call = 
            apiService.getCallHistory(authToken, phoneNumber);
        
        call.enqueue(new Callback<ApiResponse<ApiService.CallHistoryResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ApiService.CallHistoryResponse>> call, 
                                   Response<ApiResponse<ApiService.CallHistoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ApiService.CallHistoryResponse> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        ApiService.CallHistoryResponse data = apiResponse.getData();
                        
                        Log.d(TAG, "Retrieved call history for " + phoneNumber + 
                              ": " + data.getCall_history().size() + " previous calls, " +
                              data.getRelated_tickets().size() + " related tickets");
                        
                        // Broadcast call history for UI display
                        Intent broadcastIntent = new Intent("com.calltrackerpro.CALL_HISTORY_RECEIVED");
                        broadcastIntent.putExtra("phoneNumber", phoneNumber);
                        // Contact info (if available)
                        if (data.getContact() != null) {
                            broadcastIntent.putExtra("contactExists", true);
                            broadcastIntent.putExtra("contactName", data.getContact().getFullName());
                            broadcastIntent.putExtra("contactStatus", data.getContact().getStatus());
                            broadcastIntent.putExtra("contactCompany", data.getContact().getCompany());
                        } else {
                            broadcastIntent.putExtra("contactExists", false);
                        }
                        broadcastIntent.putExtra("callHistoryCount", data.getCall_history().size());
                        broadcastIntent.putExtra("relatedTicketsCount", data.getRelated_tickets().size());
                        sendBroadcast(broadcastIntent);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<ApiService.CallHistoryResponse>> call, Throwable t) {
                Log.e(TAG, "Error fetching call history: " + t.getMessage());
            }
        });
    }
    
    private void showTicketPopup(Ticket ticket) {
        Log.d(TAG, "Auto-created ticket: " + ticket.getTicketId());
        
        // Send broadcast to display ticket popup with essential data
        Intent popupIntent = new Intent("com.calltrackerpro.SHOW_TICKET_POPUP");
        popupIntent.putExtra("ticketId", ticket.getTicketId());
        popupIntent.putExtra("customerPhone", ticket.getPhoneNumber());
        popupIntent.putExtra("callType", ticket.getCallType());
        popupIntent.putExtra("priority", ticket.getPriority());
        popupIntent.putExtra("status", ticket.getStatus());
        popupIntent.putExtra("description", ticket.getContactName() != null ? ticket.getContactName() : "Auto-created from call");
        popupIntent.putExtra("mode", "auto_created");
        
        try {
            sendBroadcast(popupIntent);
            Toast.makeText(this, "Ticket created: " + ticket.getTicketId(), Toast.LENGTH_LONG).show();
            Log.d(TAG, "Sent ticket popup broadcast for: " + ticket.getTicketId());
        } catch (Exception e) {
            Log.e(TAG, "Error showing ticket popup: " + e.getMessage());
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}