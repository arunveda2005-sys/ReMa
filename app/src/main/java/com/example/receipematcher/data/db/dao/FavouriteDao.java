package com.example.receipematcher.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.receipematcher.data.entities.Favourite;

import java.util.List;

@Dao
public interface FavouriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Favourite favourite);

    @Delete
    void delete(Favourite favourite);

    @Query("DELETE FROM favourites WHERE recipeId = :recipeId")
    void deleteByRecipeId(String recipeId);

    @Query("SELECT * FROM favourites ORDER BY addedAt DESC")
    LiveData<List<Favourite>> getAll();

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE recipeId = :recipeId)")
    LiveData<Boolean> isFavourite(String recipeId);

    @Query("SELECT * FROM favourites WHERE recipeId = :recipeId LIMIT 1")
    Favourite getByRecipeIdSync(String recipeId);
}
