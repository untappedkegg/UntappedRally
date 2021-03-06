package com.untappedkegg.rally.event;

import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;

import com.untappedkegg.rally.AppState;
import com.untappedkegg.rally.BuildConfig;
import com.untappedkegg.rally.data.DbUpdated;
import com.untappedkegg.rally.data.NewDataFetcher;
import com.untappedkegg.rally.data.NewDataFetcher.Callbacks;
import com.untappedkegg.rally.data.NewDataFetcher.Fetcher;
import com.untappedkegg.rally.schedule.DbSchedule;
import com.untappedkegg.rally.util.DateManager;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventFetcher implements Fetcher {

    private static final EventFetcher instance = new EventFetcher(/*AppState.getApplication()*/);
//    private static Context ctx;
    private static final String LOG_TAG = instance.getClass().getSimpleName();

    private final List<AsyncTask<Void, Integer, Throwable>> tasks = new ArrayList<AsyncTask<Void, Integer, Throwable>>();


    public static EventFetcher getInstance() {
        return instance;
    }

    /* ----- INHERITED METHODS ----- */
    public void start(Callbacks callback, String link, String year) {
        tasks.add(NewDataFetcher.execute(new PhotoParser(callback, link, year)));
    }

    public void startDetails(Callbacks callback, String link) {
        tasks.add(NewDataFetcher.execute(new DetailsParser(callback, link)));
    }

    @Override
    public boolean isRunning() {
        return NewDataFetcher.listIsRunning(tasks);
    }

    @Override
    public void interrupt() {

    }


    private static class PhotoParser extends AsyncTask<Void, Integer, Throwable> {
        private final String function;
        private final Callbacks callback;
        private String link;
        private final String year;

        public PhotoParser(Callbacks callback, String link, String year) {
            this.callback = callback;
            this.function = "FUNC_PHOTO";
            this.link = link;
            this.year = year;
        }

        @Override
        protected Throwable doInBackground(Void... arg0) {
            Log.e(LOG_TAG, "Photo Parsing Started");
            DbUpdated.open();
            final String event = link.substring(link.lastIndexOf("/") + 1);
            link += "/photos";
            if (DateManager.timeBetweenInDays(DbUpdated.lastUpdated_by_Source(link)) > AppState.STAND_UPDATE_DELAY) {
                try {

                    Pattern pattern = Pattern.compile("<a href='/assets/(.*?)</a>", Pattern.CASE_INSENSITIVE);

                    HttpURLConnection conn = NewDataFetcher.get(link);

                    if (conn.getResponseCode() == 200) {
                        DbEvent.delete_photos(event, year);
                        DbUpdated.updated_insert(link);
                        Matcher matcher = pattern.matcher(NewDataFetcher.readStream(conn.getInputStream()));

                        while (matcher.find()) {
                            final String[] finds = matcher.group(0).replaceAll("'", "\"").split("\"");
                            DbEvent.photosInsert(finds[5], finds[1].replaceAll("/assets/", String.format("%s/assets/", AppState.RA_BASE_URL)), event, year);
                        }
                    }

                    conn.disconnect();
                } catch (Exception e) {
                    if(BuildConfig.DEBUG) {
                        Log.d(LOG_TAG, e.toString());
                        e.printStackTrace();
                    }
                    return e;
                } finally {
                    DbUpdated.close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Throwable result) {
            if(BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "Photo Parsing finished");
            }
            callback.onDataFetchComplete(result, function);
        }
    }


    private static class DetailsParser extends AsyncTask<Void, Integer, Throwable> {
        private final String function;
        private final Callbacks callback;
        private String link;

        public DetailsParser(Callbacks callback, String link) {
            this.callback = callback;
            this.function = "FUNC_DETAILS";
            this.link = link;
        }

        @Override
        protected Throwable doInBackground(Void... arg0) {

            DbUpdated.open();
            if (!DbSchedule.hasDetails(link)) {
                try {

                    Pattern pattern = Pattern.compile("<div class=\"event-details\">(.*?)</p>", Pattern.CASE_INSENSITIVE);

                    HttpURLConnection conn = NewDataFetcher.get(link);

                    if (conn.getResponseCode() == 200) {
                        DbUpdated.updated_insert(link);
                    }
                    Matcher matcher = pattern.matcher(NewDataFetcher.readStream(conn.getInputStream()));

                    while (matcher.find()) {

                        try {
                            // If there aren't any details, this may produce an IndexOutOfBoundsException
                            final String finds = matcher.group(0);
                            final String eventDetails = finds.substring(finds.indexOf("<p>"), finds.indexOf("</p>")).replace("<p>", "\n");

                            DbSchedule.insert_schedule_description(link, Html.fromHtml(eventDetails).toString());
                        } catch (Exception e) {
                            if (BuildConfig.DEBUG)
                                Log.w(LOG_TAG, e.toString());
                        }
                    }

                    conn.disconnect();
                } catch (Exception e) {
                    if (BuildConfig.DEBUG)
                        e.printStackTrace();
                    return e;
                } finally {
                    DbUpdated.close();
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Throwable result) {
            if(BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "Details Parsing finished");
            }
            callback.onDataFetchComplete(result, function);
        }
    }


}
