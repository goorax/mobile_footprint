package de.tu_berlin.mobilefootprint.util;


import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.graphics.ColorUtils;
import android.util.Pair;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import de.tu_berlin.snet.cellservice.model.database.GeoDatabaseHelper;
import de.tu_berlin.snet.cellservice.model.record.Position;
import jsqlite.Database;
import jsqlite.Exception;
import jsqlite.TableResult;

/**
 * HeatMapProvider will provide tessellation of buffered and unioned MLS cell positions, which were
 * collected. It also computes a heat map over this data.
 *
 * @author johannes
 */
public class HeatMapProvider {
    private static final Logger logger = LoggerManager.getLogger(HeatMapProvider.class);
    public static final double VALUE_METERS = 1000;
    public static final long FOUR_WEEKS = 2419200L;

    private static HeatMapProvider instance;
    private GeoDatabaseHelper geoDatabaseHelper;
    private Context context;
    private List<Polygon> heatMap;
    private List<Polygon> tessellation;
    private List<Polygon> grid;
    private DataProvider dataProvider;
    private Database mDb;
    private BoundingBox boundingBox;
    private List<Double> latitudeList;
    private List<Double> longitudeList;
    private Pair<Double, Double> medianPosition;

    public static synchronized HeatMapProvider getInstance(Context context) {
        if (instance == null) {
            instance = new HeatMapProvider(context);
        }
        return instance;
    }

    private HeatMapProvider(Context context) {
        this.context = context;
        geoDatabaseHelper = GeoDatabaseHelper.getInstance(context);
        dataProvider = DataProvider.getInstance(context);
        heatMap = new ArrayList<>();
        tessellation = new ArrayList<>();
        grid = new ArrayList<>();
        latitudeList = new ArrayList<>();
        longitudeList = new ArrayList<>();
        medianPosition = new Pair<>(52.51, 13.40);
        mDb = geoDatabaseHelper.getmDb();
        performAdjustmentsForExistingDbs();
    }

    private void performAdjustmentsForExistingDbs() {
        geoDatabaseHelper.execSQL("CREATE TABLE IF NOT EXISTS HeatMap (\n" +
                "  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                "  fillColor INTEGER\n" +
                ");\n");
        geoDatabaseHelper.execSQL("SELECT AddGeometryColumn('HeatMap','Element',4326,'POLYGON',2);");
    }


    public int processTessellation(DataLoaderTask.Progress progress) {
        int tessellationProgress = 10;
        int cnt = 0;
        double progressThreshold = 0;
        logger.d("Processing tessellation.");
        progress.publish(++tessellationProgress);
        JsonObject tessellation = getTessellationOfPolygons();
        if (!(tessellation.size() == 0)) {
            progress.publish(++tessellationProgress);
            JsonArray coords = tessellation.getAsJsonArray("coordinates");
            progressThreshold = coords.size() * 7 / 38;
            Iterator<JsonElement> it = coords.iterator();
            while (it.hasNext()) {
                JsonArray ja = it.next().getAsJsonArray();
                Iterator<JsonElement> it2 = ja.iterator();
                while (it2.hasNext()) {
                    JsonArray ja2 = it2.next().getAsJsonArray();
                    Iterator<JsonElement> it3 = ja2.iterator();
                    List<GeoPoint> coordinates = new ArrayList<>();
                    while (it3.hasNext()) {
                        JsonArray pos = it3.next().getAsJsonArray();
                        GeoPoint gp = new GeoPoint(pos.get(1).getAsDouble(), pos.get(0).getAsDouble());
                        coordinates.add(gp);
                        if (++cnt >= progressThreshold) {
                            cnt = 0;
                            progress.publish(++tessellationProgress);
                        }
                    }
                    addCoordsToTessellationAndHeatMap(coordinates);
                }
            }
        }
        logger.d("Processing tessellation done.");
        return tessellation.size();
    }

    private void addCoordsToTessellationAndHeatMap(List<GeoPoint> coordinates) {
        Polygon tessellationPolygon = new Polygon();
        tessellationPolygon.setPoints(coordinates);
        tessellationPolygon.setStrokeWidth(1f);
        tessellationPolygon.setStrokeColor(Color.BLACK);
        tessellationPolygon.setFillColor(Color.TRANSPARENT);
        this.tessellation.add(tessellationPolygon);
        // A deep copy of the tessellation is required
        Polygon gridPolygon = new Polygon();
        gridPolygon.setPoints(coordinates);
        gridPolygon.setStrokeWidth(1f);
        gridPolygon.setStrokeColor(Color.BLACK);
        gridPolygon.setFillColor(Color.TRANSPARENT);
        this.grid.add(gridPolygon);
    }

    public void saveHeatMapToDb() {
        for (Polygon p : getHeatMap()) {
            StringBuilder polygonString = new StringBuilder();
            for (GeoPoint gp : p.getPoints()) {
                polygonString.append(transformToPolygonString(gp));
            }
            polygonString.deleteCharAt(polygonString.length() - 2);
            insertHeatMapElement(polygonString.toString(), p.getFillColor());
        }
    }

    private Map<JsonObject, Integer> retrieveHeatMapElementsFromDb() {
        Map<JsonObject, Integer> heatMapElements = new HashMap<>();
        final String selectTessellationElements =
                "SELECT asGeoJSON(Element), fillColor FROM HeatMap;";
        try {
            TableResult tableResult = mDb.get_table(selectTessellationElements);
            Vector<String[]> rows = tableResult.rows;
            for (String[] fields : rows) {
                String jsonString = fields[0];
                int fillColor = Integer.valueOf(fields[1]);
                heatMapElements.put(new JsonParser().parse(jsonString).getAsJsonObject(), fillColor);
            }
        } catch (Exception e) {
            logger.e("Error during retrival of heat map elements from db.", e);
        }
        return heatMapElements;
    }


    public void processHeatMap(DataLoaderTask.Progress progress, boolean useBoudingBox) {
        int heatMapProgress = 50;
        int cnt = 0;
        double progressThreshold = 0;
        logger.d("Processing heat map.");
        geoDatabaseHelper.deleteFrom("HeatMap");
        heatMap.clear();
        logger.d("Processing buffered positions.");
        ArrayList<Polygon> polygons = new ArrayList<>();
        Map<Integer, Position> cellLoc = dataProvider.getCellLocationMap();
        progressThreshold = cellLoc.size() / 9;
        for (Position position : cellLoc.values()) {
            if (!isPositionInTimeWindow(position)) {
                continue;
            }
            if (useBoudingBox) {
                if (!boundingBox.contains(position.getLat(), position.getLng())) {
                    continue;
                }
            }
            JsonObject bufferedPosition = getBufferedPosition(position);
            List<GeoPoint> coordinates = extractGeoPointsFromJson(bufferedPosition);
            Polygon p = new Polygon();
            p.setPoints(coordinates);
            p.setStrokeWidth(1f);
            p.setStrokeColor(Color.BLUE);
            polygons.add(p);
            if (++cnt >= progressThreshold) {
                cnt = 0;
                progress.publish(++heatMapProgress);
            }
        }
        logger.d("Processing buffered positions done.");
        heatMap.addAll(processIntersectsForHeatMap(progress, polygons, useBoudingBox));
        if (!useBoudingBox) {
            saveHeatMapToDb();
        }
        logger.d("Processing heat map done.");
    }

    private List<GeoPoint> extractGeoPointsFromJson(JsonObject geoPointsJson) {
        JsonArray coords = geoPointsJson.getAsJsonArray("coordinates");
        Iterator<JsonElement> it = coords.iterator();
        List<GeoPoint> coordinates = new ArrayList<>();
        while (it.hasNext()) {
            JsonArray ja = it.next().getAsJsonArray();
            Iterator<JsonElement> it2 = ja.iterator();
            while (it2.hasNext()) {
                JsonArray pos = it2.next().getAsJsonArray();
                GeoPoint gp = new GeoPoint(pos.get(1).getAsDouble(), pos.get(0).getAsDouble());
                coordinates.add(gp);
            }
        }
        return coordinates;
    }

    private ArrayList<Polygon> processIntersectsForHeatMap(DataLoaderTask.Progress progress, ArrayList<Polygon> polygons, boolean useBoundingBox) {
        logger.d("Processing intersects for heat map.");
        int intersectProgress = 60;
        int cnt = 0;
        double progressThreshold = (grid.size() * polygons.size()) / 38;
        ArrayList<Polygon> heatMap = new ArrayList<>();
        Iterator it = grid.iterator();
        while (it.hasNext()) {
            Polygon gridPolygon = (Polygon) it.next();
            if (useBoundingBox) {
                boolean isWithinBoundingBox = false;
                for (GeoPoint gp : gridPolygon.getPoints()) {
                    if (boundingBox.contains(gp.getLatitude(), gp.getLongitude())) {
                        isWithinBoundingBox = true;
                    }
                }
                if (!isWithinBoundingBox) {
                    continue;
                }
            }
            StringBuilder gridPolygonString = new StringBuilder();
            gridPolygonString.append("GeomFromText('POLYGON((");
            for (GeoPoint gp : gridPolygon.getPoints()) {
                gridPolygonString.append(transformToPolygonString(gp));
            }
            gridPolygonString.deleteCharAt(gridPolygonString.length() - 2);
            gridPolygonString.append("))', 4326)");

            for (Polygon p : polygons) {
                if (++cnt >= progressThreshold) {
                    cnt = 0;
                    progress.publish(++intersectProgress);
                }
                StringBuilder polygonString = new StringBuilder();
                polygonString.append("GeomFromText('POLYGON((");
                for (GeoPoint gp : p.getPoints()) {
                    polygonString.append(transformToPolygonString(gp));
                }
                polygonString.deleteCharAt(polygonString.length() - 2);
                polygonString.append("))', 4326)");

                String intersect = "SELECT Intersects(%1$s, %2$s);";
                int intersectValue = -2;
                try {
                    TableResult tableResult = mDb.get_table(String.format(intersect, polygonString, gridPolygonString));
                    String row = ((String[]) tableResult.rows.get(0))[0];
                    intersectValue = Integer.valueOf(row);
                } catch (Exception e) {
                    logger.e("Error during intersect processing", e);
                }
                if (intersectValue == 1) {
                    if (!heatMap.contains(gridPolygon)) {
                        gridPolygon.setFillColor(ColorUtils.setAlphaComponent(Color.YELLOW, 130));
                        gridPolygon.setStrokeWidth(3f);
                        gridPolygon.setStrokeColor(Color.WHITE);
                        heatMap.add(gridPolygon);
                        addPositionToList(gridPolygon);
                    } else {
                        gridPolygon.setFillColor(increaseRed(gridPolygon.getFillColor(), 0.8f));
                    }
                }
            }
        }
        calculateMedianPosition();
        logger.d("Processing intersects for heat map done.");
        return heatMap;
    }

    private void addPositionToList(Polygon p) {
        for (GeoPoint gp : p.getPoints()) {
            latitudeList.add(gp.getLatitude());
            longitudeList.add(gp.getLongitude());
        }
    }

    private void calculateMedianPosition() {
        Collections.sort(latitudeList);
        Collections.sort(longitudeList);
        if (latitudeList.size() == 0 && longitudeList.size() == 0) {
            // we do not have data -> set position to berlin
            medianPosition = new Pair<>(52.51, 13.40);
        } else {
            medianPosition = new Pair<>(latitudeList.get(latitudeList.size() / 2), longitudeList.get(longitudeList.size() / 2));
        }
    }

    private String transformToPolygonString(GeoPoint gp) {
        StringBuilder polygonString = new StringBuilder();
        polygonString.append(String.valueOf(gp.getLongitude()));
        polygonString.append(" ");
        polygonString.append(String.valueOf(gp.getLatitude()));
        polygonString.append(", ");
        return polygonString.toString();
    }

    private static int increaseRed(int color, float factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(a,
                Math.max(r, 0),
                Math.max((int) (g * factor), 0),
                Math.max((int) (b * factor), 0));
    }

    private JsonObject getBufferedPosition(Position pos) {
        JsonObject jo = new JsonObject();
        String selectBufferedPosition = "SELECT asGeoJSON(ST_Transform(ST_Buffer(ST_Transform(Geometry, 32632), %1$s, 10), 4326)) FROM Positions WHERE cell_id = %2$s;";
        int radius = 0;
        // this is done to filter outliers
        if (pos.getAccuracy() > 1000) {
            radius = 1000;
        } else {
            radius = new Double(pos.getAccuracy()).intValue();
        }
        String request = String.format(selectBufferedPosition, String.valueOf(radius), String.valueOf(pos.getCellId()));
        try {
            TableResult tableResult = mDb.get_table(request);
            String row = ((String[]) tableResult.rows.get(0))[0];
            jo = new JsonParser().parse(row).getAsJsonObject();
        } catch (Exception e) {
            logger.e("Error during position buffering.", e);
        }
        return jo;
    }

    private JsonObject getTessellationOfPolygons() {
        JsonObject jo = new JsonObject();
        String selectTessellation = "SELECT asGeoJSON(ST_Transform(ST_HexagonalGrid(ST_Union(ST_Buffer(ST_Transform(Geometry, 32632),%1$s, 10)), %1$s), 4326)) FROM Positions;\n";
        try {
            TableResult tableResult = mDb.get_table(String.format(selectTessellation, String.valueOf(VALUE_METERS)));
            if (!tableResult.rows.isEmpty() && tableResult.rows.get(0) != null) {
                String row = ((String[]) tableResult.rows.get(0))[0];
                if (row != null) {
                    jo = new JsonParser().parse(row).getAsJsonObject();
                }
            }
        } catch (Exception e) {
            logger.e("Error during tessellation of polygons.", e);
        }
        return jo;
    }

    private int getHeatMapDbSize() {
        String selectCountHeatMapDb = "SELECT COUNT(*) FROM HeatMap;";
        int size = 0;
        try {
            TableResult tableResult = mDb.get_table(selectCountHeatMapDb);
            size = Integer.valueOf((String) ((String[]) tableResult.rows.get(0))[0]);
        } catch (Exception e) {
            logger.e("Get size of HeatMap table failed.", e);
        }
        return size;
    }

    private void insertHeatMapElement(String polygonString, int fillColor) {
        final String insertHeatMapElementStatement =
                "INSERT INTO HeatMap (Element, fillColor)" +
                        "   VALUES (GeomFromText('POLYGON((%1$s))', 4326), %2$s);";
        String statement = String.format(insertHeatMapElementStatement, polygonString, String.valueOf(fillColor));
        geoDatabaseHelper.execSQL(statement);
    }

    private boolean isPositionInTimeWindow(Position position) {
        long end = System.currentTimeMillis() / 1000;
        long start = end - FOUR_WEEKS;
        for (long time : position.getUsagesByTime()) {
            if (time >= start && time <= end) {
                return true;
            }
        }
        return false;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public List<Polygon> getHeatMap() {
        if (heatMap.size() == 0) {
            logger.d("Loading cached heat map.");
            for (Map.Entry heatMapElement : retrieveHeatMapElementsFromDb().entrySet()) {
                List<GeoPoint> coordinates = extractGeoPointsFromJson((JsonObject) heatMapElement.getKey());
                Polygon p = new Polygon();
                p.setPoints(coordinates);
                p.setStrokeWidth(2f);
                p.setStrokeColor(Color.WHITE);
                p.setFillColor((Integer) heatMapElement.getValue());
                heatMap.add(p);
                addPositionToList(p);
            }
            calculateMedianPosition();
            logger.d("Loading cached heat map done.");
        }
        return heatMap;
    }

    public List<Polygon> getTessellation() {
        return tessellation;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public Pair<Double, Double> getMedianPosition() {
        return medianPosition;
    }
}
