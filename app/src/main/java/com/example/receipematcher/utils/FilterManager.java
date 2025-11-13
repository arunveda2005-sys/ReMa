package com.example.receipematcher.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.receipematcher.data.repository.RecipeRepository.AiRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages complex filtering logic for recipes with persistence
 */
public class FilterManager {
    
    private static final String PREFS_NAME = "recipe_filters";
    private static final String KEY_AVAILABILITY_FILTER = "availability_filter";
    private static final String KEY_CUISINE_FILTERS = "cuisine_filters";
    private static final String KEY_DIETARY_FILTERS = "dietary_filters";
    private static final String KEY_TIME_FILTER = "time_filter";
    private static final String KEY_DIFFICULTY_FILTER = "difficulty_filter";
    
    public enum AvailabilityFilter {
        ALL("All Recipes"),
        CAN_MAKE_NOW("Can Make Now (100%)"),
        ALMOST_READY("Almost Ready (80%+)"),
        NEED_SHOPPING("Need Shopping (50%+)");
        
        public final String displayName;
        
        AvailabilityFilter(String displayName) {
            this.displayName = displayName;
        }
    }
    
    public enum CuisineType {
        ITALIAN("Italian"),
        ASIAN("Asian"),
        MEXICAN("Mexican"),
        AMERICAN("American"),
        MEDITERRANEAN("Mediterranean"),
        INDIAN("Indian"),
        FRENCH("French"),
        THAI("Thai"),
        CHINESE("Chinese"),
        JAPANESE("Japanese");
        
        public final String displayName;
        
        CuisineType(String displayName) {
            this.displayName = displayName;
        }
    }
    
    public enum DietaryRestriction {
        VEGETARIAN("Vegetarian"),
        VEGAN("Vegan"),
        GLUTEN_FREE("Gluten-Free"),
        DAIRY_FREE("Dairy-Free"),
        LOW_CARB("Low Carb"),
        KETO("Keto"),
        PALEO("Paleo"),
        HEALTHY("Healthy");
        
        public final String displayName;
        
        DietaryRestriction(String displayName) {
            this.displayName = displayName;
        }
    }
    
    public enum TimeFilter {
        ANY("Any Time"),
        QUICK("Under 30 min"),
        MEDIUM("30-60 min"),
        LONG("Over 60 min");
        
        public final String displayName;
        
        TimeFilter(String displayName) {
            this.displayName = displayName;
        }
    }
    
    public enum DifficultyFilter {
        ANY("Any Difficulty"),
        BEGINNER("Beginner"),
        INTERMEDIATE("Intermediate"),
        ADVANCED("Advanced");
        
        public final String displayName;
        
        DifficultyFilter(String displayName) {
            this.displayName = displayName;
        }
    }
    
    public static class RecipeFilters {
        public AvailabilityFilter availabilityFilter = AvailabilityFilter.ALL;
        public Set<CuisineType> cuisineFilters = new HashSet<>();
        public Set<DietaryRestriction> dietaryFilters = new HashSet<>();
        public TimeFilter timeFilter = TimeFilter.ANY;
        public DifficultyFilter difficultyFilter = DifficultyFilter.ANY;
        public boolean useExpiringFirst = false;
        public Set<String> avoidIngredients = new HashSet<>();
        
        public boolean hasActiveFilters() {
            return availabilityFilter != AvailabilityFilter.ALL ||
                   !cuisineFilters.isEmpty() ||
                   !dietaryFilters.isEmpty() ||
                   timeFilter != TimeFilter.ANY ||
                   difficultyFilter != DifficultyFilter.ANY ||
                   useExpiringFirst ||
                   !avoidIngredients.isEmpty();
        }
        
        public int getActiveFilterCount() {
            int count = 0;
            if (availabilityFilter != AvailabilityFilter.ALL) count++;
            count += cuisineFilters.size();
            count += dietaryFilters.size();
            if (timeFilter != TimeFilter.ANY) count++;
            if (difficultyFilter != DifficultyFilter.ANY) count++;
            if (useExpiringFirst) count++;
            count += avoidIngredients.size();
            return count;
        }
    }
    
    private final SharedPreferences prefs;
    private RecipeFilters currentFilters;
    
    public FilterManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentFilters = loadFilters();
    }
    
    public RecipeFilters getCurrentFilters() {
        return currentFilters;
    }
    
    public void updateFilters(RecipeFilters filters) {
        this.currentFilters = filters;
        saveFilters(filters);
    }
    
    public void clearAllFilters() {
        currentFilters = new RecipeFilters();
        saveFilters(currentFilters);
    }
    
    /**
     * Apply filters to a list of recipes
     */
    public List<AiRecipe> applyFilters(List<AiRecipe> recipes, List<String> pantryIngredients) {
        if (recipes == null || recipes.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<AiRecipe> filteredRecipes = new ArrayList<>();
        
        for (AiRecipe recipe : recipes) {
            if (passesFilters(recipe, pantryIngredients)) {
                filteredRecipes.add(recipe);
            }
        }
        
        // Sort by expiring ingredients first if enabled
        if (currentFilters.useExpiringFirst) {
            // This would require expiry date information from pantry
            // For now, we'll maintain the existing order
        }
        
        return filteredRecipes;
    }
    
    private boolean passesFilters(AiRecipe recipe, List<String> pantryIngredients) {
        // Availability filter
        if (currentFilters.availabilityFilter != AvailabilityFilter.ALL) {
            float matchPercentage = RecipeMatchCalculator.calculateMatchPercentage(recipe, pantryIngredients);
            
            switch (currentFilters.availabilityFilter) {
                case CAN_MAKE_NOW:
                    if (matchPercentage < 100f) return false;
                    break;
                case ALMOST_READY:
                    if (matchPercentage < 80f) return false;
                    break;
                case NEED_SHOPPING:
                    if (matchPercentage < 50f) return false;
                    break;
            }
        }
        
        // Cuisine filter
        if (!currentFilters.cuisineFilters.isEmpty()) {
            if (!matchesCuisineFilter(recipe)) return false;
        }
        
        // Dietary restrictions filter
        if (!currentFilters.dietaryFilters.isEmpty()) {
            if (!matchesDietaryFilter(recipe)) return false;
        }
        
        // Time filter
        if (currentFilters.timeFilter != TimeFilter.ANY) {
            if (!matchesTimeFilter(recipe)) return false;
        }
        
        // Difficulty filter
        if (currentFilters.difficultyFilter != DifficultyFilter.ANY) {
            if (!matchesDifficultyFilter(recipe)) return false;
        }
        
        // Avoid ingredients filter
        if (!currentFilters.avoidIngredients.isEmpty()) {
            if (containsAvoidedIngredients(recipe)) return false;
        }
        
        return true;
    }
    
    private boolean matchesCuisineFilter(AiRecipe recipe) {
        if (recipe.name == null) return false;
        
        String recipeName = recipe.name.toLowerCase();
        String ingredientsText = "";
        if (recipe.ingredients != null) {
            ingredientsText = String.join(" ", recipe.ingredients).toLowerCase();
        }
        
        for (CuisineType cuisine : currentFilters.cuisineFilters) {
            if (matchesCuisine(recipeName, ingredientsText, cuisine)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean matchesCuisine(String recipeName, String ingredientsText, CuisineType cuisine) {
        switch (cuisine) {
            case ITALIAN:
                return recipeName.contains("pasta") || recipeName.contains("pizza") || 
                       ingredientsText.contains("parmesan") || ingredientsText.contains("basil");
            case ASIAN:
                return recipeName.contains("stir") || recipeName.contains("asian") ||
                       ingredientsText.contains("soy sauce") || ingredientsText.contains("ginger");
            case MEXICAN:
                return recipeName.contains("taco") || recipeName.contains("burrito") ||
                       ingredientsText.contains("cumin") || ingredientsText.contains("cilantro");
            case AMERICAN:
                return recipeName.contains("burger") || recipeName.contains("bbq") ||
                       recipeName.contains("american");
            case MEDITERRANEAN:
                return ingredientsText.contains("olive oil") || ingredientsText.contains("feta") ||
                       ingredientsText.contains("olives");
            case INDIAN:
                return recipeName.contains("curry") || ingredientsText.contains("turmeric") ||
                       ingredientsText.contains("garam masala");
            case FRENCH:
                return recipeName.contains("french") || ingredientsText.contains("butter") ||
                       recipeName.contains("croissant");
            case THAI:
                return recipeName.contains("thai") || ingredientsText.contains("coconut milk") ||
                       ingredientsText.contains("lemongrass");
            case CHINESE:
                return recipeName.contains("chinese") || ingredientsText.contains("sesame oil") ||
                       recipeName.contains("fried rice");
            case JAPANESE:
                return recipeName.contains("sushi") || recipeName.contains("teriyaki") ||
                       ingredientsText.contains("miso");
            default:
                return false;
        }
    }
    
    private boolean matchesDietaryFilter(AiRecipe recipe) {
        if (recipe.ingredients == null) return false;
        
        String ingredientsText = String.join(" ", recipe.ingredients).toLowerCase();
        
        for (DietaryRestriction restriction : currentFilters.dietaryFilters) {
            if (!matchesDietaryRestriction(ingredientsText, restriction)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean matchesDietaryRestriction(String ingredientsText, DietaryRestriction restriction) {
        switch (restriction) {
            case VEGETARIAN:
                return !ingredientsText.contains("meat") && !ingredientsText.contains("chicken") &&
                       !ingredientsText.contains("beef") && !ingredientsText.contains("pork");
            case VEGAN:
                return !ingredientsText.contains("meat") && !ingredientsText.contains("chicken") &&
                       !ingredientsText.contains("dairy") && !ingredientsText.contains("cheese") &&
                       !ingredientsText.contains("milk") && !ingredientsText.contains("egg");
            case GLUTEN_FREE:
                return !ingredientsText.contains("wheat") && !ingredientsText.contains("flour") &&
                       !ingredientsText.contains("bread");
            case DAIRY_FREE:
                return !ingredientsText.contains("milk") && !ingredientsText.contains("cheese") &&
                       !ingredientsText.contains("butter") && !ingredientsText.contains("cream");
            case LOW_CARB:
                return !ingredientsText.contains("pasta") && !ingredientsText.contains("rice") &&
                       !ingredientsText.contains("bread") && !ingredientsText.contains("potato");
            case KETO:
                return !ingredientsText.contains("sugar") && !ingredientsText.contains("pasta") &&
                       !ingredientsText.contains("rice") && !ingredientsText.contains("bread");
            case PALEO:
                return !ingredientsText.contains("grain") && !ingredientsText.contains("dairy") &&
                       !ingredientsText.contains("legume");
            case HEALTHY:
                return ingredientsText.contains("vegetable") || ingredientsText.contains("fruit") ||
                       ingredientsText.contains("lean");
            default:
                return true;
        }
    }
    
    private boolean matchesTimeFilter(AiRecipe recipe) {
        // Mock implementation - in real app, this would use actual cooking time data
        int stepCount = recipe.steps != null ? recipe.steps.size() : 5;
        
        switch (currentFilters.timeFilter) {
            case QUICK:
                return stepCount <= 5;
            case MEDIUM:
                return stepCount > 5 && stepCount <= 10;
            case LONG:
                return stepCount > 10;
            default:
                return true;
        }
    }
    
    private boolean matchesDifficultyFilter(AiRecipe recipe) {
        // Mock implementation - in real app, this would use actual difficulty data
        int stepCount = recipe.steps != null ? recipe.steps.size() : 5;
        
        switch (currentFilters.difficultyFilter) {
            case BEGINNER:
                return stepCount <= 5;
            case INTERMEDIATE:
                return stepCount > 5 && stepCount <= 8;
            case ADVANCED:
                return stepCount > 8;
            default:
                return true;
        }
    }
    
    private boolean containsAvoidedIngredients(AiRecipe recipe) {
        if (recipe.ingredients == null) return false;
        
        String ingredientsText = String.join(" ", recipe.ingredients).toLowerCase();
        
        for (String avoidIngredient : currentFilters.avoidIngredients) {
            if (ingredientsText.contains(avoidIngredient.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    private RecipeFilters loadFilters() {
        RecipeFilters filters = new RecipeFilters();
        
        // Load availability filter
        String availabilityName = prefs.getString(KEY_AVAILABILITY_FILTER, AvailabilityFilter.ALL.name());
        try {
            filters.availabilityFilter = AvailabilityFilter.valueOf(availabilityName);
        } catch (IllegalArgumentException e) {
            filters.availabilityFilter = AvailabilityFilter.ALL;
        }
        
        // Load cuisine filters
        Set<String> cuisineNames = prefs.getStringSet(KEY_CUISINE_FILTERS, new HashSet<>());
        for (String name : cuisineNames) {
            try {
                filters.cuisineFilters.add(CuisineType.valueOf(name));
            } catch (IllegalArgumentException ignored) {}
        }
        
        // Load dietary filters
        Set<String> dietaryNames = prefs.getStringSet(KEY_DIETARY_FILTERS, new HashSet<>());
        for (String name : dietaryNames) {
            try {
                filters.dietaryFilters.add(DietaryRestriction.valueOf(name));
            } catch (IllegalArgumentException ignored) {}
        }
        
        // Load time filter
        String timeName = prefs.getString(KEY_TIME_FILTER, TimeFilter.ANY.name());
        try {
            filters.timeFilter = TimeFilter.valueOf(timeName);
        } catch (IllegalArgumentException e) {
            filters.timeFilter = TimeFilter.ANY;
        }
        
        // Load difficulty filter
        String difficultyName = prefs.getString(KEY_DIFFICULTY_FILTER, DifficultyFilter.ANY.name());
        try {
            filters.difficultyFilter = DifficultyFilter.valueOf(difficultyName);
        } catch (IllegalArgumentException e) {
            filters.difficultyFilter = DifficultyFilter.ANY;
        }
        
        return filters;
    }
    
    private void saveFilters(RecipeFilters filters) {
        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putString(KEY_AVAILABILITY_FILTER, filters.availabilityFilter.name());
        
        // Save cuisine filters
        Set<String> cuisineNames = new HashSet<>();
        for (CuisineType cuisine : filters.cuisineFilters) {
            cuisineNames.add(cuisine.name());
        }
        editor.putStringSet(KEY_CUISINE_FILTERS, cuisineNames);
        
        // Save dietary filters
        Set<String> dietaryNames = new HashSet<>();
        for (DietaryRestriction dietary : filters.dietaryFilters) {
            dietaryNames.add(dietary.name());
        }
        editor.putStringSet(KEY_DIETARY_FILTERS, dietaryNames);
        
        editor.putString(KEY_TIME_FILTER, filters.timeFilter.name());
        editor.putString(KEY_DIFFICULTY_FILTER, filters.difficultyFilter.name());
        
        editor.apply();
    }
}