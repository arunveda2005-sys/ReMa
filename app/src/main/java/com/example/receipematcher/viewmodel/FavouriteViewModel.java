package com.example.receipematcher.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.receipematcher.data.entities.Favourite;
import com.example.receipematcher.data.repository.FavouriteRepository;

import java.util.List;

public class FavouriteViewModel extends AndroidViewModel {
    private final FavouriteRepository repo;
    public FavouriteViewModel(@NonNull Application application) {
        super(application);
        repo = new FavouriteRepository(application);
    }
    public LiveData<List<Favourite>> getAll() { return repo.getAll(); }
    public LiveData<Boolean> isFavourite(String id) { return repo.isFavourite(id); }
    public void add(String id, java.util.List<String> ingredients, java.util.List<String> steps) { repo.add(id, ingredients, steps); }
    public void remove(String id) { repo.remove(id); }
}
