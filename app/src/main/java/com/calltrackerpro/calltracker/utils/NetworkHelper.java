package com.calltrackerpro.calltracker.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkHelper {
    private static final String TAG = "NetworkHelper";
    
    /**
     * Check if device has internet connectivity
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        
        return false;
    }
    
    /**
     * Test DNS resolution for the backend hostname
     */
    public static boolean testDNSResolution(String hostname) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(hostname);
            Log.d(TAG, "DNS Resolution successful for " + hostname + ". Found " + addresses.length + " addresses:");
            for (InetAddress addr : addresses) {
                Log.d(TAG, "  - " + addr.getHostAddress());
            }
            return true;
        } catch (UnknownHostException e) {
            Log.e(TAG, "DNS Resolution failed for " + hostname + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get detailed network status for troubleshooting
     */
    public static String getNetworkStatus(Context context) {
        StringBuilder status = new StringBuilder();
        
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) {
                status.append("Network Type: ").append(activeNetwork.getTypeName()).append("\n");
                status.append("Connected: ").append(activeNetwork.isConnected()).append("\n");
                status.append("Available: ").append(activeNetwork.isAvailable()).append("\n");
                status.append("Roaming: ").append(activeNetwork.isRoaming()).append("\n");
            } else {
                status.append("No active network connection\n");
            }
        } else {
            status.append("ConnectivityManager not available\n");
        }
        
        return status.toString();
    }
    
    /**
     * Test connectivity to the backend server
     */
    public static void testBackendConnectivity() {
        new Thread(() -> {
            String hostname = "calltrackerpro-backend.vercel.app";
            Log.d(TAG, "Testing connectivity to " + hostname);
            
            // Test DNS resolution
            boolean dnsWorking = testDNSResolution(hostname);
            Log.d(TAG, "DNS Resolution: " + (dnsWorking ? "SUCCESS" : "FAILED"));
            
            if (dnsWorking) {
                // Test basic connectivity
                try {
                    Process process = Runtime.getRuntime().exec("ping -c 1 " + hostname);
                    int returnValue = process.waitFor();
                    Log.d(TAG, "Ping test: " + (returnValue == 0 ? "SUCCESS" : "FAILED"));
                } catch (IOException | InterruptedException e) {
                    Log.e(TAG, "Ping test failed: " + e.getMessage());
                }
            }
        }).start();
    }
}