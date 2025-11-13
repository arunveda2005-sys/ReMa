package com.example.receipematcher.ui.pantry;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.receipematcher.R;
import com.example.receipematcher.data.entities.Ingredient;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages batch selection mode for pantry items
 */
public class BatchSelectionManager {

    public interface BatchActionListener {
        void onBatchEdit(List<Ingredient> selectedItems);
        void onBatchDelete(List<Ingredient> selectedItems);
        void onBatchAddToShopping(List<Ingredient> selectedItems);
        void onSelectionModeChanged(boolean isSelectionMode);
    }

    private final View batchToolbar;
    private final TextView selectedCountText;
    private final MaterialButton btnBatchEdit;
    private final MaterialButton btnBatchShop;
    private final MaterialButton btnBatchDelete;
    private final MaterialButton btnBatchCancel;
    private final PantryAdapter adapter;
    private final BatchActionListener listener;

    private boolean isSelectionMode = false;
    private final Set<Integer> selectedPositions = new HashSet<>();

    public BatchSelectionManager(View batchToolbar, PantryAdapter adapter, BatchActionListener listener) {
        this.batchToolbar = batchToolbar;
        this.adapter = adapter;
        this.listener = listener;

        // Initialize views
        selectedCountText = batchToolbar.findViewById(R.id.textSelectedCount);
        btnBatchEdit = batchToolbar.findViewById(R.id.btnBatchEdit);
        btnBatchShop = batchToolbar.findViewById(R.id.btnBatchShop);
        btnBatchDelete = batchToolbar.findViewById(R.id.btnBatchDelete);
        btnBatchCancel = batchToolbar.findViewById(R.id.btnBatchCancel);

        setupClickListeners();
    }

    private void setupClickListeners() {
        btnBatchEdit.setOnClickListener(v -> {
            List<Ingredient> selectedItems = getSelectedIngredients();
            if (!selectedItems.isEmpty()) {
                listener.onBatchEdit(selectedItems);
            }
        });

        btnBatchShop.setOnClickListener(v -> {
            List<Ingredient> selectedItems = getSelectedIngredients();
            if (!selectedItems.isEmpty()) {
                listener.onBatchAddToShopping(selectedItems);
            }
        });

        btnBatchDelete.setOnClickListener(v -> {
            List<Ingredient> selectedItems = getSelectedIngredients();
            if (!selectedItems.isEmpty()) {
                listener.onBatchDelete(selectedItems);
            }
        });

        btnBatchCancel.setOnClickListener(v -> exitSelectionMode());
    }

    public void enterSelectionMode() {
        if (!isSelectionMode) {
            isSelectionMode = true;
            selectedPositions.clear();
            adapter.setSelectionMode(true);
            showBatchToolbar();
            updateSelectedCount();
            listener.onSelectionModeChanged(true);
        }
    }

    public void exitSelectionMode() {
        if (isSelectionMode) {
            isSelectionMode = false;
            selectedPositions.clear();
            adapter.setSelectionMode(false);
            hideBatchToolbar();
            listener.onSelectionModeChanged(false);
        }
    }

    public void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
        updateSelectedCount();
        updateButtonStates();
    }

    public boolean isSelected(int position) {
        return selectedPositions.contains(position);
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public int getSelectedCount() {
        return selectedPositions.size();
    }

    private List<Ingredient> getSelectedIngredients() {
        List<Ingredient> selectedItems = new ArrayList<>();
        List<Ingredient> allItems = adapter.getCurrentList();
        
        for (int position : selectedPositions) {
            if (position < allItems.size()) {
                selectedItems.add(allItems.get(position));
            }
        }
        return selectedItems;
    }

    private void updateSelectedCount() {
        int count = selectedPositions.size();
        String text = count + " selected";
        selectedCountText.setText(text);
    }

    private void updateButtonStates() {
        boolean hasSelection = !selectedPositions.isEmpty();
        btnBatchEdit.setEnabled(hasSelection);
        btnBatchShop.setEnabled(hasSelection);
        btnBatchDelete.setEnabled(hasSelection);
    }

    private void showBatchToolbar() {
        if (batchToolbar.getVisibility() != View.VISIBLE) {
            batchToolbar.setVisibility(View.VISIBLE);
            Animation slideDown = AnimationUtils.loadAnimation(batchToolbar.getContext(), R.anim.slide_down);
            batchToolbar.startAnimation(slideDown);
        }
    }

    private void hideBatchToolbar() {
        if (batchToolbar.getVisibility() == View.VISIBLE) {
            Animation slideUp = AnimationUtils.loadAnimation(batchToolbar.getContext(), R.anim.slide_up);
            slideUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    batchToolbar.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            batchToolbar.startAnimation(slideUp);
        }
    }

    public void clearSelection() {
        selectedPositions.clear();
        updateSelectedCount();
        updateButtonStates();
    }
}