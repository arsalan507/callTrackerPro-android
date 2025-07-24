package com.calltrackerpro.calltracker.services;

import android.content.Context;
import android.util.Log;

import com.calltrackerpro.calltracker.models.ApiResponse;
import com.calltrackerpro.calltracker.models.Ticket;
import com.calltrackerpro.calltracker.models.TicketNote;
import com.calltrackerpro.calltracker.utils.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketService {
    private static final String TAG = "TicketService";
    private final ApiService apiService;
    private final TokenManager tokenManager;
    private final Context context;

    public TicketService(Context context) {
        this.context = context;
        this.apiService = ApiService.getInstance();
        this.tokenManager = new TokenManager(context);
    }

    // Callback interface for ticket operations
    public interface TicketCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    // Create a new ticket
    public void createTicket(Ticket ticket, TicketCallback<Ticket> callback) {
        String token = "Bearer " + tokenManager.getToken();
        
        Call<ApiResponse<Ticket>> call = apiService.createTicket(token, ticket);
        call.enqueue(new Callback<ApiResponse<Ticket>>() {
            @Override
            public void onResponse(Call<ApiResponse<Ticket>> call, Response<ApiResponse<Ticket>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Ticket> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Ticket created successfully");
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "API error creating ticket: " + apiResponse.getMessage());
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    String error = "HTTP " + response.code() + ": " + response.message();
                    Log.e(TAG, "Failed to create ticket: " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Ticket>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, "Network error creating ticket", t);
                callback.onError(error);
            }
        });
    }

    // Get tickets with enhanced filtering
    public void getTickets(String organizationId, String teamId, String assignedTo, 
                          String status, String category, String priority, String slaStatus,
                          int page, int limit, TicketCallback<List<Ticket>> callback) {
        String token = "Bearer " + tokenManager.getToken();
        
        Call<ApiResponse<List<Ticket>>> call = apiService.getTickets(
            token, organizationId, teamId, assignedTo, status, category, priority, slaStatus, page, limit
        );
        
        call.enqueue(new Callback<ApiResponse<List<Ticket>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Ticket>>> call, Response<ApiResponse<List<Ticket>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Ticket>> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Tickets retrieved successfully: " + apiResponse.getData().size());
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "API error getting tickets: " + apiResponse.getMessage());
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    String error = "HTTP " + response.code() + ": " + response.message();
                    Log.e(TAG, "Failed to get tickets: " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Ticket>>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, "Network error getting tickets", t);
                callback.onError(error);
            }
        });
    }

    // Get a specific ticket by ID
    public void getTicket(String ticketId, TicketCallback<Ticket> callback) {
        String token = "Bearer " + tokenManager.getToken();
        
        Call<ApiResponse<Ticket>> call = apiService.getTicket(token, ticketId);
        call.enqueue(new Callback<ApiResponse<Ticket>>() {
            @Override
            public void onResponse(Call<ApiResponse<Ticket>> call, Response<ApiResponse<Ticket>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Ticket> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Ticket retrieved successfully: " + ticketId);
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "API error getting ticket: " + apiResponse.getMessage());
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    String error = "HTTP " + response.code() + ": " + response.message();
                    Log.e(TAG, "Failed to get ticket: " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Ticket>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, "Network error getting ticket", t);
                callback.onError(error);
            }
        });
    }

    // Update a ticket
    public void updateTicket(String ticketId, Ticket ticket, TicketCallback<Ticket> callback) {
        String token = "Bearer " + tokenManager.getToken();
        
        Call<ApiResponse<Ticket>> call = apiService.updateTicket(token, ticketId, ticket);
        call.enqueue(new Callback<ApiResponse<Ticket>>() {
            @Override
            public void onResponse(Call<ApiResponse<Ticket>> call, Response<ApiResponse<Ticket>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Ticket> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Ticket updated successfully: " + ticketId);
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "API error updating ticket: " + apiResponse.getMessage());
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    String error = "HTTP " + response.code() + ": " + response.message();
                    Log.e(TAG, "Failed to update ticket: " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Ticket>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, "Network error updating ticket", t);
                callback.onError(error);
            }
        });
    }

    // Assign ticket to agent with enhanced assignment tracking
    public void assignTicket(String ticketId, String assignedTo, String assignedTeam, String assignmentNote, TicketCallback<Ticket> callback) {
        String token = "Bearer " + tokenManager.getToken();
        
        ApiService.AssignTicketRequest request = new ApiService.AssignTicketRequest(assignedTo, assignedTeam, assignmentNote);
        Call<ApiResponse<Ticket>> call = apiService.assignTicket(token, ticketId, request);
        
        call.enqueue(new Callback<ApiResponse<Ticket>>() {
            @Override
            public void onResponse(Call<ApiResponse<Ticket>> call, Response<ApiResponse<Ticket>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Ticket> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Ticket assigned successfully: " + ticketId);
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "API error assigning ticket: " + apiResponse.getMessage());
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    String error = "HTTP " + response.code() + ": " + response.message();
                    Log.e(TAG, "Failed to assign ticket: " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Ticket>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, "Network error assigning ticket", t);
                callback.onError(error);
            }
        });
    }

    // Update ticket status with enhanced status management
    public void updateTicketStatus(String ticketId, String status, String category, String priority, String statusNote, TicketCallback<Ticket> callback) {
        String token = "Bearer " + tokenManager.getToken();
        
        ApiService.UpdateTicketStatusRequest request = new ApiService.UpdateTicketStatusRequest(status, category, priority, statusNote);
        Call<ApiResponse<Ticket>> call = apiService.updateTicketStatus(token, ticketId, request);
        
        call.enqueue(new Callback<ApiResponse<Ticket>>() {
            @Override
            public void onResponse(Call<ApiResponse<Ticket>> call, Response<ApiResponse<Ticket>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Ticket> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Ticket status updated successfully: " + ticketId);
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "API error updating ticket status: " + apiResponse.getMessage());
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    String error = "HTTP " + response.code() + ": " + response.message();
                    Log.e(TAG, "Failed to update ticket status: " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Ticket>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, "Network error updating ticket status", t);
                callback.onError(error);
            }
        });
    }

    // Add note to ticket
    public void addTicketNote(String ticketId, TicketNote note, TicketCallback<TicketNote> callback) {
        String token = "Bearer " + tokenManager.getToken();
        
        Call<ApiResponse<TicketNote>> call = apiService.addTicketNote(token, ticketId, note);
        call.enqueue(new Callback<ApiResponse<TicketNote>>() {
            @Override
            public void onResponse(Call<ApiResponse<TicketNote>> call, Response<ApiResponse<TicketNote>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<TicketNote> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Note added successfully to ticket: " + ticketId);
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "API error adding note: " + apiResponse.getMessage());
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    String error = "HTTP " + response.code() + ": " + response.message();
                    Log.e(TAG, "Failed to add note: " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<TicketNote>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, "Network error adding note", t);
                callback.onError(error);
            }
        });
    }

    // Get ticket notes
    public void getTicketNotes(String ticketId, TicketCallback<List<TicketNote>> callback) {
        String token = "Bearer " + tokenManager.getToken();
        
        Call<ApiResponse<List<TicketNote>>> call = apiService.getTicketNotes(token, ticketId);
        call.enqueue(new Callback<ApiResponse<List<TicketNote>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<TicketNote>>> call, Response<ApiResponse<List<TicketNote>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<TicketNote>> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Notes retrieved successfully for ticket: " + ticketId);
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "API error getting notes: " + apiResponse.getMessage());
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    String error = "HTTP " + response.code() + ": " + response.message();
                    Log.e(TAG, "Failed to get notes: " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<TicketNote>>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, "Network error getting notes", t);
                callback.onError(error);
            }
        });
    }

    // Delete ticket (admin only)
    public void deleteTicket(String ticketId, TicketCallback<String> callback) {
        String token = "Bearer " + tokenManager.getToken();
        
        Call<ApiResponse<String>> call = apiService.deleteTicket(token, ticketId);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Ticket deleted successfully: " + ticketId);
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "API error deleting ticket: " + apiResponse.getMessage());
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    String error = "HTTP " + response.code() + ": " + response.message();
                    Log.e(TAG, "Failed to delete ticket: " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, "Network error deleting ticket", t);
                callback.onError(error);
            }
        });
    }

    // Escalate ticket to higher priority/manager
    public void escalateTicket(String ticketId, String escalatedTo, String escalationReason, String priority, TicketCallback<Ticket> callback) {
        String token = "Bearer " + tokenManager.getToken();
        
        ApiService.EscalateTicketRequest request = new ApiService.EscalateTicketRequest(escalatedTo, escalationReason, priority);
        Call<ApiResponse<Ticket>> call = apiService.escalateTicket(token, ticketId, request);
        
        call.enqueue(new Callback<ApiResponse<Ticket>>() {
            @Override
            public void onResponse(Call<ApiResponse<Ticket>> call, Response<ApiResponse<Ticket>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Ticket> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Ticket escalated successfully: " + ticketId);
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "API error escalating ticket: " + apiResponse.getMessage());
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    String error = "HTTP " + response.code() + ": " + response.message();
                    Log.e(TAG, "Failed to escalate ticket: " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Ticket>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, "Network error escalating ticket", t);
                callback.onError(error);
            }
        });
    }

    // Submit customer satisfaction rating
    public void submitSatisfactionRating(String ticketId, int rating, String feedback, TicketCallback<Ticket> callback) {
        String token = "Bearer " + tokenManager.getToken();
        
        ApiService.SatisfactionRatingRequest request = new ApiService.SatisfactionRatingRequest(rating, feedback);
        Call<ApiResponse<Ticket>> call = apiService.submitSatisfactionRating(token, ticketId, request);
        
        call.enqueue(new Callback<ApiResponse<Ticket>>() {
            @Override
            public void onResponse(Call<ApiResponse<Ticket>> call, Response<ApiResponse<Ticket>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Ticket> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Satisfaction rating submitted successfully: " + ticketId);
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "API error submitting satisfaction rating: " + apiResponse.getMessage());
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    String error = "HTTP " + response.code() + ": " + response.message();
                    Log.e(TAG, "Failed to submit satisfaction rating: " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Ticket>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, "Network error submitting satisfaction rating", t);
                callback.onError(error);
            }
        });
    }

    // Get ticket statistics
    public void getTicketStats(String organizationId, String teamId, String userId, String period, TicketCallback<ApiService.TicketStatsResponse> callback) {
        String token = "Bearer " + tokenManager.getToken();
        
        Call<ApiResponse<ApiService.TicketStatsResponse>> call = apiService.getTicketStats(token, organizationId, teamId, userId, period);
        call.enqueue(new Callback<ApiResponse<ApiService.TicketStatsResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ApiService.TicketStatsResponse>> call, Response<ApiResponse<ApiService.TicketStatsResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ApiService.TicketStatsResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Ticket stats retrieved successfully");
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        Log.e(TAG, "API error getting ticket stats: " + apiResponse.getMessage());
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    String error = "HTTP " + response.code() + ": " + response.message();
                    Log.e(TAG, "Failed to get ticket stats: " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ApiService.TicketStatsResponse>> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, "Network error getting ticket stats", t);
                callback.onError(error);
            }
        });
    }

    // Convenience method for simplified ticket filtering
    public void getTicketsSimple(String organizationId, String teamId, TicketCallback<List<Ticket>> callback) {
        getTickets(organizationId, teamId, null, null, null, null, null, 1, 50, callback);
    }

    // Convenience method for getting user's assigned tickets
    public void getMyTickets(String organizationId, String userId, TicketCallback<List<Ticket>> callback) {
        getTickets(organizationId, null, userId, null, null, null, null, 1, 100, callback);
    }

    // Convenience method for getting high priority tickets
    public void getHighPriorityTickets(String organizationId, TicketCallback<List<Ticket>> callback) {
        getTickets(organizationId, null, null, null, null, "high", null, 1, 50, callback);
    }

    // Convenience method for getting SLA breached tickets
    public void getSlaBreachedTickets(String organizationId, TicketCallback<List<Ticket>> callback) {
        getTickets(organizationId, null, null, null, null, null, "breached", 1, 50, callback);
    }
}