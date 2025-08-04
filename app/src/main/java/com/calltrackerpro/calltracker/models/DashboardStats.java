package com.calltrackerpro.calltracker.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DashboardStats {
    @SerializedName("user_stats")
    private UserStats userStats;
    
    @SerializedName("organization_stats")
    private OrganizationStats organizationStats;
    
    @SerializedName("recent_activity")
    private List<ActivityItem> recentActivity;
    
    @SerializedName("performance_metrics")
    private PerformanceMetrics performanceMetrics;

    public static class UserStats {
        @SerializedName("total_calls")
        private int totalCalls;
        
        @SerializedName("calls_today")
        private int callsToday;
        
        @SerializedName("tickets_assigned")
        private int ticketsAssigned;
        
        @SerializedName("tickets_resolved")
        private int ticketsResolved;
        
        @SerializedName("success_rate")
        private double successRate;
        
        @SerializedName("active_hours")
        private String activeHours;

        // Getters and Setters
        public int getTotalCalls() { return totalCalls; }
        public void setTotalCalls(int totalCalls) { this.totalCalls = totalCalls; }
        
        public int getCallsToday() { return callsToday; }
        public void setCallsToday(int callsToday) { this.callsToday = callsToday; }
        
        public int getTicketsAssigned() { return ticketsAssigned; }
        public void setTicketsAssigned(int ticketsAssigned) { this.ticketsAssigned = ticketsAssigned; }
        
        public int getTicketsResolved() { return ticketsResolved; }
        public void setTicketsResolved(int ticketsResolved) { this.ticketsResolved = ticketsResolved; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public String getActiveHours() { return activeHours; }
        public void setActiveHours(String activeHours) { this.activeHours = activeHours; }
    }

    public static class OrganizationStats {
        @SerializedName("total_users")
        private int totalUsers;
        
        @SerializedName("active_users")
        private int activeUsers;
        
        @SerializedName("total_tickets")
        private int totalTickets;
        
        @SerializedName("open_tickets")
        private int openTickets;
        
        @SerializedName("resolved_tickets")
        private int resolvedTickets;
        
        @SerializedName("subscription_status")
        private String subscriptionStatus;
        
        @SerializedName("organization_name")
        private String organizationName;

        // Getters and Setters
        public int getTotalUsers() { return totalUsers; }
        public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
        
        public int getActiveUsers() { return activeUsers; }
        public void setActiveUsers(int activeUsers) { this.activeUsers = activeUsers; }
        
        public int getTotalTickets() { return totalTickets; }
        public void setTotalTickets(int totalTickets) { this.totalTickets = totalTickets; }
        
        public int getOpenTickets() { return openTickets; }
        public void setOpenTickets(int openTickets) { this.openTickets = openTickets; }
        
        public int getResolvedTickets() { return resolvedTickets; }
        public void setResolvedTickets(int resolvedTickets) { this.resolvedTickets = resolvedTickets; }
        
        public String getSubscriptionStatus() { return subscriptionStatus; }
        public void setSubscriptionStatus(String subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }
        
        public String getOrganizationName() { return organizationName; }
        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    }

    public static class ActivityItem {
        @SerializedName("id")
        private String id;
        
        @SerializedName("type")
        private String type; // "call_completed", "ticket_created", "ticket_assigned", etc.
        
        @SerializedName("title")
        private String title;
        
        @SerializedName("description")
        private String description;
        
        @SerializedName("timestamp")
        private String timestamp;
        
        @SerializedName("user_name")
        private String userName;
        
        @SerializedName("priority")
        private String priority;
        
        @SerializedName("status")
        private String status;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getDisplayTime() {
            // Convert timestamp to relative time display
            // This is a simplified version - you might want to use a library
            try {
                long timestampLong = Long.parseLong(timestamp);
                long now = System.currentTimeMillis();
                long diff = now - timestampLong;
                
                if (diff < 60000) {
                    return "Just now";
                } else if (diff < 3600000) {
                    return (diff / 60000) + " min ago";
                } else if (diff < 86400000) {
                    return (diff / 3600000) + " hr ago";
                } else {
                    return (diff / 86400000) + " days ago";
                }
            } catch (Exception e) {
                return timestamp;
            }
        }
    }

    public static class PerformanceMetrics {
        @SerializedName("conversion_rate")
        private double conversionRate;
        
        @SerializedName("average_call_duration")
        private double averageCallDuration;
        
        @SerializedName("response_time")
        private double responseTime;
        
        @SerializedName("customer_satisfaction")
        private double customerSatisfaction;
        
        @SerializedName("sla_compliance")
        private double slaCompliance;

        // Getters and Setters
        public double getConversionRate() { return conversionRate; }
        public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }
        
        public double getAverageCallDuration() { return averageCallDuration; }
        public void setAverageCallDuration(double averageCallDuration) { this.averageCallDuration = averageCallDuration; }
        
        public double getResponseTime() { return responseTime; }
        public void setResponseTime(double responseTime) { this.responseTime = responseTime; }
        
        public double getCustomerSatisfaction() { return customerSatisfaction; }
        public void setCustomerSatisfaction(double customerSatisfaction) { this.customerSatisfaction = customerSatisfaction; }
        
        public double getSlaCompliance() { return slaCompliance; }
        public void setSlaCompliance(double slaCompliance) { this.slaCompliance = slaCompliance; }
    }

    // Main class getters and setters
    public UserStats getUserStats() { return userStats; }
    public void setUserStats(UserStats userStats) { this.userStats = userStats; }
    
    public OrganizationStats getOrganizationStats() { return organizationStats; }
    public void setOrganizationStats(OrganizationStats organizationStats) { this.organizationStats = organizationStats; }
    
    public List<ActivityItem> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<ActivityItem> recentActivity) { this.recentActivity = recentActivity; }
    
    public PerformanceMetrics getPerformanceMetrics() { return performanceMetrics; }
    public void setPerformanceMetrics(PerformanceMetrics performanceMetrics) { this.performanceMetrics = performanceMetrics; }
}