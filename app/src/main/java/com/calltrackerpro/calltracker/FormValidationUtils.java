package com.calltrackerpro.calltracker;

import android.text.TextUtils;
import android.util.Patterns;
import java.util.regex.Pattern;

public class FormValidationUtils {

    // Email validation
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Phone number validation
    public static boolean isValidPhoneNumber(String phone) {
        if (TextUtils.isEmpty(phone)) return false;

        // Remove all non-digits
        String cleanPhone = phone.replaceAll("[^\\d]", "");

        // Check if it has at least 10 digits
        return cleanPhone.length() >= 10 && cleanPhone.length() <= 15;
    }

    // Name validation (first name, last name)
    public static boolean isValidName(String name) {
        if (TextUtils.isEmpty(name)) return false;

        // Name should be at least 2 characters and contain only letters, spaces, hyphens, apostrophes
        String namePattern = "^[a-zA-Z\\s\\-']{2,50}$";
        return Pattern.compile(namePattern).matcher(name.trim()).matches();
    }

    // Organization name validation
    public static boolean isValidOrganizationName(String orgName) {
        if (TextUtils.isEmpty(orgName)) return false;

        // Organization name should be at least 2 characters
        String trimmed = orgName.trim();
        return trimmed.length() >= 2 && trimmed.length() <= 100;
    }

    // Format phone number for display
    public static String formatPhoneNumber(String phone) {
        if (TextUtils.isEmpty(phone)) return "";

        String cleanPhone = phone.replaceAll("[^\\d]", "");

        if (cleanPhone.length() == 10) {
            return String.format("(%s) %s-%s",
                    cleanPhone.substring(0, 3),
                    cleanPhone.substring(3, 6),
                    cleanPhone.substring(6));
        } else if (cleanPhone.length() == 11 && cleanPhone.startsWith("1")) {
            return String.format("+1 (%s) %s-%s",
                    cleanPhone.substring(1, 4),
                    cleanPhone.substring(4, 7),
                    cleanPhone.substring(7));
        }

        return phone; // Return original if can't format
    }

    // Clean phone number (remove formatting, keep only digits)
    public static String cleanPhoneNumber(String phone) {
        if (TextUtils.isEmpty(phone)) return "";
        return phone.replaceAll("[^\\d]", "");
    }

    // Validation error messages
    public static class ValidationMessages {
        public static final String REQUIRED_FIELD = "This field is required";
        public static final String INVALID_EMAIL = "Please enter a valid email address";
        public static final String INVALID_PHONE = "Please enter a valid phone number";
        public static final String INVALID_NAME = "Name must be 2-50 characters and contain only letters";
        public static final String INVALID_ORGANIZATION = "Organization name must be 2-100 characters";
        public static final String PASSWORD_TOO_SHORT = "Password must be at least 6 characters";
        public static final String PASSWORDS_DONT_MATCH = "Passwords do not match";
        public static final String TERMS_NOT_ACCEPTED = "Please accept the Terms & Conditions";
    }
}