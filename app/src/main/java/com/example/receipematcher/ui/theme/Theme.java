package com.example.receipematcher.ui.theme;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public class Theme{

    // Color scheme for light theme
    public static class LightColorScheme {
        public static final int PRIMARY = Colors.PURPLE_40;
        public static final int SECONDARY = Colors.PURPLE_GREY_40;
        public static final int TERTIARY = Colors.PINK_40;
        public static final int BACKGROUND = 0xFFFFFBFE;
        public static final int SURFACE = 0xFFFFFBFE;
        public static final int ON_PRIMARY = 0xFFFFFFFF; // White
        public static final int ON_SECONDARY = 0xFFFFFFFF; // White
        public static final int ON_TERTIARY = 0xFFFFFFFF; // White
        public static final int ON_BACKGROUND = 0xFF1C1B1F;
        public static final int ON_SURFACE = 0xFF1C1B1F;
    }

    // Color scheme for dark theme
    public static class DarkColorScheme {
        public static final int PRIMARY = Colors.PURPLE_80;
        public static final int SECONDARY = Colors.PURPLE_GREY_80;
        public static final int TERTIARY = Colors.PINK_80;
        public static final int BACKGROUND = 0xFF1C1B1F;
        public static final int SURFACE = 0xFF1C1B1F;
        public static final int ON_PRIMARY = 0xFF1C1B1F;
        public static final int ON_SECONDARY = 0xFF1C1B1F;
        public static final int ON_TERTIARY = 0xFF1C1B1F;
        public static final int ON_BACKGROUND = 0xFFFFFBFE;
        public static final int ON_SURFACE = 0xFFFFFBFE;
    }

    /**
     * Check if the system is in dark theme mode
     */
    public static boolean isSystemInDarkTheme(@NonNull Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Check if dynamic colors are supported (Android 12+)
     */
    public static boolean isDynamicColorSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }

    /**
     * Get primary color based on current theme
     */
    @ColorInt
    public static int getPrimaryColor(@NonNull Context context, boolean useDynamicColor) {
        if (useDynamicColor && isDynamicColorSupported()) {
            // For dynamic colors, you would typically use Material You colors
            // This is a simplified approach - you might want to use actual dynamic colors
            return isSystemInDarkTheme(context) ? DarkColorScheme.PRIMARY : LightColorScheme.PRIMARY;
        }

        return isSystemInDarkTheme(context) ? DarkColorScheme.PRIMARY : LightColorScheme.PRIMARY;
    }

    /**
     * Get secondary color based on current theme
     */
    @ColorInt
    public static int getSecondaryColor(@NonNull Context context, boolean useDynamicColor) {
        if (useDynamicColor && isDynamicColorSupported()) {
            return isSystemInDarkTheme(context) ? DarkColorScheme.SECONDARY : LightColorScheme.SECONDARY;
        }

        return isSystemInDarkTheme(context) ? DarkColorScheme.SECONDARY : LightColorScheme.SECONDARY;
    }

    /**
     * Get background color based on current theme
     */
    @ColorInt
    public static int getBackgroundColor(@NonNull Context context) {
        return isSystemInDarkTheme(context) ? DarkColorScheme.BACKGROUND : LightColorScheme.BACKGROUND;
    }

    /**
     * Get surface color based on current theme
     */
    @ColorInt
    public static int getSurfaceColor(@NonNull Context context) {
        return isSystemInDarkTheme(context) ? DarkColorScheme.SURFACE : LightColorScheme.SURFACE;
    }

    /**
     * Get text color for primary background
     */
    @ColorInt
    public static int getOnPrimaryColor(@NonNull Context context) {
        return isSystemInDarkTheme(context) ? DarkColorScheme.ON_PRIMARY : LightColorScheme.ON_PRIMARY;
    }

    /**
     * Get text color for background
     */
    @ColorInt
    public static int getOnBackgroundColor(@NonNull Context context) {
        return isSystemInDarkTheme(context) ? DarkColorScheme.ON_BACKGROUND : LightColorScheme.ON_BACKGROUND;
    }

    /**
     * Apply theme colors to an Activity
     */
    public static void applyTheme(@NonNull Activity activity, boolean useDynamicColor) {
        // You can set the theme programmatically here
        // For example, changing status bar colors, etc.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(getPrimaryColor(activity, useDynamicColor));
        }
    }

    // Private constructor to prevent instantiation
    private Theme() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}