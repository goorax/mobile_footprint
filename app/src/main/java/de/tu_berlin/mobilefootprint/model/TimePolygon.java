package de.tu_berlin.mobilefootprint.model;

import org.osmdroid.views.overlay.Polygon;

/**
 * Created by tob on 19.01.17.
 */

public class TimePolygon extends Polygon {

    private Long time;

    public TimePolygon() {
        super();
    }

    public TimePolygon(Long t) {
        super();
        this.setTime(t);
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
