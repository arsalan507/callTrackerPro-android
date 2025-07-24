package com.calltrackerpro.calltracker.services;

import android.content.Context;
import android.util.Log;

import com.calltrackerpro.calltracker.models.Ticket;
import com.calltrackerpro.calltracker.utils.TokenManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SSEService {
    private static final String TAG = "SSEService";
    private static final String SSE_ENDPOINT = "tickets/stream";
    
    private Context context;
    private TokenManager tokenManager;
    private ExecutorService executorService;
    private HttpURLConnection connection;
    private BufferedReader reader;
    private boolean isConnected = false;
    private SSEListener listener;
    private String organizationId;
    private String teamId;

    public SSEService(Context context) {
        this.context = context;
        this.tokenManager = new TokenManager(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public interface SSEListener {
        void onTicketUpdate(Ticket ticket);
        void onTicketCreated(Ticket ticket);
        void onTicketAssigned(Ticket ticket, String previousAssignee);
        void onTicketStatusChanged(Ticket ticket, String previousStatus);
        void onTicketEscalated(Ticket ticket);
        void onNotificationReceived(String title, String message, String ticketId);
        void onConnectionEstablished();
        void onConnectionLost();
        void onError(String error);
    }

    public void setListener(SSEListener listener) {
        this.listener = listener;
    }

    public void connect(String organizationId, String teamId) {
        this.organizationId = organizationId;
        this.teamId = teamId;
        
        if (isConnected) {
            Log.w(TAG, "SSE already connected");
            return;
        }

        executorService.execute(this::establishConnection);
    }

    private void establishConnection() {
        try {
            String baseUrl = ApiService.BASE_URL;
            if (!baseUrl.endsWith("/")) {
                baseUrl += "/";
            }
            
            String urlString = baseUrl + SSE_ENDPOINT + 
                             "?organization_id=" + organizationId;
            
            if (teamId != null && !teamId.isEmpty()) {
                urlString += "&team_id=" + teamId;
            }

            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            
            // Set headers
            String token = tokenManager.getToken();
            if (token != null) {
                connection.setRequestProperty("Authorization", "Bearer " + token);
            }
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(0); // No timeout for SSE

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                isConnected = true;
                
                if (listener != null) {
                    listener.onConnectionEstablished();
                }
                
                Log.i(TAG, "SSE connection established");
                listenForEvents();
            } else {
                Log.e(TAG, "Failed to establish SSE connection. Response code: " + responseCode);
                if (listener != null) {
                    listener.onError("Failed to connect to server. Code: " + responseCode);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error establishing SSE connection", e);
            if (listener != null) {
                listener.onError("Connection error: " + e.getMessage());
            }
        }
    }

    private void listenForEvents() {
        try {
            String line;
            StringBuilder eventData = new StringBuilder();
            String eventType = null;
            
            while (isConnected && (line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    // End of event, process it
                    if (eventType != null && eventData.length() > 0) {
                        processEvent(eventType, eventData.toString());
                    }
                    eventData.setLength(0);
                    eventType = null;
                } else if (line.startsWith("event: ")) {
                    eventType = line.substring(7);
                } else if (line.startsWith("data: ")) {
                    if (eventData.length() > 0) {
                        eventData.append("\n");
                    }
                    eventData.append(line.substring(6));
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading SSE stream", e);
            if (isConnected && listener != null) {
                listener.onConnectionLost();
            }
        } finally {
            disconnect();
        }
    }

    private void processEvent(String eventType, String data) {
        Log.d(TAG, "Received SSE event: " + eventType + " - " + data);
        
        if (listener == null) return;

        try {
            Gson gson = new Gson();
            JsonObject jsonData = JsonParser.parseString(data).getAsJsonObject();

            switch (eventType) {
                case "ticket-created":
                    Ticket newTicket = gson.fromJson(jsonData.get("ticket"), Ticket.class);
                    listener.onTicketCreated(newTicket);
                    break;

                case "ticket-updated":
                    Ticket updatedTicket = gson.fromJson(jsonData.get("ticket"), Ticket.class);
                    listener.onTicketUpdate(updatedTicket);
                    break;

                case "ticket-assigned":
                    Ticket assignedTicket = gson.fromJson(jsonData.get("ticket"), Ticket.class);
                    String previousAssignee = jsonData.has("previousAssignee") ? 
                        jsonData.get("previousAssignee").getAsString() : null;
                    listener.onTicketAssigned(assignedTicket, previousAssignee);
                    break;

                case "ticket-status-changed":
                    Ticket statusChangedTicket = gson.fromJson(jsonData.get("ticket"), Ticket.class);
                    String previousStatus = jsonData.has("previousStatus") ? 
                        jsonData.get("previousStatus").getAsString() : null;
                    listener.onTicketStatusChanged(statusChangedTicket, previousStatus);
                    break;

                case "ticket-escalated":
                    Ticket escalatedTicket = gson.fromJson(jsonData.get("ticket"), Ticket.class);
                    listener.onTicketEscalated(escalatedTicket);
                    break;

                case "notification":
                    String title = jsonData.has("title") ? jsonData.get("title").getAsString() : "Notification";
                    String message = jsonData.has("message") ? jsonData.get("message").getAsString() : "";
                    String ticketId = jsonData.has("ticketId") ? jsonData.get("ticketId").getAsString() : null;
                    listener.onNotificationReceived(title, message, ticketId);
                    break;

                case "ping":
                    // Keep-alive ping, no action needed
                    Log.d(TAG, "Received keep-alive ping");
                    break;

                default:
                    Log.w(TAG, "Unknown SSE event type: " + eventType);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing SSE event: " + eventType, e);
            if (listener != null) {
                listener.onError("Error processing event: " + e.getMessage());
            }
        }
    }

    public void disconnect() {
        isConnected = false;
        
        try {
            if (reader != null) {
                reader.close();
                reader = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing reader", e);
        }
        
        try {
            if (connection != null) {
                connection.disconnect();
                connection = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error disconnecting", e);
        }
        
        Log.i(TAG, "SSE connection closed");
        
        if (listener != null) {
            listener.onConnectionLost();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void reconnect() {
        if (isConnected) {
            disconnect();
        }
        
        // Wait a bit before reconnecting
        executorService.execute(() -> {
            try {
                Thread.sleep(2000);
                connect(organizationId, teamId);
            } catch (InterruptedException e) {
                Log.e(TAG, "Reconnect interrupted", e);
            }
        });
    }

    public void destroy() {
        disconnect();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}