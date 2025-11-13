package com.example.receipematcher.data.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Fts4;

@Fts4(contentEntity = AiRecipeEntity.class)
@Entity(tableName = "recipes_fts")
public class RecipeFtsEntity {
    @NonNull
    public String name;
    @NonNull
    public String ingredientsJson;

    public RecipeFtsEntity(@NonNull String name, @NonNull String ingredientsJson) {
        this.name = name;
        this.ingredientsJson = ingredientsJson;
    }
}
