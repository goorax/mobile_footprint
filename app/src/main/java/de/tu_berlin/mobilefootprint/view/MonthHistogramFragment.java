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
import de.tu_berlin.mobilefootprint.model.MonthStatistics;
import de.tu_berlin.mobilefootprint.util.FilterProvider;
import de.tu_berlin.mobilefootprint.util.StatisticProvider;


/**
 * Created by niels on 1/19/17.
 */

public class MonthHistogramFragment extends Fragment implements Observer {

    private CustomizedBarChart chart;
    private FilterQuery filter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_month_histogram, container, false);

        this.filter = FilterProvider.getInstance().getFilter();
        this.filter.addObserver(this);

        new DataProviderTask().execute();

        this.chart = (CustomizedBarChart) view.findViewById(R.id.chart);
        this.chart.getXAxis().setValueFormatter(new DayLabelFormatter());

        /*
        Toolbar toolbarBottom = (Toolbar) view.findViewById (R.id.toolbar_bottom);
        toolbarBottom.setTitle("Filter");
        toolbarBottom.setTitleTextColor(Color.rgb(255, 255, 255));
        toolbarBottom.inflateMenu(R.menu.eventtypes);
        */

        //Add menu click handler
        //toolbarBottom.setOnMenuItemClickListener();

        return view;
    }

    @Override
    public void update(Observable observable, Object o) {

        new DataProviderTask().execute();
    }

    private class DataProviderTask extends AsyncTask<Integer, Integer, MonthStatistics> {

        protected MonthStatistics doInBackground(Integer... n) {

            StatisticProvider dataProvider = StatisticProvider.getInstance(getActivity());

            return dataProvider.getMonthStatistics(filter);
        }

        protected void onPostExecute(final MonthStatistics month) {

            updateChart(month);

            return;
        }
    }

    private void updateChart (MonthStatistics data) {

        this.chart.updateChart(data);
    }
}
