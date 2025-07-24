package com.calltrackerpro.calltracker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.calltrackerpro.calltracker.services.CallReceiverService;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received broadcast: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) ||
            Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            
            Log.d(TAG, "System boot completed, starting CallReceiverService");
            
            // Start the call receiver service on boot
            Intent serviceIntent = new Intent(context, CallReceiverService.class);
            serviceIntent.setAction(CallReceiverService.ACTION_START_SERVICE);
            context.startForegroundService(serviceIntent);
        }
    }
}