package com.example.receipematcher.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.receipematcher.R;
import com.example.receipematcher.data.entities.Ingredient;

import java.util.List;

public class NotificationHelper {
    public static final String EXPIRY_CHANNEL_ID = "expiry_channel";

    public static void createChannels(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager.getNotificationChannel(EXPIRY_CHANNEL_ID) == null) {
            NotificationChannel channel = new NotificationChannel(
                    EXPIRY_CHANNEL_ID,
                    "Expiry Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for ingredients nearing expiry");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            manager.createNotificationChannel(channel);
        }
    }

    public static void showExpiryNotification(Context context, List<Ingredient> items) {
        if (items == null || items.isEmpty()) return;

        StringBuilder content = new StringBuilder();
        int max = Math.min(3, items.size());
        for (int i = 0; i < max; i++) {
            Ingredient ing = items.get(i);
            content.append(ing.name);
            if (ing.expiryDate != null && !ing.expiryDate.isEmpty()) {
                content.append(" (exp: ").append(ing.expiryDate).append(")");
            }
            if (i < max - 1) content.append(", ");
        }
        if (items.size() > max) {
            content.append(" +").append(items.size() - max).append(" more");
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, EXPIRY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Ingredients nearing expiry")
                .setContentText(content.toString())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content.toString()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(1001, builder.build());
    }

    public static void showExpiryNotification(Context context, List<Ingredient> items, String title, int notificationId) {
        if (items == null || items.isEmpty()) return;

        StringBuilder content = new StringBuilder();
        int max = Math.min(3, items.size());
        for (int i = 0; i < max; i++) {
            Ingredient ing = items.get(i);
            content.append(ing.name);
            if (ing.expiryDate != null && !ing.expiryDate.isEmpty()) {
                content.append(" (exp: ").append(ing.expiryDate).append(")");
            }
            if (i < max - 1) content.append(", ");
        }
        if (items.size() > max) {
            content.append(" +").append(items.size() - max).append(" more");
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, EXPIRY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content.toString())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content.toString()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }
}
