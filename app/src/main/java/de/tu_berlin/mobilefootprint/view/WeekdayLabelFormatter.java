package de.tu_berlin.mobilefootprint.view;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormatSymbols;

/**
 * Created by niels on 1/1/17.
 */

public class WeekdayLabelFormatter implements IAxisValueFormatter {

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        if (value < 0 || value >= 7) return "ERR";

        //DateFormatSymbols symbols = new DateFormatSymbols(new Locale("de"));
        DateFormatSymbols symbols = new DateFormatSymbols();
        String[] dayNames = symbols.getShortWeekdays();

        return dayNames[((int) value + 1) % 7 + 1];
    }
}
