package com.example.myproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

/**
 * Splash Activity that serves as the app's entry point
 * Features:
 * - Displays Lottie animation during app loading
 * - Checks for existing user session in SharedPreferences
 * - Routes users to appropriate activity (registration or lessons)
 * - Provides smooth transition with 2-second delay
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Configure status bar appearance for Android M and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //status bar icons black
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        super.onCreate(savedInstanceState);

        // Hide the default action bar for clean splash screen appearance
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_splash); // the xml file

        // Initialize and configure Lottie animation for splash screen
        LottieAnimationView animationView = findViewById(R.id.lottieAnimationView);
        // Enable merge paths for better performance on KitKat and above
        animationView.enableMergePathsForKitKatAndAbove(true);

        // Set up delayed navigation logic after 2 seconds
        // Uses Handler to post delayed action on main UI thread
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Check SharedPreferences for existing user session
            SharedPreferences prefs = getSharedPreferences("my_prefs", MODE_PRIVATE);
            long userId = prefs.getLong("userId", -1); // Get saved user ID, default to -1 if not found

            Intent intent;
            // Route user based on whether they have an existing session
            if (userId == -1) { //no user
                // No existing user found - navigate to registration
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else { //there is a user
                // Existing user found - navigate directly to lessons
                intent = new Intent(SplashActivity.this, LessonsActivity.class);
            }

            // Start the appropriate activity and close splash screen
            startActivity(intent);
            finish(); // Close splash activity to prevent back navigation
        }, 2000); // 2-second delay for splash screen display
    }
}