package com.example.receipematcher.ui.pantry;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import androidx.appcompat.widget.SearchView;
import android.widget.Spinner;
import android.widget.AutoCompleteTextView;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.receipematcher.R;
import com.example.receipematcher.data.entities.Ingredient;
// Removed NavigationTransitions import
import com.example.receipematcher.viewmodel.PantryViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PantryFragment extends Fragment {

    private PantryViewModel pantryViewModel;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Transitions removed for simplicity
    }
    private PantryAdapter adapter;
    private List<Ingredient> allIngredients = new ArrayList<>();
    private String currentQuery = "";
    private int currentSortOption = 0; // 0 = expiry, 1 = name, 2 = quantity

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pantry, container, false);

        // RecyclerView setup
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewPantry);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PantryAdapter();
        recyclerView.setAdapter(adapter);

        // ViewModel
        pantryViewModel = new ViewModelProvider(this).get(PantryViewModel.class);

        // Observe all ingredients
        pantryViewModel.getAllIngredients().observe(getViewLifecycleOwner(), ingredients -> {
            allIngredients.clear();
            allIngredients.addAll(ingredients);
            applySortAndFilter();
        });

        // Spinner setup (sorting)
        Spinner spinnerSort = view.findViewById(R.id.spinnerSort);
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSortOption = position;
                applySortAndFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // SearchView setup (filtering)
        SearchView searchView = view.findViewById(R.id.searchViewPantry);
        AutoCompleteTextView searchText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchText != null) {
            searchText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            int vpad = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
            searchText.setPadding(searchText.getPaddingLeft(), vpad, searchText.getPaddingRight(), vpad);
            searchText.setMinHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics()));
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query.toLowerCase().trim();
                applySortAndFilter();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText.toLowerCase().trim();
                applySortAndFilter();
                return true;
            }
        });

        // Handle delete click
        adapter.setOnDeleteClickListener(ingredient -> pantryViewModel.delete(ingredient));

        // Handle edit click
        adapter.setOnEditClickListener(ingredient -> {
            AddIngredientDialog dialog = new AddIngredientDialog(requireContext(), ingredient, updatedIngredient -> {
                pantryViewModel.update(updatedIngredient);
            });
            dialog.show();
        });

        // Floating Action Button
        FloatingActionButton fab = view.findViewById(R.id.fabAddIngredient);
        fab.setOnClickListener(v -> {
            AddIngredientDialog dialog = new AddIngredientDialog(requireContext(), newIngredient -> {
                pantryViewModel.insert(newIngredient);
            });
            dialog.show();
        });

        return view;
    }

    // Apply sorting and filtering
    private void applySortAndFilter() {
        List<Ingredient> filtered = new ArrayList<>();
        for (Ingredient ingredient : allIngredients) {
            if (ingredient.name.toLowerCase().contains(currentQuery)) {
                filtered.add(ingredient);
            }
        }

        // Sorting
        switch (currentSortOption) {
            case 0: // expiryDate ascending; push null/empty to end
                Collections.sort(filtered, (a, b) -> {
                    String ea = a.expiryDate == null ? "" : a.expiryDate;
                    String eb = b.expiryDate == null ? "" : b.expiryDate;
                    boolean aEmpty = ea.isEmpty();
                    boolean bEmpty = eb.isEmpty();
                    if (aEmpty && bEmpty) return 0;
                    if (aEmpty) return 1; // a to end
                    if (bEmpty) return -1; // b to end
                    return ea.compareTo(eb); // ISO yyyy-MM-dd lexicographic works
                });
                break;
            case 1: // name ascending
                Collections.sort(filtered, Comparator.comparing(i -> i.name));
                break;
            case 2: // quantity descending
                Collections.sort(filtered, (a, b) -> Double.compare(b.quantity, a.quantity));
                break;
        }

        adapter.submitList(filtered);
    }
}
