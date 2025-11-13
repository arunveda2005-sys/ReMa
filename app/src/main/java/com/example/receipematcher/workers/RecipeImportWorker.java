package com.example.receipematcher.workers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.receipematcher.data.db.AiRecipeEntity;
import com.example.receipematcher.data.db.AppDatabase;
import com.example.receipematcher.data.db.dao.AiRecipeDao;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class RecipeImportWorker extends Worker {
    public static final String UNIQUE_NAME = "recipe_import_worker";
    private static final String PREFS = "import_prefs";
    private static final String KEY_IMPORTED = "recipes_imported";

    public RecipeImportWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    public static void enqueueIfNeeded(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean imported = sp.getBoolean(KEY_IMPORTED, false);
        try {
            // If DB is empty, force reimport regardless of the flag
            AppDatabase db = AppDatabase.getDatabase(context);
            AiRecipeDao dao = db.aiRecipeDao();
            long count = 0;
            try { count = dao.count(); } catch (Exception ignored) {}
            if (count == 0) {
                imported = false;
                sp.edit().putBoolean(KEY_IMPORTED, false).apply();
            }
        } catch (Exception ignored) {}
        if (imported) return;
        WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_NAME,
                androidx.work.ExistingWorkPolicy.REPLACE,
                new OneTimeWorkRequest.Builder(RecipeImportWorker.class).build()
        );
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Context ctx = getApplicationContext();
            SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            if (sp.getBoolean(KEY_IMPORTED, false)) return Result.success();

            AssetManager am = ctx.getAssets();
            boolean usedGzip = false;
            InputStream raw;
            try {
                raw = am.open("recipes.jsonl.gz");
                usedGzip = true;
                Log.i("RecipeImportWorker", "Loading assets/recipes.jsonl.gz (gzipped)");
            } catch (java.io.FileNotFoundException fnfGz) {
                try {
                    raw = am.open("recipes.jsonl");
                    usedGzip = false;
                    Log.i("RecipeImportWorker", "Loading assets/recipes.jsonl (plain JSONL)");
                } catch (java.io.FileNotFoundException fnfPlain) {
                    Log.e("RecipeImportWorker", "Missing dataset. Place recipes.jsonl.gz or recipes.jsonl under app/src/main/assets/", fnfPlain);
                    return Result.failure();
                }
            }
            InputStreamReader isr;
            if (usedGzip) {
                isr = new InputStreamReader(new GZIPInputStream(raw), StandardCharsets.UTF_8);
            } else {
                isr = new InputStreamReader(raw, StandardCharsets.UTF_8);
            }
            try (InputStreamReader autoIsr = isr;
                 BufferedReader br = new BufferedReader(autoIsr)) {

                AppDatabase db = AppDatabase.getDatabase(ctx);
                AiRecipeDao dao = db.aiRecipeDao();

                List<AiRecipeEntity> batch = new ArrayList<>(1000);
                String line;
                int total = 0;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    try {
                        JSONObject obj = new JSONObject(line);
                        String name = obj.optString("name", "").trim();
                        if (name.isEmpty()) continue;
                        JSONArray ingr = obj.optJSONArray("ingredients");
                        JSONArray steps = obj.optJSONArray("steps");
                        if (ingr == null) ingr = new JSONArray();
                        if (steps == null) steps = new JSONArray();
                        // normalize ingredients to lowercase
                        JSONArray normIngr = new JSONArray();
                        for (int i = 0; i < ingr.length(); i++) {
                            String s = ingr.optString(i, "");
                            if (s != null) normIngr.put(s.trim().toLowerCase());
                        }
                        AiRecipeEntity e = new AiRecipeEntity(
                                name,
                                normIngr.toString(),
                                steps.toString()
                        );
                        batch.add(e);
                        if (batch.size() >= 1000) {
                            dao.insertAll(batch);
                            total += batch.size();
                            batch.clear();
                            setProgressAsync(new Data.Builder().putInt("imported", total).build());
                        }
                    } catch (Exception ignored) {
                    }
                }
                if (!batch.isEmpty()) {
                    dao.insertAll(batch);
                    total += batch.size();
                }
                sp.edit().putBoolean(KEY_IMPORTED, true).apply();
                Log.i("RecipeImportWorker", "Imported recipes: " + total);
                return Result.success();
            }
        } catch (Exception e) {
            Log.e("RecipeImportWorker", "Import failed", e);
            return Result.retry();
        }
    }
}
