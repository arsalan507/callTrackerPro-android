package com.calltrackerpro.calltracker.utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.snackbar.Snackbar;

public class UIHelper {
    
    /**
     * Show a short toast message
     */
    public static void showToast(Context context, String message) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show a long toast message
     */
    public static void showLongToast(Context context, String message) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Show a snackbar message
     */
    public static void showSnackbar(View view, String message) {
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show a snackbar with action
     */
    public static void showSnackbarWithAction(View view, String message, String actionText, View.OnClickListener actionListener) {
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                    .setAction(actionText, actionListener)
                    .show();
        }
    }
    
    /**
     * Show a simple alert dialog
     */
    public static void showAlertDialog(Context context, String title, String message) {
        if (context != null) {
            new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
    
    /**
     * Show a confirmation dialog
     */
    public static void showConfirmationDialog(Context context, String title, String message, 
                                            String positiveText, String negativeText,
                                            Runnable onConfirm, Runnable onCancel) {
        if (context != null) {
            new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(positiveText, (dialog, which) -> {
                        if (onConfirm != null) onConfirm.run();
                    })
                    .setNegativeButton(negativeText, (dialog, which) -> {
                        if (onCancel != null) onCancel.run();
                    })
                    .show();
        }
    }
    
    /**
     * Show error dialog
     */
    public static void showErrorDialog(Context context, String error) {
        showAlertDialog(context, "Error", error);
    }
    
    /**
     * Show success dialog
     */
    public static void showSuccessDialog(Context context, String message) {
        showAlertDialog(context, "Success", message);
    }
    
    /**
     * Show permission denied message
     */
    public static void showPermissionDenied(Context context, String action) {
        String message = "You don't have permission to " + action + ". Contact your administrator for access.";
        showErrorDialog(context, message);
    }
    
    /**
     * Show loading state
     */
    public static void showLoading(View loadingView, View contentView) {
        if (loadingView != null) loadingView.setVisibility(View.VISIBLE);
        if (contentView != null) contentView.setVisibility(View.GONE);
    }
    
    /**
     * Hide loading state
     */
    public static void hideLoading(View loadingView, View contentView) {
        if (loadingView != null) loadingView.setVisibility(View.GONE);
        if (contentView != null) contentView.setVisibility(View.VISIBLE);
    }
    
    /**
     * Format role name for display
     */
    public static String formatRoleName(String role) {
        if (role == null) return "User";
        
        switch (role.toLowerCase()) {
            case "org_admin":
                return "Organization Admin";
            case "manager":
                return "Manager";
            case "agent":
                return "Agent";
            case "viewer":
                return "Viewer";
            default:
                return role.substring(0, 1).toUpperCase() + role.substring(1);
        }
    }
    
    /**
     * Get role emoji
     */
    public static String getRoleEmoji(String role) {
        if (role == null) return "👤";
        
        switch (role.toLowerCase()) {
            case "org_admin":
                return "👑";
            case "manager":
                return "👨‍💼";
            case "agent":
                return "📞";
            case "viewer":
                return "👀";
            default:
                return "👤";
        }
    }
    
    /**
     * Format number for display
     */
    public static String formatNumber(int number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        } else {
            return String.valueOf(number);
        }
    }
    
    /**
     * Format percentage for display
     */
    public static String formatPercentage(double percentage) {
        return String.format("%.1f%%", percentage * 100);
    }
    
    /**
     * Get status color based on subscription status
     */
    public static int getStatusColor(Context context, String status) {
        if (context == null || status == null) {
            return android.R.color.darker_gray;
        }
        
        switch (status.toLowerCase()) {
            case "active":
                return android.R.color.holo_green_dark;
            case "inactive":
            case "expired":
                return android.R.color.holo_red_dark;
            case "trial":
                return android.R.color.holo_orange_dark;
            default:
                return android.R.color.darker_gray;
        }
    }
}