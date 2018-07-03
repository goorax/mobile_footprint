package de.tu_berlin.mobilefootprint.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import de.tu_berlin.mobilefootprint.ActivityHeatMap;
import de.tu_berlin.mobilefootprint.ActivityHome;

public abstract class DataLoaderTask extends AsyncTask<Void, Integer, Void> {
    public static final String KEY_SHARED_PREFS = "snet.de.tu_berlin.de.mobilefootprint.shared_prefs";
    public static final int RECORD_LOAD = 1;
    public static final int HEATMAP_LOAD = 2;
    public static final String KEY_TS_DATA = "timestamp_loading";
    public static final String KEY_TS_HEATMAP = "timestamp_heatmap";
    public Progress progress;

    protected Context ctx;
    protected int mode = -1;
    protected SharedPreferences sharedPrefs;

    public DataLoaderTask(Context ctx, int mode) {
        this.ctx = ctx;
        this.mode = mode;
        this.sharedPrefs = ctx.getSharedPreferences(KEY_SHARED_PREFS, ctx.MODE_PRIVATE);
    }

    protected abstract void onPreExecute();

    protected abstract void onPostExecute(Void aVoid);

    protected abstract void onProgressUpdate(Integer... progress);


    protected Void doInBackground(Void... aVoid) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        switch (mode) {
            case RECORD_LOAD:
            case 0:
                DataProvider.getInstance(ctx).loadData();
                editor.putLong(KEY_TS_DATA, System.currentTimeMillis());
                break;
            case HEATMAP_LOAD:
                DataProvider.getInstance(ctx).loadData();
                progress.publish(5);
                int tessellationSize = HeatMapProvider.getInstance(ctx).processTessellation(progress);
                if (tessellationSize > 0) {
                    HeatMapProvider.getInstance(ctx).processHeatMap(progress, false);
                }
                editor.putLong(KEY_TS_HEATMAP, System.currentTimeMillis());
                break;
            default:
                break;
        }
        editor.commit();
        return null;
    }

    public class Progress {
        private DataLoaderTask task;

        public Progress(DataLoaderTask task) {
            this.task = task;
        }

        public void publish(int val) {
            task.publishProgress(val);
        }
    }

}