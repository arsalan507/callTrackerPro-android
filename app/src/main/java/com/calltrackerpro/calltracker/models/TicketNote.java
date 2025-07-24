package com.calltrackerpro.calltracker.models;

import com.google.gson.annotations.SerializedName;

public class TicketNote {
    @SerializedName("_id")
    private String id;

    @SerializedName("note")
    private String note;

    @SerializedName("author")
    private String author; // User ID

    @SerializedName("authorName")
    private String authorName; // Display name for UI

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("isPrivate")
    private boolean isPrivate; // true for agent notes, false for client notes

    @SerializedName("noteType")
    private String noteType; // "agent", "client", "system"

    // Constructors
    public TicketNote() {}

    public TicketNote(String note, String author, boolean isPrivate) {
        this.note = note;
        this.author = author;
        this.isPrivate = isPrivate;
        this.noteType = isPrivate ? "agent" : "client";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }

    public String getNoteType() { return noteType; }
    public void setNoteType(String noteType) { this.noteType = noteType; }

    // Utility Methods
    public String getFormattedTimestamp() {
        // TODO: Format timestamp for display
        return timestamp;
    }

    public String getNoteTypeDisplayName() {
        switch (noteType) {
            case "agent": return "Internal Note";
            case "client": return "Client Note";
            case "system": return "System Note";
            default: return "Note";
        }
    }

    @Override
    public String toString() {
        return "TicketNote{" +
                "id='" + id + '\'' +
                ", note='" + note + '\'' +
                ", author='" + author + '\'' +
                ", authorName='" + authorName + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", isPrivate=" + isPrivate +
                ", noteType='" + noteType + '\'' +
                '}';
    }
}