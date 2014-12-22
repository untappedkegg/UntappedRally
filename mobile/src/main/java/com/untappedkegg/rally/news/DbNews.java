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


public class DbNews extends BaseDbAccessor {
    public static final String ID = "_id";

    public static final String NEWS_TABLE = "news";

    // public static final String NAME = "name";
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

    public static final void create(SQLiteDatabase db) {
        Log.d(LOG_TAG, "Creating table: " + DbNews.NEWS_TABLE);
        db.execSQL(NEWS_CREATE);

        // INDICES
        db.execSQL(String.format(CREATE_INDEX, PUBDATE, NEWS_TABLE, PUBDATE));

    }

    public static final void drop(SQLiteDatabase db) {
        db.execSQL(DROP + DbNews.NEWS_TABLE);
    }

    // CREATE METHODS
    public static final void news_insert(String title, String link, String descr, String date, String shortDate, String source, String imgLink) {
        //		String source = "";
        //		//Switch to Author
        //		if (AppState.isNullOrEmpty(link)) {
        //			 source = "";
        //		} else if (link.startsWith("http://www.irally")) {
        //			source = AppState.IRALLY;
        //		} else if (link.startsWith("http://www.wrc.com")) {
        //			source = AppState.WRC_COM;
        //		} else if (link.startsWith("http://www.minimotorsport")) {
        //			source = AppState.WRC_COM;
        //		} else {
        //			source = AppState.CITROEN;
        //		}

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

    public static final void deleteAllByUri(String uri) {
        dbAdapter.delete(uri);

    }

    public static final void deleteOldItems() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, -AppState.NEWS_OLD_ITEM_CUTOFF);
        Date daysBeforeDate = cal.getTime();
        String oldDate = DateManager.ISO8601_DATEONLY.format(daysBeforeDate);//(DateManager.now(), DateManager.ISO8601_DATEONLY, -20, Calendar.DATE);
        String where = String.format("%s < '%s'", PUBDATE, oldDate);
        dbAdapter.delete(NEWS_TABLE, where);

    }

    public static final Cursor fetchAllNews() {

        String select = String.format("SELECT * FROM %s ORDER BY %s DESC", NEWS_TABLE, PUBDATE);

        return dbAdapter.select(select);

    }

    public static final void updateReadStatusById(String id) {
        ContentValues values = new ContentValues();
        values.put(STATUS, AppState.getApplication().getResources().getString(R.string.news_read));

        final String where = String.format("%s = '%s'", ID, id);

        dbAdapter.update(NEWS_TABLE, values, where);
    }

    public static final Cursor fetchCurrentEvents() {
        return dbAdapter.selectf("SELECT * FROM %s ORDER BY %s DESC LIMIT 5", NEWS_TABLE, PUBDATE);
    }

    public static final Cursor fetchCarouselCurrentEvents() {
        return dbAdapter.selectf("SELECT * FROM %s WHERE %s != '' ORDER BY %s DESC LIMIT 5", NEWS_TABLE, IMAGE_LINK, PUBDATE);
    }

    public static final Cursor fetchItemById(String id) {
        return dbAdapter.selectf("SELECT * FROM %s WHERE %s = %d", NEWS_TABLE, ID, Integer.valueOf(id));
    }

}

