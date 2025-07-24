package com.calltrackerpro.calltracker.services;

import com.calltrackerpro.calltracker.models.AuthResponse;
import com.calltrackerpro.calltracker.models.User;
import com.calltrackerpro.calltracker.models.ApiResponse;
import com.calltrackerpro.calltracker.models.CallLog;
import com.calltrackerpro.calltracker.models.Organization;
import com.calltrackerpro.calltracker.models.Team;
import com.calltrackerpro.calltracker.models.Contact;
import com.calltrackerpro.calltracker.models.Ticket;
import com.calltrackerpro.calltracker.models.TicketNote;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.concurrent.TimeUnit;

public interface ApiService {

    // ========== BASE URL - UPDATE THIS TO YOUR BACKEND ==========
    String BASE_URL = "https://calltrackerpro-backend.vercel.app/"; // For Android emulator
    // String BASE_URL = "http://192.168.1.XXX:5000/"; // For real device - replace XXX with your IP

    /**
     * Test API connection - WORKING ✅
     */
    @GET("test")
    Call<ApiResponse<String>> testConnection();

    /**
     * Test call logs endpoint - WORKING ✅
     */
    @GET("call-logs/test")
    Call<ApiResponse<String>> testCallLogs();

    /**
     * Get call logs - WORKING ✅
     */
    @GET("call-logs")
    Call<ApiResponse<java.util.List<CallLog>>> getCallLogs(@Header("Authorization") String token);

    /**
     * Create/sync a call log - WORKING ✅
     */
    @POST("call-logs")
    Call<ApiResponse<CallLog>> createCallLog(@Header("Authorization") String token, @Body CallLog callLog);

    // ========== AUTH ENDPOINTS ==========

    /**
     * Login endpoint - Enhanced for CreateAccount flow ✅
     */
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);

    /**
     * Register endpoint - Enhanced for CreateAccount flow ✅
     */
    @POST("auth/register")
    Call<AuthResponse> register(@Body CreateAccountRequest createAccountRequest);

    /**
     * Logout endpoint - NEEDS BACKEND IMPLEMENTATION ⚠️
     */
    @POST("auth/logout")
    Call<ApiResponse<String>> logout(@Header("Authorization") String token);

    /**
     * Get current user profile - NEEDS BACKEND IMPLEMENTATION ⚠️
     */
    @GET("auth/user")
    Call<ApiResponse<User>> getUser(@Header("Authorization") String token);

    /**
     * Refresh token endpoint - NEEDS BACKEND IMPLEMENTATION ⚠️
     */
    @POST("auth/refresh")
    Call<AuthResponse> refreshToken(@Header("Authorization") String token);

    /**
     * Password reset request - NEEDS BACKEND IMPLEMENTATION ⚠️
     */
    @POST("auth/forgot-password")
    Call<ApiResponse<String>> forgotPassword(@Body ForgotPasswordRequest request);

    /**
     * Verify email endpoint - NEEDS BACKEND IMPLEMENTATION ⚠️
     */
    @POST("auth/verify-email")
    Call<ApiResponse<String>> verifyEmail(@Header("Authorization") String token, @Body VerifyEmailRequest request);

    // ========== ORGANIZATION MANAGEMENT ENDPOINTS ==========

    /**
     * Get user's organizations
     */
    @GET("organizations")
    Call<ApiResponse<java.util.List<Organization>>> getUserOrganizations(@Header("Authorization") String token);

    /**
     * Get organization details
     */
    @GET("organizations/{id}")
    Call<ApiResponse<Organization>> getOrganization(@Header("Authorization") String token, @Path("id") String organizationId);

    /**
     * Update organization settings (org_admin only)
     */
    @PUT("organizations/{id}")
    Call<ApiResponse<Organization>> updateOrganization(@Header("Authorization") String token, @Path("id") String organizationId, @Body Organization organization);

    /**
     * Get organization analytics (org_admin only)
     */
    @GET("organizations/{id}/analytics")
    Call<ApiResponse<Organization.OrganizationAnalytics>> getOrganizationAnalytics(@Header("Authorization") String token, @Path("id") String organizationId);

    // ========== USER MANAGEMENT ENDPOINTS ==========

    /**
     * Get organization users (org_admin, manager)
     */
    @GET("users")
    Call<ApiResponse<java.util.List<User>>> getOrganizationUsers(@Header("Authorization") String token, @Query("organization_id") String organizationId);

    /**
     * Invite user to organization (org_admin)
     */
    @POST("users/invite")
    Call<ApiResponse<String>> inviteUser(@Header("Authorization") String token, @Body InviteUserRequest request);

    /**
     * Update user role and permissions (org_admin)
     */
    @PUT("users/{id}")
    Call<ApiResponse<User>> updateUser(@Header("Authorization") String token, @Path("id") String userId, @Body UpdateUserRequest request);

    /**
     * Deactivate user (org_admin)
     */
    @DELETE("users/{id}")
    Call<ApiResponse<String>> deactivateUser(@Header("Authorization") String token, @Path("id") String userId);

    /**
     * Get user profile with full organization context
     */
    @GET("users/profile")
    Call<ApiResponse<User>> getUserProfile(@Header("Authorization") String token, @Query("organization_id") String organizationId);

    /**
     * Update user profile (own profile)
     */
    @PUT("users/profile/{id}")
    Call<ApiResponse<User>> updateProfile(@Header("Authorization") String token, @Path("id") String userId, @Body UpdateProfileRequest request);

    // ========== TEAM MANAGEMENT ENDPOINTS ==========

    /**
     * Get organization teams
     */
    @GET("teams")
    Call<ApiResponse<java.util.List<Team>>> getTeams(@Header("Authorization") String token, @Query("organization_id") String organizationId);

    /**
     * Create new team (org_admin, manager)
     */
    @POST("teams")
    Call<ApiResponse<Team>> createTeam(@Header("Authorization") String token, @Body CreateTeamRequest request);

    /**
     * Update team (org_admin, team manager)
     */
    @PUT("teams/{id}")
    Call<ApiResponse<Team>> updateTeam(@Header("Authorization") String token, @Path("id") String teamId, @Body UpdateTeamRequest request);

    /**
     * Delete team (org_admin)
     */
    @DELETE("teams/{id}")
    Call<ApiResponse<String>> deleteTeam(@Header("Authorization") String token, @Path("id") String teamId);

    /**
     * Get team members
     */
    @GET("teams/{id}/members")
    Call<ApiResponse<java.util.List<User>>> getTeamMembers(@Header("Authorization") String token, @Path("id") String teamId);

    /**
     * Add user to team
     */
    @POST("teams/{id}/members")
    Call<ApiResponse<String>> addTeamMember(@Header("Authorization") String token, @Path("id") String teamId, @Body AddTeamMemberRequest request);

    /**
     * Remove user from team
     */
    @DELETE("teams/{id}/members/{userId}")
    Call<ApiResponse<String>> removeTeamMember(@Header("Authorization") String token, @Path("id") String teamId, @Path("userId") String userId);

    /**
     * Get team analytics
     */
    @GET("teams/{id}/analytics")
    Call<ApiResponse<Team.TeamAnalytics>> getTeamAnalytics(@Header("Authorization") String token, @Path("id") String teamId);

    // ========== CONTACT MANAGEMENT ENDPOINTS ==========

    /**
     * Get contacts with organization scoping
     */
    @GET("contacts")
    Call<ApiResponse<java.util.List<Contact>>> getContacts(@Header("Authorization") String token, 
                                                           @Query("organization_id") String organizationId,
                                                           @Query("team_id") String teamId,
                                                           @Query("assigned_agent_id") String agentId,
                                                           @Query("status") String status,
                                                           @Query("page") int page,
                                                           @Query("limit") int limit);

    /**
     * Create new contact
     */
    @POST("contacts")
    Call<ApiResponse<Contact>> createContact(@Header("Authorization") String token, @Body Contact contact);

    /**
     * Update contact
     */
    @PUT("contacts/{id}")
    Call<ApiResponse<Contact>> updateContact(@Header("Authorization") String token, @Path("id") String contactId, @Body Contact contact);

    /**
     * Delete contact
     */
    @DELETE("contacts/{id}")
    Call<ApiResponse<String>> deleteContact(@Header("Authorization") String token, @Path("id") String contactId);

    /**
     * Get contact details
     */
    @GET("contacts/{id}")
    Call<ApiResponse<Contact>> getContact(@Header("Authorization") String token, @Path("id") String contactId);

    /**
     * Add interaction to contact
     */
    @POST("contacts/{id}/interactions")
    Call<ApiResponse<Contact.Interaction>> addContactInteraction(@Header("Authorization") String token, @Path("id") String contactId, @Body Contact.Interaction interaction);

    /**
     * Assign contact to agent
     */
    @PUT("contacts/{id}/assign")
    Call<ApiResponse<Contact>> assignContact(@Header("Authorization") String token, @Path("id") String contactId, @Body AssignContactRequest request);

    // ========== ENHANCED CALL LOG ENDPOINTS ==========

    /**
     * Get call logs with multi-tenant filtering
     */
    @GET("call-logs/filtered")
    Call<ApiResponse<java.util.List<CallLog>>> getFilteredCallLogs(@Header("Authorization") String token,
                                                                   @Query("organization_id") String organizationId,
                                                                   @Query("team_id") String teamId,
                                                                   @Query("user_id") String userId,
                                                                   @Query("contact_id") String contactId,
                                                                   @Query("start_date") String startDate,
                                                                   @Query("end_date") String endDate,
                                                                   @Query("page") int page,
                                                                   @Query("limit") int limit);

    /**
     * Get call analytics
     */
    @GET("call-logs/analytics")
    Call<ApiResponse<CallAnalytics>> getCallAnalytics(@Header("Authorization") String token,
                                                      @Query("organization_id") String organizationId,
                                                      @Query("team_id") String teamId,
                                                      @Query("user_id") String userId,
                                                      @Query("period") String period);

    // ========== ENHANCED CRM TICKET ENDPOINTS - Phase 1 Backend Integration ==========

    /**
     * Create new ticket with enhanced backend integration
     */
    @POST("tickets")
    Call<ApiResponse<Ticket>> createTicket(@Header("Authorization") String token, @Body Ticket ticket);

    /**
     * Get tickets with enhanced filtering and pagination
     */
    @GET("tickets")
    Call<ApiResponse<java.util.List<Ticket>>> getTickets(@Header("Authorization") String token,
                                                          @Query("organization_id") String organizationId,
                                                          @Query("team_id") String teamId,
                                                          @Query("assigned_to") String assignedTo,
                                                          @Query("status") String status,
                                                          @Query("category") String category,
                                                          @Query("priority") String priority,
                                                          @Query("sla_status") String slaStatus,
                                                          @Query("page") int page,
                                                          @Query("limit") int limit);

    /**
     * Get specific ticket details
     */
    @GET("tickets/{id}")
    Call<ApiResponse<Ticket>> getTicket(@Header("Authorization") String token, @Path("id") String ticketId);

    /**
     * Update ticket information
     */
    @PUT("tickets/{id}")
    Call<ApiResponse<Ticket>> updateTicket(@Header("Authorization") String token, @Path("id") String ticketId, @Body Ticket ticket);

    /**
     * Delete ticket (org_admin only)
     */
    @DELETE("tickets/{id}")
    Call<ApiResponse<String>> deleteTicket(@Header("Authorization") String token, @Path("id") String ticketId);

    /**
     * Assign/reassign ticket to agent with enhanced assignment tracking
     */
    @POST("tickets/{id}/assign")
    Call<ApiResponse<Ticket>> assignTicket(@Header("Authorization") String token, @Path("id") String ticketId, @Body AssignTicketRequest request);

    /**
     * Update ticket status with enhanced status management
     */
    @PUT("tickets/{id}/status")
    Call<ApiResponse<Ticket>> updateTicketStatus(@Header("Authorization") String token, @Path("id") String ticketId, @Body UpdateTicketStatusRequest request);

    /**
     * Escalate ticket to higher priority/manager
     */
    @POST("tickets/{id}/escalate")
    Call<ApiResponse<Ticket>> escalateTicket(@Header("Authorization") String token, @Path("id") String ticketId, @Body EscalateTicketRequest request);

    /**
     * Add note to ticket with enhanced note management
     */
    @POST("tickets/{id}/notes")
    Call<ApiResponse<TicketNote>> addTicketNote(@Header("Authorization") String token, @Path("id") String ticketId, @Body TicketNote note);

    /**
     * Get ticket notes
     */
    @GET("tickets/{id}/notes")
    Call<ApiResponse<java.util.List<TicketNote>>> getTicketNotes(@Header("Authorization") String token, @Path("id") String ticketId);

    /**
     * Update ticket note
     */
    @PUT("tickets/{id}/notes/{noteId}")
    Call<ApiResponse<TicketNote>> updateTicketNote(@Header("Authorization") String token, @Path("id") String ticketId, @Path("noteId") String noteId, @Body TicketNote note);

    /**
     * Delete ticket note
     */
    @DELETE("tickets/{id}/notes/{noteId}")
    Call<ApiResponse<String>> deleteTicketNote(@Header("Authorization") String token, @Path("id") String ticketId, @Path("noteId") String noteId);

    /**
     * Submit customer satisfaction rating
     */
    @POST("tickets/{id}/satisfaction")
    Call<ApiResponse<Ticket>> submitSatisfactionRating(@Header("Authorization") String token, @Path("id") String ticketId, @Body SatisfactionRatingRequest request);

    /**
     * Get ticket statistics with enhanced analytics
     */
    @GET("tickets/stats")
    Call<ApiResponse<TicketStatsResponse>> getTicketStats(@Header("Authorization") String token,
                                                          @Query("organization_id") String organizationId,
                                                          @Query("team_id") String teamId,
                                                          @Query("user_id") String userId,
                                                          @Query("period") String period);

    /**
     * Get ticket analytics with SLA and performance metrics
     */
    @GET("tickets/analytics")
    Call<ApiResponse<TicketAnalytics>> getTicketAnalytics(@Header("Authorization") String token,
                                                          @Query("organization_id") String organizationId,
                                                          @Query("team_id") String teamId,
                                                          @Query("user_id") String userId,
                                                          @Query("period") String period);

    /**
     * Get conversion funnel data
     */
    @GET("tickets/analytics/conversion")
    Call<ApiResponse<ConversionAnalytics>> getConversionAnalytics(@Header("Authorization") String token,
                                                                  @Query("organization_id") String organizationId,
                                                                  @Query("period") String period);

    /**
     * Real-time ticket updates via Server-Sent Events (SSE)
     */
    @GET("tickets/stream")
    Call<String> getTicketStream(@Header("Authorization") String token,
                                @Query("organization_id") String organizationId,
                                @Query("team_id") String teamId);

    // ========== ANALYTICS ENDPOINTS ==========

    /**
     * Get user performance analytics
     */
    @GET("analytics/user/{id}")
    Call<ApiResponse<UserAnalytics>> getUserAnalytics(@Header("Authorization") String token, @Path("id") String userId, @Query("period") String period);

    /**
     * Get dashboard summary
     */
    @GET("analytics/dashboard")
    Call<ApiResponse<DashboardSummary>> getDashboardSummary(@Header("Authorization") String token, @Query("organization_id") String organizationId);

    // ========== RETROFIT CLIENT BUILDER ==========

    class ApiClient {
        private static ApiService instance;

        public static ApiService getInstance() {
            if (instance == null) {
                synchronized (ApiService.class) {
                    if (instance == null) {
                        instance = createApiService();
                    }
                }
            }
            return instance;
        }

        private static ApiService createApiService() {
            // Create logging interceptor for debugging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create OkHttp client with logging
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            // Create Retrofit instance
            return new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService.class);
        }
    }

    // Static method for easy access
    static ApiService getInstance() {
        return ApiClient.getInstance();
    }

    // ========== REQUEST/RESPONSE CLASSES ==========

    // NEW: Create Account Request
    class CreateAccountRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String organizationName;
        private String password;

        public CreateAccountRequest(String firstName, String lastName, String email,
                                    String phone, String organizationName, String password) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
            this.organizationName = organizationName;
            this.password = password;
        }

        // Getters
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getOrganizationName() { return organizationName; }
        public String getPassword() { return password; }

        // Setters
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public void setEmail(String email) { this.email = email; }
        public void setPhone(String phone) { this.phone = phone; }
        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
        public void setPassword(String password) { this.password = password; }
    }

    // NEW: Login Request
    class LoginRequest {
        private String email;
        private String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public void setEmail(String email) { this.email = email; }
        public void setPassword(String password) { this.password = password; }
    }

    // Existing inner classes
    class ForgotPasswordRequest {
        private String email;

        public ForgotPasswordRequest(String email) {
            this.email = email;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    class VerifyEmailRequest {
        private String code;

        public VerifyEmailRequest(String code) {
            this.code = code;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

    // ========== MULTI-TENANT REQUEST/RESPONSE CLASSES ==========

    class InviteUserRequest {
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private String organizationId;
        private java.util.List<String> teamIds;
        private java.util.List<String> permissions;

        public InviteUserRequest(String email, String firstName, String lastName, String role, String organizationId) {
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = role;
            this.organizationId = organizationId;
        }

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getOrganizationId() { return organizationId; }
        public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
        public java.util.List<String> getTeamIds() { return teamIds; }
        public void setTeamIds(java.util.List<String> teamIds) { this.teamIds = teamIds; }
        public java.util.List<String> getPermissions() { return permissions; }
        public void setPermissions(java.util.List<String> permissions) { this.permissions = permissions; }
    }

    class UpdateUserRequest {
        private String role;
        private java.util.List<String> teamIds;
        private java.util.List<String> permissions;
        private boolean isActive;

        // Getters and Setters
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public java.util.List<String> getTeamIds() { return teamIds; }
        public void setTeamIds(java.util.List<String> teamIds) { this.teamIds = teamIds; }
        public java.util.List<String> getPermissions() { return permissions; }
        public void setPermissions(java.util.List<String> permissions) { this.permissions = permissions; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
    }

    class UpdateProfileRequest {
        private String firstName;
        private String lastName;
        private String phone;

        public UpdateProfileRequest(String firstName, String lastName, String phone) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.phone = phone;
        }

        // Getters and Setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    class CreateTeamRequest {
        private String name;
        private String description;
        private String organizationId;
        private String managerId;
        private Team.TeamTargets targets;

        public CreateTeamRequest(String name, String description, String organizationId) {
            this.name = name;
            this.description = description;
            this.organizationId = organizationId;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getOrganizationId() { return organizationId; }
        public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
        public String getManagerId() { return managerId; }
        public void setManagerId(String managerId) { this.managerId = managerId; }
        public Team.TeamTargets getTargets() { return targets; }
        public void setTargets(Team.TeamTargets targets) { this.targets = targets; }
    }

    class UpdateTeamRequest {
        private String name;
        private String description;
        private String managerId;
        private Team.TeamTargets targets;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getManagerId() { return managerId; }
        public void setManagerId(String managerId) { this.managerId = managerId; }
        public Team.TeamTargets getTargets() { return targets; }
        public void setTargets(Team.TeamTargets targets) { this.targets = targets; }
    }

    class AddTeamMemberRequest {
        private String userId;
        private String role;

        public AddTeamMemberRequest(String userId, String role) {
            this.userId = userId;
            this.role = role;
        }

        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    class AssignContactRequest {
        private String agentId;
        private String teamId;

        public AssignContactRequest(String agentId) {
            this.agentId = agentId;
        }

        // Getters and Setters
        public String getAgentId() { return agentId; }
        public void setAgentId(String agentId) { this.agentId = agentId; }
        public String getTeamId() { return teamId; }
        public void setTeamId(String teamId) { this.teamId = teamId; }
    }

    // ========== ENHANCED TICKET REQUEST CLASSES ==========

    class AssignTicketRequest {
        private String assignedTo;
        private String assignedTeam;
        private String assignmentNote;

        public AssignTicketRequest(String assignedTo) {
            this.assignedTo = assignedTo;
        }

        public AssignTicketRequest(String assignedTo, String assignedTeam, String assignmentNote) {
            this.assignedTo = assignedTo;
            this.assignedTeam = assignedTeam;
            this.assignmentNote = assignmentNote;
        }

        // Getters and Setters
        public String getAssignedTo() { return assignedTo; }
        public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
        public String getAssignedTeam() { return assignedTeam; }
        public void setAssignedTeam(String assignedTeam) { this.assignedTeam = assignedTeam; }
        public String getAssignmentNote() { return assignmentNote; }
        public void setAssignmentNote(String assignmentNote) { this.assignmentNote = assignmentNote; }
    }

    class UpdateTicketStatusRequest {
        private String status;
        private String category;
        private String priority;
        private String statusNote;

        public UpdateTicketStatusRequest(String status) {
            this.status = status;
        }

        public UpdateTicketStatusRequest(String status, String category, String priority, String statusNote) {
            this.status = status;
            this.category = category;
            this.priority = priority;
            this.statusNote = statusNote;
        }

        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public String getStatusNote() { return statusNote; }
        public void setStatusNote(String statusNote) { this.statusNote = statusNote; }
    }

    class EscalateTicketRequest {
        private String escalatedTo;
        private String escalationReason;
        private String priority;

        public EscalateTicketRequest(String escalatedTo, String escalationReason) {
            this.escalatedTo = escalatedTo;
            this.escalationReason = escalationReason;
        }

        public EscalateTicketRequest(String escalatedTo, String escalationReason, String priority) {
            this.escalatedTo = escalatedTo;
            this.escalationReason = escalationReason;
            this.priority = priority;
        }

        // Getters and Setters
        public String getEscalatedTo() { return escalatedTo; }
        public void setEscalatedTo(String escalatedTo) { this.escalatedTo = escalatedTo; }
        public String getEscalationReason() { return escalationReason; }
        public void setEscalationReason(String escalationReason) { this.escalationReason = escalationReason; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }

    class SatisfactionRatingRequest {
        private int rating;
        private String feedback;

        public SatisfactionRatingRequest(int rating) {
            this.rating = rating;
        }

        public SatisfactionRatingRequest(int rating, String feedback) {
            this.rating = rating;
            this.feedback = feedback;
        }

        // Getters and Setters
        public int getRating() { return rating; }
        public void setRating(int rating) { this.rating = rating; }
        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
    }

    // ========== ENHANCED RESPONSE CLASSES ==========

    class TicketStatsResponse {
        private int totalTickets;
        private int openTickets;
        private int inProgressTickets;
        private int resolvedTickets;
        private int closedTickets;
        private int escalatedTickets;
        private int slaBreachedTickets;
        private double averageResolutionTime;
        private double averageSatisfactionRating;
        private java.util.List<CategoryStats> categoryStats;
        private java.util.List<PriorityStats> priorityStats;

        // Getters and Setters
        public int getTotalTickets() { return totalTickets; }
        public void setTotalTickets(int totalTickets) { this.totalTickets = totalTickets; }
        public int getOpenTickets() { return openTickets; }
        public void setOpenTickets(int openTickets) { this.openTickets = openTickets; }
        public int getInProgressTickets() { return inProgressTickets; }
        public void setInProgressTickets(int inProgressTickets) { this.inProgressTickets = inProgressTickets; }
        public int getResolvedTickets() { return resolvedTickets; }
        public void setResolvedTickets(int resolvedTickets) { this.resolvedTickets = resolvedTickets; }
        public int getClosedTickets() { return closedTickets; }
        public void setClosedTickets(int closedTickets) { this.closedTickets = closedTickets; }
        public int getEscalatedTickets() { return escalatedTickets; }
        public void setEscalatedTickets(int escalatedTickets) { this.escalatedTickets = escalatedTickets; }
        public int getSlaBreachedTickets() { return slaBreachedTickets; }
        public void setSlaBreachedTickets(int slaBreachedTickets) { this.slaBreachedTickets = slaBreachedTickets; }
        public double getAverageResolutionTime() { return averageResolutionTime; }
        public void setAverageResolutionTime(double averageResolutionTime) { this.averageResolutionTime = averageResolutionTime; }
        public double getAverageSatisfactionRating() { return averageSatisfactionRating; }
        public void setAverageSatisfactionRating(double averageSatisfactionRating) { this.averageSatisfactionRating = averageSatisfactionRating; }
        public java.util.List<CategoryStats> getCategoryStats() { return categoryStats; }
        public void setCategoryStats(java.util.List<CategoryStats> categoryStats) { this.categoryStats = categoryStats; }
        public java.util.List<PriorityStats> getPriorityStats() { return priorityStats; }
        public void setPriorityStats(java.util.List<PriorityStats> priorityStats) { this.priorityStats = priorityStats; }

        public static class CategoryStats {
            private String category;
            private int count;
            private double averageResolutionTime;

            // Getters and Setters
            public String getCategory() { return category; }
            public void setCategory(String category) { this.category = category; }
            public int getCount() { return count; }
            public void setCount(int count) { this.count = count; }
            public double getAverageResolutionTime() { return averageResolutionTime; }
            public void setAverageResolutionTime(double averageResolutionTime) { this.averageResolutionTime = averageResolutionTime; }
        }

        public static class PriorityStats {
            private String priority;
            private int count;
            private double averageResolutionTime;

            // Getters and Setters
            public String getPriority() { return priority; }
            public void setPriority(String priority) { this.priority = priority; }
            public int getCount() { return count; }
            public void setCount(int count) { this.count = count; }
            public double getAverageResolutionTime() { return averageResolutionTime; }
            public void setAverageResolutionTime(double averageResolutionTime) { this.averageResolutionTime = averageResolutionTime; }
        }
    }

    // CRM Ticket Analytics response classes
    class TicketAnalytics {
        private int totalTickets;
        private int newTickets;
        private int inProgressTickets;
        private int closedTickets;
        private double conversionRate;
        private double averageResponseTime;
        private java.util.List<StageStats> stageStats;

        // Getters and Setters
        public int getTotalTickets() { return totalTickets; }
        public void setTotalTickets(int totalTickets) { this.totalTickets = totalTickets; }
        public int getNewTickets() { return newTickets; }
        public void setNewTickets(int newTickets) { this.newTickets = newTickets; }
        public int getInProgressTickets() { return inProgressTickets; }
        public void setInProgressTickets(int inProgressTickets) { this.inProgressTickets = inProgressTickets; }
        public int getClosedTickets() { return closedTickets; }
        public void setClosedTickets(int closedTickets) { this.closedTickets = closedTickets; }
        public double getConversionRate() { return conversionRate; }
        public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        public java.util.List<StageStats> getStageStats() { return stageStats; }
        public void setStageStats(java.util.List<StageStats> stageStats) { this.stageStats = stageStats; }

        public static class StageStats {
            private String stage;
            private int count;
            private double percentage;

            // Getters and Setters
            public String getStage() { return stage; }
            public void setStage(String stage) { this.stage = stage; }
            public int getCount() { return count; }
            public void setCount(int count) { this.count = count; }
            public double getPercentage() { return percentage; }
            public void setPercentage(double percentage) { this.percentage = percentage; }
        }
    }

    class ConversionAnalytics {
        private String organizationId;
        private String period;
        private java.util.List<ConversionStage> conversionFunnel;
        private double overallConversionRate;

        // Getters and Setters
        public String getOrganizationId() { return organizationId; }
        public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public java.util.List<ConversionStage> getConversionFunnel() { return conversionFunnel; }
        public void setConversionFunnel(java.util.List<ConversionStage> conversionFunnel) { this.conversionFunnel = conversionFunnel; }
        public double getOverallConversionRate() { return overallConversionRate; }
        public void setOverallConversionRate(double overallConversionRate) { this.overallConversionRate = overallConversionRate; }

        public static class ConversionStage {
            private String stage;
            private int count;
            private double conversionRate;

            // Getters and Setters
            public String getStage() { return stage; }
            public void setStage(String stage) { this.stage = stage; }
            public int getCount() { return count; }
            public void setCount(int count) { this.count = count; }
            public double getConversionRate() { return conversionRate; }
            public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }
        }
    }

    // Analytics response classes
    class CallAnalytics {
        private int totalCalls;
        private int dailyCalls;
        private int weeklyCalls;
        private int monthlyCalls;
        private double averageDuration;
        private double conversionRate;
        private java.util.List<DailyStats> dailyStats;

        // Getters and Setters
        public int getTotalCalls() { return totalCalls; }
        public void setTotalCalls(int totalCalls) { this.totalCalls = totalCalls; }
        public int getDailyCalls() { return dailyCalls; }
        public void setDailyCalls(int dailyCalls) { this.dailyCalls = dailyCalls; }
        public int getWeeklyCalls() { return weeklyCalls; }
        public void setWeeklyCalls(int weeklyCalls) { this.weeklyCalls = weeklyCalls; }
        public int getMonthlyCalls() { return monthlyCalls; }
        public void setMonthlyCalls(int monthlyCalls) { this.monthlyCalls = monthlyCalls; }
        public double getAverageDuration() { return averageDuration; }
        public void setAverageDuration(double averageDuration) { this.averageDuration = averageDuration; }
        public double getConversionRate() { return conversionRate; }
        public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }
        public java.util.List<DailyStats> getDailyStats() { return dailyStats; }
        public void setDailyStats(java.util.List<DailyStats> dailyStats) { this.dailyStats = dailyStats; }

        public static class DailyStats {
            private String date;
            private int calls;
            private double duration;

            // Getters and Setters
            public String getDate() { return date; }
            public void setDate(String date) { this.date = date; }
            public int getCalls() { return calls; }
            public void setCalls(int calls) { this.calls = calls; }
            public double getDuration() { return duration; }
            public void setDuration(double duration) { this.duration = duration; }
        }
    }

    class UserAnalytics {
        private String userId;
        private String period;
        private int totalCalls;
        private double averageDuration;
        private int contactsCreated;
        private double conversionRate;
        private java.util.List<String> topPerformingDays;

        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public int getTotalCalls() { return totalCalls; }
        public void setTotalCalls(int totalCalls) { this.totalCalls = totalCalls; }
        public double getAverageDuration() { return averageDuration; }
        public void setAverageDuration(double averageDuration) { this.averageDuration = averageDuration; }
        public int getContactsCreated() { return contactsCreated; }
        public void setContactsCreated(int contactsCreated) { this.contactsCreated = contactsCreated; }
        public double getConversionRate() { return conversionRate; }
        public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }
        public java.util.List<String> getTopPerformingDays() { return topPerformingDays; }
        public void setTopPerformingDays(java.util.List<String> topPerformingDays) { this.topPerformingDays = topPerformingDays; }
    }

    class DashboardSummary {
        private String organizationId;
        private Organization.OrganizationAnalytics orgAnalytics;
        private java.util.List<Team.TeamAnalytics> teamSummaries;
        private java.util.List<User> topPerformers;
        private CallAnalytics recentCallStats;

        // Getters and Setters
        public String getOrganizationId() { return organizationId; }
        public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
        public Organization.OrganizationAnalytics getOrgAnalytics() { return orgAnalytics; }
        public void setOrgAnalytics(Organization.OrganizationAnalytics orgAnalytics) { this.orgAnalytics = orgAnalytics; }
        public java.util.List<Team.TeamAnalytics> getTeamSummaries() { return teamSummaries; }
        public void setTeamSummaries(java.util.List<Team.TeamAnalytics> teamSummaries) { this.teamSummaries = teamSummaries; }
        public java.util.List<User> getTopPerformers() { return topPerformers; }
        public void setTopPerformers(java.util.List<User> topPerformers) { this.topPerformers = topPerformers; }
        public CallAnalytics getRecentCallStats() { return recentCallStats; }
        public void setRecentCallStats(CallAnalytics recentCallStats) { this.recentCallStats = recentCallStats; }
    }
}