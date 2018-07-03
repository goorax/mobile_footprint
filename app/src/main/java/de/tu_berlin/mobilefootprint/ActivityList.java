package de.tu_berlin.mobilefootprint;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import de.tu_berlin.mobilefootprint.model.FilterQuery;
import de.tu_berlin.mobilefootprint.util.CalendarDialogManager;
import de.tu_berlin.mobilefootprint.util.FilterProvider;
import de.tu_berlin.mobilefootprint.util.TypeFilterMenuManager;
import de.tu_berlin.mobilefootprint.view.SublimePickerFragment;
import de.tu_berlin.snet.cellservice.model.database.GeoDatabaseHelper;

public class ActivityList extends AbstractActivity implements Observer {

    private TypeFilterMenuManager filterManager;
    private CalendarDialogManager calendarManager;
    private EventRecordAdapter listAdapter;
    private FilterQuery filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ListView listview = (ListView) findViewById(R.id.listview);
        listAdapter = new EventRecordAdapter(this);
        listview.setAdapter(listAdapter);

        filter = FilterProvider.getInstance().getFilter();
        filter.addObserver(this);

        filterManager = new TypeFilterMenuManager(filter);
        calendarManager = new CalendarDialogManager(filter);

        new DataProviderTask().execute();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_list;
    }

    @Override
    protected int getOptionsMenuResourceId() {
        return R.menu.activity_list;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_date_range:

                calendarManager.showDialog(getFragmentManager());

                return true;

            case R.id.action_filter_types:

                filterManager.showMenu(this, findViewById(R.id.action_filter_types));

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateList(List<String[]> entries) {

        listAdapter.clear();
        listAdapter.addAll(entries);
    }

    @Override
    public void update(Observable observable, Object o) {

        new DataProviderTask().execute();
    }

    private class DataProviderTask extends AsyncTask<Integer, Integer, String[][]> {

        protected String[][] doInBackground(Integer... n) {

            if (!(
                    filter.getIncludeCalls() ||
                            filter.getIncludeTextMessages() ||
                            filter.getIncludeLocationUpdates() ||
                            filter.getIncludeLocationUpdates()
            )) {

                return new String[][] {};
            }

            final String queryCalls = "SELECT 'Call' AS type, starttime, endtime FROM Calls";
            final String queryHandovers = "SELECT 'Handover' AS type, time AS starttime, time AS endtime FROM Handovers";
            final String queryTextMessages = "SELECT 'TextMessage' AS type, time AS starttime, time AS endtime FROM TextMessages";
            final String queryData = "SELECT 'Data' AS type, starttime, endtime FROM DataRecords";
            final String queryLocationUpdates = "SELECT 'LocationUpdate' AS type, time AS starttime, time AS endtime FROM LocationUpdates";

            List<String> subqueries = new ArrayList<>();

            if (filter.getIncludeCalls()) {
                subqueries.add(queryCalls);
                subqueries.add(queryHandovers);
            }
            if (filter.getIncludeTextMessages()) {
                subqueries.add(queryTextMessages);
            }
            if (filter.getIncludeData()) {
                subqueries.add(queryData);
            }
            if (filter.getIncludeLocationUpdates()) {
                subqueries.add(queryLocationUpdates);
            }

            final String query =
                    "SELECT type, starttime, endtime FROM (" +
                            TextUtils.join(" UNION ", subqueries) +
                            ")" +
                            " WHERE starttime > " + filter.getStartDateTime() +
                            " AND starttime < " + filter.getEndDateTime() +
                            " ORDER BY starttime DESC" +
                            ";";

            final String[][] result = GeoDatabaseHelper.getInstance(getApplicationContext())
                    .getSQLTableResult(query);

            return result;
        }

        protected void onPostExecute(final String[][] rows) {

            List<String[]> entries = new ArrayList<>(Arrays.asList(rows));

            updateList(entries);

            return;
        }
    }

    public class EventRecordAdapter extends ArrayAdapter<String[]> {

        public EventRecordAdapter(Context context) {

            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // Get the data item for this position
            String[] event = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {

                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_event, parent, false);
            }

            // Lookup view for data population
            TextView tvFirst = (TextView) convertView.findViewById(R.id.list_event_first);
            TextView tvSecond = (TextView) convertView.findViewById(R.id.list_event_second);

            int textColor = ContextCompat.getColor(context, R.color.primary_dark_material_light);
            tvSecond.setTextColor(textColor);

            switch (event[0]) {
                case "LocationUpdate":
                    textColor = ContextCompat.getColor(context, R.color.colorLocationUpdate);
                    break;
                case "Data":
                    textColor = ContextCompat.getColor(context, R.color.colorDataRecord);
                    break;
                case "Handover":
                    textColor = ContextCompat.getColor(context, R.color.colorHandover);
                    break;
                case "Call":
                    textColor = ContextCompat.getColor(context, R.color.colorCall);
                    break;
                case "TextMessage":
                    textColor = ContextCompat.getColor(context, R.color.colorTextMessage);
                    break;
            }
            tvFirst.setTextColor(textColor);

            String timeString;
            final long start = Long.parseLong(event[1]) * 1000L;
            final long end = Long.parseLong(event[2]) * 1000L;

            DateFormat format = DateFormat.getDateTimeInstance();

            timeString = format.format(new Date(start));

            if (start != end) {

                timeString = timeString + " - " + format.format(new Date(end));
            }

            // Populate the data into the template view using the data object
            tvFirst.setText(event[0]);
            tvSecond.setText(timeString);

            // Return the completed view to render on screen
            return convertView;
        }
    }
}
