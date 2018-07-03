package de.tu_berlin.mobilefootprint.model;

import java.util.Observable;

/**
 * Created by niels on 1/1/17.
 */

public class FilterQuery extends Observable {

    private int startDateTime = 0;
    private int endDateTime = 0;

    private boolean includeCalls = true;
    private boolean includeTextMessages = true;
    private boolean includeData = true;
    private boolean includeLocationUpdates = true;
    private boolean includeHandovers = true;

    public FilterQuery () {
        int now = (int) (System.currentTimeMillis() / 1000L);
        startDateTime = now - 60 * 60 * 24 * 30;
        endDateTime = now;
    }

    public int getStartDateTime() {
        return startDateTime;
    }

    public int getEndDateTime() {
        return endDateTime;
    }

    public boolean getIncludeCalls() {
        return includeCalls;
    }

    public boolean getIncludeTextMessages() {
        return includeTextMessages;
    }

    public boolean getIncludeData() {
        return includeData;
    }

    public boolean getIncludeLocationUpdates() {
        return includeLocationUpdates;
    }

    public boolean getIncludeHandovers() {
        return includeHandovers;
    }

    public void setStartDateTime(int startDateTime) {
        this.startDateTime = startDateTime;
        setChanged();
    }

    public void setEndDateTime(int endDateTime) {
        this.endDateTime = endDateTime;
        setChanged();
    }

    public void setIncludeCalls(boolean includeCalls) {
        this.includeCalls = includeCalls;
        setChanged();
    }

    public void setIncludeTextMessages(boolean includeTextMessages) {
        this.includeTextMessages = includeTextMessages;
        setChanged();
    }

    public void setIncludeData(boolean includeData) {
        this.includeData = includeData;
        setChanged();
    }

    public void setIncludeLocationUpdates(boolean includeLocationUpdates) {
        this.includeLocationUpdates = includeLocationUpdates;
        setChanged();
    }

    public void setIncludeHandovers(boolean includeHandovers) {
        this.includeHandovers = includeHandovers;
        setChanged();
    }
}
