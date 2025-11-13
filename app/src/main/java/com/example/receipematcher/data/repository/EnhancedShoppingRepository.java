package com.example.receipematcher.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.receipematcher.data.db.AppDatabase;
import com.example.receipematcher.data.db.dao.ShoppingDao;
import com.example.receipematcher.data.entities.ShoppingItem;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Enhanced shopping repository with support for shopping items
 */
public class EnhancedShoppingRepository {
    private final ShoppingDao shoppingDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public EnhancedShoppingRepository(Application app) {
        AppDatabase db = AppDatabase.getDatabase(app);
        this.shoppingDao = db.shoppingDao();
    }

    // Enhanced Shopping Item operations
    public CompletableFuture<Void> insertItems(List<ShoppingItem> items) {
        return CompletableFuture.runAsync(() -> {
            for (ShoppingItem item : items) {
                item.addedAt = new Date();
                shoppingDao.insert(item);
            }
        }, executor);
    }
    
    public CompletableFuture<Void> markItemCompleted(ShoppingItem item) {
        return CompletableFuture.runAsync(() -> {
            item.completed = true;
            item.completedAt = new Date();
            shoppingDao.update(item);
        }, executor);
    }
    
    public CompletableFuture<Void> markItemIncomplete(ShoppingItem item) {
        return CompletableFuture.runAsync(() -> {
            item.completed = false;
            item.completedAt = null;
            shoppingDao.update(item);
        }, executor);
    }
    
    public CompletableFuture<Void> updateItemQuantity(ShoppingItem item, double newQuantity) {
        return CompletableFuture.runAsync(() -> {
            item.quantity = newQuantity;
            shoppingDao.update(item);
        }, executor);
    }
    
    public CompletableFuture<Void> assignItemToUser(ShoppingItem item, String userId) {
        return CompletableFuture.runAsync(() -> {
            item.assignedTo = userId;
            shoppingDao.update(item);
        }, executor);
    }

    // Bulk operations
    public CompletableFuture<Void> markMultipleItemsCompleted(List<ShoppingItem> items) {
        return CompletableFuture.runAsync(() -> {
            Date completedAt = new Date();
            for (ShoppingItem item : items) {
                item.completed = true;
                item.completedAt = completedAt;
                shoppingDao.update(item);
            }
        }, executor);
    }
    
    public CompletableFuture<Void> deleteMultipleItems(List<ShoppingItem> items) {
        return CompletableFuture.runAsync(() -> {
            for (ShoppingItem item : items) {
                shoppingDao.delete(item);
            }
        }, executor);
    }

    // Analytics and insights
    public CompletableFuture<List<String>> getMostFrequentCategories() {
        return CompletableFuture.supplyAsync(() -> {
            // Get most frequently shopped categories
            // This would need proper database aggregation
            return List.of("Produce", "Dairy", "Meat & Seafood", "Pantry");
        }, executor);
    }

    // Legacy support methods
    public LiveData<List<ShoppingItem>> getAll() {
        return shoppingDao.getAll();
    }

    public void insert(ShoppingItem item) {
        executor.execute(() -> {
            item.addedAt = new Date();
            shoppingDao.insert(item);
        });
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
}