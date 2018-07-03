package de.tu_berlin.mobilefootprint.util;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.util.Pair;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.tu_berlin.mobilefootprint.ActivityMap;
import de.tu_berlin.mobilefootprint.R;
import de.tu_berlin.mobilefootprint.model.FilterQuery;
import de.tu_berlin.mobilefootprint.model.TimePolygon;
import de.tu_berlin.snet.cellservice.model.record.AbstractCallOrText;
import de.tu_berlin.snet.cellservice.model.record.AbstractCellChange;
import de.tu_berlin.snet.cellservice.model.record.Call;
import de.tu_berlin.snet.cellservice.model.record.Data;
import de.tu_berlin.snet.cellservice.model.record.Position;
import de.tu_berlin.snet.cellservice.model.record.TextMessage;

import static de.tu_berlin.mobilefootprint.ActivityMap.TSD;


public class DataProviderTask
        extends AsyncTask<Object, Integer, List<TimePolygon>> {
    private Context context;
    private TaskCompleted taskCompletedListener;
    private static final Logger logger = LoggerManager.getLogger(DataProviderTask.class);
    private boolean fullRange;

    public DataProviderTask(TaskCompleted taskCompletedListener, Context context) {
        this.taskCompletedListener = taskCompletedListener;
        this.context = context;
    }

    protected List<TimePolygon> doInBackground(Object... p) {
        List<TimePolygon> polygons = new ArrayList<>();
        DataProvider dataProvider = DataProvider.getInstance(context);

        Pair<Long, Long> pair = (Pair<Long, Long>) p[0];
        fullRange = (boolean) p[1];
        FilterQuery filter = (FilterQuery) p[2];
        long startTime = System.currentTimeMillis();
        if (filter.getIncludeLocationUpdates()) {
            Map<AbstractCellChange, android.util.Pair<Position, Position>> cellChangeLocationMap = dataProvider.getCellChangeLocationMap();
            for (AbstractCellChange cc : cellChangeLocationMap.keySet()) {
                if (checkIfInTimeWindow(pair, cc.getTimestamp())) {
                    polygons.add(createCellChangePolygon(cc, cellChangeLocationMap.get(cc).first, pair, cc.getTimestamp()));
                    polygons.add(createCellChangePolygon(cc, cellChangeLocationMap.get(cc).second, pair, cc.getTimestamp()));
                }
            }
        }
        if (filter.getIncludeData()) {
            Map<Data, Position> dataLocationMap = dataProvider.getDataLocationMap();
            for (Data d : dataLocationMap.keySet()) {
                if (checkIfInTimeWindow((Pair<Long, Long>) p[0], d.getSessionStart())) {
                    polygons.add(createDataPolygon(d, dataLocationMap.get(d), pair, d.getSessionStart()));
                }
            }
        }
        Map<AbstractCallOrText, Position> callTextLocationMap = dataProvider.getCallTextLocationMap();
        for (AbstractCallOrText ct : callTextLocationMap.keySet()) {
            if (filter.getIncludeCalls()) {
                if (ct instanceof Call) {
                    Call c = (Call) ct;
                    if (checkIfInTimeWindow(pair, c.getStartTime())) {
                        polygons.add(createCallPolygon(c, callTextLocationMap.get(ct), pair, c.getStartTime()));
                    }
                }
            }
            if (filter.getIncludeTextMessages()) {
                if (ct instanceof TextMessage) {
                    TextMessage tm = (TextMessage) ct;
                    if (checkIfInTimeWindow(pair, tm.getTime())) {
                        polygons.add(createTextMsgPolygon(tm, callTextLocationMap.get(tm), pair, tm.getTime()));
                    }
                }
            }
        }
        long endTime = System.currentTimeMillis();
        logger.d("getting time: %d", (endTime - startTime));

        // sort polygons by date ascending
        Collections.sort(polygons, new Comparator<TimePolygon>() {
            @Override
            public int compare(TimePolygon p1, TimePolygon p2) {
                return (p1.getTime().compareTo(p2.getTime()));
            }
        });

        if (!polygons.isEmpty()) {
            polygons.get(polygons.size() - 1).setStrokeColor(Color.WHITE);
            polygons.get(polygons.size() - 1).setStrokeWidth(2f);
        }

        return polygons;
    }

    private boolean checkIfInTimeWindow(Pair<Long, Long> timeWindow, Long time) {
        if (timeWindow.first == 0 && timeWindow.second == 0) {
            return true;
        } else {
            return (time >= timeWindow.first && time <= timeWindow.second);
        }
    }

    private TimePolygon createDataPolygon(Data data, Position position, Pair<Long, Long> p, long polygonTime) {
        int color = ContextCompat.getColor(context, R.color.colorDataRecord);
        TimePolygon polygon = setGeneralPolygonSettings(position, color, p, polygonTime);
        String start = new Date(data.getSessionStart() * TSD).toString();
        String end = new Date(data.getSessionEnd() * TSD).toString();
        polygon.setTitle(context.getResources().getString(R.string.data_usage));
        polygon.setSubDescription("Startdate: " + start + System.getProperty("line.separator") + "Enddate: " + end);
        return polygon;
    }

    private TimePolygon createCellChangePolygon(AbstractCellChange cc, Position position, Pair<Long, Long> p, long polygonTime) {
        int color = ContextCompat.getColor(context, R.color.colorLocationUpdate);
        TimePolygon polygon = setGeneralPolygonSettings(position, color, p, polygonTime);
        String time = new Date(cc.getTimestamp() * TSD).toString();
        polygon.setTitle("Location update");
        polygon.setSubDescription("Time: " + time);
        return polygon;
    }

    private TimePolygon createTextMsgPolygon(TextMessage tm, Position position, Pair<Long, Long> p, long polygonTime) {
        int color = ContextCompat.getColor(context, R.color.colorTextMessage);
        TimePolygon polygon = setGeneralPolygonSettings(position, color, p, polygonTime);
        String time = new Date(tm.getTime() * TSD).toString();
        polygon.setTitle("Text message");
        polygon.setSubDescription("Time: " + time);
        return polygon;
    }

    private TimePolygon createCallPolygon(Call call, Position position, Pair<Long, Long> p, long polygonTime) {
        int color = ContextCompat.getColor(context, R.color.colorCall);
        TimePolygon polygon = setGeneralPolygonSettings(position, color, p, polygonTime);
        String start = new Date(call.getStartTime() * TSD).toString();
        String end = new Date(call.getEndTime() * TSD).toString();
        polygon.setTitle("Phone call");
        polygon.setSubDescription("Starttime: " + start + System.getProperty("line.separator") + "Endtime: " + end);
        return polygon;
    }

    private TimePolygon setGeneralPolygonSettings(Position position, int color, Pair<Long, Long> p, long polygonTime) {
        TimePolygon polygon = new TimePolygon(polygonTime);
        GeoPoint gp = new GeoPoint(position.getLat(), position.getLng());
        polygon.setPoints(Polygon.pointsAsCircle(gp, 300));
        int alpha = calculateAlpha(p.first, p.second, polygonTime);
        polygon.setFillColor(ColorUtils.setAlphaComponent(color, alpha));
        polygon.setStrokeColor(ColorUtils.setAlphaComponent(color, alpha));
        polygon.setStrokeWidth(0);
        polygon.setInfoWindow(new BasicInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, ActivityMap.map));
        return polygon;
    }

    private int calculateAlpha(long from, long until, long polygonTime) {
        double alpha = 150 - ((150 / (double) (until - from)) * (until - polygonTime));
        return (int) alpha;
    }

    protected void onPostExecute(List<TimePolygon> polygons) {
        taskCompletedListener.onDataTaskCompleted(polygons, fullRange);
    }
}
