package com.calltrackerpro.calltracker.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    private static final String WS_URL = "wss://calltrackerpro-backend.vercel.app/ws";
    private static final int RECONNECT_INTERVAL = 5000; // 5 seconds
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    
    private static WebSocketManager instance;
    private WebSocketClient webSocketClient;
    private TokenManager tokenManager;
    private Handler mainHandler;
    private Gson gson;
    
    private AtomicBoolean isConnected = new AtomicBoolean(false);
    private AtomicBoolean shouldReconnect = new AtomicBoolean(true);
    private int reconnectAttempts = 0;
    
    // Event listeners
    private Map<String, WebSocketEventListener> eventListeners = new HashMap<>();
    
    public interface WebSocketEventListener {
        void onEvent(String eventType, JsonObject data);
    }
    
    private WebSocketManager(Context context) {
        tokenManager = new TokenManager(context);
        mainHandler = new Handler(Looper.getMainLooper());
        gson = new Gson();
    }
    
    public static synchronized WebSocketManager getInstance(Context context) {
        if (instance == null) {
            instance = new WebSocketManager(context.getApplicationContext());
        }
        return instance;
    }
    
    public void connect() {
        if (!tokenManager.isLoggedIn()) {
            Log.w(TAG, "Cannot connect WebSocket: User not logged in");
            return;
        }
        
        if (isConnected.get()) {
            Log.d(TAG, "WebSocket already connected");
            return;
        }
        
        try {
            String token = tokenManager.getToken();
            String orgId = tokenManager.getUser().getOrganizationId();
            
            String wsUrlWithParams = WS_URL + "?token=" + token;
            if (orgId != null) {
                wsUrlWithParams += "&organizationId=" + orgId;
            }
            
            URI serverUri = URI.create(wsUrlWithParams);
            
            webSocketClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    Log.d(TAG, "WebSocket connected successfully");
                    isConnected.set(true);
                    reconnectAttempts = 0;
                    
                    // Send authentication message
                    sendAuthenticationMessage();
                    
                    // Subscribe to relevant events based on user role
                    subscribeToEvents();
                }
                
                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "WebSocket message received: " + message);
                    handleWebSocketMessage(message);
                }
                
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "WebSocket disconnected: " + code + " - " + reason);
                    isConnected.set(false);
                    
                    if (shouldReconnect.get() && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                        scheduleReconnect();
                    }
                }
                
                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket error: " + ex.getMessage());
                    isConnected.set(false);
                }
            };
            
            webSocketClient.connect();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to connect WebSocket: " + e.getMessage());
        }
    }
    
    public void disconnect() {
        shouldReconnect.set(false);
        
        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }
        
        isConnected.set(false);
        Log.d(TAG, "WebSocket disconnected");
    }
    
    private void sendAuthenticationMessage() {
        if (!isConnected.get()) return;
        
        try {
            JsonObject authMessage = new JsonObject();
            authMessage.addProperty("type", "authenticate");
            authMessage.addProperty("token", tokenManager.getToken());
            authMessage.addProperty("userId", tokenManager.getUser().getId());
            authMessage.addProperty("organizationId", tokenManager.getUser().getOrganizationId());
            authMessage.addProperty("role", tokenManager.getUser().getRole());
            
            send(authMessage.toString());
            Log.d(TAG, "Authentication message sent");
        } catch (Exception e) {
            Log.e(TAG, "Failed to send authentication message: " + e.getMessage());
        }
    }
    
    private void subscribeToEvents() {
        if (!isConnected.get()) return;
        
        try {
            JsonObject subscribeMessage = new JsonObject();
            subscribeMessage.addProperty("type", "subscribe");
            
            // Subscribe to events based on user role
            String userRole = tokenManager.getUser().getRole();
            switch (userRole) {
                case "super_admin":
                    subscribeMessage.addProperty("events", "all");
                    break;
                case "org_admin":
                    subscribeMessage.addProperty("events", "organization,tickets,users,analytics");
                    break;
                case "manager":
                    subscribeMessage.addProperty("events", "tickets,team_analytics,user_status");
                    break;
                case "agent":
                    subscribeMessage.addProperty("events", "tickets,calls,assignments");
                    break;
                default:
                    subscribeMessage.addProperty("events", "tickets");
                    break;
            }
            
            send(subscribeMessage.toString());
            Log.d(TAG, "Event subscription sent for role: " + userRole);
        } catch (Exception e) {
            Log.e(TAG, "Failed to subscribe to events: " + e.getMessage());
        }
    }
    
    private void handleWebSocketMessage(String message) {
        try {
            JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
            String eventType = jsonMessage.get("type").getAsString();
            
            // Handle specific event types
            switch (eventType) {
                case "ticket_created":
                case "ticket_updated":
                case "ticket_assigned":
                    handleTicketEvent(eventType, jsonMessage);
                    break;
                    
                case "user_status_changed":
                case "user_login":
                case "user_logout":
                    handleUserEvent(eventType, jsonMessage);
                    break;
                    
                case "call_completed":
                case "call_started":
                    handleCallEvent(eventType, jsonMessage);
                    break;
                    
                case "dashboard_refresh":
                    handleDashboardRefresh(eventType, jsonMessage);
                    break;
                    
                case "organization_updated":
                    handleOrganizationEvent(eventType, jsonMessage);
                    break;
                    
                default:
                    Log.d(TAG, "Unhandled event type: " + eventType);
                    break;
            }
            
            // Notify all registered listeners
            notifyEventListeners(eventType, jsonMessage);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse WebSocket message: " + e.getMessage());
        }
    }
    
    private void handleTicketEvent(String eventType, JsonObject data) {
        Log.d(TAG, "Handling ticket event: " + eventType);
        
        // Post to main thread for UI updates
        mainHandler.post(() -> {
            WebSocketEventListener listener = eventListeners.get("tickets");
            if (listener != null) {
                listener.onEvent(eventType, data);
            }
        });
    }
    
    private void handleUserEvent(String eventType, JsonObject data) {
        Log.d(TAG, "Handling user event: " + eventType);
        
        mainHandler.post(() -> {
            WebSocketEventListener listener = eventListeners.get("users");
            if (listener != null) {
                listener.onEvent(eventType, data);
            }
        });
    }
    
    private void handleCallEvent(String eventType, JsonObject data) {
        Log.d(TAG, "Handling call event: " + eventType);
        
        mainHandler.post(() -> {
            WebSocketEventListener listener = eventListeners.get("calls");
            if (listener != null) {
                listener.onEvent(eventType, data);
            }
        });
    }
    
    private void handleDashboardRefresh(String eventType, JsonObject data) {
        Log.d(TAG, "Handling dashboard refresh event");
        
        mainHandler.post(() -> {
            WebSocketEventListener listener = eventListeners.get("dashboard");
            if (listener != null) {
                listener.onEvent(eventType, data);
            }
        });
    }
    
    private void handleOrganizationEvent(String eventType, JsonObject data) {
        Log.d(TAG, "Handling organization event: " + eventType);
        
        mainHandler.post(() -> {
            WebSocketEventListener listener = eventListeners.get("organization");
            if (listener != null) {
                listener.onEvent(eventType, data);
            }
        });
    }
    
    private void notifyEventListeners(String eventType, JsonObject data) {
        // Notify global event listener if exists
        WebSocketEventListener globalListener = eventListeners.get("global");
        if (globalListener != null) {
            mainHandler.post(() -> globalListener.onEvent(eventType, data));
        }
    }
    
    private void scheduleReconnect() {
        reconnectAttempts++;
        Log.d(TAG, "Scheduling reconnect attempt " + reconnectAttempts + "/" + MAX_RECONNECT_ATTEMPTS);
        
        mainHandler.postDelayed(() -> {
            if (shouldReconnect.get() && !isConnected.get()) {
                connect();
            }
        }, RECONNECT_INTERVAL);
    }
    
    public void send(String message) {
        if (webSocketClient != null && isConnected.get()) {
            webSocketClient.send(message);
        } else {
            Log.w(TAG, "Cannot send message: WebSocket not connected");
        }
    }
    
    public void addEventListener(String eventType, WebSocketEventListener listener) {
        eventListeners.put(eventType, listener);
        Log.d(TAG, "Event listener registered for: " + eventType);
    }
    
    public void removeEventListener(String eventType) {
        eventListeners.remove(eventType);
        Log.d(TAG, "Event listener removed for: " + eventType);
    }
    
    public boolean isConnected() {
        return isConnected.get();
    }
    
    public void sendTicketUpdate(String ticketId, String action, JsonObject data) {
        if (!isConnected.get()) return;
        
        try {
            JsonObject message = new JsonObject();
            message.addProperty("type", "ticket_action");
            message.addProperty("action", action);
            message.addProperty("ticketId", ticketId);
            message.add("data", data);
            message.addProperty("timestamp", System.currentTimeMillis());
            
            send(message.toString());
            Log.d(TAG, "Ticket update sent: " + action + " for ticket: " + ticketId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send ticket update: " + e.getMessage());
        }
    }
    
    public void sendStatusUpdate(String status) {
        if (!isConnected.get()) return;
        
        try {
            JsonObject message = new JsonObject();
            message.addProperty("type", "user_status");
            message.addProperty("status", status);
            message.addProperty("userId", tokenManager.getUser().getId());
            message.addProperty("timestamp", System.currentTimeMillis());
            
            send(message.toString());
            Log.d(TAG, "Status update sent: " + status);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send status update: " + e.getMessage());
        }
    }
}