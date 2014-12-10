package com.keggemeyer.rallyamerica.social;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.keggemeyer.rallyamerica.data.BaseDbAccessor;

public class DbSocial extends BaseDbAccessor {

    public DbSocial() {
    }

    public static final String YOUTUBE_TABLE = "youtube";
    public static final String YOUTUBE_ID = "_id";
    public static final String YOUTUBE_TITLE = "title";
    public static final String YOUTUBE_LINK = "link";
    public static final String YOUTUBE_DIR_LINK = "dir_link";
    public static final String YOUTUBE_ICON = "icon";

    public static final String YT_CREATE = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT, %s TEXT, %s TEXT)", YOUTUBE_TABLE, YOUTUBE_ID, YOUTUBE_TITLE, YOUTUBE_LINK, YOUTUBE_DIR_LINK, YOUTUBE_ICON);


    /* ----- PROCEDURES ----- */
    // global
    public static final void create(SQLiteDatabase db) {
        dbAdapter.create(db, YOUTUBE_TABLE, YT_CREATE);
    }

    public static final void drop(SQLiteDatabase db) {
        dbAdapter.drop(db, YOUTUBE_TABLE);
    }

    public static final void delete(String function) {
        dbAdapter.delete(function);
    }

    // Queries
    public static final void insertVideo(String title, String ytLink, String dirLink, String thumbnail) {
        ContentValues values = new ContentValues();
        values.put(YOUTUBE_TITLE, title);
        values.put(YOUTUBE_LINK, ytLink);
        values.put(YOUTUBE_DIR_LINK, dirLink);
        values.put(YOUTUBE_ICON, thumbnail);

        //		final String where = String.format("%s = %s", YOUTUBE_TITLE, title);

        dbAdapter.insert(YOUTUBE_TABLE, values);
    }

    public static final Cursor youtube_select() {
        return dbAdapter.selectf("SELECT * FROM %s", YOUTUBE_TABLE);
    }


}
