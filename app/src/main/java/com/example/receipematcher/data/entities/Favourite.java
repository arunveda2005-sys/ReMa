package com.example.receipematcher.data.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "favourites", indices = {@Index(value = {"recipeId"}, unique = true)})
public class Favourite {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String recipeId; // references Recipe.idMeal

    public long addedAt; // epoch millis

    // Snapshot of AI recipe to render details later
    public String ingredientsJson; // JSON array of strings
    public String stepsJson;       // JSON array of strings
}
