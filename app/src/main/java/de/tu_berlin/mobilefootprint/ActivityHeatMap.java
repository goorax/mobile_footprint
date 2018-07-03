package de.tu_berlin.mobilefootprint;

import android.util.Pair;
import android.view.Menu;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.MapQuestTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.List;

import de.tu_berlin.mobilefootprint.model.TimePolygon;
import de.tu_berlin.mobilefootprint.util.HeatMapProvider;
import de.tu_berlin.mobilefootprint.util.HeatMapTask;
import de.tu_berlin.mobilefootprint.util.TaskCompleted;

/**
 * This activity shows a heat map over the visited locations.
 *
 * @author johannes
 */

public class ActivityHeatMap extends AbstractActivity
        implements MapEventsReceiver, TaskCompleted {

    private static final Logger logger = LoggerManager.getLogger(ActivityHeatMap.class);

    public static MapView map;
    IMapController mapController;
    HeatMapProvider hmp;
    private int id;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_hotspots;
    }

    @Override
    protected int getOptionsMenuResourceId() {
        return R.menu.activity_hotspots;
    }

    @Override
    protected void onResume() {
        super.onResume();
        hmp = HeatMapProvider.getInstance(getApplicationContext());
        initializeMap();
        new HeatMapTask(this, getApplicationContext()).execute();
    }

    private void initializeMap() {
        org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);
        map = (MapView) findViewById(R.id.osmap);

        //final MapQuestTileSource tileSource = new MapQuestTileSource(context);
        final MapQuestTileSource tileSource = new MapQuestTileSource("mapquest.light-mb", "");
        tileSource.setAccessToken(getString(R.string.access_token));
        map.setTileSource(tileSource);

        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
    }

    private void drawHeatMap(List<Polygon> heatMap) {
        map.getOverlays().clear();
        for (Polygon p : heatMap) {
            map.getOverlays().add(p);
        }
        map.invalidate();
    }

    private void setCentralPosition() {
        mapController = map.getController();
        mapController.setZoom(12);
        Pair medianPosition = hmp.getMedianPosition();
        GeoPoint medianGeoPoint = new GeoPoint((Double) medianPosition.first, (Double) medianPosition.second);
        mapController.setCenter(medianGeoPoint);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_hotspots, menu);
        return true;
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        InfoWindow.closeAllInfoWindowsOn(map);
        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }

    @Override
    public void onDataTaskCompleted(List<TimePolygon> polygons, boolean fullRange) {

    }

    @Override
    public void onHeatMapTaskCompleted(List<Polygon> heatMap) {
        drawHeatMap(heatMap);
        setCentralPosition();
    }
}
