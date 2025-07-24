package com.calltrackerpro.calltracker.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.calltrackerpro.calltracker.R;
import com.calltrackerpro.calltracker.models.TicketNote;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TicketNotesAdapter extends RecyclerView.Adapter<TicketNotesAdapter.NoteViewHolder> {
    
    private List<TicketNote> notes;

    public TicketNotesAdapter(List<TicketNote> notes) {
        this.notes = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        TicketNote note = notes.get(position);
        holder.bind(note);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        private TextView textNoteContent;
        private TextView textAuthorName;
        private TextView textTimestamp;
        private TextView textNoteType;
        private ImageView iconNoteType;
        private View noteContainer;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            
            textNoteContent = itemView.findViewById(R.id.text_note_content);
            textAuthorName = itemView.findViewById(R.id.text_author_name);
            textTimestamp = itemView.findViewById(R.id.text_timestamp);
            textNoteType = itemView.findViewById(R.id.text_note_type);
            iconNoteType = itemView.findViewById(R.id.icon_note_type);
            noteContainer = itemView.findViewById(R.id.note_container);
        }

        public void bind(TicketNote note) {
            textNoteContent.setText(note.getNote());
            textAuthorName.setText(note.getAuthorName() != null ? note.getAuthorName() : "Unknown");
            textTimestamp.setText(formatTimestamp(note.getTimestamp()));
            
            // Set note type and styling
            if (note.isPrivate()) {
                textNoteType.setText("Internal Note");
                textNoteType.setVisibility(View.VISIBLE);
                iconNoteType.setImageResource(R.drawable.ic_lock);
                iconNoteType.setVisibility(View.VISIBLE);
                
                // Style for private notes
                noteContainer.setBackgroundColor(Color.parseColor("#FFF3E0")); // Light orange
                textNoteType.setTextColor(Color.parseColor("#E65100")); // Dark orange
                
            } else {
                textNoteType.setText("Client Note");
                textNoteType.setVisibility(View.VISIBLE);
                iconNoteType.setImageResource(R.drawable.ic_person);
                iconNoteType.setVisibility(View.VISIBLE);
                
                // Style for client notes
                noteContainer.setBackgroundColor(Color.parseColor("#E8F5E8")); // Light green
                textNoteType.setTextColor(Color.parseColor("#2E7D32")); // Dark green
            }
            
            // Handle system notes
            if ("system".equals(note.getNoteType())) {
                textNoteType.setText("System Note");
                iconNoteType.setImageResource(R.drawable.ic_settings);
                
                // Style for system notes
                noteContainer.setBackgroundColor(Color.parseColor("#F5F5F5")); // Light gray
                textNoteType.setTextColor(Color.parseColor("#616161")); // Dark gray
                textNoteContent.setTypeface(null, android.graphics.Typeface.ITALIC);
            }
        }

        private String formatTimestamp(String timestamp) {
            if (timestamp == null || timestamp.isEmpty()) {
                return "";
            }
            
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                
                Date date = inputFormat.parse(timestamp);
                return date != null ? outputFormat.format(date) : timestamp;
            } catch (Exception e) {
                return timestamp;
            }
        }
    }
}