package com.untappedkegg.rally.event;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.BaseDbAccessor;
import com.untappedkegg.rally.data.DbAdapter;
import com.untappedkegg.rally.schedule.DbSchedule;
import com.untappedkegg.rally.util.DateManager;

import java.util.Locale;

public final class DbEvent extends BaseDbAccessor {

    public static final String PHOTO_TABLE = "photos";
    public static final String PHOTO_ID = "_id";
    public static final String PHOTO_EVENT = "photos_event";
    public static final String PHOTO_YEAR = "year";
    public static final String PHOTO_URL = "p_url";
    public static final String PHOTO_TITLE = "title";

    public static final String STAGES_TABLE = "stages";
    public static final String STAGES_ID = "_id";
    public static final String STAGES_EVENT = "stages_event";
    public static final String STAGES_YEAR = "year";
    public static final String STAGES_NAME = "name";
    public static final String STAGES_LENGTH = "length";
    public static final String STAGES_ATC = "atc";
    public static final String STAGES_NUMBER = "num";
    public static final String STAGES_HEADER = "header";
    public static final String STAGES_TIMES = "times";
    public static final String STAGES_RESULTS = "results";


    // Create Statements
    private static final String EVENT_PHOTOS_CREATE = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT, %s TEXT, %s INTEGER)", DbEvent.PHOTO_TABLE, DbEvent.PHOTO_ID, DbEvent.PHOTO_TITLE, DbEvent.PHOTO_URL, DbEvent.PHOTO_EVENT, DbEvent.PHOTO_YEAR);

    private static final String STAGES_CREATE = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s INTEGER, %s INTEGER, %s NUMERIC, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT)", DbEvent.STAGES_TABLE, DbEvent.STAGES_ID, DbEvent.STAGES_YEAR, DbEvent.STAGES_NUMBER, DbEvent.STAGES_LENGTH, DbEvent.STAGES_ATC, DbEvent.STAGES_NAME, DbEvent.STAGES_EVENT, DbEvent.STAGES_HEADER, DbEvent.STAGES_RESULTS, DbEvent.STAGES_TIMES);

    public static void create(final SQLiteDatabase db) {
        Log.d(LOG_TAG, "Creating table: " + DbEvent.PHOTO_TABLE);
        db.execSQL(EVENT_PHOTOS_CREATE);

        Log.d(LOG_TAG, "Creating table: " + DbEvent.STAGES_TABLE);
        db.execSQL(STAGES_CREATE);

        // Indexes
        db.execSQL(String.format(CREATE_INDEX, PHOTO_EVENT, PHOTO_TABLE, PHOTO_EVENT));
        db.execSQL(String.format(CREATE_INDEX, STAGES_EVENT, STAGES_TABLE, STAGES_EVENT));
        db.execSQL(String.format(CREATE_TWO_INDICES, STAGES_YEAR, STAGES_TABLE, STAGES_EVENT, STAGES_YEAR));
        db.execSQL(String.format(CREATE_TWO_INDICES, STAGES_NUMBER, STAGES_TABLE, STAGES_EVENT, STAGES_NUMBER));

    }

    public static void drop(final SQLiteDatabase db) {
        db.execSQL(DROP + DbEvent.PHOTO_TABLE);

        db.execSQL(DROP + DbEvent.STAGES_TABLE);
    }

    public static Cursor fetchDetails(final int eventId) {
        return dbAdapter.selectf("SELECT * FROM %s WHERE %s = %s", DbSchedule.SCHED_TABLE, DbSchedule.SCHED_ID, eventId);
    }

    public static String fetchNameById(final String eventId) {
        Cursor c = dbAdapter.selectf("SELECT %s FROM %s WHERE %s = %s", DbSchedule.SCHED_TITLE, DbSchedule.SCHED_TABLE, DbSchedule.SCHED_ID, eventId);
        if (c.moveToFirst()) {
            String name = c.getString(c.getColumnIndex(DbSchedule.SCHED_TITLE));
            c.close();
            return name;
        }
        return null;
    }

    public static void photosInsert(final String title, final String link, final String eventCode, final String year) {
        ContentValues vals = new ContentValues();
        vals.put(PHOTO_TITLE, title);
        vals.put(PHOTO_URL, link);
        vals.put(PHOTO_EVENT, eventCode);
        vals.put(PHOTO_YEAR, year);

        dbAdapter.insert(PHOTO_TABLE, vals);
    }

    public static Cursor getPhotosByEvent_and_Year(final String eventCode, final String year) {
        return dbAdapter.selectf("SELECT * FROM %s WHERE %s = '%s' AND %s = %s", PHOTO_TABLE, PHOTO_EVENT, eventCode, PHOTO_YEAR, year);
    }

    public static void stagesInsert(final String event, final String name, final String num, final String atc, final String length, final String year, final String header) {
        ContentValues vals = new ContentValues();
        vals.put(STAGES_EVENT, event);
        vals.put(STAGES_YEAR, year);
        vals.put(STAGES_NAME, name);
        vals.put(STAGES_NUMBER, num);
        vals.put(STAGES_ATC, atc);
        vals.put(STAGES_LENGTH, length);
        vals.put(STAGES_HEADER, header);

        dbAdapter.insert(STAGES_TABLE, vals);
    }

    public static void stageResultsInsert(final String eventCode, final short year, final short stage, final String results, final boolean isResults) {
        ContentValues vals = new ContentValues();
        if(isResults) {
            vals.put(STAGES_RESULTS, results);
        } else
        {
            vals.put(STAGES_TIMES, results);
        }

        dbAdapter.update(STAGES_TABLE, vals, String.format("%s = '%s' AND %s = %s AND %s = %s", STAGES_EVENT, eventCode, STAGES_NUMBER, stage, STAGES_YEAR, year));
    }

    public static Cursor stagesSelect(final String eventCode, final String year) {
        return dbAdapter.selectf("SELECT %s, %s || '. ' || %s AS %s, %s, %s || ' mi.' AS %s, %s, %s FROM %s WHERE %s = '%s' AND %s = %s ORDER BY %s",
                STAGES_ID, STAGES_NUMBER, STAGES_NAME, STAGES_NAME, STAGES_ATC, STAGES_LENGTH, STAGES_LENGTH, STAGES_NUMBER, STAGES_HEADER, STAGES_TABLE, STAGES_EVENT, eventCode, STAGES_YEAR, year, STAGES_NUMBER);
    }

    public static Cursor stagesHeaderSelect(final String eventCode, final String year) {
        return dbAdapter.selectf("SELECT DISTINCT %s, %s, %s, %s FROM %s WHERE %s = '%s' AND %s = '%s' GROUP BY %s",
                STAGES_HEADER, STAGES_YEAR, STAGES_EVENT, STAGES_ID, STAGES_TABLE, STAGES_EVENT, eventCode, STAGES_YEAR, year, STAGES_HEADER);
    }

    public static Cursor getChildren(String forHeader, String eventCode, String year) {
        return dbAdapter.selectf("SELECT %s, %s || '. ' || %s AS %s, %s, %s || ' mi.' AS %s, %s FROM %s WHERE %s = '%s' AND %s = %s AND %s = '%s' ORDER BY %s",
                STAGES_ID, STAGES_NUMBER, STAGES_NAME, STAGES_NAME, STAGES_ATC, STAGES_LENGTH, STAGES_LENGTH, STAGES_NUMBER, STAGES_TABLE, STAGES_EVENT, eventCode, STAGES_YEAR, year, STAGES_HEADER, forHeader, STAGES_NUMBER);
    }

    public static void delete_stages(final String eventCode, final String year) {
        final String where = String.format("%s = '%s' AND %s = %s", STAGES_EVENT, eventCode, STAGES_YEAR, year);
        dbAdapter.delete(STAGES_TABLE, where);
    }

    public static void delete_photos(final String eventCode, final String year) {
        final String where = String.format("%s = '%s' AND %s = %s", PHOTO_EVENT, eventCode, PHOTO_YEAR, year);
        dbAdapter.delete(PHOTO_TABLE, where);
    }

    public static String getStageName(final String year, final String eventCode, final short stageNo) {
        final Cursor c = dbAdapter.selectf("SELECT %s || '. ' || %s AS %s FROM %s WHERE %s = '%s' AND %s = %s AND %s = %s", STAGES_NUMBER, STAGES_NAME, STAGES_NAME, STAGES_TABLE, STAGES_EVENT, eventCode, STAGES_NUMBER, stageNo, STAGES_YEAR, year);
        if (c.moveToFirst()) {
            final String stageName = c.getString(0);
            c.close();
            return stageName;
        }
        c.close();
        return "";
    }

    public static String[] getStageNamesForEvent(final String year, final String eventCode) {
        final Cursor c = dbAdapter.selectf("SELECT %s || '. ' || %s AS %s FROM %s WHERE %s = '%s' AND %s = %s ORDER BY %s ASC", STAGES_NUMBER, STAGES_NAME, STAGES_NAME, STAGES_TABLE, STAGES_EVENT, eventCode, STAGES_YEAR, year, STAGES_NUMBER);

        return DbAdapter.toStringArray(c);
    }

    public static short getMaxStageNumber(final String year, final String eventCode) {
        final Cursor c = dbAdapter.selectf("SELECT MAX(%s) FROM %s WHERE %s = '%s' AND %s = %s", STAGES_NUMBER, STAGES_TABLE, STAGES_EVENT, eventCode, STAGES_YEAR, year);
        if (c.moveToFirst()) {
            final short maxStage = c.getShort(0);
            c.close();
            return maxStage;
        }
        c.close();
        return 0;
    }

    public static String fetchStageResults(final String eventCode, final short year, final short curStage, final boolean isResults) {
        final Cursor c = dbAdapter.selectf("SELECT %s FROM %s WHERE %s = '%s' AND %s = %s AND %s = %s", isResults ? STAGES_RESULTS : STAGES_TIMES, STAGES_TABLE, STAGES_EVENT, eventCode, STAGES_NUMBER, curStage, STAGES_YEAR, year);
        if (c.moveToFirst()) {
            final String results = c.getString(0);
            c.close();
            if (TextUtils.isEmpty(results)) {
                return AppState.getApplication().getResources().getString(R.string.stage_results_format, AppState.getApplication().getResources().getString(R.string.stage_results_error));
            } else {
                return results;
            }
        }
        c.close();
        return AppState.getApplication().getResources().getString(R.string.stage_results_format, AppState.getApplication().getResources().getString(R.string.stage_results_error));
    }


    public static boolean isStageDataPresent(String event) {
        return dbAdapter.count(STAGES_TABLE, String.format(Locale.US, "%s = %s AND %s = '%s'", STAGES_YEAR, DateManager.now(DateManager.YEAR), STAGES_EVENT, event)) > 0;
    }

//    public static Cursor getNextStage() {
//        final String today = DateManager.format(DateManager.now(), DateManager.DATABASE);
//        final String year = DateManager.format(DateManager.now(), DateManager.YEAR);
//
////        Calendar.getInstance().getTimeZone()
////        final String query = String.format(Locale.US, "SELECT ", );
//        if (AppState.getSettings().getBoolean(AppState.getApplication().getString(R.string.settings_show_regional_events), true)) {
//            return dbAdapter.selectf("SELECT * FROM %s WHERE '%s' <= %s AND %s >= %s ORDER BY %s ASC, %s ASC LIMIT 2", STAGES_TABLE, today, SCHED_END_DATE, SCHED_YEAR, year, SCHED_YEAR, SCHED_START_DATE);
//        } else {
//            return dbAdapter.selectf("SELECT * FROM %s WHERE '%s' <= %s AND %s >= %s AND %s ORDER BY %s ASC, %s ASC LIMIT 2", STAGES_TABLE, today, SCHED_END_DATE, SCHED_YEAR, year, NATIONAL, SCHED_YEAR, SCHED_START_DATE);
//        }
//    }

}
