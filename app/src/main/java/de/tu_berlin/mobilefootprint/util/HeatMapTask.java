package de.tu_berlin.mobilefootprint.util;

import android.content.Context;
import android.os.AsyncTask;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.overlay.Polygon;

import java.util.List;

public class HeatMapTask
        extends AsyncTask<BoundingBox, Integer, List<Polygon>> {
    private Context context;
    private TaskCompleted taskCompletedListener;

    public HeatMapTask(TaskCompleted taskCompletedListener, Context context) {
        this.taskCompletedListener = taskCompletedListener;
        this.context = context;
    }

    protected List<Polygon> doInBackground(BoundingBox... p) {
        HeatMapProvider tp = HeatMapProvider.getInstance(context);
        List<Polygon> heatMap = tp.getHeatMap();
        return heatMap;
    }

    protected void onPostExecute(List<Polygon> heatMap) {
        taskCompletedListener.onHeatMapTaskCompleted(heatMap);
    }
}
