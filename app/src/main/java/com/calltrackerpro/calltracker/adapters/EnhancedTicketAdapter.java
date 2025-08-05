package com.calltrackerpro.calltracker.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.models.Ticket;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EnhancedTicketAdapter extends RecyclerView.Adapter<EnhancedTicketAdapter.TicketViewHolder> {
    
    private List<Ticket> tickets;
    private List<Ticket> filteredTickets;
    private Context context;
    private OnTicketClickListener listener;
    
    public interface OnTicketClickListener {
        void onTicketClick(Ticket ticket);
        void onTicketLongClick(Ticket ticket);
        void onTicketMenuClick(Ticket ticket, View anchorView);
    }
    
    public EnhancedTicketAdapter(Context context) {
        this.context = context;
        this.tickets = new ArrayList<>();
        this.filteredTickets = new ArrayList<>();
    }
    
    public void setOnTicketClickListener(OnTicketClickListener listener) {
        this.listener = listener;
    }
    
    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets != null ? tickets : new ArrayList<>();
        this.filteredTickets = new ArrayList<>(this.tickets);
        notifyDataSetChanged();
    }
    
    public void addTicket(Ticket newTicket) {
        tickets.add(0, newTicket);
        filteredTickets.add(0, newTicket);
        notifyItemInserted(0);
    }
    
    public void updateTicket(Ticket updatedTicket) {
        for (int i = 0; i < tickets.size(); i++) {
            if (tickets.get(i).getId().equals(updatedTicket.getId())) {
                tickets.set(i, updatedTicket);
                break;
            }
        }
        
        for (int i = 0; i < filteredTickets.size(); i++) {
            if (filteredTickets.get(i).getId().equals(updatedTicket.getId())) {
                filteredTickets.set(i, updatedTicket);
                notifyItemChanged(i);
                break;
            }
        }
    }
    
    public void filter(String query) {
        filteredTickets.clear();
        
        if (query == null || query.trim().isEmpty()) {
            filteredTickets.addAll(tickets);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Ticket ticket : tickets) {
                if (ticket.getContactName() != null && ticket.getContactName().toLowerCase().contains(lowerCaseQuery) ||
                    ticket.getPhoneNumber() != null && ticket.getPhoneNumber().toLowerCase().contains(lowerCaseQuery) ||
                    ticket.getCompany() != null && ticket.getCompany().toLowerCase().contains(lowerCaseQuery) ||
                    ticket.getTicketId() != null && ticket.getTicketId().toLowerCase().contains(lowerCaseQuery) ||
                    ticket.getEmail() != null && ticket.getEmail().toLowerCase().contains(lowerCaseQuery)) {
                    filteredTickets.add(ticket);
                }
            }
        }
        
        notifyDataSetChanged();
    }
    
    public void applyFilters(String statusFilter, String priorityFilter, String tabFilter, String searchQuery, String currentUserId) {
        filteredTickets.clear();
        
        for (Ticket ticket : tickets) {
            if (matchesFilters(ticket, statusFilter, priorityFilter, tabFilter, searchQuery, currentUserId)) {
                filteredTickets.add(ticket);
            }
        }
        
        notifyDataSetChanged();
    }
    
    private boolean matchesFilters(Ticket ticket, String statusFilter, String priorityFilter, String tabFilter, String searchQuery, String currentUserId) {
        // Search query filter
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String lowerCaseQuery = searchQuery.toLowerCase().trim();
            boolean matchesSearch = (ticket.getContactName() != null && ticket.getContactName().toLowerCase().contains(lowerCaseQuery)) ||
                                  (ticket.getPhoneNumber() != null && ticket.getPhoneNumber().toLowerCase().contains(lowerCaseQuery)) ||
                                  (ticket.getCompany() != null && ticket.getCompany().toLowerCase().contains(lowerCaseQuery)) ||
                                  (ticket.getTicketId() != null && ticket.getTicketId().toLowerCase().contains(lowerCaseQuery)) ||
                                  (ticket.getEmail() != null && ticket.getEmail().toLowerCase().contains(lowerCaseQuery));
            if (!matchesSearch) return false;
        }
        
        // Status filter
        if (statusFilter != null && !statusFilter.equals("all")) {
            String ticketStatus = ticket.getStatus() != null ? ticket.getStatus() : ticket.getLeadStatus();
            if (!statusFilter.equals(ticketStatus)) return false;
        }
        
        // Priority filter
        if (priorityFilter != null && !priorityFilter.equals("all")) {
            if (!priorityFilter.equals(ticket.getPriority())) return false;
        }
        
        // Tab filter
        if (tabFilter != null && !tabFilter.equals("all")) {
            switch (tabFilter) {
                case "my_tickets":
                    if (currentUserId == null || !currentUserId.equals(ticket.getAssignedTo())) {
                        return false;
                    }
                    break;
                case "open":
                    String status = ticket.getStatus() != null ? ticket.getStatus() : ticket.getLeadStatus();
                    if (!"open".equals(status) && !"new".equals(status)) {
                        return false;
                    }
                    break;
                case "in_progress":
                    String progressStatus = ticket.getStatus() != null ? ticket.getStatus() : ticket.getLeadStatus();
                    if (!"in_progress".equals(progressStatus) && !"contacted".equals(progressStatus)) {
                        return false;
                    }
                    break;
                case "high_priority":
                    if (!"high".equals(ticket.getPriority()) && !"urgent".equals(ticket.getPriority())) {
                        return false;
                    }
                    break;
                case "overdue":
                    if (!ticket.isOverdue()) {
                        return false;
                    }
                    break;
            }
        }
        
        return true;
    }
    
    public void filterByStatus(String status) {
        filteredTickets.clear();
        
        if (status == null || status.equals("all")) {
            filteredTickets.addAll(tickets);
        } else {
            for (Ticket ticket : tickets) {
                if (status.equals(ticket.getStatus()) || status.equals(ticket.getLeadStatus())) {
                    filteredTickets.add(ticket);
                }
            }
        }
        
        notifyDataSetChanged();
    }
    
    public void filterByPriority(String priority) {
        filteredTickets.clear();
        
        if (priority == null || priority.equals("all")) {
            filteredTickets.addAll(tickets);
        } else {
            for (Ticket ticket : tickets) {
                if (priority.equals(ticket.getPriority())) {
                    filteredTickets.add(ticket);
                }
            }
        }
        
        notifyDataSetChanged();
    }
    
    public void filterByAssignment(String assignment) {
        filteredTickets.clear();
        
        if (assignment == null || assignment.equals("all")) {
            filteredTickets.addAll(tickets);
        } else if (assignment.equals("assigned")) {
            for (Ticket ticket : tickets) {
                if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().isEmpty()) {
                    filteredTickets.add(ticket);
                }
            }
        } else if (assignment.equals("unassigned")) {
            for (Ticket ticket : tickets) {
                if (ticket.getAssignedTo() == null || ticket.getAssignedTo().isEmpty()) {
                    filteredTickets.add(ticket);
                }
            }
        }
        
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ticket_enhanced, parent, false);
        return new TicketViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = filteredTickets.get(position);
        holder.bind(ticket);
    }
    
    @Override
    public int getItemCount() {
        return filteredTickets.size();
    }
    
    class TicketViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTicketNumber;
        private TextView tvSlaStatus;
        private TextView tvContactAvatar;
        private TextView tvContactName;
        private TextView tvContactInfo;
        private TextView tvTicketStatus;
        private TextView tvCategory;
        private TextView tvSource;
        private TextView tvAssignedTo;
        private TextView tvDueDate;
        private View priorityIndicator;
        private TextView tvPriority;
        private TextView tvDealValue;
        private ImageView btnTicketMenu;
        
        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvTicketNumber = itemView.findViewById(R.id.tvTicketNumber);
            tvSlaStatus = itemView.findViewById(R.id.tvSlaStatus);
            tvContactAvatar = itemView.findViewById(R.id.tvContactAvatar);
            tvContactName = itemView.findViewById(R.id.tvContactName);
            tvContactInfo = itemView.findViewById(R.id.tvContactInfo);
            tvTicketStatus = itemView.findViewById(R.id.tvTicketStatus);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvSource = itemView.findViewById(R.id.tvSource);
            tvAssignedTo = itemView.findViewById(R.id.tvAssignedTo);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            tvDealValue = itemView.findViewById(R.id.tvDealValue);
            btnTicketMenu = itemView.findViewById(R.id.btnTicketMenu);
            
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onTicketClick(filteredTickets.get(getAdapterPosition()));
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onTicketLongClick(filteredTickets.get(getAdapterPosition()));
                    return true;
                }
                return false;
            });
            
            btnTicketMenu.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onTicketMenuClick(filteredTickets.get(getAdapterPosition()), v);
                }
            });
        }
        
        public void bind(Ticket ticket) {
            // Set ticket number
            String ticketNumber = ticket.getTicketId() != null ? ticket.getTicketId() : 
                "#TKT-" + (ticket.getId() != null ? ticket.getId().substring(0, 6) : "000000");
            tvTicketNumber.setText(ticketNumber);
            
            // Set SLA status
            setSlaStatus(ticket.getSlaStatus());
            
            // Set contact avatar
            String avatarText = ticket.getContactName() != null && !ticket.getContactName().isEmpty() ? 
                ticket.getContactName().substring(0, 1).toUpperCase() : "?";
            tvContactAvatar.setText(avatarText);
            setContactAvatarBackground(tvContactAvatar, ticket.getPriority());
            
            // Set contact info
            tvContactName.setText(ticket.getDisplayName());
            
            StringBuilder contactInfo = new StringBuilder();
            if (ticket.getPhoneNumber() != null) {
                contactInfo.append(ticket.getPhoneNumber());
            }
            if (ticket.getCompany() != null && !ticket.getCompany().isEmpty()) {
                if (contactInfo.length() > 0) contactInfo.append(" • ");
                contactInfo.append(ticket.getCompany());
            }
            tvContactInfo.setText(contactInfo.toString());
            
            // Set ticket status
            setTicketStatus(ticket.getStatus() != null ? ticket.getStatus() : ticket.getLeadStatus());
            
            // Set category
            tvCategory.setText(ticket.getCategoryDisplayName());
            
            // Set source with icon
            setSource(ticket.getSource());
            
            // Set assignment
            if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().isEmpty()) {
                tvAssignedTo.setText("Assigned: " + getAssigneeName(ticket.getAssignedTo()));
            } else {
                tvAssignedTo.setText("Unassigned");
                tvAssignedTo.setTextColor(ContextCompat.getColor(context, R.color.error_red));
            }
            
            // Set due date
            setDueDate(ticket.getDueDate());
            
            // Set priority
            setPriority(ticket.getPriority());
            
            // Set deal value
            if (ticket.getDealValue() > 0) {
                tvDealValue.setText(String.format(Locale.getDefault(), "$%.0f", ticket.getDealValue()));
                tvDealValue.setVisibility(View.VISIBLE);
            } else {
                tvDealValue.setVisibility(View.GONE);
            }
        }
        
        private void setSlaStatus(String slaStatus) {
            String status = slaStatus != null ? slaStatus : "on_track";
            
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(8f);
            
            switch (status) {
                case "on_track":
                    tvSlaStatus.setText("On Track");
                    drawable.setColor(ContextCompat.getColor(context, R.color.success_green));
                    break;
                case "at_risk":
                    tvSlaStatus.setText("At Risk");
                    drawable.setColor(ContextCompat.getColor(context, R.color.warning_color));
                    break;
                case "breached":
                    tvSlaStatus.setText("Breached");
                    drawable.setColor(ContextCompat.getColor(context, R.color.error_red));
                    break;
                default:
                    tvSlaStatus.setText("Unknown");
                    drawable.setColor(ContextCompat.getColor(context, R.color.role_default));
                    break;
            }
            
            tvSlaStatus.setBackground(drawable);
        }
        
        private void setContactAvatarBackground(TextView avatar, String priority) {
            int color = getColorForPriority(priority);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            avatar.setBackground(drawable);
        }
        
        private void setTicketStatus(String status) {
            String displayStatus = status != null ? status : "open";
            
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(12f);
            
            switch (displayStatus.toLowerCase()) {
                case "open":
                case "new":
                    tvTicketStatus.setText("Open");
                    drawable.setColor(ContextCompat.getColor(context, R.color.info_color));
                    break;
                case "in_progress":
                case "contacted":
                    tvTicketStatus.setText("In Progress");
                    drawable.setColor(ContextCompat.getColor(context, R.color.warning_color));
                    break;
                case "resolved":
                case "qualified":
                    tvTicketStatus.setText("Resolved");
                    drawable.setColor(ContextCompat.getColor(context, R.color.success_green));
                    break;
                case "closed":
                case "converted":
                    tvTicketStatus.setText("Closed");
                    drawable.setColor(ContextCompat.getColor(context, R.color.role_default));
                    break;
                default:
                    tvTicketStatus.setText(displayStatus);
                    drawable.setColor(ContextCompat.getColor(context, R.color.role_default));
                    break;
            }
            
            tvTicketStatus.setBackground(drawable);
        }
        
        private void setSource(String source) {
            String sourceText = source != null ? source : "phone";
            
            switch (sourceText.toLowerCase()) {
                case "phone":
                    tvSource.setText("📞 Phone");
                    break;
                case "email":
                    tvSource.setText("📧 Email");
                    break;
                case "web":
                    tvSource.setText("🌐 Website");
                    break;
                case "mobile_app":
                    tvSource.setText("📱 Mobile");
                    break;
                default:
                    tvSource.setText("📄 " + sourceText);
                    break;
            }
        }
        
        private void setDueDate(String dueDate) {
            if (dueDate == null || dueDate.isEmpty()) {
                tvDueDate.setText("No due date");
                tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.role_default));
                return;
            }
            
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date due = sdf.parse(dueDate);
                Date now = new Date();
                
                if (due != null) {
                    long diffInMillis = due.getTime() - now.getTime();
                    long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
                    
                    if (diffInDays < 0) {
                        tvDueDate.setText("Overdue");
                        tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.error_red));
                    } else if (diffInDays == 0) {
                        tvDueDate.setText("Due: Today");
                        tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.warning_color));
                    } else if (diffInDays == 1) {
                        tvDueDate.setText("Due: Tomorrow");
                        tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.info_color));
                    } else {
                        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                        tvDueDate.setText("Due: " + displayFormat.format(due));
                        tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.role_default));
                    }
                }
            } catch (Exception e) {
                tvDueDate.setText("Due: " + dueDate);
                tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.role_default));
            }
        }
        
        private void setPriority(String priority) {
            String priorityText = priority != null ? priority : "medium";
            
            int color = getColorForPriority(priorityText);
            priorityIndicator.setBackgroundColor(color);
            
            switch (priorityText.toLowerCase()) {
                case "urgent":
                    tvPriority.setText("Urgent");
                    tvPriority.setTextColor(ContextCompat.getColor(context, R.color.error_red));
                    break;
                case "high":
                    tvPriority.setText("High");
                    tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_high));
                    break;
                case "medium":
                    tvPriority.setText("Medium");
                    tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_medium));
                    break;
                case "low":
                    tvPriority.setText("Low");
                    tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_low));
                    break;
                default:
                    tvPriority.setText(priorityText);
                    tvPriority.setTextColor(ContextCompat.getColor(context, R.color.role_default));
                    break;
            }
        }
        
        private int getColorForPriority(String priority) {
            if (priority == null) priority = "medium";
            
            switch (priority.toLowerCase()) {
                case "urgent":
                    return ContextCompat.getColor(context, R.color.error_red);
                case "high":
                    return ContextCompat.getColor(context, R.color.priority_high);
                case "medium":
                    return ContextCompat.getColor(context, R.color.priority_medium);
                case "low":
                    return ContextCompat.getColor(context, R.color.priority_low);
                default:
                    return ContextCompat.getColor(context, R.color.role_default);
            }
        }
        
        private String getAssigneeName(String assigneeId) {
            // TODO: Implement user lookup by ID
            // For now, return the ID or a shortened version
            if (assigneeId == null || assigneeId.length() < 6) {
                return assigneeId != null ? assigneeId : "Unknown";
            }
            return assigneeId.substring(0, 6) + "..."; // Temporary placeholder
        }
    }
}