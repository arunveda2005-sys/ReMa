package com.example.receipematcher.ui.pantry;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.receipematcher.R;
import com.example.receipematcher.data.entities.Ingredient;
import com.example.receipematcher.utils.IngredientStatusCalculator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddIngredientDialog {

    public interface OnIngredientSavedListener {
        void onSaved(Ingredient ingredient);
    }

    private Context context;
    private Ingredient ingredient; // null if adding new
    private OnIngredientSavedListener listener;

    public AddIngredientDialog(@NonNull Context context, OnIngredientSavedListener listener) {
        this(context, null, listener);
    }

    public AddIngredientDialog(@NonNull Context context, Ingredient ingredient, OnIngredientSavedListener listener) {
        this.context = context;
        this.ingredient = ingredient;
        this.listener = listener;
    }

    public void show() {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_ingredient, null);
        
        // Initialize views
        TextView dialogTitle = view.findViewById(R.id.dialogTitle);
        AutoCompleteTextView editName = view.findViewById(R.id.editIngredientName);
        TextInputEditText editQuantity = view.findViewById(R.id.editIngredientQuantity);
        AutoCompleteTextView editUnit = view.findViewById(R.id.editIngredientUnit);
        TextInputEditText editExpiry = view.findViewById(R.id.editIngredientExpiry);
        RecyclerView recyclerSuggestions = view.findViewById(R.id.recyclerSuggestions);
        View quickDateSuggestions = view.findViewById(R.id.quickDateSuggestions);
        View previewCard = view.findViewById(R.id.previewCard);
        Button btnSave = view.findViewById(R.id.btnSaveIngredient);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // Set dialog title
        dialogTitle.setText(ingredient != null ? "Edit Ingredient" : "Add Ingredient");

        // Setup suggestions adapter
        SuggestionAdapter suggestionAdapter = new SuggestionAdapter(suggestion -> {
            editName.setText(suggestion);
            recyclerSuggestions.setVisibility(View.GONE);
            updateSmartPredictions(editName, editQuantity, editUnit, editExpiry, view);
        });
        
        recyclerSuggestions.setLayoutManager(new LinearLayoutManager(context));
        recyclerSuggestions.setAdapter(suggestionAdapter);

        // Setup unit dropdown
        // Simple unit suggestions
        String[] units = {"pcs", "kg", "g", "ml", "l", "cups", "tbsp", "tsp"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(context, 
            android.R.layout.simple_dropdown_item_1line, units);
        editUnit.setAdapter(unitAdapter);

        // Pre-fill fields if editing
        if (ingredient != null) {
            editName.setText(ingredient.name);
            editQuantity.setText(String.valueOf(ingredient.quantity));
            editUnit.setText(ingredient.unit != null ? ingredient.unit : "");
            editExpiry.setText(ingredient.expiryDate);
            updatePreview(view, ingredient.name, String.valueOf(ingredient.quantity), 
                         ingredient.unit, ingredient.expiryDate);
        }

        // Setup name input with suggestions
        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();
                if (input.length() > 0) {
                    // Simple suggestions (removed complex engine)
                    List<String> suggestions = new ArrayList<>();
                    if (!suggestions.isEmpty()) {
                        suggestionAdapter.updateSuggestions(suggestions);
                        recyclerSuggestions.setVisibility(View.VISIBLE);
                    } else {
                        recyclerSuggestions.setVisibility(View.GONE);
                    }
                } else {
                    recyclerSuggestions.setVisibility(View.GONE);
                }
                
                // Update smart predictions when name changes
                updateSmartPredictions(editName, editQuantity, editUnit, editExpiry, view);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup quantity and unit change listeners for preview updates
        TextWatcher previewUpdater = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview(view, editName.getText().toString(), 
                            editQuantity.getText().toString(),
                            editUnit.getText().toString(),
                            editExpiry.getText().toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        
        editQuantity.addTextChangedListener(previewUpdater);
        editUnit.addTextChangedListener(previewUpdater);

        // Setup date picker and quick suggestions
        setupDatePicker(editExpiry, view);
        setupQuickDateSuggestions(view, editExpiry);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(true)
                .create();

        // Save button click
        btnSave.setOnClickListener(v -> {
            if (validateAndSave(editName, editQuantity, editUnit, editExpiry, dialog)) {
                dialog.dismiss();
            }
        });

        // Cancel button click
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateSmartPredictions(AutoCompleteTextView editName, TextInputEditText editQuantity, 
                                      AutoCompleteTextView editUnit, TextInputEditText editExpiry, View view) {
        String name = editName.getText().toString().trim();
        if (!name.isEmpty() && ingredient == null) { // Only predict for new ingredients
            // Simple defaults (prediction removed)
            if (editQuantity.getText().toString().trim().isEmpty()) {
                editQuantity.setText("1");
            }
            
            if (editUnit.getText().toString().trim().isEmpty()) {
                editUnit.setText("pcs");
            }
            
            if (editExpiry.getText().toString().trim().isEmpty()) {
                // Default to 7 days from now
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 7);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                editExpiry.setText(dateFormat.format(cal.getTime()));
            }
        }
        
        // Update preview
        updatePreview(view, name, editQuantity.getText().toString(), 
                     editUnit.getText().toString(), editExpiry.getText().toString());
    }

    private void setupDatePicker(TextInputEditText editExpiry, View view) {
        editExpiry.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(context,
                    (DatePicker view1, int year, int month, int dayOfMonth) -> {
                        String dateStr = dayOfMonth + "/" + (month + 1) + "/" + year;
                        editExpiry.setText(dateStr);
                        updatePreview(view, 
                                    ((AutoCompleteTextView) view.findViewById(R.id.editIngredientName)).getText().toString(),
                                    ((TextInputEditText) view.findViewById(R.id.editIngredientQuantity)).getText().toString(),
                                    ((AutoCompleteTextView) view.findViewById(R.id.editIngredientUnit)).getText().toString(),
                                    dateStr);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });
    }

    private void setupQuickDateSuggestions(View view, TextInputEditText editExpiry) {
        View quickDateSuggestions = view.findViewById(R.id.quickDateSuggestions);
        Chip chipToday = view.findViewById(R.id.chipToday);
        Chip chipWeek = view.findViewById(R.id.chipWeek);
        Chip chipMonth = view.findViewById(R.id.chipMonth);

        // Simple date suggestions
        Map<String, Date> suggestions = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 3);
        suggestions.put("3 days", cal.getTime());
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        suggestions.put("1 week", cal.getTime());
        cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        suggestions.put("1 month", cal.getTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        chipToday.setOnClickListener(v -> {
            editExpiry.setText(dateFormat.format(suggestions.get("Today")));
            updatePreview(view, 
                        ((AutoCompleteTextView) view.findViewById(R.id.editIngredientName)).getText().toString(),
                        ((TextInputEditText) view.findViewById(R.id.editIngredientQuantity)).getText().toString(),
                        ((AutoCompleteTextView) view.findViewById(R.id.editIngredientUnit)).getText().toString(),
                        editExpiry.getText().toString());
        });

        chipWeek.setOnClickListener(v -> {
            editExpiry.setText(dateFormat.format(suggestions.get("1 Week")));
            updatePreview(view, 
                        ((AutoCompleteTextView) view.findViewById(R.id.editIngredientName)).getText().toString(),
                        ((TextInputEditText) view.findViewById(R.id.editIngredientQuantity)).getText().toString(),
                        ((AutoCompleteTextView) view.findViewById(R.id.editIngredientUnit)).getText().toString(),
                        editExpiry.getText().toString());
        });

        chipMonth.setOnClickListener(v -> {
            editExpiry.setText(dateFormat.format(suggestions.get("1 Month")));
            updatePreview(view, 
                        ((AutoCompleteTextView) view.findViewById(R.id.editIngredientName)).getText().toString(),
                        ((TextInputEditText) view.findViewById(R.id.editIngredientQuantity)).getText().toString(),
                        ((AutoCompleteTextView) view.findViewById(R.id.editIngredientUnit)).getText().toString(),
                        editExpiry.getText().toString());
        });

        quickDateSuggestions.setVisibility(View.VISIBLE);
    }

    private void updatePreview(View view, String name, String quantity, String unit, String expiry) {
        View previewCard = view.findViewById(R.id.previewCard);
        TextView previewName = view.findViewById(R.id.previewName);
        TextView previewDetails = view.findViewById(R.id.previewDetails);
        View previewStatusIndicator = view.findViewById(R.id.previewStatusIndicator);

        if (!name.trim().isEmpty()) {
            previewCard.setVisibility(View.VISIBLE);
            previewName.setText(name);
            
            String details = "";
            if (!quantity.trim().isEmpty()) {
                details += quantity;
                if (!unit.trim().isEmpty()) {
                    details += " " + unit;
                }
            }
            if (!expiry.trim().isEmpty()) {
                if (!details.isEmpty()) {
                    details += " • ";
                }
                details += expiry;
            }
            previewDetails.setText(details);

            // Update status indicator based on expiry
            if (!expiry.trim().isEmpty()) {
                Ingredient tempIngredient = new Ingredient();
                tempIngredient.name = name;
                tempIngredient.expiryDate = expiry;
                
                IngredientStatusCalculator.IngredientStatus status = 
                    IngredientStatusCalculator.calculateStatus(tempIngredient);
                int statusColor = ContextCompat.getColor(context, 
                    IngredientStatusCalculator.getStatusColorResource(status));
                previewStatusIndicator.setBackgroundColor(statusColor);
            }
        } else {
            previewCard.setVisibility(View.GONE);
        }
    }

    private boolean validateAndSave(AutoCompleteTextView editName, TextInputEditText editQuantity, 
                                  AutoCompleteTextView editUnit, TextInputEditText editExpiry, 
                                  AlertDialog dialog) {
        String name = editName.getText().toString().trim();
        String quantityStr = editQuantity.getText().toString().trim();
        String unit = editUnit.getText().toString().trim();
        String expiry = editExpiry.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(quantityStr) || TextUtils.isEmpty(expiry)) {
            Toast.makeText(context, "Name, quantity, and expiry date are required", Toast.LENGTH_SHORT).show();
            return false;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(quantityStr);
            if (quantity <= 0) {
                Toast.makeText(context, "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Quantity must be a valid number", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Update existing ingredient or create new
        Ingredient newIngredient = ingredient != null ? ingredient : new Ingredient();
        newIngredient.name = name;
        newIngredient.quantity = quantity;
        newIngredient.unit = unit.isEmpty() ? "pcs" : unit;
        newIngredient.expiryDate = expiry;

        listener.onSaved(newIngredient);
        return true;
    }
}
