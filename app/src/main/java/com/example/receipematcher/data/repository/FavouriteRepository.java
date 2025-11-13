package com.example.receipematcher.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.receipematcher.data.db.AppDatabase;
import com.example.receipematcher.data.db.dao.FavouriteDao;
import com.example.receipematcher.data.entities.Favourite;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavouriteRepository {
    private final FavouriteDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public FavouriteRepository(Application app) {
        dao = AppDatabase.getDatabase(app).favouriteDao();
    }

    public LiveData<List<Favourite>> getAll() { return dao.getAll(); }

    public LiveData<Boolean> isFavourite(String recipeId) { return dao.isFavourite(recipeId); }

    public void add(String recipeId, java.util.List<String> ingredients, java.util.List<String> steps) {
        Favourite f = new Favourite();
        f.recipeId = recipeId;
        f.addedAt = System.currentTimeMillis();
        try {
            org.json.JSONArray i = new org.json.JSONArray();
            if (ingredients != null) for (String s : ingredients) i.put(s);
            org.json.JSONArray st = new org.json.JSONArray();
            if (steps != null) for (String s : steps) st.put(s);
            f.ingredientsJson = i.toString();
            f.stepsJson = st.toString();
        } catch (Exception ignored) {}
        executor.execute(() -> dao.insert(f));
    }

    public void remove(String recipeId) {
        executor.execute(() -> dao.deleteByRecipeId(recipeId));
    }
}
