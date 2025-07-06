package com.calltrackerpro.calltracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import com.calltrackerpro.calltracker.models.User;

public class TokenManager {
    private static final String TAG = "TokenManager";
    private static final String PREF_NAME = "calltracker_auth";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER = "user_data";
    private static final String KEY_EXPIRES_AT = "expires_at";

    private SharedPreferences prefs;
    private Gson gson;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveAuthData(String token, User user, long expiresIn) {
        long expiresAt = System.currentTimeMillis() + (expiresIn * 1000);
        String userJson = gson.toJson(user);

        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER, userJson)
                .putLong(KEY_EXPIRES_AT, expiresAt)
                .apply();

        Log.d(TAG, "✅ Auth data saved for user: " + user.getEmail());
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getAuthHeader() {
        String token = getToken();
        return token != null ? "Bearer " + token : null;
    }

    public User getUser() {
        String userJson = prefs.getString(KEY_USER, null);
        if (userJson != null) {
            try {
                return gson.fromJson(userJson, User.class);
            } catch (Exception e) {
                Log.e(TAG, "❌ Error parsing user data: " + e.getMessage());
                clearAuthData();
            }
        }
        return null;
    }

    public boolean isLoggedIn() {
        String token = getToken();
        long expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0);

        if (token == null || token.isEmpty()) {
            return false;
        }

        if (System.currentTimeMillis() > expiresAt) {
            Log.d(TAG, "🔐 Token expired, clearing auth data");
            clearAuthData();
            return false;
        }

        return true;
    }

    public boolean isTokenExpired() {
        long expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0);
        return System.currentTimeMillis() > expiresAt;
    }

    public void clearAuthData() {
        prefs.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_USER)
                .remove(KEY_EXPIRES_AT)
                .apply();

        Log.d(TAG, "🚪 Auth data cleared");
    }

    public void logout() {
        clearAuthData();
        Log.d(TAG, "👋 User logged out");
    }

    // Helper methods
    public String getUserRole() {
        User user = getUser();
        return user != null ? user.getRole() : null;
    }

    public boolean hasPermission(String permission) {
        User user = getUser();
        return user != null && user.hasPermission(permission);
    }

    public boolean isManager() {
        User user = getUser();
        return user != null && user.isManager();
    }

    public int getCallCount() {
        User user = getUser();
        return user != null ? user.getCallCount() : 0;
    }

    public int getCallLimit() {
        User user = getUser();
        return user != null ? user.getCallLimit() : 0;
    }

    public boolean canMakeCall() {
        User user = getUser();
        if (user == null) return false;

        int callCount = user.getCallCount();
        int callLimit = user.getCallLimit();

        return callLimit == 0 || callCount < callLimit; // 0 means unlimited
    }
}