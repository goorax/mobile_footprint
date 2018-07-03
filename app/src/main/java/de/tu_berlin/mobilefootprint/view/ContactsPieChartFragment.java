package de.tu_berlin.mobilefootprint.view;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import de.tu_berlin.mobilefootprint.R;
import de.tu_berlin.mobilefootprint.util.StatisticProvider;


public class ContactsPieChartFragment extends Fragment {

    private PieChart chart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contacts_piechart, container, false);

        new DataProviderTask().execute();

        this.chart = (PieChart) view.findViewById(R.id.chart);

        return view;
    }

    private void updateChart (List<PieEntry> entries) {

        PieDataSet set = new PieDataSet(entries, "Communications");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        set.setDrawValues(false);

        PieData data = new PieData(set);

        Description desc = new Description();
        desc.setText("");
        this.chart.setDescription(desc);
        this.chart.setTouchEnabled(false);

        this.chart.setData(data);
        this.chart.invalidate();
    }

    private class DataProviderTask extends AsyncTask<Integer, Integer, String[][]> {

        protected String[][] doInBackground(Integer... n) {

            StatisticProvider dataProvider = StatisticProvider.getInstance(getActivity());

            return dataProvider.getContactStats();
        }

        protected void onPostExecute(final String[][] rows) {

            List<PieEntry> entries = new ArrayList<>();

            for (String[] fields : rows) {

                entries.add(new PieEntry(Integer.parseInt(fields[0]), fields[1]));
            }

            updateChart(entries);

            return;
        }
    }

    /* *
     * Work in progress: Find contacts by phone number
     * @see https://developer.android.com/training/contacts-provider/retrieve-names.html#TypeMatch
     * @see https://developer.android.com/guide/components/intents-common.html#Contacts
     * @see https://developer.android.com/guide/topics/providers/content-provider-basics.html#RequestPermissions
     */
    private void todo() {

        final String[] projection = {
                /*
                 * The detail data row ID. To make a ListView work,
                 * this column is required.
                 */
                ContactsContract.Data._ID,
                // The primary display name
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                        ContactsContract.Data.DISPLAY_NAME_PRIMARY :
                        ContactsContract.Data.DISPLAY_NAME,
                // The contact's _ID, to construct a content URI
                ContactsContract.Data.CONTACT_ID,
                // The contact's LOOKUP_KEY, to construct a content URI
                ContactsContract.Data.LOOKUP_KEY
        };

        final String selection =
            /*
             * Searches for an email address
             * that matches the search string
             */
                ContactsContract.CommonDataKinds.Email.ADDRESS + " LIKE ? " + "AND " +
            /*
             * Searches for a MIME type that matches
             * the value of the constant
             * Email.CONTENT_ITEM_TYPE. Note the
             * single quotes surrounding Email.CONTENT_ITEM_TYPE.
             */
                ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE + "'";

        //ContentResolver cr = getContentResolver();
        ContentResolver cr = null;
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                projection, selection, null, null);
        // projection, selection clause, selection args, sort order

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        //Toast.makeText(NativeContentProvider.this, "Name: " + name
                        //        + ", Phone No: " + phoneNo, Toast.LENGTH_SHORT).show();
                    }
                    pCur.close();
                }
            }
        }
    }
}
