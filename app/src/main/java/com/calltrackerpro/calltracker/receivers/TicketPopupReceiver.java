package com.calltrackerpro.calltracker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.calltrackerpro.calltracker.activities.TicketPopupActivity;
import com.calltrackerpro.calltracker.models.Ticket;

/**
 * Broadcast receiver that displays ticket popup when tickets are auto-created from calls
 */
public class TicketPopupReceiver extends BroadcastReceiver {
    private static final String TAG = "TicketPopupReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received broadcast: " + action);
        
        if ("com.calltrackerpro.SHOW_TICKET_POPUP".equals(action)) {
            // Get essential ticket data as strings instead of full object
            String ticketId = intent.getStringExtra("ticketId");
            String customerPhone = intent.getStringExtra("customerPhone");
            String callType = intent.getStringExtra("callType");
            String priority = intent.getStringExtra("priority");
            String status = intent.getStringExtra("status");
            String description = intent.getStringExtra("description");
            String mode = intent.getStringExtra("mode");
            
            if (ticketId != null) {
                // Launch ticket popup activity
                Intent popupIntent = new Intent(context, TicketPopupActivity.class);
                popupIntent.putExtra("ticketId", ticketId);
                popupIntent.putExtra("customerPhone", customerPhone);
                popupIntent.putExtra("callType", callType);
                popupIntent.putExtra("priority", priority);
                popupIntent.putExtra("status", status);
                popupIntent.putExtra("description", description);
                popupIntent.putExtra("mode", mode);
                popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                
                try {
                    context.startActivity(popupIntent);
                    Log.d(TAG, "Launched ticket popup for: " + ticketId);
                } catch (Exception e) {
                    Log.e(TAG, "Error launching ticket popup: " + e.getMessage());
                }
            } else {
                Log.w(TAG, "No ticket data received in popup broadcast");
            }
        }
    }
}