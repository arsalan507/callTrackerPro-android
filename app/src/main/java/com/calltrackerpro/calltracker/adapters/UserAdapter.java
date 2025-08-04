package com.calltrackerpro.calltracker.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    
    private List<User> users;
    private List<User> filteredUsers;
    private Context context;
    private OnUserClickListener listener;
    
    public interface OnUserClickListener {
        void onUserClick(User user);
        void onUserLongClick(User user);
    }
    
    public UserAdapter(Context context) {
        this.context = context;
        this.users = new ArrayList<>();
        this.filteredUsers = new ArrayList<>();
    }
    
    public void setOnUserClickListener(OnUserClickListener listener) {
        this.listener = listener;
    }
    
    public void setUsers(List<User> users) {
        this.users = users != null ? users : new ArrayList<>();
        this.filteredUsers = new ArrayList<>(this.users);
        notifyDataSetChanged();
    }
    
    public void updateUser(User updatedUser) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(updatedUser.getId())) {
                users.set(i, updatedUser);
                break;
            }
        }
        
        for (int i = 0; i < filteredUsers.size(); i++) {
            if (filteredUsers.get(i).getId().equals(updatedUser.getId())) {
                filteredUsers.set(i, updatedUser);
                notifyItemChanged(i);
                break;
            }
        }
    }
    
    public void filter(String query) {
        filteredUsers.clear();
        
        if (query == null || query.trim().isEmpty()) {
            filteredUsers.addAll(users);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (User user : users) {
                if (user.getFullName().toLowerCase().contains(lowerCaseQuery) ||
                    user.getEmail().toLowerCase().contains(lowerCaseQuery) ||
                    user.getRoleDisplayName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredUsers.add(user);
                }
            }
        }
        
        notifyDataSetChanged();
    }
    
    public void filterByRole(String role) {
        filteredUsers.clear();
        
        if (role == null || role.equals("all")) {
            filteredUsers.addAll(users);
        } else if (role.equals("admin")) {
            // Special case: show both org_admin and super_admin
            for (User user : users) {
                if (user.getRole().equals("org_admin") || user.getRole().equals("super_admin")) {
                    filteredUsers.add(user);
                }
            }
        } else {
            for (User user : users) {
                if (user.getRole().equals(role)) {
                    filteredUsers.add(user);
                }
            }
        }
        
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = filteredUsers.get(position);
        holder.bind(user);
    }
    
    @Override
    public int getItemCount() {
        return filteredUsers.size();
    }
    
    class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserAvatar;
        private TextView tvUserName;
        private TextView tvUserEmail;
        private TextView tvUserRole;
        private TextView tvLoginCount;
        private TextView tvLastActive;
        private View statusIndicator;
        private TextView tvUserStatus;
        
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvUserAvatar = itemView.findViewById(R.id.tvUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            tvLoginCount = itemView.findViewById(R.id.tvLoginCount);
            tvLastActive = itemView.findViewById(R.id.tvLastActive);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            tvUserStatus = itemView.findViewById(R.id.tvUserStatus);
            
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onUserClick(filteredUsers.get(getAdapterPosition()));
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onUserLongClick(filteredUsers.get(getAdapterPosition()));
                    return true;
                }
                return false;
            });
        }
        
        public void bind(User user) {
            // Set avatar with first letter of name
            String avatarText = user.getFullName().substring(0, 1).toUpperCase();
            tvUserAvatar.setText(avatarText);
            setAvatarBackground(tvUserAvatar, user.getRole());
            
            // Set user info
            tvUserName.setText(user.getFullName());
            tvUserEmail.setText(user.getEmail());
            
            // Set role badge
            tvUserRole.setText(user.getRoleDisplayName());
            setRoleBackground(tvUserRole, user.getRole());
            
            // Set login count (placeholder for now)
            tvLoginCount.setText("Logins: " + (user.getId().hashCode() % 50 + 1));
            
            // Set last active time
            String lastActiveText = getLastActiveText(user.getLastLogin());
            tvLastActive.setText("Last: " + lastActiveText);
            
            // Set status indicator
            boolean isActive = user.isActive();
            setStatusIndicator(statusIndicator, tvUserStatus, isActive);
        }
        
        private void setAvatarBackground(TextView avatar, String role) {
            int color = getColorForRole(role);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            avatar.setBackground(drawable);
        }
        
        private void setRoleBackground(TextView roleView, String role) {
            int color = getColorForRole(role);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(12f);
            drawable.setColor(color);
            roleView.setBackground(drawable);
        }
        
        private int getColorForRole(String role) {
            switch (role) {
                case "super_admin":
                    return ContextCompat.getColor(context, R.color.role_super_admin);
                case "org_admin":
                    return ContextCompat.getColor(context, R.color.role_org_admin);
                case "manager":
                    return ContextCompat.getColor(context, R.color.role_manager);
                case "agent":
                    return ContextCompat.getColor(context, R.color.role_agent);
                case "viewer":
                    return ContextCompat.getColor(context, R.color.role_viewer);
                default:
                    return ContextCompat.getColor(context, R.color.role_default);
            }
        }
        
        private void setStatusIndicator(View indicator, TextView statusText, boolean isActive) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            
            if (isActive) {
                drawable.setColor(ContextCompat.getColor(context, R.color.success_green));
                statusText.setText("Active");
                statusText.setTextColor(ContextCompat.getColor(context, R.color.success_green));
            } else {
                drawable.setColor(ContextCompat.getColor(context, R.color.error_red));
                statusText.setText("Inactive");
                statusText.setTextColor(ContextCompat.getColor(context, R.color.error_red));
            }
            
            indicator.setBackground(drawable);
        }
        
        private String getLastActiveText(String lastLogin) {
            if (lastLogin == null || lastLogin.isEmpty()) {
                return "Never";
            }
            
            try {
                // Parse the date string and format it relative to now
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date lastLoginDate = sdf.parse(lastLogin);
                Date now = new Date();
                
                long diffInMillis = now.getTime() - lastLoginDate.getTime();
                long diffInHours = diffInMillis / (1000 * 60 * 60);
                long diffInDays = diffInHours / 24;
                
                if (diffInHours < 1) {
                    return "Just now";
                } else if (diffInHours < 24) {
                    return diffInHours + "h ago";
                } else if (diffInDays < 7) {
                    return diffInDays + "d ago";
                } else {
                    SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                    return displayFormat.format(lastLoginDate);
                }
            } catch (Exception e) {
                return "Unknown";
            }
        }
    }
}