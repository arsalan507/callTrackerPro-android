package com.calltrackerpro.calltracker.utils;

/**
 * Test credentials for Android app testing
 * Use these working credentials until new user creation is fixed
 */
public class TestCredentials {
    
    // Working test credentials for Android app
    public static final String TEST_EMAIL = "anas@anas.com";
    public static final String TEST_PASSWORD = "Anas@1234";
    public static final String TEST_ROLE = "org_admin";
    public static final String TEST_ORGANIZATION = "Blackarrowtechnologies";
    
    // Test phone numbers for call logging
    public static final String TEST_PHONE_1 = "+1234567890";
    public static final String TEST_PHONE_2 = "+1987654321";
    public static final String TEST_CALLER_NAME = "Test Caller";
    
    // Helper method to get login request
    public static class LoginRequest {
        public String email;
        public String password;
        
        public LoginRequest() {
            this.email = TEST_EMAIL;
            this.password = TEST_PASSWORD;
        }
        
        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
    
    // Helper method to create test call log request
    public static class TestCallLogRequest {
        public String phone_number;
        public String type;
        public int duration;
        public String status;
        public String caller_name;
        public String notes;
        public boolean auto_create_ticket;
        
        public TestCallLogRequest() {
            this.phone_number = TEST_PHONE_1;
            this.type = "inbound";
            this.duration = 120;
            this.status = "completed";
            this.caller_name = TEST_CALLER_NAME;
            this.notes = "Test call from Android app";
            this.auto_create_ticket = true;
        }
    }
}