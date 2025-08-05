package com.calltrackerpro.calltracker.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.activities.TicketDetailsActivity;
import com.calltrackerpro.calltracker.activities.UnifiedDashboardActivity;
import com.calltrackerpro.calltracker.models.Ticket;
import com.calltrackerpro.calltracker.utils.PreferenceManager;
import com.calltrackerpro.calltracker.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

public class RealTimeNotificationService extends Service implements SSEService.SSEListener {
    private static final String TAG = "RealTimeNotificationService";
    private static final int NOTIFICATION_ID = 2001;
    private static final String CHANNEL_ID = "calltracker_realtime";
    private static final String CHANNEL_NAME = "CallTracker Real-time Updates";

    private SSEService sseService;
    private TokenManager tokenManager;
    private PreferenceManager preferenceManager;
    private NotificationManager notificationManager;
    private List<RealTimeListener> listeners = new ArrayList<>();

    public interface RealTimeListener {
        void onTicketUpdate(Ticket ticket);
        void onTicketCreated(Ticket ticket);
        void onTicketAssigned(Ticket ticket, String previousAssignee);
        void onTicketStatusChanged(Ticket ticket, String previousStatus);
        void onTicketEscalated(Ticket ticket);
        void onConnectionStatusChanged(boolean connected);
    }

    public class LocalBinder extends Binder {
        public RealTimeNotificationService getService() {
            return RealTimeNotificationService.this;
        }
    }

    private final IBinder binder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "RealTimeNotificationService created");

        tokenManager = new TokenManager(this);
        preferenceManager = new PreferenceManager(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        sseService = new SSEService(this);
        sseService.setListener(this);

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createForegroundNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "RealTimeNotificationService started");

        // Start SSE connection if user is logged in
        if (tokenManager.isLoggedIn()) {
            String organizationId = preferenceManager.getOrganizationId();
            String teamId = preferenceManager.getTeamId();
            
            if (organizationId != null) {
                sseService.connect(organizationId, teamId);
            }
        }

        return START_STICKY; // Restart if killed
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "RealTimeNotificationService destroyed");
        
        if (sseService != null) {
            sseService.destroy();
        }
        listeners.clear();
    }

    public void addListener(RealTimeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(RealTimeListener listener) {
        listeners.remove(listener);
    }

    public void startRealTimeUpdates() {
        if (tokenManager.isLoggedIn()) {
            String organizationId = preferenceManager.getOrganizationId();
            String teamId = preferenceManager.getTeamId();
            
            if (organizationId != null) {
                sseService.connect(organizationId, teamId);
            }
        }
    }

    public void stopRealTimeUpdates() {
        if (sseService != null) {
            sseService.disconnect();
        }
    }

    public boolean isConnected() {
        return sseService != null && sseService.isConnected();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Real-time updates for CallTracker Pro tickets and notifications");
            channel.setShowBadge(true);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createForegroundNotification() {
        Intent notificationIntent = new Intent(this, UnifiedDashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("CallTracker Pro")
                .setContentText("Real-time updates active")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void showTicketNotification(String title, String message, String ticketId) {
        Intent intent;
        if (ticketId != null) {
            intent = new Intent(this, TicketDetailsActivity.class);
            intent.putExtra("ticket_id", ticketId);
        } else {
            intent = new Intent(this, UnifiedDashboardActivity.class);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, ticketId != null ? ticketId.hashCode() : 0, intent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_ticket_add)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        int notificationId = ticketId != null ? ticketId.hashCode() : (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, notification);
    }

    // SSEService.SSEListener implementation
    @Override
    public void onTicketUpdate(Ticket ticket) {
        Log.d(TAG, "Ticket updated: " + ticket.getTicketId());
        
        for (RealTimeListener listener : listeners) {
            listener.onTicketUpdate(ticket);
        }

        // Show notification for important updates
        if (ticket.isSlaBreached() || ticket.isEscalated()) {
            String message = ticket.isSlaBreached() ? "SLA breached" : "Ticket escalated";
            showTicketNotification(
                "Ticket Updated: " + ticket.getDisplayName(),
                message,
                ticket.getTicketId()
            );
        }
    }

    @Override
    public void onTicketCreated(Ticket ticket) {
        Log.d(TAG, "New ticket created: " + ticket.getTicketId());
        
        for (RealTimeListener listener : listeners) {
            listener.onTicketCreated(ticket);
        }

        // Show notification for new tickets
        showTicketNotification(
            "New Ticket: " + ticket.getDisplayName(),
            "Priority: " + ticket.getPriorityDisplayName(),
            ticket.getTicketId()
        );
    }

    @Override
    public void onTicketAssigned(Ticket ticket, String previousAssignee) {
        Log.d(TAG, "Ticket assigned: " + ticket.getTicketId());
        
        for (RealTimeListener listener : listeners) {
            listener.onTicketAssigned(ticket, previousAssignee);
        }

        // Show notification if assigned to current user
        String currentUserId = preferenceManager.getUserId();
        if (currentUserId != null && currentUserId.equals(ticket.getAssignedTo())) {
            showTicketNotification(
                "Ticket Assigned: " + ticket.getDisplayName(),
                "You have been assigned to this ticket",
                ticket.getTicketId()
            );
        }
    }

    @Override
    public void onTicketStatusChanged(Ticket ticket, String previousStatus) {
        Log.d(TAG, "Ticket status changed: " + ticket.getTicketId());
        
        for (RealTimeListener listener : listeners) {
            listener.onTicketStatusChanged(ticket, previousStatus);
        }

        // Show notification for important status changes
        if ("resolved".equals(ticket.getStatus()) || "closed".equals(ticket.getStatus())) {
            showTicketNotification(
                "Ticket " + ticket.getStatusDisplayName() + ": " + ticket.getDisplayName(),
                "Status changed from " + (previousStatus != null ? previousStatus : "unknown"),
                ticket.getTicketId()
            );
        }
    }

    @Override
    public void onTicketEscalated(Ticket ticket) {
        Log.d(TAG, "Ticket escalated: " + ticket.getTicketId());
        
        for (RealTimeListener listener : listeners) {
            listener.onTicketEscalated(ticket);
        }

        showTicketNotification(
            "Ticket Escalated: " + ticket.getDisplayName(),
            "Priority escalated to " + ticket.getPriorityDisplayName(),
            ticket.getTicketId()
        );
    }

    @Override
    public void onNotificationReceived(String title, String message, String ticketId) {
        Log.d(TAG, "Generic notification received: " + title);
        showTicketNotification(title, message, ticketId);
    }

    @Override
    public void onConnectionEstablished() {
        Log.i(TAG, "Real-time connection established");
        
        for (RealTimeListener listener : listeners) {
            listener.onConnectionStatusChanged(true);
        }

        // Update foreground notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("CallTracker Pro")
                .setContentText("Real-time updates connected")
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    public void onConnectionLost() {
        Log.w(TAG, "Real-time connection lost");
        
        for (RealTimeListener listener : listeners) {
            listener.onConnectionStatusChanged(false);
        }

        // Update foreground notification - silent reconnection attempt
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("CallTracker Pro")
                .setContentText("Service running")
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setSilent(true)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);

        // Attempt to reconnect
        if (sseService != null) {
            sseService.reconnect();
        }
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "Real-time connection error: " + error);
        
        // Log error silently - Don't spam user with notifications
        // TODO: Implement error tracking/analytics here instead of user notifications
        // Only show critical errors that require user action
    }
}