package de.tu_berlin.mobilefootprint.util;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.util.Pair;

import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;

import java.util.Calendar;

import de.tu_berlin.mobilefootprint.model.FilterQuery;
import de.tu_berlin.mobilefootprint.view.SublimePickerFragment;

/**
 * Created by niels on 1/20/17.
 */

public class CalendarDialogManager {

    FilterQuery filter;

    public CalendarDialogManager(FilterQuery filter) {
        this.filter = filter;
    }

    public void showDialog(FragmentManager fragmentManager) {

        SublimePickerFragment pickerFrag = new SublimePickerFragment();
        pickerFrag.setCallback(mFragmentCallback);

        // Options
        SublimeOptions options = new SublimeOptions();
        int displayOptions = SublimeOptions.ACTIVATE_DATE_PICKER;
        long now = System.currentTimeMillis();
        long fourWeeksAgo = now - 2419200000L; // = 60 * 60 * 24 * 7 * 4

        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTimeInMillis(filter.getStartDateTime() * 1000);
        c2.setTimeInMillis(filter.getEndDateTime() * 1000);

        options.setPickerToShow(SublimeOptions.Picker.DATE_PICKER);
        options.setDisplayOptions(displayOptions);
        options.setCanPickDateRange(true);

        options.setDateParams(c1, c2);
        options.setDateRange(fourWeeksAgo, now);

        // If 'displayOptions' is zero, the chosen options are not valid
        Pair<Boolean, SublimeOptions> optionsPair = new Pair<>(
                displayOptions != 0 ? Boolean.TRUE : Boolean.FALSE,
                options
        );

        // Valid options
        Bundle bundle = new Bundle();
        bundle.putParcelable("SUBLIME_OPTIONS", optionsPair.second);
        pickerFrag.setArguments(bundle);

        //pickerFrag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        pickerFrag.show(fragmentManager, "SUBLIME_PICKER");
    }

    SublimePickerFragment.Callback mFragmentCallback = new SublimePickerFragment.Callback() {

        @Override
        public void onCancelled() {
        }

        @Override
        public void onDateTimeRecurrenceSet(
                SelectedDate selectedDate,
                int hourOfDay, int minute,
                SublimeRecurrencePicker.RecurrenceOption recurrenceOption,
                String recurrenceRule
        ) {

            FilterQuery filter = FilterProvider.getInstance().getFilter();
            long min = selectedDate.getStartDate().getTimeInMillis();
            long max = min;

            if (selectedDate.getType() == SelectedDate.Type.RANGE) {

                max = selectedDate.getEndDate().getTimeInMillis();
            }

            max = max + 1000 * 60 * 60 * 24;

            filter.setStartDateTime((int) (min / 1000L));
            filter.setEndDateTime((int) (max / 1000L));
            filter.notifyObservers();
        }
    };
}
