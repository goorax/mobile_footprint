package de.tu_berlin.mobilefootprint.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import de.tu_berlin.mobilefootprint.R;
import de.tu_berlin.mobilefootprint.model.FilterQuery;

/**
 * Created by niels on 1/20/17.
 */

public class TypeFilterMenuManager implements PopupMenu.OnMenuItemClickListener {

    FilterQuery filter;
    private Context context1;
    private static final Logger logger = LoggerManager.getLogger(TypeFilterMenuManager.class);

    TypeFilterListener listener;

    public interface TypeFilterListener {
        public void TypeFilterMenuDismissed();
    }

    public TypeFilterMenuManager(FilterQuery filter) {
        this.filter = filter;
    }

    public TypeFilterMenuManager(FilterQuery filter, TypeFilterListener l) {
        this.filter = filter;
        this.listener = l;
    }

    public void setTypeFilterListener(TypeFilterListener l) {
        this.listener = l;
    }

    public void showMenu(Context context, View anchor) {
        context1 = context;
        PopupMenu popupMenu = new PopupMenu(context, anchor);

        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.eventtypes);

        Menu menu = popupMenu.getMenu();

        MenuItem m = menu.findItem(R.id.action_toggle_calls);
        m.setChecked(filter.getIncludeCalls());
        SpannableString s = new SpannableString(m.getTitle());
        int color = ContextCompat.getColor(context, R.color.colorCall);
        s.setSpan(new ForegroundColorSpan(color), 0, s.length(), 0);
        m.setTitle(s);

        m = menu.findItem(R.id.action_toggle_textmessages);
        m.setChecked(filter.getIncludeTextMessages());
        s = new SpannableString(m.getTitle());
        color = ContextCompat.getColor(context, R.color.colorTextMessage);
        s.setSpan(new ForegroundColorSpan(color), 0, s.length(), 0);
        m.setTitle(s);

        m = menu.findItem(R.id.action_toggle_data);
        m.setChecked(filter.getIncludeData());
        s = new SpannableString(m.getTitle());
        color = ContextCompat.getColor(context, R.color.colorDataRecord);
        s.setSpan(new ForegroundColorSpan(color), 0, s.length(), 0);
        m.setTitle(s);

        m = menu.findItem(R.id.action_toggle_locationupdates);
        m.setChecked(filter.getIncludeLocationUpdates());
        s = new SpannableString(m.getTitle());
        color = ContextCompat.getColor(context, R.color.colorLocationUpdate);
        s.setSpan(new ForegroundColorSpan(color), 0, s.length(), 0);
        m.setTitle(s);


        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                if (listener != null) {
                    listener.TypeFilterMenuDismissed();
                }
            }
        });

        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        item.setChecked(!item.isChecked());
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        item.setActionView(new View(context1));

        switch (item.getItemId()) {

            case R.id.action_toggle_calls:

                filter.setIncludeCalls(item.isChecked());
                filter.notifyObservers();
                return false;

            case R.id.action_toggle_textmessages:

                filter.setIncludeTextMessages(item.isChecked());
                filter.notifyObservers();
                return false;

            case R.id.action_toggle_data:

                filter.setIncludeData(item.isChecked());
                filter.notifyObservers();
                return false;

            case R.id.action_toggle_locationupdates:

                filter.setIncludeLocationUpdates(item.isChecked());
                filter.notifyObservers();
                return false;

            default:

                return false;
        }
    }
}
