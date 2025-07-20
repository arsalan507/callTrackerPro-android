package com.calltrackerpro.calltracker.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Team {
    @SerializedName("_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("organization_id")
    private String organizationId;

    @SerializedName("manager_id")
    private String managerId;

    @SerializedName("manager")
    private User manager;

    @SerializedName("members")
    private List<User> members;

    @SerializedName("targets")
    private TeamTargets targets;

    @SerializedName("analytics")
    private TeamAnalytics analytics;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Constructors
    public Team() {}

    public Team(String name, String description, String organizationId) {
        this.name = name;
        this.description = description;
        this.organizationId = organizationId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }

    public User getManager() { return manager; }
    public void setManager(User manager) { this.manager = manager; }

    public List<User> getMembers() { return members; }
    public void setMembers(List<User> members) { this.members = members; }

    public TeamTargets getTargets() { return targets; }
    public void setTargets(TeamTargets targets) { this.targets = targets; }

    public TeamAnalytics getAnalytics() { return analytics; }
    public void setAnalytics(TeamAnalytics analytics) { this.analytics = analytics; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Utility methods
    public int getMemberCount() {
        return members != null ? members.size() : 0;
    }

    public boolean isUserMember(String userId) {
        if (members == null || userId == null) return false;
        return members.stream().anyMatch(member -> userId.equals(member.getId()));
    }

    public boolean isUserManager(String userId) {
        return userId != null && userId.equals(managerId);
    }

    // Inner classes
    public static class TeamTargets {
        @SerializedName("daily_calls")
        private int dailyCalls;

        @SerializedName("weekly_calls")
        private int weeklyCalls;

        @SerializedName("monthly_calls")
        private int monthlyCalls;

        @SerializedName("conversion_rate")
        private double conversionRate;

        // Getters and Setters
        public int getDailyCalls() { return dailyCalls; }
        public void setDailyCalls(int dailyCalls) { this.dailyCalls = dailyCalls; }

        public int getWeeklyCalls() { return weeklyCalls; }
        public void setWeeklyCalls(int weeklyCalls) { this.weeklyCalls = weeklyCalls; }

        public int getMonthlyCalls() { return monthlyCalls; }
        public void setMonthlyCalls(int monthlyCalls) { this.monthlyCalls = monthlyCalls; }

        public double getConversionRate() { return conversionRate; }
        public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }
    }

    public static class TeamAnalytics {
        @SerializedName("total_calls")
        private int totalCalls;

        @SerializedName("daily_calls")
        private int dailyCalls;

        @SerializedName("weekly_calls")
        private int weeklyCalls;

        @SerializedName("monthly_calls")
        private int monthlyCalls;

        @SerializedName("average_call_duration")
        private int averageCallDuration;

        @SerializedName("conversion_rate")
        private double conversionRate;

        @SerializedName("top_performer")
        private User topPerformer;

        // Getters and Setters
        public int getTotalCalls() { return totalCalls; }
        public void setTotalCalls(int totalCalls) { this.totalCalls = totalCalls; }

        public int getDailyCalls() { return dailyCalls; }
        public void setDailyCalls(int dailyCalls) { this.dailyCalls = dailyCalls; }

        public int getWeeklyCalls() { return weeklyCalls; }
        public void setWeeklyCalls(int weeklyCalls) { this.weeklyCalls = weeklyCalls; }

        public int getMonthlyCalls() { return monthlyCalls; }
        public void setMonthlyCalls(int monthlyCalls) { this.monthlyCalls = monthlyCalls; }

        public int getAverageCallDuration() { return averageCallDuration; }
        public void setAverageCallDuration(int averageCallDuration) { this.averageCallDuration = averageCallDuration; }

        public double getConversionRate() { return conversionRate; }
        public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }

        public User getTopPerformer() { return topPerformer; }
        public void setTopPerformer(User topPerformer) { this.topPerformer = topPerformer; }
    }
}