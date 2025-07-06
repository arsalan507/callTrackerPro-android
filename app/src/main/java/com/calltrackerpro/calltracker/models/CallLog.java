package com.calltrackerpro.calltracker.models;

import com.google.gson.annotations.SerializedName;

public class CallLog {
    @SerializedName("_id")
    private String id;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("contactName")
    private String contactName;

    @SerializedName("callType")
    private String callType; // "incoming", "outgoing", "missed"

    @SerializedName("duration")
    private long duration; // in seconds

    @SerializedName("timestamp")
    private long timestamp; // Unix timestamp

    @SerializedName("date")
    private String date; // ISO date string

    @SerializedName("userId")
    private String userId;

    @SerializedName("organizationId")
    private String organizationId;

    @SerializedName("callStatus")
    private String callStatus; // "completed", "busy", "no_answer", "failed"

    @SerializedName("notes")
    private String notes;

    @SerializedName("tags")
    private String[] tags;

    @SerializedName("recordingUrl")
    private String recordingUrl;

    @SerializedName("transcription")
    private String transcription;

    @SerializedName("sentiment")
    private String sentiment; // "positive", "negative", "neutral"

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    // Constructors
    public CallLog() {}

    public CallLog(String phoneNumber, String callType, long duration, long timestamp) {
        this.phoneNumber = phoneNumber;
        this.callType = callType;
        this.duration = duration;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getCallType() { return callType; }
    public void setCallType(String callType) { this.callType = callType; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getCallStatus() { return callStatus; }
    public void setCallStatus(String callStatus) { this.callStatus = callStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }

    public String getRecordingUrl() { return recordingUrl; }
    public void setRecordingUrl(String recordingUrl) { this.recordingUrl = recordingUrl; }

    public String getTranscription() { return transcription; }
    public void setTranscription(String transcription) { this.transcription = transcription; }

    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public String getFormattedDuration() {
        if (duration <= 0) return "0s";

        long minutes = duration / 60;
        long seconds = duration % 60;

        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

    public boolean isIncoming() {
        return "incoming".equalsIgnoreCase(callType);
    }

    public boolean isOutgoing() {
        return "outgoing".equalsIgnoreCase(callType);
    }

    public boolean isMissed() {
        return "missed".equalsIgnoreCase(callType);
    }

    public String getDisplayName() {
        return contactName != null && !contactName.isEmpty() ? contactName : phoneNumber;
    }

    public boolean hasRecording() {
        return recordingUrl != null && !recordingUrl.isEmpty();
    }

    public boolean hasTranscription() {
        return transcription != null && !transcription.isEmpty();
    }
}