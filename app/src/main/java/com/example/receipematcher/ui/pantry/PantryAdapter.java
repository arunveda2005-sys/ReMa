package com.example.receipematcher.ui.pantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.button.MaterialButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.receipematcher.R;
import com.example.receipematcher.data.entities.Ingredient;
import com.example.receipematcher.utils.IngredientStatusCalculator;

public class PantryAdapter extends ListAdapter<Ingredient, PantryAdapter.PantryViewHolder> {

    private OnItemClickListener editClickListener;
    private OnItemClickListener deleteClickListener;
    private OnItemClickListener addToShoppingClickListener;
    private OnItemClickListener useInRecipeClickListener;
    private boolean isSelectionMode = false;
    private BatchSelectionManager batchSelectionManager;

    protected PantryAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Ingredient> DIFF_CALLBACK = new DiffUtil.ItemCallback<Ingredient>() {
        @Override
        public boolean areItemsTheSame(@NonNull Ingredient oldItem, @NonNull Ingredient newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Ingredient oldItem, @NonNull Ingredient newItem) {
            return oldItem.name.equals(newItem.name)
                    && oldItem.quantity == newItem.quantity
                    && oldItem.expiryDate.equals(newItem.expiryDate);
        }
    };

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        Ingredient ingredient = getItem(position);
        holder.nameText.setText(ingredient.name);
        String quantityText = String.format("%.1f", ingredient.quantity);
        if (quantityText.endsWith(".0")) {
            quantityText = quantityText.substring(0, quantityText.length() - 2);
        }
        holder.quantityText.setText(quantityText + " " + (ingredient.unit != null ? ingredient.unit : "pcs"));
        
        // Apply color coding based on ingredient status
        IngredientStatusCalculator.IngredientStatus status = IngredientStatusCalculator.calculateStatus(ingredient);
        Long daysUntilExpiry = IngredientStatusCalculator.getDaysUntilExpiry(ingredient);
        
        // Set status indicator color
        int statusColor = ContextCompat.getColor(holder.itemView.getContext(), 
            IngredientStatusCalculator.getStatusColorResource(status));
        holder.expiryIndicator.setBackgroundColor(statusColor);
        
        // Update expiry text with status information
        String statusDescription = IngredientStatusCalculator.getStatusDescription(status, daysUntilExpiry);
        holder.expiryText.setText(statusDescription);
        
        // Set text color based on status
        int textColor;
        switch (status) {
            case EXPIRED:
                textColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.error);
                break;
            case EXPIRING:
                textColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.warning);
                break;
            case FRESH:
                textColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.success);
                break;
            default:
                textColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary);
                break;
        }
        holder.expiryText.setTextColor(textColor);

        // Handle selection mode
        if (isSelectionMode) {
            holder.checkboxSelect.setVisibility(View.VISIBLE);
            holder.quickActionsLayout.setVisibility(View.GONE);
            holder.checkboxSelect.setChecked(batchSelectionManager != null && batchSelectionManager.isSelected(position));
        } else {
            holder.checkboxSelect.setVisibility(View.GONE);
            holder.quickActionsLayout.setVisibility(View.VISIBLE);
        }

        // Main action buttons
        holder.editButton.setOnClickListener(v -> {
            if (editClickListener != null) editClickListener.onItemClick(ingredient);
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteClickListener != null) deleteClickListener.onItemClick(ingredient);
        });

        // Quick action buttons
        holder.btnQuickEdit.setOnClickListener(v -> {
            if (editClickListener != null) editClickListener.onItemClick(ingredient);
        });

        holder.btnQuickShop.setOnClickListener(v -> {
            if (addToShoppingClickListener != null) addToShoppingClickListener.onItemClick(ingredient);
        });

        holder.btnQuickRecipe.setOnClickListener(v -> {
            if (useInRecipeClickListener != null) useInRecipeClickListener.onItemClick(ingredient);
        });

        // Card click handling
        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode && batchSelectionManager != null) {
                batchSelectionManager.toggleSelection(position);
                holder.checkboxSelect.setChecked(batchSelectionManager.isSelected(position));
            } else {
                // Toggle quick actions visibility
                if (holder.quickActionsLayout.getVisibility() == View.VISIBLE) {
                    holder.quickActionsLayout.setVisibility(View.GONE);
                } else {
                    holder.quickActionsLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        // Checkbox click handling
        holder.checkboxSelect.setOnClickListener(v -> {
            if (isSelectionMode && batchSelectionManager != null) {
                batchSelectionManager.toggleSelection(position);
            }
        });

        // Long click to enter selection mode
        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode && batchSelectionManager != null) {
                batchSelectionManager.enterSelectionMode();
                batchSelectionManager.toggleSelection(position);
                return true;
            }
            return false;
        });
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, quantityText, expiryText;
        MaterialButton editButton, deleteButton;
        MaterialButton btnQuickEdit, btnQuickShop, btnQuickRecipe;
        View expiryIndicator, quickActionsLayout;
        android.widget.CheckBox checkboxSelect;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textName);
            quantityText = itemView.findViewById(R.id.textQuantity);
            expiryText = itemView.findViewById(R.id.textExpiry);
            editButton = itemView.findViewById(R.id.btnEdit);
            deleteButton = itemView.findViewById(R.id.btnDelete);
            expiryIndicator = itemView.findViewById(R.id.expiryIndicator);
            
            // Quick action buttons
            btnQuickEdit = itemView.findViewById(R.id.btnQuickEdit);
            btnQuickShop = itemView.findViewById(R.id.btnQuickShop);
            btnQuickRecipe = itemView.findViewById(R.id.btnQuickRecipe);
            quickActionsLayout = itemView.findViewById(R.id.quickActionsLayout);
            
            // Selection checkbox
            checkboxSelect = itemView.findViewById(R.id.checkboxSelect);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Ingredient ingredient);
    }

    public void setOnEditClickListener(OnItemClickListener listener) {
        this.editClickListener = listener;
    }

    public void setOnDeleteClickListener(OnItemClickListener listener) {
        this.deleteClickListener = listener;
    }

    public void setOnAddToShoppingClickListener(OnItemClickListener listener) {
        this.addToShoppingClickListener = listener;
    }

    public void setOnUseInRecipeClickListener(OnItemClickListener listener) {
        this.useInRecipeClickListener = listener;
    }

    public void setSelectionMode(boolean selectionMode) {
        this.isSelectionMode = selectionMode;
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public void setBatchSelectionManager(BatchSelectionManager batchSelectionManager) {
        this.batchSelectionManager = batchSelectionManager;
    }
}
