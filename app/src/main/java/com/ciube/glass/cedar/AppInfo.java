package com.ciube.glass.cedar;

import android.graphics.drawable.Drawable;

/**
 * Holds the display name, package name, and icon for a launchable app.
 */
public class AppInfo {

    private final String mLabel;
    private final String mPackageName;
    private final Drawable mIcon;

    public AppInfo(String label, String packageName, Drawable icon) {
        mLabel = label;
        mPackageName = packageName;
        mIcon = icon;
    }

    public String getLabel() {
        return mLabel;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public Drawable getIcon() {
        return mIcon;
    }
}
