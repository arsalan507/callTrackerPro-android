package com.calltrackerpro.calltracker.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password; // Only used for requests, not stored

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("role")
    private String role;

    @SerializedName("call_count")
    private int callCount;

    @SerializedName("call_limit")
    private int callLimit;

    @SerializedName("organization_id")
    private String organizationId;

    @SerializedName("organizations")
    private java.util.List<Organization> organizations;

    @SerializedName("current_organization")
    private Organization currentOrganization;

    @SerializedName("team_ids")
    private java.util.List<String> teamIds;

    @SerializedName("teams")
    private java.util.List<Team> teams;

    @SerializedName("permissions")
    private java.util.List<String> permissions;

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("last_login")
    private String lastLogin;

    // Constructors
    public User() {}

    // Constructor for login
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Constructor for registration
    public User(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getRole() {
        return role != null ? role : "user";
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getCallCount() {
        return callCount;
    }

    public void setCallCount(int callCount) {
        this.callCount = callCount;
    }

    public int getCallLimit() {
        return callLimit;
    }

    public void setCallLimit(int callLimit) {
        this.callLimit = callLimit;
    }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public java.util.List<Organization> getOrganizations() { return organizations; }
    public void setOrganizations(java.util.List<Organization> organizations) { this.organizations = organizations; }

    public Organization getCurrentOrganization() { return currentOrganization; }
    public void setCurrentOrganization(Organization currentOrganization) { this.currentOrganization = currentOrganization; }

    public java.util.List<String> getTeamIds() { return teamIds; }
    public void setTeamIds(java.util.List<String> teamIds) { this.teamIds = teamIds; }

    public java.util.List<Team> getTeams() { return teams; }
    public void setTeams(java.util.List<Team> teams) { this.teams = teams; }

    public java.util.List<String> getPermissions() { return permissions; }
    public void setPermissions(java.util.List<String> permissions) { this.permissions = permissions; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }

    // Utility methods
    public String getFullName() {
        if (name != null && !name.isEmpty()) {
            return name;
        }

        StringBuilder fullName = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            fullName.append(firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(lastName);
        }

        return fullName.length() > 0 ? fullName.toString() : email;
    }

    public String getDisplayName() {
        String fullName = getFullName();
        return fullName != null && !fullName.equals(email) ? fullName : email;
    }

    // Role-based utility methods
    public boolean isOrganizationAdmin() { return "org_admin".equals(role); }
    public boolean isManager() { return "manager".equals(role); }
    public boolean isAgent() { return "agent".equals(role); }
    public boolean isViewer() { return "viewer".equals(role); }

    // Permission checking methods
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    public boolean hasAnyPermission(String... permissionList) {
        if (permissions == null) return false;
        for (String permission : permissionList) {
            if (permissions.contains(permission)) return true;
        }
        return false;
    }

    // Organization-related methods
    public boolean hasMultipleOrganizations() {
        return organizations != null && organizations.size() > 1;
    }

    public boolean belongsToOrganization(String orgId) {
        if (organizations == null || orgId == null) return false;
        return organizations.stream().anyMatch(org -> orgId.equals(org.getId()));
    }

    // Team-related methods
    public boolean isInTeam(String teamId) {
        return teamIds != null && teamIds.contains(teamId);
    }

    public boolean isManagerOfTeam(String teamId) {
        if (!isManager() || teams == null) return false;
        return teams.stream().anyMatch(team -> teamId.equals(team.getId()) && getId().equals(team.getManagerId()));
    }

    public String getRoleDisplayName() {
        switch (getRole()) {
            case "org_admin": return "Organization Admin";
            case "manager": return "Manager";
            case "agent": return "Agent";
            case "viewer": return "Viewer";
            default: return "User";
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}