package com.untappedkegg.rally.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.event.DbEvent;
import com.untappedkegg.rally.news.DbNews;
import com.untappedkegg.rally.schedule.DbSchedule;
import com.untappedkegg.rally.social.DbSocial;

import java.util.Map.Entry;

public class DbAdapter {
    /* ----- CONSTANTS ----- */
    //	public static final String DEFAULTS_TBL = "defaults";
    //	public static final String DEFAULTS_ID = "_id";
    //	public static final String DEFAULTS_CODE = "code";
    //	public static final String DEFAULTS_VALUE = "value";
    //	public static final String DEFAULTS_UPDATE = "lastupdate";

    /**
     * Update this value anytime there is a change made in this page or to any of the constants that this page references
     */
    private static final int DB_VERSION = 16;
    public static final String DB_NAME = "WRCLive.db";

    private static final String LOG_TAG = DbAdapter.class.getSimpleName();

    //	Create Statements
    private static final String UPDATED_CREATE = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s REAL)", DbUpdated.UPDATED_TABLE, DbUpdated.ID, DbUpdated.SOURCE, DbUpdated.TIME);


    /* ----- VARIABLES ----- */
    private static final DbAdapter instance = new DbAdapter(AppState.getApplication());
    private final Context ctx;
    private static SQLiteDatabase db;
    private final DbHelper dbHelper;
    private volatile static int handleCount = 0;

    /* ----- CONSTRUCTORS ----- */
    public DbAdapter(Context context) {
        this.ctx = context;
        dbHelper = new DbHelper(ctx, DB_NAME, null, DB_VERSION);
    }

    /**
     * Intended for use only with BaseDb
     */
    // Package scoped
    static DbAdapter getInstance() {
        return instance;
    }

    /**
     * @return the {@link android.database.sqlite.SQLiteDatabase} for use with transaction handling
     */
    synchronized SQLiteDatabase getDatabase() {
        return db;
    }

	/* ----- CUSTOM METHODS ----- */
    // Db Operations

    /**
     * <p>Opens the {@code db} if it is not already open.</p>
     */
    public synchronized void open() {
        if (handleCount == 0) {
            db = dbHelper.getWritableDatabase();
            Log.d(LOG_TAG, "Opened database for writing.");
        }
        handleCount++;
        Log.d(LOG_TAG, String.format("Added Db handle. Adapter now reports %d handles.", handleCount));
    }

    /**
     * <p>Closes the {@code db} after all of the connections close.</p>
     */
    public synchronized void close() {
        handleCount--;
        Log.d(LOG_TAG, String.format("Removed Db handle. Adapter now reports %d handles.", handleCount));

        if (handleCount == 0 && db != null) {
            db.close();
            Log.d(LOG_TAG, "Released database to free memory.");
        }
    }

    // SQL Operations

    /**
     * <p>Creates {@code table} in the {@link #db} using the {@code create} sql.</p>
     *
     * @param table  the name of the table to be created
     * @param create the sql used to create the table
     */
    public void create(SQLiteDatabase db, String table, String create) {
        Log.d(LOG_TAG, String.format("Creating %s table.", table));
        db.execSQL(create);
    }

    /**
     * <p>Drops {@code table} in the {@link #db}.</p>
     *
     * @param table the table to be dropped
     */
    public void drop(SQLiteDatabase db, String table) {
        Log.d(LOG_TAG, String.format("Dropping %s table.", table));
        String drop = String.format("DROP TABLE IF EXISTS %s", table);
        db.execSQL(drop);
    }

    public final void beginTransaction() {
        db.beginTransactionNonExclusive();
    }

    public final void setTransactionSuccess() {
        db.setTransactionSuccessful();
    }

    public final void endTransaction() {
        db.endTransaction();
    }

    /**
     * <p>Runs an SQL query on {@code db} and returns a {@link Cursor} over the result set.</p>
     *
     * @param select the SQL select statement
     * @return the {@link Cursor} over the query results, positioned before the first entry
     * @see #selectf(String, Object...)
     */
    public Cursor select(String select) {
        Log.d(LOG_TAG, "Executing SQL: " + select);
        return db.rawQuery(select, null);
    }

    /**
     * <p>Works the same as {@link #select(String)}, except that it formats {@code format} and {@code args} using {@link String#format(String, Object...)}
     * into the SQL query first.</p>
     *
     * @param format the string format
     * @param args   the string arguments
     * @return the {@link Cursor} over the query results, positioned before the first entry
     * @see #select(String)
     */
    public Cursor selectf(String format, Object... args) {
        return this.select(String.format(format, args));
    }

    /**
     * <p>Inserts {@code values} into the table specified by {@code table}.
     *
     * @param table  the table name
     * @param values this map contains the initial column values for the row
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     * @see #updateIfExistsElseInsert(String, ContentValues, String)
     * @see #updateIfExistsElseInsert(String, ContentValues, ContentValues, String)
     */
    public long insert(String table, ContentValues values) {
        Log.d(LOG_TAG, String.format("Inserting %s into %s table.", values.toString(), table));
        return db.insert(table, null, values);
    }

    /**
     * <p>Updates {@code values} in the table specified by {@code table} for rows that meet the {@code where} criteria.
     *
     * @param table  the table name
     * @param values a map from column names to new column values
     * @param where  the optional WHERE clause to apply when updating, passing null will update all rows
     * @return the number of rows affected
     * @see #updateIfExistsElseInsert(String, ContentValues, String)
     * @see #updateIfExistsElseInsert(String, ContentValues, ContentValues, String)
     */
    public int update(String table, ContentValues values, String where) {
        Log.d(LOG_TAG, String.format("Updating table %s where %s with values %s.", table, where, values.toString()));
        return db.update(table, values, where, null);
    }

    /**
     * <p>Attempts to update using the {@code updateValues} and the {@code where} clause, if no rows are affected then it inserts the {@code insertValues} into
     * the table specified by {@code table}.</p>
     *
     * @param table        the table name
     * @param insertValues a map from column names to initial column values, if not already in the table
     * @param updateValues a map from column names to new column values, if already in the table
     * @param where        where the optional WHERE clause to apply when updating, passing null will update all rows
     * @return the id of the first row updated or the row inserted, -1 if an error occurred
     * @see #updateIfExistsElseInsert(String, ContentValues, String)
     */
    public long updateIfExistsElseInsert(String table, ContentValues insertValues, ContentValues updateValues, String where) {
        final int affected = update(table, updateValues, where);
        if (affected <= 0) {
            return insert(table, insertValues);
        } else {
            return affected;
            //			Cursor cursor = selectf("SELECT _id FROM %s WHERE %s", table, where);
            //			try {
            //				if(cursor.moveToFirst()) {
            //					return cursor.getLong(0);
            //				}
            //			} finally {
            //				cursor.close();
            //			}
        }
        //		return -1;
    }

    /**
     * <p>Works the same as {@link #updateIfExistsElseInsert(String, ContentValues, ContentValues, String)}, except that {@code insertValues} and {@code updateValues} are
     * the same.</p>
     *
     * @param table  the table name
     * @param values a map from column names to column values
     * @param where  where the optional WHERE clause to apply when updating, passing null will update all rows
     * @return the id of the first row updated or the row inserted, -1 if an error occurred
     * @see #updateIfExistsElseInsert(String, ContentValues, ContentValues, String)
     */
    public long updateIfExistsElseInsert(String table, ContentValues values, String where) {
        return updateIfExistsElseInsert(table, values, values, where);
    }

    /**
     * <p>Deletes all rows specified in the table specified by {@code table}.</p>
     *
     * @param table the table name
     * @return the number of rows affected
     * @see #delete(String, String)
     */
    public int delete(String table) {
        Log.d(LOG_TAG, String.format("Deleting all from table %s.", table));
        return db.delete(table, "1", null);
    }

    /**
     * <p>Works the same as {@link #delete(String)}, except that only rows that meet the {@code where} criteria are deleted.</p>
     *
     * @param table the table name
     * @param where the optional WHERE clause to apply when deleting, passing null will delete all rows
     * @return the number of rows affected
     * @see #delete(String)
     */
    public int delete(String table, String where) {
        Log.d(LOG_TAG, String.format("Deleting from table %s where %s.", table, where));
        return db.delete(table, where, null);
    }

    /**
     * <p>Gets the number of rows in the table.</p>
     *
     * @param table the table name
     * @return the number of rows in the table
     * @see #count(String, String)
     */
    public int count(String table) {
        return count(table, null);
    }

    /**
     * <p>Gets the number of rows in the table that meet the {@code where} criteria.</p>
     *
     * @param table the table name
     * @param where the optional WHERE clause to apply when getting the count, passing null will get the count for all rows
     * @return the number of rows in the table that meet the {@code where} criteria
     * @see #count(String)
     */
    public int count(String table, String where) {
        //		StringBuilder select = new StringBuilder(String.format("SELECT count(_id) FROM %s", table));
        String select = String.format("SELECT count(_id) FROM %s", table);
        if (!AppState.isNullOrEmpty(where)) {
            //			select.append(String.format(" WHERE %s", where));
            select += String.format(" WHERE %s", where);
        }

        Cursor cursor = db.rawQuery(select.toString(), null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                return 0;
            }
        } finally {
            cursor.close();
        }
    }

    public static void resetHandleCount() {
        handleCount = 0;
    }

    // shared preferences

    /**
     * <p>Gets the {@code field} preference from the {@link SharedPreferences}.</p>
     *
     * @param field the name of the preference to retrieve
     * @return the preference value if it exists, or empty string
     */
    public String prefs_select(String field) {
        SharedPreferences preferences = AppState.getApplication().getSharedPreferences(AppState.PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getString(field, "");
    }

    /**
     * <p>Stores the {@code values} in the {@link SharedPreferences}.</p>
     *
     * @param values a map of preference names to preference values
     * @return {@code true} if all of the {@code values} were successfully stored, {@code false} otherwise
     */
    public boolean prefs_insert(ContentValues values) {
        try {
            SharedPreferences preferences = AppState.getApplication().getSharedPreferences(AppState.PREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            for (Entry<String, Object> value : values.valueSet()) {
                editor.putString(value.getKey(), value.getValue().toString());
            }
            editor.apply();
        } catch (Exception e) {
            if (AppState.DEBUG) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    /**
     * <p>Deletes the specified preference from the {@link SharePreferences}.</p>
     *
     * @param field the name of the preference to delete
     * @return {@code true} if the preference was successfully deleted, {@code false} otherwise
     */
    public boolean prefs_delete(String field) {
        try {
            SharedPreferences preferences = AppState.getApplication().getSharedPreferences(AppState.PREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(field);
            editor.apply();
        } catch (Exception e) {
            if (AppState.DEBUG) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    /* ----- NESTED CLASSES ----- */
    private static class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            DbNews.create(db);

            Log.d(LOG_TAG, "Creating table: " + DbUpdated.UPDATED_TABLE);
            db.execSQL(UPDATED_CREATE);
            db.execSQL(String.format("CREATE INDEX %s ON %s(%s)", DbUpdated.SOURCE, DbUpdated.UPDATED_TABLE, DbUpdated.SOURCE));

            DbSchedule.create(db);

            DbEvent.create(db);

            DbSocial.create(db);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(LOG_TAG, "Upgrading from version " + oldVersion + " to " + newVersion + " which will destroy all old data.");

            db.enableWriteAheadLogging();

            final String drop = "DROP TABLE IF EXISTS ";

            DbNews.drop(db);

            execSQL(db, drop + DbUpdated.UPDATED_TABLE);

            //			execSQL(db, drop + DbSchedule.SCHED_TABLE);
            DbSchedule.drop(db);

            DbEvent.drop(db);


            onCreate(db);
        }

        private static void execSQL(SQLiteDatabase db, String sql) {
            Log.d(LOG_TAG, "Executing SQL: " + sql);
            db.execSQL(sql);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
            onCreate(db);
        }


    }
}