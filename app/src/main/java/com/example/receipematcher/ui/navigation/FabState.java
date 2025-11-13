package com.example.receipematcher.ui.navigation;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

/**
 * Represents the state of the Floating Action Button
 */
public class FabState {
    
    public enum Type {
        HIDDEN,
        ADD_INGREDIENT,
        SAVE_RECIPE,
        ADD_SHOPPING_ITEM
    }
    
    private final Type type;
    private final int iconRes;
    private final int contentDescriptionRes;
    private final Runnable action;
    
    public FabState(Type type, @DrawableRes int iconRes, @StringRes int contentDescriptionRes, Runnable action) {
        this.type = type;
        this.iconRes = iconRes;
        this.contentDescriptionRes = contentDescriptionRes;
        this.action = action;
    }
    
    public static FabState hidden() {
        return new FabState(Type.HIDDEN, 0, 0, null);
    }
    
    public static FabState addIngredient(Runnable action) {
        return new FabState(Type.ADD_INGREDIENT, 
            com.example.receipematcher.R.drawable.ic_add,
            com.example.receipematcher.R.string.fab_add_ingredient,
            action);
    }
    
    public static FabState saveRecipe(Runnable action) {
        return new FabState(Type.SAVE_RECIPE,
            com.example.receipematcher.R.drawable.ic_star,
            com.example.receipematcher.R.string.fab_save_recipe,
            action);
    }
    
    public static FabState addShoppingItem(Runnable action) {
        return new FabState(Type.ADD_SHOPPING_ITEM,
            com.example.receipematcher.R.drawable.ic_add,
            com.example.receipematcher.R.string.fab_add_shopping_item,
            action);
    }
    
    public Type getType() {
        return type;
    }
    
    public int getIconRes() {
        return iconRes;
    }
    
    public int getContentDescriptionRes() {
        return contentDescriptionRes;
    }
    
    public Runnable getAction() {
        return action;
    }
    
    public boolean isHidden() {
        return type == Type.HIDDEN;
    }
}