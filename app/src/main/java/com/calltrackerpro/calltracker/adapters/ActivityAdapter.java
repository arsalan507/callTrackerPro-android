package com.calltrackerpro.calltracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.models.DashboardStats;
import java.util.ArrayList;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {
    private final Context context;
    private List<DashboardStats.ActivityItem> activities;
    private OnItemClickListener onItemClickListener;
    
    public interface OnItemClickListener {
        void onItemClick(DashboardStats.ActivityItem activity);
    }
    
    public ActivityAdapter(Context context) {
        this.context = context;
        this.activities = new ArrayList<>();
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    
    public void updateActivities(List<DashboardStats.ActivityItem> newActivities) {
        this.activities.clear();
        if (newActivities != null) {
            this.activities.addAll(newActivities);
        }
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        DashboardStats.ActivityItem activity = activities.get(position);
        holder.bind(activity);
    }
    
    @Override
    public int getItemCount() {
        return activities.size();
    }
    
    class ActivityViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconImageView;
        private final TextView titleTextView;
        private final TextView descriptionTextView;
        private final TextView timeTextView;
        private final TextView userNameTextView;
        private final View priorityIndicator;
        private final TextView statusTextView;
        
        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            
            iconImageView = itemView.findViewById(R.id.iv_activity_icon);
            titleTextView = itemView.findViewById(R.id.tv_activity_title);
            descriptionTextView = itemView.findViewById(R.id.tv_activity_description);
            timeTextView = itemView.findViewById(R.id.tv_activity_time);
            userNameTextView = itemView.findViewById(R.id.tv_activity_user);
            priorityIndicator = itemView.findViewById(R.id.view_priority_indicator);
            statusTextView = itemView.findViewById(R.id.tv_activity_status);
            
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(activities.get(getAdapterPosition()));
                }
            });
        }
        
        public void bind(DashboardStats.ActivityItem activity) {
            // Set title and description
            titleTextView.setText(activity.getTitle() != null ? activity.getTitle() : "Activity");
            descriptionTextView.setText(activity.getDescription() != null ? activity.getDescription() : "");
            
            // Set time
            timeTextView.setText(activity.getDisplayTime());
            
            // Set user name if available
            if (activity.getUserName() != null && !activity.getUserName().isEmpty()) {
                userNameTextView.setText(activity.getUserName());
                userNameTextView.setVisibility(View.VISIBLE);
            } else {
                userNameTextView.setVisibility(View.GONE);
            }
            
            // Set status if available
            if (activity.getStatus() != null && !activity.getStatus().isEmpty()) {
                statusTextView.setText(activity.getStatus());
                statusTextView.setVisibility(View.VISIBLE);
                setStatusStyle(statusTextView, activity.getStatus());
            } else {
                statusTextView.setVisibility(View.GONE);
            }
            
            // Set icon based on activity type
            setActivityIcon(iconImageView, activity.getType());
            
            // Set priority indicator
            setPriorityIndicator(priorityIndicator, activity.getPriority());
            
            // Show/hide description based on content
            if (activity.getDescription() == null || activity.getDescription().isEmpty()) {
                descriptionTextView.setVisibility(View.GONE);
            } else {
                descriptionTextView.setVisibility(View.VISIBLE);
            }
        }
        
        private void setActivityIcon(ImageView iconView, String activityType) {
            int iconRes;
            int tintColor;
            
            switch (activityType) {
                case "ticket_created":
                    iconRes = R.drawable.ic_ticket_add;
                    tintColor = ContextCompat.getColor(context, R.color.success_color);
                    break;
                case "ticket_updated":
                    iconRes = R.drawable.ic_update;
                    tintColor = ContextCompat.getColor(context, R.color.warning_color);
                    break;
                case "ticket_assigned":
                    iconRes = R.drawable.ic_person;
                    tintColor = ContextCompat.getColor(context, R.color.info_color);
                    break;
                case "call_completed":
                    iconRes = R.drawable.ic_phone;
                    tintColor = ContextCompat.getColor(context, R.color.success_color);
                    break;
                case "call_started":
                    iconRes = R.drawable.ic_call_outgoing;
                    tintColor = ContextCompat.getColor(context, R.color.primary_color);
                    break;
                case "user_login":
                    iconRes = R.drawable.ic_person;
                    tintColor = ContextCompat.getColor(context, R.color.success_color);
                    break;
                case "user_logout":
                    iconRes = R.drawable.ic_person;
                    tintColor = ContextCompat.getColor(context, R.color.secondary_color);
                    break;
                case "organization_updated":
                    iconRes = R.drawable.ic_business_white;
                    tintColor = ContextCompat.getColor(context, R.color.info_color);
                    break;
                default:
                    iconRes = R.drawable.ic_notification;
                    tintColor = ContextCompat.getColor(context, R.color.primary_color);
                    break;
            }
            
            iconView.setImageResource(iconRes);
            iconView.setColorFilter(tintColor);
        }
        
        private void setPriorityIndicator(View indicator, String priority) {
            if (priority == null || priority.isEmpty()) {
                indicator.setVisibility(View.GONE);
                return;
            }
            
            indicator.setVisibility(View.VISIBLE);
            int color;
            
            switch (priority.toLowerCase()) {
                case "high":
                case "urgent":
                    color = ContextCompat.getColor(context, R.color.error_color);
                    break;
                case "medium":
                    color = ContextCompat.getColor(context, R.color.warning_color);
                    break;
                case "low":
                    color = ContextCompat.getColor(context, R.color.success_color);
                    break;
                default:
                    color = ContextCompat.getColor(context, R.color.secondary_color);
                    break;
            }
            
            indicator.setBackgroundColor(color);
        }
        
        private void setStatusStyle(TextView statusView, String status) {
            int backgroundColor;
            int textColor = ContextCompat.getColor(context, android.R.color.white);
            
            switch (status.toLowerCase()) {
                case "open":
                case "new":
                    backgroundColor = ContextCompat.getColor(context, R.color.info_color);
                    break;
                case "in_progress":
                case "assigned":
                    backgroundColor = ContextCompat.getColor(context, R.color.warning_color);
                    break;
                case "resolved":
                case "completed":
                    backgroundColor = ContextCompat.getColor(context, R.color.success_color);
                    break;
                case "closed":
                    backgroundColor = ContextCompat.getColor(context, R.color.secondary_color);
                    break;
                case "escalated":
                    backgroundColor = ContextCompat.getColor(context, R.color.error_color);
                    break;
                default:
                    backgroundColor = ContextCompat.getColor(context, R.color.primary_color);
                    break;
            }
            
            statusView.setBackgroundColor(backgroundColor);
            statusView.setTextColor(textColor);
            
            // Add padding to status view
            int paddingHorizontal = context.getResources().getDimensionPixelSize(R.dimen.status_padding_horizontal);
            int paddingVertical = context.getResources().getDimensionPixelSize(R.dimen.status_padding_vertical);
            statusView.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
        }
    }
}