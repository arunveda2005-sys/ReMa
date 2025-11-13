package com.example.receipematcher.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.receipematcher.data.converters.DateConverter;

import java.util.Date;

@Entity(tableName = "shopping_items")
@TypeConverters(DateConverter.class)
public class ShoppingItem {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public double quantity; // Changed to double for decimal quantities
    public String unit; // e.g., kg, g, pcs

    public boolean completed; // checkbox status
    public String sourceRecipeId; // optional link to Recipe.idMeal
    public int shoppingListId; // Link to ShoppingList
    public String category; // Store section category (Produce, Dairy, etc.)
    public int priority; // 1-5, higher number = higher priority
    public double estimatedPrice; // For budget tracking
    public String notes; // Additional notes for the item
    public String assignedTo; // For shared shopping lists
    public Date addedAt;
    public Date completedAt;
}
