package com.calltrackerpro.calltracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "CallTrackerProPrefs";
    private static final String KEY_ORGANIZATION_ID = "organization_id";
    private static final String KEY_TEAM_ID = "team_id";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_REAL_TIME_ENABLED = "real_time_enabled";
    private static final String KEY_NOTIFICATION_ENABLED = "notification_enabled";

    private SharedPreferences sharedPreferences;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setOrganizationId(String organizationId) {
        sharedPreferences.edit().putString(KEY_ORGANIZATION_ID, organizationId).apply();
    }

    public String getOrganizationId() {
        return sharedPreferences.getString(KEY_ORGANIZATION_ID, null);
    }

    public void setTeamId(String teamId) {
        sharedPreferences.edit().putString(KEY_TEAM_ID, teamId).apply();
    }

    public String getTeamId() {
        return sharedPreferences.getString(KEY_TEAM_ID, null);
    }

    public void setUserId(String userId) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public void setUserRole(String userRole) {
        sharedPreferences.edit().putString(KEY_USER_ROLE, userRole).apply();
    }

    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, null);
    }

    public void setRealTimeEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_REAL_TIME_ENABLED, enabled).apply();
    }

    public boolean isRealTimeEnabled() {
        return sharedPreferences.getBoolean(KEY_REAL_TIME_ENABLED, true);
    }

    public void setNotificationEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply();
    }

    public boolean isNotificationEnabled() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, true);
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }
}