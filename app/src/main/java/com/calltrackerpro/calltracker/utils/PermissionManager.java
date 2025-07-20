package com.calltrackerpro.calltracker.utils;

import com.calltrackerpro.calltracker.models.User;

public class PermissionManager {
    
    // Organization Management Permissions
    public static final String MANAGE_USERS = "manage_users";
    public static final String INVITE_USERS = "invite_users";
    public static final String DELETE_USERS = "delete_users";
    public static final String MANAGE_TEAMS = "manage_teams";
    public static final String VIEW_ORG_ANALYTICS = "view_org_analytics";
    public static final String MANAGE_ORG_SETTINGS = "manage_org_settings";
    
    // Team Management Permissions
    public static final String MANAGE_TEAM_MEMBERS = "manage_team_members";
    public static final String VIEW_TEAM_ANALYTICS = "view_team_analytics";
    public static final String ASSIGN_LEADS = "assign_leads";
    public static final String VIEW_TEAM_CALLS = "view_team_calls";
    
    // Contact/Lead Management Permissions
    public static final String CREATE_CONTACTS = "create_contacts";
    public static final String EDIT_CONTACTS = "edit_contacts";
    public static final String DELETE_CONTACTS = "delete_contacts";
    public static final String VIEW_CONTACTS = "view_contacts";
    public static final String EXPORT_CONTACTS = "export_contacts";
    
    // Call Management Permissions
    public static final String VIEW_CALLS = "view_calls";
    public static final String RECORD_CALLS = "record_calls";
    public static final String DELETE_CALLS = "delete_calls";
    public static final String EXPORT_CALLS = "export_calls";
    
    // Analytics and Reporting Permissions
    public static final String VIEW_ANALYTICS = "view_analytics";
    public static final String EXPORT_REPORTS = "export_reports";
    public static final String VIEW_INDIVIDUAL_PERFORMANCE = "view_individual_performance";
    
    private final User currentUser;
    
    public PermissionManager(User user) {
        this.currentUser = user;
    }
    
    // Organization Admin permissions
    public boolean canManageUsers() {
        return currentUser.isOrganizationAdmin() || currentUser.hasPermission(MANAGE_USERS);
    }
    
    public boolean canInviteUsers() {
        return currentUser.isOrganizationAdmin() || currentUser.hasPermission(INVITE_USERS);
    }
    
    public boolean canDeleteUsers() {
        return currentUser.isOrganizationAdmin() || currentUser.hasPermission(DELETE_USERS);
    }
    
    public boolean canManageTeams() {
        return currentUser.isOrganizationAdmin() || currentUser.hasPermission(MANAGE_TEAMS);
    }
    
    public boolean canViewOrgAnalytics() {
        return currentUser.isOrganizationAdmin() || currentUser.hasPermission(VIEW_ORG_ANALYTICS);
    }
    
    public boolean canManageOrgSettings() {
        return currentUser.isOrganizationAdmin() || currentUser.hasPermission(MANAGE_ORG_SETTINGS);
    }
    
    // Manager permissions
    public boolean canManageTeamMembers() {
        return currentUser.isOrganizationAdmin() || 
               currentUser.isManager() || 
               currentUser.hasPermission(MANAGE_TEAM_MEMBERS);
    }
    
    public boolean canViewTeamAnalytics() {
        return currentUser.isOrganizationAdmin() || 
               currentUser.isManager() || 
               currentUser.hasPermission(VIEW_TEAM_ANALYTICS);
    }
    
    public boolean canAssignLeads() {
        return currentUser.isOrganizationAdmin() || 
               currentUser.isManager() || 
               currentUser.hasPermission(ASSIGN_LEADS);
    }
    
    public boolean canViewTeamCalls() {
        return currentUser.isOrganizationAdmin() || 
               currentUser.isManager() || 
               currentUser.hasPermission(VIEW_TEAM_CALLS);
    }
    
    // Contact/Lead management permissions
    public boolean canCreateContacts() {
        return !currentUser.isViewer() && 
               (currentUser.hasPermission(CREATE_CONTACTS) || 
                currentUser.isAgent() || 
                currentUser.isManager() || 
                currentUser.isOrganizationAdmin());
    }
    
    public boolean canEditContacts() {
        return !currentUser.isViewer() && 
               (currentUser.hasPermission(EDIT_CONTACTS) || 
                currentUser.isAgent() || 
                currentUser.isManager() || 
                currentUser.isOrganizationAdmin());
    }
    
    public boolean canDeleteContacts() {
        return currentUser.isOrganizationAdmin() || 
               currentUser.isManager() || 
               currentUser.hasPermission(DELETE_CONTACTS);
    }
    
    public boolean canViewContacts() {
        return currentUser.hasPermission(VIEW_CONTACTS) || 
               !currentUser.getRole().isEmpty(); // All authenticated users can view
    }
    
    public boolean canExportContacts() {
        return currentUser.isOrganizationAdmin() || 
               currentUser.isManager() || 
               currentUser.hasPermission(EXPORT_CONTACTS);
    }
    
    // Call management permissions
    public boolean canViewCalls() {
        return currentUser.hasPermission(VIEW_CALLS) || 
               !currentUser.getRole().isEmpty(); // All authenticated users can view their calls
    }
    
    public boolean canRecordCalls() {
        return !currentUser.isViewer() && 
               (currentUser.hasPermission(RECORD_CALLS) || 
                currentUser.isAgent() || 
                currentUser.isManager() || 
                currentUser.isOrganizationAdmin());
    }
    
    public boolean canDeleteCalls() {
        return currentUser.isOrganizationAdmin() || 
               currentUser.hasPermission(DELETE_CALLS);
    }
    
    public boolean canExportCalls() {
        return currentUser.isOrganizationAdmin() || 
               currentUser.isManager() || 
               currentUser.hasPermission(EXPORT_CALLS);
    }
    
    // Analytics and reporting permissions
    public boolean canViewAnalytics() {
        return currentUser.hasPermission(VIEW_ANALYTICS) || 
               !currentUser.getRole().isEmpty(); // All users can view some analytics
    }
    
    public boolean canExportReports() {
        return currentUser.isOrganizationAdmin() || 
               currentUser.isManager() || 
               currentUser.hasPermission(EXPORT_REPORTS);
    }
    
    public boolean canViewIndividualPerformance() {
        return currentUser.hasPermission(VIEW_INDIVIDUAL_PERFORMANCE) || 
               !currentUser.getRole().isEmpty(); // Users can view their own performance
    }
    
    // Role-based dashboard access
    public boolean canAccessOrgAdminDashboard() {
        return currentUser.isOrganizationAdmin();
    }
    
    public boolean canAccessManagerDashboard() {
        return currentUser.isOrganizationAdmin() || currentUser.isManager();
    }
    
    public boolean canAccessAgentDashboard() {
        return currentUser.isAgent() || currentUser.isManager() || currentUser.isOrganizationAdmin();
    }
    
    public boolean canAccessViewerDashboard() {
        return currentUser.isViewer();
    }
    
    // Team-specific permissions
    public boolean canManageTeam(String teamId) {
        return currentUser.isOrganizationAdmin() || 
               (currentUser.isManager() && currentUser.isManagerOfTeam(teamId));
    }
    
    public boolean canViewTeamData(String teamId) {
        return currentUser.isOrganizationAdmin() || 
               currentUser.isManagerOfTeam(teamId) || 
               currentUser.isInTeam(teamId);
    }
    
    // Contact assignment permissions
    public boolean canAssignContactToUser(String targetUserId) {
        return currentUser.isOrganizationAdmin() || 
               (currentUser.isManager() && canManageUser(targetUserId));
    }
    
    public boolean canManageUser(String userId) {
        // Organization admins can manage all users
        if (currentUser.isOrganizationAdmin()) return true;
        
        // Managers can manage users in their teams
        if (currentUser.isManager() && currentUser.getTeams() != null) {
            return currentUser.getTeams().stream()
                    .anyMatch(team -> team.isUserMember(userId));
        }
        
        return false;
    }
    
    // Utility method to get role-based dashboard destination
    public String getPrimaryDashboardType() {
        if (currentUser.isOrganizationAdmin()) return "org_admin";
        if (currentUser.isManager()) return "manager";
        if (currentUser.isAgent()) return "agent";
        if (currentUser.isViewer()) return "viewer";
        return "agent"; // Default fallback
    }
}