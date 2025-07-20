package com.calltrackerpro.calltracker.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Contact {
    @SerializedName("_id")
    private String id;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("organization_id")
    private String organizationId;

    @SerializedName("team_id")
    private String teamId;

    @SerializedName("assigned_agent_id")
    private String assignedAgentId;

    @SerializedName("assigned_agent")
    private User assignedAgent;

    @SerializedName("status")
    private String status; // lead, prospect, customer, inactive

    @SerializedName("source")
    private String source; // website, referral, cold_call, etc.

    @SerializedName("company")
    private String company;

    @SerializedName("job_title")
    private String jobTitle;

    @SerializedName("address")
    private Address address;

    @SerializedName("tags")
    private List<String> tags;

    @SerializedName("custom_fields")
    private CustomFields customFields;

    @SerializedName("interactions")
    private List<Interaction> interactions;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("last_contact_date")
    private String lastContactDate;

    // Constructors
    public Contact() {}

    public Contact(String firstName, String lastName, String phone, String organizationId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.organizationId = organizationId;
        this.status = "lead"; // Default status
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public String getAssignedAgentId() { return assignedAgentId; }
    public void setAssignedAgentId(String assignedAgentId) { this.assignedAgentId = assignedAgentId; }

    public User getAssignedAgent() { return assignedAgent; }
    public void setAssignedAgent(User assignedAgent) { this.assignedAgent = assignedAgent; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public CustomFields getCustomFields() { return customFields; }
    public void setCustomFields(CustomFields customFields) { this.customFields = customFields; }

    public List<Interaction> getInteractions() { return interactions; }
    public void setInteractions(List<Interaction> interactions) { this.interactions = interactions; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getLastContactDate() { return lastContactDate; }
    public void setLastContactDate(String lastContactDate) { this.lastContactDate = lastContactDate; }

    // Utility methods
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            fullName.append(firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(lastName);
        }
        return fullName.length() > 0 ? fullName.toString() : "Unknown Contact";
    }

    public String getDisplayName() {
        String fullName = getFullName();
        if (company != null && !company.isEmpty()) {
            return fullName + " (" + company + ")";
        }
        return fullName;
    }

    public boolean isLead() { return "lead".equals(status); }
    public boolean isProspect() { return "prospect".equals(status); }
    public boolean isCustomer() { return "customer".equals(status); }
    public boolean isInactive() { return "inactive".equals(status); }

    public int getInteractionCount() {
        return interactions != null ? interactions.size() : 0;
    }

    // Inner classes
    public static class Address {
        @SerializedName("street")
        private String street;

        @SerializedName("city")
        private String city;

        @SerializedName("state")
        private String state;

        @SerializedName("zip_code")
        private String zipCode;

        @SerializedName("country")
        private String country;

        // Getters and Setters
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public String getFormattedAddress() {
            StringBuilder address = new StringBuilder();
            if (street != null) address.append(street).append(", ");
            if (city != null) address.append(city).append(", ");
            if (state != null) address.append(state).append(" ");
            if (zipCode != null) address.append(zipCode);
            return address.toString().trim().replaceAll(",$", "");
        }
    }

    public static class CustomFields {
        @SerializedName("priority")
        private String priority;

        @SerializedName("budget")
        private String budget;

        @SerializedName("timeline")
        private String timeline;

        @SerializedName("notes")
        private String notes;

        // Getters and Setters
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getBudget() { return budget; }
        public void setBudget(String budget) { this.budget = budget; }

        public String getTimeline() { return timeline; }
        public void setTimeline(String timeline) { this.timeline = timeline; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class Interaction {
        @SerializedName("_id")
        private String id;

        @SerializedName("type")
        private String type; // call, email, meeting, note

        @SerializedName("direction")
        private String direction; // inbound, outbound

        @SerializedName("duration")
        private int duration;

        @SerializedName("notes")
        private String notes;

        @SerializedName("outcome")
        private String outcome; // interested, not_interested, callback, meeting_scheduled

        @SerializedName("agent_id")
        private String agentId;

        @SerializedName("agent")
        private User agent;

        @SerializedName("created_at")
        private String createdAt;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getDirection() { return direction; }
        public void setDirection(String direction) { this.direction = direction; }

        public int getDuration() { return duration; }
        public void setDuration(int duration) { this.duration = duration; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public String getOutcome() { return outcome; }
        public void setOutcome(String outcome) { this.outcome = outcome; }

        public String getAgentId() { return agentId; }
        public void setAgentId(String agentId) { this.agentId = agentId; }

        public User getAgent() { return agent; }
        public void setAgent(User agent) { this.agent = agent; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public boolean isCall() { return "call".equals(type); }
        public boolean isEmail() { return "email".equals(type); }
        public boolean isMeeting() { return "meeting".equals(type); }
        public boolean isNote() { return "note".equals(type); }
    }
}