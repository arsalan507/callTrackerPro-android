package com.calltrackerpro.calltracker.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class User {
    @SerializedName("_id")
    private String userId;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    @SerializedName("organization")
    private String organizationId;

    @SerializedName("permissions")
    private List<String> permissions;

    @SerializedName("callCount")
    private int callCount;

    @SerializedName("callLimit")
    private int callLimit;

    @SerializedName("subscriptionPlan")
    private String subscriptionPlan;

    // Constructors
    public User() {}

    public User(String email, String password) {
        this.email = email;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }

    public int getCallCount() { return callCount; }
    public void setCallCount(int callCount) { this.callCount = callCount; }

    public int getCallLimit() { return callLimit; }
    public void setCallLimit(int callLimit) { this.callLimit = callLimit; }

    public String getSubscriptionPlan() { return subscriptionPlan; }
    public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "").trim();
    }

    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    public boolean isManager() {
        return "manager".equalsIgnoreCase(role) || "admin".equalsIgnoreCase(role);
    }
}