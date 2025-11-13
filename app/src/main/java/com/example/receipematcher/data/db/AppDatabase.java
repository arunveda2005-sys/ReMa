package com.example.receipematcher.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.receipematcher.data.db.dao.PantryDao;
import com.example.receipematcher.data.db.dao.FavouriteDao;
import com.example.receipematcher.data.db.dao.RecipeDao;
import com.example.receipematcher.data.db.dao.ShoppingDao;
import com.example.receipematcher.data.db.dao.AiRecipeDao;
import com.example.receipematcher.data.entities.Ingredient;
import com.example.receipematcher.data.entities.Recipe;
import com.example.receipematcher.data.entities.Favourite;
import com.example.receipematcher.data.entities.ShoppingItem;

@Database(entities = {Ingredient.class, Recipe.class, Favourite.class, ShoppingItem.class, AiRecipeEntity.class, RecipeFtsEntity.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract PantryDao pantryDao();
    public abstract RecipeDao recipeDao();
    public abstract FavouriteDao favouriteDao();
    public abstract ShoppingDao shoppingDao();
    public abstract AiRecipeDao aiRecipeDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "recipe_matcher_db"
                    )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
