package de.tu_berlin.mobilefootprint.view;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Observable;
import java.util.Observer;

import de.tu_berlin.mobilefootprint.R;
import de.tu_berlin.mobilefootprint.model.FilterQuery;
import de.tu_berlin.mobilefootprint.model.WeekStatistics;
import de.tu_berlin.mobilefootprint.util.FilterProvider;
import de.tu_berlin.mobilefootprint.util.StatisticProvider;


/**
 * Created by niels on 1/19/17.
 */

public class WeekdayHistogramFragment extends Fragment implements Observer {

    private CustomizedBarChart chart;
    private FilterQuery filter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_weekdays_histogram, container, false);

        this.filter = FilterProvider.getInstance().getFilter();
        this.filter.addObserver(this);

        new DataProviderTask().execute();

        this.chart = (CustomizedBarChart) view.findViewById(R.id.chart);

        return view;
    }

    @Override
    public void update(Observable observable, Object o) {

        new DataProviderTask().execute();
    }

    private class DataProviderTask extends AsyncTask<Integer, Integer, WeekStatistics> {

        protected WeekStatistics doInBackground(Integer... n) {

            StatisticProvider dataProvider = StatisticProvider.getInstance(getActivity());

            return dataProvider.getWeekStatistics(filter);
        }

        protected void onPostExecute(final WeekStatistics week) {

            updateChart(week);

            return;
        }
    }

    private void updateChart (WeekStatistics data) {

        this.chart.updateChart(data);
    }
}
