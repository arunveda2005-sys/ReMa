package com.example.receipematcher.ui.recipes;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.receipematcher.R;
import com.example.receipematcher.utils.FilterManager;
import com.example.receipematcher.utils.FilterManager.RecipeFilters;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

/**
 * Dialog for advanced recipe filtering options
 */
public class RecipeFilterDialog extends DialogFragment {
    
    public interface OnFiltersAppliedListener {
        void onFiltersApplied(RecipeFilters filters);
    }
    
    private OnFiltersAppliedListener listener;
    private FilterManager filterManager;
    private RecipeFilters currentFilters;
    
    // UI Components
    private RadioGroup radioGroupAvailability;
    private ChipGroup chipGroupCuisine;
    private ChipGroup chipGroupDietary;
    private Spinner spinnerCookingTime;
    private Spinner spinnerDifficulty;
    private CheckBox checkboxExpiringFirst;
    
    public static RecipeFilterDialog newInstance() {
        return new RecipeFilterDialog();
    }
    
    public void setOnFiltersAppliedListener(OnFiltersAppliedListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = requireContext();
        filterManager = new FilterManager(context);
        currentFilters = new RecipeFilters();
        // Copy current filters to avoid modifying original until applied
        copyFilters(filterManager.getCurrentFilters(), currentFilters);
        
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_recipe_filters, null);
        initializeViews(view);
        setupSpinners();
        loadCurrentFilters();
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        
        AlertDialog dialog = builder.create();
        
        // Set up button listeners
        MaterialButton btnApplyFilters = view.findViewById(R.id.btnApplyFilters);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        MaterialButton btnClearFilters = view.findViewById(R.id.btnClearFilters);
        
        btnApplyFilters.setOnClickListener(v -> {
            saveFiltersFromUI();
            if (listener != null) {
                listener.onFiltersApplied(currentFilters);
            }
            dismiss();
        });
        
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnClearFilters.setOnClickListener(v -> {
            clearAllFilters();
            loadCurrentFilters();
        });
        
        return dialog;
    }
    
    private void initializeViews(View view) {
        radioGroupAvailability = view.findViewById(R.id.radioGroupAvailability);
        chipGroupCuisine = view.findViewById(R.id.chipGroupCuisine);
        chipGroupDietary = view.findViewById(R.id.chipGroupDietary);
        spinnerCookingTime = view.findViewById(R.id.spinnerCookingTime);
        spinnerDifficulty = view.findViewById(R.id.spinnerDifficulty);
        checkboxExpiringFirst = view.findViewById(R.id.checkboxExpiringFirst);
    }
    
    private void setupSpinners() {
        // Cooking Time Spinner
        String[] timeOptions = new String[FilterManager.TimeFilter.values().length];
        for (int i = 0; i < FilterManager.TimeFilter.values().length; i++) {
            timeOptions[i] = FilterManager.TimeFilter.values()[i].displayName;
        }
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(requireContext(), 
            android.R.layout.simple_spinner_item, timeOptions);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCookingTime.setAdapter(timeAdapter);
        
        // Difficulty Spinner
        String[] difficultyOptions = new String[FilterManager.DifficultyFilter.values().length];
        for (int i = 0; i < FilterManager.DifficultyFilter.values().length; i++) {
            difficultyOptions[i] = FilterManager.DifficultyFilter.values()[i].displayName;
        }
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(requireContext(), 
            android.R.layout.simple_spinner_item, difficultyOptions);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(difficultyAdapter);
    }
    
    private void loadCurrentFilters() {
        // Load availability filter
        switch (currentFilters.availabilityFilter) {
            case ALL:
                radioGroupAvailability.check(R.id.radioAllRecipes);
                break;
            case CAN_MAKE_NOW:
                radioGroupAvailability.check(R.id.radioCanMakeNow);
                break;
            case ALMOST_READY:
                radioGroupAvailability.check(R.id.radioAlmostReady);
                break;
            case NEED_SHOPPING:
                radioGroupAvailability.check(R.id.radioNeedShopping);
                break;
        }
        
        // Load cuisine filters
        chipGroupCuisine.clearCheck();
        for (FilterManager.CuisineType cuisine : currentFilters.cuisineFilters) {
            Chip chip = getCuisineChip(cuisine);
            if (chip != null) {
                chip.setChecked(true);
            }
        }
        
        // Load dietary filters
        chipGroupDietary.clearCheck();
        for (FilterManager.DietaryRestriction dietary : currentFilters.dietaryFilters) {
            Chip chip = getDietaryChip(dietary);
            if (chip != null) {
                chip.setChecked(true);
            }
        }
        
        // Load time filter
        spinnerCookingTime.setSelection(currentFilters.timeFilter.ordinal());
        
        // Load difficulty filter
        spinnerDifficulty.setSelection(currentFilters.difficultyFilter.ordinal());
        
        // Load special options
        checkboxExpiringFirst.setChecked(currentFilters.useExpiringFirst);
    }
    
    private void saveFiltersFromUI() {
        // Save availability filter
        int checkedAvailabilityId = radioGroupAvailability.getCheckedRadioButtonId();
        if (checkedAvailabilityId == R.id.radioAllRecipes) {
            currentFilters.availabilityFilter = FilterManager.AvailabilityFilter.ALL;
        } else if (checkedAvailabilityId == R.id.radioCanMakeNow) {
            currentFilters.availabilityFilter = FilterManager.AvailabilityFilter.CAN_MAKE_NOW;
        } else if (checkedAvailabilityId == R.id.radioAlmostReady) {
            currentFilters.availabilityFilter = FilterManager.AvailabilityFilter.ALMOST_READY;
        } else if (checkedAvailabilityId == R.id.radioNeedShopping) {
            currentFilters.availabilityFilter = FilterManager.AvailabilityFilter.NEED_SHOPPING;
        }
        
        // Save cuisine filters
        currentFilters.cuisineFilters.clear();
        for (int i = 0; i < chipGroupCuisine.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupCuisine.getChildAt(i);
            if (chip.isChecked()) {
                FilterManager.CuisineType cuisine = getCuisineFromChip(chip);
                if (cuisine != null) {
                    currentFilters.cuisineFilters.add(cuisine);
                }
            }
        }
        
        // Save dietary filters
        currentFilters.dietaryFilters.clear();
        for (int i = 0; i < chipGroupDietary.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupDietary.getChildAt(i);
            if (chip.isChecked()) {
                FilterManager.DietaryRestriction dietary = getDietaryFromChip(chip);
                if (dietary != null) {
                    currentFilters.dietaryFilters.add(dietary);
                }
            }
        }
        
        // Save time filter
        int timeSelection = spinnerCookingTime.getSelectedItemPosition();
        if (timeSelection >= 0 && timeSelection < FilterManager.TimeFilter.values().length) {
            currentFilters.timeFilter = FilterManager.TimeFilter.values()[timeSelection];
        }
        
        // Save difficulty filter
        int difficultySelection = spinnerDifficulty.getSelectedItemPosition();
        if (difficultySelection >= 0 && difficultySelection < FilterManager.DifficultyFilter.values().length) {
            currentFilters.difficultyFilter = FilterManager.DifficultyFilter.values()[difficultySelection];
        }
        
        // Save special options
        currentFilters.useExpiringFirst = checkboxExpiringFirst.isChecked();
        
        // Update filter manager
        filterManager.updateFilters(currentFilters);
    }
    
    private void clearAllFilters() {
        currentFilters = new RecipeFilters();
        filterManager.clearAllFilters();
    }
    
    private Chip getCuisineChip(FilterManager.CuisineType cuisine) {
        switch (cuisine) {
            case ITALIAN: return getView().findViewById(R.id.chipItalian);
            case ASIAN: return getView().findViewById(R.id.chipAsian);
            case MEXICAN: return getView().findViewById(R.id.chipMexican);
            case AMERICAN: return getView().findViewById(R.id.chipAmerican);
            case MEDITERRANEAN: return getView().findViewById(R.id.chipMediterranean);
            case INDIAN: return getView().findViewById(R.id.chipIndian);
            default: return null;
        }
    }
    
    private FilterManager.CuisineType getCuisineFromChip(Chip chip) {
        int id = chip.getId();
        if (id == R.id.chipItalian) return FilterManager.CuisineType.ITALIAN;
        if (id == R.id.chipAsian) return FilterManager.CuisineType.ASIAN;
        if (id == R.id.chipMexican) return FilterManager.CuisineType.MEXICAN;
        if (id == R.id.chipAmerican) return FilterManager.CuisineType.AMERICAN;
        if (id == R.id.chipMediterranean) return FilterManager.CuisineType.MEDITERRANEAN;
        if (id == R.id.chipIndian) return FilterManager.CuisineType.INDIAN;
        return null;
    }
    
    private Chip getDietaryChip(FilterManager.DietaryRestriction dietary) {
        switch (dietary) {
            case VEGETARIAN: return getView().findViewById(R.id.chipVegetarian);
            case VEGAN: return getView().findViewById(R.id.chipVegan);
            case GLUTEN_FREE: return getView().findViewById(R.id.chipGlutenFree);
            case DAIRY_FREE: return getView().findViewById(R.id.chipDairyFree);
            case LOW_CARB: return getView().findViewById(R.id.chipLowCarb);
            case HEALTHY: return getView().findViewById(R.id.chipHealthy);
            default: return null;
        }
    }
    
    private FilterManager.DietaryRestriction getDietaryFromChip(Chip chip) {
        int id = chip.getId();
        if (id == R.id.chipVegetarian) return FilterManager.DietaryRestriction.VEGETARIAN;
        if (id == R.id.chipVegan) return FilterManager.DietaryRestriction.VEGAN;
        if (id == R.id.chipGlutenFree) return FilterManager.DietaryRestriction.GLUTEN_FREE;
        if (id == R.id.chipDairyFree) return FilterManager.DietaryRestriction.DAIRY_FREE;
        if (id == R.id.chipLowCarb) return FilterManager.DietaryRestriction.LOW_CARB;
        if (id == R.id.chipHealthy) return FilterManager.DietaryRestriction.HEALTHY;
        return null;
    }
    
    private void copyFilters(RecipeFilters source, RecipeFilters destination) {
        destination.availabilityFilter = source.availabilityFilter;
        destination.cuisineFilters.clear();
        destination.cuisineFilters.addAll(source.cuisineFilters);
        destination.dietaryFilters.clear();
        destination.dietaryFilters.addAll(source.dietaryFilters);
        destination.timeFilter = source.timeFilter;
        destination.difficultyFilter = source.difficultyFilter;
        destination.useExpiringFirst = source.useExpiringFirst;
        destination.avoidIngredients.clear();
        destination.avoidIngredients.addAll(source.avoidIngredients);
    }
}