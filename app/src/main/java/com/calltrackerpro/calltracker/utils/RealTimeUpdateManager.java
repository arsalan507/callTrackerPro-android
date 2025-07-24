package com.calltrackerpro.calltracker.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.calltrackerpro.calltracker.models.Ticket;
import com.calltrackerpro.calltracker.services.RealTimeNotificationService;

import java.util.ArrayList;
import java.util.List;

public class RealTimeUpdateManager implements RealTimeNotificationService.RealTimeListener {
    private static final String TAG = "RealTimeUpdateManager";
    
    private Context context;
    private RealTimeNotificationService notificationService;
    private boolean isServiceBound = false;
    private List<RealTimeUpdateListener> listeners = new ArrayList<>();
    
    public interface RealTimeUpdateListener {
        default void onTicketUpdate(Ticket ticket) {}
        default void onTicketCreated(Ticket ticket) {}
        default void onTicketAssigned(Ticket ticket, String previousAssignee) {}
        default void onTicketStatusChanged(Ticket ticket, String previousStatus) {}
        default void onTicketEscalated(Ticket ticket) {}
        default void onConnectionStatusChanged(boolean connected) {}
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "RealTimeNotificationService connected");
            RealTimeNotificationService.LocalBinder binder = (RealTimeNotificationService.LocalBinder) service;
            notificationService = binder.getService();
            notificationService.addListener(RealTimeUpdateManager.this);
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "RealTimeNotificationService disconnected");
            notificationService = null;
            isServiceBound = false;
        }
    };

    public RealTimeUpdateManager(Context context) {
        this.context = context;
    }

    public void startRealTimeUpdates() {
        Intent intent = new Intent(context, RealTimeNotificationService.class);
        context.startForegroundService(intent);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void stopRealTimeUpdates() {
        if (isServiceBound) {
            if (notificationService != null) {
                notificationService.removeListener(this);
                notificationService.stopRealTimeUpdates();
            }
            context.unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

    public void addListener(RealTimeUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(RealTimeUpdateListener listener) {
        listeners.remove(listener);
    }

    public boolean isConnected() {
        return notificationService != null && notificationService.isConnected();
    }

    // RealTimeNotificationService.RealTimeListener implementation
    @Override
    public void onTicketUpdate(Ticket ticket) {
        Log.d(TAG, "Broadcasting ticket update to " + listeners.size() + " listeners");
        for (RealTimeUpdateListener listener : listeners) {
            listener.onTicketUpdate(ticket);
        }
    }

    @Override
    public void onTicketCreated(Ticket ticket) {
        Log.d(TAG, "Broadcasting ticket creation to " + listeners.size() + " listeners");
        for (RealTimeUpdateListener listener : listeners) {
            listener.onTicketCreated(ticket);
        }
    }

    @Override
    public void onTicketAssigned(Ticket ticket, String previousAssignee) {
        Log.d(TAG, "Broadcasting ticket assignment to " + listeners.size() + " listeners");
        for (RealTimeUpdateListener listener : listeners) {
            listener.onTicketAssigned(ticket, previousAssignee);
        }
    }

    @Override
    public void onTicketStatusChanged(Ticket ticket, String previousStatus) {
        Log.d(TAG, "Broadcasting ticket status change to " + listeners.size() + " listeners");
        for (RealTimeUpdateListener listener : listeners) {
            listener.onTicketStatusChanged(ticket, previousStatus);
        }
    }

    @Override
    public void onTicketEscalated(Ticket ticket) {
        Log.d(TAG, "Broadcasting ticket escalation to " + listeners.size() + " listeners");
        for (RealTimeUpdateListener listener : listeners) {
            listener.onTicketEscalated(ticket);
        }
    }

    @Override
    public void onConnectionStatusChanged(boolean connected) {
        Log.d(TAG, "Broadcasting connection status change: " + connected + " to " + listeners.size() + " listeners");
        for (RealTimeUpdateListener listener : listeners) {
            listener.onConnectionStatusChanged(connected);
        }
    }

    public void destroy() {
        stopRealTimeUpdates();
        listeners.clear();
    }
}