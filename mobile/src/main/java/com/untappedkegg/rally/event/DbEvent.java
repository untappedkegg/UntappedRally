package com.untappedkegg.rally.event;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.BaseDbAccessor;
import com.untappedkegg.rally.data.DbAdapter;
import com.untappedkegg.rally.schedule.DbSchedule;

public class DbEvent extends BaseDbAccessor {

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

    public static final void create(SQLiteDatabase db) {
        Log.d(LOG_TAG, "Creating table: " + DbEvent.PHOTO_TABLE);
        db.execSQL(EVENT_PHOTOS_CREATE);

        Log.d(LOG_TAG, "Creating table: " + DbEvent.STAGES_TABLE);
        db.execSQL(STAGES_CREATE);

        // Indexes
        db.execSQL(String.format(CREATE_INDEX, PHOTO_EVENT, PHOTO_TABLE, PHOTO_EVENT));
        db.execSQL(String.format(CREATE_INDEX, STAGES_EVENT, STAGES_TABLE, STAGES_EVENT));
        db.execSQL(String.format(CREATE_TWO_INDICIES, STAGES_YEAR, STAGES_TABLE, STAGES_EVENT, STAGES_YEAR));
        db.execSQL(String.format(CREATE_TWO_INDICIES, STAGES_NUMBER, STAGES_TABLE, STAGES_EVENT, STAGES_NUMBER));

    }

    public static final void drop(SQLiteDatabase db) {
        db.execSQL(DROP + DbEvent.PHOTO_TABLE);

        db.execSQL(DROP + DbEvent.STAGES_TABLE);
    }

    public static final Cursor fetchDetails(String eventId) {
        return dbAdapter.selectf("SELECT * FROM %s WHERE %s = %s", DbSchedule.SCHED_TABLE, DbSchedule.SCHED_ID, eventId);
    }

    public static final String fetchNameById(String eventId) {
        Cursor c = dbAdapter.selectf("SELECT %s FROM %s WHERE %s = %s", DbSchedule.SCHED_TITLE, DbSchedule.SCHED_TABLE, DbSchedule.SCHED_ID, eventId);
        if (c.moveToFirst()) {
            String name = c.getString(c.getColumnIndex(DbSchedule.SCHED_TITLE));
            c.close();
            return name;
        }
        return null;
    }

    public static final void photosInsert(String title, String link, String eventCode, String year) {
        ContentValues vals = new ContentValues();
        vals.put(PHOTO_TITLE, title);
        vals.put(PHOTO_URL, link);
        vals.put(PHOTO_EVENT, eventCode);
        vals.put(PHOTO_YEAR, year);

        dbAdapter.insert(PHOTO_TABLE, vals);
    }

    public static final Cursor getPhotosByEvent_and_Year(String eventCode, String year) {

        return dbAdapter.selectf("SELECT * FROM %s WHERE %s = '%s' AND %s = %s", PHOTO_TABLE, PHOTO_EVENT, eventCode, PHOTO_YEAR, year);
    }

    public static final void stagesInsert(String event, String name, String num, String atc, String length, String year, String header) {
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

    public static final void stageResultsInsert(String eventCode, short year, short stage, String results, boolean isResults) {
        ContentValues vals = new ContentValues();
        if(isResults) {
            vals.put(STAGES_RESULTS, results);
        } else
        {
            vals.put(STAGES_TIMES, results);
        }

        dbAdapter.update(STAGES_TABLE, vals, String.format("%s = '%s' AND %s = %s AND %s = %s", STAGES_EVENT, eventCode, STAGES_NUMBER, stage, STAGES_YEAR, year));
    }

    public static final Cursor stagesSelect(String eventCode, String year) {
        return dbAdapter.selectf("SELECT %s, %s || '. ' || %s AS %s, %s, %s || ' mi.' AS %s, %s, %s FROM %s WHERE %s = '%s' AND %s = %s ORDER BY %s",
                STAGES_ID, STAGES_NUMBER, STAGES_NAME, STAGES_NAME, STAGES_ATC, STAGES_LENGTH, STAGES_LENGTH, STAGES_NUMBER, STAGES_HEADER, STAGES_TABLE, STAGES_EVENT, eventCode, STAGES_YEAR, year, STAGES_NUMBER);
    }

    public static final void delete_stages(String eventCode, String year) {
        final String where = String.format("%s = '%s' AND %s = %s", STAGES_EVENT, eventCode, STAGES_YEAR, year);
        dbAdapter.delete(STAGES_TABLE, where);
    }

    public static final void delete_photos(String eventCode, String year) {
        final String where = String.format("%s = '%s' AND %s = %s", PHOTO_EVENT, eventCode, PHOTO_YEAR, year);
        dbAdapter.delete(PHOTO_TABLE, where);
    }

    public static final String getStageName(String year, String eventCode, short stageNo) {
        final Cursor c = dbAdapter.selectf("SELECT %s || '. ' || %s AS %s FROM %s WHERE %s = '%s' AND %s = %s AND %s = %s", STAGES_NUMBER, STAGES_NAME, STAGES_NAME, STAGES_TABLE, STAGES_EVENT, eventCode, STAGES_NUMBER, stageNo, STAGES_YEAR, year);
        if (c.moveToFirst()) {
            final String stageName = c.getString(0);
            c.close();
            return stageName;
        }
        c.close();
        return "";
    }

    public static final String[] getStageNamesForEvent(String year, String eventCode) {
        final Cursor c = dbAdapter.selectf("SELECT %s || '. ' || %s AS %s FROM %s WHERE %s = '%s' AND %s = %s ORDER BY %s ASC", STAGES_NUMBER, STAGES_NAME, STAGES_NAME, STAGES_TABLE, STAGES_EVENT, eventCode, STAGES_YEAR, year, STAGES_NUMBER);

        return DbAdapter.toStringArray(c);
    }

    public static final short getMaxStageNumber(String year, String eventCode) {
        final Cursor c = dbAdapter.selectf("SELECT MAX(%s) FROM %s WHERE %s = '%s' AND %s = %s", STAGES_NUMBER, STAGES_TABLE, STAGES_EVENT, eventCode, STAGES_YEAR, year);
        if (c.moveToFirst()) {
            final short maxStage = c.getShort(0);
            c.close();
            return maxStage;
        }
        c.close();
        return 0;
    }

    public static final String fetchStageResults(String eventCode, short year, short curStage, boolean isResults) {
        final Cursor c = dbAdapter.selectf("SELECT %s FROM %s WHERE %s = '%s' AND %s = %s AND %s = %s", isResults ? STAGES_RESULTS : STAGES_TIMES, STAGES_TABLE, STAGES_EVENT, eventCode, STAGES_NUMBER, curStage, STAGES_YEAR, year);
        if (c.moveToFirst()) {
            final String results = c.getString(0);
            c.close();
//            Log.w(STAGES_RESULTS, results + "");
            if (AppState.isNullOrEmpty(results)) {
                return AppState.getApplication().getResources().getString(R.string.stage_results_format, AppState.getApplication().getResources().getString(R.string.stage_results_error));
            } else {
                return results;
            }
        }
        c.close();
        return AppState.getApplication().getResources().getString(R.string.stage_results_format, AppState.getApplication().getResources().getString(R.string.stage_results_error));
    }

}
