package com.example.receipematcher.utils;

import com.example.receipematcher.data.repository.RecipeRepository.AiRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for calculating recipe match scores based on available pantry ingredients
 */
public class RecipeMatchCalculator {
    
    /**
     * Calculate match percentage for a recipe based on available pantry ingredients
     * @param recipe The recipe to calculate match for
     * @param pantryIngredients List of available pantry ingredients
     * @return Match percentage (0-100)
     */
    public static float calculateMatchPercentage(AiRecipe recipe, List<String> pantryIngredients) {
        if (recipe == null || recipe.ingredients == null || recipe.ingredients.isEmpty()) {
            return 0f;
        }
        
        if (pantryIngredients == null || pantryIngredients.isEmpty()) {
            return 0f;
        }
        
        int totalIngredients = recipe.ingredients.size();
        int matchedIngredients = 0;
        
        // Convert pantry ingredients to lowercase for case-insensitive matching
        List<String> normalizedPantry = new ArrayList<>();
        for (String ingredient : pantryIngredients) {
            if (ingredient != null) {
                normalizedPantry.add(ingredient.toLowerCase().trim());
            }
        }
        
        // Check each recipe ingredient against pantry
        for (String recipeIngredient : recipe.ingredients) {
            if (recipeIngredient == null) continue;
            
            String normalizedRecipeIngredient = recipeIngredient.toLowerCase().trim();
            
            // Check for exact matches or partial matches
            for (String pantryItem : normalizedPantry) {
                if (isIngredientMatch(normalizedRecipeIngredient, pantryItem)) {
                    matchedIngredients++;
                    break; // Found a match, move to next recipe ingredient
                }
            }
        }
        
        return (float) matchedIngredients / totalIngredients * 100f;
    }
    
    /**
     * Get list of missing ingredients for a recipe
     * @param recipe The recipe to check
     * @param pantryIngredients List of available pantry ingredients
     * @return List of missing ingredients
     */
    public static List<String> getMissingIngredients(AiRecipe recipe, List<String> pantryIngredients) {
        List<String> missingIngredients = new ArrayList<>();
        
        if (recipe == null || recipe.ingredients == null) {
            return missingIngredients;
        }
        
        if (pantryIngredients == null || pantryIngredients.isEmpty()) {
            return new ArrayList<>(recipe.ingredients);
        }
        
        // Convert pantry ingredients to lowercase for case-insensitive matching
        List<String> normalizedPantry = new ArrayList<>();
        for (String ingredient : pantryIngredients) {
            if (ingredient != null) {
                normalizedPantry.add(ingredient.toLowerCase().trim());
            }
        }
        
        // Check each recipe ingredient
        for (String recipeIngredient : recipe.ingredients) {
            if (recipeIngredient == null) continue;
            
            String normalizedRecipeIngredient = recipeIngredient.toLowerCase().trim();
            boolean found = false;
            
            // Check if ingredient is available in pantry
            for (String pantryItem : normalizedPantry) {
                if (isIngredientMatch(normalizedRecipeIngredient, pantryItem)) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                missingIngredients.add(recipeIngredient);
            }
        }
        
        return missingIngredients;
    }
    
    /**
     * Get count of matched ingredients
     * @param recipe The recipe to check
     * @param pantryIngredients List of available pantry ingredients
     * @return Number of matched ingredients
     */
    public static int getMatchedIngredientsCount(AiRecipe recipe, List<String> pantryIngredients) {
        if (recipe == null || recipe.ingredients == null || recipe.ingredients.isEmpty()) {
            return 0;
        }
        
        if (pantryIngredients == null || pantryIngredients.isEmpty()) {
            return 0;
        }
        
        int matchedCount = 0;
        
        // Convert pantry ingredients to lowercase for case-insensitive matching
        List<String> normalizedPantry = new ArrayList<>();
        for (String ingredient : pantryIngredients) {
            if (ingredient != null) {
                normalizedPantry.add(ingredient.toLowerCase().trim());
            }
        }
        
        // Check each recipe ingredient against pantry
        for (String recipeIngredient : recipe.ingredients) {
            if (recipeIngredient == null) continue;
            
            String normalizedRecipeIngredient = recipeIngredient.toLowerCase().trim();
            
            // Check for matches
            for (String pantryItem : normalizedPantry) {
                if (isIngredientMatch(normalizedRecipeIngredient, pantryItem)) {
                    matchedCount++;
                    break; // Found a match, move to next recipe ingredient
                }
            }
        }
        
        return matchedCount;
    }
    
    /**
     * Determine if a recipe ingredient matches a pantry ingredient
     * Uses fuzzy matching to handle variations in ingredient names
     */
    private static boolean isIngredientMatch(String recipeIngredient, String pantryIngredient) {
        if (recipeIngredient == null || pantryIngredient == null) {
            return false;
        }
        
        // Exact match
        if (recipeIngredient.equals(pantryIngredient)) {
            return true;
        }
        
        // Check if pantry ingredient is contained in recipe ingredient or vice versa
        if (recipeIngredient.contains(pantryIngredient) || pantryIngredient.contains(recipeIngredient)) {
            return true;
        }
        
        // Check for common ingredient variations
        return checkIngredientVariations(recipeIngredient, pantryIngredient);
    }
    
    /**
     * Check for common ingredient name variations
     */
    private static boolean checkIngredientVariations(String ingredient1, String ingredient2) {
        // Remove common words that don't affect matching
        String[] commonWords = {"fresh", "dried", "ground", "whole", "chopped", "sliced", "diced", "minced"};
        
        String cleaned1 = ingredient1;
        String cleaned2 = ingredient2;
        
        for (String word : commonWords) {
            cleaned1 = cleaned1.replaceAll("\\b" + word + "\\b", "").trim();
            cleaned2 = cleaned2.replaceAll("\\b" + word + "\\b", "").trim();
        }
        
        // Check if cleaned versions match
        if (cleaned1.equals(cleaned2)) {
            return true;
        }
        
        // Check if one contains the other after cleaning
        return cleaned1.contains(cleaned2) || cleaned2.contains(cleaned1);
    }
    
    /**
     * Get match quality description based on percentage
     */
    public static String getMatchQualityDescription(float matchPercentage) {
        if (matchPercentage >= 100f) {
            return "Perfect Match";
        } else if (matchPercentage >= 80f) {
            return "Great Match";
        } else if (matchPercentage >= 60f) {
            return "Good Match";
        } else if (matchPercentage >= 40f) {
            return "Partial Match";
        } else if (matchPercentage > 0f) {
            return "Few Ingredients";
        } else {
            return "No Match";
        }
    }

    /**
     * Calculate match score between present and missing ingredients
     * @param presentIngredients List of ingredients the user has
     * @param missingIngredients List of ingredients the user needs
     * @return Match score as a double (0.0 to 1.0)
     */
    public static double calculateMatchScore(List<String> presentIngredients, List<String> missingIngredients) {
        if (presentIngredients == null && missingIngredients == null) {
            return 0.0;
        }
        
        int totalIngredients = (presentIngredients != null ? presentIngredients.size() : 0) + 
                             (missingIngredients != null ? missingIngredients.size() : 0);
        
        if (totalIngredients == 0) {
            return 0.0;
        }
        
        int presentCount = presentIngredients != null ? presentIngredients.size() : 0;
        return (double) presentCount / totalIngredients;
    }
}