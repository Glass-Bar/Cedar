package com.ciube.glass.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardScrollView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Immersion activity that shows all launchable installed packages in a
 * {@link CardScrollView}.
 *
 * <h3>Interaction model (Glass touchpad)</h3>
 * <ul>
 *   <li><b>Swipe forward / backward</b> — scroll through apps (handled by
 *       {@code CardScrollView} natively).</li>
 *   <li><b>Tap</b> — launch the selected app and finish this activity.</li>
 *   <li><b>Swipe down</b> — exit the immersion (standard Glass back gesture,
 *       handled by the framework via {@code finish()} in onBackPressed).</li>
 * </ul>
 *
 * <h3>Package enumeration</h3>
 * We query for apps that declare a CATEGORY_LAUNCHER intent, which is the same
 * set that would appear in an Android home screen launcher.  The current app
 * itself is excluded to avoid launching ourselves recursively.
 */
public class AppListActivity extends Activity {

    private static final String TAG = "AppListActivity";

    private CardScrollView mCardScrollView;
    private AppScrollAdapter mAdapter;
    private List<AppInfo> mApps = new ArrayList<AppInfo>();

    // -------------------------------------------------------------------------
    // Activity lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // CardScrollView is the root view — no extra layout XML needed.
        mCardScrollView = new CardScrollView(this);
        mAdapter = new AppScrollAdapter(this, mApps);
        mCardScrollView.setAdapter(mAdapter);
        mCardScrollView.activate();

        // Handle tap gestures on the currently visible card.
        mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                launchApp(position);
            }
        });

        setContentView(mCardScrollView);

        // Load the app list in the background so we don't block the UI thread.
        new LoadAppsTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScrollView.activate();
    }

    @Override
    protected void onPause() {
        mCardScrollView.deactivate();
        super.onPause();
    }

    // -------------------------------------------------------------------------
    // App loading
    // -------------------------------------------------------------------------

    /**
     * Queries the PackageManager for all apps that have a CATEGORY_LAUNCHER
     * activity, sorts them alphabetically, and populates the adapter.
     * Also adds a "Stop Launcher" option at the very end.
     */
    private class LoadAppsTask extends AsyncTask<Void, Void, List<AppInfo>> {

        @Override
        protected List<AppInfo> doInBackground(Void... params) {
            PackageManager pm = getPackageManager();

            // Intent that matches all launcher-visible activities.
            Intent mainIntent = new Intent(Intent.ACTION_MAIN);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
            List<AppInfo> result = new ArrayList<AppInfo>();

            for (ResolveInfo ri : resolveInfos) {
                String pkg = ri.activityInfo.packageName;

                // Skip our own package so we don't show "App Launcher" in the list.
                if (pkg.equals(getPackageName())) {
                    continue;
                }

                String label;
                try {
                    label = ri.loadLabel(pm).toString();
                } catch (Exception e) {
                    label = pkg;
                }

                Drawable icon;
                try {
                    icon = ri.loadIcon(pm);
                } catch (Exception e) {
                    icon = getResources().getDrawable(android.R.drawable.sym_def_app_icon);
                }

                result.add(new AppInfo(label, pkg, icon));
            }

            // Sort alphabetically by display label (case-insensitive).
            Collections.sort(result, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo a, AppInfo b) {
                    return a.getLabel().compareToIgnoreCase(b.getLabel());
                }
            });

            // Add "Stop Launcher" as the last item (use empty package name to identify it)
            result.add(new AppInfo("Stop Launcher", "", null));

            return result;
        }

        @Override
        protected void onPostExecute(List<AppInfo> apps) {
            mApps.clear();
            mApps.addAll(apps);
            mAdapter.notifyDataSetChanged();

            if (mApps.isEmpty()) {
                Toast.makeText(AppListActivity.this,
                        R.string.no_apps_found, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Launching
    // -------------------------------------------------------------------------

    /**
     * Launches the app at {@code position}, or stops the launcher if it's the Stop button.
     */
    private void launchApp(int position) {
        if (position < 0 || position >= mApps.size()) {
            return;
        }

        AppInfo app = mApps.get(position);

        // Check if this is the "Stop Launcher" item (empty package name)
        if (app.getPackageName().isEmpty()) {
            // Return to the clock/home screen
            finish();
            return;
        }

        Log.d(TAG, "Launching: " + app.getPackageName());

        // Glass audible feedback.
        android.media.AudioManager audio =
                (android.media.AudioManager) getSystemService(AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.TAP);

        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(app.getPackageName());

        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(launchIntent);
            // Give the launched app time to start before finishing
            finish();
        } else {
            Toast.makeText(this, "Cannot launch " + app.getLabel(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Swipe down on Glass or back button — exit the immersion
        super.onBackPressed();
    }
}
