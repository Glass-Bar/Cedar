package com.ciube.glass.launcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Main entry point for the app.
 * 
 * Handles both:
 * 1. Voice trigger "ok glass, launch app" — opens AppListActivity immediately
 * 2. Tap from home screen — same behavior
 */
public class MainActivity extends Activity {

    private static final String TAG = "glAss launcher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "MainActivity onCreate - intent action: " + getIntent().getAction());

        // Open the app list immediately
        Intent appListIntent = new Intent(this, AppListActivity.class);
        appListIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(appListIntent);

        // Finish this activity so it doesn't stay in the back stack
        finish();
    }
}
