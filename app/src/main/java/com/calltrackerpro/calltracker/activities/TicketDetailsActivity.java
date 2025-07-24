package com.calltrackerpro.calltracker.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.adapters.TicketNotesAdapter;
import com.calltrackerpro.calltracker.models.Ticket;
import com.calltrackerpro.calltracker.models.TicketNote;
import com.calltrackerpro.calltracker.models.User;
import com.calltrackerpro.calltracker.services.TicketService;
import com.calltrackerpro.calltracker.utils.PermissionManager;
import com.calltrackerpro.calltracker.utils.TokenManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TicketDetailsActivity extends AppCompatActivity {
    private static final String TAG = "TicketDetailsActivity";
    
    // UI Components
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private LinearLayout layoutContent;
    private CardView cardContactInfo;
    private CardView cardCallInfo;
    private CardView cardLeadInfo;
    private CardView cardPipelineInfo;
    private CardView cardNotes;
    
    // Contact Information
    private TextView textContactName;
    private TextView textPhoneNumber;
    private TextView textCompany;
    private TextView textEmail;
    private ImageView buttonCall;
    private ImageView buttonSms;
    
    // Call Information
    private TextView textCallType;
    private TextView textCallDuration;
    private TextView textCallDate;
    private TextView textCallQuality;
    
    // Lead Information
    private Chip chipLeadStatus;
    private Chip chipPriority;
    private Chip chipInterestLevel;
    private TextView textLeadSource;
    private TextView textBudgetRange;
    private TextView textTimeline;
    
    // Pipeline Information
    private Chip chipStage;
    private TextView textAssignedAgent;
    private TextView textNextFollowUp;
    private TextView textDealValue;
    private TextView textConversionProbability;
    
    // Notes Section
    private RecyclerView recyclerViewNotes;
    private TicketNotesAdapter notesAdapter;
    private TextInputEditText editTextNewNote;
    private Button buttonAddNote;
    private Button buttonAddPrivateNote;
    
    // Data and Services
    private TicketService ticketService;
    private TokenManager tokenManager;
    private PermissionManager permissionManager;
    private User currentUser;
    private Ticket currentTicket;
    private List<TicketNote> notesList = new ArrayList<>();
    
    // Activity Mode
    private String mode; // "view", "edit", "create"
    private String ticketId;
    private boolean isEditing = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_details);
        
        initServices();
        getIntentData();
        initViews();
        setupToolbar();
        setupNotes();
        
        if ("create".equals(mode)) {
            enterEditMode();
        } else if (ticketId != null) {
            loadTicketDetails();
        }
    }

    private void initServices() {
        ticketService = new TicketService(this);
        tokenManager = new TokenManager(this);
        // TODO: Get current user from TokenManager
        currentUser = getCurrentUser();
        if (currentUser != null) {
            permissionManager = new PermissionManager(currentUser);
        }
    }

    private void getIntentData() {
        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");
        ticketId = intent.getStringExtra("ticketId");
        
        if (mode == null) mode = "view";
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        layoutContent = findViewById(R.id.layout_content);
        
        // Cards
        cardContactInfo = findViewById(R.id.card_contact_info);
        cardCallInfo = findViewById(R.id.card_call_info);
        cardLeadInfo = findViewById(R.id.card_lead_info);
        cardPipelineInfo = findViewById(R.id.card_pipeline_info);
        cardNotes = findViewById(R.id.card_notes);
        
        // Contact Information
        textContactName = findViewById(R.id.text_contact_name);
        textPhoneNumber = findViewById(R.id.text_phone_number);
        textCompany = findViewById(R.id.text_company);
        textEmail = findViewById(R.id.text_email);
        buttonCall = findViewById(R.id.button_call);
        buttonSms = findViewById(R.id.button_sms);
        
        // Call Information
        textCallType = findViewById(R.id.text_call_type);
        textCallDuration = findViewById(R.id.text_call_duration);
        textCallDate = findViewById(R.id.text_call_date);
        textCallQuality = findViewById(R.id.text_call_quality);
        
        // Lead Information
        chipLeadStatus = findViewById(R.id.chip_lead_status);
        chipPriority = findViewById(R.id.chip_priority);
        chipInterestLevel = findViewById(R.id.chip_interest_level);
        textLeadSource = findViewById(R.id.text_lead_source);
        textBudgetRange = findViewById(R.id.text_budget_range);
        textTimeline = findViewById(R.id.text_timeline);
        
        // Pipeline Information
        chipStage = findViewById(R.id.chip_stage);
        textAssignedAgent = findViewById(R.id.text_assigned_agent);
        textNextFollowUp = findViewById(R.id.text_next_follow_up);
        textDealValue = findViewById(R.id.text_deal_value);
        textConversionProbability = findViewById(R.id.text_conversion_probability);
        
        // Notes
        recyclerViewNotes = findViewById(R.id.recycler_view_notes);
        editTextNewNote = findViewById(R.id.edit_text_new_note);
        buttonAddNote = findViewById(R.id.button_add_note);
        buttonAddPrivateNote = findViewById(R.id.button_add_private_note);
        
        setupClickListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            
            String title = "create".equals(mode) ? "Create Ticket" : 
                          "edit".equals(mode) ? "Edit Ticket" : "Ticket Details";
            getSupportActionBar().setTitle(title);
        }
    }

    private void setupNotes() {
        notesAdapter = new TicketNotesAdapter(notesList);
        recyclerViewNotes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotes.setAdapter(notesAdapter);
    }

    private void setupClickListeners() {
        buttonCall.setOnClickListener(v -> makePhoneCall());
        buttonSms.setOnClickListener(v -> sendSms());
        buttonAddNote.setOnClickListener(v -> addNote(false));
        buttonAddPrivateNote.setOnClickListener(v -> addNote(true));
        
        // Chip click listeners for editing
        chipLeadStatus.setOnClickListener(v -> {
            if (isEditing) showStatusDialog();
        });
        chipPriority.setOnClickListener(v -> {
            if (isEditing) showPriorityDialog();
        });
        chipStage.setOnClickListener(v -> {
            if (isEditing) showStageDialog();
        });
    }

    private void loadTicketDetails() {
        showLoading(true);
        
        ticketService.getTicket(ticketId, new TicketService.TicketCallback<Ticket>() {
            @Override
            public void onSuccess(Ticket ticket) {
                showLoading(false);
                currentTicket = ticket;
                populateTicketData();
                loadTicketNotes();
            }
            
            @Override
            public void onError(String error) {
                showLoading(false);
                Log.e(TAG, "Error loading ticket: " + error);
                Toast.makeText(TicketDetailsActivity.this, "Error loading ticket: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadTicketNotes() {
        ticketService.getTicketNotes(ticketId, new TicketService.TicketCallback<List<TicketNote>>() {
            @Override
            public void onSuccess(List<TicketNote> notes) {
                notesList.clear();
                notesList.addAll(notes);
                notesAdapter.notifyDataSetChanged();
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading notes: " + error);
            }
        });
    }

    private void populateTicketData() {
        if (currentTicket == null) return;
        
        // Contact Information
        textContactName.setText(currentTicket.getDisplayName());
        textPhoneNumber.setText(currentTicket.getPhoneNumber());
        textCompany.setText(currentTicket.getCompany());
        textEmail.setText(currentTicket.getEmail());
        
        // Call Information
        textCallType.setText(formatCallType(currentTicket.getCallType()));
        textCallDuration.setText(currentTicket.getFormattedDuration());
        textCallDate.setText(formatDate(currentTicket.getCallDate()));
        textCallQuality.setText(formatCallQuality(currentTicket.getCallQuality()));
        
        // Lead Information
        chipLeadStatus.setText(currentTicket.getLeadStatusDisplayName());
        chipPriority.setText(currentTicket.getPriorityDisplayName());
        chipInterestLevel.setText(formatInterestLevel(currentTicket.getInterestLevel()));
        textLeadSource.setText(formatLeadSource(currentTicket.getLeadSource()));
        textBudgetRange.setText(currentTicket.getBudgetRange());
        textTimeline.setText(currentTicket.getTimeline());
        
        // Pipeline Information
        chipStage.setText(currentTicket.getStageDisplayName());
        textAssignedAgent.setText(formatAssignedAgent(currentTicket.getAssignedAgent()));
        textNextFollowUp.setText(formatDate(currentTicket.getNextFollowUp()));
        textDealValue.setText(formatDealValue(currentTicket.getDealValue()));
        textConversionProbability.setText(formatProbability(currentTicket.getConversionProbability()));
        
        // Update chip styles based on values
        updateChipStyles();
    }

    private void updateChipStyles() {
        // TODO: Apply different colors/styles based on status, priority, stage
    }

    private void makePhoneCall() {
        if (currentTicket != null && currentTicket.getPhoneNumber() != null) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + currentTicket.getPhoneNumber()));
            startActivity(intent);
        }
    }

    private void sendSms() {
        if (currentTicket != null && currentTicket.getPhoneNumber() != null) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + currentTicket.getPhoneNumber()));
            startActivity(intent);
        }
    }

    private void addNote(boolean isPrivate) {
        String noteText = editTextNewNote.getText().toString().trim();
        if (noteText.isEmpty()) {
            Toast.makeText(this, "Please enter a note", Toast.LENGTH_SHORT).show();
            return;
        }
        
        TicketNote note = new TicketNote(noteText, currentUser.getId(), isPrivate);
        note.setAuthorName(currentUser.getFullName());
        note.setTimestamp(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date()));
        
        ticketService.addTicketNote(ticketId, note, new TicketService.TicketCallback<TicketNote>() {
            @Override
            public void onSuccess(TicketNote addedNote) {
                notesList.add(0, addedNote); // Add to top
                notesAdapter.notifyItemInserted(0);
                editTextNewNote.setText("");
                Toast.makeText(TicketDetailsActivity.this, "Note added successfully", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error adding note: " + error);
                Toast.makeText(TicketDetailsActivity.this, "Error adding note: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showStatusDialog() {
        String[] statuses = {"New", "Contacted", "Qualified", "Converted", "Closed"};
        String[] statusValues = {"new", "contacted", "qualified", "converted", "closed"};
        
        int currentIndex = findArrayIndex(statusValues, currentTicket.getLeadStatus());
        
        new AlertDialog.Builder(this)
            .setTitle("Select Lead Status")
            .setSingleChoiceItems(statuses, currentIndex, (dialog, which) -> {
                currentTicket.setLeadStatus(statusValues[which]);
                chipLeadStatus.setText(statuses[which]);
                dialog.dismiss();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showPriorityDialog() {
        String[] priorities = {"High", "Medium", "Low"};
        String[] priorityValues = {"high", "medium", "low"};
        
        int currentIndex = findArrayIndex(priorityValues, currentTicket.getPriority());
        
        new AlertDialog.Builder(this)
            .setTitle("Select Priority")
            .setSingleChoiceItems(priorities, currentIndex, (dialog, which) -> {
                currentTicket.setPriority(priorityValues[which]);
                chipPriority.setText(priorities[which]);
                dialog.dismiss();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showStageDialog() {
        String[] stages = {"Prospect", "Qualified", "Proposal", "Negotiation", "Closed Won", "Closed Lost"};
        String[] stageValues = {"prospect", "qualified", "proposal", "negotiation", "closed-won", "closed-lost"};
        
        int currentIndex = findArrayIndex(stageValues, currentTicket.getStage());
        
        new AlertDialog.Builder(this)
            .setTitle("Select Stage")
            .setSingleChoiceItems(stages, currentIndex, (dialog, which) -> {
                currentTicket.setStage(stageValues[which]);
                chipStage.setText(stages[which]);
                dialog.dismiss();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private int findArrayIndex(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }

    private void enterEditMode() {
        isEditing = true;
        invalidateOptionsMenu();
        
        // Enable chip clicking and other edit functionality
        chipLeadStatus.setClickable(true);
        chipPriority.setClickable(true);
        chipStage.setClickable(true);
        
        if ("create".equals(mode)) {
            // Initialize new ticket
            currentTicket = new Ticket();
            currentTicket.setPhoneNumber("");
            currentTicket.setContactName("");
            populateTicketData();
        }
    }

    private void exitEditMode() {
        isEditing = false;
        invalidateOptionsMenu();
        
        // Disable edit functionality
        chipLeadStatus.setClickable(false);
        chipPriority.setClickable(false);
        chipStage.setClickable(false);
    }

    private void saveTicket() {
        if (currentTicket == null) return;
        
        showLoading(true);
        
        if ("create".equals(mode)) {
            ticketService.createTicket(currentTicket, new TicketService.TicketCallback<Ticket>() {
                @Override
                public void onSuccess(Ticket ticket) {
                    showLoading(false);
                    currentTicket = ticket;
                    ticketId = ticket.getId();
                    mode = "view";
                    exitEditMode();
                    Toast.makeText(TicketDetailsActivity.this, "Ticket created successfully", Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onError(String error) {
                    showLoading(false);
                    Log.e(TAG, "Error creating ticket: " + error);
                    Toast.makeText(TicketDetailsActivity.this, "Error creating ticket: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ticketService.updateTicket(ticketId, currentTicket, new TicketService.TicketCallback<Ticket>() {
                @Override
                public void onSuccess(Ticket ticket) {
                    showLoading(false);
                    currentTicket = ticket;
                    exitEditMode();
                    Toast.makeText(TicketDetailsActivity.this, "Ticket updated successfully", Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onError(String error) {
                    showLoading(false);
                    Log.e(TAG, "Error updating ticket: " + error);
                    Toast.makeText(TicketDetailsActivity.this, "Error updating ticket: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        layoutContent.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // Formatting helper methods
    private String formatCallType(String callType) {
        if (callType == null) return "Unknown";
        switch (callType.toLowerCase()) {
            case "incoming": return "Incoming Call";
            case "outgoing": return "Outgoing Call";
            case "missed": return "Missed Call";
            default: return callType;
        }
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return date != null ? outputFormat.format(date) : dateString;
        } catch (Exception e) {
            return dateString;
        }
    }

    private String formatCallQuality(int quality) {
        if (quality <= 0) return "Not rated";
        return quality + "/5 stars";
    }

    private String formatInterestLevel(String level) {
        if (level == null) return "Unknown";
        switch (level.toLowerCase()) {
            case "hot": return "Hot Lead";
            case "warm": return "Warm Lead";
            case "cold": return "Cold Lead";
            default: return level;
        }
    }

    private String formatLeadSource(String source) {
        if (source == null) return "Unknown";
        switch (source.toLowerCase()) {
            case "phone_call": return "Phone Call";
            case "cold_call": return "Cold Call";
            case "referral": return "Referral";
            case "website": return "Website";
            case "marketing": return "Marketing";
            default: return source;
        }
    }

    private String formatAssignedAgent(String agentId) {
        if (agentId == null || agentId.isEmpty()) return "Unassigned";
        // TODO: Get actual agent name from ID
        return "Agent: " + agentId;
    }

    private String formatDealValue(double value) {
        if (value <= 0) return "Not set";
        return String.format(Locale.getDefault(), "$%.2f", value);
    }

    private String formatProbability(int probability) {
        if (probability <= 0) return "Not set";
        return probability + "%";
    }

    private User getCurrentUser() {
        // TODO: Implement getting current user from TokenManager
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ticket_details, menu);
        
        MenuItem editItem = menu.findItem(R.id.action_edit);
        MenuItem saveItem = menu.findItem(R.id.action_save);
        MenuItem deleteItem = menu.findItem(R.id.action_delete);
        
        if (isEditing) {
            editItem.setVisible(false);
            saveItem.setVisible(true);
        } else {
            editItem.setVisible(true);
            saveItem.setVisible(false);
        }
        
        // Show delete only if user has permission and not in create mode
        deleteItem.setVisible(!isEditing && !"create".equals(mode) && 
                              permissionManager != null && permissionManager.canDeleteContacts());
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_edit) {
            enterEditMode();
            return true;
        } else if (id == R.id.action_save) {
            saveTicket();
            return true;
        } else if (id == R.id.action_delete) {
            showDeleteConfirmation();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Ticket")
            .setMessage("Are you sure you want to delete this ticket? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> deleteTicket())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteTicket() {
        showLoading(true);
        
        ticketService.deleteTicket(ticketId, new TicketService.TicketCallback<String>() {
            @Override
            public void onSuccess(String result) {
                showLoading(false);
                Toast.makeText(TicketDetailsActivity.this, "Ticket deleted successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
            
            @Override
            public void onError(String error) {
                showLoading(false);
                Log.e(TAG, "Error deleting ticket: " + error);
                Toast.makeText(TicketDetailsActivity.this, "Error deleting ticket: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isEditing && "create".equals(mode)) {
            new AlertDialog.Builder(this)
                .setTitle("Discard Changes")
                .setMessage("Are you sure you want to discard this ticket?")
                .setPositiveButton("Discard", (dialog, which) -> super.onBackPressed())
                .setNegativeButton("Cancel", null)
                .show();
        } else {
            super.onBackPressed();
        }
    }
}