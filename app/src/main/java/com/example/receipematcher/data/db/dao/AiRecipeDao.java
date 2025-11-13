package com.example.receipematcher.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.receipematcher.data.db.AiRecipeEntity;

import java.util.List;

@Dao
public interface AiRecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AiRecipeEntity> items);

    @Query("SELECT COUNT(*) FROM ai_recipes")
    long count();

    // Full-text search across name and ingredients via FTS table
    @Query("SELECT ai_recipes.* FROM ai_recipes JOIN recipes_fts ON ai_recipes.rowid = recipes_fts.docid WHERE recipes_fts MATCH :match ORDER BY ai_recipes.name LIMIT :limit")
    List<AiRecipeEntity> searchByFts(String match, int limit);
}
