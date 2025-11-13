package com.example.receipematcher.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.receipematcher.data.entities.Ingredient;

import java.util.List;

@Dao
public interface PantryDao {
    @Insert
    void insert(Ingredient ingredient);

    @Update
    void update(Ingredient ingredient);

    @Delete
    void delete(Ingredient ingredient);

    @Query("SELECT * FROM ingredients ORDER BY expiryDate ASC")
    LiveData<List<Ingredient>> getAllIngredients();

    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    LiveData<List<Ingredient>> getIngredientsSortedByName();

    @Query("SELECT * FROM ingredients ORDER BY quantity DESC")
    LiveData<List<Ingredient>> getIngredientsSortedByQuantity();

    // Synchronous queries for background work (assumes expiryDate stored as ISO yyyy-MM-dd)
    @Query("SELECT * FROM ingredients WHERE expiryDate IS NOT NULL AND expiryDate != '' AND expiryDate <= :boundary ORDER BY expiryDate ASC")
    List<Ingredient> getExpiringOnOrBefore(String boundary);

    @Query("SELECT * FROM ingredients WHERE expiryDate = :date ORDER BY expiryDate ASC")
    List<Ingredient> getExpiringOn(String date);

    @Query("SELECT * FROM ingredients")
    List<Ingredient> getAllSync();
}