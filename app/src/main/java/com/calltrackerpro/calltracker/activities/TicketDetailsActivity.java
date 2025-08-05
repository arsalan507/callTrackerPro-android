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
import androidx.activity.OnBackPressedCallback;
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
    private androidx.core.widget.NestedScrollView layoutContent;
    private CardView cardContactInfo;
    private CardView cardCallInfo;
    private CardView cardLeadInfo;
    private CardView cardPipelineInfo;
    private CardView cardNotes;
    
    // Contact Information - View Mode
    private TextView textContactName;
    private TextView textPhoneNumber;
    private TextView textCompany;
    private TextView textEmail;
    private ImageView buttonCall;
    private ImageView buttonSms;
    
    // Contact Information - Edit Mode
    private TextInputLayout layoutContactName;
    private TextInputLayout layoutPhoneNumber;
    private TextInputLayout layoutCompany;
    private TextInputLayout layoutEmail;
    private TextInputEditText editContactName;
    private TextInputEditText editPhoneNumber;
    private TextInputEditText editCompany;
    private TextInputEditText editEmail;
    
    // Call Information - View Mode
    private LinearLayout layoutCallTypeView;
    private LinearLayout layoutCallDurationView;
    private LinearLayout layoutCallDateView;
    private LinearLayout layoutCallQualityView;
    private TextView textCallType;
    private TextView textCallDuration;
    private TextView textCallDate;
    private TextView textCallQuality;
    
    // Call Information - Edit Mode
    private LinearLayout layoutCallTypeEdit;
    private LinearLayout layoutCallQualityEdit;
    private TextInputLayout layoutCallDuration;
    private TextInputLayout layoutCallDate;
    private Spinner spinnerCallType;
    private Spinner spinnerCallQuality;
    private TextInputEditText editCallDuration;
    private TextInputEditText editCallDate;
    
    // Lead Information - View Mode
    private LinearLayout layoutStatusChipsView;
    private LinearLayout layoutLeadDetailsView;
    private Chip chipLeadStatus;
    private Chip chipPriority;
    private Chip chipInterestLevel;
    private TextView textLeadSource;
    private TextView textBudgetRange;
    private TextView textTimeline;
    
    // Lead Information - Edit Mode
    private LinearLayout layoutLeadStatusEdit;
    private LinearLayout layoutPriorityEdit;
    private LinearLayout layoutInterestLevelEdit;
    private TextInputLayout layoutBudgetRange;
    private TextInputLayout layoutTimeline;
    private Spinner spinnerLeadStatus;
    private Spinner spinnerPriority;
    private Spinner spinnerInterestLevel;
    private TextInputEditText editBudgetRange;
    private TextInputEditText editTimeline;
    
    // Pipeline Information - View Mode
    private LinearLayout layoutPipelineDetailsView;
    private Chip chipStage;
    private TextView textAssignedAgent;
    private TextView textNextFollowUp;
    private TextView textDealValue;
    private TextView textConversionProbability;
    
    // Pipeline Information - Edit Mode
    private LinearLayout layoutStageEdit;
    private TextInputLayout layoutAssignedAgent;
    private TextInputLayout layoutNextFollowUp;
    private TextInputLayout layoutDealValue;
    private TextInputLayout layoutConversionProbability;
    private Spinner spinnerStage;
    private TextInputEditText editAssignedAgent;
    private TextInputEditText editNextFollowUp;
    private TextInputEditText editDealValue;
    private TextInputEditText editConversionProbability;
    
    // Notes Section
    private RecyclerView recyclerViewNotes;
    private TicketNotesAdapter notesAdapter;
    private TextInputEditText editTextNewNote;
    private Button buttonAddNote;
    private Button buttonAddPrivateNote;
    
    // Save Button
    private com.google.android.material.button.MaterialButton btnSaveTicket;
    
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
        setupBackPressedCallback();
        
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
        
        // Contact Information - View Mode
        textContactName = findViewById(R.id.text_contact_name);
        textPhoneNumber = findViewById(R.id.text_phone_number);
        textCompany = findViewById(R.id.text_company);
        textEmail = findViewById(R.id.text_email);
        buttonCall = findViewById(R.id.button_call);
        buttonSms = findViewById(R.id.button_sms);
        
        // Contact Information - Edit Mode
        layoutContactName = findViewById(R.id.layout_contact_name);
        layoutPhoneNumber = findViewById(R.id.layout_phone_number);
        layoutCompany = findViewById(R.id.layout_company);
        layoutEmail = findViewById(R.id.layout_email);
        editContactName = findViewById(R.id.edit_contact_name);
        editPhoneNumber = findViewById(R.id.edit_phone_number);
        editCompany = findViewById(R.id.edit_company);
        editEmail = findViewById(R.id.edit_email);
        
        // Call Information - View Mode
        layoutCallTypeView = findViewById(R.id.layout_call_type_view);
        layoutCallDurationView = findViewById(R.id.layout_call_duration_view);
        layoutCallDateView = findViewById(R.id.layout_call_date_view);
        layoutCallQualityView = findViewById(R.id.layout_call_quality_view);
        textCallType = findViewById(R.id.text_call_type);
        textCallDuration = findViewById(R.id.text_call_duration);
        textCallDate = findViewById(R.id.text_call_date);
        textCallQuality = findViewById(R.id.text_call_quality);
        
        // Call Information - Edit Mode
        layoutCallTypeEdit = findViewById(R.id.layout_call_type_edit);
        layoutCallQualityEdit = findViewById(R.id.layout_call_quality_edit);
        layoutCallDuration = findViewById(R.id.layout_call_duration);
        layoutCallDate = findViewById(R.id.layout_call_date);
        spinnerCallType = findViewById(R.id.spinner_call_type);
        spinnerCallQuality = findViewById(R.id.spinner_call_quality);
        editCallDuration = findViewById(R.id.edit_call_duration);
        editCallDate = findViewById(R.id.edit_call_date);
        
        // Lead Information - View Mode
        layoutStatusChipsView = findViewById(R.id.layout_status_chips_view);
        layoutLeadDetailsView = findViewById(R.id.layout_lead_details_view);
        chipLeadStatus = findViewById(R.id.chip_lead_status);
        chipPriority = findViewById(R.id.chip_priority);
        chipInterestLevel = findViewById(R.id.chip_interest_level);
        textLeadSource = findViewById(R.id.text_lead_source);
        textBudgetRange = findViewById(R.id.text_budget_range);
        textTimeline = findViewById(R.id.text_timeline);
        
        // Lead Information - Edit Mode
        layoutLeadStatusEdit = findViewById(R.id.layout_lead_status_edit);
        layoutPriorityEdit = findViewById(R.id.layout_priority_edit);
        layoutInterestLevelEdit = findViewById(R.id.layout_interest_level_edit);
        layoutBudgetRange = findViewById(R.id.layout_budget_range);
        layoutTimeline = findViewById(R.id.layout_timeline);
        spinnerLeadStatus = findViewById(R.id.spinner_lead_status);
        spinnerPriority = findViewById(R.id.spinner_priority);
        spinnerInterestLevel = findViewById(R.id.spinner_interest_level);
        editBudgetRange = findViewById(R.id.edit_budget_range);
        editTimeline = findViewById(R.id.edit_timeline);
        
        // Pipeline Information - View Mode
        layoutPipelineDetailsView = findViewById(R.id.layout_pipeline_details_view);
        chipStage = findViewById(R.id.chip_stage);
        textAssignedAgent = findViewById(R.id.text_assigned_agent);
        textNextFollowUp = findViewById(R.id.text_next_follow_up);
        textDealValue = findViewById(R.id.text_deal_value);
        textConversionProbability = findViewById(R.id.text_conversion_probability);
        
        // Pipeline Information - Edit Mode
        layoutStageEdit = findViewById(R.id.layout_stage_edit);
        layoutAssignedAgent = findViewById(R.id.layout_assigned_agent);
        layoutNextFollowUp = findViewById(R.id.layout_next_follow_up);
        layoutDealValue = findViewById(R.id.layout_deal_value);
        layoutConversionProbability = findViewById(R.id.layout_conversion_probability);
        spinnerStage = findViewById(R.id.spinner_stage);
        editAssignedAgent = findViewById(R.id.edit_assigned_agent);
        editNextFollowUp = findViewById(R.id.edit_next_follow_up);
        editDealValue = findViewById(R.id.edit_deal_value);
        editConversionProbability = findViewById(R.id.edit_conversion_probability);
        
        // Notes
        recyclerViewNotes = findViewById(R.id.recycler_view_notes);
        editTextNewNote = findViewById(R.id.edit_text_new_note);
        buttonAddNote = findViewById(R.id.button_add_note);
        buttonAddPrivateNote = findViewById(R.id.button_add_private_note);
        
        // Save Button
        btnSaveTicket = findViewById(R.id.btn_save_ticket);
        btnSaveTicket.setOnClickListener(v -> saveTicket());
        
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
        
        // Contact Information with null checks
        textContactName.setText(currentTicket.getDisplayName() != null ? currentTicket.getDisplayName() : "Unknown Contact");
        textPhoneNumber.setText(currentTicket.getPhoneNumber() != null ? currentTicket.getPhoneNumber() : "No phone");
        textCompany.setText(currentTicket.getCompany() != null ? currentTicket.getCompany() : "No company");
        textEmail.setText(currentTicket.getEmail() != null ? currentTicket.getEmail() : "No email");
        
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
        
        if (currentUser == null) {
            Toast.makeText(this, "User session expired. Please login again.", Toast.LENGTH_SHORT).show();
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
        
        // Show Save button
        btnSaveTicket.setVisibility(View.VISIBLE);
        
        // Contact Information - Switch to edit mode
        textContactName.setVisibility(View.GONE);
        textPhoneNumber.setVisibility(View.GONE);
        textCompany.setVisibility(View.GONE);
        textEmail.setVisibility(View.GONE);
        buttonCall.setVisibility(View.GONE);
        buttonSms.setVisibility(View.GONE);
        
        layoutContactName.setVisibility(View.VISIBLE);
        layoutPhoneNumber.setVisibility(View.VISIBLE);
        layoutCompany.setVisibility(View.VISIBLE);
        layoutEmail.setVisibility(View.VISIBLE);
        
        // Call Information - Switch to edit mode
        layoutCallTypeView.setVisibility(View.GONE);
        layoutCallDurationView.setVisibility(View.GONE);
        layoutCallDateView.setVisibility(View.GONE);
        layoutCallQualityView.setVisibility(View.GONE);
        
        layoutCallTypeEdit.setVisibility(View.VISIBLE);
        layoutCallDuration.setVisibility(View.VISIBLE);
        layoutCallDate.setVisibility(View.VISIBLE);
        layoutCallQualityEdit.setVisibility(View.VISIBLE);
        
        // Lead Information - Switch to edit mode
        layoutStatusChipsView.setVisibility(View.GONE);
        layoutLeadDetailsView.setVisibility(View.GONE);
        
        layoutLeadStatusEdit.setVisibility(View.VISIBLE);
        layoutPriorityEdit.setVisibility(View.VISIBLE);
        layoutInterestLevelEdit.setVisibility(View.VISIBLE);
        layoutBudgetRange.setVisibility(View.VISIBLE);
        layoutTimeline.setVisibility(View.VISIBLE);
        
        // Pipeline Information - Switch to edit mode
        chipStage.setVisibility(View.GONE);
        layoutPipelineDetailsView.setVisibility(View.GONE);
        
        layoutStageEdit.setVisibility(View.VISIBLE);
        layoutAssignedAgent.setVisibility(View.VISIBLE);
        layoutNextFollowUp.setVisibility(View.VISIBLE);
        layoutDealValue.setVisibility(View.VISIBLE);
        layoutConversionProbability.setVisibility(View.VISIBLE);
        
        if ("create".equals(mode)) {
            // Initialize new ticket
            currentTicket = new Ticket();
            currentTicket.setPhoneNumber("");
            currentTicket.setContactName("");
            clearEditFields();
        } else {
            // Populate edit fields with current data
            populateEditFields();
        }
    }

    private void exitEditMode() {
        isEditing = false;
        invalidateOptionsMenu();
        
        // Hide Save button
        btnSaveTicket.setVisibility(View.GONE);
        
        // Contact Information - Switch to view mode
        layoutContactName.setVisibility(View.GONE);
        layoutPhoneNumber.setVisibility(View.GONE);
        layoutCompany.setVisibility(View.GONE);
        layoutEmail.setVisibility(View.GONE);
        
        textContactName.setVisibility(View.VISIBLE);
        textPhoneNumber.setVisibility(View.VISIBLE);
        textCompany.setVisibility(View.VISIBLE);
        textEmail.setVisibility(View.VISIBLE);
        buttonCall.setVisibility(View.VISIBLE);
        buttonSms.setVisibility(View.VISIBLE);
        
        // Call Information - Switch to view mode
        layoutCallTypeEdit.setVisibility(View.GONE);
        layoutCallDuration.setVisibility(View.GONE);
        layoutCallDate.setVisibility(View.GONE);
        layoutCallQualityEdit.setVisibility(View.GONE);
        
        layoutCallTypeView.setVisibility(View.VISIBLE);
        layoutCallDurationView.setVisibility(View.VISIBLE);
        layoutCallDateView.setVisibility(View.VISIBLE);
        layoutCallQualityView.setVisibility(View.VISIBLE);
        
        // Lead Information - Switch to view mode
        layoutLeadStatusEdit.setVisibility(View.GONE);
        layoutPriorityEdit.setVisibility(View.GONE);
        layoutInterestLevelEdit.setVisibility(View.GONE);
        layoutBudgetRange.setVisibility(View.GONE);
        layoutTimeline.setVisibility(View.GONE);
        
        layoutStatusChipsView.setVisibility(View.VISIBLE);
        layoutLeadDetailsView.setVisibility(View.VISIBLE);
        
        // Pipeline Information - Switch to view mode
        layoutStageEdit.setVisibility(View.GONE);
        layoutAssignedAgent.setVisibility(View.GONE);
        layoutNextFollowUp.setVisibility(View.GONE);
        layoutDealValue.setVisibility(View.GONE);
        layoutConversionProbability.setVisibility(View.GONE);
        
        chipStage.setVisibility(View.VISIBLE);
        layoutPipelineDetailsView.setVisibility(View.VISIBLE);
        
        // Update display with current data
        populateTicketData();
    }

    private void saveTicket() {
        if (currentTicket == null) currentTicket = new Ticket();
        
        // Collect data from input fields
        if (!collectFormData()) {
            return; // Validation failed
        }
        
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
        if (tokenManager != null) {
            return tokenManager.getUser();
        }
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

    private void setupBackPressedCallback() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isEditing && "create".equals(mode)) {
                    new AlertDialog.Builder(TicketDetailsActivity.this)
                        .setTitle("Discard Changes")
                        .setMessage("Are you sure you want to discard this ticket?")
                        .setPositiveButton("Discard", (dialog, which) -> finish())
                        .setNegativeButton("Cancel", null)
                        .show();
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
    
    private void clearEditFields() {
        // Clear Contact Information
        editContactName.setText("");
        editPhoneNumber.setText("");
        editCompany.setText("");
        editEmail.setText("");
        
        // Clear Call Information
        spinnerCallType.setSelection(0);
        editCallDuration.setText("");
        editCallDate.setText("");
        spinnerCallQuality.setSelection(0);
        
        // Clear Lead Information
        spinnerLeadStatus.setSelection(0);
        spinnerPriority.setSelection(0);
        spinnerInterestLevel.setSelection(0);
        editBudgetRange.setText("");
        editTimeline.setText("");
        
        // Clear Pipeline Information
        spinnerStage.setSelection(0);
        editAssignedAgent.setText("");
        editNextFollowUp.setText("");
        editDealValue.setText("");
        editConversionProbability.setText("");
    }
    
    private void populateEditFields() {
        if (currentTicket == null) return;
        
        // Populate Contact Information
        editContactName.setText(currentTicket.getDisplayName() != null ? currentTicket.getDisplayName() : "");
        editPhoneNumber.setText(currentTicket.getPhoneNumber() != null ? currentTicket.getPhoneNumber() : "");
        editCompany.setText(currentTicket.getCompany() != null ? currentTicket.getCompany() : "");
        editEmail.setText(currentTicket.getEmail() != null ? currentTicket.getEmail() : "");
        
        // Populate Call Information
        setSpinnerSelection(spinnerCallType, getCallTypeIndex(currentTicket.getCallType()));
        editCallDuration.setText(String.valueOf(currentTicket.getCallDuration()));
        editCallDate.setText(formatDateForInput(currentTicket.getCallDate()));
        setSpinnerSelection(spinnerCallQuality, currentTicket.getCallQuality() - 1);
        
        // Populate Lead Information
        setSpinnerSelection(spinnerLeadStatus, getLeadStatusIndex(currentTicket.getLeadStatus()));
        setSpinnerSelection(spinnerPriority, getPriorityIndex(currentTicket.getPriority()));
        setSpinnerSelection(spinnerInterestLevel, getInterestLevelIndex(currentTicket.getInterestLevel()));
        editBudgetRange.setText(currentTicket.getBudgetRange() != null ? currentTicket.getBudgetRange() : "");
        editTimeline.setText(currentTicket.getTimeline() != null ? currentTicket.getTimeline() : "");
        
        // Populate Pipeline Information
        setSpinnerSelection(spinnerStage, getStageIndex(currentTicket.getStage()));
        editAssignedAgent.setText(currentTicket.getAssignedAgent() != null ? currentTicket.getAssignedAgent() : "");
        editNextFollowUp.setText(formatDateForInput(currentTicket.getNextFollowUp()));
        editDealValue.setText(currentTicket.getDealValue() > 0 ? String.valueOf(currentTicket.getDealValue()) : "");
        editConversionProbability.setText(currentTicket.getConversionProbability() > 0 ? String.valueOf(currentTicket.getConversionProbability()) : "");
    }
    
    private boolean collectFormData() {
        try {
            // Collect Contact Information
            currentTicket.setContactName(editContactName.getText().toString().trim());
            currentTicket.setPhoneNumber(editPhoneNumber.getText().toString().trim());
            currentTicket.setCompany(editCompany.getText().toString().trim());
            currentTicket.setEmail(editEmail.getText().toString().trim());
            
            // Validate required fields
            if (currentTicket.getContactName().isEmpty() && currentTicket.getPhoneNumber().isEmpty()) {
                Toast.makeText(this, "Please enter either a contact name or phone number", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            // Collect Call Information
            String[] callTypes = getResources().getStringArray(R.array.call_types);
            if (spinnerCallType.getSelectedItemPosition() < callTypes.length) {
                currentTicket.setCallType(callTypes[spinnerCallType.getSelectedItemPosition()].toLowerCase());
            }
            
            String durationStr = editCallDuration.getText().toString().trim();
            if (!durationStr.isEmpty()) {
                currentTicket.setCallDuration(Long.parseLong(durationStr));
            }
            
            currentTicket.setCallDate(editCallDate.getText().toString().trim());
            currentTicket.setCallQuality(spinnerCallQuality.getSelectedItemPosition() + 1);
            
            // Collect Lead Information
            String[] leadStatuses = getResources().getStringArray(R.array.lead_status_options);
            if (spinnerLeadStatus.getSelectedItemPosition() < leadStatuses.length) {
                currentTicket.setLeadStatus(leadStatuses[spinnerLeadStatus.getSelectedItemPosition()].toLowerCase());
            }
            
            String[] priorities = getResources().getStringArray(R.array.priority_options);
            if (spinnerPriority.getSelectedItemPosition() < priorities.length) {
                currentTicket.setPriority(priorities[spinnerPriority.getSelectedItemPosition()].toLowerCase());
            }
            
            String[] interestLevels = getResources().getStringArray(R.array.interest_level_options);
            if (spinnerInterestLevel.getSelectedItemPosition() < interestLevels.length) {
                currentTicket.setInterestLevel(interestLevels[spinnerInterestLevel.getSelectedItemPosition()].toLowerCase());
            }
            
            currentTicket.setBudgetRange(editBudgetRange.getText().toString().trim());
            currentTicket.setTimeline(editTimeline.getText().toString().trim());
            
            // Collect Pipeline Information
            String[] stages = getResources().getStringArray(R.array.pipeline_stages);
            if (spinnerStage.getSelectedItemPosition() < stages.length) {
                currentTicket.setStage(stages[spinnerStage.getSelectedItemPosition()].toLowerCase());
            }
            
            currentTicket.setAssignedAgent(editAssignedAgent.getText().toString().trim());
            currentTicket.setNextFollowUp(editNextFollowUp.getText().toString().trim());
            
            String dealValueStr = editDealValue.getText().toString().trim();
            if (!dealValueStr.isEmpty()) {
                currentTicket.setDealValue(Double.parseDouble(dealValueStr));
            }
            
            String conversionProbStr = editConversionProbability.getText().toString().trim();
            if (!conversionProbStr.isEmpty()) {
                currentTicket.setConversionProbability(Integer.parseInt(conversionProbStr));
            }
            
            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please check numeric fields for valid values", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    
    // Helper methods for spinner selection
    private void setSpinnerSelection(Spinner spinner, int position) {
        if (position >= 0 && position < spinner.getCount()) {
            spinner.setSelection(position);
        }
    }
    
    private int getCallTypeIndex(String callType) {
        if (callType == null) return 0;
        String[] types = getResources().getStringArray(R.array.call_types);
        for (int i = 0; i < types.length; i++) {
            if (types[i].toLowerCase().equals(callType.toLowerCase())) {
                return i;
            }
        }
        return 0;
    }
    
    private int getLeadStatusIndex(String status) {
        if (status == null) return 0;
        String[] statuses = getResources().getStringArray(R.array.lead_status_options);
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].toLowerCase().equals(status.toLowerCase())) {
                return i;
            }
        }
        return 0;
    }
    
    private int getPriorityIndex(String priority) {
        if (priority == null) return 0;
        String[] priorities = getResources().getStringArray(R.array.priority_options);
        for (int i = 0; i < priorities.length; i++) {
            if (priorities[i].toLowerCase().equals(priority.toLowerCase())) {
                return i;
            }
        }
        return 0;
    }
    
    private int getInterestLevelIndex(String level) {
        if (level == null) return 0;
        String[] levels = getResources().getStringArray(R.array.interest_level_options);
        for (int i = 0; i < levels.length; i++) {
            if (levels[i].toLowerCase().equals(level.toLowerCase())) {
                return i;
            }
        }
        return 0;
    }
    
    private int getStageIndex(String stage) {
        if (stage == null) return 0;
        String[] stages = getResources().getStringArray(R.array.pipeline_stages);
        for (int i = 0; i < stages.length; i++) {
            if (stages[i].toLowerCase().equals(stage.toLowerCase())) {
                return i;
            }
        }
        return 0;
    }
    
    private String formatDateForInput(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return date != null ? outputFormat.format(date) : dateString;
        } catch (Exception e) {
            return dateString;
        }
    }
}