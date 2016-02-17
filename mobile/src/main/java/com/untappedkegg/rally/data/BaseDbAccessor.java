package com.untappedkegg.rally.data;


import android.database.sqlite.SQLiteDatabase;

public class BaseDbAccessor {
    protected static final String LOG_TAG = BaseDbAccessor.class.getClass().getSimpleName();
    protected static final String DROP = "DROP TABLE IF EXISTS ";
    protected static final String CREATE_INDEX = "CREATE INDEX %s ON %s(%s)";
    protected static final String CREATE_TWO_INDICES = "CREATE INDEX %s ON %s(%s , %s)";
    protected final static DbAdapter dbAdapter = DbAdapter.getInstance();

    protected BaseDbAccessor() {
        // Do not allow instances
    }

    public static void open() {
        dbAdapter.open();
    }

    public static void close() {
        dbAdapter.close();
    }

    public static SQLiteDatabase getDb() {
        return dbAdapter.getDatabase();
    }
}
