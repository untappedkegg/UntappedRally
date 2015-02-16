package com.untappedkegg.rally;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.untappedkegg.rally.data.BaseDbAccessor;
import com.untappedkegg.rally.data.DbAdapter;
import com.untappedkegg.rally.schedule.DbSchedule;
import com.untappedkegg.rally.util.DateManager;

import java.io.File;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;


/**
 *
 * All Constants and literals go here
 * the only exception being Strings
 * (ones that are shown in the UI)
 *
 */
public class AppState extends Application {

    // Generic Keys
    public static final String KEY_SCROLL_X = "com.untappedkegg.rally.SCROLL_X";
    public static final String KEY_SCROLL_Y = "com.untappedkegg.rally.SCROLL_Y";
    public static final String KEY_URL = "com.untappedkegg.rally.URL";
    public static final String KEY_URI = "com.untappedkegg.rally.URI";
    public static final String KEY_ID = "com.untappedkegg.rally.ID";
    public static final String KEY_SHOULD_RECREATE = "com.untappedkegg.rally.RECREATE";
    public static final String KEY_ARGS = "com.untappedkegg.rally.ARGS";
    public static final String KEY_ISFINISHED = "com.untappedkegg.rally.ISFINISHED";
    public static final String KEY_BUNDLE = "com.untappedkegg.rally.ICICLE";

    public static final String KEY_RESTARTING = "com.untappedkegg.rally.RESTART";
    public static final String KEY_POSITION = "com.untappedkegg.rally.POSITION";

    // Market Package Names
    public static final String APP_LINK_GOOGLE = "market://details?id=";
    public static final String APP_LINK_AMAZON = "amzn://apps/android?p=";

    public static final String MARKET_GOOGLE = "com.android.vending";
    public static final String MARKET_AMAZON = "com.amazon.venezia";

    public static final String APP_LINK_AMAZON_ASIN = "amzn://apps/android?asin=B00RT90NAW";

    // About
    public static final String CHANGELOG_URL = "http://untappedkegg.com/android/rally/changelog.txt";
    public static final String SOCIAL_TWITTER = "https://twitter.com/untappedkegg";
    public static final String SOCIAL_G_PLUS = "https://google.com/+Untappedkegg/posts";

    // Concurrency
    public static boolean NEWS_REFRESH = false;
    public static final short REQUERY_WAIT = 500;
    public static final short STAGE_RESULT_DELAY = 60;
    public static final short STAND_UPDATE_DELAY = 1;
    public static final short YT_UPDATE_DELAY = 1;
    public static final short RSS_UPDATE_DELAY = 30; //Do not update on News if this amt of time (in minutes) has not elapsed
    public static final short CAL_UPDATE_DELAY = 7; //7 days


    // News Links
    public static final String RSS_IRALLY = "http://www.irallylive.com/rss/irally_news.xml";
    public static final String RSS_RALLY_MERICA = "http://rally-america.com/news/rss";

    // Event Specific News
    public static final HashMap<String, String> NEWS_MAP = new HashMap<String, String>() {{
       put (SOURCE_100AW, RSS_100AW);
       put(SOURCE_OREGON, RSS_OREGON);
       put( SOURCE_STPR, RSS_STPR );
       put( SOURCE_MTWA, RSS_MTWA);
       put( SOURCE_NEFR, RSS_NEFR);
       put( SOURCE_OFPR, RSS_OFPR);
       put( SOURCE_LSPR, RSS_LSPR);
       put(SOURCE_SHOME, RSS_SHOWME);

    }};

    public static final String RSS_100AW = "http://www.100aw.org/?feed=rss2";
    public static final String RSS_OREGON = "http://oregontrailrally.com/?feed=rss2";
    public static final String RSS_STPR = "http://www.stpr.org/feed/";
    public static final String RSS_MTWA = "http://climbtotheclouds.com/feed/";
    public static final String RSS_NEFR = "http://www.newenglandforestrally.com/feed/";
    //Ojibwe
    public static final String RSS_OFPR = "http://ojibweforestrally.com/feed";
    public static final String RSS_LSPR = "http://www.lsprorally.com/feed/";
    public static final String RSS_SHOWME = "http://showmerally.100aw.org/?feed=rss2";

    //Calendars
    public static final String EGG_CAL_XML = "http://untappedkegg.com/rally/db/xml/";
    public static final String EGG_DRAWABLE = "https://web.missouri.edu/~kpetg6/rally/drawable/";
//    public static final String EGG_DRAWABLE = "http://untappedkegg.com/rally/drawable/";

    public static final String RA_STANDINGS = "http://rally-america.com/champ_standings2?Endo=%s&Class=%s&Champ=0&yr=%s";
    public static final String RA_BASE_URL = "http://rally-america.com";

    //	public static final String YOUTUBE_RA = "http://gdata.youtube.com/feeds/base/users/RallyAmericaSeries/uploads?max-results=20&alt=rss&orderby=published";
    public static final String YOUTUBE_RA = "http://gdata.youtube.com/feeds/mobile/users/RallyAmericaSeries/uploads?max-results=20&orderby=published&format=1";


    // Sources
    public static final String SOURCE_IRALLY = "iRally";
    public static final String SOURCE_RALLY_AMERICA = "RA";
    public static final String SOURCE_100AW = "100aw";
    public static final String SOURCE_OREGON = "oregon";
    public static final String SOURCE_STPR = "stpr";
    public static final String SOURCE_MTWA = "mtwa";
    public static final String SOURCE_NEFR = "nefr";
    public static final String SOURCE_OFPR = "ofpr";
    public static final String SOURCE_LSPR = "lspr";
    public static final String SOURCE_SHOME = "smr";


    // Modules
    public static final String MOD_NEWS = "com.untappedkegg.rally.News";
    public static final String MOD_SCHED = "com.untappedkegg.rally.Schedule";
    public static final String MOD_STAND = "com.untappedkegg.rally.Standings";
    public static final String MOD_YOUTUBE = "com.untappedkegg.rally.YouTube";
    public static final String MOD_STAGES = "com.untappedkegg.rally.Stages";
    public static final String MOD_PICS = "com.untappedkegg.rally.Photos";

    public static final String FUNC_RA_STAND = "ra-standing";
    public static final String FUNC_STAGE_TIMES = "/stages/stage/%d";
    public static final String FUNC_STAGE_RESULTS = "/results/standings/%d";

    //SharedPreferences Fields
    public static final String PREFERENCES_NAME = "SharedPreferences";
    //Carry over
    public static final float CAROUSEL_SCROLL_DURATION_SECS = (float) .75;
    public static final short CAROUSEL_DELAY_SECS = 5;
    public static final short CAROUSEL_START_DELAY_SECS = 7;
//    public static final short LOCATION_UPDATE_INTERVAL_MINUTES = 10;

    public static final String RALLY_AMERICA_CSS = "<style type=\"text/css\"> a:link {color: #FDBC11;} a:visited {color: #FDBC11;}" +
            ".table th, .table td {border-top:0;border-bottom:1px solid #FFF; padding:5px}" +
            "\ntable.tablesorter tbody td{color: #fff; padding:10px; margin:0px; background-color: transparent; vertical-align: top;}\n" +
            "table.tablesorter tbody tr.odd td {background-color: transparent; }" +
            ".table-imported th { border-bottom: 5px solid #FDBC11; } tr.tablesorter-headerRow { font-size:18px; border-bottom: 2px solid #FDBC11; }\n" +
            "champ_standings {color:#aaa; margin-top:10px;} .table th, .table td {border-top: border-bottom:1px solid FFF\n} " +
            "a.errorBubble { color: #ff0000; font-weight: bold; }tr.odd { background-color: #222; }.errorBox { font-size: 10px; }\n " +//font-size: 108%;}
            "a.errorBubble.bad { color: #ff0000;} a.errorBubble.good { color: #00ff00;}\n" +
            ".shift-up, .shift-dn {font-size: 9px; background-image: url(http://rally-america.com/images/icons/shift.gif); " +
            "background-repeat: no-repeat; padding-left: 6px; } .shift-up { color: #6f6; background-position: top left;} " +
            ".shift-dn { color: #f66; background-position: bottom left;}" +
            "</style>";



    //	public static final Locale USERLOCALE;
    public static final Locale localeUser = Locale.getDefault();

    //time constants
//    public static final String DAY_START = " 00:00";
//    public static final String DAY_END = " 23:59";

    /* ----- VARIABLES ----- */
    private static Application instance;

    /* ----- CONSTRUCTORS ----- */
    public AppState() {
        instance = this;
    }

    /**
     * @return the global Application instance.
     */
    public static Application getApplication() {
        return instance;
    }

    public static final SharedPreferences getSettings() {
        return PreferenceManager.getDefaultSharedPreferences(instance);
    }

	/* CUSTOM METHODS */

    // Other

    /**
     * Tests {@code str} for a null or "".
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Case insensitive check if a String starts with a specified prefix.
     * <p/>
     * <code>null</code>s are handled without exceptions. Two <code>null</code>
     * references are considered to be equal. The comparison is case insensitive.
     * <p/>
     * <pre>
     * AppState.startsWithIgnoreCase(null, null)      = true
     * AppState.startsWithIgnoreCase(null, "abcdef")  = false
     * AppState.startsWithIgnoreCase("abc", null)     = false
     * AppState.startsWithIgnoreCase("abc", "abcdef") = true
     * AppState.startsWithIgnoreCase("abc", "ABCDEF") = true
     * </pre>
     *
     * @param str    the String to check, may be null
     * @param prefix the prefix to find, may be null
     * @return <code>true</code> if the String starts with the prefix, case insensitive, or
     * both <code>null</code>
     * @see java.lang.String#startsWith(String)
     * @since 2.4
     */
    public static boolean startsWithIgnoreCase(String str, String prefix) {
        if (str == null || prefix == null) {
            return (str == null && prefix == null);
        }
        if (prefix.length() > str.length()) {
            return false;
        }
        return str.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the default preferences. The third parameter indicates whether this should be done more than once
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
//        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);

        // Create global configuration and initialize ImageLoader with this configuration
        final DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.image_placeholder) // resource or drawable
                .showImageForEmptyUri(R.drawable.image_placeholder) // resource or drawable
                .showImageOnFail(R.drawable.ic_launcher_large) // resource or drawable
                .cacheInMemory(true).cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(750, true, true, false))
                .build();
        final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .threadPoolSize(4)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();

        ImageLoader.getInstance().init(config);

    }

    @Override
    public void onLowMemory() {
        ImageLoader.getInstance().clearMemoryCache();
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            ImageLoader.getInstance().clearMemoryCache();
        }
        super.onTrimMemory(level);
    }

    public static int screenWidth(Context ctx) {
        DisplayMetrics metrics = instance.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    public static int screenHeight(Context ctx) {
        DisplayMetrics metrics = instance.getResources().getDisplayMetrics();
        return metrics.heightPixels;

    }

    public static boolean clearDiskCache() {
        ImageLoader.getInstance().clearDiskCache();
        File[] extFiles = null;
        try {
            extFiles = instance.getExternalCacheDir().listFiles();
        } catch (NullPointerException e) {

        }
        final File[] files = instance.getCacheDir().listFiles();
        if (files == null && extFiles == null) {
            return false;
        }
        for (File file : files) {
            file.delete();
        }
        for (File file : extFiles) {
            file.delete();
        }
        return true;
    }

    /**
     * Deletes the Database. It should get re-created upon next call to {@link DbAdapter#open()}{@code open()}
     * but may result in inconsistent data depending on when it is called.
     *
     * @return {@code true} if the database was successfully deleted; else {@code false}.
     */
    public static boolean clearDatabase() {
        final boolean status = instance.deleteDatabase(DbAdapter.DB_NAME);
        if (status) {
            DbAdapter.resetHandleCount();
        }
        return status;
    }

    public static boolean clearData() {
        return clearDatabase() && clearDiskCache();
    }

    public static void setNextNotification() {
            final AlarmManager alarm = (AlarmManager) instance.getSystemService(Context.ALARM_SERVICE);
            BaseDbAccessor.open();
            try {
                final long diff = DateManager.parse(DbSchedule.fetchNextEventStart(), DateManager.ISO8601_DATEONLY).getTime();
                //The intent is declared in the manifest, if changed here it must also be changed there
                PendingIntent pendingIntent = PendingIntent.getBroadcast(instance, 0, new Intent("com.untappedkegg.rally.notification.NEXT_EVENT_RECEIVER"), PendingIntent.FLAG_UPDATE_CURRENT);
                alarm.set(AlarmManager.RTC, diff, pendingIntent);
            } catch (ParseException e) {
                e.printStackTrace();
            } finally {
                BaseDbAccessor.close();
            }
    }
}
