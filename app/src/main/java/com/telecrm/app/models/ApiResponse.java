package com.telecrm.app.models;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("error")
    private String error;

    @SerializedName("code")
    private int code;

    // Constructors
    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    // Helper methods
    public boolean hasError() {
        return !success || error != null;
    }

    public String getDisplayMessage() {
        if (hasError() && error != null) {
            return error;
        }
        return message != null ? message : "Operation completed";
    }
}