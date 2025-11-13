package com.example.receipematcher.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.receipematcher.data.db.AppDatabase;
import com.example.receipematcher.data.db.dao.PantryDao;
import com.example.receipematcher.data.entities.Ingredient;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



    public class PantryRepository {
        private PantryDao pantryDao;
        private LiveData<List<Ingredient>> allIngredients;
        private ExecutorService executorService;

        public PantryRepository(Application application) {
            AppDatabase db = AppDatabase.getDatabase(application);
            pantryDao = db.pantryDao();
            allIngredients = pantryDao.getAllIngredients();
            executorService = Executors.newSingleThreadExecutor();
        }

        public LiveData<List<Ingredient>> getAllIngredients() {
            return allIngredients;
        }

        public LiveData<List<Ingredient>> getIngredientsSortedByName() {
            return pantryDao.getIngredientsSortedByName();
        }

        public LiveData<List<Ingredient>> getIngredientsSortedByQuantity() {
            return pantryDao.getIngredientsSortedByQuantity();
        }

        public void insert(Ingredient ingredient) {
            executorService.execute(() -> pantryDao.insert(ingredient));
        }

        public void update(Ingredient ingredient) {
            executorService.execute(() -> pantryDao.update(ingredient));
        }

        public void delete(Ingredient ingredient) {
            executorService.execute(() -> pantryDao.delete(ingredient));
        }
    }
