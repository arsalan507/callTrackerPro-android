package com.calltrackerpro.calltracker.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.models.CallLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CallLogsAdapter extends RecyclerView.Adapter<CallLogsAdapter.CallLogViewHolder> {
    
    private List<CallLog> callLogs;
    private OnCallLogClickListener listener;

    public interface OnCallLogClickListener {
        void onCallLogClick(CallLog callLog);
        void onCallLogLongClick(CallLog callLog);
    }

    public CallLogsAdapter(List<CallLog> callLogs, OnCallLogClickListener listener) {
        this.callLogs = callLogs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CallLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_call_log, parent, false);
        return new CallLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallLogViewHolder holder, int position) {
        CallLog callLog = callLogs.get(position);
        holder.bind(callLog, listener);
    }

    @Override
    public int getItemCount() {
        return callLogs.size();
    }

    public static class CallLogViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView textContactName;
        private TextView textPhoneNumber;
        private TextView textCallType;
        private TextView textCallDuration;
        private TextView textCallDate;
        private TextView textCallStatus;
        private ImageView iconCallType;
        private ImageView buttonCreateTicket;
        private View callTypeIndicator;

        public CallLogViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = itemView.findViewById(R.id.card_call_log);
            textContactName = itemView.findViewById(R.id.text_contact_name);
            textPhoneNumber = itemView.findViewById(R.id.text_phone_number);
            textCallType = itemView.findViewById(R.id.text_call_type);
            textCallDuration = itemView.findViewById(R.id.text_call_duration);
            textCallDate = itemView.findViewById(R.id.text_call_date);
            textCallStatus = itemView.findViewById(R.id.text_call_status);
            iconCallType = itemView.findViewById(R.id.icon_call_type);
            buttonCreateTicket = itemView.findViewById(R.id.button_create_ticket);
            callTypeIndicator = itemView.findViewById(R.id.call_type_indicator);
        }

        public void bind(CallLog callLog, OnCallLogClickListener listener) {
            // Contact information
            textContactName.setText(callLog.getDisplayName());
            textPhoneNumber.setText(callLog.getPhoneNumber());
            
            // Call information
            setCallType(callLog.getCallType());
            textCallDuration.setText(callLog.getFormattedDuration());
            textCallDate.setText(formatTimestamp(callLog.getTimestamp()));
            setCallStatus(callLog.getCallStatus());

            // Click listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCallLogClick(callLog);
                }
            });

            cardView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onCallLogLongClick(callLog);
                }
                return true;
            });

            // Create ticket button
            buttonCreateTicket.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCallLogLongClick(callLog); // Reuse long click for ticket creation
                }
            });
        }

        private void setCallType(String callType) {
            if (callType == null) callType = "unknown";
            
            switch (callType.toLowerCase()) {
                case "incoming":
                    iconCallType.setImageResource(R.drawable.ic_call_incoming);
                    textCallType.setText("Incoming");
                    textCallType.setTextColor(Color.parseColor("#4CAF50")); // Green
                    callTypeIndicator.setBackgroundColor(Color.parseColor("#4CAF50"));
                    break;
                case "outgoing":
                    iconCallType.setImageResource(R.drawable.ic_call_outgoing);
                    textCallType.setText("Outgoing");
                    textCallType.setTextColor(Color.parseColor("#2196F3")); // Blue
                    callTypeIndicator.setBackgroundColor(Color.parseColor("#2196F3"));
                    break;
                case "missed":
                    iconCallType.setImageResource(R.drawable.ic_call_missed);
                    textCallType.setText("Missed");
                    textCallType.setTextColor(Color.parseColor("#F44336")); // Red
                    callTypeIndicator.setBackgroundColor(Color.parseColor("#F44336"));
                    break;
                default:
                    iconCallType.setImageResource(R.drawable.ic_phone);
                    textCallType.setText("Unknown");
                    textCallType.setTextColor(Color.parseColor("#9E9E9E")); // Gray
                    callTypeIndicator.setBackgroundColor(Color.parseColor("#9E9E9E"));
                    break;
            }
        }

        private void setCallStatus(String status) {
            if (status == null) status = "unknown";
            
            switch (status.toLowerCase()) {
                case "completed":
                    textCallStatus.setText("Completed");
                    textCallStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
                    textCallStatus.setVisibility(View.VISIBLE);
                    break;
                case "missed":
                    textCallStatus.setText("Missed");
                    textCallStatus.setTextColor(Color.parseColor("#F44336")); // Red
                    textCallStatus.setVisibility(View.VISIBLE);
                    break;
                case "declined":
                    textCallStatus.setText("Declined");
                    textCallStatus.setTextColor(Color.parseColor("#FF9800")); // Orange
                    textCallStatus.setVisibility(View.VISIBLE);
                    break;
                case "busy":
                    textCallStatus.setText("Busy");
                    textCallStatus.setTextColor(Color.parseColor("#FF9800")); // Orange
                    textCallStatus.setVisibility(View.VISIBLE);
                    break;
                default:
                    textCallStatus.setVisibility(View.GONE);
                    break;
            }
        }

        private String formatTimestamp(long timestamp) {
            if (timestamp <= 0) return "";
            
            try {
                Date date = new Date(timestamp);
                Date now = new Date();
                
                // Check if it's today
                SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                if (dayFormat.format(date).equals(dayFormat.format(now))) {
                    // Show time only for today
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    return timeFormat.format(date);
                } else {
                    // Show date and time for other days
                    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                    return dateTimeFormat.format(date);
                }
            } catch (Exception e) {
                return "";
            }
        }
    }
}