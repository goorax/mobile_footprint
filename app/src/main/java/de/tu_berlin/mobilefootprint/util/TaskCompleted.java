package de.tu_berlin.mobilefootprint.util;


import org.osmdroid.views.overlay.Polygon;

import java.util.List;

import de.tu_berlin.mobilefootprint.model.TimePolygon;

public interface TaskCompleted {

    void onDataTaskCompleted(List<TimePolygon> polygons, boolean fullRange);

    void onHeatMapTaskCompleted(List<Polygon> heatMap);
}


