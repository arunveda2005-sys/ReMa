package com.example.receipematcher.ui.pantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.receipematcher.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying ingredient suggestions
 */
public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder> {

    public interface OnSuggestionClickListener {
        void onSuggestionClick(String suggestion);
    }

    private List<String> suggestions = new ArrayList<>();
    private OnSuggestionClickListener listener;

    public SuggestionAdapter(OnSuggestionClickListener listener) {
        this.listener = listener;
    }

    public void updateSuggestions(List<String> newSuggestions) {
        this.suggestions.clear();
        this.suggestions.addAll(newSuggestions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        String suggestion = suggestions.get(position);
        holder.textSuggestion.setText(suggestion);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSuggestionClick(suggestion);
            }
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView textSuggestion;

        SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            textSuggestion = itemView.findViewById(R.id.textSuggestion);
        }
    }
}