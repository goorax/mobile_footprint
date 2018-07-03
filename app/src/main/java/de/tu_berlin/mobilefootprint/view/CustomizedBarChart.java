package de.tu_berlin.mobilefootprint.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

import de.tu_berlin.mobilefootprint.R;
import de.tu_berlin.mobilefootprint.model.MonthStatistics;
import de.tu_berlin.mobilefootprint.model.WeekStatistics;


/**
 * Created by niels on 1/1/17.
 */

public class CustomizedBarChart extends BarChart {

    public CustomizedBarChart(Context context) {
        super(context);
    }

    public CustomizedBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomizedBarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();

        Description desc = new Description();
        desc.setText("");
        setDescription(desc);

        // Remove unwanted lines
        setDrawGridBackground(false);

        // Adjust X axis
        setVisibleXRangeMinimum(6);
        setVisibleXRangeMaximum(6);

        XAxis xaxis = getXAxis();
        xaxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xaxis.setAxisMinimum(-0.5f);
        //xaxis.setAxisMaximum(6.5f);
        xaxis.setDrawAxisLine(false);
        xaxis.setDrawGridLines(false);

        xaxis.setValueFormatter(new WeekdayLabelFormatter());

        // Adjust Y axis
        //setVisibleYRangeMinimum(5, YAxis.AxisDependency.LEFT);
        //setVisibleYRangeMaximum(5, YAxis.AxisDependency.LEFT);
        getAxisRight().setEnabled(false); // no right axis

        // data has AxisDependency.LEFT
        YAxis yaxis = getAxisLeft();
        yaxis.setDrawLabels(true); // draw axis labels
        yaxis.setDrawAxisLine(false); // no axis line
        yaxis.setDrawGridLines(true); // draw grid lines
        yaxis.setDrawZeroLine(true); // draw zero line

        /*
        // Tried these, but to no avail.
        setDrawValueAboveBar(true);
        setDrawMarkers(false);
        */
    }

    public void updateChart(WeekStatistics weekData) {

        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < weekData.getDays().length; i++) {

            entries.add(new BarEntry(i, new float[] {
                    weekData.getDays()[i].getCallCount(),
                    weekData.getDays()[i].getHandOverCount(),
                    weekData.getDays()[i].getTextMessageCount(),
                    weekData.getDays()[i].getDataCount(),
                    weekData.getDays()[i].getLocationUpdateCount()
            }));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(getColors());
        dataSet.setStackLabels(getLabels());
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);

        setTouchEnabled(false);

        setData(barData);
        invalidate();
    }

    public void updateChart(MonthStatistics monthData) {

        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < monthData.getDays().length; i++) {

            entries.add(new BarEntry(i, new float[] {
                    monthData.getDays()[i].getCallCount(),
                    monthData.getDays()[i].getHandOverCount(),
                    monthData.getDays()[i].getTextMessageCount(),
                    monthData.getDays()[i].getDataCount(),
                    monthData.getDays()[i].getLocationUpdateCount()
            }));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(getColors());
        dataSet.setStackLabels(getLabels());
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);

        setTouchEnabled(false);

        setData(barData);
        invalidate();
    }

    public int[] getColors() {

        Resources res = getResources();

        return new int[]{
                res.getColor(R.color.colorCall),
                res.getColor(R.color.colorHandover),
                res.getColor(R.color.colorTextMessage),
                res.getColor(R.color.colorDataRecord),
                res.getColor(R.color.colorLocationUpdate)
        };
    }

    public String[] getLabels() {

        return new String[]{
                "Calls",
                "Handovers",
                "SMS",
                "Data",
                "Location updates"
        };
    }
}
