package com.example.receipematcher.data.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ai_recipes")
public class AiRecipeEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;

    @NonNull
    public String ingredientsJson;

    @NonNull
    public String stepsJson;

    public AiRecipeEntity(@NonNull String name, @NonNull String ingredientsJson, @NonNull String stepsJson) {
        this.name = name;
        this.ingredientsJson = ingredientsJson;
        this.stepsJson = stepsJson;
    }
}
