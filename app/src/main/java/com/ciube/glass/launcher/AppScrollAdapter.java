package com.ciube.glass.launcher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.widget.CardScrollAdapter;

import java.util.List;

/**
 * {@link CardScrollAdapter} that displays one installed app per card.
 *
 * <p>Each card is inflated from {@code res/layout/card_app_item.xml} and
 * populated with the app's icon, label, and package name.</p>
 *
 * <p>The GDK {@code CardScrollAdapter} extends {@code BaseAdapter}; the only
 * mandatory extra method is {@link #getItemId(int)}, which must return a
 * stable unique long for each position.</p>
 */
public class AppScrollAdapter extends CardScrollAdapter {

    private final Context mContext;
    private final List<AppInfo> mApps;
    private final LayoutInflater mInflater;

    public AppScrollAdapter(Context context, List<AppInfo> apps) {
        mContext = context;
        mApps = apps;
        mInflater = LayoutInflater.from(context);
    }

    // -------------------------------------------------------------------------
    // Adapter contract
    // -------------------------------------------------------------------------

    @Override
    public int getCount() {
        return mApps.size();
    }

    @Override
    public Object getItem(int position) {
        return mApps.get(position);
    }

    @Override
    public long getItemId(int position) {
        // Use position as id; package names are unique but long hashing is fine.
        return position;
    }

    /**
     * Required by {@link CardScrollAdapter}: returns the position that
     * corresponds to the given object.  Used by the framework for selection.
     */
    @Override
    public int getPosition(Object item) {
        if (item instanceof AppInfo) {
            return mApps.indexOf(item);
        }
        return AdapterView.INVALID_POSITION;
    }

    // -------------------------------------------------------------------------
    // View recycling
    // -------------------------------------------------------------------------

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.card_app_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AppInfo app = mApps.get(position);
        holder.icon.setImageDrawable(app.getIcon());
        holder.name.setText(app.getLabel());
        holder.pkg.setText(app.getPackageName());

        return convertView;
    }

    // -------------------------------------------------------------------------
    // ViewHolder pattern
    // -------------------------------------------------------------------------

    private static final class ViewHolder {
        final ImageView icon;
        final TextView name;
        final TextView pkg;

        ViewHolder(View root) {
            icon = (ImageView) root.findViewById(R.id.iv_icon);
            name = (TextView) root.findViewById(R.id.tv_app_name);
            pkg  = (TextView) root.findViewById(R.id.tv_package);
        }
    }
}
