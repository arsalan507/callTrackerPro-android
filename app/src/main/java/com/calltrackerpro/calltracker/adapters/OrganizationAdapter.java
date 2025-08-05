package com.calltrackerpro.calltracker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.models.Organization;
import java.util.ArrayList;
import java.util.List;

public class OrganizationAdapter extends RecyclerView.Adapter<OrganizationAdapter.OrganizationViewHolder> {
    
    private List<Organization> organizations;
    private OnOrganizationClickListener listener;
    
    public interface OnOrganizationClickListener {
        void onOrganizationClick(Organization organization);
    }
    
    public OrganizationAdapter(OnOrganizationClickListener listener) {
        this.listener = listener;
        this.organizations = new ArrayList<>();
    }
    
    public void setOrganizations(List<Organization> organizations) {
        this.organizations = organizations != null ? organizations : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public OrganizationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_organization, parent, false);
        return new OrganizationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull OrganizationViewHolder holder, int position) {
        Organization organization = organizations.get(position);
        holder.bind(organization, listener);
    }
    
    @Override
    public int getItemCount() {
        return organizations.size();
    }
    
    static class OrganizationViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView domainTextView;
        private TextView subscriptionTextView;
        private TextView statusTextView;
        
        public OrganizationViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tvOrganizationName);
            domainTextView = itemView.findViewById(R.id.tvOrganizationDomain);
            subscriptionTextView = itemView.findViewById(R.id.tvSubscriptionPlan);
            statusTextView = itemView.findViewById(R.id.tvSubscriptionStatus);
        }
        
        public void bind(Organization organization, OnOrganizationClickListener listener) {
            String orgName = organization.getName() != null ? organization.getName() : "Unknown Organization";
            nameTextView.setText(orgName);
            
            if (organization.getDomain() != null && !organization.getDomain().isEmpty()) {
                domainTextView.setText(organization.getDomain());
                domainTextView.setVisibility(View.VISIBLE);
            } else {
                domainTextView.setVisibility(View.GONE);
            }
            
            if (organization.getSubscription() != null) {
                Organization.Subscription subscription = organization.getSubscription();
                
                // Set subscription plan
                String planText = "Plan: " + (subscription.getPlan() != null ? subscription.getPlan() : "Unknown");
                subscriptionTextView.setText(planText);
                subscriptionTextView.setVisibility(View.VISIBLE);
                
                // Set subscription status
                String statusText = "Status: " + (subscription.getStatus() != null ? subscription.getStatus() : "Unknown");
                statusTextView.setText(statusText);
                statusTextView.setVisibility(View.VISIBLE);
                
                // Set status color
                if (subscription.isActive()) {
                    statusTextView.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                } else {
                    statusTextView.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                }
            } else {
                subscriptionTextView.setVisibility(View.GONE);
                statusTextView.setVisibility(View.GONE);
            }
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrganizationClick(organization);
                }
            });
        }
    }
}