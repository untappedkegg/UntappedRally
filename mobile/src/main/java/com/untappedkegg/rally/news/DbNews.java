package com.untappedkegg.rally.news;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.R;
import com.untappedkegg.rally.data.BaseDbAccessor;
import com.untappedkegg.rally.util.DateManager;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public final class DbNews extends BaseDbAccessor {
    public static final String ID = "_id";

    public static final String NEWS_TABLE = "news";


    public static final String TITLE = "title";
    public static final String LINK = "link";
    public static final String DESCR = "descr";
    public static final String PUBDATE = "date";
    public static final String SOURCE = "source";
    public static final String SHORTDATE = "short_date";
    public static final String IMAGE_LINK = "img_link";
    public static final String STATUS = "status";

    // CREATE STATEMENTS
    private static final String NEWS_CREATE = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s varchar(6) DEFAULT 'Unread')", DbNews.NEWS_TABLE, DbNews.ID, DbNews.TITLE, DbNews.LINK, DbNews.DESCR, DbNews.PUBDATE, DbNews.SHORTDATE, DbNews.SOURCE, DbNews.IMAGE_LINK, DbNews.STATUS);

    public static void create(final SQLiteDatabase db) {
        Log.d(LOG_TAG, "Creating table: " + DbNews.NEWS_TABLE);
        db.execSQL(NEWS_CREATE);

        // INDICES
        db.execSQL(String.format(CREATE_INDEX, PUBDATE, NEWS_TABLE, PUBDATE));

    }

    public static void drop(final SQLiteDatabase db) {
        db.execSQL(DROP + DbNews.NEWS_TABLE);
    }

    // CREATE METHODS
    public static void news_insert(final String title, final String link, final String descr, final String date, final String shortDate, final String source, final String imgLink) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(TITLE, title);
        initialValues.put(LINK, link);
        initialValues.put(DESCR, descr);
        initialValues.put(PUBDATE, date);
        initialValues.put(SHORTDATE, shortDate);
        initialValues.put(SOURCE, source);
        initialValues.put(IMAGE_LINK, imgLink);

        String where = String.format("%s = '%s'", LINK, link);
        dbAdapter.updateIfExistsElseInsert(NEWS_TABLE, initialValues, where);
    }

    public static void deleteAllByUri(final String uri) {
        dbAdapter.delete(uri);

    }

    public static void deleteOldItems() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, -Integer.parseInt(AppState.getSettings().getString("pref_news_cutoff", "30")));
        Date daysBeforeDate = cal.getTime();
        String oldDate = DateManager.ISO8601_DATEONLY.format(daysBeforeDate);
        String where = String.format("%s < '%s'", PUBDATE, oldDate);
        dbAdapter.delete(NEWS_TABLE, where);

    }

    public static Cursor fetchAllNews() {
        return dbAdapter.select(String.format("SELECT * FROM %s ORDER BY %s DESC", NEWS_TABLE, PUBDATE));
    }

    public static void updateReadStatusById(final String id) {
        ContentValues values = new ContentValues();
        values.put(STATUS, AppState.getApplication().getResources().getString(R.string.news_read));

        final String where = String.format("%s = '%s'", ID, id);

        dbAdapter.update(NEWS_TABLE, values, where);
    }

    public static Cursor fetchCurrentEvents() {
        return dbAdapter.selectf("SELECT * FROM %s ORDER BY %s DESC LIMIT 5", NEWS_TABLE, PUBDATE);
    }

    public static Cursor fetchCarouselCurrentEvents() {
        return dbAdapter.selectf("SELECT * FROM %s WHERE %s != '' ORDER BY %s DESC LIMIT 5", NEWS_TABLE, IMAGE_LINK, PUBDATE);
    }

    public static Cursor fetchItemById(final String id) {
        return dbAdapter.selectf("SELECT * FROM %s WHERE %s = %d", NEWS_TABLE, ID, Integer.valueOf(id));
    }

}

