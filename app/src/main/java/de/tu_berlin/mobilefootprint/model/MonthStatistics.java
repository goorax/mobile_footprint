package de.tu_berlin.mobilefootprint.model;

/**
 * Created by niels on 12/31/16.
 */

public class MonthStatistics {

    private DayStatistics[] days;
    private int offsetDay; // First weekday of the month, from 0 = Monday through 6 = Friday
    private int offsetWeek; // First calendar week of the month

    /**
     * Empty constructor creates dummy data
     */
    public MonthStatistics () {

        java.util.Random r = new java.util.Random();

        this.days = new DayStatistics[30];
        this.offsetDay = r.nextInt(7);
        this.offsetWeek = 50;

        for (int i = 0; i < this.days.length; i++) {

            this.days[i] = new DayStatistics();

            this.days[i].setCallCount(r.nextInt(5));
            this.days[i].setTextMessageCount(r.nextInt(5));
            this.days[i].setDataCount(r.nextInt(10));
            this.days[i].setLocationUpdateCount(5);
            this.days[i].setHandOverCount(5);
        }
    }

    public MonthStatistics (int days, int offsetDay, int offsetWeek) {

        this(new DayStatistics[days], offsetDay, offsetWeek);
    }

    public MonthStatistics (DayStatistics[] days, int offsetDay, int offsetWeek) {

        this.days = days;
        this.offsetDay = offsetDay;
        this.offsetWeek = offsetWeek;
    }

    public int getOffsetDay () { return offsetDay; }
    public int getOffsetWeek () { return offsetWeek; }
    public DayStatistics[] getDays () { return days; }

    public void setDays(DayStatistics[] days) {
        this.days = days;
    }
}
