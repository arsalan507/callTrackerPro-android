package com.calltrackerpro.calltracker.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.models.ApiResponse;
import com.calltrackerpro.calltracker.models.Ticket;
import com.calltrackerpro.calltracker.services.ApiService;
import com.calltrackerpro.calltracker.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Popup activity that appears when a ticket is auto-created from a phone call
 * Allows agents to quickly update ticket details
 */
public class TicketPopupActivity extends AppCompatActivity {
    private static final String TAG = "TicketPopupActivity";
    
    private TextView tvTicketId;
    private TextView tvCustomerPhone;
    private TextView tvCallInfo;
    private EditText etNotes;
    private Spinner spinnerPriority;
    private Spinner spinnerStatus;
    private Button btnUpdateTicket;
    private Button btnViewFull;
    private Button btnClose;
    
    private Ticket currentTicket;
    private ApiService apiService;
    private TokenManager tokenManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configure as popup window
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_ticket_popup);
        
        // Make it appear as popup
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);
        
        initializeViews();
        initializeServices();
        loadTicketData();
        setupListeners();
    }
    
    private void initializeViews() {
        tvTicketId = findViewById(R.id.tvTicketId);
        tvCustomerPhone = findViewById(R.id.tvCustomerPhone);
        tvCallInfo = findViewById(R.id.tvCallInfo);
        etNotes = findViewById(R.id.etNotes);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnUpdateTicket = findViewById(R.id.btnUpdateTicket);
        btnViewFull = findViewById(R.id.btnViewFull);
        btnClose = findViewById(R.id.btnClose);
        
        // Setup spinners
        setupPrioritySpinner();
        setupStatusSpinner();
    }
    
    private void initializeServices() {
        apiService = ApiService.getInstance();
        tokenManager = new TokenManager(this);
    }
    
    private void loadTicketData() {
        Intent intent = getIntent();
        String ticketId = intent.getStringExtra("ticketId");
        
        if (ticketId != null) {
            // Create a basic ticket object from the received data
            currentTicket = new Ticket();
            currentTicket.setTicketId(ticketId);
            currentTicket.setPhoneNumber(intent.getStringExtra("customerPhone"));
            currentTicket.setCallType(intent.getStringExtra("callType"));
            currentTicket.setPriority(intent.getStringExtra("priority"));
            currentTicket.setStatus(intent.getStringExtra("status"));
            currentTicket.setContactName(intent.getStringExtra("description"));
            
            populateTicketInfo();
        } else {
            Log.e(TAG, "No ticket data received");
            finish();
        }
    }
    
    private void populateTicketInfo() {
        tvTicketId.setText("Ticket: " + currentTicket.getTicketId());
        tvCustomerPhone.setText("Customer: " + currentTicket.getCustomerPhone());
        
        // Show call information if available
        String callInfo = "Auto-created from call";
        Intent intent = getIntent();
        String callType = intent.getStringExtra("callType");
        int duration = intent.getIntExtra("duration", 0);
        
        if (callType != null) {
            callInfo = String.format("%s call (%d seconds)", 
                callType.equals("inbound") ? "Incoming" : "Outgoing", duration);
        }
        tvCallInfo.setText(callInfo);
        
        // Set current values - use contact name as initial description if available
        String initialNotes = currentTicket.getContactName() != null ? 
                              "Contact: " + currentTicket.getContactName() : "";
        etNotes.setText(initialNotes);
        setSpinnerValue(spinnerPriority, currentTicket.getPriority());
        setSpinnerValue(spinnerStatus, currentTicket.getStatus());
    }
    
    private void setupPrioritySpinner() {
        String[] priorities = {"low", "medium", "high", "urgent"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(adapter);
    }
    
    private void setupStatusSpinner() {
        String[] statuses = {"open", "in_progress", "resolved", "closed"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);
    }
    
    private void setSpinnerValue(Spinner spinner, String value) {
        if (value == null) return;
        
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.equals(adapter.getItem(i))) {
                spinner.setSelection(i);
                break;
            }
        }
    }
    
    private void setupListeners() {
        btnUpdateTicket.setOnClickListener(v -> updateTicket());
        btnViewFull.setOnClickListener(v -> openFullTicketView());
        btnClose.setOnClickListener(v -> finish());
    }
    
    private void updateTicket() {
        String notes = etNotes.getText().toString().trim();
        String priority = spinnerPriority.getSelectedItem().toString();
        String status = spinnerStatus.getSelectedItem().toString();
        
        // Update ticket object with available fields
        currentTicket.setPriority(priority);
        currentTicket.setStatus(status);
        // Note: Notes will be handled separately as the Ticket model structure may vary
        
        // Call API to update
        String authToken = "Bearer " + tokenManager.getToken();
        Call<ApiResponse<Ticket>> call = apiService.updateTicket(authToken, currentTicket.getId(), currentTicket);
        
        call.enqueue(new Callback<ApiResponse<Ticket>>() {
            @Override
            public void onResponse(Call<ApiResponse<Ticket>> call, Response<ApiResponse<Ticket>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Ticket> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(TicketPopupActivity.this, "Ticket updated successfully", Toast.LENGTH_SHORT).show();
                        
                        // Return result
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("updatedTicket", apiResponse.getData());
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                        
                    } else {
                        Toast.makeText(TicketPopupActivity.this, "Failed to update ticket", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TicketPopupActivity.this, "Error updating ticket", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Ticket>> call, Throwable t) {
                Log.e(TAG, "Error updating ticket: " + t.getMessage());
                Toast.makeText(TicketPopupActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void openFullTicketView() {
        Intent intent = new Intent(this, TicketDetailsActivity.class);
        intent.putExtra("ticketId", currentTicket.getId());
        intent.putExtra("mode", "edit");
        startActivity(intent);
        finish();
    }
}