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
import com.calltrackerpro.calltracker.models.Ticket;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TicketsAdapter extends RecyclerView.Adapter<TicketsAdapter.TicketViewHolder> {
    
    private List<Ticket> tickets;
    private OnTicketClickListener listener;

    public interface OnTicketClickListener {
        void onTicketClick(Ticket ticket);
        void onTicketLongClick(Ticket ticket);
    }

    public TicketsAdapter(List<Ticket> tickets, OnTicketClickListener listener) {
        this.tickets = tickets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = tickets.get(position);
        holder.bind(ticket, listener);
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView textContactName;
        private TextView textPhoneNumber;
        private TextView textCompany;
        private TextView textCallType;
        private TextView textCallDuration;
        private TextView textCallDate;
        private TextView textLeadStatus;
        private TextView textStage;
        private TextView textPriority;
        private TextView textDealValue;
        private TextView textAssignedAgent;
        private ImageView iconCallType;
        private ImageView iconPriority;
        private View priorityIndicator;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = itemView.findViewById(R.id.card_ticket);
            textContactName = itemView.findViewById(R.id.text_contact_name);
            textPhoneNumber = itemView.findViewById(R.id.text_phone_number);
            textCompany = itemView.findViewById(R.id.text_company);
            textCallType = itemView.findViewById(R.id.text_call_type);
            textCallDuration = itemView.findViewById(R.id.text_call_duration);
            textCallDate = itemView.findViewById(R.id.text_call_date);
            textLeadStatus = itemView.findViewById(R.id.text_lead_status);
            textStage = itemView.findViewById(R.id.text_stage);
            textPriority = itemView.findViewById(R.id.text_priority);
            textDealValue = itemView.findViewById(R.id.text_deal_value);
            textAssignedAgent = itemView.findViewById(R.id.text_assigned_agent);
            iconCallType = itemView.findViewById(R.id.icon_call_type);
            iconPriority = itemView.findViewById(R.id.icon_priority);
            priorityIndicator = itemView.findViewById(R.id.priority_indicator);
        }

        public void bind(Ticket ticket, OnTicketClickListener listener) {
            // Basic contact information
            textContactName.setText(ticket.getDisplayName());
            textPhoneNumber.setText(ticket.getPhoneNumber());
            
            // Company information
            if (ticket.getCompany() != null && !ticket.getCompany().isEmpty()) {
                textCompany.setText(ticket.getCompany());
                textCompany.setVisibility(View.VISIBLE);
            } else {
                textCompany.setVisibility(View.GONE);
            }

            // Call information
            setCallType(ticket.getCallType());
            textCallDuration.setText(ticket.getFormattedDuration());
            textCallDate.setText(formatDate(ticket.getCallDate()));

            // Lead information
            setLeadStatus(ticket.getLeadStatus());
            setStage(ticket.getStage());
            setPriority(ticket.getPriority());

            // Deal value
            if (ticket.getDealValue() > 0) {
                textDealValue.setText(String.format(Locale.getDefault(), "$%.2f", ticket.getDealValue()));
                textDealValue.setVisibility(View.VISIBLE);
            } else {
                textDealValue.setVisibility(View.GONE);
            }

            // Assigned agent
            if (ticket.getAssignedAgent() != null && !ticket.getAssignedAgent().isEmpty()) {
                textAssignedAgent.setText("Assigned: " + ticket.getAssignedAgent()); // TODO: Get agent name
                textAssignedAgent.setVisibility(View.VISIBLE);
            } else {
                textAssignedAgent.setText("Unassigned");
                textAssignedAgent.setVisibility(View.VISIBLE);
            }

            // Click listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTicketClick(ticket);
                }
            });

            cardView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onTicketLongClick(ticket);
                }
                return true;
            });
        }

        private void setCallType(String callType) {
            if (callType == null) callType = "unknown";
            
            switch (callType.toLowerCase()) {
                case "incoming":
                    iconCallType.setImageResource(R.drawable.ic_phone); // Incoming call icon
                    textCallType.setText("Incoming");
                    textCallType.setTextColor(Color.parseColor("#4CAF50")); // Green
                    break;
                case "outgoing":
                    iconCallType.setImageResource(R.drawable.ic_phone); // Outgoing call icon
                    textCallType.setText("Outgoing");
                    textCallType.setTextColor(Color.parseColor("#2196F3")); // Blue
                    break;
                case "missed":
                    iconCallType.setImageResource(R.drawable.ic_phone); // Missed call icon
                    textCallType.setText("Missed");
                    textCallType.setTextColor(Color.parseColor("#F44336")); // Red
                    break;
                default:
                    iconCallType.setImageResource(R.drawable.ic_phone);
                    textCallType.setText("Unknown");
                    textCallType.setTextColor(Color.parseColor("#9E9E9E")); // Gray
                    break;
            }
        }

        private void setLeadStatus(String status) {
            if (status == null) status = "new";
            
            textLeadStatus.setText(formatStatus(status));
            
            switch (status.toLowerCase()) {
                case "new":
                    textLeadStatus.setBackgroundColor(Color.parseColor("#E3F2FD")); // Light blue
                    textLeadStatus.setTextColor(Color.parseColor("#1976D2")); // Blue
                    break;
                case "contacted":
                    textLeadStatus.setBackgroundColor(Color.parseColor("#FFF3E0")); // Light orange
                    textLeadStatus.setTextColor(Color.parseColor("#F57C00")); // Orange
                    break;
                case "qualified":
                    textLeadStatus.setBackgroundColor(Color.parseColor("#E8F5E8")); // Light green
                    textLeadStatus.setTextColor(Color.parseColor("#388E3C")); // Green
                    break;
                case "converted":
                    textLeadStatus.setBackgroundColor(Color.parseColor("#E1F5FE")); // Light cyan
                    textLeadStatus.setTextColor(Color.parseColor("#0097A7")); // Cyan
                    break;
                case "closed":
                    textLeadStatus.setBackgroundColor(Color.parseColor("#FAFAFA")); // Light gray
                    textLeadStatus.setTextColor(Color.parseColor("#616161")); // Gray
                    break;
                default:
                    textLeadStatus.setBackgroundColor(Color.parseColor("#F5F5F5"));
                    textLeadStatus.setTextColor(Color.parseColor("#757575"));
                    break;
            }
        }

        private void setStage(String stage) {
            if (stage == null) stage = "prospect";
            textStage.setText(formatStage(stage));
        }

        private void setPriority(String priority) {
            if (priority == null) priority = "medium";
            
            textPriority.setText(formatPriority(priority));
            
            switch (priority.toLowerCase()) {
                case "high":
                    iconPriority.setImageResource(R.drawable.ic_priority_high);
                    priorityIndicator.setBackgroundColor(Color.parseColor("#F44336")); // Red
                    textPriority.setTextColor(Color.parseColor("#F44336"));
                    break;
                case "medium":
                    iconPriority.setImageResource(R.drawable.ic_priority_medium);
                    priorityIndicator.setBackgroundColor(Color.parseColor("#FF9800")); // Orange
                    textPriority.setTextColor(Color.parseColor("#FF9800"));
                    break;
                case "low":
                    iconPriority.setImageResource(R.drawable.ic_priority_low);
                    priorityIndicator.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
                    textPriority.setTextColor(Color.parseColor("#4CAF50"));
                    break;
                default:
                    iconPriority.setImageResource(R.drawable.ic_priority_medium);
                    priorityIndicator.setBackgroundColor(Color.parseColor("#9E9E9E")); // Gray
                    textPriority.setTextColor(Color.parseColor("#9E9E9E"));
                    break;
            }
        }

        private String formatStatus(String status) {
            if (status == null) return "New";
            switch (status.toLowerCase()) {
                case "new": return "New";
                case "contacted": return "Contacted";
                case "qualified": return "Qualified";
                case "converted": return "Converted";
                case "closed": return "Closed";
                default: return status;
            }
        }

        private String formatStage(String stage) {
            if (stage == null) return "Prospect";
            switch (stage.toLowerCase()) {
                case "prospect": return "Prospect";
                case "qualified": return "Qualified";
                case "proposal": return "Proposal";
                case "negotiation": return "Negotiation";
                case "closed-won": return "Closed Won";
                case "closed-lost": return "Closed Lost";
                default: return stage;
            }
        }

        private String formatPriority(String priority) {
            if (priority == null) return "Medium";
            switch (priority.toLowerCase()) {
                case "high": return "High";
                case "medium": return "Medium";
                case "low": return "Low";
                default: return priority;
            }
        }

        private String formatDate(String dateString) {
            if (dateString == null || dateString.isEmpty()) {
                return "";
            }
            
            try {
                // Assuming the date comes in ISO format
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                
                Date date = inputFormat.parse(dateString);
                return date != null ? outputFormat.format(date) : dateString;
            } catch (Exception e) {
                // Fallback to original string if parsing fails
                return dateString;
            }
        }
    }
}