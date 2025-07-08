package com.calltrackerpro.calltracker.models;

import com.google.gson.annotations.SerializedName;

/**
 * Logout response model
 */
public class LogoutResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    public LogoutResponse() {}

    public LogoutResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

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

    @Override
    public String toString() {
        return "LogoutResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}