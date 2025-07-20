package com.calltrackerpro.calltracker;

public class CreateAccountRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String organizationName;
    private String password;

    public CreateAccountRequest(String firstName, String lastName, String email,
                                String phone, String organizationName, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.organizationName = organizationName;
        this.password = password;
    }

    // Getters
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public String getPassword() {
        return password;
    }

    // Setters (optional, for flexibility)
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}