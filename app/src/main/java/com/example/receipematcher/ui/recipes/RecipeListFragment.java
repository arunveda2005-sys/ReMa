package com.example.receipematcher.ui.recipes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.receipematcher.R;
import com.example.receipematcher.data.entities.Ingredient;
import com.example.receipematcher.viewmodel.PantryViewModel;
import com.example.receipematcher.viewmodel.FavouriteViewModel;
import com.example.receipematcher.viewmodel.RecipeViewModel;
import com.example.receipematcher.data.repository.RecipeRepository.AiRecipe;
import com.example.receipematcher.workers.RecipeImportWorker;
// Removed NavigationTransitions import
import com.example.receipematcher.utils.FilterManager;
// Removed SmartRecipeEngine import
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import android.widget.HorizontalScrollView;

import java.util.ArrayList;
import java.util.List;

public class RecipeListFragment extends Fragment {

    private RecipeViewModel recipeViewModel;
    private PantryViewModel pantryViewModel;
    private FavouriteViewModel favouriteViewModel;
    private AiRecipeAdapter adapter;
    private final List<AiRecipe> aiRecipes = new ArrayList<>();
    private final List<AiRecipe> filtered = new ArrayList<>();
    private String currentQuery = "";
    private Snackbar importSnackbar;
    private List<String> pantryTerms = new ArrayList<>();
    private List<Ingredient> pantryIngredients = new ArrayList<>();
    private FilterManager filterManager;
    private MaterialButton btnFilter;
    private HorizontalScrollView scrollViewActiveFilters;
    private ChipGroup chipGroupActiveFilters;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Transitions removed for simplicity
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);

        // Initialize filter manager
        filterManager = new FilterManager(requireContext());
        
        // Initialize UI components
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewRecipes);
        btnFilter = view.findViewById(R.id.btnFilter);
        scrollViewActiveFilters = view.findViewById(R.id.scrollViewActiveFilters);
        chipGroupActiveFilters = view.findViewById(R.id.chipGroupActiveFilters);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AiRecipeAdapter();
        adapter.setOnItemClickListener(recipe -> navigateToDetail(recipe));
        adapter.setOnFavoriteToggleListener((recipe, toFavorite) -> {
            String rid = recipe == null || recipe.name == null ? "" : recipe.name.trim().toLowerCase();
            if (toFavorite) {
                favouriteViewModel.add(rid, recipe.ingredients, recipe.steps);
            } else {
                favouriteViewModel.remove(rid);
            }
        });
        recyclerView.setAdapter(adapter);
        
        // Set up filter button
        btnFilter.setOnClickListener(v -> showFilterDialog());
        
        // Update active filters display
        updateActiveFiltersDisplay();

        SearchView searchView = view.findViewById(R.id.searchViewRecipes);
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    currentQuery = query == null ? "" : query.trim();
                    applyFilter();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    currentQuery = newText == null ? "" : newText.trim();
                    applyFilter();
                    return true;
                }
            });
        }

        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
        pantryViewModel = new ViewModelProvider(this).get(PantryViewModel.class);
        favouriteViewModel = new ViewModelProvider(this).get(FavouriteViewModel.class);

        favouriteViewModel.getAll().observe(getViewLifecycleOwner(), favs -> {
            java.util.HashSet<String> set = new java.util.HashSet<>();
            if (favs != null) {
                for (com.example.receipematcher.data.entities.Favourite f : favs) {
                    if (f.recipeId != null) set.add(f.recipeId);
                }
            }
            adapter.setFavoriteIds(set);
        });

        fetchAiRecipes();
        observeImportProgress(view);

        return view;
    }

    private void fetchAiRecipes() {
        pantryViewModel.getAllIngredients().observe(getViewLifecycleOwner(), ingredients -> {
            if (ingredients == null || ingredients.isEmpty()) return;
            
            // Store both pantry ingredients and terms
            pantryIngredients = new ArrayList<>(ingredients);
            List<String> pantryList = new ArrayList<>();
            for (Ingredient ing : ingredients) {
                if (ing.name != null) pantryList.add(ing.name.toLowerCase().trim());
            }
            if (pantryList.isEmpty()) return;
            pantryTerms = pantryList;
            if (adapter != null) adapter.setPantryTerms(pantryTerms);

            android.util.Log.d("AI_RECIPES", "Pantry -> " + pantryList);

            recipeViewModel.getAiRecipes(pantryList).observe(getViewLifecycleOwner(), recipes -> {
                aiRecipes.clear();
                android.util.Log.d("AI_RECIPES", "AI returned: " + (recipes == null ? "null" : recipes.size()));
                if (recipes != null) {
                    // Simple recipe list (smart prioritization removed)
                    aiRecipes.addAll(recipes);
                }
                applyFilter();
            });
        });
    }

    private void observeImportProgress(View root) {
        WorkManager.getInstance(requireContext())
                .getWorkInfosForUniqueWorkLiveData(RecipeImportWorker.UNIQUE_NAME)
                .observe(getViewLifecycleOwner(), workInfos -> {
                    if (workInfos == null || workInfos.isEmpty()) return;
                    WorkInfo info = workInfos.get(0);
                    WorkInfo.State state = info.getState();
                    if (state == WorkInfo.State.ENQUEUED || state == WorkInfo.State.RUNNING) {
                        int imported = info.getProgress().getInt("imported", 0);
                        String msg = imported > 0 ? ("Preparing recipes... " + imported) : "Preparing recipes...";
                        if (importSnackbar == null) {
                            importSnackbar = Snackbar.make(root, msg, Snackbar.LENGTH_INDEFINITE);
                            importSnackbar.show();
                        } else {
                            importSnackbar.setText(msg);
                        }
                    } else if (state == WorkInfo.State.SUCCEEDED) {
                        if (importSnackbar != null) { importSnackbar.dismiss(); importSnackbar = null; }
                        // Auto-retry loading after import completes
                        fetchAiRecipes();
                    } else if (state == WorkInfo.State.FAILED || state == WorkInfo.State.CANCELLED) {
                        if (importSnackbar != null) { importSnackbar.dismiss(); importSnackbar = null; }
                        Snackbar.make(root, "Recipe import failed. Retry from app restart.", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void applyFilter() {
        filtered.clear();
        
        // First apply search query filter
        List<AiRecipe> searchFiltered = new ArrayList<>();
        if (currentQuery == null || currentQuery.isEmpty()) {
            searchFiltered.addAll(aiRecipes);
        } else {
            String q = currentQuery.toLowerCase();
            for (AiRecipe r : aiRecipes) {
                String name = r.name == null ? "" : r.name.toLowerCase();
                boolean match = name.contains(q);
                if (!match && r.ingredients != null) {
                    for (String ing : r.ingredients) {
                        if (ing != null && ing.toLowerCase().contains(q)) { match = true; break; }
                    }
                }
                if (match) searchFiltered.add(r);
            }
        }
        
        // Then apply advanced filters
        List<AiRecipe> advancedFiltered = filterManager.applyFilters(searchFiltered, pantryTerms);
        
        // Simple filtering (smart prioritization removed)
        filtered.addAll(advancedFiltered);
        
        adapter.submitList(new ArrayList<>(filtered));
    }

    private void sortByCoverage(List<AiRecipe> list) {
        if (list == null || list.isEmpty() || pantryTerms == null) return;
        list.sort((a, b) -> Integer.compare(matchCount(b), matchCount(a)));
    }

    private int matchCount(AiRecipe r) {
        if (r == null || r.ingredients == null || pantryTerms == null) return 0;
        int c = 0;
        for (String ing : r.ingredients) {
            String s = ing == null ? "" : ing.toLowerCase();
            for (String p : pantryTerms) {
                if (p != null && !p.isEmpty() && s.contains(p)) { c++; break; }
            }
        }
        return c;
    }

    private void navigateToDetail(AiRecipe recipe) {
        Bundle args = new Bundle();
        args.putString("title", recipe.name);
        args.putStringArrayList("ingredients", recipe.ingredients == null ? new ArrayList<>() : new ArrayList<>(recipe.ingredients));
        args.putStringArrayList("steps", recipe.steps == null ? new ArrayList<>() : new ArrayList<>(recipe.steps));
        if (pantryTerms != null) {
            args.putStringArrayList("pantry", new ArrayList<>(pantryTerms));
        }
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_recipeListFragment_to_aiRecipeDetailFragment, args);
    }
    
    private void showFilterDialog() {
        RecipeFilterDialog dialog = RecipeFilterDialog.newInstance();
        dialog.setOnFiltersAppliedListener(filters -> {
            updateActiveFiltersDisplay();
            applyFilter();
        });
        dialog.show(getParentFragmentManager(), "filter_dialog");
    }
    
    private void updateActiveFiltersDisplay() {
        FilterManager.RecipeFilters filters = filterManager.getCurrentFilters();
        chipGroupActiveFilters.removeAllViews();
        
        if (!filters.hasActiveFilters()) {
            scrollViewActiveFilters.setVisibility(View.GONE);
            return;
        }
        
        scrollViewActiveFilters.setVisibility(View.VISIBLE);
        
        // Add availability filter chip
        if (filters.availabilityFilter != FilterManager.AvailabilityFilter.ALL) {
            addFilterChip(filters.availabilityFilter.displayName, () -> {
                filters.availabilityFilter = FilterManager.AvailabilityFilter.ALL;
                filterManager.updateFilters(filters);
                updateActiveFiltersDisplay();
                applyFilter();
            });
        }
        
        // Add cuisine filter chips
        for (FilterManager.CuisineType cuisine : filters.cuisineFilters) {
            addFilterChip(cuisine.displayName, () -> {
                filters.cuisineFilters.remove(cuisine);
                filterManager.updateFilters(filters);
                updateActiveFiltersDisplay();
                applyFilter();
            });
        }
        
        // Add dietary filter chips
        for (FilterManager.DietaryRestriction dietary : filters.dietaryFilters) {
            addFilterChip(dietary.displayName, () -> {
                filters.dietaryFilters.remove(dietary);
                filterManager.updateFilters(filters);
                updateActiveFiltersDisplay();
                applyFilter();
            });
        }
        
        // Add time filter chip
        if (filters.timeFilter != FilterManager.TimeFilter.ANY) {
            addFilterChip(filters.timeFilter.displayName, () -> {
                filters.timeFilter = FilterManager.TimeFilter.ANY;
                filterManager.updateFilters(filters);
                updateActiveFiltersDisplay();
                applyFilter();
            });
        }
        
        // Add difficulty filter chip
        if (filters.difficultyFilter != FilterManager.DifficultyFilter.ANY) {
            addFilterChip(filters.difficultyFilter.displayName, () -> {
                filters.difficultyFilter = FilterManager.DifficultyFilter.ANY;
                filterManager.updateFilters(filters);
                updateActiveFiltersDisplay();
                applyFilter();
            });
        }
        
        // Add expiring first chip
        if (filters.useExpiringFirst) {
            addFilterChip("Expiring First", () -> {
                filters.useExpiringFirst = false;
                filterManager.updateFilters(filters);
                updateActiveFiltersDisplay();
                applyFilter();
            });
        }
    }
    
    private void addFilterChip(String text, Runnable onRemove) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setCheckable(false);
        chip.setOnCloseIconClickListener(v -> onRemove.run());
        chipGroupActiveFilters.addView(chip);
    }
}
