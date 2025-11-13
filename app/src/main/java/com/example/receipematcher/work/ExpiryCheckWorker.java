package com.example.receipematcher.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.receipematcher.data.db.AppDatabase;
import com.example.receipematcher.data.db.dao.PantryDao;
import com.example.receipematcher.data.entities.Ingredient;
import com.example.receipematcher.util.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ExpiryCheckWorker extends Worker {

    public ExpiryCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Context ctx = getApplicationContext();
            NotificationHelper.createChannels(ctx);

            PantryDao pantryDao = AppDatabase.getDatabase(ctx).pantryDao();
            List<Ingredient> all = pantryDao.getAllSync();

            long todayIndex = normalizeDay(new java.util.Date());
            java.util.ArrayList<Ingredient> threeDayItems = new java.util.ArrayList<>();
            java.util.ArrayList<Ingredient> oneDayItems = new java.util.ArrayList<>();

            int sampled = 0;
            for (Ingredient ing : all) {
                if (ing.expiryDate == null) continue;
                java.util.Date d = parseDateFlexible(ing.expiryDate);
                if (d == null) continue;
                long idx = normalizeDay(d);
                int daysUntil = (int)(idx - todayIndex);
                if (daysUntil == 3) threeDayItems.add(ing);
                if (daysUntil == 1) oneDayItems.add(ing);
                if (sampled < 5) {
                    android.util.Log.d("ExpiryCheck", "item=" + ing.name + ", raw='" + ing.expiryDate + "', daysUntil=" + daysUntil);
                    sampled++;
                }
            }

            android.util.Log.d("ExpiryCheck", "Matched +3d count=" + threeDayItems.size());
            android.util.Log.d("ExpiryCheck", "Matched +1d count=" + oneDayItems.size());

            if (!threeDayItems.isEmpty()) {
                NotificationHelper.showExpiryNotification(ctx, threeDayItems, "Expiring in 3 days", 1003);
            }
            if (!oneDayItems.isEmpty()) {
                NotificationHelper.showExpiryNotification(ctx, oneDayItems, "Expiring tomorrow", 1002);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }

    private String getBoundaryDate(int addDays) {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_YEAR, addDays);
        Date date = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(date);
    }

    private List<Ingredient> filterByDate(List<Ingredient> items, String target) {
        if (items == null || target == null) return java.util.Collections.emptyList();
        Date targetDate = parseDateFlexible(target);
        if (targetDate == null) return java.util.Collections.emptyList();
        long targetDay = normalizeDay(targetDate);
        java.util.ArrayList<Ingredient> out = new java.util.ArrayList<>();
        for (Ingredient ing : items) {
            String d = ing.expiryDate;
            if (d == null) continue;
            Date dd = parseDateFlexible(d.trim());
            if (dd != null && normalizeDay(dd) == targetDay) out.add(ing);
        }
        return out;
    }

    private Date parseDateFlexible(String input) {
        if (input == null) return null;
        String norm = input.trim().replace(" ", "").replace('-', '/');
        String[] patterns = {
                "dd/MM/yyyy",
                "d/M/yyyy",
                "dd/MM/yy",
                "d/M/yy",
                "dd.MM.yyyy",
                "d.M.yyyy",
                "dd MMM yyyy"
        };
        for (String p : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(p, Locale.getDefault());
                sdf.setLenient(false);
                sdf.setTimeZone(TimeZone.getDefault());
                return sdf.parse(norm);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private long normalizeDay(Date date) {
        Calendar c = Calendar.getInstance(TimeZone.getDefault());
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis() / (24L * 60L * 60L * 1000L);
    }
}
