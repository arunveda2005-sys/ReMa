package com.example.receipematcher.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.receipematcher.data.repository.RecipeRepository;

import java.util.List;
import com.example.receipematcher.data.repository.RecipeRepository.AiRecipe;

public class RecipeViewModel extends ViewModel {

    private final RecipeRepository repository;

    public RecipeViewModel() {
        repository = RecipeRepository.getInstance();
    }

    // AI recipes via Gemini
    public LiveData<List<AiRecipe>> getAiRecipes(List<String> pantry) {
        return repository.getAiRecipes(pantry);
    }
}
