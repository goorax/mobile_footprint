package de.tu_berlin.mobilefootprint.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.BubbleChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.tu_berlin.mobilefootprint.R;
import de.tu_berlin.mobilefootprint.model.DayStatistics;
import de.tu_berlin.mobilefootprint.model.FilterQuery;
import de.tu_berlin.mobilefootprint.model.MonthStatistics;


/**
 * Created by niels on 1/1/17.
 */

public class PunchCardChart extends BubbleChart {

    public PunchCardChart(Context context) {
        super(context);
    }

    public PunchCardChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PunchCardChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();

        Description desc = new Description();
        desc.setText("");
        setDescription(desc);

        // Disable unwanted interaction
        setScaleEnabled(false);
        setPinchZoom(false);
        setDoubleTapToZoomEnabled(false);

        // Remove unwanted lines
        setDrawGridBackground(false);

        // Adjust viewport and paddings
        setExtraOffsets(0.5f, 0.5f, 0.5f, 0.5f);

        // Adjust X axis
        setVisibleXRangeMinimum(6);
        setVisibleXRangeMaximum(6);

        XAxis xaxis = getXAxis();
        xaxis.setPosition(XAxis.XAxisPosition.TOP);
        xaxis.setAxisMinimum(-0.5f);
        xaxis.setAxisMaximum(6.5f);
        xaxis.setDrawAxisLine(false);
        xaxis.setDrawGridLines(false);

        getXAxis().setValueFormatter(new WeekdayLabelFormatter());

        // Adjust Y axis
        setVisibleYRangeMinimum(5, YAxis.AxisDependency.LEFT);
        setVisibleYRangeMaximum(5, YAxis.AxisDependency.LEFT);
        getAxisRight().setEnabled(false); // no right axis

        // data has AxisDependency.LEFT
        YAxis yaxis = getAxisLeft();
        yaxis.setDrawLabels(true); // draw axis labels
        yaxis.setDrawAxisLine(false); // no axis line
        yaxis.setDrawGridLines(false); // no grid lines
        yaxis.setDrawZeroLine(false); // no zero line

        yaxis.setValueFormatter(new WeekLabelFormatter());
    }

    public void applyData(MonthStatistics data, FilterQuery filter) {

        final DayStatistics[] days = data.getDays();

        int day = data.getOffsetDay();
        int week = data.getOffsetWeek();

        final List<BubbleEntry> callEntries = new ArrayList<BubbleEntry>();
        final List<BubbleEntry> textMessageEntries = new ArrayList<BubbleEntry>();
        final List<BubbleEntry> dataEntries = new ArrayList<BubbleEntry>();
        final List<BubbleEntry> locationUpdateEntries = new ArrayList<BubbleEntry>();
        final List<BubbleEntry> handoverEntries = new ArrayList<BubbleEntry>();

        for (DayStatistics stat : days) {

            final int x = day % 7;
            final int y = 0 - week;

            int sum = 0;

            if (filter.getIncludeLocationUpdates()) {

                sum += stat.getLocationUpdateCount();
                locationUpdateEntries.add(new BubbleEntry(x, y, sum));
            }

            if (filter.getIncludeData()) {

                sum += stat.getDataCount();
                dataEntries.add(new BubbleEntry(x, y, sum));
            }

            if (filter.getIncludeTextMessages()) {

                sum += stat.getTextMessageCount();
                textMessageEntries.add(new BubbleEntry(x, y, sum));
            }

            if (filter.getIncludeCalls()) {

                sum += stat.getHandOverCount();
                handoverEntries.add(new BubbleEntry(x, y, sum));

                sum += stat.getCallCount();
                callEntries.add(new BubbleEntry(x, y, sum));
            }

            /*
            callEntries.add(new BubbleEntry(x, y, stat.getCallCount()));
            textMessageEntries.add(new BubbleEntry(x, y, stat.getTextMessageCount()));
            dataEntries.add(new BubbleEntry(x, y, stat.getDataCount()));
            locationUpdateEntries.add(new BubbleEntry(x, y, stat.getLocationUpdateCount()));
            handoverEntries.add(new BubbleEntry(x, y, stat.getHandOverCount()));
            */

            day = day + 1;

            if (day % 7 == 0) {
                week = week + 1;
            }
        }

        YAxis yaxis = getAxisLeft();
        yaxis.setAxisMinimum(0 - 5.5f - data.getOffsetWeek());
        yaxis.setAxisMaximum(0 - 0.5f - data.getOffsetWeek());

        List<IBubbleDataSet> dataSets = new ArrayList<>();
        Resources res = getResources();

        if (filter.getIncludeCalls()) {

            Collections.sort(callEntries, new EntryXComparator());
            BubbleDataSet calls = new BubbleDataSet(callEntries, "Calls");
            calls.setColors(res.getColor(R.color.colorCall));
            calls.setDrawValues(false);
            dataSets.add(calls);

            Collections.sort(handoverEntries, new EntryXComparator());
            BubbleDataSet handovers = new BubbleDataSet(handoverEntries, "Handovers");
            handovers.setColors(res.getColor(R.color.colorHandover));
            handovers.setDrawValues(false);
            dataSets.add(handovers);
        }

        if (filter.getIncludeTextMessages()) {

            Collections.sort(textMessageEntries, new EntryXComparator());
            BubbleDataSet textMessages = new BubbleDataSet(textMessageEntries, "SMS");
            textMessages.setColors(res.getColor(R.color.colorTextMessage));
            textMessages.setDrawValues(false);
            dataSets.add(textMessages);
        }

        if (filter.getIncludeData()) {

            Collections.sort(dataEntries, new EntryXComparator());
            BubbleDataSet dataRecords = new BubbleDataSet(dataEntries, "Data");
            dataRecords.setColors(res.getColor(R.color.colorDataRecord));
            dataRecords.setDrawValues(false);
            dataSets.add(dataRecords);
        }

        if (filter.getIncludeLocationUpdates()) {

            Collections.sort(locationUpdateEntries, new EntryXComparator());
            BubbleDataSet locationUpdates = new BubbleDataSet(locationUpdateEntries, "Location Updates");
            locationUpdates.setColors(res.getColor(R.color.colorLocationUpdate));
            locationUpdates.setDrawValues(false);
            dataSets.add(locationUpdates);
        }

        setTouchEnabled(false);
        setData(new BubbleData(dataSets));
        invalidate();
    }
}
