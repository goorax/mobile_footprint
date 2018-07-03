package de.tu_berlin.mobilefootprint;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import java.util.Observable;
import java.util.Observer;

import de.tu_berlin.mobilefootprint.util.CalendarDialogManager;
import de.tu_berlin.mobilefootprint.util.FilterProvider;
import de.tu_berlin.mobilefootprint.util.TypeFilterMenuManager;
import de.tu_berlin.mobilefootprint.view.ContactsPieChartFragment;
import de.tu_berlin.mobilefootprint.view.MonthHistogramFragment;
import de.tu_berlin.mobilefootprint.view.MonthPunchCardChartFragment;
import de.tu_berlin.mobilefootprint.view.WeekdayHistogramFragment;

public class ActivityStatistics extends AbstractActivity implements Observer {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link FragmentPagerAdapter} derivative, which will keep every loaded
     * fragment in memory. If this becomes too memory intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private ActivityStatistics.SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private TypeFilterMenuManager filterManager;
    private CalendarDialogManager calendarManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new ActivityStatistics.SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getBoolean("switch_to_contacts")) {
            mViewPager.setCurrentItem(3);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        filterManager = new TypeFilterMenuManager(FilterProvider.getInstance().getFilter());
        calendarManager = new CalendarDialogManager(FilterProvider.getInstance().getFilter());
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_statistics;
    }

    @Override
    protected int getOptionsMenuResourceId() {
        return R.menu.activity_statistics;
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

    @Override
    public void update(Observable observable, Object filter) {

        //
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the
     * sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position) {
                case 0:
                    return new WeekdayHistogramFragment();
                case 1:
                    return new MonthPunchCardChartFragment();
                case 2:
                    return new MonthHistogramFragment();
                case 3:
                    return new ContactsPieChartFragment();
            }

            return null;
        }

        @Override
        public int getCount() {
            return 4; // Show N total pages.
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Day of week";
                case 1:
                    return "Month (punchcard)";
                case 2:
                    return "Month (histogram)";
                case 3:
                    return "Contacts";
            }
            return null;
        }
    }
}
