package com.calltrackerpro.calltracker.models;

import com.google.gson.annotations.SerializedName;

public class TicketHistory {
    @SerializedName("_id")
    private String id;

    @SerializedName("previousStatus")
    private String previousStatus;

    @SerializedName("newStatus")
    private String newStatus;

    @SerializedName("changedBy")
    private String changedBy; // User ID

    @SerializedName("changedByName")
    private String changedByName; // Display name for UI

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("reason")
    private String reason;

    @SerializedName("changeType")
    private String changeType; // "status", "assignment", "stage", "priority"

    @SerializedName("fieldChanged")
    private String fieldChanged; // specific field that was changed

    @SerializedName("previousValue")
    private String previousValue;

    @SerializedName("newValue")
    private String newValue;

    // Constructors
    public TicketHistory() {}

    public TicketHistory(String previousStatus, String newStatus, String changedBy, String reason) {
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.reason = reason;
        this.changeType = "status";
    }

    public TicketHistory(String fieldChanged, String previousValue, String newValue, String changedBy, String changeType) {
        this.fieldChanged = fieldChanged;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
        this.changeType = changeType;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public String getChangedByName() { return changedByName; }
    public void setChangedByName(String changedByName) { this.changedByName = changedByName; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }

    public String getFieldChanged() { return fieldChanged; }
    public void setFieldChanged(String fieldChanged) { this.fieldChanged = fieldChanged; }

    public String getPreviousValue() { return previousValue; }
    public void setPreviousValue(String previousValue) { this.previousValue = previousValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    // Utility Methods
    public String getFormattedTimestamp() {
        // TODO: Format timestamp for display
        return timestamp;
    }

    public String getChangeDescription() {
        switch (changeType) {
            case "status":
                return String.format("Status changed from %s to %s", 
                    formatStatusName(previousStatus), formatStatusName(newStatus));
            case "assignment":
                return String.format("Assigned to %s", newValue);
            case "stage":
                return String.format("Stage changed from %s to %s", 
                    formatStageName(previousValue), formatStageName(newValue));
            case "priority":
                return String.format("Priority changed from %s to %s", 
                    formatPriorityName(previousValue), formatPriorityName(newValue));
            default:
                return String.format("%s changed from %s to %s", 
                    fieldChanged, previousValue, newValue);
        }
    }

    private String formatStatusName(String status) {
        if (status == null) return "Unknown";
        switch (status) {
            case "new": return "New";
            case "contacted": return "Contacted";  
            case "qualified": return "Qualified";
            case "converted": return "Converted";
            case "closed": return "Closed";
            default: return status;
        }
    }

    private String formatStageName(String stage) {
        if (stage == null) return "Unknown";
        switch (stage) {
            case "prospect": return "Prospect";
            case "qualified": return "Qualified";
            case "proposal": return "Proposal";
            case "negotiation": return "Negotiation";
            case "closed-won": return "Closed Won";
            case "closed-lost": return "Closed Lost";
            default: return stage;
        }
    }

    private String formatPriorityName(String priority) {
        if (priority == null) return "Unknown";
        switch (priority) {
            case "high": return "High";
            case "medium": return "Medium";
            case "low": return "Low";
            default: return priority;
        }
    }

    @Override
    public String toString() {
        return "TicketHistory{" +
                "id='" + id + '\'' +
                ", previousStatus='" + previousStatus + '\'' +
                ", newStatus='" + newStatus + '\'' +
                ", changedBy='" + changedBy + '\'' +
                ", changedByName='" + changedByName + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", reason='" + reason + '\'' +
                ", changeType='" + changeType + '\'' +
                '}';
    }
}