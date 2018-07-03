package de.tu_berlin.mobilefootprint.model;

/**
 * Created by niels on 12/31/16.
 */

public class WeekStatistics {

    private DayStatistics[] days = new DayStatistics[7];

    public DayStatistics[] getDays () { return days; }

    public void setDays(DayStatistics[] days) {
        this.days = days;
    }

    public void setDay(int day, DayStatistics data) {

        this.days[day % 7] = data;
    }
}
