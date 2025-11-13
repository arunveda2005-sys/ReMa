package com.example.receipematcher.ui.recipes;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.receipematcher.ui.components.CircularProgressIndicator;
import com.example.receipematcher.utils.RecipeMatchCalculator;

import com.example.receipematcher.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.receipematcher.data.entities.ShoppingItem;
import com.example.receipematcher.data.repository.ShoppingRepository;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AiRecipeDetailFragment extends Fragment implements 
        IngredientMissingAdapter.OnSubstitutionListener,
        ShoppingListIntegration.OnShoppingListUpdateListener {

    // UI Components
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView recipeImage;
    private CircularProgressIndicator matchScoreIndicator;
    private TextView cookingTimeText;
    private TextView servingsText;
    private TextView difficultyText;
    private MaterialButton startCookingButton;

    private RecyclerView ingredientsPresentRecycler;
    private RecyclerView ingredientsMissingRecycler;
    private TextView ingredientsPresentCount;
    private TextView ingredientsMissingCount;
    private MaterialButton quickAddToShoppingButton;
    private FloatingActionButton fabStartCooking;
    private FloatingActionButton fabAddToShopping;
    private TextView stepsText;
    
    // Adapters
    private IngredientPresentAdapter ingredientsPresentAdapter;
    private IngredientMissingAdapter ingredientsMissingAdapter;
    
    // Managers
    private ShoppingListIntegration shoppingListIntegration;
    private int currentServings = 4; // Default serving size
    
    // Data
    private String recipeTitle;
    private List<String> originalIngredients;
    private List<String> originalSteps;
    private List<String> pantryIngredients;
    private List<String> presentIngredients;
    private List<String> missingIngredients;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Transitions removed for simplicity
        
        // Initialize managers
        shoppingListIntegration = new ShoppingListIntegration(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ai_recipe_detail, container, false);

        initializeViews(v);
        setupRecyclerViews();
        loadRecipeData();
        setupClickListeners();

        return v;
    }
    
    private void setupClickListeners() {
        // Start cooking mode button
        if (startCookingButton != null) {
            startCookingButton.setOnClickListener(v -> startCookingMode());
        }
        
        // Quick add to shopping button
        if (quickAddToShoppingButton != null) {
            quickAddToShoppingButton.setOnClickListener(v -> addMissingIngredientsToShopping());
        }
    }
    
    private void startCookingMode() {
        if (getActivity() != null && originalSteps != null && !originalSteps.isEmpty()) {
            Bundle args = new Bundle();
            args.putString("title", recipeTitle == null ? "Recipe" : recipeTitle);
            args.putStringArrayList("steps", new java.util.ArrayList<>(originalSteps));
            NavHostFragment.findNavController(this).navigate(R.id.cookingFragment, args);
        } else {
            Toast.makeText(getContext(), "No cooking instructions available", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void addMissingIngredientsToShopping() {
        if (shoppingListIntegration != null && missingIngredients != null && !missingIngredients.isEmpty()) {
            shoppingListIntegration.addMissingIngredientsToShoppingList(recipeTitle, missingIngredients, currentServings, this);
        } else {
            Toast.makeText(getContext(), "No missing ingredients to add", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void initializeViews(View v) {
        // Toolbar and collapsing layout
        collapsingToolbar = v.findViewById(R.id.collapsingToolbar);
        MaterialToolbar toolbar = v.findViewById(R.id.toolbar);
        recipeImage = v.findViewById(R.id.imageRecipe);
        
        // Metadata section
        matchScoreIndicator = v.findViewById(R.id.matchScoreIndicator);
        cookingTimeText = v.findViewById(R.id.textCookingTime);
        servingsText = v.findViewById(R.id.textServings);
        difficultyText = v.findViewById(R.id.textDifficulty);
        
        // Serving size adjuster removed for simplicity
        
        // Ingredient sections
        ingredientsPresentRecycler = v.findViewById(R.id.recyclerIngredientsPresent);
        ingredientsMissingRecycler = v.findViewById(R.id.recyclerIngredientsMissing);
        ingredientsPresentCount = v.findViewById(R.id.textIngredientsPresentCount);
        ingredientsMissingCount = v.findViewById(R.id.textIngredientsMissingCount);
        quickAddToShoppingButton = v.findViewById(R.id.btnQuickAddToShopping);
        
        // Steps section
        stepsText = v.findViewById(R.id.textSteps);
        
        // Cooking mode button
        startCookingButton = v.findViewById(R.id.start_cooking_button);
        
        // Floating action buttons
        fabStartCooking = v.findViewById(R.id.fabStartCooking);
        fabAddToShopping = v.findViewById(R.id.fabAddToShopping);
        
        // Setup toolbar
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v1 -> NavHostFragment.findNavController(this).navigateUp());
        }
    }
    
    private void setupRecyclerViews() {
        // Setup present ingredients recycler
        ingredientsPresentAdapter = new IngredientPresentAdapter();
        ingredientsPresentRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        ingredientsPresentRecycler.setAdapter(ingredientsPresentAdapter);
        
        // Setup missing ingredients recycler
        ingredientsMissingAdapter = new IngredientMissingAdapter();
        ingredientsMissingAdapter.setOnSubstitutionListener(this);
        ingredientsMissingRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        ingredientsMissingRecycler.setAdapter(ingredientsMissingAdapter);
    }
    
    private void loadRecipeData() {
        Bundle args = getArguments();
        recipeTitle = args != null ? args.getString("title") : "Recipe";
        originalIngredients = args != null ? args.getStringArrayList("ingredients") : new ArrayList<>();
        originalSteps = args != null ? args.getStringArrayList("steps") : new ArrayList<>();
        pantryIngredients = args != null ? args.getStringArrayList("pantry") : new ArrayList<>();
        
        // Set recipe title
        if (collapsingToolbar != null) {
            collapsingToolbar.setTitle(recipeTitle);
        }
        
        // Set default serving size
        currentServings = 4;
        
        // Process ingredients
        processIngredients();
        
        // Setup steps
        setupSteps();
        
        // Update UI
        updateMetadata();
    }
    
    private void processIngredients() {
        presentIngredients = new ArrayList<>();
        missingIngredients = new ArrayList<>();
        
        List<String> currentIngredients = originalIngredients;
        
        for (String ingredient : currentIngredients) {
            if (ingredient == null || ingredient.trim().isEmpty()) continue;
            
            String ingredientLower = ingredient.toLowerCase(Locale.ROOT);
            boolean matched = false;
            
            for (String pantryItem : pantryIngredients) {
                if (pantryItem != null && !pantryItem.isEmpty() && 
                    ingredientLower.contains(pantryItem.toLowerCase(Locale.ROOT))) {
                    matched = true;
                    break;
                }
            }
            
            if (matched) {
                presentIngredients.add(ingredient);
            } else {
                missingIngredients.add(ingredient);
            }
        }
        
        // Update adapters
        ingredientsPresentAdapter.setIngredients(presentIngredients);
        ingredientsMissingAdapter.setIngredients(missingIngredients);
        
        // Update counts
        ingredientsPresentCount.setText(String.valueOf(presentIngredients.size()));
        ingredientsMissingCount.setText(String.valueOf(missingIngredients.size()));
    }
    
    private void setupSteps() {
        if (originalSteps != null && !originalSteps.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < originalSteps.size(); i++) {
                sb.append(i + 1).append(". ").append(originalSteps.get(i)).append("\n\n");
            }
            stepsText.setText(sb.toString());
        } else {
            stepsText.setText("No instructions available");
        }
    }
    
    private void updateMetadata() {
        // Update match score
        double matchScore = RecipeMatchCalculator.calculateMatchScore(presentIngredients, missingIngredients);
        if (matchScoreIndicator != null) {
            matchScoreIndicator.setProgress((int) (matchScore * 100));
        }
        
        // Update serving size display
        if (servingsText != null) {
            servingsText.setText(String.valueOf(currentServings));
        }
        
        // Set default values for other metadata
        if (cookingTimeText != null) {
            cookingTimeText.setText("30 min");
        }
        if (difficultyText != null) {
            difficultyText.setText("Easy");
        }
    }
    
    // Duplicate setupClickListeners method removed
    
    // Serving size functionality removed for simplicity
    
    // IngredientMissingAdapter.OnSubstitutionListener
    @Override
    public void onSubstitutionSelected(String originalIngredient, String substitute) {
        Toast.makeText(requireContext(), 
            "Substituted " + originalIngredient + " with " + substitute, 
            Toast.LENGTH_SHORT).show();
        
        // Here you could update the recipe with the substitution
        // For now, just show a confirmation
    }
    
    // ShoppingListIntegration.OnShoppingListUpdateListener
    @Override
    public void onItemsAdded(int addedCount, int updatedCount) {
        String message;
        if (updatedCount > 0) {
            message = String.format("Added %d items, updated %d existing items", addedCount, updatedCount);
        } else {
            message = String.format("Added %d items to shopping list", addedCount);
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onNoItemsToAdd() {
        Toast.makeText(requireContext(), "No items to add to shopping list", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }
    
    @Override
    public void onCancelled() {
        // User cancelled the operation
    }
    
    private void showCookingInstructionsDialog() {
        if (originalSteps == null || originalSteps.isEmpty()) {
            Toast.makeText(getContext(), "No cooking instructions available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Build instructions string
        StringBuilder instructionsBuilder = new StringBuilder();
        for (int i = 0; i < originalSteps.size(); i++) {
            instructionsBuilder.append(i + 1).append(". ").append(originalSteps.get(i));
            if (i < originalSteps.size() - 1) {
                instructionsBuilder.append("\n\n");
            }
        }
        
        // Show dialog with cooking instructions
        new AlertDialog.Builder(requireContext())
            .setTitle("Cooking Instructions - " + recipeTitle)
            .setMessage(instructionsBuilder.toString())
            .setPositiveButton("Got it!", null)
            .setNeutralButton("Add to Shopping", (dialog, which) -> {
                if (quickAddToShoppingButton != null) {
                    quickAddToShoppingButton.performClick();
                }
            })
            .show();
    }    

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (shoppingListIntegration != null) {
            shoppingListIntegration.cleanup();
        }
    }
}