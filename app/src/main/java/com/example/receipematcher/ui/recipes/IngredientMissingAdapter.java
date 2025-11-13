package com.example.receipematcher.ui.recipes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.receipematcher.R;
// Removed IngredientSubstitutionEngine import
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying missing ingredients with substitution suggestions
 */
public class IngredientMissingAdapter extends RecyclerView.Adapter<IngredientMissingAdapter.ViewHolder> {
    
    private List<String> ingredients = new ArrayList<>();
    private OnSubstitutionListener substitutionListener;
    
    public interface OnSubstitutionListener {
        void onSubstitutionSelected(String originalIngredient, String substitute);
    }
    
    public void setIngredients(List<String> ingredients) {
        this.ingredients = new ArrayList<>(ingredients);
        notifyDataSetChanged();
    }
    
    public void setOnSubstitutionListener(OnSubstitutionListener listener) {
        this.substitutionListener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient_missing, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String ingredient = ingredients.get(position);
        holder.bind(ingredient, substitutionListener);
    }
    
    @Override
    public int getItemCount() {
        return ingredients.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView quantityText;
        private final MaterialButton substituteButton;
        private final LinearLayout substitutionsLayout;
        private final RecyclerView substitutionsRecycler;
        
        // Substitution adapter removed for simplicity
        private boolean substitutionsVisible = false;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textIngredientName);
            quantityText = itemView.findViewById(R.id.textIngredientQuantity);
            substituteButton = itemView.findViewById(R.id.btnSubstitute);
            substitutionsLayout = itemView.findViewById(R.id.layoutSubstitutions);
            substitutionsRecycler = itemView.findViewById(R.id.recyclerSubstitutions);
            
            setupSubstitutionsRecycler();
        }
        
        private void setupSubstitutionsRecycler() {
            // Substitution adapter initialization removed
            substitutionsRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            // No adapter when substitutions feature is disabled
        }
        
        public void bind(String ingredient, OnSubstitutionListener listener) {
            // Parse ingredient to separate name and quantity
            String[] parts = parseIngredient(ingredient);
            String quantity = parts[0];
            String name = parts[1];
            
            nameText.setText(name);
            quantityText.setText(quantity);
            
            // Check if substitutions are available
            // Substitution engine removed - no substitutions available
            List<String> substitutions = new ArrayList<>();
            
            if (substitutions.isEmpty()) {
                substituteButton.setVisibility(View.GONE);
                substitutionsLayout.setVisibility(View.GONE);
            } else {
                substituteButton.setVisibility(View.VISIBLE);
                substituteButton.setOnClickListener(v -> toggleSubstitutions(substitutions, ingredient, listener));
            }
            
            // Reset visibility state
            substitutionsVisible = false;
            substitutionsLayout.setVisibility(View.GONE);
        }
        
        private void toggleSubstitutions(List<String> substitutions, 
                                       String originalIngredient, OnSubstitutionListener listener) {
            if (substitutionsVisible) {
                // Hide substitutions
                substitutionsLayout.setVisibility(View.GONE);
                substituteButton.setText("Substitute");
                substitutionsVisible = false;
            } else {
                // Show substitutions
                substitutionsLayout.setVisibility(View.VISIBLE);
                substituteButton.setText("Hide");
                substitutionsVisible = true;
            }
        }
        
        private String[] parseIngredient(String ingredient) {
            if (ingredient == null || ingredient.trim().isEmpty()) {
                return new String[]{"", ""};
            }
            
            // Simple parsing - look for quantity at the beginning
            String[] words = ingredient.trim().split("\\s+", 3);
            if (words.length >= 2) {
                // Check if first word looks like a quantity
                if (words[0].matches("\\d+(?:\\.\\d+)?(?:/\\d+)?")) {
                    String quantity = words[0];
                    if (words.length >= 2 && isUnit(words[1])) {
                        quantity += " " + words[1];
                        String name = words.length > 2 ? words[2] : "";
                        return new String[]{quantity, name};
                    } else {
                        String name = ingredient.substring(words[0].length()).trim();
                        return new String[]{quantity, name};
                    }
                }
            }
            
            // If no quantity found, treat entire string as name
            return new String[]{"", ingredient};
        }
        
        private boolean isUnit(String word) {
            String[] units = {"cup", "cups", "tbsp", "tsp", "oz", "lb", "lbs", "g", "kg", "ml", "l"};
            String lowerWord = word.toLowerCase();
            for (String unit : units) {
                if (lowerWord.equals(unit) || lowerWord.equals(unit + "s")) {
                    return true;
                }
            }
            return false;
        }
    }
}