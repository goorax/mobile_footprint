package de.tu_berlin.mobilefootprint.util;

import de.tu_berlin.mobilefootprint.model.FilterQuery;

/**
 * Created by niels on 1/4/17.
 */

public class FilterProvider {

    private static FilterProvider instance;

    private FilterQuery filter;

    private FilterProvider() {

        filter = new FilterQuery();
    }

    public static synchronized FilterProvider getInstance() {

        if (instance == null) {

            instance = new FilterProvider();
        }

        return instance;
    }

    public FilterQuery getFilter() {

        return filter;
    }
}
