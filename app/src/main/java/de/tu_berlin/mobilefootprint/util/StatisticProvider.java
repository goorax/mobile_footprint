package de.tu_berlin.mobilefootprint.util;

import android.content.Context;

import java.util.Calendar;
import java.util.GregorianCalendar;

import de.tu_berlin.mobilefootprint.model.DayStatistics;
import de.tu_berlin.mobilefootprint.model.FilterQuery;
import de.tu_berlin.mobilefootprint.model.MonthStatistics;
import de.tu_berlin.mobilefootprint.model.WeekStatistics;
import de.tu_berlin.snet.cellservice.model.database.GeoDatabaseHelper;

public class StatisticProvider {

    private static StatisticProvider instance;

    private GeoDatabaseHelper geoDatabaseHelper;
    private Context context;

    public static synchronized StatisticProvider getInstance (Context context) {

        if (instance == null) {

            instance = new StatisticProvider(context);
        }

        return instance;
    }

    private StatisticProvider(Context context) {

        this.context = context;
        this.geoDatabaseHelper = GeoDatabaseHelper.getInstance(context);
    }

    public String[][] getContactStats () {

        final String query =
                "SELECT count(id), address" +
                " FROM TextMessages" +
                " GROUP BY address" +
                ";";

        String[][] result = this.geoDatabaseHelper.getSQLTableResult(query);

        return result;
    }

    public WeekStatistics getWeekStatistics(FilterQuery query) {

        /*
        final Map<String, String> columns1 = new HashMap<String, String>();
        columns1.put("count", "count(id) AS count");
        columns1.put("weekday", "strftime('%w', starttime) AS weekday");

        final Map<String, String> columns2 = new HashMap<String, String>();
        columns2.put("count", "count(id) AS count");
        columns2.put("weekday", "strftime('%w', time) AS weekday");


        final SQLiteQueryBuilder callQueryBuilder = new SQLiteQueryBuilder();
        callQueryBuilder.setTables("Calls");
        callQueryBuilder.setProjectionMap(columns1);

        final SQLiteQueryBuilder dataQueryBuilder = new SQLiteQueryBuilder();
        dataQueryBuilder.setTables("DataRecords");
        dataQueryBuilder.setProjectionMap(columns1);

        for (SQLiteQueryBuilder queryBuilder : new SQLiteQueryBuilder[]{
                callQueryBuilder, dataQueryBuilder
        }) {
            if (query.getStartDateTime() > 0) {
                queryBuilder.appendWhere("starttime > " + query.getStartDateTime());

                if (query.getEndDateTime() > 0) {
                    queryBuilder.appendWhere(" AND starttime < " + query.getEndDateTime());
                }
            }
            else if (query.getEndDateTime() > 0) {
                queryBuilder.appendWhere("starttime < " + query.getEndDateTime());
            }
        }


        final SQLiteQueryBuilder textMessagesQueryBuilder = new SQLiteQueryBuilder();
        textMessagesQueryBuilder.setTables("TextMessages");
        textMessagesQueryBuilder.setProjectionMap(columns2);

        final SQLiteQueryBuilder locationUpdateQueryBuilder = new SQLiteQueryBuilder();
        textMessagesQueryBuilder.setTables("LocationUpdates");
        textMessagesQueryBuilder.setProjectionMap(columns2);

        final SQLiteQueryBuilder handoverQueryBuilder = new SQLiteQueryBuilder();
        textMessagesQueryBuilder.setTables("Handovers");
        textMessagesQueryBuilder.setProjectionMap(columns2);

        for (SQLiteQueryBuilder queryBuilder : new SQLiteQueryBuilder[]{
                textMessagesQueryBuilder, locationUpdateQueryBuilder, handoverQueryBuilder
        }) {
            if (query.getStartDateTime() > 0) {
                queryBuilder.appendWhere("time > " + query.getStartDateTime());

                if (query.getEndDateTime() > 0) {
                    queryBuilder.appendWhere(" AND time < " + query.getEndDateTime());
                }
            }
            else if (query.getEndDateTime() > 0) {
                queryBuilder.appendWhere("time < " + query.getEndDateTime());
            }
        }


        String queryCalls = callQueryBuilder.buildQuery(
                null, // projectionIn new String[] { "" },
                null, // selection
                "weekday", // groupBy
                null, // having
                null, // sortOrder
                null // limit
        );
        String queryTextMessages = textMessagesQueryBuilder.buildQuery(
                null, // projectionIn new String[] { "" },
                null, // selection
                "weekday", // groupBy
                null, // having
                null, // sortOrder
                null // limit
        );
        String queryData = dataQueryBuilder.buildQuery(
                null, // projectionIn new String[] { "" },
                null, // selection
                "weekday", // groupBy
                null, // having
                null, // sortOrder
                null // limit
        );
        String queryLocationUpdates = locationUpdateQueryBuilder.buildQuery(
                null, // projectionIn new String[] { "" },
                null, // selection
                "weekday", // groupBy
                null, // having
                null, // sortOrder
                null // limit
        );
        String queryHandovers = handoverQueryBuilder.buildQuery(
                null, // projectionIn new String[] { "" },
                null, // selection
                "weekday", // groupBy
                null, // having
                null, // sortOrder
                null // limit
        );
        */

        //*

        final DayStatistics[] days = new DayStatistics[]{
                new DayStatistics(),
                new DayStatistics(),
                new DayStatistics(),
                new DayStatistics(),
                new DayStatistics(),
                new DayStatistics(),
                new DayStatistics()
        };

        if (query.getIncludeTextMessages()) {

            final String queryTextMessages =
                    "SELECT count(id), strftime('%w', time) as weekday" +
                            " FROM TextMessages" +
                            " WHERE time > " + query.getStartDateTime() +
                            " AND time < " + query.getEndDateTime() +
                            " GROUP BY weekday" +
                            ";";

            final String[][] textMessages = this.geoDatabaseHelper.getSQLTableResult(queryTextMessages);

            for (String[] fields : textMessages) {

                days[Integer.parseInt(fields[1])].setTextMessageCount(Integer.parseInt(fields[0]));
            }
        }

        if (query.getIncludeCalls()) {

            final String queryCalls =
                    "SELECT count(id), strftime('%w', starttime) as weekday" +
                            " FROM Calls" +
                            " WHERE starttime > " + query.getStartDateTime() +
                            " AND starttime < " + query.getEndDateTime() +
                            " GROUP BY weekday" +
                            ";";

            final String[][] calls = this.geoDatabaseHelper.getSQLTableResult(queryCalls);

            for (String[] fields : calls) {

                days[Integer.parseInt(fields[1])].setCallCount(Integer.parseInt(fields[0]));
            }

            final String queryHandovers =
                    "SELECT count(id), strftime('%w', time) as weekday" +
                            " FROM Handovers" +
                            " WHERE time > " + query.getStartDateTime() +
                            " AND time < " + query.getEndDateTime() +
                            " GROUP BY weekday" +
                            ";";

            final String[][] handovers = this.geoDatabaseHelper.getSQLTableResult(queryHandovers);

            for (String[] fields : handovers) {

                days[Integer.parseInt(fields[1])].setHandOverCount(Integer.parseInt(fields[0]));
            }
        }

        if (query.getIncludeData()) {

            final String queryData =
                    "SELECT count(id), strftime('%w', starttime) as weekday" +
                            " FROM DataRecords" +
                            " WHERE starttime > " + query.getStartDateTime() +
                            " AND starttime < " + query.getEndDateTime() +
                            " GROUP BY weekday" +
                            ";";

            final String[][] data = this.geoDatabaseHelper.getSQLTableResult(queryData);

            for (String[] fields : data) {

                days[Integer.parseInt(fields[1])].setDataCount(Integer.parseInt(fields[0]));
            }
        }

        if (query.getIncludeLocationUpdates()) {

            final String queryLocationUpdates =
                    "SELECT count(id), strftime('%w', time) as weekday" +
                            " FROM LocationUpdates" +
                            " WHERE time > " + query.getStartDateTime() +
                            " AND time < " + query.getEndDateTime() +
                            " GROUP BY weekday" +
                            ";";

            final String[][] locationUpdates = this.geoDatabaseHelper.getSQLTableResult(queryLocationUpdates);

            for (String[] fields : locationUpdates) {

                days[Integer.parseInt(fields[1])].setLocationUpdateCount(Integer.parseInt(fields[0]));
            }
        }

        final WeekStatistics week = new WeekStatistics();
        week.setDays(days);

        return week;
    }

    public MonthStatistics getMonthStatistics(FilterQuery query) {

        //Calendar calendar = (Calendar) GregorianCalendar.getInstance();

        Calendar start = new GregorianCalendar();
        start.setTimeInMillis(query.getStartDateTime());

        Calendar end = new GregorianCalendar();
        end.setTimeInMillis(query.getEndDateTime());

        // Get the number of days
        int daysInMonth = start.getActualMaximum(Calendar.DAY_OF_MONTH);
        // Get the first day of the first week
        //int offsetDay = start.getActualMinimum(Calendar.DAY_OF_WEEK_IN_MONTH);
        int offsetDay = 0; // TODO
        // Get the number of the first week
        int offsetWeek = start.get(Calendar.WEEK_OF_YEAR);

        DayStatistics[] days = new DayStatistics[daysInMonth];

        for (int d = 0; d < daysInMonth; d++) {
            days[d] = new DayStatistics();
        }

        if (query.getIncludeCalls()) {

            final String queryCalls =
                    "SELECT count(id), strftime('%d', starttime) as day" +
                            " FROM Calls" +
                            " WHERE starttime > " + query.getStartDateTime() +
                            " AND starttime < " + query.getEndDateTime() +
                            " GROUP BY day" +
                            ";";

            final String[][] calls = this.geoDatabaseHelper.getSQLTableResult(queryCalls);

            for (String[] fields : calls) {

                days[Integer.parseInt(fields[1]) - 1].setCallCount(Integer.parseInt(fields[0]));
            }

            final String queryHandovers =
                    "SELECT count(id), strftime('%d', time) as day" +
                            " FROM Handovers" +
                            " WHERE time > " + query.getStartDateTime() +
                            " AND time < " + query.getEndDateTime() +
                            " GROUP BY day" +
                            ";";

            final String[][] handovers = this.geoDatabaseHelper.getSQLTableResult(queryHandovers);

            for (String[] fields : handovers) {

                days[Integer.parseInt(fields[1]) - 1].setHandOverCount(Integer.parseInt(fields[0]));
            }
        }

        if (query.getIncludeTextMessages()) {

            final String queryTextMessages =
                    "SELECT count(id), strftime('%d', time) as day" +
                            " FROM TextMessages" +
                            " WHERE time > " + query.getStartDateTime() +
                            " AND time < " + query.getEndDateTime() +
                            " GROUP BY day" +
                            ";";

            final String[][] textMessages = this.geoDatabaseHelper.getSQLTableResult(queryTextMessages);

            for (String[] fields : textMessages) {

                days[Integer.parseInt(fields[1]) - 1].setTextMessageCount(Integer.parseInt(fields[0]));
            }
        }

        if (query.getIncludeData()) {

            final String queryData =
                    "SELECT count(id), strftime('%d', starttime) as day" +
                            " FROM DataRecords" +
                            " WHERE starttime > " + query.getStartDateTime() +
                            " AND starttime < " + query.getEndDateTime() +
                            " GROUP BY day" +
                            ";";

            final String[][] dataRecords = this.geoDatabaseHelper.getSQLTableResult(queryData);

            for (String[] fields : dataRecords) {

                days[Integer.parseInt(fields[1]) - 1].setDataCount(Integer.parseInt(fields[0]));
            }
        }

        if (query.getIncludeLocationUpdates()) {

            final String queryLocationUpdates =
                    "SELECT count(id), strftime('%d', time) as day" +
                            " FROM LocationUpdates" +
                            " WHERE time > " + query.getStartDateTime() +
                            " AND time < " + query.getEndDateTime() +
                            " GROUP BY day" +
                            ";";

            final String[][] locationupdates = this.geoDatabaseHelper.getSQLTableResult(queryLocationUpdates);

            for (String[] fields : locationupdates) {

                days[Integer.parseInt(fields[1]) - 1].setLocationUpdateCount(Integer.parseInt(fields[0]));
            }
        }

        return new MonthStatistics(days, offsetDay, offsetWeek);
    }
}
