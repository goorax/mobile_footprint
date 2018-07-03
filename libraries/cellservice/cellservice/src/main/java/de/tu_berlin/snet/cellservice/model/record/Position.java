package de.tu_berlin.snet.cellservice.model.record;

import java.util.TreeSet;

public class Position {
    private int cellId;
    private double lat;
    private double lng;
    private double accuracy;
    private TreeSet<Long> usagesByTime;

    public Position(double lat, double lng, double accuracy, int cellId) {
        this.lat = lat;
        this.lng = lng;
        this.accuracy = accuracy;
        this.cellId = cellId;
        usagesByTime = new TreeSet<>();
    }

    public double getAccuracy() {
        return accuracy;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public int getCellId() {
        return cellId;
    }

    public void addTime(Long time) {
        usagesByTime.add(time);
    }

    public TreeSet<Long> getUsagesByTime() {
        return usagesByTime;
    }
}
