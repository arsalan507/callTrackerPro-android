package com.calltrackerpro.calltracker.models;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("token")
    private String token;

    @SerializedName("user")
    private User user;

    @SerializedName("expires_in")
    private long expiresIn; // Token expiration time in seconds

    @SerializedName("token_type")
    private String tokenType; // Usually "Bearer"

    // Constructors
    public AuthResponse() {}

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(boolean success, String message, String token, User user) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.user = user;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    // Utility methods
    public boolean hasToken() {
        return token != null && !token.isEmpty();
    }

    public boolean hasUser() {
        return user != null;
    }

    public String getFullToken() {
        if (tokenType != null && token != null) {
            return tokenType + " " + token;
        }
        return token;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", token='" + (token != null ? "***" : "null") + '\'' +
                ", user=" + user +
                ", expiresIn=" + expiresIn +
                ", tokenType='" + tokenType + '\'' +
                '}';
    }
}