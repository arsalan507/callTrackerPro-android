package com.calltrackerpro.calltracker.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.calltrackerpro.calltracker.DashboardRouterActivity;
import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.activities.TicketDetailsActivity;
import com.calltrackerpro.calltracker.models.User;
import com.calltrackerpro.calltracker.utils.TokenManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificationService extends Service {
    private static final String TAG = "NotificationService";
    
    // Notification Channels
    private static final String CHANNEL_TICKETS = "tickets_channel";
    private static final String CHANNEL_ASSIGNMENTS = "assignments_channel";
    private static final String CHANNEL_REMINDERS = "reminders_channel";
    private static final String CHANNEL_SYSTEM = "system_channel";
    
    // Notification IDs
    private static final int NOTIFICATION_ID_NEW_TICKET = 1001;
    private static final int NOTIFICATION_ID_ASSIGNMENT = 1002;
    private static final int NOTIFICATION_ID_REMINDER = 1003;
    private static final int NOTIFICATION_ID_SYSTEM = 1004;
    
    private TokenManager tokenManager;
    private User currentUser;
    private Handler mainHandler;
    private ScheduledExecutorService scheduledExecutor;
    
    // Polling intervals (in production, use WebSocket or FCM)
    private static final long POLLING_INTERVAL_SECONDS = 30;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "NotificationService created");
        
        tokenManager = new TokenManager(this);
        mainHandler = new Handler(Looper.getMainLooper());
        
        createNotificationChannels();
        getCurrentUser();
        startPollingForNotifications();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "NotificationService started");
        return START_STICKY; // Service will be restarted if killed
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // We don't provide binding
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "NotificationService destroyed");
        
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            
            // Tickets Channel
            NotificationChannel ticketsChannel = new NotificationChannel(
                CHANNEL_TICKETS,
                "Ticket Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            ticketsChannel.setDescription("Notifications for new tickets and ticket updates");
            manager.createNotificationChannel(ticketsChannel);
            
            // Assignments Channel
            NotificationChannel assignmentsChannel = new NotificationChannel(
                CHANNEL_ASSIGNMENTS,
                "Ticket Assignments",
                NotificationManager.IMPORTANCE_HIGH
            );
            assignmentsChannel.setDescription("Notifications for ticket assignments");
            manager.createNotificationChannel(assignmentsChannel);
            
            // Reminders Channel
            NotificationChannel remindersChannel = new NotificationChannel(
                CHANNEL_REMINDERS,
                "Follow-up Reminders",
                NotificationManager.IMPORTANCE_HIGH
            );
            remindersChannel.setDescription("Notifications for follow-up reminders");
            manager.createNotificationChannel(remindersChannel);
            
            // System Channel
            NotificationChannel systemChannel = new NotificationChannel(
                CHANNEL_SYSTEM,
                "System Notifications",
                NotificationManager.IMPORTANCE_LOW
            );
            systemChannel.setDescription("System-level notifications");
            manager.createNotificationChannel(systemChannel);
        }
    }

    private void getCurrentUser() {
        // TODO: Get current user from TokenManager or shared preferences
        // This should be implemented based on your authentication system
        currentUser = null;
    }

    private void startPollingForNotifications() {
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                checkForNewNotifications();
            } catch (Exception e) {
                Log.e(TAG, "Error checking for notifications", e);
            }
        }, 0, POLLING_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void checkForNewNotifications() {
        if (currentUser == null) {
            Log.d(TAG, "No current user, skipping notification check");
            return;
        }
        
        // TODO: Implement actual API call to check for notifications
        // For now, we'll simulate some notifications for testing
        
        Log.d(TAG, "Checking for new notifications...");
        
        // In a real implementation, you would:
        // 1. Call API to get pending notifications for current user
        // 2. Filter based on user role and permissions
        // 3. Show appropriate notifications
        
        // Example implementation:
        // apiService.getNotifications(token, lastCheckTime, callback);
    }

    // Public methods to trigger specific notifications
    public static void showNewTicketNotification(Context context, String ticketId, String contactName, String phoneNumber) {
        Intent intent = new Intent(context, TicketDetailsActivity.class);
        intent.putExtra("ticketId", ticketId);
        intent.putExtra("mode", "view");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            ticketId.hashCode(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_TICKETS)
            .setSmallIcon(R.drawable.ic_contacts)
            .setContentTitle("New Ticket Created")
            .setContentText("Call with " + contactName + " (" + phoneNumber + ")")
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("A new ticket has been created for the call with " + contactName + " (" + phoneNumber + "). Tap to view details."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(ticketId.hashCode(), builder.build());
        }
    }

    public static void showTicketAssignmentNotification(Context context, String ticketId, String contactName, String assignedBy) {
        Intent intent = new Intent(context, TicketDetailsActivity.class);
        intent.putExtra("ticketId", ticketId);
        intent.putExtra("mode", "view");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            (ticketId + "_assignment").hashCode(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ASSIGNMENTS)
            .setSmallIcon(R.drawable.ic_person)
            .setContentTitle("Ticket Assigned")
            .setContentText("You've been assigned to " + contactName)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("You have been assigned a new ticket for " + contactName + " by " + assignedBy + ". Tap to view details."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((ticketId + "_assignment").hashCode(), builder.build());
        }
    }

    public static void showFollowUpReminderNotification(Context context, String ticketId, String contactName, String followUpTime) {
        Intent intent = new Intent(context, TicketDetailsActivity.class);
        intent.putExtra("ticketId", ticketId);
        intent.putExtra("mode", "view");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            (ticketId + "_reminder").hashCode(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_schedule)
            .setContentTitle("Follow-up Reminder")
            .setContentText("Follow up with " + contactName)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("It's time to follow up with " + contactName + ". Scheduled for " + followUpTime + ". Tap to view ticket."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((ticketId + "_reminder").hashCode(), builder.build());
        }
    }

    public static void showTicketStatusChangeNotification(Context context, String ticketId, String contactName, String oldStatus, String newStatus) {
        Intent intent = new Intent(context, TicketDetailsActivity.class);
        intent.putExtra("ticketId", ticketId);
        intent.putExtra("mode", "view");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            (ticketId + "_status").hashCode(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_TICKETS)
            .setSmallIcon(R.drawable.ic_update)
            .setContentTitle("Ticket Status Updated")
            .setContentText(contactName + ": " + oldStatus + " → " + newStatus)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("Ticket status for " + contactName + " has been updated from " + oldStatus + " to " + newStatus + "."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((ticketId + "_status").hashCode(), builder.build());
        }
    }

    public static void showHighPriorityTicketNotification(Context context, String ticketId, String contactName, String reason) {
        Intent intent = new Intent(context, TicketDetailsActivity.class);
        intent.putExtra("ticketId", ticketId);
        intent.putExtra("mode", "view");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            (ticketId + "_priority").hashCode(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ASSIGNMENTS)
            .setSmallIcon(R.drawable.ic_priority_high)
            .setContentTitle("High Priority Ticket")
            .setContentText("Urgent: " + contactName)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("High priority ticket for " + contactName + ". " + reason + ". Immediate attention required."))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setColor(0xFFF44336); // Red color for urgency

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((ticketId + "_priority").hashCode(), builder.build());
        }
    }

    public static void showTeamNotification(Context context, String title, String message, String actionData) {
        Intent intent = new Intent(context, DashboardRouterActivity.class);
        if (actionData != null) {
            intent.putExtra("action_data", actionData);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            title.hashCode(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_SYSTEM)
            .setSmallIcon(R.drawable.ic_group)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(title.hashCode(), builder.build());
        }
    }

    // Utility methods for notification management
    public static void cancelNotification(Context context, String notificationId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(notificationId.hashCode());
        }
    }

    public static void cancelAllNotifications(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancelAll();
        }
    }

    // Start the notification service
    public static void startNotificationService(Context context) {
        Intent serviceIntent = new Intent(context, NotificationService.class);
        context.startService(serviceIntent);
    }

    // Stop the notification service
    public static void stopNotificationService(Context context) {
        Intent serviceIntent = new Intent(context, NotificationService.class);
        context.stopService(serviceIntent);
    }
}