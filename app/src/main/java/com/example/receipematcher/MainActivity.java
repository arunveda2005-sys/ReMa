package com.example.receipematcher;

import android.os.Bundle;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.receipematcher.databinding.ActivityMainBinding;
import com.example.receipematcher.util.NotificationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.receipematcher.work.ExpiryCheckWorker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        // Bottom navigation view
        BottomNavigationView bottomNav = binding.bottomNavigation;

        // Navigation host fragment
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // Setup bottom navigation with nav controller
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Hide bottom nav on detail screens
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.aiRecipeDetailFragment) {
                bottomNav.setVisibility(View.GONE);
            } else {
                bottomNav.setVisibility(View.VISIBLE);
            }
        });

        // Create notification channel and request permission if needed (Android 13+)
        NotificationHelper.createChannels(this);
        requestPostNotificationsIfNeeded();

        // Schedule periodic worker for expiry checks (once a day)
        scheduleExpiryWorker();

        // DEBUG ONLY: trigger expiry checks immediately once
        if (BuildConfig.DEBUG) {
            androidx.work.OneTimeWorkRequest req =
                    new androidx.work.OneTimeWorkRequest.Builder(ExpiryCheckWorker.class)
                            .build();
            WorkManager.getInstance(this).enqueue(req);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_favorites) {
            if (navController != null) navController.navigate(R.id.favouritesFragment);
            return true;
        } else if (item.getItemId() == R.id.menu_shopping) {
            if (navController != null) navController.navigate(R.id.shoppingFragment);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestPostNotificationsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        100);
            }
        }
    }

    private void scheduleExpiryWorker() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                ExpiryCheckWorker.class,
                24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .addTag("expiry_worker")
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "expiry_worker_unique",
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }
    

}
