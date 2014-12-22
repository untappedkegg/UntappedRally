package com.untappedkegg.rally.data;

import android.content.ContentValues;
import android.database.Cursor;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.util.DateManager;

import java.util.Calendar;


public class DbUpdated extends BaseDbAccessor {

    public DbUpdated() {
        //Required empty constructor
    }

    public static final String UPDATED_TABLE = "tbl_updated";

    public static final String ID = "_id";
    public static final String TIME = "updated";
    public static final String SOURCE = "source";

    /**
     * @param source The Module which is making the request (ie. com.WRCLive.News)
     * @return
     */
    public static final void updated_insert(String source) {
        long time = System.currentTimeMillis();
        ContentValues initialValues = new ContentValues();
        initialValues.put(SOURCE, source);
        initialValues.put(TIME, time);
        String where = String.format("'%s' = %s", source, SOURCE);
        dbAdapter.updateIfExistsElseInsert(UPDATED_TABLE, initialValues, where);
    }

    /**
     * @param source The Module which is making the request (ie. com.WRCLive.News)
     * @return a {@code long} which represents the {@code System.currentTimeMillis()/1000} of the last update
     */
    public static final long lastUpdated_by_Source(String source) {
        final Cursor c = dbAdapter.select(String.format("SELECT %s FROM %s WHERE %s='%s'", TIME, UPDATED_TABLE, SOURCE, source));
        //check that something is returned to avoid 'Cursor Out of Bounds Exception
        //if no results are returned, make sure that an update is performed
        if (c.getCount() > 0 && c.moveToFirst()) {
            //		c.moveToFirst();
            final long time = c.getLong(c.getColumnIndex(TIME));
            c.close();
            return time;
        } else {
            c.close();
            if (source.equals(AppState.MOD_NEWS)) {
                return DateManager.add(Calendar.MINUTE, DateManager.now(), -(AppState.RSS_UPDATE_DELAY + 1)).getTime();
            } else if (source.equals(AppState.MOD_SCHED)) {
                return DateManager.add(Calendar.DATE, DateManager.now(), -(AppState.CAL_UPDATE_DELAY + 1)).getTime();
            } else {
                return 0;
            }
        }
    }
}
