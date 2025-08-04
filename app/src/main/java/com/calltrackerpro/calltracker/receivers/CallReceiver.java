package com.calltrackerpro.calltracker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.calltrackerpro.calltracker.services.CallReceiverService;
import com.calltrackerpro.calltracker.services.EnhancedCallService;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver";
    private static String lastState = TelephonyManager.EXTRA_STATE_IDLE;
    private static String incomingPhoneNumber = "";
    private static boolean isIncoming = false;
    private static boolean wasRinging = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received broadcast: " + action);

        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
            handlePhoneStateChange(context, intent);
        } else if (Intent.ACTION_NEW_OUTGOING_CALL.equals(action)) {
            handleOutgoingCall(context, intent);
        }
    }

    private void handlePhoneStateChange(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        Log.d(TAG, "Phone state changed: " + state + ", Number: " + phoneNumber);

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            incomingPhoneNumber = phoneNumber;
        }

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            // Incoming call ringing
            isIncoming = true;
            wasRinging = true;
            Log.d(TAG, "Incoming call ringing: " + incomingPhoneNumber);
            
            // Start service to prepare for call processing
            Intent serviceIntent = new Intent(context, CallReceiverService.class);
            serviceIntent.setAction(CallReceiverService.ACTION_CALL_RINGING);
            serviceIntent.putExtra("phoneNumber", incomingPhoneNumber);
            serviceIntent.putExtra("callType", "incoming");
            context.startForegroundService(serviceIntent);
            
            // NEW: Start enhanced call service for call history and preparation
            Intent enhancedServiceIntent = new Intent(context, EnhancedCallService.class);
            enhancedServiceIntent.setAction(EnhancedCallService.ACTION_CALL_STARTED);
            enhancedServiceIntent.putExtra("phoneNumber", incomingPhoneNumber);
            enhancedServiceIntent.putExtra("callType", "inbound");
            context.startService(enhancedServiceIntent);

        } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
            // Call answered
            if (wasRinging && isIncoming) {
                Log.d(TAG, "Incoming call answered: " + incomingPhoneNumber);
                
                Intent serviceIntent = new Intent(context, CallReceiverService.class);
                serviceIntent.setAction(CallReceiverService.ACTION_CALL_ANSWERED);
                serviceIntent.putExtra("phoneNumber", incomingPhoneNumber);
                serviceIntent.putExtra("callType", "incoming");
                context.startForegroundService(serviceIntent);
            }

        } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            // Call ended
            if (wasRinging && isIncoming) {
                if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(lastState)) {
                    // Call was answered and now ended
                    Log.d(TAG, "Incoming call ended (was answered): " + incomingPhoneNumber);
                    
                    Intent serviceIntent = new Intent(context, CallReceiverService.class);
                    serviceIntent.setAction(CallReceiverService.ACTION_CALL_ENDED);
                    serviceIntent.putExtra("phoneNumber", incomingPhoneNumber);
                    serviceIntent.putExtra("callType", "incoming");
                    serviceIntent.putExtra("callStatus", "completed");
                    context.startForegroundService(serviceIntent);
                    
                    // NEW: Enhanced call service for automatic ticket creation
                    Intent enhancedServiceIntent = new Intent(context, EnhancedCallService.class);
                    enhancedServiceIntent.setAction(EnhancedCallService.ACTION_CALL_ENDED);
                    enhancedServiceIntent.putExtra("phoneNumber", incomingPhoneNumber);
                    enhancedServiceIntent.putExtra("callType", "inbound");
                    enhancedServiceIntent.putExtra("duration", 0); // TODO: Calculate actual duration
                    enhancedServiceIntent.putExtra("status", "completed");
                    context.startService(enhancedServiceIntent);
                    
                } else {
                    // Call was missed
                    Log.d(TAG, "Incoming call missed: " + incomingPhoneNumber);
                    
                    Intent serviceIntent = new Intent(context, CallReceiverService.class);
                    serviceIntent.setAction(CallReceiverService.ACTION_CALL_ENDED);
                    serviceIntent.putExtra("phoneNumber", incomingPhoneNumber);
                    serviceIntent.putExtra("callType", "missed");
                    serviceIntent.putExtra("callStatus", "missed");
                    context.startForegroundService(serviceIntent);
                    
                    // NEW: Enhanced call service for automatic ticket creation
                    Intent enhancedServiceIntent = new Intent(context, EnhancedCallService.class);
                    enhancedServiceIntent.setAction(EnhancedCallService.ACTION_CALL_ENDED);
                    enhancedServiceIntent.putExtra("phoneNumber", incomingPhoneNumber);
                    enhancedServiceIntent.putExtra("callType", "inbound");
                    enhancedServiceIntent.putExtra("duration", 0);
                    enhancedServiceIntent.putExtra("status", "missed");
                    context.startService(enhancedServiceIntent);
                }
            }

            // Reset state
            resetCallState();
        }

        lastState = state;
    }

    private void handleOutgoingCall(Context context, Intent intent) {
        String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        Log.d(TAG, "Outgoing call initiated: " + phoneNumber);

        isIncoming = false;
        incomingPhoneNumber = phoneNumber;

        Intent serviceIntent = new Intent(context, CallReceiverService.class);
        serviceIntent.setAction(CallReceiverService.ACTION_OUTGOING_CALL);
        serviceIntent.putExtra("phoneNumber", phoneNumber);
        serviceIntent.putExtra("callType", "outgoing");
        context.startForegroundService(serviceIntent);
        
        // NEW: Enhanced call service for outgoing calls
        Intent enhancedServiceIntent = new Intent(context, EnhancedCallService.class);
        enhancedServiceIntent.setAction(EnhancedCallService.ACTION_CALL_STARTED);
        enhancedServiceIntent.putExtra("phoneNumber", phoneNumber);
        enhancedServiceIntent.putExtra("callType", "outbound");
        context.startService(enhancedServiceIntent);
    }

    private void resetCallState() {
        lastState = TelephonyManager.EXTRA_STATE_IDLE;
        incomingPhoneNumber = "";
        isIncoming = false;
        wasRinging = false;
    }
}