package com.calltrackerpro.calltracker;

import android.os.Parcel;
import android.os.Parcelable;

public class SignupData implements Parcelable {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String organizationName;

    public SignupData() {
    }

    public SignupData(String email, String password) {
        this.email = email;
        this.password = password;
    }

    protected SignupData(Parcel in) {
        email = in.readString();
        password = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        phone = in.readString();
        organizationName = in.readString();
    }

    public static final Creator<SignupData> CREATOR = new Creator<SignupData>() {
        @Override
        public SignupData createFromParcel(Parcel in) {
            return new SignupData(in);
        }

        @Override
        public SignupData[] newArray(int size) {
            return new SignupData[size];
        }
    };

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    // Convert to CreateAccountRequest
    public CreateAccountRequest toCreateAccountRequest() {
        return new CreateAccountRequest(
                firstName,
                lastName,
                email,
                phone,
                organizationName,
                password
        );
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(phone);
        dest.writeString(organizationName);
    }
}