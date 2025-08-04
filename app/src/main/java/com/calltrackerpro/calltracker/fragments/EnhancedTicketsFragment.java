package com.calltrackerpro.calltracker.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.activities.TicketDetailsActivity;
import com.calltrackerpro.calltracker.activities.UnifiedDashboardActivity;
import com.calltrackerpro.calltracker.adapters.EnhancedTicketAdapter;
import com.calltrackerpro.calltracker.models.ApiResponse;
import com.calltrackerpro.calltracker.models.Ticket;
import com.calltrackerpro.calltracker.models.TicketNote;
import com.calltrackerpro.calltracker.models.User;
import com.calltrackerpro.calltracker.services.ApiService;
import com.calltrackerpro.calltracker.utils.PermissionManager;
import com.calltrackerpro.calltracker.utils.TokenManager;
import com.calltrackerpro.calltracker.utils.WebSocketManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class EnhancedTicketsFragment extends Fragment implements UnifiedDashboardActivity.RefreshableFragment {
    
    private static final String TAG = "EnhancedTickets";
    
    // UI Components
    private TextView tvTicketStats;
    private ImageView btnTicketSearch;
    private TextInputLayout layoutSearch;
    private TextInputEditText etSearchTickets;
    private TabLayout tabLayoutFilters;
    private LinearLayout layoutAdvancedFilters;
    private ChipGroup chipGroupStatus;
    private ChipGroup chipGroupPriority;
    private SwipeRefreshLayout swipeRefreshTickets;
    private RecyclerView recyclerTickets;
    private LinearLayout layoutEmpty;
    private LinearLayout layoutLoading;
    private MaterialButton btnCreateFirstTicket;
    private FloatingActionButton fabCreateTicket;
    private FloatingActionButton fabFilterMenu;
    
    // Adapter and Data
    private EnhancedTicketAdapter ticketAdapter;
    
    // Services
    private ApiService apiService;
    private TokenManager tokenManager;
    private PermissionManager permissionManager;
    private User currentUser;
    private WebSocketManager webSocketManager;
    
    // Filter State
    private String currentStatusFilter = "all";
    private String currentPriorityFilter = "all";
    private String currentTabFilter = "all";
    private boolean isAdvancedFiltersVisible = false;
    private boolean isSearchVisible = false;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tickets_enhanced, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeComponents(view);
        setupServices();
        setupRecyclerView();
        setupTabs();
        setupSearch();
        setupFilters();
        setupFloatingActionButtons();
        
        // Load tickets for the first time
        loadTickets();
    }
    
    private void initializeComponents(View view) {
        tvTicketStats = view.findViewById(R.id.tvTicketStats);
        btnTicketSearch = view.findViewById(R.id.btnTicketSearch);
        layoutSearch = view.findViewById(R.id.layoutSearch);
        etSearchTickets = view.findViewById(R.id.etSearchTickets);
        tabLayoutFilters = view.findViewById(R.id.tabLayoutFilters);
        layoutAdvancedFilters = view.findViewById(R.id.layoutAdvancedFilters);
        chipGroupStatus = view.findViewById(R.id.chipGroupStatus);
        chipGroupPriority = view.findViewById(R.id.chipGroupPriority);
        swipeRefreshTickets = view.findViewById(R.id.swipeRefreshTickets);
        recyclerTickets = view.findViewById(R.id.recyclerTickets);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        layoutLoading = view.findViewById(R.id.layoutLoading);
        btnCreateFirstTicket = view.findViewById(R.id.btnCreateFirstTicket);
        fabCreateTicket = view.findViewById(R.id.fabCreateTicket);
        fabFilterMenu = view.findViewById(R.id.fabFilterMenu);
    }
    
    private void setupServices() {
        apiService = ApiService.getInstance();
        tokenManager = new TokenManager(requireContext());
        currentUser = tokenManager.getUser();
        webSocketManager = WebSocketManager.getInstance(requireContext());
        
        if (currentUser != null) {
            permissionManager = new PermissionManager(currentUser);
        }
        
        setupRealTimeUpdates();
    }
    
    private void setupRecyclerView() {
        ticketAdapter = new EnhancedTicketAdapter(requireContext());
        recyclerTickets.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerTickets.setAdapter(ticketAdapter);
        
        ticketAdapter.setOnTicketClickListener(new EnhancedTicketAdapter.OnTicketClickListener() {
            @Override
            public void onTicketClick(Ticket ticket) {
                openTicketDetails(ticket);
            }
            
            @Override
            public void onTicketLongClick(Ticket ticket) {
                showTicketQuickActions(ticket);
            }
            
            @Override
            public void onTicketMenuClick(Ticket ticket, View anchorView) {
                showTicketMenu(ticket, anchorView);
            }
        });
        
        // Setup swipe refresh
        swipeRefreshTickets.setOnRefreshListener(this::loadTickets);
        swipeRefreshTickets.setColorSchemeResources(
            R.color.primary_color,
            R.color.secondary_color,
            R.color.info_color
        );
    }
    
    private void setupTabs() {
        tabLayoutFilters.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: // All
                        currentTabFilter = "all";
                        break;
                    case 1: // My Tickets
                        currentTabFilter = "my_tickets";
                        break;
                    case 2: // Open
                        currentTabFilter = "open";
                        break;
                    case 3: // In Progress
                        currentTabFilter = "in_progress";
                        break;
                    case 4: // High Priority
                        currentTabFilter = "high_priority";
                        break;
                    case 5: // Overdue
                        currentTabFilter = "overdue";
                        break;
                }
                applyTabFilter();
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void setupSearch() {
        btnTicketSearch.setOnClickListener(v -> toggleSearch());
        
        etSearchTickets.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (ticketAdapter != null) {
                    ticketAdapter.filter(s.toString());
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void setupFilters() {
        // Status filter chips
        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipStatusAll) {
                currentStatusFilter = "all";
            } else if (checkedId == R.id.chipStatusOpen) {
                currentStatusFilter = "open";
            } else if (checkedId == R.id.chipStatusInProgress) {
                currentStatusFilter = "in_progress";
            } else if (checkedId == R.id.chipStatusResolved) {
                currentStatusFilter = "resolved";
            }
            
            applyFilters();
        });
        
        // Priority filter chips
        chipGroupPriority.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipPriorityAll) {
                currentPriorityFilter = "all";
            } else if (checkedId == R.id.chipPriorityUrgent) {
                currentPriorityFilter = "urgent";
            } else if (checkedId == R.id.chipPriorityHigh) {
                currentPriorityFilter = "high";
            } else if (checkedId == R.id.chipPriorityMedium) {
                currentPriorityFilter = "medium";
            } else if (checkedId == R.id.chipPriorityLow) {
                currentPriorityFilter = "low";
            }
            
            applyFilters();
        });
    }
    
    private void setupFloatingActionButtons() {
        // Create ticket FAB
        if (permissionManager != null && permissionManager.canCreateContacts()) {
            fabCreateTicket.setVisibility(View.VISIBLE);
            fabCreateTicket.setOnClickListener(v -> createNewTicket());
            btnCreateFirstTicket.setOnClickListener(v -> createNewTicket());
        } else {
            fabCreateTicket.setVisibility(View.GONE);
            btnCreateFirstTicket.setVisibility(View.GONE);
        }
        
        // Filter menu FAB
        fabFilterMenu.setOnClickListener(v -> toggleAdvancedFilters());
    }
    
    private void loadTickets() {
        if (currentUser == null) {
            showError("User not authenticated");
            return;
        }
        
        showLoading(true);
        
        String authToken = "Bearer " + tokenManager.getToken();
        String organizationId = currentUser.getOrganizationId();
        
        // Apply role-based filtering
        String teamId = null;
        String assignedTo = null;
        
        if (currentUser.isAgent()) {
            assignedTo = currentUser.getId(); // Agents see only their tickets
        } else if (currentUser.isManager() && currentUser.getTeamIds() != null && !currentUser.getTeamIds().isEmpty()) {
            teamId = currentUser.getTeamIds().get(0); // Managers see team tickets
        }
        // Org admins and super admins see all tickets
        
        Call<ApiResponse<List<Ticket>>> call = apiService.getTickets(
            authToken, organizationId, teamId, assignedTo, 
            currentStatusFilter.equals("all") ? null : currentStatusFilter,
            null, // category
            currentPriorityFilter.equals("all") ? null : currentPriorityFilter,
            null, // slaStatus
            1, 100 // page, limit
        );
        
        call.enqueue(new Callback<ApiResponse<List<Ticket>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Ticket>>> call, Response<ApiResponse<List<Ticket>>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Ticket>> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Ticket> tickets = apiResponse.getData();
                        ticketAdapter.setTickets(tickets);
                        updateTicketStats(tickets);
                        updateEmptyState();
                        Log.d(TAG, "Loaded " + tickets.size() + " tickets");
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? 
                            apiResponse.getMessage() : "Failed to load tickets";
                        showError(errorMsg);
                    }
                } else {
                    showError("Error: " + response.code() + " - " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<Ticket>>> call, Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
                Log.e(TAG, "Network error loading tickets", t);
            }
        });
    }
    
    private void applyTabFilter() {
        // Apply additional filtering based on tab selection
        loadTickets(); // For now, reload from server with tab filter
    }
    
    private void applyFilters() {
        if (ticketAdapter != null) {
            ticketAdapter.filterByStatus(currentStatusFilter);
            ticketAdapter.filterByPriority(currentPriorityFilter);
        }
    }
    
    private void toggleSearch() {
        isSearchVisible = !isSearchVisible;
        layoutSearch.setVisibility(isSearchVisible ? View.VISIBLE : View.GONE);
        
        if (isSearchVisible) {
            etSearchTickets.requestFocus();
        } else {
            etSearchTickets.setText("");
        }
    }
    
    private void toggleAdvancedFilters() {
        isAdvancedFiltersVisible = !isAdvancedFiltersVisible;
        layoutAdvancedFilters.setVisibility(isAdvancedFiltersVisible ? View.VISIBLE : View.GONE);
    }
    
    private void createNewTicket() {
        Intent intent = new Intent(requireContext(), TicketDetailsActivity.class);
        intent.putExtra("mode", "create");
        startActivity(intent);
    }
    
    private void openTicketDetails(Ticket ticket) {
        Intent intent = new Intent(requireContext(), TicketDetailsActivity.class);
        intent.putExtra("ticketId", ticket.getId());
        intent.putExtra("mode", "view");
        startActivity(intent);
    }
    
    private void showTicketQuickActions(Ticket ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Quick Actions");
        
        String[] actions = {"View Details", "Edit Ticket", "Assign to Me", "Change Status", "Add Note"};
        
        builder.setItems(actions, (dialog, which) -> {
            switch (which) {
                case 0:
                    openTicketDetails(ticket);
                    break;
                case 1:
                    editTicket(ticket);
                    break;
                case 2:
                    assignTicketToMe(ticket);
                    break;
                case 3:
                    showStatusChangeDialog(ticket);
                    break;
                case 4:
                    addNoteToTicket(ticket);
                    break;
            }
        });
        
        builder.show();
    }
    
    private void showTicketMenu(Ticket ticket, View anchorView) {
        PopupMenu popup = new PopupMenu(requireContext(), anchorView);
        popup.getMenuInflater().inflate(R.menu.menu_ticket_actions, popup.getMenu());
        
        // Show/hide menu items based on permissions and ticket state
        if (!permissionManager.canManageUsers()) {
            popup.getMenu().findItem(R.id.action_assign_ticket).setVisible(false);
        }
        
        if (ticket.getAssignedTo() != null && ticket.getAssignedTo().equals(currentUser.getId())) {
            popup.getMenu().findItem(R.id.action_assign_to_me).setVisible(false);
        }
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.action_view_details) {
                openTicketDetails(ticket);
                return true;
            } else if (itemId == R.id.action_edit_ticket) {
                editTicket(ticket);
                return true;
            } else if (itemId == R.id.action_assign_ticket) {
                showAssignmentDialog(ticket);
                return true;
            } else if (itemId == R.id.action_assign_to_me) {
                assignTicketToMe(ticket);
                return true;
            } else if (itemId == R.id.action_change_status) {
                showStatusChangeDialog(ticket);
                return true;
            } else if (itemId == R.id.action_add_note) {
                addNoteToTicket(ticket);
                return true;
            } else if (itemId == R.id.action_escalate) {
                escalateTicket(ticket);
                return true;
            }
            
            return false;
        });
        
        popup.show();
    }
    
    private void editTicket(Ticket ticket) {
        Intent intent = new Intent(requireContext(), TicketDetailsActivity.class);
        intent.putExtra("ticketId", ticket.getId());
        intent.putExtra("mode", "edit");
        startActivity(intent);
    }
    
    private void assignTicketToMe(Ticket ticket) {
        if (currentUser == null) {
            showError("Current user not available");
            return;
        }
        
        String authToken = "Bearer " + tokenManager.getToken();
        
        ApiService.AssignTicketRequest request = new ApiService.AssignTicketRequest(currentUser.getId());
        Call<ApiResponse<Ticket>> call = apiService.assignTicket(
            authToken, ticket.getId(), request
        );
        
        call.enqueue(new Callback<ApiResponse<Ticket>>() {
            @Override
            public void onResponse(Call<ApiResponse<Ticket>> call, Response<ApiResponse<Ticket>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Ticket> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Ticket updatedTicket = apiResponse.getData();
                        ticketAdapter.updateTicket(updatedTicket);
                        Toast.makeText(requireContext(), "Ticket assigned to you successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        showError(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to assign ticket");
                    }
                } else {
                    showError("Error: " + response.code() + " - " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Ticket>> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void showAssignmentDialog(Ticket ticket) {
        String authToken = "Bearer " + tokenManager.getToken();
        
        // Get available agents from the API
        Call<ApiResponse<List<User>>> call = apiService.getOrganizationUsers(authToken, currentUser.getOrganizationId());
        
        call.enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<User>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<User> users = apiResponse.getData();
                        
                        // Filter to agents and managers only
                        List<User> availableUsers = new ArrayList<>();
                        for (User user : users) {
                            if (user.isAgent() || user.isManager()) {
                                availableUsers.add(user);
                            }
                        }
                        
                        showUserSelectionDialog(ticket, availableUsers);
                    } else {
                        showError("Failed to load users");
                    }
                } else {
                    showError("Error loading users: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void showUserSelectionDialog(Ticket ticket, List<User> users) {
        String[] userNames = new String[users.size() + 1];
        userNames[0] = "Unassigned";
        
        for (int i = 0; i < users.size(); i++) {
            userNames[i + 1] = users.get(i).getFullName() + " (" + users.get(i).getRole() + ")";
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Assign Ticket To");
        builder.setItems(userNames, (dialog, which) -> {
            if (which == 0) {
                // Unassign ticket
                assignTicketToUser(ticket, null);
            } else {
                // Assign to selected user
                User selectedUser = users.get(which - 1);
                assignTicketToUser(ticket, selectedUser.getId());
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void assignTicketToUser(Ticket ticket, String userId) {
        String authToken = "Bearer " + tokenManager.getToken();
        
        ApiService.AssignTicketRequest request = new ApiService.AssignTicketRequest(userId);
        Call<ApiResponse<Ticket>> call = apiService.assignTicket(authToken, ticket.getId(), request);
        
        call.enqueue(new Callback<ApiResponse<Ticket>>() {
            @Override
            public void onResponse(Call<ApiResponse<Ticket>> call, Response<ApiResponse<Ticket>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Ticket> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Ticket updatedTicket = apiResponse.getData();
                        ticketAdapter.updateTicket(updatedTicket);
                        
                        String message = userId != null ? "Ticket assigned successfully" : "Ticket unassigned successfully";
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        showError(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to assign ticket");
                    }
                } else {
                    showError("Error: " + response.code() + " - " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Ticket>> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void showStatusChangeDialog(Ticket ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Change Status");
        
        String[] statuses = {"Open", "In Progress", "Resolved", "Closed"};
        
        builder.setItems(statuses, (dialog, which) -> {
            String newStatus = statuses[which].toLowerCase().replace(" ", "_");
            updateTicketStatus(ticket, newStatus);
        });
        
        builder.show();
    }
    
    private void updateTicketStatus(Ticket ticket, String newStatus) {
        String authToken = "Bearer " + tokenManager.getToken();
        
        ApiService.UpdateTicketStatusRequest request = new ApiService.UpdateTicketStatusRequest(newStatus);
        Call<ApiResponse<Ticket>> call = apiService.updateTicketStatus(authToken, ticket.getId(), request);
        
        call.enqueue(new Callback<ApiResponse<Ticket>>() {
            @Override
            public void onResponse(Call<ApiResponse<Ticket>> call, Response<ApiResponse<Ticket>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Ticket> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Ticket updatedTicket = apiResponse.getData();
                        ticketAdapter.updateTicket(updatedTicket);
                        Toast.makeText(requireContext(), "Status updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        showError(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to update status");
                    }
                } else {
                    showError("Error: " + response.code() + " - " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Ticket>> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void addNoteToTicket(Ticket ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Note to Ticket");
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_note, null);
        TextInputEditText etNote = dialogView.findViewById(R.id.etNote);
        CheckBox cbPrivate = dialogView.findViewById(R.id.cbPrivate);
        
        builder.setView(dialogView);
        builder.setPositiveButton("Add Note", (dialog, which) -> {
            String noteText = etNote.getText().toString().trim();
            if (!noteText.isEmpty()) {
                addNoteToTicketAPI(ticket, noteText, cbPrivate.isChecked());
            } else {
                Toast.makeText(requireContext(), "Please enter a note", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void addNoteToTicketAPI(Ticket ticket, String noteText, boolean isPrivate) {
        String authToken = "Bearer " + tokenManager.getToken();
        
        // Create note object
        TicketNote note = new TicketNote(noteText, currentUser.getId(), isPrivate);
        note.setAuthorName(currentUser.getFullName());
        note.setTimestamp(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(new java.util.Date()));
        
        Call<ApiResponse<TicketNote>> call = apiService.addTicketNote(authToken, ticket.getId(), note);
        
        call.enqueue(new Callback<ApiResponse<TicketNote>>() {
            @Override
            public void onResponse(Call<ApiResponse<TicketNote>> call, Response<ApiResponse<TicketNote>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<TicketNote> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(requireContext(), "Note added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        showError(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to add note");
                    }
                } else {
                    showError("Error: " + response.code() + " - " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<TicketNote>> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }
    
    private void escalateTicket(Ticket ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Escalate Ticket");
        builder.setMessage("Are you sure you want to escalate this ticket? This will notify the manager and increase its priority.");
        
        builder.setPositiveButton("Escalate", (dialog, which) -> {
            String authToken = "Bearer " + tokenManager.getToken();
            
            // Create escalation request with auto-assignment (system will assign to appropriate manager)
            String escalationReason = "Escalated due to high priority or complexity";
            String escalatedTo = "auto-assign"; // System will assign to organization admin or team lead
            
            ApiService.EscalateTicketRequest request = new ApiService.EscalateTicketRequest(
                escalatedTo, 
                escalationReason,
                "high" // Increase priority to high
            );
            
            Call<ApiResponse<Ticket>> call = apiService.escalateTicket(authToken, ticket.getId(), request);
            
            call.enqueue(new Callback<ApiResponse<Ticket>>() {
                @Override
                public void onResponse(Call<ApiResponse<Ticket>> call, Response<ApiResponse<Ticket>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<Ticket> apiResponse = response.body();
                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            Ticket updatedTicket = apiResponse.getData();
                            ticketAdapter.updateTicket(updatedTicket);
                            Toast.makeText(requireContext(), "Ticket escalated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            showError(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to escalate ticket");
                        }
                    } else {
                        showError("Error: " + response.code() + " - " + response.message());
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<Ticket>> call, Throwable t) {
                    showError("Network error: " + t.getMessage());
                }
            });
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void updateTicketStats(List<Ticket> tickets) {
        int openCount = 0;
        int assignedCount = 0;
        
        for (Ticket ticket : tickets) {
            if ("open".equals(ticket.getStatus()) || "new".equals(ticket.getLeadStatus())) {
                openCount++;
            }
            if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().isEmpty()) {
                assignedCount++;
            }
        }
        
        tvTicketStats.setText(openCount + " Open • " + assignedCount + " Assigned");
    }
    
    private void updateEmptyState() {
        boolean isEmpty = ticketAdapter.getItemCount() == 0;
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerTickets.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
    
    private void showLoading(boolean show) {
        if (show) {
            layoutLoading.setVisibility(View.VISIBLE);
            recyclerTickets.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.GONE);
        } else {
            layoutLoading.setVisibility(View.GONE);
            swipeRefreshTickets.setRefreshing(false);
        }
    }
    
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
        Log.e(TAG, message);
    }
    
    @Override
    public void refreshData() {
        loadTickets();
    }
    
    // Method to refresh tickets from external sources (WebSocket updates)
    public void refreshTickets() {
        if (isAdded() && getView() != null) {
            loadTickets();
        }
    }
    
    // Public method to filter tickets externally
    public void filterTickets(String query) {
        if (ticketAdapter != null) {
            ticketAdapter.filter(query);
        }
    }
    
    private void setupRealTimeUpdates() {
        webSocketManager.addEventListener("tickets", new WebSocketManager.WebSocketEventListener() {
            @Override
            public void onEvent(String eventType, com.google.gson.JsonObject data) {
                if (!isAdded() || getView() == null) return;
                
                try {
                    switch (eventType) {
                        case "ticket_created":
                            handleTicketCreated(data);
                            break;
                        case "ticket_updated":
                            handleTicketUpdated(data);
                            break;
                        case "ticket_assigned":
                            handleTicketAssigned(data);
                            break;
                        default:
                            Log.d(TAG, "Unhandled ticket event: " + eventType);
                            break;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error handling real-time event: " + e.getMessage());
                }
            }
        });
        
        // Connect WebSocket if not already connected
        if (!webSocketManager.isConnected()) {
            webSocketManager.connect();
        }
    }
    
    private void handleTicketCreated(com.google.gson.JsonObject data) {
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            Ticket newTicket = gson.fromJson(data.get("ticket"), Ticket.class);
            
            if (newTicket != null && ticketMatchesCurrentFilters(newTicket)) {
                ticketAdapter.addTicket(newTicket);
                updateEmptyState();
                Log.d(TAG, "New ticket added via WebSocket: " + newTicket.getTicketId());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling ticket created event: " + e.getMessage());
        }
    }
    
    private void handleTicketUpdated(com.google.gson.JsonObject data) {
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            Ticket updatedTicket = gson.fromJson(data.get("ticket"), Ticket.class);
            
            if (updatedTicket != null) {
                ticketAdapter.updateTicket(updatedTicket);
                Log.d(TAG, "Ticket updated via WebSocket: " + updatedTicket.getTicketId());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling ticket updated event: " + e.getMessage());
        }
    }
    
    private void handleTicketAssigned(com.google.gson.JsonObject data) {
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            Ticket assignedTicket = gson.fromJson(data.get("ticket"), Ticket.class);
            
            if (assignedTicket != null) {
                ticketAdapter.updateTicket(assignedTicket);
                
                // Show notification if assigned to current user
                if (currentUser != null && currentUser.getId().equals(assignedTicket.getAssignedTo())) {
                    Toast.makeText(requireContext(), 
                        "You have been assigned to ticket: " + assignedTicket.getDisplayName(), 
                        Toast.LENGTH_SHORT).show();
                }
                
                Log.d(TAG, "Ticket assigned via WebSocket: " + assignedTicket.getTicketId());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling ticket assigned event: " + e.getMessage());
        }
    }
    
    private boolean ticketMatchesCurrentFilters(Ticket ticket) {
        // Check status filter
        if (!currentStatusFilter.equals("all")) {
            String ticketStatus = ticket.getStatus() != null ? ticket.getStatus() : ticket.getLeadStatus();
            if (!currentStatusFilter.equals(ticketStatus)) {
                return false;
            }
        }
        
        // Check priority filter
        if (!currentPriorityFilter.equals("all")) {
            if (!currentPriorityFilter.equals(ticket.getPriority())) {
                return false;
            }
        }
        
        // Check tab filter
        if (!currentTabFilter.equals("all")) {
            switch (currentTabFilter) {
                case "my_tickets":
                    if (currentUser == null || !currentUser.getId().equals(ticket.getAssignedTo())) {
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
                    // TODO: Check if ticket is overdue based on due date
                    break;
            }
        }
        
        // Check role-based visibility
        if (currentUser != null) {
            if (currentUser.isAgent()) {
                return currentUser.getId().equals(ticket.getAssignedTo());
            } else if (currentUser.isManager()) {
                return currentUser.getTeamIds() != null && 
                       currentUser.getTeamIds().contains(ticket.getTeamId());
            }
        }
        
        return true;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webSocketManager != null) {
            webSocketManager.removeEventListener("tickets");
        }
    }
}