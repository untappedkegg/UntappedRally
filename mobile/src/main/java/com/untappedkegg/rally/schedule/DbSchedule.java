package com.untappedkegg.rally.schedule;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.BaseDbAccessor;
import com.untappedkegg.rally.util.DateManager;

public class DbSchedule extends BaseDbAccessor {

    public DbSchedule() {
        // Required Empty Constructor
    }


    public static final String SCHED_TABLE = "schedule_table";
    public static final String SCHED_ID = "_id";
    public static final String SCHED_TITLE = "title";
    public static final String SCHED_START_DATE = "start";
    public static final String SCHED_FROM_TO = "range";
    public static final String SCHED_SHORT_CODE = "code";
    public static final String SCHED_SERIES = "series";
    public static final String SCHED_END_DATE = "end";
    public static final String SCHED_YEAR = "year";
    public static final String SCHED_YEAR_ACTUAL = "year_actual";
    public static final String SCHED_SITE = "website";
    public static final String SCHED_SEQ = "sequence";
    public static final String SCHED_EVT_SITE = "event_site";
    public static final String SCHED_IMG = "img_link";
    public static final String SCHED_DESCR = "description";
    public static final String SCHED_LOC = "location";

    private static final String NATIONAL = String.format("%s = '%s'", SCHED_SERIES, "National");
    private static boolean SHOW_ALL;


    // Create statements
    private static final String SCHEDULE_CREATE = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s NUMERIC, %s TEXT, %s INTEGER, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s INTEGER)", DbSchedule.SCHED_TABLE, DbSchedule.SCHED_ID, DbSchedule.SCHED_TITLE, DbSchedule.SCHED_START_DATE, DbSchedule.SCHED_FROM_TO, DbSchedule.SCHED_SHORT_CODE, DbSchedule.SCHED_SERIES, DbSchedule.SCHED_END_DATE, DbSchedule.SCHED_YEAR, DbSchedule.SCHED_SITE, DbSchedule.SCHED_SEQ, DbSchedule.SCHED_EVT_SITE, DbSchedule.SCHED_IMG, DbSchedule.SCHED_DESCR, DbSchedule.SCHED_LOC, DbSchedule.SCHED_YEAR_ACTUAL);


    public static final void create(SQLiteDatabase db) {
        Log.d(LOG_TAG, "Creating table: " + DbSchedule.SCHED_TABLE);
        db.execSQL(SCHEDULE_CREATE);

        //Indexes
        db.execSQL(String.format(CREATE_INDEX, SCHED_END_DATE, SCHED_TABLE, SCHED_END_DATE));
        db.execSQL(String.format(CREATE_INDEX, SCHED_SHORT_CODE, SCHED_TABLE, SCHED_SHORT_CODE));
    }

    public static final void drop(SQLiteDatabase db) {
        db.execSQL(DROP + SCHED_TABLE);
    }

    public static final void insert_schedule(String title, String startDate, String endDate, String fromTo, String imgLink, String series, String year, String website, String seq, String eventSite, String location, String yearActual) {
        final ContentValues vals = new ContentValues();
        vals.put(SCHED_TITLE, title);
        vals.put(SCHED_START_DATE, startDate);
        vals.put(SCHED_END_DATE, endDate);
        vals.put(SCHED_FROM_TO, fromTo);
        vals.put(SCHED_SHORT_CODE, eventSite.substring(eventSite.lastIndexOf("/") + 1));
        vals.put(SCHED_SERIES, series);
        vals.put(SCHED_YEAR, year);
        vals.put(SCHED_SITE, website);
        vals.put(SCHED_SEQ, seq);
        vals.put(SCHED_EVT_SITE, eventSite);
        vals.put(SCHED_IMG, imgLink);
        vals.put(SCHED_LOC, location);
        vals.put(SCHED_YEAR_ACTUAL, yearActual);
        String where = String.format("%s = '%s' AND %s = %s", SCHED_TITLE, title, SCHED_YEAR, year);
        dbAdapter.updateIfExistsElseInsert(SCHED_TABLE, vals, where);
    }

    public static final void insert_schedule_description(String eventSite, String description) {
        final ContentValues vals = new ContentValues();
        vals.put(SCHED_DESCR, description);
        final String where = String.format("%s = '%s' ", SCHED_EVT_SITE, eventSite);
        dbAdapter.update(SCHED_TABLE, vals, where);
    }

    public static final Cursor fetch() {
        SHOW_ALL = AppState.getSettings().getBoolean(AppState.getApplication().getString(R.string.settings_show_regional_events), true);
        if (SHOW_ALL) {
            return dbAdapter.selectf("SELECT * FROM %s ORDER BY %s DESC, %s ASC", SCHED_TABLE, SCHED_YEAR, SCHED_START_DATE);
        } else {
            return dbAdapter.selectf("SELECT * FROM %s WHERE %s ORDER BY %s DESC, %s ASC", SCHED_TABLE, NATIONAL, SCHED_YEAR, SCHED_START_DATE);
        }
    }

    public static final Cursor fetchSections() {
        return dbAdapter.selectf("SELECT DISTINCT %s, _id FROM %s GROUP BY %s ORDER BY %s DESC", SCHED_YEAR_ACTUAL, SCHED_TABLE, SCHED_YEAR_ACTUAL, SCHED_YEAR_ACTUAL);
    }

    public static final Cursor fetchUpcoming() {
        SHOW_ALL = AppState.getSettings().getBoolean(AppState.getApplication().getString(R.string.settings_show_regional_events), true);
        final String today = DateManager.format(DateManager.now(), DateManager.ISO8601_DATEONLY);
        final String year = DateManager.format(DateManager.now(), DateManager.YEAR);
        if (SHOW_ALL) {
            return dbAdapter.selectf("SELECT * FROM %s WHERE '%s' <= %s AND %s >= %s ORDER BY %s ASC, %s ASC LIMIT 3", SCHED_TABLE, today, SCHED_END_DATE, SCHED_YEAR, year, SCHED_YEAR, SCHED_START_DATE);
        } else {
            return dbAdapter.selectf("SELECT * FROM %s WHERE '%s' <= %s AND %s >= %s AND %s ORDER BY %s ASC, %s ASC LIMIT 3", SCHED_TABLE, today, SCHED_END_DATE, SCHED_YEAR, year, NATIONAL, SCHED_YEAR, SCHED_START_DATE);
        }
    }

    public static final Cursor getChildren(String yearActual) {
        SHOW_ALL = AppState.getSettings().getBoolean(AppState.getApplication().getString(R.string.settings_show_regional_events), true);
//        Log.w(DbSchedule.LOG_TAG, "SHOW_ALL = " + SHOW_ALL );
        if (SHOW_ALL) {
            return dbAdapter.selectf("SELECT * FROM %s WHERE %s = %s ORDER BY %s ASC", SCHED_TABLE, SCHED_YEAR_ACTUAL, yearActual,SCHED_START_DATE );
        } else {
            return dbAdapter.selectf("SELECT * FROM %s WHERE %s = %s AND %s ORDER BY %s ASC", SCHED_TABLE, SCHED_YEAR_ACTUAL, yearActual, NATIONAL, SCHED_START_DATE);
        }
    }

    public static final Cursor fetchNextEvent() {
        final String today = DateManager.format(DateManager.now(), DateManager.ISO8601_DATEONLY);
        final String year = DateManager.format(DateManager.now(), DateManager.YEAR);
        SHOW_ALL = AppState.getSettings().getBoolean(AppState.getApplication().getString(R.string.settings_show_regional_events), true);
        if (SHOW_ALL) {
            return dbAdapter.selectf("SELECT * FROM %s WHERE '%s' <= %s AND %s >= %s ORDER BY %s ASC, %s ASC LIMIT 2", SCHED_TABLE, today, SCHED_END_DATE, SCHED_YEAR, year, SCHED_YEAR, SCHED_START_DATE);
        } else {
            return dbAdapter.selectf("SELECT * FROM %s WHERE '%s' <= %s AND %s >= %s AND %s ORDER BY %s ASC, %s ASC LIMIT 2", SCHED_TABLE, today, SCHED_END_DATE, SCHED_YEAR, year, NATIONAL, SCHED_YEAR, SCHED_START_DATE);
        }
    }
    public static boolean isEventStarted(int eventId) {
        final Cursor c = dbAdapter.selectf("SELECT %s FROM %s WHERE %s = %s", SCHED_START_DATE, SCHED_TABLE, SCHED_ID, eventId);

            try {
                c.moveToFirst();
                return DateManager.todayIsBefore(c.getString(0));
            } catch (Exception e) {
                return false;
            } finally {
                c.close();
            }

    }

    public static final boolean isEventFinished(String eventCode, short year) {
        final Cursor c = dbAdapter.selectf("SELECT %s, %s FROM %s WHERE %s = '%s' AND %s = %s", SCHED_START_DATE, SCHED_END_DATE, SCHED_TABLE, SCHED_SHORT_CODE, eventCode, SCHED_YEAR, year);
        if (c.moveToFirst()) {
            final short status = DateManager.todayIsBetween(c.getString(0), c.getString(1));
            c.close();
            return status == 2;
        }
        c.close();
        return false;
    }

    public static final boolean isEventFinished(int id) {
        final Cursor c = dbAdapter.selectf("SELECT %s, %s FROM %s WHERE %s = %s", SCHED_START_DATE, SCHED_END_DATE, SCHED_TABLE, SCHED_ID, id);
        if (c.moveToFirst()) {
            final short status = DateManager.todayIsBetween(c.getString(0), c.getString(1));
            c.close();
            return status == 2;
        }
        c.close();
        return false;
    }

    public static final boolean isEventFinished(String id) {
        final Cursor c = dbAdapter.selectf("SELECT %s, %s FROM %s WHERE %s = %s", SCHED_START_DATE, SCHED_END_DATE, SCHED_TABLE, SCHED_ID, id);
        if (c.moveToFirst()) {
            final short status = DateManager.todayIsBetween(c.getString(0), c.getString(1));
            c.close();
            return status == 2;
        }
        c.close();
        return false;
    }

    public static final String fetchNextEventStart() {
        final String today = DateManager.now(DateManager.ISO8601_DATEONLY);
//        Log.e("DbSchedule", "today = " + today);
        SHOW_ALL = AppState.getSettings().getBoolean(AppState.getApplication().getString(R.string.settings_show_regional_events), true);
        Cursor cursor;
        if ( SHOW_ALL ) {
            cursor = dbAdapter.selectf("SELECT * FROM %s WHERE '%s' <= %s ORDER BY %s DESC, %s ASC LIMIT 3", SCHED_TABLE, today, SCHED_END_DATE, SCHED_YEAR, SCHED_START_DATE);
        } else {
            cursor = dbAdapter.selectf("SELECT * FROM %s WHERE '%s' <= %s AND %s ORDER BY %s DESC, %s ASC LIMIT 3", SCHED_TABLE, today, SCHED_END_DATE, NATIONAL, SCHED_YEAR, SCHED_START_DATE);
        }
        cursor.moveToFirst();
        try {
            return cursor.getString(cursor.getColumnIndexOrThrow(SCHED_START_DATE));
        } catch (Exception e) {
            Log.e("DbSchedule", "Error getting time");
            return DateManager.now(DateManager.ISO8601_DATEONLY);
        } finally {
            cursor.close();
        }
    }

    public static String fetchNextEventImage() {
        final String today = DateManager.now(DateManager.ISO8601_DATEONLY);
        final Cursor c = dbAdapter.selectf("SELECT * FROM %s WHERE '%s' <= %s ORDER BY %s DESC, %s ASC LIMIT 3", SCHED_TABLE, today, SCHED_END_DATE, SCHED_YEAR, SCHED_START_DATE);
        c.moveToFirst();
        try {
            return c.getString(c.getColumnIndexOrThrow(SCHED_IMG));
        } catch (Exception e) {
            Log.e("DbSchedule", "Error getting time");
            return "";
        } finally {
            c.close();
        }
    }

    public static final String fetchEventRA_link(int eventId) {
        final Cursor c = dbAdapter.selectf("SELECT %s FROM %s WHERE %s = %s", SCHED_EVT_SITE, SCHED_TABLE, SCHED_ID, eventId);

        try {
            c.moveToFirst();
            return c.getString(0);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            c.close();
        }
    }
}
