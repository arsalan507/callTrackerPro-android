package com.calltrackerpro.calltracker.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.calltrackerpro.calltracker.DashboardRouterActivity;
import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.models.Ticket;
import com.calltrackerpro.calltracker.utils.TokenManager;
import com.calltrackerpro.calltracker.utils.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CallReceiverService extends Service {
    private static final String TAG = "CallReceiverService";
    private static final String CHANNEL_ID = "CallReceiverChannel";
    private static final int NOTIFICATION_ID = 1001;

    // Action constants
    public static final String ACTION_START_SERVICE = "START_SERVICE";
    public static final String ACTION_CALL_RINGING = "CALL_RINGING";
    public static final String ACTION_CALL_ANSWERED = "CALL_ANSWERED";
    public static final String ACTION_CALL_ENDED = "CALL_ENDED";
    public static final String ACTION_OUTGOING_CALL = "OUTGOING_CALL";

    private Handler handler;
    private Map<String, CallSession> activeCalls = new HashMap<>();
    private TicketService ticketService;
    private TokenManager tokenManager;
    private PreferenceManager preferenceManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        
        handler = new Handler(Looper.getMainLooper());
        ticketService = new TicketService(this);
        tokenManager = new TokenManager(this);
        preferenceManager = new PreferenceManager(this);
        
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification("CallTracker Pro is monitoring calls"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            String phoneNumber = intent.getStringExtra("phoneNumber");
            String callType = intent.getStringExtra("callType");
            
            Log.d(TAG, "Service command: " + action + ", Phone: " + phoneNumber + ", Type: " + callType);

            switch (action) {
                case ACTION_START_SERVICE:
                    Log.d(TAG, "Service started and ready to monitor calls");
                    break;
                    
                case ACTION_CALL_RINGING:
                    handleCallRinging(phoneNumber, callType);
                    break;
                    
                case ACTION_CALL_ANSWERED:
                    handleCallAnswered(phoneNumber, callType);
                    break;
                    
                case ACTION_CALL_ENDED:
                    String callStatus = intent.getStringExtra("callStatus");
                    handleCallEnded(phoneNumber, callType, callStatus);
                    break;
                    
                case ACTION_OUTGOING_CALL:
                    handleOutgoingCall(phoneNumber);
                    break;
            }
        }

        return START_STICKY; // Service will be restarted if killed
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleCallRinging(String phoneNumber, String callType) {
        Log.d(TAG, "Call ringing: " + phoneNumber);
        
        CallSession session = new CallSession(phoneNumber, callType);
        activeCalls.put(phoneNumber, session);
        
        // Resolve contact name
        String contactName = resolveContactName(phoneNumber);
        session.contactName = contactName;
        
        updateNotification("Incoming call from " + (contactName != null ? contactName : phoneNumber));
    }

    private void handleCallAnswered(String phoneNumber, String callType) {
        Log.d(TAG, "Call answered: " + phoneNumber);
        
        CallSession session = activeCalls.get(phoneNumber);
        if (session != null) {
            session.startTime = System.currentTimeMillis();
            session.isAnswered = true;
        }
        
        updateNotification("Call in progress with " + phoneNumber);
    }

    private void handleCallEnded(String phoneNumber, String callType, String callStatus) {
        Log.d(TAG, "Call ended: " + phoneNumber + ", Status: " + callStatus);
        
        CallSession session = activeCalls.get(phoneNumber);
        if (session != null) {
            session.endTime = System.currentTimeMillis();
            session.callStatus = callStatus;
            
            // Calculate duration
            if (session.startTime > 0) {
                session.duration = (session.endTime - session.startTime) / 1000; // in seconds
            }
            
            // Create ticket automatically
            createTicketFromCall(session);
            
            // Remove from active calls
            activeCalls.remove(phoneNumber);
        }
        
        updateNotification("CallTracker Pro is monitoring calls");
    }

    private void handleOutgoingCall(String phoneNumber) {
        Log.d(TAG, "Outgoing call: " + phoneNumber);
        
        CallSession session = new CallSession(phoneNumber, "outgoing");
        session.contactName = resolveContactName(phoneNumber);
        session.startTime = System.currentTimeMillis();
        
        activeCalls.put(phoneNumber, session);
        
        updateNotification("Outgoing call to " + phoneNumber);
        
        // For outgoing calls, we'll create the ticket after a delay to get duration from call log
        handler.postDelayed(() -> {
            processOutgoingCallEnd(phoneNumber);
        }, 5000); // Wait 5 seconds then check call log
    }

    private void processOutgoingCallEnd(String phoneNumber) {
        // Query call log for the most recent call to this number
        String[] projection = {
            CallLog.Calls.DURATION,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE
        };
        
        String selection = CallLog.Calls.NUMBER + " = ?";
        String[] selectionArgs = {phoneNumber};
        String sortOrder = CallLog.Calls.DATE + " DESC";
        
        try {
            Cursor cursor = getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));
                long date = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                int type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
                
                CallSession session = activeCalls.get(phoneNumber);
                if (session != null) {
                    session.duration = duration;
                    session.endTime = date;
                    session.callStatus = duration > 0 ? "completed" : "missed";
                    
                    createTicketFromCall(session);
                    activeCalls.remove(phoneNumber);
                }
            }
            
            if (cursor != null) {
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error querying call log", e);
        }
    }

    private void createTicketFromCall(CallSession session) {
        Log.d(TAG, "Creating ticket from call session: " + session.phoneNumber);
        
        // Only create tickets if user is logged in
        if (!tokenManager.isLoggedIn()) {
            Log.w(TAG, "User not logged in, skipping ticket creation");
            return;
        }
        
        try {
            Ticket ticket = new Ticket();
            
            // Basic contact information
            ticket.setPhoneNumber(session.phoneNumber);
            ticket.setContactName(session.contactName != null ? session.contactName : session.phoneNumber);
            
            // Call details
            ticket.setCallType(session.callType);
            ticket.setCallDuration(session.duration);
            ticket.setCallDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date()));
            
            // Enhanced backend schema - Set default values for new ticket lifecycle
            ticket.setStatus("open");
            ticket.setCategory(determineCategoryFromCall(session));
            ticket.setPriority(determinePriorityFromCall(session));
            ticket.setSource("phone");
            
            // SLA management - set due date based on priority
            String dueDate = calculateDueDate(ticket.getPriority());
            ticket.setDueDate(dueDate);
            ticket.setSlaStatus("on_track");
            
            // Legacy CRM fields (maintained for compatibility)
            ticket.setLeadSource("cold_call");
            ticket.setLeadStatus("new");
            ticket.setStage("prospect");
            ticket.setInterestLevel(determineInterestLevel(session));
            
            // Multi-tenant context
            String organizationId = preferenceManager.getOrganizationId();
            String teamId = preferenceManager.getTeamId();
            String userId = preferenceManager.getUserId();
            
            if (organizationId != null) {
                ticket.setOrganizationId(organizationId);
            }
            if (teamId != null) {
                ticket.setTeamId(teamId);
            }
            if (userId != null) {
                ticket.setCreatedBy(userId);
            }
            
            // Set audit trail
            String currentTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());
            ticket.setCreatedAt(currentTime);
            ticket.setUpdatedAt(currentTime);
            ticket.setActive(true);
            
            // Create ticket via enhanced API
            ticketService.createTicket(ticket, new TicketService.TicketCallback<Ticket>() {
                @Override
                public void onSuccess(Ticket createdTicket) {
                    Log.d(TAG, "Enhanced ticket created successfully: " + createdTicket.getTicketId());
                    showEnhancedTicketCreatedNotification(createdTicket);
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Failed to create enhanced ticket: " + error);
                    // Store call data for manual ticket creation later
                    storeFailedCallForRetry(session, error);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating ticket from call", e);
        }
    }

    private String resolveContactName(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) return null;
        
        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME};
            
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String contactName = cursor.getString(0);
                    cursor.close();
                    return contactName;
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error resolving contact name", e);
        }
        
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Call Monitoring",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Monitors calls for CRM ticket creation");
            channel.setSound(null, null);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification(String content) {
        Intent intent = new Intent(this, DashboardRouterActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CallTracker Pro")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_phone)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build();
    }

    private void updateNotification(String content) {
        Notification notification = createNotification(content);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    // Enhanced helper methods for ticket creation
    private String determineCategoryFromCall(CallSession session) {
        // Logic to determine category based on call characteristics
        if (session.duration > 300) { // Calls longer than 5 minutes might be support
            return "support";
        } else if (session.callType.equals("outgoing")) {
            return "sales";
        } else {
            return "sales"; // Default to sales for incoming calls
        }
    }
    
    private String determinePriorityFromCall(CallSession session) {
        // Logic to determine priority based on call characteristics
        if (session.callType.equals("missed")) {
            return "high"; // Missed calls get high priority
        } else if (session.duration < 30) {
            return "medium"; // Short calls get medium priority
        } else {
            return "low"; // Normal calls get low priority
        }
    }
    
    private String determineInterestLevel(CallSession session) {
        // Logic to determine interest level
        if (session.duration > 180) { // Calls longer than 3 minutes
            return "hot";
        } else if (session.duration > 60) {
            return "warm";
        } else {
            return "cold";
        }
    }
    
    private String calculateDueDate(String priority) {
        long currentTime = System.currentTimeMillis();
        long dueTime;
        
        switch (priority) {
            case "urgent":
                dueTime = currentTime + (2 * 60 * 60 * 1000); // 2 hours
                break;
            case "high":
                dueTime = currentTime + (4 * 60 * 60 * 1000); // 4 hours
                break;
            case "medium":
                dueTime = currentTime + (24 * 60 * 60 * 1000); // 24 hours
                break;
            case "low":
            default:
                dueTime = currentTime + (3 * 24 * 60 * 60 * 1000); // 3 days
                break;
        }
        
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date(dueTime));
    }
    
    private void storeFailedCallForRetry(CallSession session, String error) {
        // Store call information for manual retry later
        // This could be implemented with a local database or shared preferences
        Log.w(TAG, "Storing failed call for retry: " + session.phoneNumber + ", Error: " + error);
        // TODO: Implement local storage for failed ticket creation attempts
    }
    
    private void showEnhancedTicketCreatedNotification(Ticket ticket) {
        // Create enhanced notification with more details
        String ticketInfo = String.format("Category: %s | Priority: %s | Duration: %s", 
            ticket.getCategoryDisplayName(), 
            ticket.getPriorityDisplayName(), 
            ticket.getFormattedDuration());
            
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("New Ticket Created: " + ticket.getDisplayName())
            .setContentText(ticketInfo)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(ticketInfo + "\nStatus: " + ticket.getStatusDisplayName() + 
                         "\nSLA: " + ticket.getSlaStatusDisplayName()))
            .setSmallIcon(R.drawable.ic_ticket_add)
            .setAutoCancel(true);
            
        // Add action to view ticket details
        if (ticket.getTicketId() != null) {
            Intent viewIntent = new Intent(this, DashboardRouterActivity.class);
            viewIntent.putExtra("open_ticket_id", ticket.getTicketId());
            PendingIntent viewPendingIntent = PendingIntent.getActivity(
                this, 
                ticket.getTicketId().hashCode(), 
                viewIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            builder.addAction(R.drawable.ic_ticket_add, "View Ticket", viewPendingIntent);
        }

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }

    // Inner class to track call sessions
    private static class CallSession {
        String phoneNumber;
        String callType;
        String contactName;
        long startTime = 0;
        long endTime = 0;
        long duration = 0;
        boolean isAnswered = false;
        String callStatus = "unknown";

        CallSession(String phoneNumber, String callType) {
            this.phoneNumber = phoneNumber;
            this.callType = callType;
        }
    }
}