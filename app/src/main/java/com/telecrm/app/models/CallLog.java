package com.telecrm.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class CallLog {

    @SerializedName("_id")
    private String id;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("contactName")
    private String contactName;

    @SerializedName("duration")
    private int duration; // in seconds

    @SerializedName("callType")
    private String callType; // "incoming", "outgoing", "missed"

    @SerializedName("timestamp")
    private Date timestamp;

    @SerializedName("simSlot")
    private int simSlot; // 0 for SIM1, 1 for SIM2

    @SerializedName("tags")
    private List<String> tags;

    @SerializedName("notes")
    private String notes;

    @SerializedName("leadStatus")
    private String leadStatus = "new";

    @SerializedName("priority")
    private String priority = "medium";

    @SerializedName("source")
    private String source = "android_app";

    @SerializedName("isDeleted")
    private boolean isDeleted = false;

    // Additional fields for display (not sent to API)
    private String durationFormatted;
    private String timeAgo;
    private boolean isSynced = false;

    // Constructors
    public CallLog() {
        this.timestamp = new Date();
    }

    public CallLog(String phoneNumber, String contactName, int duration, String callType) {
        this();
        this.phoneNumber = phoneNumber;
        this.contactName = contactName;
        this.duration = duration;
        this.callType = callType;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
        this.durationFormatted = formatDuration(duration);
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getSimSlot() {
        return simSlot;
    }

    public void setSimSlot(int simSlot) {
        this.simSlot = simSlot;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getLeadStatus() {
        return leadStatus;
    }

    public void setLeadStatus(String leadStatus) {
        this.leadStatus = leadStatus;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getDurationFormatted() {
        if (durationFormatted == null) {
            durationFormatted = formatDuration(duration);
        }
        return durationFormatted;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    // Helper methods
    private String formatDuration(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return String.format("%d:%02d", minutes, remainingSeconds);
        } else {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            int remainingSeconds = seconds % 60;
            return String.format("%d:%02d:%02d", hours, minutes, remainingSeconds);
        }
    }

    public String getCallTypeIcon() {
        if (callType == null) return "📱";

        switch (callType) {
            case "incoming":
                return "📞";
            case "outgoing":
                return "📤";
            case "missed":
                return "📵";
            default:
                return "📱";
        }
    }

    public String getDisplayName() {
        return (contactName != null && !contactName.isEmpty()) ? contactName : phoneNumber;
    }
}