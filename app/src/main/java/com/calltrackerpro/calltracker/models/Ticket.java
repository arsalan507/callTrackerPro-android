package com.calltrackerpro.calltracker.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Ticket implements Serializable {
    private static final long serialVersionUID = 1L;
    @SerializedName("_id")
    private String id;

    @SerializedName("ticketId")
    private String ticketId; // UUID format for external reference

    // Basic Contact Information
    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("contactName")
    private String contactName;

    @SerializedName("alternatePhones")
    private List<String> alternatePhones;

    @SerializedName("email")
    private String email;

    @SerializedName("company")
    private String company;

    @SerializedName("jobTitle")
    private String jobTitle;

    @SerializedName("location")
    private Location location;

    // Call Details
    @SerializedName("callLogId")
    private String callLogId; // Reference to original call

    @SerializedName("callDate")
    private String callDate;

    @SerializedName("callDuration")
    private long callDuration; // seconds

    @SerializedName("callType")
    private String callType; // 'incoming', 'outgoing', 'missed'

    @SerializedName("callRecordingUrl")
    private String callRecordingUrl;

    @SerializedName("callQuality")
    private int callQuality; // 1-5 rating

    // Lead Qualification
    @SerializedName("leadSource")
    private String leadSource; // 'cold_call', 'referral', 'website', 'marketing'

    @SerializedName("leadStatus")
    private String leadStatus; // 'new', 'contacted', 'qualified', 'converted', 'closed'

    @SerializedName("priority")
    private String priority; // 'low', 'medium', 'high', 'urgent'

    @SerializedName("interestLevel")
    private String interestLevel; // 'hot', 'warm', 'cold'

    @SerializedName("budgetRange")
    private String budgetRange;

    @SerializedName("timeline")
    private String timeline;

    @SerializedName("productsInterested")
    private List<String> productsInterested;

    // Enhanced Backend Schema - Ticket Lifecycle
    @SerializedName("status")
    private String status; // 'open', 'in_progress', 'resolved', 'closed'

    @SerializedName("category")
    private String category; // 'sales', 'support', 'billing', 'technical'

    @SerializedName("source")
    private String source; // 'phone', 'email', 'web', 'mobile_app'

    // SLA and Escalation
    @SerializedName("slaStatus")
    private String slaStatus; // 'on_track', 'at_risk', 'breached'

    @SerializedName("dueDate")
    private String dueDate;

    @SerializedName("escalatedAt")
    private String escalatedAt;

    @SerializedName("escalatedTo")
    private String escalatedTo;

    @SerializedName("resolutionTime")
    private long resolutionTime; // in minutes

    // Assignment and Team
    @SerializedName("assignedTo")
    private String assignedTo; // User ID

    @SerializedName("assignedTeam")
    private String assignedTeam; // Team ID

    @SerializedName("previousAssignee")
    private String previousAssignee;

    @SerializedName("assignedAt")
    private String assignedAt;

    // Customer Satisfaction
    @SerializedName("satisfactionRating")
    private int satisfactionRating; // 1-5

    @SerializedName("satisfactionFeedback")
    private String satisfactionFeedback;

    @SerializedName("satisfactionDate")
    private String satisfactionDate;

    // Legacy CRM Pipeline (maintained for compatibility)
    @SerializedName("stage")
    private String stage; // 'prospect', 'qualified', 'proposal', 'negotiation', 'closed-won', 'closed-lost'

    @SerializedName("nextFollowUp")
    private String nextFollowUp;

    @SerializedName("followUpActions")
    private List<String> followUpActions;

    @SerializedName("dealValue")
    private double dealValue;

    @SerializedName("conversionProbability")
    private int conversionProbability; // 0-100%

    // Notes and Tracking
    @SerializedName("agentNotes")
    private List<TicketNote> agentNotes;

    @SerializedName("clientNotes")
    private List<TicketNote> clientNotes;

    @SerializedName("tags")
    private List<String> tags;

    @SerializedName("customFields")
    private Map<String, Object> customFields;

    // Multi-tenant Data
    @SerializedName("organizationId")
    private String organizationId;

    @SerializedName("teamId")
    private String teamId;

    // Audit Trail
    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedBy")
    private String updatedBy;

    @SerializedName("updatedAt")
    private String updatedAt;

    // Workflow Status
    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("ticketHistory")
    private List<TicketHistory> ticketHistory;

    // Constructors
    public Ticket() {}

    public Ticket(String phoneNumber, String contactName, String callType) {
        this.phoneNumber = phoneNumber;
        this.contactName = contactName;
        this.callType = callType;
        this.isActive = true;
        this.leadStatus = "new";
        this.stage = "prospect";
        this.priority = "medium";
        this.interestLevel = "warm";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public List<String> getAlternatePhones() { return alternatePhones; }
    public void setAlternatePhones(List<String> alternatePhones) { this.alternatePhones = alternatePhones; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public String getCallLogId() { return callLogId; }
    public void setCallLogId(String callLogId) { this.callLogId = callLogId; }

    public String getCallDate() { return callDate; }
    public void setCallDate(String callDate) { this.callDate = callDate; }

    public long getCallDuration() { return callDuration; }
    public void setCallDuration(long callDuration) { this.callDuration = callDuration; }

    public String getCallType() { return callType; }
    public void setCallType(String callType) { this.callType = callType; }

    public String getCallRecordingUrl() { return callRecordingUrl; }
    public void setCallRecordingUrl(String callRecordingUrl) { this.callRecordingUrl = callRecordingUrl; }

    public int getCallQuality() { return callQuality; }
    public void setCallQuality(int callQuality) { this.callQuality = callQuality; }

    public String getLeadSource() { return leadSource; }
    public void setLeadSource(String leadSource) { this.leadSource = leadSource; }

    public String getLeadStatus() { return leadStatus; }
    public void setLeadStatus(String leadStatus) { this.leadStatus = leadStatus; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getInterestLevel() { return interestLevel; }
    public void setInterestLevel(String interestLevel) { this.interestLevel = interestLevel; }

    public String getBudgetRange() { return budgetRange; }
    public void setBudgetRange(String budgetRange) { this.budgetRange = budgetRange; }

    public String getTimeline() { return timeline; }
    public void setTimeline(String timeline) { this.timeline = timeline; }

    public List<String> getProductsInterested() { return productsInterested; }
    public void setProductsInterested(List<String> productsInterested) { this.productsInterested = productsInterested; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    // Enhanced Backend Schema Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getSlaStatus() { return slaStatus; }
    public void setSlaStatus(String slaStatus) { this.slaStatus = slaStatus; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getEscalatedAt() { return escalatedAt; }
    public void setEscalatedAt(String escalatedAt) { this.escalatedAt = escalatedAt; }

    public String getEscalatedTo() { return escalatedTo; }
    public void setEscalatedTo(String escalatedTo) { this.escalatedTo = escalatedTo; }

    public long getResolutionTime() { return resolutionTime; }
    public void setResolutionTime(long resolutionTime) { this.resolutionTime = resolutionTime; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getPreviousAssignee() { return previousAssignee; }
    public void setPreviousAssignee(String previousAssignee) { this.previousAssignee = previousAssignee; }

    public String getAssignedAt() { return assignedAt; }
    public void setAssignedAt(String assignedAt) { this.assignedAt = assignedAt; }

    public int getSatisfactionRating() { return satisfactionRating; }
    public void setSatisfactionRating(int satisfactionRating) { this.satisfactionRating = satisfactionRating; }

    public String getSatisfactionFeedback() { return satisfactionFeedback; }
    public void setSatisfactionFeedback(String satisfactionFeedback) { this.satisfactionFeedback = satisfactionFeedback; }

    public String getSatisfactionDate() { return satisfactionDate; }
    public void setSatisfactionDate(String satisfactionDate) { this.satisfactionDate = satisfactionDate; }

    // Legacy getter for backward compatibility
    public String getAssignedAgent() { return assignedTo; }
    public void setAssignedAgent(String assignedAgent) { this.assignedTo = assignedAgent; }

    public String getAssignedTeam() { return assignedTeam; }
    public void setAssignedTeam(String assignedTeam) { this.assignedTeam = assignedTeam; }

    public String getNextFollowUp() { return nextFollowUp; }
    public void setNextFollowUp(String nextFollowUp) { this.nextFollowUp = nextFollowUp; }

    public List<String> getFollowUpActions() { return followUpActions; }
    public void setFollowUpActions(List<String> followUpActions) { this.followUpActions = followUpActions; }

    public double getDealValue() { return dealValue; }
    public void setDealValue(double dealValue) { this.dealValue = dealValue; }

    public int getConversionProbability() { return conversionProbability; }
    public void setConversionProbability(int conversionProbability) { this.conversionProbability = conversionProbability; }

    public List<TicketNote> getAgentNotes() { return agentNotes; }
    public void setAgentNotes(List<TicketNote> agentNotes) { this.agentNotes = agentNotes; }

    public List<TicketNote> getClientNotes() { return clientNotes; }
    public void setClientNotes(List<TicketNote> clientNotes) { this.clientNotes = clientNotes; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public Map<String, Object> getCustomFields() { return customFields; }
    public void setCustomFields(Map<String, Object> customFields) { this.customFields = customFields; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public List<TicketHistory> getTicketHistory() { return ticketHistory; }
    public void setTicketHistory(List<TicketHistory> ticketHistory) { this.ticketHistory = ticketHistory; }

    // Utility Methods
    public String getDisplayName() {
        return contactName != null && !contactName.isEmpty() ? contactName : phoneNumber;
    }

    public String getCustomerPhone() {
        return phoneNumber;
    }

    public String getFormattedDuration() {
        if (callDuration <= 0) return "0:00";
        long minutes = callDuration / 60;
        long seconds = callDuration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public String getPriorityDisplayName() {
        if (priority == null) return "No Priority";
        switch (priority) {
            case "urgent": return "Urgent Priority";
            case "high": return "High Priority";
            case "medium": return "Medium Priority";
            case "low": return "Low Priority";
            default: return "Unknown Priority";
        }
    }

    public String getStageDisplayName() {
        if (stage == null) return "No Stage";
        switch (stage) {
            case "prospect": return "Prospect";
            case "qualified": return "Qualified";
            case "proposal": return "Proposal";
            case "negotiation": return "Negotiation";
            case "closed-won": return "Closed Won";
            case "closed-lost": return "Closed Lost";
            default: return "Unknown Stage";
        }
    }

    public String getLeadStatusDisplayName() {
        if (leadStatus == null) return "No Status";
        switch (leadStatus) {
            case "new": return "New";
            case "contacted": return "Contacted";
            case "qualified": return "Qualified";
            case "converted": return "Converted";
            case "closed": return "Closed";
            default: return "Unknown Status";
        }
    }

    public boolean isHighPriority() {
        return "high".equals(priority);
    }

    public boolean isOverdue() {
        if (dueDate == null || dueDate.isEmpty()) return false;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date due = format.parse(dueDate);
            return due != null && due.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // Enhanced Backend Schema Utility Methods
    public String getStatusDisplayName() {
        switch (status != null ? status : "open") {
            case "open": return "Open";
            case "in_progress": return "In Progress";
            case "resolved": return "Resolved";
            case "closed": return "Closed";
            default: return "Unknown";
        }
    }

    public String getCategoryDisplayName() {
        switch (category != null ? category : "sales") {
            case "sales": return "Sales";
            case "support": return "Support";
            case "billing": return "Billing";
            case "technical": return "Technical";
            default: return "General";
        }
    }

    public String getSourceDisplayName() {
        switch (source != null ? source : "phone") {
            case "phone": return "Phone Call";
            case "email": return "Email";
            case "web": return "Website";
            case "mobile_app": return "Mobile App";
            default: return "Unknown";
        }
    }

    public String getSlaStatusDisplayName() {
        switch (slaStatus != null ? slaStatus : "on_track") {
            case "on_track": return "On Track";
            case "at_risk": return "At Risk";
            case "breached": return "SLA Breached";
            default: return "Unknown";
        }
    }

    public boolean isSlaBreached() {
        return "breached".equals(slaStatus);
    }

    public boolean isSlaAtRisk() {
        return "at_risk".equals(slaStatus);
    }

    public boolean isEscalated() {
        return escalatedAt != null && !escalatedAt.isEmpty();
    }

    public boolean hasCustomerFeedback() {
        return satisfactionRating > 0 || (satisfactionFeedback != null && !satisfactionFeedback.isEmpty());
    }

    public String getFormattedResolutionTime() {
        if (resolutionTime <= 0) return "Not resolved";
        
        long hours = resolutionTime / 60;
        long minutes = resolutionTime % 60;
        
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
        } else {
            return String.format(Locale.getDefault(), "%dm", minutes);
        }
    }

    public String getSatisfactionRatingDisplay() {
        if (satisfactionRating <= 0) return "Not rated";
        return satisfactionRating + "/5 ⭐";
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id='" + id + '\'' +
                ", ticketId='" + ticketId + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", contactName='" + contactName + '\'' +
                ", leadStatus='" + leadStatus + '\'' +
                ", stage='" + stage + '\'' +
                ", priority='" + priority + '\'' +
                '}';
    }

    // Inner Classes
    public static class Location {
        @SerializedName("city")
        private String city;

        @SerializedName("state")
        private String state;

        @SerializedName("country")
        private String country;

        @SerializedName("address")
        private String address;

        public Location() {}

        public Location(String city, String state, String country) {
            this.city = city;
            this.state = state;
            this.country = country;
        }

        // Getters and Setters
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getFormattedLocation() {
            StringBuilder location = new StringBuilder();
            if (city != null && !city.isEmpty()) {
                location.append(city);
            }
            if (state != null && !state.isEmpty()) {
                if (location.length() > 0) location.append(", ");
                location.append(state);
            }
            if (country != null && !country.isEmpty()) {
                if (location.length() > 0) location.append(", ");
                location.append(country);
            }
            return location.toString();
        }
    }
}