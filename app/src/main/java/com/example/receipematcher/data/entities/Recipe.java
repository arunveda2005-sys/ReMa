package com.example.receipematcher.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recipes")
public class Recipe {

    @PrimaryKey
    @NonNull
    public String idMeal; // matches remote ID

    public String name;
    public String thumbnailUrl;
    public String instructions;

    public long cachedAt; // epoch millis to manage cache expiry
}
