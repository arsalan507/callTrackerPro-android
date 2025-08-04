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
import com.calltrackerpro.calltracker.models.Organization;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrganizationManagementAdapter extends RecyclerView.Adapter<OrganizationManagementAdapter.OrganizationViewHolder> {
    
    private List<Organization> organizations;
    private List<Organization> filteredOrganizations;
    private Context context;
    private OnOrganizationClickListener listener;
    
    public interface OnOrganizationClickListener {
        void onOrganizationClick(Organization organization);
        void onOrganizationMenuClick(Organization organization, View anchorView);
    }
    
    public OrganizationManagementAdapter(Context context) {
        this.context = context;
        this.organizations = new ArrayList<>();
        this.filteredOrganizations = new ArrayList<>();
    }
    
    public void setOnOrganizationClickListener(OnOrganizationClickListener listener) {
        this.listener = listener;
    }
    
    public void setOrganizations(List<Organization> organizations) {
        this.organizations = organizations != null ? organizations : new ArrayList<>();
        this.filteredOrganizations = new ArrayList<>(this.organizations);
        notifyDataSetChanged();
    }
    
    public void updateOrganization(Organization updatedOrganization) {
        for (int i = 0; i < organizations.size(); i++) {
            if (organizations.get(i).getId().equals(updatedOrganization.getId())) {
                organizations.set(i, updatedOrganization);
                break;
            }
        }
        
        for (int i = 0; i < filteredOrganizations.size(); i++) {
            if (filteredOrganizations.get(i).getId().equals(updatedOrganization.getId())) {
                filteredOrganizations.set(i, updatedOrganization);
                notifyItemChanged(i);
                break;
            }
        }
    }
    
    public void filter(String query) {
        filteredOrganizations.clear();
        
        if (query == null || query.trim().isEmpty()) {
            filteredOrganizations.addAll(organizations);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Organization org : organizations) {
                if (org.getName().toLowerCase().contains(lowerCaseQuery) ||
                    (org.getDomain() != null && org.getDomain().toLowerCase().contains(lowerCaseQuery)) ||
                    (org.getSubscriptionPlan() != null && org.getSubscriptionPlan().toLowerCase().contains(lowerCaseQuery))) {
                    filteredOrganizations.add(org);
                }
            }
        }
        
        notifyDataSetChanged();
    }
    
    public void filterByStatus(String status) {
        filteredOrganizations.clear();
        
        if (status == null || status.equals("all")) {
            filteredOrganizations.addAll(organizations);
        } else {
            for (Organization org : organizations) {
                if (status.equals(org.getSubscriptionStatus())) {
                    filteredOrganizations.add(org);
                }
            }
        }
        
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public OrganizationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_organization_management, parent, false);
        return new OrganizationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull OrganizationViewHolder holder, int position) {
        Organization organization = filteredOrganizations.get(position);
        holder.bind(organization);
    }
    
    @Override
    public int getItemCount() {
        return filteredOrganizations.size();
    }
    
    class OrganizationViewHolder extends RecyclerView.ViewHolder {
        private TextView tvOrgIcon;
        private TextView tvOrgName;
        private TextView tvOrgStatus;
        private TextView tvSubscriptionStatus;
        private TextView tvUserCount;
        private TextView tvTicketCount;
        private TextView tvLastActivity;
        private ImageView btnOrgMenu;
        
        public OrganizationViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvOrgIcon = itemView.findViewById(R.id.tvOrgIcon);
            tvOrgName = itemView.findViewById(R.id.tvOrgName);
            tvOrgStatus = itemView.findViewById(R.id.tvOrgStatus);
            tvSubscriptionStatus = itemView.findViewById(R.id.tvSubscriptionStatus);
            tvUserCount = itemView.findViewById(R.id.tvUserCount);
            tvTicketCount = itemView.findViewById(R.id.tvTicketCount);
            tvLastActivity = itemView.findViewById(R.id.tvLastActivity);
            btnOrgMenu = itemView.findViewById(R.id.btnOrgMenu);
            
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onOrganizationClick(filteredOrganizations.get(getAdapterPosition()));
                }
            });
            
            btnOrgMenu.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onOrganizationMenuClick(filteredOrganizations.get(getAdapterPosition()), v);
                }
            });
        }
        
        public void bind(Organization organization) {
            // Set organization icon with first letter of name
            String iconText = organization.getName().substring(0, 1).toUpperCase();
            tvOrgIcon.setText(iconText);
            setOrgIconBackground(tvOrgIcon, organization.getSubscriptionStatus());
            
            // Set organization info
            tvOrgName.setText(organization.getName());
            
            // Set status badge
            String status = organization.getSubscriptionStatus() != null ? 
                organization.getSubscriptionStatus() : "unknown";
            tvOrgStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
            setStatusBackground(tvOrgStatus, status);
            
            // Set subscription status
            String subscriptionText = organization.getSubscriptionPlan() != null ? 
                organization.getSubscriptionPlan() + " Plan" : "No Plan";
            tvSubscriptionStatus.setText(subscriptionText);
            
            // Set user count - use mock data if not available
            int userCount = organization.getUserCount();
            if (userCount <= 0) {
                userCount = Math.abs(organization.getId().hashCode() % 50 + 1); // Mock data
            }
            tvUserCount.setText("Users: " + userCount);
            
            // Set ticket count - use mock data if not available
            int ticketCount = organization.getTicketCount();
            if (ticketCount <= 0) {
                ticketCount = Math.abs(organization.getId().hashCode() % 200 + 10); // Mock data
            }
            tvTicketCount.setText("Tickets: " + ticketCount);
            
            // Set last activity time
            String lastActivityText = getLastActivityText(organization.getLastActivity());
            tvLastActivity.setText("Last: " + lastActivityText);
        }
        
        private void setOrgIconBackground(TextView icon, String status) {
            int color = getColorForStatus(status);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            icon.setBackground(drawable);
        }
        
        private void setStatusBackground(TextView statusView, String status) {
            int color = getColorForStatus(status);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(12f);
            drawable.setColor(color);
            statusView.setBackground(drawable);
        }
        
        private int getColorForStatus(String status) {
            if (status == null) status = "unknown";
            
            switch (status.toLowerCase()) {
                case "active":
                    return ContextCompat.getColor(context, R.color.success_green);
                case "suspended":
                case "inactive":
                    return ContextCompat.getColor(context, R.color.error_red);
                case "trial":
                    return ContextCompat.getColor(context, R.color.warning_color);
                case "pending":
                    return ContextCompat.getColor(context, R.color.info_color);
                default:
                    return ContextCompat.getColor(context, R.color.role_default);
            }
        }
        
        private String getLastActivityText(String lastActivity) {
            if (lastActivity == null || lastActivity.isEmpty()) {
                return "Unknown";
            }
            
            try {
                // Parse the date string and format it relative to now
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date lastActivityDate = sdf.parse(lastActivity);
                Date now = new Date();
                
                long diffInMillis = now.getTime() - lastActivityDate.getTime();
                long diffInHours = diffInMillis / (1000 * 60 * 60);
                long diffInDays = diffInHours / 24;
                
                if (diffInHours < 1) {
                    return "Now";
                } else if (diffInHours < 24) {
                    return diffInHours + "h ago";
                } else if (diffInDays < 7) {
                    return diffInDays + "d ago";
                } else {
                    SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                    return displayFormat.format(lastActivityDate);
                }
            } catch (Exception e) {
                // Generate mock last activity based on organization ID
                return generateMockLastActivity();
            }
        }
        
        private String generateMockLastActivity() {
            // Generate mock last activity based on organization ID
            long hash = Math.abs(itemView.getContext().toString().hashCode());
            long hoursAgo = hash % 72 + 1; // 1-72 hours ago
            
            if (hoursAgo < 24) {
                return hoursAgo + "h ago";
            } else {
                return (hoursAgo / 24) + "d ago";
            }
        }
    }
}