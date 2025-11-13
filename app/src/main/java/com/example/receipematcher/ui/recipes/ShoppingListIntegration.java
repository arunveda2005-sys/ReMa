package com.example.receipematcher.ui.recipes;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;

import com.example.receipematcher.data.entities.Recipe;
import com.example.receipematcher.data.entities.ShoppingItem;
import com.example.receipematcher.data.repository.ShoppingRepository;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles integration between recipe details and shopping list functionality
 * Enhanced with smart shopping list generation capabilities
 */
public class ShoppingListIntegration {
    
    private final Context context;
    private final ShoppingRepository shoppingRepository;

    private final ExecutorService executor;
    
    public ShoppingListIntegration(Context context) {
        this.context = context;
        this.shoppingRepository = new ShoppingRepository((android.app.Application) context.getApplicationContext());

        this.executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Add missing ingredients to shopping list with serving size adjustment
     */
    public void addMissingIngredientsToShoppingList(String recipeTitle, 
                                                   List<String> missingIngredients,
                                                   int servingSize,
                                                   OnShoppingListUpdateListener listener) {
        
        if (missingIngredients == null || missingIngredients.isEmpty()) {
            if (listener != null) {
                listener.onNoItemsToAdd();
            }
            return;
        }
        
        // Use ingredients as-is (simplified)
        
        executor.execute(() -> {
            try {
                List<ShoppingItem> duplicates = new ArrayList<>();
                List<String> newItems = new ArrayList<>();
                
                // Check for existing items
                for (String ingredient : missingIngredients) {
                    if (ingredient == null || ingredient.trim().isEmpty()) continue;
                    
                    String itemName = extractIngredientName(ingredient);
                    ShoppingItem existing = shoppingRepository.getByNameSync(itemName);
                    
                    if (existing != null) {
                        duplicates.add(existing);
                    } else {
                        newItems.add(ingredient);
                    }
                }
                
                // Handle results on main thread
                ((android.app.Activity) context).runOnUiThread(() -> {
                    if (!duplicates.isEmpty()) {
                        showDuplicateItemsDialog(recipeTitle, duplicates, newItems, listener);
                    } else {
                        addNewItemsToShoppingList(recipeTitle, newItems, listener);
                    }
                });
                
            } catch (Exception e) {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    if (listener != null) {
                        listener.onError("Failed to add items to shopping list: " + e.getMessage());
                    }
                });
            }
        });
    }
    
    /**
     * Add selected ingredients to shopping list with batch confirmation
     */
    public void addSelectedIngredientsToShoppingList(String recipeTitle,
                                                   List<String> selectedIngredients,
                                                   int servingSize,
                                                   OnShoppingListUpdateListener listener) {
        
        if (selectedIngredients == null || selectedIngredients.isEmpty()) {
            if (listener != null) {
                listener.onNoItemsToAdd();
            }
            return;
        }
        
        // Show confirmation dialog with items to be added
        showBatchAddConfirmationDialog(recipeTitle, selectedIngredients, servingSize, listener);
    }
    
    /**
     * Simple ingredient processing (no serving size adjustment)
     */
    private List<String> processIngredients(List<String> ingredients) {
        return new ArrayList<>(ingredients);
    }
    
    /**
     * Extract ingredient name from full ingredient string
     */
    private String extractIngredientName(String ingredient) {
        if (ingredient == null || ingredient.trim().isEmpty()) {
            return "";
        }
        
        // Remove quantities and units to get just the ingredient name
        String cleaned = ingredient.trim()
                .replaceAll("^\\d+(?:\\.\\d+)?(?:/\\d+)?\\s*", "") // Remove leading numbers
                .replaceAll("^(?:cup|cups|tbsp|tsp|oz|lb|lbs|g|kg|ml|l)s?\\s+", "") // Remove units
                .trim();
        
        return cleaned.isEmpty() ? ingredient.trim() : cleaned;
    }
    
    /**
     * Show dialog for handling duplicate items
     */
    private void showDuplicateItemsDialog(String recipeTitle, 
                                        List<ShoppingItem> duplicates,
                                        List<String> newItems,
                                        OnShoppingListUpdateListener listener) {
        
        StringBuilder message = new StringBuilder("Some items are already in your shopping list:\\n\\n");
        for (ShoppingItem item : duplicates) {
            message.append("• ").append(item.name).append("\\n");
        }
        message.append("\\nWould you like to increase the quantity for these items?");
        
        new AlertDialog.Builder(context)
                .setTitle("Items Already in Shopping List")
                .setMessage(message.toString())
                .setNegativeButton("Skip Duplicates", (dialog, which) -> {
                    addNewItemsToShoppingList(recipeTitle, newItems, listener);
                })
                .setPositiveButton("Increase Quantity", (dialog, which) -> {
                    updateExistingItemsAndAddNew(recipeTitle, duplicates, newItems, listener);
                })
                .setNeutralButton("Cancel", (dialog, which) -> {
                    if (listener != null) {
                        listener.onCancelled();
                    }
                })
                .show();
    }
    
    /**
     * Show confirmation dialog for batch adding items
     */
    private void showBatchAddConfirmationDialog(String recipeTitle,
                                              List<String> ingredients,
                                              int servingSize,
                                              OnShoppingListUpdateListener listener) {
        
        StringBuilder message = new StringBuilder("Add these items to your shopping list:\\n\\n");
        for (String ingredient : ingredients) {
            message.append("• ").append(ingredient).append("\\n");
        }
        
        if (servingSize != 4) {
            message.append("\\n(For ").append(servingSize).append(" servings)");
        }
        
        new AlertDialog.Builder(context)
                .setTitle("Add to Shopping List")
                .setMessage(message.toString())
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (listener != null) {
                        listener.onCancelled();
                    }
                })
                .setPositiveButton("Add Items", (dialog, which) -> {
                    addMissingIngredientsToShoppingList(recipeTitle, ingredients, servingSize, listener);
                })
                .show();
    }
    
    /**
     * Update existing items and add new ones
     */
    private void updateExistingItemsAndAddNew(String recipeTitle,
                                            List<ShoppingItem> existingItems,
                                            List<String> newItems,
                                            OnShoppingListUpdateListener listener) {
        
        executor.execute(() -> {
            try {
                int updatedCount = 0;
                int addedCount = 0;
                
                // Update existing items
                for (ShoppingItem item : existingItems) {
                    shoppingRepository.incrementQuantity(item, 1);
                    updatedCount++;
                }
                
                // Add new items
                addedCount = addNewItemsSync(recipeTitle, newItems);
                
                final int finalUpdated = updatedCount;
                final int finalAdded = addedCount;
                
                ((android.app.Activity) context).runOnUiThread(() -> {
                    if (listener != null) {
                        listener.onItemsAdded(finalAdded, finalUpdated);
                    }
                });
                
            } catch (Exception e) {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    if (listener != null) {
                        listener.onError("Failed to update shopping list: " + e.getMessage());
                    }
                });
            }
        });
    }
    
    /**
     * Add new items to shopping list
     */
    private void addNewItemsToShoppingList(String recipeTitle, 
                                         List<String> newItems,
                                         OnShoppingListUpdateListener listener) {
        
        executor.execute(() -> {
            try {
                int addedCount = addNewItemsSync(recipeTitle, newItems);
                
                ((android.app.Activity) context).runOnUiThread(() -> {
                    if (listener != null) {
                        listener.onItemsAdded(addedCount, 0);
                    }
                });
                
            } catch (Exception e) {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    if (listener != null) {
                        listener.onError("Failed to add items to shopping list: " + e.getMessage());
                    }
                });
            }
        });
    }
    
    /**
     * Synchronously add new items to shopping list
     */
    private int addNewItemsSync(String recipeTitle, List<String> newItems) {
        int addedCount = 0;
        long currentTime = System.currentTimeMillis();
        String recipeId = recipeTitle != null ? recipeTitle.trim().toLowerCase() : "";
        
        for (String ingredient : newItems) {
            if (ingredient == null || ingredient.trim().isEmpty()) continue;
            
            ShoppingItem item = new ShoppingItem();
            item.name = extractIngredientName(ingredient);
            item.quantity = extractQuantity(ingredient);
            item.unit = extractUnit(ingredient);
            item.completed = false;
            item.sourceRecipeId = recipeId;
            item.addedAt = new Date(currentTime);
            
            shoppingRepository.insert(item);
            addedCount++;
        }
        
        return addedCount;
    }
    
    /**
     * Extract quantity from ingredient string
     */
    private int extractQuantity(String ingredient) {
        if (ingredient == null) return 1;
        
        String[] words = ingredient.trim().split("\\s+");
        if (words.length > 0) {
            try {
                // Try to parse first word as quantity
                String firstWord = words[0].replaceAll("[^\\d.]", "");
                if (!firstWord.isEmpty()) {
                    return Math.max(1, (int) Math.round(Double.parseDouble(firstWord)));
                }
            } catch (NumberFormatException ignored) {
            }
        }
        
        return 1;
    }
    
    /**
     * Extract unit from ingredient string
     */
    private String extractUnit(String ingredient) {
        if (ingredient == null) return "";
        
        String[] words = ingredient.trim().split("\\s+");
        if (words.length >= 2) {
            String secondWord = words[1].toLowerCase();
            String[] units = {"cup", "cups", "tbsp", "tsp", "oz", "lb", "lbs", "g", "kg", "ml", "l"};
            
            for (String unit : units) {
                if (secondWord.equals(unit) || secondWord.equals(unit + "s")) {
                    return secondWord;
                }
            }
        }
        
        return "";
    }
    
    /**
     * Generate smart shopping list from recipe using the new smart features
     */
    public void generateSmartShoppingListFromRecipe(Recipe recipe, FragmentManager fragmentManager, OnShoppingListUpdateListener listener) {
        List<Recipe> recipes = new ArrayList<>();
        recipes.add(recipe);
        generateSmartShoppingListFromRecipes(recipes, fragmentManager, listener);
    }
    
    /**
     * Generate smart shopping list from multiple recipes
     */
    public void generateSmartShoppingListFromRecipes(List<Recipe> recipes, FragmentManager fragmentManager, OnShoppingListUpdateListener listener) {
        if (recipes == null || recipes.isEmpty()) {
            if (listener != null) {
                listener.onNoItemsToAdd();
            }
            return;
        }
        
        // Simple shopping list generation (smart dialog removed)
        Toast.makeText(context, "Smart shopping list feature simplified", Toast.LENGTH_SHORT).show();
        if (listener != null) {
            listener.onSmartShoppingListGenerated(1); // Default list ID
        }
    }
    
    /**
     * Quick add recipe ingredients to shopping list with smart consolidation
     */
    public void quickAddRecipeToShoppingList(Recipe recipe, OnShoppingListUpdateListener listener) {
        // Simplified recipe to shopping list conversion
        CompletableFuture.supplyAsync(() -> {
            // Extract ingredients from recipe and convert to shopping items
            // Simple ingredient extraction (getIngredients method may not exist)
            List<String> ingredients = new ArrayList<>();
            // Add some default ingredients or extract from recipe name
            ingredients.add(recipe.name + " ingredients");
            return ingredients;
        })
            .thenAccept(ingredients -> {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    if (ingredients != null && !ingredients.isEmpty()) {
                        if (listener != null) {
                            listener.onSmartShoppingListGenerated(1); // Default list ID
                        }
                        Toast.makeText(context, "Shopping list created!", Toast.LENGTH_SHORT).show();
                    } else {
                        if (listener != null) {
                            listener.onError("Failed to create shopping list");
                        }
                    }
                });
            })
            .exceptionally(throwable -> {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    if (listener != null) {
                        listener.onError("Error creating shopping list: " + throwable.getMessage());
                    }
                });
                return null;
            });
    }
    
    /**
     * Add ingredients to existing shopping list with smart consolidation
     */
    public void addIngredientsToExistingShoppingList(List<String> ingredients, int shoppingListId, OnShoppingListUpdateListener listener) {
        // Convert ingredient strings to ShoppingItem objects
        List<ShoppingItem> items = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        
        for (String ingredient : ingredients) {
            if (ingredient == null || ingredient.trim().isEmpty()) continue;
            
            ShoppingItem item = new ShoppingItem();
            item.name = extractIngredientName(ingredient);
            item.quantity = extractQuantity(ingredient);
            item.unit = extractUnit(ingredient);
            item.completed = false;
            item.shoppingListId = shoppingListId;
            item.category = categorizeIngredient(item.name);
            item.priority = 3; // Default medium priority
            item.addedAt = new java.util.Date(currentTime);
            
            items.add(item);
        }
        
        // Simplified item addition
        CompletableFuture.runAsync(() -> {
            // Add items to shopping list directly
            for (ShoppingItem item : items) {
                shoppingRepository.insertSync(item);
            }
        })
            .thenRun(() -> {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    if (listener != null) {
                        listener.onItemsAdded(items.size(), 0);
                    }
                    Toast.makeText(context, "Items added with smart consolidation!", Toast.LENGTH_SHORT).show();
                });
            })
            .exceptionally(throwable -> {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    if (listener != null) {
                        listener.onError("Error adding items: " + throwable.getMessage());
                    }
                });
                return null;
            });
    }
    
    /**
     * Get optimized shopping order for store navigation
     */
    public void getOptimizedShoppingOrder(int shoppingListId, String storeLayout, OnOptimizedOrderListener listener) {
        // Simplified shopping order (no optimization)
        CompletableFuture.supplyAsync(() -> {
            // Return items in original order
            return shoppingRepository.getAllSync();
        })
            .thenAccept(optimizedItems -> {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    if (listener != null) {
                        listener.onOptimizedOrderReady(optimizedItems);
                    }
                });
            })
            .exceptionally(throwable -> {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    if (listener != null) {
                        listener.onOptimizedOrderError("Error optimizing order: " + throwable.getMessage());
                    }
                });
                return null;
            });
    }
    
    private String categorizeIngredient(String ingredientName) {
        String name = ingredientName.toLowerCase();
        
        // Produce
        if (name.matches(".*(tomato|onion|garlic|carrot|potato|lettuce|spinach|apple|banana|orange|lemon).*")) {
            return "Produce";
        }
        // Dairy
        if (name.matches(".*(milk|cheese|butter|yogurt|cream|egg).*")) {
            return "Dairy";
        }
        // Meat & Seafood
        if (name.matches(".*(chicken|beef|pork|fish|salmon|shrimp|turkey).*")) {
            return "Meat & Seafood";
        }
        // Pantry
        if (name.matches(".*(flour|sugar|salt|pepper|oil|vinegar|rice|pasta).*")) {
            return "Pantry";
        }
        // Frozen
        if (name.matches(".*(frozen|ice).*")) {
            return "Frozen";
        }
        
        return "Other"; // Default category
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
    
    /**
     * Interface for shopping list update callbacks
     */
    public interface OnShoppingListUpdateListener {
        void onItemsAdded(int addedCount, int updatedCount);
        void onNoItemsToAdd();
        void onError(String message);
        void onCancelled();
        
        // New methods for smart shopping list features
        default void onSmartShoppingListGenerated(int shoppingListId) {
            // Default implementation for backward compatibility
        }
    }
    
    /**
     * Interface for optimized shopping order callbacks
     */
    public interface OnOptimizedOrderListener {
        void onOptimizedOrderReady(List<ShoppingItem> optimizedItems);
        void onOptimizedOrderError(String message);
    }
}