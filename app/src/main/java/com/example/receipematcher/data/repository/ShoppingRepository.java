package com.example.receipematcher.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.receipematcher.data.db.AppDatabase;
import com.example.receipematcher.data.db.dao.ShoppingDao;
import com.example.receipematcher.data.entities.ShoppingItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShoppingRepository {
    private final ShoppingDao shoppingDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ShoppingRepository(Application app) {
        AppDatabase db = AppDatabase.getDatabase(app);
        this.shoppingDao = db.shoppingDao();
    }

    public LiveData<List<ShoppingItem>> getAll() {
        return shoppingDao.getAll();
    }

    public void insert(ShoppingItem item) {
        executor.execute(() -> shoppingDao.insert(item));
    }

    public void update(ShoppingItem item) {
        executor.execute(() -> shoppingDao.update(item));
    }

    public void delete(ShoppingItem item) {
        executor.execute(() -> shoppingDao.delete(item));
    }

    public void clearCompleted() {
        executor.execute(shoppingDao::clearCompleted);
    }

    public void clearAll() {
        executor.execute(shoppingDao::clearAll);
    }

    public ShoppingItem getByNameSync(String name) {
        try {
            return shoppingDao.getByNameSync(name);
        } catch (Exception e) {
            return null;
        }
    }

    public void incrementQuantity(ShoppingItem item, int delta) {
        executor.execute(() -> {
            double q = item.quantity <= 0 ? 0 : item.quantity;
            item.quantity = Math.max(0, q + delta);
            shoppingDao.update(item);
        });
    }
    
    public void insertSync(ShoppingItem item) {
        shoppingDao.insert(item);
    }
    
    public List<ShoppingItem> getAllSync() {
        return shoppingDao.getAllSync();
    }
}