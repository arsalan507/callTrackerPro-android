package com.calltrackerpro.calltracker;

import java.util.regex.Pattern;

public class PasswordStrengthHelper {

    public static class PasswordStrength {
        private int score;
        private String message;
        private boolean isValid;

        public PasswordStrength(int score, String message, boolean isValid) {
            this.score = score;
            this.message = message;
            this.isValid = isValid;
        }

        public int getScore() { return score; }
        public String getMessage() { return message; }
        public boolean isValid() { return isValid; }
    }

    public static PasswordStrength checkPasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return new PasswordStrength(0, "Password must be at least 6 characters", false);
        }

        int score = 0;
        StringBuilder feedback = new StringBuilder();

        // Length check
        if (password.length() >= 8) {
            score += 1;
        } else {
            feedback.append("Use 8+ characters. ");
        }

        // Uppercase check
        if (Pattern.compile("[A-Z]").matcher(password).find()) {
            score += 1;
        } else {
            feedback.append("Add uppercase letters. ");
        }

        // Lowercase check
        if (Pattern.compile("[a-z]").matcher(password).find()) {
            score += 1;
        } else {
            feedback.append("Add lowercase letters. ");
        }

        // Number check
        if (Pattern.compile("[0-9]").matcher(password).find()) {
            score += 1;
        } else {
            feedback.append("Add numbers. ");
        }

        // Special character check
        if (Pattern.compile("[^a-zA-Z0-9]").matcher(password).find()) {
            score += 1;
        } else {
            feedback.append("Add special characters. ");
        }

        // Check for common weak passwords
        if (isCommonPassword(password)) {
            score = Math.max(0, score - 2);
            feedback.append("Avoid common passwords. ");
        }

        String message;
        boolean isValid = score >= 3; // Require at least 3/5 criteria

        switch (score) {
            case 0:
            case 1:
                message = "Very Weak - " + feedback.toString();
                break;
            case 2:
                message = "Weak - " + feedback.toString();
                break;
            case 3:
                message = "Fair - " + feedback.toString();
                break;
            case 4:
                message = "Good password";
                break;
            case 5:
                message = "Strong password";
                break;
            default:
                message = "Password strength unknown";
        }

        return new PasswordStrength(score, message.trim(), isValid);
    }

    private static boolean isCommonPassword(String password) {
        String[] commonPasswords = {
                "123456", "password", "123456789", "12345678", "12345",
                "1234567", "admin", "qwerty", "abc123", "password123",
                "welcome", "login", "guest", "test", "user", "root"
        };

        String lowerPassword = password.toLowerCase();
        for (String common : commonPasswords) {
            if (lowerPassword.equals(common) || lowerPassword.contains(common)) {
                return true;
            }
        }
        return false;
    }
}