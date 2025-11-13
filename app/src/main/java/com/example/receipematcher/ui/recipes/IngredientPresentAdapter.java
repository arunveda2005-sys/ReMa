package com.example.receipematcher.ui.recipes;

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
 * Adapter for displaying ingredients that the user has available
 */
public class IngredientPresentAdapter extends RecyclerView.Adapter<IngredientPresentAdapter.ViewHolder> {
    
    private List<String> ingredients = new ArrayList<>();
    
    public void setIngredients(List<String> ingredients) {
        this.ingredients = new ArrayList<>(ingredients);
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient_present, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String ingredient = ingredients.get(position);
        holder.bind(ingredient);
    }
    
    @Override
    public int getItemCount() {
        return ingredients.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView quantityText;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textIngredientName);
            quantityText = itemView.findViewById(R.id.textIngredientQuantity);
        }
        
        public void bind(String ingredient) {
            // Parse ingredient to separate name and quantity
            String[] parts = parseIngredient(ingredient);
            String quantity = parts[0];
            String name = parts[1];
            
            nameText.setText(name);
            quantityText.setText(quantity);
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