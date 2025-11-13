package com.example.receipematcher.utils;

import com.example.receipematcher.data.entities.Ingredient;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for calculating ingredient freshness status based on expiry dates
 */
public class IngredientStatusCalculator {
    
    public enum IngredientStatus {
        FRESH,          // 7+ days until expiry
        EXPIRING,       // 1-7 days until expiry
        EXPIRED,        // Past expiry date
        RECENTLY_ADDED, // Added within last 24 hours
        UNKNOWN         // No expiry date or invalid date
    }
    
    private static final SimpleDateFormat[] DATE_FORMATS = {
        new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
        new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
        new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    };
    
    /**
     * Calculate the status of an ingredient based on its expiry date
     * @param ingredient The ingredient to check
     * @return The status of the ingredient
     */
    public static IngredientStatus calculateStatus(Ingredient ingredient) {
        if (ingredient == null || ingredient.expiryDate == null || ingredient.expiryDate.trim().isEmpty()) {
            return IngredientStatus.UNKNOWN;
        }
        
        Date expiryDate = parseDate(ingredient.expiryDate);
        if (expiryDate == null) {
            return IngredientStatus.UNKNOWN;
        }
        
        Date currentDate = new Date();
        long diffInDays = normalizeDay(expiryDate) - normalizeDay(currentDate);
        
        // Check if recently added (within 24 hours)
        // Note: This would require a dateAdded field in the Ingredient entity
        // For now, we'll skip this check and focus on expiry-based status
        
        if (diffInDays < 0) {
            return IngredientStatus.EXPIRED;
        } else if (diffInDays <= 7) {
            return IngredientStatus.EXPIRING;
        } else {
            return IngredientStatus.FRESH;
        }
    }
    
    /**
     * Get the number of days until expiry (negative if expired)
     * @param ingredient The ingredient to check
     * @return Days until expiry, or null if date is invalid
     */
    public static Long getDaysUntilExpiry(Ingredient ingredient) {
        if (ingredient == null || ingredient.expiryDate == null || ingredient.expiryDate.trim().isEmpty()) {
            return null;
        }
        
        Date expiryDate = parseDate(ingredient.expiryDate);
        if (expiryDate == null) {
            return null;
        }
        
        Date currentDate = new Date();
        return normalizeDay(expiryDate) - normalizeDay(currentDate);
    }
    
    /**
     * Get a human-readable status description
     * @param status The ingredient status
     * @param daysUntilExpiry Days until expiry (can be null)
     * @return Human-readable status string
     */
    public static String getStatusDescription(IngredientStatus status, Long daysUntilExpiry) {
        switch (status) {
            case FRESH:
                if (daysUntilExpiry != null && daysUntilExpiry > 30) {
                    return "Fresh (30+ days)";
                } else if (daysUntilExpiry != null) {
                    return "Fresh (" + daysUntilExpiry + " days)";
                }
                return "Fresh";
            case EXPIRING:
                if (daysUntilExpiry != null) {
                    if (daysUntilExpiry == 0) {
                        return "Expires today";
                    }
                    return "Expires in " + daysUntilExpiry + " day" + (daysUntilExpiry == 1 ? "" : "s");
                }
                return "Expiring soon";
            case EXPIRED:
                if (daysUntilExpiry != null) {
                    long daysExpired = Math.abs(daysUntilExpiry);
                    return "Expired " + daysExpired + " day" + (daysExpired == 1 ? "" : "s") + " ago";
                }
                return "Expired";
            case RECENTLY_ADDED:
                return "Recently added";
            case UNKNOWN:
            default:
                return "Unknown expiry";
        }
    }
    
    /**
     * Parse date string using multiple formats
     * @param dateString The date string to parse
     * @return Parsed Date object or null if parsing fails
     */
    private static Date parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        String trimmedDate = dateString.trim();
        
        for (SimpleDateFormat format : DATE_FORMATS) {
            try {
                return format.parse(trimmedDate);
            } catch (ParseException e) {
                // Try next format
            }
        }
        
        return null;
    }

    private static long normalizeDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis() / (24L * 60L * 60L * 1000L);
    }
    
    /**
     * Check if an ingredient is considered critical (expired or expiring within 1 day)
     * @param ingredient The ingredient to check
     * @return true if the ingredient needs immediate attention
     */
    public static boolean isCritical(Ingredient ingredient) {
        IngredientStatus status = calculateStatus(ingredient);
        return status == IngredientStatus.EXPIRED || 
               (status == IngredientStatus.EXPIRING && getDaysUntilExpiry(ingredient) != null && getDaysUntilExpiry(ingredient) <= 1);
    }
    
    /**
     * Get the appropriate background drawable resource ID for the ingredient status
     * @param status The ingredient status
     * @return Resource ID for the background drawable
     */
    public static int getStatusBackgroundResource(IngredientStatus status) {
        switch (status) {
            case FRESH:
                return com.example.receipematcher.R.drawable.gradient_fresh;
            case EXPIRING:
                return com.example.receipematcher.R.drawable.gradient_expiring;
            case EXPIRED:
                return com.example.receipematcher.R.drawable.gradient_expired;
            case RECENTLY_ADDED:
                return com.example.receipematcher.R.drawable.gradient_recently_added;
            case UNKNOWN:
            default:
                return com.example.receipematcher.R.drawable.gradient_fresh; // Default to fresh
        }
    }
    
    /**
     * Get the appropriate color resource ID for the status indicator
     * @param status The ingredient status
     * @return Color resource ID
     */
    public static int getStatusColorResource(IngredientStatus status) {
        switch (status) {
            case FRESH:
                return com.example.receipematcher.R.color.success;
            case EXPIRING:
                return com.example.receipematcher.R.color.warning;
            case EXPIRED:
                return com.example.receipematcher.R.color.error;
            case RECENTLY_ADDED:
                return com.example.receipematcher.R.color.primary;
            case UNKNOWN:
            default:
                return com.example.receipematcher.R.color.gray_400;
        }
    }
}