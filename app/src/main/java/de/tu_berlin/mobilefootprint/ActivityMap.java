package de.tu_berlin.mobilefootprint;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.AppCompatDrawableManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.MapQuestTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import de.tu_berlin.mobilefootprint.model.FilterQuery;
import de.tu_berlin.mobilefootprint.model.TimePolygon;
import de.tu_berlin.mobilefootprint.util.DataProviderTask;
import de.tu_berlin.mobilefootprint.util.FilterProvider;
import de.tu_berlin.mobilefootprint.util.TaskCompleted;
import de.tu_berlin.mobilefootprint.util.TypeFilterMenuManager;
import de.tu_berlin.mobilefootprint.view.SublimePickerFragment;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class ActivityMap extends AbstractActivity
        implements MapEventsReceiver, TaskCompleted, Observer {

    public static final long FOUR_WEEKS = 2419200L;
    public static final long TWO_WEEKS = 1209600L;
    public static final long TWO_DAYS = 172800L;
    public static final long TSD = 1000L;
    private static final Logger logger = LoggerManager.getLogger(ActivityMap.class);


//private Handler customHandler = new Handler();

    public static MapView map;
    private IMapController mapController;
    private SeekBar seekBar;
    private long fourWeeksAgo, twoWeeksAgo, twoDaysAgo;
    private List<TimePolygon> polygons = new ArrayList<>();
    private List<TimePolygon> allPolygons = new ArrayList<>();
    private static long selectedTime = 0l;
    private long maxDate, minDate, dateToday;
    private Thread playerThread;
    private boolean dataTaskCompleted = true;
    private TextView textViewSelectedTime, textViewMinDate, textViewMaxDate;
    private boolean paused = true;
    private TypeFilterMenuManager filterManager;
    private FilterQuery filter;
    private boolean hasFilterChanged;
    private ImageButton buttonPrevious;
    private ImageButton buttonNext;
    private ImageButton buttonPlay;
    private MaterialProgressBar progressBar;

    @Override
    protected void onResume() {
        super.onResume();
        initializeMap();
    }

    private void initializeMap() {
        getSupportActionBar().setElevation(0);
        maxDate = System.currentTimeMillis() / TSD;
        dateToday = maxDate;
        fourWeeksAgo = maxDate - FOUR_WEEKS;
        twoWeeksAgo = maxDate - TWO_WEEKS;
        minDate = twoWeeksAgo;


        seekBar = (SeekBar) findViewById(R.id.seekBar1);
        //seekBar.setProgress((int) (((selectedTime - minDate) * 100) / (maxDate - minDate)));
        seekBar.setProgress(50);
        selectedTime = minDate + (((maxDate - minDate) / 100) * (seekBar.getProgress() + 1));

        progressBar = (MaterialProgressBar) findViewById(R.id.progressbar);

        Date df = new java.util.Date(minDate * TSD);
        String vv = new SimpleDateFormat("dd. MMM").format(df);
        textViewMinDate = (TextView) findViewById(R.id.TextViewMinDate);
        textViewMaxDate = (TextView) findViewById(R.id.TextViewMaxDate);
        textViewSelectedTime = (TextView) findViewById(R.id.textViewSelectedTime);
        textViewSelectedTime.setText(convertToDateString(selectedTime));

        textViewMinDate.setText(vv);

        buttonPrevious = (ImageButton) findViewById(R.id.action2_skip_previous);
        buttonNext = (ImageButton) findViewById(R.id.action2_skip_next);
        buttonPlay = (ImageButton) findViewById(R.id.btn_play);


        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipPrevious();
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipNext();
            }
        });

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int icon;

                if (paused) {
                    paused = false;
                    icon = R.drawable.ic_pause_white_24dp;
                    play();
                } else {
                    paused = true;
                    icon = R.drawable.ic_play_arrow_white_24dp;
                    pause();
                }
                buttonPlay.setImageDrawable(
                        AppCompatDrawableManager.get().getDrawable(getApplicationContext(), icon));
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                dataProviderTask(minDate, selectedTime, false);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // progress goes from 0 to 100
                if (fromUser) {
                    selectedTime = minDate + (((maxDate - minDate) / 100) * progress);
                    textViewSelectedTime.setText(convertToDateString(selectedTime));
                }
            }
        });


        org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);
        map = (MapView) findViewById(R.id.osmap);
        //map.setTileSource(TileSourceFactory.MAPNIK);

        //final MapQuestTileSource tileSource = new MapQuestTileSource(context);
        final MapQuestTileSource tileSource = new MapQuestTileSource("mapquest.light-mb", "");
        tileSource.setAccessToken(getString(R.string.access_token));
        map.setTileSource(tileSource);

        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        mapController = map.getController();
        mapController.setZoom(13);

        GeoPoint initialGeoPoint = new GeoPoint(52.51, 13.40);
        mapController.setCenter(initialGeoPoint);

        //filter = new FilterQuery()
        filter = FilterProvider.getInstance().getFilter();
        filter.addObserver(this);
        filterManager = new TypeFilterMenuManager(filter);
        dataProviderTask(minDate, maxDate, true);
        dataProviderTask(minDate, selectedTime, false);
    }

    /**
     * Sets the specified image button to the given state, while modifying or
     * "graying-out" the icon as well
     * <p>
     * source: http://stackoverflow.com/a/14128907
     *
     * @param enabled   The state of the menu item
     * @param item      The menu item to modify
     * @param iconResId The icon ID
     */
    public static void setImageButtonEnabled(Context ctxt, boolean enabled, ImageButton item,
                                             int iconResId) {
        item.setEnabled(enabled);
        Drawable originalIcon = AppCompatDrawableManager.get().getDrawable(ctxt, iconResId);
        Drawable icon = enabled ? originalIcon : convertDrawableToGrayScale(originalIcon);
        item.setImageDrawable(icon);
    }

    /**
     * Mutates and applies a filter that converts the given drawable to a Gray
     * image.
     * <p>
     * source: http://stackoverflow.com/a/14128907
     *
     * @return a mutated version of the given drawable with a color filter
     * applied.
     */
    public static Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Drawable res = drawable.mutate();
        res.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        return res;
    }


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_map;
    }

    @Override
    protected int getOptionsMenuResourceId() {
        return R.menu.activity_map;
    }

    // Validates & returns SublimePicker options
    Pair<Boolean, SublimeOptions> getOptions() {
        SublimeOptions options = new SublimeOptions();
        int displayOptions = 0;
        displayOptions |= SublimeOptions.ACTIVATE_DATE_PICKER;

        options.setPickerToShow(SublimeOptions.Picker.DATE_PICKER);
        options.setDisplayOptions(displayOptions);
        options.setCanPickDateRange(true);

        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTimeInMillis(minDate * TSD);
        c2.setTimeInMillis(maxDate * TSD);

        options.setDateParams(c1, c2);

        options.setDateRange((fourWeeksAgo * TSD), (dateToday * TSD));

        // If 'displayOptions' is zero, the chosen options are not valid
        return new Pair<>(displayOptions != 0 ? Boolean.TRUE : Boolean.FALSE, options);
    }


    private void skipPrevious() {
        if (polygons.size() > 1) {
            TimePolygon p = polygons.get(polygons.size() - 2);

            // hack!
            for (int i = polygons.size() - 1; i > 0; i--) {
                if (!polygons.get(i - 1).getTime().equals(polygons.get(i).getTime())) {
                    p = polygons.get(i - 1);
                    break;
                }
            }

            selectedTime = p.getTime();
            textViewSelectedTime.setText(convertToDateString(selectedTime));
            logger.d("time: %d", p.getTime());

            dataProviderTask(minDate, selectedTime, false);

            seekBar.setProgress((int) (((selectedTime - minDate) * 100) / (maxDate - minDate)));
            logger.d("Seekbar-Progress set to: %d", seekBar.getProgress());
        }
        if (polygons.size() == 1) {
            setImageButtonEnabled(context, false, buttonPrevious, R.drawable.ic_skip_previous_white_24dp);
        }
    }

    private void play() {
        playerThread = new Thread(updateTimerThread);
        playerThread.start();
    }

    private void pause() {
        playerThread.interrupt();
    }

    private void skipNext() {

        if (polygons.size() < allPolygons.size()) {

            TimePolygon p = allPolygons.get(polygons.size());
            polygons.add(p);
            logger.d("size: %d", polygons.size());
            logger.d("time: %d", p.getTime());
            logger.d("mintime: %d", minDate);

            selectedTime = p.getTime();
            textViewSelectedTime.setText(convertToDateString(selectedTime));
            logger.d("selected time: %d", selectedTime);
            dataProviderTask(minDate, selectedTime, false);
            seekBar.setProgress((int) (((selectedTime - minDate) * 100) / (maxDate - minDate)));
            logger.d("Seekbar-Progress set to: %d", seekBar.getProgress());
        }
    }

    SublimePickerFragment.Callback mFragmentCallback = new SublimePickerFragment.Callback() {
        @Override
        public void onCancelled() {
        }

        @Override
        public void onDateTimeRecurrenceSet(SelectedDate selectedDate,
                                            int hourOfDay, int minute,
                                            SublimeRecurrencePicker.RecurrenceOption recurrenceOption,
                                            String recurrenceRule) {

            Calendar firstDate = selectedDate.getFirstDate();
            Calendar secondDate = selectedDate.getSecondDate();

            SimpleDateFormat sdf = new SimpleDateFormat("dd. MMM");
            String formatedDate;

            //this is just a helper for simple year/month/day comparison
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
            Date d = new Date(dateToday * 1000);


            //If user picked today's date as EndDate, set maxDate/textView to today/"Now"
            if (fmt.format(d).equals(fmt.format(secondDate.getTime()))) {
                textViewMaxDate.setText("Now");
                maxDate = dateToday;
            } else {
                formatedDate = sdf.format(secondDate.getTime());
                textViewMaxDate.setText(formatedDate);
                maxDate = (secondDate.getTimeInMillis() / TSD);
            }


            formatedDate = sdf.format(firstDate.getTime());
            textViewMinDate.setText(formatedDate);
            minDate = (firstDate.getTimeInMillis() / TSD);

            dataProviderTask(minDate, maxDate, true);
            seekBar.setProgress(50);
            selectedTime = minDate + (((maxDate - minDate) / 100) * (seekBar.getProgress() + 1));
            textViewSelectedTime.setText(convertToDateString(selectedTime));
            dataProviderTask(minDate, selectedTime, false);
        }
    };


    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    textViewSelectedTime.setText(convertToDateString(selectedTime));
                }
            };

            while (selectedTime < maxDate) {
                SystemClock.sleep(1000);
                while (!dataTaskCompleted) {
                    SystemClock.sleep(200);
                    logger.d("DataTask not yet completet. Continue sleeping...");
                }
                selectedTime = selectedTime + 120;

                dataTaskCompleted = false;
                dataProviderTask(minDate, selectedTime, false);

                runOnUiThread(r);

                seekBar.setProgress((int) (((selectedTime - minDate) * 100) / (maxDate - minDate)));
                //logger.d("Seekbar-Progress set to: %d", seekBar.getProgress());
                if (Thread.interrupted()) {
                    logger.d("INTERRUPTED");
                    return;
                }
            }
        }
    };

    private void displayPolygons(List<TimePolygon> polygons) {
        map.getOverlays().clear();
        if (!polygons.isEmpty()) {
            map.getOverlays().addAll(polygons);
            TimePolygon p = polygons.get(polygons.size() - 1);
            mapController.animateTo(new GeoPoint(p.getPoints().get(0).getLatitude(), p.getPoints().get(0).getLongitude()));
        }
        map.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        long currUnixTime = System.currentTimeMillis() / TSD;

        switch (item.getItemId()) {

            case R.id.action_date_range:
                SublimePickerFragment pickerFrag = new SublimePickerFragment();
                pickerFrag.setCallback(mFragmentCallback);

                // Options
                Pair<Boolean, SublimeOptions> optionsPair = getOptions();

                // Valid options
                Bundle bundle = new Bundle();
                bundle.putParcelable("SUBLIME_OPTIONS", optionsPair.second);
                pickerFrag.setArguments(bundle);

                pickerFrag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                pickerFrag.show(getFragmentManager(), "SUBLIME_PICKER");

                //showDatePickerDialog(true);
                return true;

            case R.id.action_filter_types:

                filterManager.setTypeFilterListener(new TypeFilterMenuManager.TypeFilterListener() {
                    @Override
                    public void TypeFilterMenuDismissed() {
                        if (hasFilterChanged) {
                            dataProviderTask(minDate, selectedTime, false);
                            hasFilterChanged = false;
                        }
                    }
                });

                filterManager.showMenu(this, findViewById(R.id.action_filter_types));

                return true;

            case android.R.id.home:
                Intent returnIntent = new Intent(getApplicationContext(), ActivityHome.class);
                startActivityForResult(returnIntent, 0);
                break;

            default:
                break;
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public void update(Observable observable, Object o) {
        hasFilterChanged = true;
    }


    private String convertToDateString(long unixtime) {
        Date df = new java.util.Date(unixtime * 1000);
        String vv = new SimpleDateFormat("dd. MMM, HH:mm").format(df);
        return vv;
    }

    private void dataProviderTask(long from, long until, boolean fullRange) {
        //progressBar.setVisibility(View.VISIBLE);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        };
        runOnUiThread(r);
        new DataProviderTask(this, context).execute(Pair.create(from, until), fullRange, filter);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        InfoWindow.closeAllInfoWindowsOn(map);
        return true;
    }

    public static Long getSelectedTime() {
        return selectedTime;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return true;
    }

    @Override
    public void onDataTaskCompleted(List<TimePolygon> plgns, boolean fullRange) {
        logger.d("return-size: %d", plgns.size());
        if (fullRange) {
            allPolygons = plgns;
            return;
        }
        polygons = plgns;
        displayPolygons(polygons);
        if (polygons.size() < 2) {
            setImageButtonEnabled(context, false, buttonPrevious, R.drawable.ic_skip_previous_white_24dp);
        } else {
            setImageButtonEnabled(context, true, buttonPrevious, R.drawable.ic_skip_previous_white_24dp);
        }

        if (polygons.size() < allPolygons.size()) {
            setImageButtonEnabled(context, true, buttonNext, R.drawable.ic_skip_next_white_24dp);
            // setImageButtonEnabled(context, true, buttonPlay, R.drawable.ic_play_arrow_white_24dp);
        } else {
            setImageButtonEnabled(context, false, buttonNext, R.drawable.ic_skip_next_white_24dp);
            //setImageButtonEnabled(context, false, buttonPlay, R.drawable.ic_play_arrow_white_24dp);
        }
        dataTaskCompleted = true;
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onHeatMapTaskCompleted(List<Polygon> heatMap) {
    }

}
