package com.example.receipematcher.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.receipematcher.data.entities.ShoppingItem;
import com.example.receipematcher.data.repository.ShoppingRepository;

import java.util.List;

public class ShoppingViewModel extends AndroidViewModel {
    private final ShoppingRepository repo;
    public ShoppingViewModel(@NonNull Application application) {
        super(application);
        repo = new ShoppingRepository(application);
    }
    public LiveData<List<ShoppingItem>> getAll() { return repo.getAll(); }
    public void insert(ShoppingItem item) { repo.insert(item); }
    public void update(ShoppingItem item) { repo.update(item); }
    public void delete(ShoppingItem item) { repo.delete(item); }
    public void clearCompleted() { repo.clearCompleted(); }
    public void clearAll() { repo.clearAll(); }
    public void incrementQuantity(com.example.receipematcher.data.entities.ShoppingItem item, int delta) { repo.incrementQuantity(item, delta); }
}

