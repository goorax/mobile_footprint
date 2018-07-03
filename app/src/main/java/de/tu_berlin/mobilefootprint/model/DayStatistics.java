package de.tu_berlin.mobilefootprint.model;

/**
 * Created by niels on 12/13/16.
 */
public class DayStatistics {

    private int callCount = 0;
    private int textMessageCount = 0;
    private int dataCount = 0;
    private int handOverCount = 0;
    private int locationUpdateCount = 0;

    public void setCallCount(int count) { this.callCount = count; }

    public void setTextMessageCount(int count) {
        this.textMessageCount = count;
    }

    public void setDataCount(int dataCount) {
        this.dataCount = dataCount;
    }

    public void setHandOverCount(int handOverCount) {
        this.handOverCount = handOverCount;
    }

    public void setLocationUpdateCount(int locationUpdateCount) {
        this.locationUpdateCount = locationUpdateCount;
    }

    public int getCallCount() {
        return this.callCount;
    }

    public int getTextMessageCount() {
        return this.textMessageCount;
    }

    public int getDataCount() {
        return dataCount;
    }

    public int getHandOverCount() {
        return handOverCount;
    }

    public int getLocationUpdateCount() {
        return locationUpdateCount;
    }
}
