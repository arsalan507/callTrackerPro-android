package com.calltrackerpro.calltracker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.activities.TicketDetailsActivity;
import com.calltrackerpro.calltracker.adapters.CallLogsAdapter;
import com.calltrackerpro.calltracker.models.ApiResponse;
import com.calltrackerpro.calltracker.models.CallLog;
import com.calltrackerpro.calltracker.models.Ticket;
import com.calltrackerpro.calltracker.models.User;
import com.calltrackerpro.calltracker.services.ApiService;
import com.calltrackerpro.calltracker.services.TicketService;
import com.calltrackerpro.calltracker.utils.PermissionManager;
import com.calltrackerpro.calltracker.utils.TokenManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallLogsFragment extends Fragment implements CallLogsAdapter.OnCallLogClickListener {
    private static final String TAG = "CallLogsFragment";
    
    // UI Components
    private RecyclerView recyclerView;
    private CallLogsAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabCreateTicket;
    private View emptyView;
    
    // Services and Data
    private ApiService apiService;
    private TicketService ticketService;
    private TokenManager tokenManager;
    private PermissionManager permissionManager;
    private User currentUser;
    
    private List<CallLog> callLogsList = new ArrayList<>();
    private boolean isLoading = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        apiService = ApiService.getInstance();
        ticketService = new TicketService(getContext());
        tokenManager = new TokenManager(getContext());
        
        // Get current user
        currentUser = getCurrentUser();
        if (currentUser != null) {
            permissionManager = new PermissionManager(currentUser);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_call_logs, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupFab();
        
        loadCallLogs();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_call_logs);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_call_logs);
        fabCreateTicket = view.findViewById(R.id.fab_create_ticket);
        emptyView = view.findViewById(R.id.layout_empty_call_logs);
    }

    private void setupRecyclerView() {
        adapter = new CallLogsAdapter(callLogsList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            callLogsList.clear();
            loadCallLogs();
        });
    }

    private void setupFab() {
        // Show FAB only if user has permission to create tickets
        if (permissionManager != null && permissionManager.canCreateContacts()) {
            fabCreateTicket.setVisibility(View.VISIBLE);
            fabCreateTicket.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), TicketDetailsActivity.class);
                intent.putExtra("mode", "create");
                startActivity(intent);
            });
        } else {
            fabCreateTicket.setVisibility(View.GONE);
        }
    }

    private void loadCallLogs() {
        if (isLoading) return;
        
        isLoading = true;
        swipeRefreshLayout.setRefreshing(true);
        
        String token = "Bearer " + tokenManager.getToken();
        
        Call<ApiResponse<List<CallLog>>> call = apiService.getCallLogs(token);
        call.enqueue(new Callback<ApiResponse<List<CallLog>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CallLog>>> call, Response<ApiResponse<List<CallLog>>> response) {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<CallLog>> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        callLogsList.clear();
                        callLogsList.addAll(apiResponse.getData());
                        adapter.notifyDataSetChanged();
                        updateEmptyView();
                        
                        Log.d(TAG, "Loaded " + callLogsList.size() + " call logs");
                    } else {
                        Log.e(TAG, "API error loading call logs: " + apiResponse.getMessage());
                        Toast.makeText(getContext(), "Error: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String error = "HTTP " + response.code() + ": " + response.message();
                    Log.e(TAG, "Failed to load call logs: " + error);
                    Toast.makeText(getContext(), "Failed to load call logs", Toast.LENGTH_SHORT).show();
                }
                
                updateEmptyView();
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<CallLog>>> call, Throwable t) {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
                
                Log.e(TAG, "Network error loading call logs", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                
                updateEmptyView();
            }
        });
    }

    private void updateEmptyView() {
        if (callLogsList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCallLogClick(CallLog callLog) {
        // Show options dialog: View Details, Create Ticket, Call Back
        showCallLogOptionsDialog(callLog);
    }

    @Override
    public void onCallLogLongClick(CallLog callLog) {
        // Quick create ticket from long press
        if (permissionManager != null && permissionManager.canCreateContacts()) {
            createTicketFromCallLog(callLog);
        } else {
            Toast.makeText(getContext(), "You don't have permission to create tickets", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCallLogOptionsDialog(CallLog callLog) {
        String[] options;
        if (permissionManager != null && permissionManager.canCreateContacts()) {
            options = new String[]{"View Details", "Create Ticket", "Call Back", "Send SMS"};
        } else {
            options = new String[]{"View Details", "Call Back", "Send SMS"};
        }
        
        new MaterialAlertDialogBuilder(getContext())
            .setTitle("Call with " + (callLog.getContactName() != null ? callLog.getContactName() : callLog.getPhoneNumber()))
            .setItems(options, (dialog, which) -> {
                if (permissionManager != null && permissionManager.canCreateContacts()) {
                    switch (which) {
                        case 0: // View Details
                            showCallLogDetails(callLog);
                            break;
                        case 1: // Create Ticket
                            createTicketFromCallLog(callLog);
                            break;
                        case 2: // Call Back
                            callPhoneNumber(callLog.getPhoneNumber());
                            break;
                        case 3: // Send SMS
                            sendSms(callLog.getPhoneNumber());
                            break;
                    }
                } else {
                    switch (which) {
                        case 0: // View Details
                            showCallLogDetails(callLog);
                            break;
                        case 1: // Call Back
                            callPhoneNumber(callLog.getPhoneNumber());
                            break;
                        case 2: // Send SMS
                            sendSms(callLog.getPhoneNumber());
                            break;
                    }
                }
            })
            .show();
    }

    private void showCallLogDetails(CallLog callLog) {
        String details = "Contact: " + (callLog.getContactName() != null ? callLog.getContactName() : "Unknown") + "\n" +
                        "Phone: " + callLog.getPhoneNumber() + "\n" +
                        "Type: " + formatCallType(callLog.getCallType()) + "\n" +
                        "Duration: " + callLog.getFormattedDuration() + "\n" +
                        "Date: " + formatTimestamp(callLog.getTimestamp()) + "\n" +
                        "Status: " + formatCallStatus(callLog.getCallStatus());
        
        new MaterialAlertDialogBuilder(getContext())
            .setTitle("Call Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .setNeutralButton("Create Ticket", (dialog, which) -> {
                if (permissionManager != null && permissionManager.canCreateContacts()) {
                    createTicketFromCallLog(callLog);
                }
            })
            .show();
    }

    private void createTicketFromCallLog(CallLog callLog) {
        Log.d(TAG, "Creating ticket from call log: " + callLog.getPhoneNumber());
        
        // Create ticket object from call log
        Ticket ticket = new Ticket();
        ticket.setPhoneNumber(callLog.getPhoneNumber());
        ticket.setContactName(callLog.getContactName() != null ? callLog.getContactName() : callLog.getPhoneNumber());
        ticket.setCallType(callLog.getCallType());
        ticket.setCallDuration(callLog.getDuration());
        ticket.setCallDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date(callLog.getTimestamp())));
        ticket.setCallLogId(callLog.getId());
        
        // Set default CRM values
        ticket.setLeadSource("phone_call");
        ticket.setLeadStatus("new");
        ticket.setStage("prospect");
        ticket.setPriority("medium");
        ticket.setInterestLevel("warm");
        
        // Set organization context
        if (currentUser != null) {
            ticket.setOrganizationId(currentUser.getOrganizationId());
            ticket.setAssignedAgent(currentUser.getId());
            ticket.setCreatedBy(currentUser.getId());
        }
        
        // Show loading
        swipeRefreshLayout.setRefreshing(true);
        
        ticketService.createTicket(ticket, new TicketService.TicketCallback<Ticket>() {
            @Override
            public void onSuccess(Ticket createdTicket) {
                swipeRefreshLayout.setRefreshing(false);
                
                Log.d(TAG, "Ticket created successfully: " + createdTicket.getTicketId());
                Toast.makeText(getContext(), "Ticket created successfully", Toast.LENGTH_SHORT).show();
                
                // Open ticket details
                Intent intent = new Intent(getContext(), TicketDetailsActivity.class);
                intent.putExtra("ticketId", createdTicket.getId());
                intent.putExtra("mode", "view");
                startActivity(intent);
            }
            
            @Override
            public void onError(String error) {
                swipeRefreshLayout.setRefreshing(false);
                
                Log.e(TAG, "Failed to create ticket: " + error);
                Toast.makeText(getContext(), "Failed to create ticket: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void callPhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        }
    }

    private void sendSms(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(android.net.Uri.parse("smsto:" + phoneNumber));
            startActivity(intent);
        }
    }

    private String formatCallType(String callType) {
        if (callType == null) return "Unknown";
        switch (callType.toLowerCase()) {
            case "incoming": return "Incoming";
            case "outgoing": return "Outgoing";
            case "missed": return "Missed";
            default: return callType;
        }
    }

    private String formatCallStatus(String status) {
        if (status == null) return "Unknown";
        switch (status.toLowerCase()) {
            case "completed": return "Completed";
            case "missed": return "Missed";
            case "declined": return "Declined";
            case "busy": return "Busy";
            default: return status;
        }
    }

    private String formatTimestamp(long timestamp) {
        if (timestamp <= 0) return "Unknown";
        
        try {
            Date date = new Date(timestamp);
            SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            return format.format(date);
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private User getCurrentUser() {
        // TODO: Get current user from TokenManager or shared preferences
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh call logs when fragment becomes visible
        if (adapter != null) {
            callLogsList.clear();
            loadCallLogs();
        }
    }

    public void refreshCallLogs() {
        callLogsList.clear();
        loadCallLogs();
    }
}