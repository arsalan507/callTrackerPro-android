package com.calltrackerpro.calltracker.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Organization {
    @SerializedName("_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("domain")
    private String domain;

    @SerializedName("subscription")
    private Subscription subscription;

    @SerializedName("settings")
    private OrganizationSettings settings;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("analytics")
    private OrganizationAnalytics analytics;

    // Constructors
    public Organization() {}

    public Organization(String id, String name, String domain) {
        this.id = id;
        this.name = name;
        this.domain = domain;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public Subscription getSubscription() { return subscription; }
    public void setSubscription(Subscription subscription) { this.subscription = subscription; }

    public OrganizationSettings getSettings() { return settings; }
    public void setSettings(OrganizationSettings settings) { this.settings = settings; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public OrganizationAnalytics getAnalytics() { return analytics; }
    public void setAnalytics(OrganizationAnalytics analytics) { this.analytics = analytics; }
    
    // Convenience methods for easier access to nested properties
    public String getSubscriptionStatus() {
        return subscription != null ? subscription.getStatus() : "unknown";
    }
    
    public String getSubscriptionPlan() {
        return subscription != null ? subscription.getPlan() : "No Plan";
    }
    
    public int getUserLimit() {
        return subscription != null ? subscription.getUserLimit() : 0;
    }
    
    public int getCallLimit() {
        return subscription != null ? subscription.getCallLimit() : 0;
    }
    
    public boolean isActive() {
        return subscription != null && subscription.isActive();
    }
    
    public int getUserCount() {
        return analytics != null ? analytics.getTotalUsers() : 0;
    }
    
    public int getTicketCount() {
        // TODO: Add ticket count to analytics when available from backend
        return 0; // Placeholder
    }
    
    public String getLastActivity() {
        // TODO: Add last activity tracking to organization model
        return null; // Placeholder
    }

    // Inner classes for nested objects
    public static class Subscription {
        @SerializedName("plan")
        private String plan;

        @SerializedName("status")
        private String status;

        @SerializedName("user_limit")
        private int userLimit;

        @SerializedName("call_limit")
        private int callLimit;

        @SerializedName("expires_at")
        private String expiresAt;

        // Getters and Setters
        public String getPlan() { return plan; }
        public void setPlan(String plan) { this.plan = plan; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public int getUserLimit() { return userLimit; }
        public void setUserLimit(int userLimit) { this.userLimit = userLimit; }

        public int getCallLimit() { return callLimit; }
        public void setCallLimit(int callLimit) { this.callLimit = callLimit; }

        public String getExpiresAt() { return expiresAt; }
        public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

        public boolean isActive() { return "active".equals(status); }
    }

    public static class OrganizationSettings {
        @SerializedName("call_recording_enabled")
        private boolean callRecordingEnabled;

        @SerializedName("auto_sync_enabled")
        private boolean autoSyncEnabled;

        @SerializedName("timezone")
        private String timezone;

        @SerializedName("business_hours")
        private BusinessHours businessHours;

        // Getters and Setters
        public boolean isCallRecordingEnabled() { return callRecordingEnabled; }
        public void setCallRecordingEnabled(boolean callRecordingEnabled) { this.callRecordingEnabled = callRecordingEnabled; }

        public boolean isAutoSyncEnabled() { return autoSyncEnabled; }
        public void setAutoSyncEnabled(boolean autoSyncEnabled) { this.autoSyncEnabled = autoSyncEnabled; }

        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }

        public BusinessHours getBusinessHours() { return businessHours; }
        public void setBusinessHours(BusinessHours businessHours) { this.businessHours = businessHours; }
    }

    public static class BusinessHours {
        @SerializedName("start_time")
        private String startTime;

        @SerializedName("end_time")
        private String endTime;

        @SerializedName("days")
        private List<String> days;

        // Getters and Setters
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }

        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }

        public List<String> getDays() { return days; }
        public void setDays(List<String> days) { this.days = days; }
    }

    public static class OrganizationAnalytics {
        @SerializedName("total_users")
        private int totalUsers;

        @SerializedName("total_calls")
        private int totalCalls;

        @SerializedName("active_users")
        private int activeUsers;

        @SerializedName("monthly_calls")
        private int monthlyCalls;

        // Getters and Setters
        public int getTotalUsers() { return totalUsers; }
        public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }

        public int getTotalCalls() { return totalCalls; }
        public void setTotalCalls(int totalCalls) { this.totalCalls = totalCalls; }

        public int getActiveUsers() { return activeUsers; }
        public void setActiveUsers(int activeUsers) { this.activeUsers = activeUsers; }

        public int getMonthlyCalls() { return monthlyCalls; }
        public void setMonthlyCalls(int monthlyCalls) { this.monthlyCalls = monthlyCalls; }
    }
}