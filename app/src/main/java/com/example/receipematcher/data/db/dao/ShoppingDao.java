package com.example.receipematcher.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.receipematcher.data.entities.ShoppingItem;

import java.util.List;

@Dao
public interface ShoppingDao {
    @Insert
    void insert(ShoppingItem item);

    @Update
    void update(ShoppingItem item);

    @Delete
    void delete(ShoppingItem item);

    @Query("SELECT * FROM shopping_items ORDER BY completed ASC, addedAt DESC")
    LiveData<List<ShoppingItem>> getAll();

    @Query("DELETE FROM shopping_items WHERE completed = 1")
    void clearCompleted();

    @Query("DELETE FROM shopping_items")
    void clearAll();

    @Query("SELECT * FROM shopping_items WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    ShoppingItem getByNameSync(String name);
    
    @Query("SELECT * FROM shopping_items ORDER BY completed ASC, addedAt DESC")
    List<ShoppingItem> getAllSync();
}