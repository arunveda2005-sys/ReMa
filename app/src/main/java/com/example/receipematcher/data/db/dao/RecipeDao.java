package com.example.receipematcher.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.receipematcher.data.entities.Recipe;

import java.util.List;

@Dao
public interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Recipe recipe);

    @Update
    void update(Recipe recipe);

    @Query("SELECT * FROM recipes WHERE idMeal = :id LIMIT 1")
    Recipe getById(String id);

    @Query("SELECT * FROM recipes WHERE name LIKE '%' || :name || '%' ORDER BY name ASC")
    LiveData<List<Recipe>> searchByName(String name);

    @Query("DELETE FROM recipes WHERE cachedAt < :olderThan")
    void deleteOlderThan(long olderThan);

    @Query("SELECT * FROM recipes ORDER BY cachedAt DESC")
    LiveData<List<Recipe>> getAll();
}
