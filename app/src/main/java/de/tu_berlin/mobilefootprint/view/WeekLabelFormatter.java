package de.tu_berlin.mobilefootprint.view;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

/**
 * Created by niels on 1/1/17.
 */

public class WeekLabelFormatter implements IAxisValueFormatter {

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        return "W" + Integer.toString(Math.round(value));
    }
}
