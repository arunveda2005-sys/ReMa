package com.example.receipematcher.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.receipematcher.App;
import com.example.receipematcher.data.db.AiRecipeEntity;
import com.example.receipematcher.data.db.AppDatabase;
import com.example.receipematcher.data.db.dao.AiRecipeDao;
import com.example.receipematcher.workers.RecipeImportWorker;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class RecipeRepository {

    private static RecipeRepository instance;

    private RecipeRepository() { }

    public static synchronized RecipeRepository getInstance() {
        if (instance == null) {
            instance = new RecipeRepository();
        }
        return instance;
    }

    public static class AiRecipe {
        public String name;
        public List<String> ingredients;
        public List<String> steps;
        
        // Enhanced metadata fields
        public String cuisine;
        public List<String> dietaryTags;
        public int cookingTimeMinutes;
        public String difficulty; // "Easy", "Medium", "Hard"
        public float rating; // 0.0 - 5.0
        public int reviewCount;
        public String nutritionInfo;
        public boolean isTrending;
        public boolean isPopular;
        public boolean isQuick; // Under 30 minutes
        public boolean isHealthy;
        
        public AiRecipe() {
            this.dietaryTags = new ArrayList<>();
            this.cookingTimeMinutes = 30; // Default
            this.difficulty = "Easy"; // Default
            this.rating = 4.0f; // Default
            this.reviewCount = 0;
            this.isTrending = false;
            this.isPopular = false;
            this.isQuick = false;
            this.isHealthy = false;
        }
    }

    public LiveData<List<AiRecipe>> getAiRecipes(List<String> pantry) {
        MutableLiveData<List<AiRecipe>> live = new MutableLiveData<>();

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(App.get());
            AiRecipeDao dao = db.aiRecipeDao();
            long count = 0;
            try { count = dao.count(); } catch (Exception ignored) {}
            if (count == 0) {
                // Kick off import once, return empty for now; UI can retry shortly
                RecipeImportWorker.enqueueIfNeeded(App.get());
                live.postValue(new ArrayList<>());
                return;
            }
            // Build FTS match query: join tokens with OR to widen matches
            String match = buildFtsMatch(pantry);
            if (match.isEmpty()) {
                live.postValue(new ArrayList<>());
                return;
            }
            List<AiRecipeEntity> entities = dao.searchByFts(match, 200);
            List<AiRecipe> out = new ArrayList<>();
            for (AiRecipeEntity e : entities) out.add(map(e));
            live.postValue(out);
        }).start();

        return live;
    }

    private static String buildFtsMatch(List<String> pantry) {
        if (pantry == null || pantry.isEmpty()) return "";
        List<String> terms = new ArrayList<>();
        for (String p : pantry) {
            if (p == null) continue;
            String t = p.trim().toLowerCase();
            if (t.isEmpty()) continue;
            // Quote term for FTS MATCH; use prefix search
            terms.add('"' + t + '"' + "*");
        }
        if (terms.isEmpty()) return "";
        // name and ingredientsJson are both indexed in FTS content
        return String.join(" OR ", terms);
    }

    private static RecipeRepository.AiRecipe map(AiRecipeEntity e) {
        RecipeRepository.AiRecipe r = new RecipeRepository.AiRecipe();
        r.name = e.name;
        r.ingredients = jsonToList(e.ingredientsJson);
        r.steps = jsonToList(e.stepsJson);
        
        // Generate metadata based on recipe content (mock implementation)
        generateRecipeMetadata(r);
        
        return r;
    }
    
    private static void generateRecipeMetadata(RecipeRepository.AiRecipe recipe) {
        if (recipe.name == null || recipe.ingredients == null) return;
        
        String recipeName = recipe.name.toLowerCase();
        String ingredientsText = String.join(" ", recipe.ingredients).toLowerCase();
        
        // Determine cuisine based on ingredients and name
        recipe.cuisine = determineCuisine(recipeName, ingredientsText);
        
        // Generate dietary tags
        recipe.dietaryTags = generateDietaryTags(ingredientsText);
        
        // Estimate cooking time based on steps count
        int stepCount = recipe.steps != null ? recipe.steps.size() : 5;
        recipe.cookingTimeMinutes = Math.max(15, stepCount * 5 + new java.util.Random(recipe.name.hashCode()).nextInt(20));
        
        // Determine difficulty based on steps and ingredients
        recipe.difficulty = determineDifficulty(stepCount, recipe.ingredients.size());
        
        // Generate rating (mock)
        java.util.Random random = new java.util.Random(recipe.name.hashCode());
        recipe.rating = 3.5f + random.nextFloat() * 1.5f; // 3.5 - 5.0
        recipe.reviewCount = random.nextInt(500) + 10;
        
        // Set flags
        recipe.isQuick = recipe.cookingTimeMinutes <= 30;
        recipe.isHealthy = isHealthyRecipe(ingredientsText);
        recipe.isPopular = recipe.rating >= 4.3f && recipe.reviewCount >= 100;
        recipe.isTrending = random.nextFloat() < 0.15f; // 15% chance
        
        // Generate nutrition info (mock)
        recipe.nutritionInfo = generateNutritionInfo(recipe.ingredients.size());
    }
    
    private static String determineCuisine(String recipeName, String ingredientsText) {
        if (recipeName.contains("pasta") || recipeName.contains("pizza") || 
            ingredientsText.contains("parmesan") || ingredientsText.contains("basil")) {
            return "Italian";
        } else if (recipeName.contains("stir") || recipeName.contains("asian") ||
                   ingredientsText.contains("soy sauce") || ingredientsText.contains("ginger")) {
            return "Asian";
        } else if (recipeName.contains("taco") || recipeName.contains("burrito") ||
                   ingredientsText.contains("cumin") || ingredientsText.contains("cilantro")) {
            return "Mexican";
        } else if (recipeName.contains("curry") || ingredientsText.contains("turmeric")) {
            return "Indian";
        } else if (ingredientsText.contains("olive oil") || ingredientsText.contains("feta")) {
            return "Mediterranean";
        } else {
            return "American";
        }
    }
    
    private static List<String> generateDietaryTags(String ingredientsText) {
        List<String> tags = new ArrayList<>();
        
        if (!ingredientsText.contains("meat") && !ingredientsText.contains("chicken") &&
            !ingredientsText.contains("beef") && !ingredientsText.contains("pork")) {
            tags.add("Vegetarian");
            
            if (!ingredientsText.contains("dairy") && !ingredientsText.contains("cheese") &&
                !ingredientsText.contains("milk") && !ingredientsText.contains("egg")) {
                tags.add("Vegan");
            }
        }
        
        if (!ingredientsText.contains("wheat") && !ingredientsText.contains("flour") &&
            !ingredientsText.contains("bread")) {
            tags.add("Gluten-Free");
        }
        
        if (!ingredientsText.contains("milk") && !ingredientsText.contains("cheese") &&
            !ingredientsText.contains("butter")) {
            tags.add("Dairy-Free");
        }
        
        if (!ingredientsText.contains("pasta") && !ingredientsText.contains("rice") &&
            !ingredientsText.contains("bread") && !ingredientsText.contains("potato")) {
            tags.add("Low-Carb");
        }
        
        return tags;
    }
    
    private static String determineDifficulty(int stepCount, int ingredientCount) {
        int complexity = stepCount + (ingredientCount / 2);
        
        if (complexity <= 6) {
            return "Easy";
        } else if (complexity <= 10) {
            return "Medium";
        } else {
            return "Hard";
        }
    }
    
    private static boolean isHealthyRecipe(String ingredientsText) {
        return ingredientsText.contains("vegetable") || ingredientsText.contains("fruit") ||
               ingredientsText.contains("spinach") || ingredientsText.contains("broccoli") ||
               ingredientsText.contains("carrot") || ingredientsText.contains("lean") ||
               ingredientsText.contains("quinoa") || ingredientsText.contains("salmon");
    }
    
    private static String generateNutritionInfo(int ingredientCount) {
        // Mock nutrition info based on ingredient count
        int calories = 200 + (ingredientCount * 50) + new java.util.Random().nextInt(200);
        return calories + " cal";
    }

    private static List<String> jsonToList(String json) {
        List<String> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                String s = arr.optString(i, "");
                if (s != null && !s.isEmpty()) list.add(s);
            }
        } catch (Exception ignored) {}
        return list;
    }
}
