package com.calltrackerpro.calltracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.calltrackerpro.calltracker.models.User;
import com.google.gson.Gson;

public class TokenManager {
    private static final String PREF_NAME = "calltracker_auth";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER = "user_data";
    private static final String KEY_EXPIRES_AT = "expires_at";
    private static final String KEY_LOGIN_TIME = "login_time";
    private static final String TAG = "TokenManager";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Gson gson;

    public TokenManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        gson = new Gson();
    }

    /**
     * Save authentication data after successful login
     */
    public void saveAuthData(String token, User user, long expiresInSeconds) {
        try {
            long expiresAt = System.currentTimeMillis() + (expiresInSeconds * 1000);
            String userJson = gson.toJson(user);

            editor.putString(KEY_TOKEN, token);
            editor.putString(KEY_USER, userJson);
            editor.putLong(KEY_EXPIRES_AT, expiresAt);
            editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
            editor.apply();

            Log.d(TAG, "✅ Auth data saved successfully for user: " + user.getEmail());
        } catch (Exception e) {
            Log.e(TAG, "❌ Error saving auth data: " + e.getMessage());
        }
    }

    /**
     * Get stored authentication token
     */
    public String getToken() {
        try {
            String token = preferences.getString(KEY_TOKEN, null);
            if (token != null) {
                Log.d(TAG, "Token retrieved successfully");
            }
            return token;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error retrieving token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get stored user data
     */
    public User getUser() {
        try {
            String userJson = preferences.getString(KEY_USER, null);
            if (userJson != null) {
                User user = gson.fromJson(userJson, User.class);
                Log.d(TAG, "User data retrieved for: " + user.getEmail());
                return user;
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error retrieving user data: " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if user is logged in and token is valid
     */
    public boolean isLoggedIn() {
        String token = getToken();
        boolean hasToken = token != null && !token.isEmpty();
        boolean notExpired = !isTokenExpired();

        boolean isLoggedIn = hasToken && notExpired;

        if (isLoggedIn) {
            Log.d(TAG, "✅ User is logged in");
        } else {
            Log.d(TAG, "❌ User is not logged in (hasToken: " + hasToken + ", notExpired: " + notExpired + ")");
        }

        return isLoggedIn;
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired() {
        long expiresAt = preferences.getLong(KEY_EXPIRES_AT, 0);

        if (expiresAt == 0) {
            // No expiration time set, consider it expired
            return true;
        }

        boolean expired = System.currentTimeMillis() > expiresAt;

        if (expired) {
            Log.d(TAG, "⏰ Token has expired");
        }

        return expired;
    }

    /**
     * Get token with Bearer prefix for API calls
     */
    public String getBearerToken() {
        String token = getToken();
        return token != null ? "Bearer " + token : null;
    }

    /**
     * Get login timestamp
     */
    public long getLoginTime() {
        return preferences.getLong(KEY_LOGIN_TIME, 0);
    }

    /**
     * Get token expiration time
     */
    public long getExpirationTime() {
        return preferences.getLong(KEY_EXPIRES_AT, 0);
    }

    /**
     * Check how much time is left before token expires (in minutes)
     */
    public long getTimeUntilExpiration() {
        long expiresAt = getExpirationTime();
        if (expiresAt == 0) return 0;

        long timeLeft = expiresAt - System.currentTimeMillis();
        return Math.max(0, timeLeft / (1000 * 60)); // Convert to minutes
    }

    /**
     * Clear all authentication data (logout)
     */
    public void clearAuthData() {
        try {
            editor.clear();
            editor.apply();
            Log.d(TAG, "🔐 Auth data cleared successfully");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error clearing auth data: " + e.getMessage());
        }
    }

    /**
     * Update user data (useful for profile updates)
     */
    public void updateUser(User user) {
        try {
            String userJson = gson.toJson(user);
            editor.putString(KEY_USER, userJson);
            editor.apply();
            Log.d(TAG, "✅ User data updated for: " + user.getEmail());
        } catch (Exception e) {
            Log.e(TAG, "❌ Error updating user data: " + e.getMessage());
        }
    }

    /**
     * Refresh token with new expiration time
     */
    public void refreshToken(String newToken, long expiresInSeconds) {
        try {
            long expiresAt = System.currentTimeMillis() + (expiresInSeconds * 1000);

            editor.putString(KEY_TOKEN, newToken);
            editor.putLong(KEY_EXPIRES_AT, expiresAt);
            editor.apply();

            Log.d(TAG, "🔄 Token refreshed successfully");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error refreshing token: " + e.getMessage());
        }
    }

    /**
     * Get authorization header for API calls (used by MainActivity)
     */
    public String getAuthHeader() {
        return getBearerToken();
    }
}