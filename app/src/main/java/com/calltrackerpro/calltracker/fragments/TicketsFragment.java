package com.calltrackerpro.calltracker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.activities.TicketDetailsActivity;
import com.calltrackerpro.calltracker.adapters.TicketsAdapter;
import com.calltrackerpro.calltracker.models.Ticket;
import com.calltrackerpro.calltracker.models.User;
import com.calltrackerpro.calltracker.services.TicketService;
import com.calltrackerpro.calltracker.utils.PermissionManager;
import com.calltrackerpro.calltracker.utils.RealTimeUpdateManager;
import com.calltrackerpro.calltracker.utils.TokenManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class TicketsFragment extends Fragment implements TicketsAdapter.OnTicketClickListener, RealTimeUpdateManager.RealTimeUpdateListener {
    private static final String TAG = "TicketsFragment";
    
    private RecyclerView recyclerView;
    private TicketsAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabCreateTicket;
    private ChipGroup chipGroupFilters;
    private View emptyView;
    
    private TicketService ticketService;
    private TokenManager tokenManager;
    private PermissionManager permissionManager;
    private RealTimeUpdateManager realTimeUpdateManager;
    private User currentUser;
    
    private List<Ticket> ticketsList = new ArrayList<>();
    private String currentStatusFilter = "all";
    private String currentStageFilter = "all";
    private int currentPage = 1;
    private static final int PAGE_SIZE = 20;
    private boolean isLoading = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ticketService = new TicketService(getContext());
        tokenManager = new TokenManager(getContext());
        realTimeUpdateManager = new RealTimeUpdateManager(getContext());
        
        // Get current user from arguments or TokenManager
        currentUser = getCurrentUser();
        if (currentUser != null) {
            permissionManager = new PermissionManager(currentUser);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tickets, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupFilters();
        setupSwipeRefresh();
        setupFab();
        
        loadTickets();
        setupRealTimeUpdates();
        
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_tickets);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_tickets);
        fabCreateTicket = view.findViewById(R.id.fab_create_ticket);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        emptyView = view.findViewById(R.id.layout_empty_tickets);
    }

    private void setupRecyclerView() {
        adapter = new TicketsAdapter(ticketsList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        // Add scroll listener for pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        loadMoreTickets();
                    }
                }
            }
        });
    }

    private void setupFilters() {
        // Status filters
        addFilterChip("All", "all", "status", true);
        addFilterChip("New", "new", "status", false);
        addFilterChip("Contacted", "contacted", "status", false);
        addFilterChip("Qualified", "qualified", "status", false);
        addFilterChip("Converted", "converted", "status", false);
        
        // Stage filters
        addFilterChip("Prospect", "prospect", "stage", false);
        addFilterChip("Proposal", "proposal", "stage", false);
        addFilterChip("Negotiation", "negotiation", "stage", false);
        addFilterChip("Closed Won", "closed-won", "stage", false);
        addFilterChip("Closed Lost", "closed-lost", "stage", false);
    }

    private void addFilterChip(String label, String value, String type, boolean isSelected) {
        Chip chip = new Chip(getContext());
        chip.setText(label);
        chip.setCheckable(true);
        chip.setChecked(isSelected);
        chip.setTag(type + ":" + value);
        
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Uncheck other chips of the same type
                for (int i = 0; i < chipGroupFilters.getChildCount(); i++) {
                    View child = chipGroupFilters.getChildAt(i);
                    if (child instanceof Chip) {
                        Chip otherChip = (Chip) child;
                        String otherTag = (String) otherChip.getTag();
                        if (otherTag != null && otherTag.startsWith(type + ":") && otherChip != chip) {
                            otherChip.setChecked(false);
                        }
                    }
                }
                
                // Update filter
                if (type.equals("status")) {
                    currentStatusFilter = value;
                } else if (type.equals("stage")) {
                    currentStageFilter = value;
                }
                
                // Reload tickets with new filter
                currentPage = 1;
                ticketsList.clear();
                loadTickets();
            }
        });
        
        chipGroupFilters.addView(chip);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 1;
            ticketsList.clear();
            loadTickets();
        });
    }

    private void setupFab() {
        // Show FAB only if user has permission to create tickets
        if (permissionManager != null && permissionManager.canCreateContacts()) {
            fabCreateTicket.setVisibility(View.VISIBLE);
            fabCreateTicket.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), TicketDetailsActivity.class);
                intent.putExtra("mode", "create");
                startActivity(intent);
            });
        } else {
            fabCreateTicket.setVisibility(View.GONE);
        }
    }

    private void loadTickets() {
        if (isLoading) return;
        
        isLoading = true;
        swipeRefreshLayout.setRefreshing(true);
        
        String organizationId = currentUser != null ? currentUser.getOrganizationId() : null;
        String teamId = null;
        String assignedAgent = null;
        
        // Apply role-based filtering
        if (currentUser != null) {
            if (currentUser.isAgent()) {
                // Agents see only their own tickets
                assignedAgent = currentUser.getId();
            } else if (currentUser.isManager()) {
                // Managers see their team's tickets
                teamId = currentUser.getTeamIds() != null && !currentUser.getTeamIds().isEmpty() 
                        ? currentUser.getTeamIds().get(0) : null;
            }
            // Org admins see all tickets (no additional filtering)
        }
        
        String status = "all".equals(currentStatusFilter) ? null : currentStatusFilter;
        String stage = "all".equals(currentStageFilter) ? null : currentStageFilter;
        
        ticketService.getTickets(organizationId, teamId, assignedAgent, status, null, null, null, 
                currentPage, PAGE_SIZE, new TicketService.TicketCallback<List<Ticket>>() {
            @Override
            public void onSuccess(List<Ticket> tickets) {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
                
                if (currentPage == 1) {
                    ticketsList.clear();
                }
                
                ticketsList.addAll(tickets);
                adapter.notifyDataSetChanged();
                
                updateEmptyView();
                
                Log.d(TAG, "Loaded " + tickets.size() + " tickets for page " + currentPage);
            }
            
            @Override
            public void onError(String error) {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
                
                Log.e(TAG, "Error loading tickets: " + error);
                Toast.makeText(getContext(), "Error loading tickets: " + error, Toast.LENGTH_SHORT).show();
                
                updateEmptyView();
            }
        });
    }

    private void loadMoreTickets() {
        currentPage++;
        loadTickets();
    }

    private void updateEmptyView() {
        if (ticketsList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTicketClick(Ticket ticket) {
        Intent intent = new Intent(getContext(), TicketDetailsActivity.class);
        intent.putExtra("ticketId", ticket.getId());
        intent.putExtra("mode", "view");
        startActivity(intent);
    }

    @Override
    public void onTicketLongClick(Ticket ticket) {
        // Show context menu for ticket actions
        showTicketContextMenu(ticket);
    }

    private void showTicketContextMenu(Ticket ticket) {
        // TODO: Implement context menu with options like:
        // - Edit ticket
        // - Assign to agent
        // - Change status
        // - Add note
        // - Delete (if permitted)
    }

    private User getCurrentUser() {
        // TODO: Get current user from TokenManager or shared preferences
        // This should be implemented based on your authentication system
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh tickets when fragment becomes visible
        if (adapter != null) {
            currentPage = 1;
            ticketsList.clear();
            loadTickets();
        }
    }

    public void refreshTickets() {
        currentPage = 1;
        ticketsList.clear();
        loadTickets();
    }

    // Method to apply external filters (can be called from parent activity)
    public void applyFilter(String filterType, String filterValue) {
        if ("status".equals(filterType)) {
            currentStatusFilter = filterValue;
        } else if ("stage".equals(filterType)) {
            currentStageFilter = filterValue;
        }
        
        // Update chip selection
        updateChipSelection(filterType, filterValue);
        
        // Reload tickets
        currentPage = 1;
        ticketsList.clear();
        loadTickets();
    }

    private void updateChipSelection(String filterType, String filterValue) {
        for (int i = 0; i < chipGroupFilters.getChildCount(); i++) {
            View child = chipGroupFilters.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                String tag = (String) chip.getTag();
                if (tag != null && tag.equals(filterType + ":" + filterValue)) {
                    chip.setChecked(true);
                } else if (tag != null && tag.startsWith(filterType + ":")) {
                    chip.setChecked(false);
                }
            }
        }
    }

    // Real-time update methods
    private void setupRealTimeUpdates() {
        if (realTimeUpdateManager != null) {
            realTimeUpdateManager.addListener(this);
            realTimeUpdateManager.startRealTimeUpdates();
        }
    }

    // RealTimeUpdateManager.RealTimeUpdateListener implementation
    @Override
    public void onTicketUpdate(Ticket updatedTicket) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // Find and update the ticket in the list
                for (int i = 0; i < ticketsList.size(); i++) {
                    if (ticketsList.get(i).getId().equals(updatedTicket.getId())) {
                        ticketsList.set(i, updatedTicket);
                        adapter.notifyItemChanged(i);
                        Log.d(TAG, "Updated ticket in list: " + updatedTicket.getTicketId());
                        break;
                    }
                }
            });
        }
    }

    @Override
    public void onTicketCreated(Ticket newTicket) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // Add new ticket to the top of the list if it matches current filters
                if (ticketMatchesCurrentFilters(newTicket)) {
                    ticketsList.add(0, newTicket);
                    adapter.notifyItemInserted(0);
                    recyclerView.scrollToPosition(0);
                    updateEmptyView();
                    Log.d(TAG, "Added new ticket to list: " + newTicket.getTicketId());
                }
            });
        }
    }

    @Override
    public void onTicketAssigned(Ticket assignedTicket, String previousAssignee) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // Update the ticket if it's in the current list
                onTicketUpdate(assignedTicket);
                
                // Show toast if assigned to current user
                if (currentUser != null && currentUser.getId().equals(assignedTicket.getAssignedTo())) {
                    Toast.makeText(getContext(), "You have been assigned to ticket: " + assignedTicket.getDisplayName(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onTicketStatusChanged(Ticket statusChangedTicket, String previousStatus) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // Check if ticket still matches current filters after status change
                if (ticketMatchesCurrentFilters(statusChangedTicket)) {
                    onTicketUpdate(statusChangedTicket);
                } else {
                    // Remove ticket from list if it no longer matches filters
                    for (int i = 0; i < ticketsList.size(); i++) {
                        if (ticketsList.get(i).getId().equals(statusChangedTicket.getId())) {
                            ticketsList.remove(i);
                            adapter.notifyItemRemoved(i);
                            updateEmptyView();
                            Log.d(TAG, "Removed ticket from list due to status change: " + statusChangedTicket.getTicketId());
                            break;
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onTicketEscalated(Ticket escalatedTicket) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                onTicketUpdate(escalatedTicket);
                Toast.makeText(getContext(), "Ticket escalated: " + escalatedTicket.getDisplayName(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onConnectionStatusChanged(boolean connected) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // You can add a connection status indicator in the UI if needed
                Log.d(TAG, "Real-time connection status: " + (connected ? "Connected" : "Disconnected"));
            });
        }
    }

    private boolean ticketMatchesCurrentFilters(Ticket ticket) {
        // Check if ticket matches current status filter
        if (!"all".equals(currentStatusFilter)) {
            if (!currentStatusFilter.equals(ticket.getLeadStatus())) {
                return false;
            }
        }

        // Check if ticket matches current stage filter
        if (!"all".equals(currentStageFilter)) {
            if (!currentStageFilter.equals(ticket.getStage())) {
                return false;
            }
        }

        // Check role-based visibility
        if (currentUser != null) {
            if (currentUser.isAgent()) {
                // Agents should only see their own tickets
                return currentUser.getId().equals(ticket.getAssignedTo());
            } else if (currentUser.isManager()) {
                // Managers should see their team's tickets
                return currentUser.getTeamIds() != null && 
                       currentUser.getTeamIds().contains(ticket.getTeamId());
            }
            // Org admins see all tickets
        }

        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (realTimeUpdateManager != null) {
            realTimeUpdateManager.removeListener(this);
            realTimeUpdateManager.stopRealTimeUpdates();
        }
    }
}