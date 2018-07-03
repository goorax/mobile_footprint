package de.tu_berlin.mobilefootprint.view;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import java.util.Calendar;

import de.tu_berlin.mobilefootprint.R;

/**
 * Created by tob
 */

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private static final Logger logger = LoggerManager.getLogger(DatePickerFragment.class);

    TheListener listener;
    boolean from;

    public interface TheListener {
        public void returnDate(int year, int month, int day, boolean from);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        //DatePickerDialog d = new DatePickerDialog(getActivity(), this, year, month, day);
        DatePickerDialog d = new DatePickerDialog(getActivity(), R.style.AppTheme_Dialog, this, year, month, day);

        final Bundle bdl = getArguments();
        from = bdl.getBoolean("from");

        logger.d("maxdate: %d", bdl.getLong("maxDate") / 1000L);
        logger.d("mindate: %d", bdl.getLong("minDate") / 1000L);

        d.getDatePicker().setMinDate(bdl.getLong("minDate"));
        d.getDatePicker().setMaxDate(bdl.getLong("maxDate"));

        listener = (TheListener) getActivity();
        return d;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {

        // Do something with the date chosen by the user
        //Button buttonFrom= (Button) getActivity().findViewById(R.id.buttonFrom);

        // Date df = new java.util.Date(fourWeeksAgo * TSD);
        // String vv = new SimpleDateFormat("dd.MM").format(df);
        // buttonFrom.setText("Year: "+view.getYear()+" Month: "+view.getMonth()+" Day: "+view.getDayOfMonth());
        if (listener != null) {
            listener.returnDate(year, month, day, from);
        }

    }
}