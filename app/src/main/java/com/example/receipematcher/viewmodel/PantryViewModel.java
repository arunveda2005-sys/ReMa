package com.example.receipematcher.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.receipematcher.data.entities.Ingredient;
import com.example.receipematcher.data.repository.PantryRepository;

import java.util.List;

public class PantryViewModel extends AndroidViewModel {
    private PantryRepository repository;
    private LiveData<List<Ingredient>> allIngredients;

    public PantryViewModel(@NonNull Application application) {
        super(application);
        repository = new PantryRepository(application);
        allIngredients = repository.getAllIngredients();
    }

    public LiveData<List<Ingredient>> getAllIngredients() {
        return allIngredients;
    }

    public LiveData<List<Ingredient>> getIngredientsSortedByName() {
        return repository.getIngredientsSortedByName();
    }

    public LiveData<List<Ingredient>> getIngredientsSortedByQuantity() {
        return repository.getIngredientsSortedByQuantity();
    }

    public void insert(Ingredient ingredient) { repository.insert(ingredient); }
    public void update(Ingredient ingredient) { repository.update(ingredient); }
    public void delete(Ingredient ingredient) { repository.delete(ingredient); }
}
